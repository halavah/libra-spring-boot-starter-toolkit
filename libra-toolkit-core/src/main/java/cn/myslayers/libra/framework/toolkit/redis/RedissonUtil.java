package cn.myslayers.libra.framework.toolkit.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScoredValueScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.ValueScanCursor;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RLock;
import org.redisson.api.RStream;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis 工具类 - 企业级 Redis 操作工具
 * 1. 懒加载机制</b>：连接池采用懒加载模式，仅在首次使用时初始化，提升应用启动速度
 * 2. 多数据源支持</b>：支持同时连接多个 Redis 实例，提供全局和线程级数据源切换
 * 3. 双 API 模式</b>：提供静态 API（简单操作）和链式访问器（事务支持）两种使用方式
 * 4. 自动资源管理</b>：实现 AutoCloseable 接口，支持 try-with-resources 自动资源释放
 * 5. 事务支持</b>：完整支持 Redis 事务（MULTI/EXEC/DISCARD）和乐观锁（WATCH/UNWATCH）
 * 6. 分布式锁</b>：基于 Redisson 提供可重入锁、公平锁、读写锁等多种锁实现
 */
public class RedissonUtil {

    // ==================== 全局核心配置 =============================

    // 全局调试日志开关
    private static volatile boolean DEBUG_LOG_ENABLED = false;

    // 全局调试日志的启用状态
    public static void setDebugLogEnabled(boolean enabled) {
        DEBUG_LOG_ENABLED = enabled;
        LoggerCore.log("INFO", "RedisUtil 调试日志已 " + (enabled ? "开启" : "关闭"));
    }

    // 数据源连接信息
    private static final String[][] REDIS_MANAGE_CONNECTIONS = {
        // 数据源标识：唯一标识符，用于在代码中引用该数据源
        // 主机地址：Redis 服务器的 IP 地址或域名
        // 端口号：Redis 服务器的端口号，默认 6379
        // 密码：Redis 认证密码，无密码时使用空字符串 ""
        // 数据库索引：Redis 数据库编号，范围 0-15
        // 最大连接数：连接池允许的最大连接数
        // 最大空闲连接：连接池中保持的最大空闲连接数
        // 最小空闲连接：连接池中保持的最小空闲连接数

        // 默认数据源：主业务数据库
        {"default_redis", "127.0.0.1", "6379", "", "0", "8", "8", "0"},
        // 测试数据源：用于单元测试和功能验证
        {"db1_for_test", "127.0.0.1", "6379", "", "1", "8", "8", "0"}
    };

    // 顶层静态初始化块
    static {
        try {
            // 1. 首先，加载并执行 RedisDataCore 的静态初始化，注册所有数据源
            for (String[] configArray : REDIS_MANAGE_CONNECTIONS) {
                try {
                    RedisDataCore.register(
                        configArray[0],                          // key
                        configArray[1],                          // host
                        Integer.parseInt(configArray[2]),        // port
                        configArray[3],                          // password
                        Integer.parseInt(configArray[4]),        // database
                        Integer.parseInt(configArray[5]),        // maxTotal
                        Integer.parseInt(configArray[6]),        // maxIdle
                        Integer.parseInt(configArray[7])         // minIdle
                    );
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "加载Redis配置失败: " + String.join(",", configArray));
                }
            }
            if (RedisDataCore.getDefaultKey() != null) {
                LoggerCore.log("INFO", "默认Redis数据源已设置为: " + RedisDataCore.getDefaultKey());
            }

            // 2. 然后，强制加载其他核心组件
            Class.forName(LettuceCore.class.getName());
            Class.forName(RedissonCore.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法初始化RedisUtil核心组件", e);
        }
    }

    // ==================== Core：核心配置与连接器 ====================

    // 内部日志记录器核心
    public static class LoggerCore {

        // SLF4J 日志记录器实例
        private static final Logger logger = LoggerFactory.getLogger(LoggerCore.class);

        /**
         * 记录日志信息
         *
         * <p>根据全局调试开关 {@link #DEBUG_LOG_ENABLED} 决定是否输出日志。
         * 日志格式：[级别] 时间戳 消息内容
         *
         * @param level   日志级别，支持 "ERROR"、"WARN"、"INFO"
         * @param message 日志消息内容
         * @see #setDebugLogEnabled(boolean)
         */
        public static void log(String level, String message) {
            if (DEBUG_LOG_ENABLED) {
                String logMessage = String.format("[%s] %s %s", level,
                    java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                    message);
                if ("ERROR".equals(level)) {
                    logger.error(logMessage);
                } else if ("WARN".equals(level)) {
                    logger.warn(logMessage);
                } else {
                    logger.info(logMessage);
                }
            }
        }
    }

    // Redis 数据源配置与管理核心
    public static class RedisDataCore {

        // ==================== 实例字段 ====================

        // 数据源唯一标识符
        final String key;

        // Redis 服务器主机地址
        final String host;

        // Redis 服务器端口号
        final int port;

        // Redis 认证密码
        final String password;

        // Redis 数据库索引
        final int database;

        // 连接池最大连接数
        final int maxTotal;

        // 连接池最大空闲连接数
        final int maxIdle;

        // 连接池最小空闲连接数
        final int minIdle;

        // ==================== 静态管理字段 ====================

        // 全局数据源配置注册表，线程安全
        private static final Map<String, RedisDataCore> redisConfigs = new ConcurrentHashMap<>();

        // 全局默认数据源标识符，原子操作保证线程安全
        private static final AtomicReference<String> ATOMIC_DEFAULT_SOURCE = new AtomicReference<>();

        // ==================== 构造函数 ====================

        /**
         * 私有构造函数，创建 Redis 数据源配置实例
         *
         * @param key      数据源唯一标识符
         * @param host     Redis 服务器主机地址
         * @param port     Redis 服务器端口号
         * @param password Redis 认证密码，无密码时传入空字符串
         * @param database Redis 数据库索引，范围 0-15
         * @param maxTotal 连接池最大连接数
         * @param maxIdle  连接池最大空闲连接数
         * @param minIdle  连接池最小空闲连接数
         */
        private RedisDataCore(String key, String host, int port, String password, int database, int maxTotal, int maxIdle, int minIdle) {
            this.key = key;
            this.host = host;
            this.port = port;
            this.password = password;
            this.database = database;
            this.maxTotal = maxTotal;
            this.maxIdle = maxIdle;
            this.minIdle = minIdle;
        }

        // ==================== 静态管理方法 ====================

        /**
         * 根据标识符获取数据源配置
         *
         * @param key 数据源标识符
         * @return 对应的数据源配置实例，如果不存在则返回 null
         */
        public static RedisDataCore getConfig(String key) {
            return redisConfigs.get(key);
        }

        /**
         * 获取当前全局默认数据源的标识符
         *
         * @return 默认数据源标识符，如果未设置则返回 null
         */
        public static String getDefaultKey() {
            return ATOMIC_DEFAULT_SOURCE.get();
        }

        /**
         * 设置全局默认数据源
         *
         * <p>设置后，所有未明确指定数据源的操作都将使用此数据源。
         * 此操作是线程安全的，会立即对所有线程生效。
         *
         * @param key 要设置为默认的数据源标识符
         * @throws IllegalArgumentException 如果指定的数据源不存在
         */
        public static void setDefaultKey(String key) {
            if (redisConfigs.containsKey(key)) {
                ATOMIC_DEFAULT_SOURCE.set(key);
                LoggerCore.log("INFO", "默认Redis数据源已动态设置为: " + key);
            } else {
                LoggerCore.log("WARN", "尝试设置的默认数据源 '" + key + "' 不存在。");
            }
        }

        /**
         * 注册新的 Redis 数据源（使用默认连接池参数）
         *
         * <p>使用默认的连接池配置：最大连接数8，最大空闲连接数8，最小空闲连接数0。
         * 适用于大多数场景的快速配置。
         *
         * @param key      数据源唯一标识符
         * @param host     Redis 服务器主机地址
         * @param port     Redis 服务器端口号
         * @param password Redis 认证密码，无密码时传入空字符串
         * @param database Redis 数据库索引，范围 0-15
         * @return 新创建的数据源配置实例
         * @see #register(String, String, int, String, int, int, int, int)
         */
        public static RedisDataCore register(String key, String host, int port, String password, int database) {
            return register(key, host, port, password, database, 8, 8, 0);
        }

        /**
         * 注册新的 Redis 数据源（完整参数版本）
         *
         * <p>动态注册一个新的 Redis 数据源到全局配置中。如果是第一个注册的数据源，
         * 将自动设置为默认数据源。如果指定的标识符已存在，将覆盖原有配置。
         *
         * <h4>连接池参数建议</h4>
         * <ul>
         *   <li><b>高并发场景</b>：maxTotal=20, maxIdle=10, minIdle=5</li>
         *   <li><b>一般业务场景</b>：maxTotal=8, maxIdle=8, minIdle=2</li>
         *   <li><b>轻量级应用</b>：maxTotal=5, maxIdle=3, minIdle=1</li>
         * </ul>
         *
         * @param key      数据源唯一标识符，不能为空
         * @param host     Redis 服务器主机地址
         * @param port     Redis 服务器端口号，通常为 6379
         * @param password Redis 认证密码，无密码时传入空字符串
         * @param database Redis 数据库索引，范围 0-15
         * @param maxTotal 连接池最大连接数，建议 5-50
         * @param maxIdle  连接池最大空闲连接数，建议不超过 maxTotal
         * @param minIdle  连接池最小空闲连接数，建议 0-5
         * @return 新创建的数据源配置实例
         * @throws IllegalArgumentException 如果参数不合法
         */
        public static RedisDataCore register(String key, String host, int port, String password, int database, int maxTotal, int maxIdle, int minIdle) {
            if (redisConfigs.containsKey(key)) {
                LoggerCore.log("WARN", "Redis数据源 '" + key + "' 已存在，将进行覆盖。");
            }
            RedisDataCore newSource = new RedisDataCore(key, host, port, password, database, maxTotal, maxIdle, minIdle);
            redisConfigs.put(key, newSource);
            ATOMIC_DEFAULT_SOURCE.compareAndSet(null, key); // 如果是第一个，则设为默认
            LoggerCore.log("INFO", "已动态注册Redis数据源: " + key);
            return newSource;
        }

        // ==================== 实例链式方法 ====================

        /**
         * 将当前数据源设置为全局默认数据源，并返回对应的访问器
         *
         * <p>此操作会影响所有线程的默认数据源设置，适用于应用级别的数据源切换。
         *
         * @return 操作当前数据源的 RedisAccessor 实例
         * @see #setDefaultKey(String)
         */
        public RedisAccessor setDefaultDataSource() {
            setDefaultKey(this.key);
            return new RedisAccessor(this.key);
        }

        /**
         * 将当前数据源设置为线程级数据源，并返回对应的访问器
         *
         * <p>此操作仅影响当前线程，不会影响其他线程的数据源设置。
         * 使用完毕后应调用 {@link LettuceCore#clear()} 清除线程上下文。
         *
         * @return 操作当前数据源的 RedisAccessor 实例
         * @see LettuceCore#use(String)
         * @see LettuceCore#clear()
         */
        public RedisAccessor setThreadDataSourceContext() {
            LettuceCore.use(this.key);
            return new RedisAccessor(this.key);
        }

        /**
         * 设置指定数据源为全局默认数据源，并返回对应的访问器
         *
         * @param key 要设置为默认的数据源标识符
         * @return 操作指定数据源的 RedisAccessor 实例
         * @deprecated 此方法设计有误，建议使用 {@link #setDefaultDataSource()}
         */
        public RedisAccessor setDefaultDataSource(String key) {
            return new RedisAccessor(key);
        }

        /**
         * 设置指定数据源为线程级数据源，并返回对应的访问器
         *
         * @param key 要设置为线程级的数据源标识符
         * @return 操作指定数据源的 RedisAccessor 实例
         * @deprecated 此方法设计有误，建议使用 {@link #setThreadDataSourceContext()}
         */
        public RedisAccessor setThreadDataSourceContext(String key) {
            return new RedisAccessor(key);
        }

        /**
         * 直接获取操作当前数据源的访问器
         *
         * <p>这是最常用的方法，用于获取一个操作当前数据源的 RedisAccessor 实例。
         *
         * @return 操作当前数据源的 RedisAccessor 实例
         */
        public RedisAccessor accessor() {
            return new RedisAccessor(this.key);
        }
    }

    // Lettuce 数据源管理核心（懒加载连接池管理器）
    public static class LettuceCore {

        // 连接池注册表，每个数据源对应一个独立的连接池
        private static final Map<String, GenericObjectPool<StatefulRedisConnection<String, String>>> connectionPools = new ConcurrentHashMap<>();

        // 线程级数据源上下文，用于临时切换数据源
        private static final ThreadLocal<String> currentDatabase = new ThreadLocal<>();

        /**
         * 获取当前生效数据源的 Lettuce 连接
         *
         * <p>根据线程上下文和全局默认设置，自动选择合适的数据源并返回连接。
         * 优先级：线程级设置 > 全局默认设置。
         *
         * @return Lettuce Redis 连接实例
         * @throws RuntimeException 如果获取连接失败
         * @see #getEffectiveDbKey()
         * @see #getConnection(String)
         */
        public static StatefulRedisConnection<String, String> getConnection() {
            return getConnection(getEffectiveDbKey());
        }

        /**
         * 获取指定数据源的 Lettuce 连接
         *
         * <p>使用懒加载策略，如果指定数据源的连接池尚未初始化，将自动创建。
         * 采用双重检查锁定模式确保线程安全的初始化过程。
         *
         * <h4>连接池管理</h4>
         * <ul>
         *   <li>每个数据源维护独立的连接池</li>
         *   <li>连接池参数基于数据源注册时的配置</li>
         *   <li>支持连接的自动验证和故障恢复</li>
         * </ul>
         *
         * @param key 数据源标识符
         * @return Lettuce Redis 连接实例
         * @throws RuntimeException 如果数据源不存在或连接失败
         * @see #initializePool(String)
         */
        public static StatefulRedisConnection<String, String> getConnection(String key) {
            // 双重检查锁定 (Double-Checked Locking)
            if (!connectionPools.containsKey(key)) {
                synchronized (LettuceCore.class) {
                    if (!connectionPools.containsKey(key)) {
                        initializePool(key);
                    }
                }
            }

            try {
                return connectionPools.get(key).borrowObject();
            } catch (Exception e) {
                LoggerCore.log("ERROR", "获取Lettuce连接失败: " + e.getMessage());
                throw new RuntimeException("获取Lettuce连接失败", e);
            }
        }

        /**
         * 初始化指定数据源的连接池（私有方法）
         *
         * <p>执行连接池的完整初始化流程，包括连接验证、连接池配置和注册。
         * 此方法在双重检查锁定的保护下调用，确保线程安全。
         *
         * <h4>初始化步骤</h4>
         * <ol>
         *   <li>获取数据源配置信息</li>
         *   <li>构建 Redis URI 连接字符串</li>
         *   <li>创建 Lettuce 客户端实例</li>
         *   <li>执行连接验证（PING 测试）</li>
         *   <li>配置连接池参数</li>
         *   <li>创建并注册连接池</li>
         * </ol>
         *
         * <h4>连接池配置</h4>
         * <p>连接池参数基于数据源注册时的配置：
         * <ul>
         *   <li><b>maxTotal</b>：连接池最大连接数</li>
         *   <li><b>maxIdle</b>：连接池最大空闲连接数</li>
         *   <li><b>minIdle</b>：连接池最小空闲连接数</li>
         * </ul>
         *
         * @param key 数据源标识符
         * @throws RuntimeException 如果数据源配置不存在或初始化失败
         * @see RedisDataCore#getConfig(String)
         * @see ConnectionPoolSupport#createGenericObjectPool
         */
        private static void initializePool(String key) {
            LoggerCore.log("INFO", "首次访问，开始初始化Lettuce数据源: " + key);
            RedisDataCore config = RedisDataCore.getConfig(key);
            if (config == null) {
                throw new RuntimeException("未找到名为 '" + key + "' 的Redis配置。");
            }

            try {
                // 构建 Redis URI
                RedisURI.Builder builder = RedisURI.builder()
                    .withHost(config.host)
                    .withPort(config.port)
                    .withDatabase(config.database);
                if (config.password != null && !config.password.isEmpty()) {
                    builder.withPassword(config.password.toCharArray());
                }
                RedisClient redisClient = RedisClient.create(builder.build());

                // 连接验证
                try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
                    String pong = connection.sync().ping();
                    if (!"PONG".equalsIgnoreCase(pong)) {
                        throw new RuntimeException("Lettuce连接 " + key + " PING失败，响应: " + pong);
                    }
                    LoggerCore.log("INFO", "Lettuce数据源 " + key + " PING成功");
                }

                // 配置连接池
                GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
                poolConfig.setMaxTotal(config.maxTotal);
                poolConfig.setMaxIdle(config.maxIdle);
                poolConfig.setMinIdle(config.minIdle);

                // 创建并注册连接池
                GenericObjectPool<StatefulRedisConnection<String, String>> pool =
                    ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, poolConfig);
                connectionPools.put(key, pool);
                LoggerCore.log("INFO", "Lettuce数据源 " + key + " 初始化并验证成功");
            } catch (Exception e) {
                LoggerCore.log("ERROR", "初始化Lettuce数据源 " + key + " 失败: " + e.getMessage());
                throw new RuntimeException("无法初始化Lettuce数据源: " + key, e);
            }
        }

        /**
         * 设置当前线程的数据源上下文
         *
         * <p>将指定的数据源设置为当前线程的临时数据源。此设置仅对当前线程有效，
         * 不会影响其他线程的数据源选择。使用完毕后应调用 {@link #clear()} 清除设置。
         *
         * <h4>使用场景</h4>
         * <ul>
         *   <li>临时访问不同的 Redis 数据库</li>
         *   <li>在特定业务流程中切换数据源</li>
         *   <li>多租户应用的数据隔离</li>
         * </ul>
         *
         * <h4>最佳实践</h4>
         * <pre>{@code
         * try {
         *     LettuceCore.use("cache_db");
         *     // 执行需要特定数据源的操作
         * } finally {
         *     LettuceCore.clear(); // 必须清除，避免线程池污染
         * }
         * }</pre>
         *
         * @param key 要设置的数据源标识符
         * @see #clear()
         * @see #getEffectiveDbKey()
         */
        public static void use(String key) {
            LoggerCore.log("INFO", "切换Redis数据源到: " + key);
            currentDatabase.set(key);
        }

        /**
         * 清除当前线程的数据源上下文设置
         *
         * <p>移除当前线程的临时数据源设置，使其恢复使用全局默认数据源。
         * 此方法必须在 finally 块中调用，以防止线程池中的线程状态污染。
         *
         * <h4>重要性</h4>
         * <p>在使用线程池的环境中（如 Web 应用），线程会被重复使用。
         * 如果不清除 ThreadLocal 设置，可能导致后续请求使用错误的数据源。
         *
         * @see #use(String)
         */
        public static void clear() {
            LoggerCore.log("INFO", "清除Redis数据源设置，恢复默认");
            currentDatabase.remove();
        }

        /**
         * 获取当前生效的数据源标识符
         *
         * <p>根据优先级返回当前应该使用的数据源标识符：
         * <ol>
         *   <li>线程级设置（通过 {@link #use(String)} 设置）</li>
         *   <li>全局默认设置（通过 {@link RedisDataCore#setDefaultKey(String)} 设置）</li>
         * </ol>
         *
         * @return 当前生效的数据源标识符，如果都未设置则返回 null
         * @see #use(String)
         * @see RedisDataCore#getDefaultKey()
         */
        public static String getEffectiveDbKey() {
            String key = currentDatabase.get();
            return key != null ? key : RedisDataCore.getDefaultKey();
        }

        /**
         * 将连接归还到指定数据源的连接池
         *
         * <p>将使用完毕的 Lettuce 连接归还到对应的连接池中，以供后续复用。
         * 此方法通常由 RedisAccessor 的 close() 方法自动调用，开发者无需手动调用。
         *
         * <h4>异常处理</h4>
         * <p>如果归还过程中发生异常，会记录错误日志但不会抛出异常，
         * 以避免影响业务逻辑的正常执行。
         *
         * @param key        数据源标识符
         * @param connection 要归还的连接实例
         * @see RedisAccessor#close()
         */
        public static void returnConnection(String key, StatefulRedisConnection<String, String> connection) {
            if (connectionPools.containsKey(key)) {
                try {
                    connectionPools.get(key).returnObject(connection);
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "归还Lettuce连接失败: " + e.getMessage());
                }
            }
        }
    }

    // Redisson 客户端管理核心（懒加载分布式功能提供者）
    public static class RedissonCore {

        // Redisson 客户端注册表，每个数据源对应一个独立的客户端
        private static final Map<String, RedissonClient> redissonClients = new ConcurrentHashMap<>();

        /**
         * 获取当前生效数据源的 Redisson 客户端
         *
         * <p>根据线程上下文和全局默认设置，自动选择合适的数据源并返回 Redisson 客户端。
         * 优先级：线程级设置 > 全局默认设置。
         *
         * @return Redisson 客户端实例
         * @throws RuntimeException 如果获取客户端失败
         * @see LettuceCore#getEffectiveDbKey()
         * @see #getClient(String)
         */
        public static RedissonClient getClient() {
            return getClient(LettuceCore.getEffectiveDbKey());
        }

        /**
         * 获取指定数据源的 Redisson 客户端
         *
         * <p>使用懒加载策略，如果指定数据源的客户端尚未初始化，将自动创建。
         * 采用双重检查锁定模式确保线程安全的初始化过程。
         *
         * <h4>客户端管理</h4>
         * <ul>
         *   <li>每个数据源维护独立的 Redisson 客户端</li>
         *   <li>客户端配置基于数据源注册时的参数</li>
         *   <li>支持客户端的自动验证和故障检测</li>
         * </ul>
         *
         * @param key 数据源标识符
         * @return Redisson 客户端实例
         * @throws RuntimeException 如果数据源不存在或客户端初始化失败
         * @see #initializeClient(String)
         */
        public static RedissonClient getClient(String key) {
            if (!redissonClients.containsKey(key)) {
                synchronized (RedissonCore.class) {
                    if (!redissonClients.containsKey(key)) {
                        initializeClient(key);
                    }
                }
            }
            return redissonClients.get(key);
        }

        /**
         * 初始化指定数据源的 Redisson 客户端（私有方法）
         *
         * <p>执行 Redisson 客户端的完整初始化流程，包括配置构建、客户端创建、
         * 连接验证和注册。此方法在双重检查锁定的保护下调用，确保线程安全。
         *
         * <h4>初始化步骤</h4>
         * <ol>
         *   <li>获取数据源配置信息</li>
         *   <li>构建 Redisson 配置对象</li>
         *   <li>配置单服务器连接参数</li>
         *   <li>创建 Redisson 客户端实例</li>
         *   <li>执行连接验证（PING 测试）</li>
         *   <li>注册客户端到全局映射表</li>
         * </ol>
         *
         * <h4>连接配置</h4>
         * <ul>
         *   <li><b>连接超时</b>：10秒，适应网络延迟</li>
         *   <li><b>PING 间隔</b>：禁用，减少网络开销</li>
         *   <li><b>密码认证</b>：根据数据源配置自动设置</li>
         *   <li><b>数据库选择</b>：支持 Redis 多数据库</li>
         * </ul>
         *
         * <h4>故障处理</h4>
         * <p>如果初始化失败，会自动清理已创建的资源，并抛出详细的异常信息。
         *
         * @param key 数据源标识符
         * @throws RuntimeException 如果数据源配置不存在或初始化失败
         * @see RedisDataCore#getConfig(String)
         * @see Redisson#create(Config)
         */
        private static void initializeClient(String key) {
            LoggerCore.log("INFO", "首次访问，开始初始化Redisson数据源: " + key);
            RedisDataCore config = RedisDataCore.getConfig(key);
            if (config == null) {
                throw new RuntimeException("未找到名为 '" + key + "' 的Redis配置。");
            }

            try {
                // 构建 Redisson 配置
                Config redissonConfig = new Config();
                org.redisson.config.SingleServerConfig singleServerConfig = redissonConfig.useSingleServer()
                    .setAddress("redis://" + config.host + ":" + config.port)
                    .setDatabase(config.database)
                    .setConnectTimeout(10000)  // 10秒连接超时
                    .setPingConnectionInterval(0);  // 禁用PING间隔

                // 设置密码认证
                if (config.password != null && !config.password.isEmpty()) {
                    singleServerConfig.setPassword(config.password);
                }

                // 创建客户端
                RedissonClient redissonClient = Redisson.create(redissonConfig);

                // 连接验证
                if (!redissonClient.getNodesGroup().pingAll()) {
                    redissonClient.shutdown();
                    throw new RuntimeException("Redisson连接 " + key + " PING失败");
                }
                LoggerCore.log("INFO", "Redisson数据源 " + key + " PING成功");

                // 注册客户端
                redissonClients.put(key, redissonClient);
                LoggerCore.log("INFO", "Redisson数据源 " + key + " 初始化并验证成功");
            } catch (Exception e) {
                LoggerCore.log("ERROR", "初始化Redisson数据源 " + key + " 失败: " + e.getMessage());
                throw new RuntimeException("无法初始化Redisson数据源: " + key, e);
            }
        }
    }

    // Redis 发布/订阅消息监听器接口
    public interface MessageListener<T> {

        /**
         * 处理接收到的消息
         *
         * @param channel 消息来源频道
         * @param msg     接收到的消息内容
         */
        void onMessage(CharSequence channel, T msg);
    }

    // ==================== Extend：链式API =========================

    // Redis 链式访问器
    public static class RedisAccessor implements AutoCloseable {

        // 当前访问器使用的数据源标识符
        final String dbKey;

        // 持有的 Redis 连接实例，在整个生命周期内复用
        private final StatefulRedisConnection<String, String> connection;

        // Redis 命令执行器
        private RedisCommands<String, String> commands;

        // 事务状态标识
        private boolean inTransaction = false;

        // ==================== 操作器实例 ====================

        // 键操作器：处理 Redis 键的通用操作
        public final KeyOps KeyOps = new KeyOps();

        // 字符串操作器：处理 Redis String 类型数据
        public final StringOps StringOps = new StringOps();

        // 哈希操作器：处理 Redis Hash 类型数据
        public final HashOps HashOps = new HashOps();

        // 列表操作器：处理 Redis List 类型数据
        public final ListOps ListOps = new ListOps();

        // 集合操作器：处理 Redis Set 类型数据
        public final SetOps SetOps = new SetOps();

        // 有序集合操作器：处理 Redis ZSet 类型数据
        public final ZSetOps ZSetOps = new ZSetOps();

        // 位图操作器：处理 Redis Bitmap 操作
        public final BitmapOps BitmapOps = new BitmapOps();

        // 可重入锁操作器：基于 Redisson 的基础分布式锁功能
        public final ReentrantLockOps ReentrantLockOps = new ReentrantLockOps();

        // 公平锁操作器：基于 Redisson 的公平锁功能
        public final FairLockOps FairLockOps = new FairLockOps();

        // 读写锁操作器：基于 Redisson 的读写锁功能
        public final ReadWriteLockOps ReadWriteLockOps = new ReadWriteLockOps();

        // 信号量操作器：基于 Redisson 的信号量功能
        public final SemaphoreOps SemaphoreOps = new SemaphoreOps();

        // 倒计时器操作器：基于 Redisson 的倒计时器功能
        public final CountDownLatchOps CountDownLatchOps = new CountDownLatchOps();

        // 服务器管理操作器：Redis 服务器管理命令
        public final ServerOps ServerOps = new ServerOps();

        // 发布/订阅操作器：Redis Pub/Sub 功能
        public final PubSubOps PubSubOps = new PubSubOps();

        // 脚本操作器：Lua 脚本执行功能
        public final ScriptOps ScriptOps = new ScriptOps();

        // HyperLogLog 操作器：基数估算功能
        public final HyperLogLogOps HyperLogLogOps = new HyperLogLogOps();

        // 地理空间操作器：地理位置相关功能
        public final GeoOps GeoOps = new GeoOps();

        // 流操作器：Redis Stream 数据结构
        public final StreamOps StreamOps = new StreamOps();

        // 布隆过滤器操作器：概率数据结构
        public final BloomFilterOps BloomFilterOps = new BloomFilterOps();

        // 创建 Redis 访问器实例
        private RedisAccessor(String dbKey) {
            this.dbKey = (dbKey != null) ? dbKey : LettuceCore.getEffectiveDbKey();
            // 在构造时获取一个连接并持有它
            this.connection = LettuceCore.getConnection(this.dbKey);
            this.commands = this.connection.sync();
        }

        // ==================== 事务和乐观锁 API ====================

        /**
         * 监视一个或多个键，用于实现乐观锁
         *
         * <p>WATCH 命令用于监视一个或多个键，如果在事务执行之前这些键被其他命令修改，
         * 那么事务将被打断。这是 Redis 实现乐观锁的机制。
         *
         * <h4>使用场景</h4>
         * <ul>
         *   <li>防止并发修改导致的数据不一致</li>
         *   <li>实现基于版本的乐观锁</li>
         *   <li>确保事务执行前的数据完整性</li>
         * </ul>
         *
         * <h4>使用示例</h4>
         * <pre>{@code
         * try (RedisAccessor accessor = RedisUtil.accessor()) {
         *     accessor.watch("balance:1001");  // 监视余额
         *     String balance = accessor.StringOps.get("balance:1001");
         *     if (Integer.parseInt(balance) >= 100) {
         *         accessor.multi();
         *         accessor.StringOps.decrBy("balance:1001", 100);
         *         accessor.StringOps.incrBy("balance:1002", 100);
         *         List<Object> results = accessor.exec();
         *         if (results == null) {
         *             System.out.println("转账失败：余额被其他操作修改");
         *         }
         *     }
         * }
         * }</pre>
         *
         * @param keys 要监视的键名数组
         * @return 服务器响应，通常为 "OK"
         * @throws IllegalStateException 如果在事务开始后调用此方法
         * @see #multi()
         * @see #exec()
         * @see #unwatch()
         */
        public String watch(String... keys) {
            if (inTransaction) {
                throw new IllegalStateException("不能在事务开始后调用 watch()");
            }
            return this.commands.watch(keys);
        }

        /**
         * 取消对所有键的监视
         *
         * <p>取消 WATCH 命令对所有键的监视。如果在执行 WATCH 命令之后，
         * EXEC 命令或 DISCARD 命令先被执行了的话，那么就不需要再执行 UNWATCH 了。
         *
         * @return 服务器响应，通常为 "OK"
         * @see #watch(String...)
         */
        public String unwatch() {
            if (inTransaction) {
                LoggerCore.log("WARN", "在事务中调用 unwatch() 通常没有效果，事务结束后会自动 unwatch。");
            }
            return this.commands.unwatch();
        }

        /**
         * 开启 Redis 事务
         *
         * <p>标记一个事务块的开始。事务块内的多条命令会被放入队列中，
         * 当 EXEC 命令被调用时，所有命令会被原子性地执行。
         *
         * <h4>事务特性</h4>
         * <ul>
         *   <li><b>原子性</b>：事务中的所有命令要么全部执行，要么全部不执行</li>
         *   <li><b>隔离性</b>：事务执行期间，其他客户端的命令不会插入到事务中</li>
         *   <li><b>一致性</b>：配合 WATCH 命令可以实现乐观锁，保证数据一致性</li>
         * </ul>
         *
         * <h4>使用示例</h4>
         * <pre>{@code
         * try (RedisAccessor accessor = RedisUtil.accessor()) {
         *     accessor.multi();
         *     accessor.StringOps.set("key1", "value1");
         *     accessor.StringOps.set("key2", "value2");
         *     accessor.HashOps.hset("hash1", "field1", "value1");
         *     List<Object> results = accessor.exec();
         *     // results 包含每个命令的执行结果
         * }
         * }</pre>
         *
         * @return 当前访问器实例，支持链式调用
         * @throws IllegalStateException 如果事务已经开启
         * @see #exec()
         * @see #discard()
         * @see #watch(String...)
         */
        public RedisAccessor multi() {
            if (inTransaction) {
                throw new IllegalStateException("事务已经开启，不能重复调用 multi()");
            }
            this.commands.multi();
            this.inTransaction = true;
            LoggerCore.log("INFO", "Redis 事务已开启 (MULTI) on " + dbKey);
            return this;
        }

        /**
         * 执行事务中的所有命令
         *
         * <p>执行所有事务块内的命令，并返回每个命令的执行结果。
         * 如果在事务执行前，被 WATCH 监视的键被修改，则事务会被取消，返回 null。
         *
         * <h4>返回值说明</h4>
         * <ul>
         *   <li><b>正常执行</b>：返回包含每个命令结果的列表</li>
         *   <li><b>乐观锁失败</b>：返回 null（被监视的键被修改）</li>
         *   <li><b>命令错误</b>：对应位置返回错误信息</li>
         * </ul>
         *
         * <h4>使用示例</h4>
         * <pre>{@code
         * try (RedisAccessor accessor = RedisUtil.accessor()) {
         *     accessor.watch("counter");
         *     String current = accessor.StringOps.get("counter");
         *
         *     accessor.multi();
         *     accessor.StringOps.set("counter", String.valueOf(Integer.parseInt(current) + 1));
         *     accessor.StringOps.set("last_update", String.valueOf(System.currentTimeMillis()));
         *
         *     List<Object> results = accessor.exec();
         *     if (results == null) {
         *         System.out.println("事务被取消：counter 被其他客户端修改");
         *     } else {
         *         System.out.println("事务执行成功，结果：" + results);
         *     }
         * }
         * }</pre>
         *
         * @return 命令执行结果列表，如果事务被取消则返回 null
         * @throws IllegalStateException 如果没有活跃的事务
         * @see #multi()
         * @see #watch(String...)
         */
        public List<Object> exec() {
            if (!inTransaction) {
                throw new IllegalStateException("没有活跃的事务可以执行 exec()");
            }
            try {
                TransactionResult result = this.commands.exec();
                // 修复：TransactionResult 是 Iterable，不能直接构造ArrayList，需通过Stream转换
                return StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());
            } finally {
                this.inTransaction = false;
                LoggerCore.log("INFO", "Redis 事务已执行 (EXEC) on " + dbKey);
            }
        }

        /**
         * 取消事务，清空事务队列
         *
         * <p>取消事务，丢弃事务队列中的所有命令，并退出事务状态。
         * 如果之前使用了 WATCH 命令，DISCARD 命令会自动执行 UNWATCH。
         *
         * <h4>使用场景</h4>
         * <ul>
         *   <li>业务逻辑检查失败，需要取消事务</li>
         *   <li>异常情况下的事务清理</li>
         *   <li>条件不满足时的事务回滚</li>
         * </ul>
         *
         * <h4>使用示例</h4>
         * <pre>{@code
         * try (RedisAccessor accessor = RedisUtil.accessor()) {
         *     accessor.multi();
         *     accessor.StringOps.set("key1", "value1");
         *
         *     // 某些业务条件检查
         *     if (!businessConditionMet()) {
         *         accessor.discard();  // 取消事务
         *         return;
         *     }
         *
         *     accessor.StringOps.set("key2", "value2");
         *     accessor.exec();
         * }
         * }</pre>
         *
         * @see #multi()
         * @see #exec()
         */
        public void discard() {
            if (inTransaction) {
                try {
                    this.commands.discard();
                } finally {
                    this.inTransaction = false;
                    LoggerCore.log("WARN", "Redis 事务已放弃 (DISCARD) on " + dbKey);
                }
            }
        }

        /**
         * 关闭访问器并释放资源
         *
         * <p>实现 {@link AutoCloseable} 接口，用于自动资源管理。
         * 关闭时会执行以下清理操作：
         * <ol>
         *   <li>如果存在未完成的事务，自动执行 DISCARD</li>
         *   <li>将持有的连接归还到连接池</li>
         *   <li>清理内部状态</li>
         * </ol>
         *
         * <h4>重要性</h4>
         * <p>必须确保访问器被正确关闭，否则会导致连接泄漏。
         * 强烈建议使用 try-with-resources 语法：
         *
         * <pre>{@code
         * try (RedisAccessor accessor = RedisUtil.accessor()) {
         *     // Redis 操作
         * } // 自动调用 close()
         * }</pre>
         *
         * <h4>异常安全</h4>
         * <p>即使在异常情况下，close() 方法也会被自动调用，
         * 确保资源得到正确释放。
         *
         * @see AutoCloseable#close()
         * @see LettuceCore#returnConnection(String, StatefulRedisConnection)
         */
        @Override
        public void close() {
            if (inTransaction) {
                discard();
            }
            // 将持有的连接返回到池中
            LettuceCore.returnConnection(this.dbKey, this.connection);
        }

        // ==================== LinkApi1：链式静态API ======================

        // 键
        public class KeyOps {

            /**
             * 异步删除指定的键
             *
             * <p>将传入的一个或多个键异步删除，性能更优。
             *
             * @param keys 要删除的键数组
             * @return 删除成功的键数量
             */
            public Long unlink(String... keys) {
                return commands.unlink(keys);
            }

            /**
             * 更新键的最后访问时间，不存在的键会被忽略
             *
             * <p>更新指定键的最后访问时间，仅对存在的键生效。
             *
             * @param keys 要更新的键数组
             * @return 更新成功的键数量
             */
            public Long touch(String... keys) {
                return commands.touch(keys);
            }

            /**
             * 复制键到新的键名
             *
             * <p>将源键的数据复制到目标键，如果目标键已存在则覆盖。
             *
             * @param source      源键名
             * @param destination 目标键名
             * @return 如果复制成功返回 true，否则返回 false
             */
            public Boolean copy(String source, String destination) {
                return commands.copy(source, destination);
            }

            /**
             * 同步删除指定的键
             *
             * <p>同步删除传入的一个或多个键。
             *
             * @param keys 要删除的键数组
             * @return 删除成功的键数量
             */
            public Long delete(String... keys) {
                return commands.del(keys);
            }

            /**
             * 同步删除指定的键集合
             *
             * <p>同步删除传入的键集合中的所有键。
             *
             * @param keys 要删除的键集合
             * @return 删除成功的键数量
             */
            public Long delete(Collection<String> keys) {
                return commands.del(keys.toArray(new String[0]));
            }

            /**
             * 序列化给定键，返回被序列化的值
             *
             * <p>对指定键的数据进行序列化处理，并返回序列化后的字节数组。
             *
             * @param key 要序列化的键
             * @return 序列化后的字节数组
             */
            public byte[] dump(String key) {
                return commands.dump(key);
            }

            /**
             * 检查键是否存在
             *
             * <p>检查指定键是否存在于当前数据库中。
             *
             * @param key 要检查的键
             * @return 如果键存在返回 true，否则返回 false
             */
            public Boolean hasKey(String key) {
                return commands.exists(key) > 0;
            }

            /**
             * 设置键的过期时间
             *
             * <p>设置指定键的过期时间，时间单位由 unit 指定。
             *
             * @param key     要设置过期时间的键
             * @param timeout 过期时间
             * @param unit    时间单位
             * @return 如果设置成功返回 true，否则返回 false
             */
            public Boolean expire(String key, long timeout, TimeUnit unit) {
                return commands.expire(key, unit.toSeconds(timeout));
            }

            /**
             * 设置键在指定日期过期
             *
             * <p>设置指定键在给定日期（绝对时间）过期。
             *
             * @param key  要设置过期的键
             * @param date 指定日期
             * @return 如果设置成功返回 true，否则返回 false
             */
            public Boolean expireAt(String key, Date date) {
                return commands.expireat(key, date);
            }

            /**
             * 查找匹配模式的所有键
             *
             * <p>根据给定的模式查找所有匹配的键，返回一个集合。
             *
             * @param pattern 匹配模式
             * @return 匹配的键集合
             */
            public Set<String> keys(String pattern) {
                return new HashSet<>(commands.keys(pattern));
            }

            /**
             * 将键移动到指定数据库
             *
             * <p>将指定键移动到目标数据库，dbIndex 指定目标数据库下标。
             *
             * @param key     要移动的键
             * @param dbIndex 目标数据库下标
             * @return 如果移动成功返回 true，否则返回 false
             */
            public Boolean move(String key, int dbIndex) {
                return commands.move(key, dbIndex);
            }

            /**
             * 移除键的过期时间，使其永久有效
             *
             * <p>移除指定键的过期时间，使其成为永久有效。
             *
             * @param key 指定键
             * @return 如果操作成功返回 true，否则返回 false
             */
            public Boolean persist(String key) {
                return commands.persist(key);
            }

            /**
             * 获取键的剩余生存时间（指定时间单位）
             *
             * <p>获取指定键距离过期的剩余时间，并转换成目标时间单位。
             *
             * @param key  指定键
             * @param unit 目标时间单位
             * @return 剩余生存时间
             */
            public Long getExpire(String key, TimeUnit unit) {
                return unit.convert(commands.ttl(key), TimeUnit.SECONDS);
            }

            /**
             * 获取键的剩余生存时间（秒）
             *
             * <p>获取指定键剩余的生存时间，单位为秒。
             *
             * @param key 指定键
             * @return 剩余生存时间（秒）
             */
            public Long getExpire(String key) {
                return commands.ttl(key);
            }

            /**
             * 随机返回一个键
             *
             * <p>从当前数据库中随机返回一个键，适用于随机取样等场景。
             *
             * @return 随机返回的键，如果数据库为空返回 null
             */
            public String randomKey() {
                return commands.randomkey();
            }

            /**
             * 重命名键
             *
             * <p>将旧键名重命名为新键名，如果新键名已存在则覆盖。
             *
             * @param oldKey 旧键名
             * @param newKey 新键名
             */
            public void rename(String oldKey, String newKey) {
                commands.rename(oldKey, newKey);
            }

            /**
             * 仅当新键名不存在时重命名键
             *
             * <p>如果新键名不存在时将旧键重命名为新键，否则不做任何操作。
             *
             * @param oldKey 旧键名
             * @param newKey 新键名
             * @return 如果重命名成功返回 true，否则返回 false
             */
            public Boolean renameIfAbsent(String oldKey, String newKey) {
                return commands.renamenx(oldKey, newKey);
            }

            /**
             * 获取键的数据类型
             *
             * <p>返回指定键存储的数据类型，如 string、hash、list 等。
             *
             * @param key 指定键
             * @return 键的数据类型
             */
            public String type(String key) {
                return commands.type(key);
            }
        }

        // 字符串
        public class StringOps {

            /**
             * 获取指定键的值
             *
             * <p>根据给定的键名获取对应的字符串值，如果键不存在则返回 null。
             *
             * @param key 要获取值的键
             * @return 指定键对应的字符串值或 null
             */
            public String get(String key) {
                return commands.get(key);
            }

            /**
             * 获取字符串指定范围的子串
             *
             * <p>获取指定键的字符串值中，从 start 到 end 范围内的子串。
             *
             * @param key   字符串的键名
             * @param start 开始索引
             * @param end   结束索引
             * @return 指定范围的子串
             */
            public String getRange(String key, long start, long end) {
                return commands.getrange(key, start, end);
            }

            /**
             * 设置新值并返回旧值
             *
             * <p>对指定键设置新值，并返回原有的旧值。
             *
             * @param key   要设置的键
             * @param value 新的字符串值
             * @return 设置前的旧值
             */
            public String getAndSet(String key, String value) {
                return commands.getset(key, value);
            }

            /**
             * 获取值并删除键
             *
             * <p>获取指定键的值，并将该键及其值从数据库中删除。
             *
             * @param key 要操作的键
             * @return 获取到的值
             */
            public String getDel(String key) {
                return commands.getdel(key);
            }

            /**
             * 获取值并设置过期时间
             *
             * <p>获取指定键的值，并同时修改该键的过期时间。
             *
             * @param key     要操作的键
             * @param timeout 新的过期时间
             * @param unit    时间单位
             * @return 获取到的值
             */
            public String getEx(String key, long timeout, TimeUnit unit) {
                return commands.getex(key, io.lettuce.core.GetExArgs.Builder.ex(unit.toSeconds(timeout)));
            }

            /**
             * 获取指定偏移量的位值
             *
             * <p>获取指定键对应字符串在指定偏移量处的位值。
             *
             * @param key    字符串的键名
             * @param offset 偏移量
             * @return 如果位值为 1 则返回 true，否则返回 false
             */
            public Boolean getBit(String key, long offset) {
                return commands.getbit(key, offset) == 1;
            }

            /**
             * 批量获取多个键的值
             *
             * <p>批量获取多个键对应的字符串值，返回键值映射，不存在的键被过滤掉。
             *
             * @param keys 要获取的键集合
             * @return 键值对映射
             */
            public Map<String, String> multiGet(Collection<String> keys) {
                return commands.mget(keys.toArray(new String[0])).stream()
                    .filter(KeyValue::hasValue)
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
            }

            /**
             * 设置键的值
             *
             * <p>为指定的键设置新的字符串值，如果键不存在则创建。
             *
             * @param key   要设置的键
             * @param value 新的字符串值
             */
            public void set(String key, String value) {
                commands.set(key, value);
            }

            /**
             * 设置指定偏移量的位值
             *
             * <p>在指定键对应的字符串值中，将指定偏移量处的位设置为给定状态值。
             *
             * @param key    字符串的键名
             * @param offset 偏移量
             * @param value  要设置的位状态，true 表示1，false 表示0
             * @return 如果设置后值为 1 则返回 true，否则返回 false
             */
            public boolean setBit(String key, long offset, boolean value) {
                return commands.setbit(key, offset, value ? 1 : 0) == 1;
            }

            /**
             * 设置键的值并指定过期时间
             *
             * <p>为指定的键设置新的字符串值，并同时指定过期时间。
             *
             * @param key     要设置的键
             * @param value   新的字符串值
             * @param timeout 过期时间
             * @param unit    时间单位
             */
            public void setEx(String key, String value, long timeout, TimeUnit unit) {
                commands.setex(key, unit.toSeconds(timeout), value);
            }

            /**
             * 仅当键不存在时设置值
             *
             * <p>仅在指定键不存在时对其进行赋值操作，防止覆盖已有值。
             *
             * @param key   要设置的键
             * @param value 新的字符串值
             * @return 如果键不存在并成功设置返回 true，否则返回 false
             */
            public boolean setIfAbsent(String key, String value) {
                return commands.setnx(key, value);
            }

            /**
             * 在指定偏移量处覆写字符串的一部分
             *
             * <p>在指定键对应的字符串值中，从指定偏移量开始覆写部分字符串内容。
             *
             * @param key    要操作的键
             * @param value  将要写入的字符串内容
             * @param offset 偏移量
             */
            public void setRange(String key, String value, long offset) {
                commands.setrange(key, offset, value);
            }

            /**
             * 批量设置多个键值对
             *
             * <p>批量设置多个键对应的字符串值，对所有键进行一次性更新。
             *
             * @param maps 键值对映射集合
             */
            public void multiSet(Map<String, String> maps) {
                commands.mset(maps);
            }

            /**
             * 仅当所有键都不存在时批量设置
             *
             * <p>当所有指定键均不存在时一次性批量设置键值对。
             *
             * @param maps 键值对映射集合
             * @return 如果所有键都不存在并成功设置返回 true，否则返回 false
             */
            public boolean multiSetIfAbsent(Map<String, String> maps) {
                return commands.msetnx(maps);
            }

            /**
             * 在字符串末尾追加值
             *
             * <p>为指定键对应的字符串值追加给定的内容，追加后返回新字符串的长度。
             *
             * @param key   要操作的键
             * @param value 要追加的字符串内容
             * @return 追加后的字符串长度
             */
            public Integer append(String key, String value) {
                return commands.append(key, value).intValue();
            }

            /**
             * 将键的值增加指定的整数
             *
             * <p>将指定键的数值字符串增加给定的整数值，支持负数。
             *
             * @param key       要操作的键
             * @param increment 增加的整数值
             * @return 增加后的新值
             */
            public Long incrBy(String key, long increment) {
                return commands.incrby(key, increment);
            }

            /**
             * 将键的值增加指定的浮点数
             *
             * <p>将指定键的数值字符串增加给定的浮点数值。
             *
             * @param key       要操作的键
             * @param increment 增加的浮点数值
             * @return 增加后的新值
             */
            public Double incrByFloat(String key, double increment) {
                return commands.incrbyfloat(key, increment);
            }

            /**
             * 获取字符串的长度
             *
             * <p>获取指定键对应字符串的长度，返回长度值。
             *
             * @param key 字符串的键
             * @return 字符串的长度
             */
            public Long size(String key) {
                return commands.strlen(key);
            }
        }

        // 哈希
        public class HashOps {

            /**
             * 获取哈希表中指定字段的值
             *
             * <p>从指定哈希表中获取给定字段的值，若字段不存在返回 null。
             *
             * @param key   哈希表的键名
             * @param field 要获取值的字段名
             * @return 指定字段对应的值或 null
             */
            public String hGet(String key, String field) {
                return commands.hget(key, field);
            }

            /**
             * 获取哈希表中所有字段和值
             *
             * <p>获取指定哈希表中所有字段及其对应的值，并以 Map 形式返回。
             *
             * @param key 哈希表的键名
             * @return 哈希表所有字段和值
             */
            public Map<String, String> hGetAll(String key) {
                return commands.hgetall(key);
            }

            /**
             * 批量获取哈希表中多个字段的值
             *
             * <p>从指定哈希表中一次性获取多个字段对应的值，过滤掉不存在的字段。
             *
             * @param key    哈希表的键名
             * @param fields 要获取的字段集合
             * @return 多个字段对应的键值对映射
             */
            public Map<String, String> hMultiGet(String key, Collection<String> fields) {
                return commands.hmget(key, fields.toArray(new String[0])).stream()
                    .filter(KeyValue::hasValue)
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
            }

            /**
             * 设置哈希表中字段的值
             *
             * <p>在指定哈希表中设置或更新给定字段的值，如果字段已存在则覆盖旧值。
             *
             * @param key     哈希表的键名
             * @param hashKey 哈希表中的字段名
             * @param value   新的值
             */
            public void hPut(String key, String hashKey, String value) {
                commands.hset(key, hashKey, value);
            }

            /**
             * 批量设置哈希表中多个字段的值
             *
             * <p>批量在指定哈希表中设置多个字段及其对应的值。
             *
             * @param key  哈希表的键名
             * @param maps 字段和值的映射集合
             */
            public void hPutAll(String key, Map<String, String> maps) {
                commands.hmset(key, maps);
            }

            /**
             * 仅当字段不存在时设置字段的值
             *
             * <p>如果指定的字段不存在于哈希表中，则设置该字段的值，否则不做任何操作。
             *
             * @param key     哈希表的键名
             * @param hashKey 要设置的字段名
             * @param value   要设置的值
             * @return 如果设置成功返回 true，否则返回 false
             */
            public Boolean hPutIfAbsent(String key, String hashKey, String value) {
                return commands.hsetnx(key, hashKey, value);
            }

            /**
             * 删除哈希表中一个或多个字段
             *
             * <p>从指定哈希表中删除一个或多个字段，并返回删除成功的字段数量。
             *
             * @param key    哈希表的键名
             * @param fields 要删除的字段数组
             * @return 删除成功的字段数量
             */
            public Long hDelete(String key, String... fields) {
                return commands.hdel(key, fields);
            }

            /**
             * 检查哈希表中是否存在指定字段
             *
             * <p>检查指定哈希表中是否包含给定字段。
             *
             * @param key   哈希表的键名
             * @param field 要检查的字段名
             * @return 如果字段存在返回 true，否则返回 false
             */
            public boolean hExists(String key, String field) {
                return commands.hexists(key, field);
            }

            /**
             * 将哈希表中字段的整数值增加指定增量
             *
             * <p>将指定哈希表中的数字字段增加给定的整数值。
             *
             * @param key       哈希表的键名
             * @param field     要增加值的字段
             * @param increment 增加的整数值
             * @return 增加后的新值
             */
            public Long hIncrBy(String key, String field, long increment) {
                return commands.hincrby(key, field, increment);
            }

            /**
             * 将哈希表中字段的浮点数值增加指定增量
             *
             * <p>将指定哈希表中的数字字段增加给定的浮点数值。
             *
             * @param key   哈希表的键名
             * @param field 要增加值的字段
             * @param delta 增加的浮点数值
             * @return 增加后的新值
             */
            public Double hIncrByFloat(String key, String field, double delta) {
                return commands.hincrbyfloat(key, field, delta);
            }

            /**
             * 获取哈希表中所有字段名
             *
             * <p>获取指定哈希表中的所有字段名，并以 Set 集合返回。
             *
             * @param key 哈希表的键名
             * @return 哈希表中所有的字段名集合
             */
            public Set<String> hKeys(String key) {
                return new HashSet<>(commands.hkeys(key));
            }

            /**
             * 获取哈希表中字段的数量
             *
             * <p>返回指定哈希表中字段的数量。
             *
             * @param key 哈希表的键名
             * @return 字段数量
             */
            public Long hSize(String key) {
                return commands.hlen(key);
            }

            /**
             * 获取哈希表中所有字段的值
             *
             * <p>获取指定哈希表中所有字段对应的值，并以列表形式返回。
             *
             * @param key 哈希表的键名
             * @return 所有字段的值列表
             */
            public List<String> hValues(String key) {
                return commands.hvals(key);
            }

            /**
             * 扫描哈希表中的字段和值
             *
             * <p>通过扫描方式迭代获取指定哈希表中的字段和值，适用于数据量较大时分批处理。
             *
             * @param key     哈希表的键名
             * @param options 扫描选项
             * @return 扫描结果的游标
             */
            public io.lettuce.core.MapScanCursor<String, String> hScan(String key, ScanArgs options) {
                return commands.hscan(key, options);
            }

            /**
             * 随机返回哈希表中的一个字段名
             *
             * <p>从指定哈希表随机返回一个字段名，不保证一定返回该字段对应的值。
             *
             * @param key 哈希表的键名
             * @return 随机返回的字段名
             */
            public String hRandField(String key) {
                return commands.hrandfield(key);
            }

            /**
             * 随机返回哈希表中指定数量的字段名
             *
             * <p>从指定哈希表中随机返回指定数量的不重复字段名。
             *
             * @param key   哈希表的键名
             * @param count 要返回的字段数量
             * @return 随机返回的字段名列表
             */
            public List<String> hRandField(String key, long count) {
                return commands.hrandfield(key, count);
            }
        }

        // 列表
        public class ListOps {

            /**
             * 获取列表中指定索引的元素
             *
             * <p>从指定列表中获取位于给定索引位置的元素。
             *
             * @param key   列表的键名
             * @param index 元素索引
             * @return 指定位置的元素
             */
            public String lIndex(String key, long index) {
                return commands.lindex(key, index);
            }

            /**
             * 获取列表指定范围内的元素
             *
             * <p>获取指定列表中从 start 到 end 范围内的所有元素。
             *
             * @param key   列表的键名
             * @param start 起始索引
             * @param end   结束索引
             * @return 指定范围内的元素列表
             */
            public List<String> lRange(String key, long start, long end) {
                return commands.lrange(key, start, end);
            }

            /**
             * 将一个值插入到列表头部
             *
             * <p>在指定列表的左端插入一个新的元素，插入后该元素成为第一个元素。
             *
             * @param key   列表的键名
             * @param value 要插入的元素
             * @return 返回插入后的列表长度
             */
            public Long lLeftPush(String key, String value) {
                return commands.lpush(key, value);
            }

            /**
             * 将多个值插入到列表头部
             *
             * <p>在指定列表的左端批量插入多个元素。
             *
             * @param key   列表的键名
             * @param value 多个要插入的元素
             * @return 返回插入后的列表长度
             */
            public Long lLeftPushAll(String key, String... value) {
                return commands.lpush(key, value);
            }

            /**
             * 将集合中的所有值插入到列表头部
             *
             * <p>将集合中的所有元素批量插入到指定列表的左端。
             *
             * @param key   列表的键名
             * @param value 集合中的元素
             * @return 返回插入后的列表长度
             */
            public Long lLeftPushAll(String key, Collection<String> value) {
                return commands.lpush(key, value.toArray(new String[0]));
            }

            /**
             * 仅当列表存在时，将值插入到列表头部
             *
             * <p>仅在指定列表存在时，将一个新元素插入到其左端。
             *
             * @param key   列表的键名
             * @param value 要插入的元素
             * @return 返回插入后的列表长度，如果列表不存在返回 0
             */
            public Long lLeftPushIfPresent(String key, String value) {
                return commands.lpushx(key, value);
            }

            /**
             * 在指定元素前插入值
             *
             * <p>在列表中找到指定的 pivot 元素，在其左侧插入新的元素。
             *
             * @param key   列表的键名
             * @param pivot 参照的 pivot 元素
             * @param value 要插入的元素
             * @return 如果插入成功返回新的列表长度，否则返回 -1
             */
            public Long lLeftPush(String key, String pivot, String value) {
                return commands.linsert(key, true, pivot, value);
            }

            /**
             * 将一个值插入到列表尾部
             *
             * <p>在指定列表的右端插入一个新的元素，插入后该元素成为最后一个元素。
             *
             * @param key   列表的键名
             * @param value 要插入的元素
             * @return 返回插入后的列表长度
             */
            public Long lRightPush(String key, String value) {
                return commands.rpush(key, value);
            }

            /**
             * 将多个值插入到列表尾部
             *
             * <p>在指定列表的右端批量插入多个元素。
             *
             * @param key   列表的键名
             * @param value 多个要插入的元素
             * @return 返回插入后的列表长度
             */
            public Long lRightPushAll(String key, String... value) {
                return commands.rpush(key, value);
            }

            /**
             * 将集合中的所有值插入到列表尾部
             *
             * <p>将集合中的所有元素批量插入到指定列表的右端。
             *
             * @param key   列表的键名
             * @param value 集合中的元素
             * @return 返回插入后的列表长度
             */
            public Long lRightPushAll(String key, Collection<String> value) {
                return commands.rpush(key, value.toArray(new String[0]));
            }

            /**
             * 仅当列表存在时，将值插入到列表尾部
             *
             * <p>仅在指定列表存在时，将一个新元素插入到其右端。
             *
             * @param key   列表的键名
             * @param value 要插入的元素
             * @return 返回插入后的列表长度，如果列表不存在返回 0
             */
            public Long lRightPushIfPresent(String key, String value) {
                return commands.rpushx(key, value);
            }

            /**
             * 在指定元素后插入值
             *
             * <p>在列表中找到指定的 pivot 元素，在其右侧插入新的元素。
             *
             * @param key   列表的键名
             * @param pivot 参照的 pivot 元素
             * @param value 要插入的元素
             * @return 如果插入成功返回新的列表长度，否则返回 -1
             */
            public Long lRightPush(String key, String pivot, String value) {
                return commands.linsert(key, false, pivot, value);
            }

            /**
             * 设置列表中指定索引的元素值
             *
             * <p>为指定列表中给定索引位置设置新的元素值。
             *
             * @param key   列表的键名
             * @param index 要更新的索引位置
             * @param value 新的元素值
             */
            public void lSet(String key, long index, String value) {
                commands.lset(key, index, value);
            }

            /**
             * 移除并返回列表头部的元素
             *
             * <p>从指定列表的左端移除第一个元素，并返回该元素的值。
             *
             * @param key 列表的键名
             * @return 被移除的元素值
             */
            public String lLeftPop(String key) {
                return commands.lpop(key);
            }

            /**
             * 阻塞式移除并返回列表头部的元素
             *
             * <p>在指定超时时间内阻塞等待并移除列表左端的第一个元素。
             *
             * @param key     列表的键名
             * @param timeout 等待时间
             * @param unit    时间单位
             * @return 被移除的元素值，如果超时返回 null
             */
            public String lBLeftPop(String key, long timeout, TimeUnit unit) {
                KeyValue<String, String> kv = commands.blpop(unit.toSeconds(timeout), key);
                return kv == null ? null : kv.getValue();
            }

            /**
             * 移除并返回列表尾部的元素
             *
             * <p>从指定列表的右端移除最后一个元素，并返回该元素的值。
             *
             * @param key 列表的键名
             * @return 被移除的元素值
             */
            public String lRightPop(String key) {
                return commands.rpop(key);
            }

            /**
             * 阻塞式移除并返回列表尾部的元素
             *
             * <p>在指定超时时间内阻塞等待并移除列表右端的最后一个元素。
             *
             * @param key     列表的键名
             * @param timeout 等待时间
             * @param unit    时间单位
             * @return 被移除的元素值，如果超时返回 null
             */
            public String lBRightPop(String key, long timeout, TimeUnit unit) {
                KeyValue<String, String> kv = commands.brpop(unit.toSeconds(timeout), key);
                return kv == null ? null : kv.getValue();
            }

            /**
             * 移除源列表尾部元素并插入到目标列表头部
             *
             * <p>将指定源列表的右端最后一个元素移除后插入到目标列表的左端，并返回该元素值。
             *
             * @param sourceKey      源列表的键名
             * @param destinationKey 目标列表的键名
             * @return 被移除并插入的元素值
             */
            public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
                return commands.rpoplpush(sourceKey, destinationKey);
            }

            /**
             * 阻塞式移除源列表尾部元素并插入到目标列表头部
             *
             * <p>在指定超时时间内阻塞等待，将源列表右端的最后一个元素移除后插入目标列表左端。
             *
             * @param sourceKey      源列表的键名
             * @param destinationKey 目标列表的键名
             * @param timeout        等待时间
             * @param unit           时间单位
             * @return 被移除并插入的元素值，如果超时返回 null
             */
            public String lBRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
                return commands.brpoplpush(unit.toSeconds(timeout), sourceKey, destinationKey);
            }

            /**
             * 移除列表中指定数量的指定值
             *
             * <p>从指定列表中移除 count 个值与给定 value 相同的元素。
             *
             * @param key   列表的键名
             * @param count 移除的数量（正数从头部开始，负数从尾部开始，0 表示全部）
             * @param value 要移除的元素值
             * @return 实际移除的元素数量
             */
            public Long lRemove(String key, long count, String value) {
                return commands.lrem(key, count, value);
            }

            /**
             * 修剪列表，只保留指定范围内的元素
             *
             * <p>对指定列表进行修剪操作，只保留从 start 到 end 索引范围内的元素，其余元素被移除。
             *
             * @param key   列表的键名
             * @param start 起始索引
             * @param end   结束索引
             */
            public void lTrim(String key, long start, long end) {
                commands.ltrim(key, start, end);
            }

            /**
             * 获取列表的长度
             *
             * <p>返回指定列表中包含的元素个数。
             *
             * @param key 列表的键名
             * @return 列表的长度
             */
            public Long lLen(String key) {
                return commands.llen(key);
            }
        }

        // 集合
        public class SetOps {

            /**
             * 向集合添加一个或多个成员
             *
             * <p>向指定集合中添加一个或多个成员，重复的成员将被忽略。
             *
             * @param key    集合的键名
             * @param values 要添加的成员数组
             * @return 被成功添加的成员数量
             */
            public Long sAdd(String key, String... values) {
                return commands.sadd(key, values);
            }

            /**
             * 获取集合中的所有成员
             *
             * <p>获取指定集合中所有的成员，返回一个 Set 集合。
             *
             * @param key 集合的键名
             * @return 集合中所有成员的 Set 集合
             */
            public Set<String> sMembers(String key) {
                return commands.smembers(key);
            }

            /**
             * 获取集合中成员的数量
             *
             * <p>返回指定集合中成员的个数。
             *
             * @param key 集合的键名
             * @return 集合中成员的数量
             */
            public Long sSize(String key) {
                return commands.scard(key);
            }

            /**
             * 检查成员是否存在于集合中
             *
             * <p>检查指定值是否为给定集合的元素。
             *
             * @param key   集合的键名
             * @param value 要检查的成员
             * @return 如果成员存在返回 true，否则返回 false
             */
            public Boolean sIsMember(String key, String value) {
                return commands.sismember(key, value);
            }

            /**
             * 随机返回集合中的一个成员
             *
             * <p>从指定集合中随机返回一个成员，不保证随机性重复。
             *
             * @param key 集合的键名
             * @return 随机返回的成员
             */
            public String sRandomMember(String key) {
                return commands.srandmember(key);
            }

            /**
             * 随机返回集合中指定数量的成员（可能重复）
             *
             * <p>从指定集合中随机返回给定数量的成员，可能存在重复。
             *
             * @param key   集合的键名
             * @param count 要返回的成员数量
             * @return 随机返回的成员列表，可能含重复项
             */
            public List<String> sRandomMembers(String key, long count) {
                return commands.srandmember(key, count);
            }

            /**
             * 随机返回集合中指定数量的不重复成员
             *
             * <p>从指定集合中随机返回给定数量的不重复成员。
             *
             * @param key   集合的键名
             * @param count 要返回的不重复成员数量
             * @return 随机返回的不重复成员集合
             */
            public Set<String> sDistinctRandomMembers(String key, long count) {
                return new HashSet<>(commands.srandmember(key, count));
            }

            /**
             * 扫描集合中的成员
             *
             * <p>采用扫描方式迭代获取指定集合中的所有成员。
             *
             * @param key     集合的键名
             * @param options 扫描选项
             * @return 扫描结果的游标
             */
            public ValueScanCursor<String> sScan(String key, ScanArgs options) {
                return commands.sscan(key, options);
            }

            /**
             * 计算两个集合的交集
             *
             * <p>计算两个集合的交集，并返回交集中的所有成员。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @return 两个集合交集的成员集合
             */
            public Set<String> sIntersect(String key, String otherKey) {
                return commands.sinter(key, otherKey);
            }

            /**
             * 计算多个集合的交集
             *
             * <p>计算多个集合的交集，并返回交集中的所有成员。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @return 多个集合交集的成员集合
             */
            public Set<String> sIntersect(String key, Collection<String> otherKeys) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sinter(keys.toArray(new String[0]));
            }

            /**
             * 计算两个集合的交集并存储到目标集合
             *
             * <p>计算两个集合的交集，并将结果存储在目标集合中。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @param destKey  存储交集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sIntersectAndStore(String key, String otherKey, String destKey) {
                return commands.sinterstore(destKey, key, otherKey);
            }

            /**
             * 计算多个集合的交集并存储到目标集合
             *
             * <p>计算多个集合的交集，并将结果存储在目标集合中。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @param destKey   存储交集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sinterstore(destKey, keys.toArray(new String[0]));
            }

            /**
             * 计算两个集合的并集
             *
             * <p>计算两个集合的并集，并返回并集中的所有成员。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @return 两个集合并集的成员集合
             */
            public Set<String> sUnion(String key, String otherKey) {
                return commands.sunion(key, otherKey);
            }

            /**
             * 计算多个集合的并集
             *
             * <p>计算多个集合的并集，并返回并集中所有的成员。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @return 多个集合并集的成员集合
             */
            public Set<String> sUnion(String key, Collection<String> otherKeys) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sunion(keys.toArray(new String[0]));
            }

            /**
             * 计算两个集合的并集并存储到目标集合
             *
             * <p>计算两个集合的并集，并将结果存储到目标集合中。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @param destKey  存储并集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sUnionAndStore(String key, String otherKey, String destKey) {
                return commands.sunionstore(destKey, key, otherKey);
            }

            /**
             * 计算多个集合的并集并存储到目标集合
             *
             * <p>计算多个集合的并集，并将结果存储到目标集合中。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @param destKey   存储并集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sunionstore(destKey, keys.toArray(new String[0]));
            }

            /**
             * 计算两个集合的差集
             *
             * <p>计算两个集合的差集，并返回在第一个集合中但不在第二个集合中的成员。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @return 差集的成员集合
             */
            public Set<String> sDifference(String key, String otherKey) {
                return commands.sdiff(key, otherKey);
            }

            /**
             * 计算多个集合的差集
             *
             * <p>计算多个集合的差集，并返回在第一个集合中但不在其他集合中的成员。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @return 差集的成员集合
             */
            public Set<String> sDifference(String key, Collection<String> otherKeys) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sdiff(keys.toArray(new String[0]));
            }

            /**
             * 计算两个集合的差集并存储到目标集合
             *
             * <p>计算两个集合的差集，并将结果存储在目标集合中。
             *
             * @param key      第一个集合的键名
             * @param otherKey 第二个集合的键名
             * @param destKey  存储差集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sDifferenceAndStore(String key, String otherKey, String destKey) {
                return commands.sdiffstore(destKey, key, otherKey);
            }

            /**
             * 计算多个集合的差集并存储到目标集合
             *
             * <p>计算多个集合的差集，并将结果存储在目标集合中。
             *
             * @param key       第一个集合的键名
             * @param otherKeys 其他集合的键集合
             * @param destKey   存储差集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long sDifferenceAndStore(String key, Collection<String> otherKeys, String destKey) {
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.addAll(otherKeys);
                return commands.sdiffstore(destKey, keys.toArray(new String[0]));
            }

            /**
             * 从集合中移除一个或多个成员
             *
             * <p>从指定集合中移除传入的一个或多个成员，返回被移除的数量。
             *
             * @param key    集合的键名
             * @param values 要移除的成员数组
             * @return 实际移除的成员数量
             */
            public Long sRemove(String key, String... values) {
                return commands.srem(key, values);
            }

            /**
             * 随机移除并返回集合中的一个成员
             *
             * <p>从指定集合中随机移除一个成员，并返回该成员的值。
             *
             * @param key 集合的键名
             * @return 被移除的成员值
             */
            public String sPop(String key) {
                return commands.spop(key);
            }

            /**
             * 将成员从源集合移动到目标集合
             *
             * <p>将指定成员从源集合中移除，并添加到目标集合中。
             *
             * @param key     源集合的键名
             * @param value   要移动的成员
             * @param destKey 目标集合的键名
             * @return 如果移动成功返回 true，否则返回 false
             */
            public Boolean sMove(String key, String value, String destKey) {
                return commands.smove(key, destKey, value);
            }
        }

        // 有序集合
        public class ZSetOps {

            /**
             * 向有序集合添加一个成员
             *
             * <p>在指定有序集合中添加一个成员及其分数，如果添加后基数估计值发生变化则返回 true。
             *
             * @param key   有序集合的键名
             * @param value 要添加的成员
             * @param score 成员对应的分数
             * @return 如果添加成功返回 true，否则返回 false
             */
            public Boolean zAdd(String key, String value, double score) {
                return commands.zadd(key, score, value) > 0;
            }

            /**
             * 向有序集合批量添加成员
             *
             * <p>在指定有序集合中批量添加多个成员及其对应分数。
             *
             * @param key    有序集合的键名
             * @param values 要添加的成员及分数的集合
             * @return 被成功添加的成员数量
             */
            public Long zAdd(String key, Set<ScoredValue<String>> values) {
                return commands.zadd(key, values.toArray(new ScoredValue[0]));
            }

            /**
             * 按排名范围获取成员（分数从低到高）
             *
             * <p>按成员排名顺序（从低到高）获取指定范围内的成员集合。
             *
             * @param key   有序集合的键名
             * @param start 起始排名
             * @param end   结束排名
             * @return 指定排名范围内的成员集合
             */
            public Set<String> zRange(String key, long start, long end) {
                return new LinkedHashSet<>(commands.zrange(key, start, end));
            }

            /**
             * 按排名范围获取成员及其分数（分数从低到高）
             *
             * <p>按成员排名顺序（从低到高）获取指定范围内的成员及其分数。
             *
             * @param key   有序集合的键名
             * @param start 起始排名
             * @param end   结束排名
             * @return 指定排名范围内成员及其分数的集合
             */
            public Set<ScoredValue<String>> zRangeWithScores(String key, long start, long end) {
                return new LinkedHashSet<>(commands.zrangeWithScores(key, start, end));
            }

            /**
             * 按分数范围获取成员
             *
             * <p>获取有序集合中分数在指定范围内的所有成员，返回成员集合。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 指定分数区间内的成员集合
             */
            public Set<String> zRangeByScore(String key, double min, double max) {
                return new LinkedHashSet<>(commands.zrangebyscore(key, Range.create(min, max)));
            }

            /**
             * 按分数范围获取成员及其分数
             *
             * <p>获取有序集合中分数在指定范围内的所有成员及其分数。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 指定分数区间内的成员及其分数集合
             */
            public Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max) {
                return new LinkedHashSet<>(commands.zrangebyscoreWithScores(key, Range.create(min, max)));
            }

            /**
             * 按分数范围获取成员及其分数（支持分页）
             *
             * <p>获取有序集合中分数在指定范围内的成员及其分数，支持分页查询。
             *
             * @param key   有序集合的键名
             * @param min   分数下限
             * @param max   分数上限
             * @param start 分页起始索引
             * @param end   分页结束索引
             * @return 指定分数区间内分页后的成员及其分数集合
             */
            public Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
                return new LinkedHashSet<>(commands.zrangebyscoreWithScores(key, Range.create(min, max), Limit.create(start, end)));
            }

            /**
             * 按排名范围获取成员（分数从高到低）
             *
             * <p>按成员排名顺序（从高到低）获取指定范围内的成员集合。
             *
             * @param key   有序集合的键名
             * @param start 起始排名
             * @param end   结束排名
             * @return 指定排名范围内的成员集合
             */
            public Set<String> zReverseRange(String key, long start, long end) {
                return new LinkedHashSet<>(commands.zrevrange(key, start, end));
            }

            /**
             * 按排名范围获取成员及其分数（分数从高到低）
             *
             * <p>按成员排名顺序（从高到低）获取指定范围内的成员及其分数。
             *
             * @param key   有序集合的键名
             * @param start 起始排名
             * @param end   结束排名
             * @return 指定排名范围内成员及其分数的集合
             */
            public Set<ScoredValue<String>> zReverseRangeWithScores(String key, long start, long end) {
                return new LinkedHashSet<>(commands.zrevrangeWithScores(key, start, end));
            }

            /**
             * 按分数范围获取成员（分数从高到低）
             *
             * <p>获取有序集合中分数在指定范围内的所有成员，按照分数从高到低排序。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 指定分数区间内从高到低排序的成员集合
             */
            public Set<String> zReverseRangeByScore(String key, double min, double max) {
                return new LinkedHashSet<>(commands.zrevrangebyscore(key, Range.create(min, max)));
            }

            /**
             * 按分数范围获取成员及其分数（分数从高到低）
             *
             * <p>获取有序集合中分数在指定范围内的所有成员及其分数，按照分数从高到低排序。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 指定分数区间内从高到低排序的成员及其分数集合
             */
            public Set<ScoredValue<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
                return new LinkedHashSet<>(commands.zrevrangebyscoreWithScores(key, Range.create(min, max)));
            }

            /**
             * 按分数范围获取成员（分数从高到低，支持分页）
             *
             * <p>获取有序集合中分数在指定范围内的成员，按照分数从高到低排序，并支持分页查询。
             *
             * @param key   有序集合的键名
             * @param min   分数下限
             * @param max   分数上限
             * @param start 分页起始索引
             * @param end   分页结束索引
             * @return 指定分数区间内分页后的从高到低排序的成员集合
             */
            public Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end) {
                return new LinkedHashSet<>(commands.zrevrangebyscore(key, Range.create(min, max), Limit.create(start, end)));
            }

            /**
             * 获取成员的排名（分数从低到高，排名从0开始）
             *
             * <p>获取指定成员在有序集合中的排名（从低到高排序，排名以0开始统计）。
             *
             * @param key   有序集合的键名
             * @param value 要查询排名的成员
             * @return 成员的排名或 null（如果不存在）
             */
            public Long zRank(String key, String value) {
                return commands.zrank(key, value);
            }

            /**
             * 获取成员的排名（分数从高到低，排名从0开始）
             *
             * <p>获取指定成员在有序集合中的逆向排名（从高到低排序，排名以0开始统计）。
             *
             * @param key   有序集合的键名
             * @param value 要查询排名的成员
             * @return 成员的逆向排名或 null（如果不存在）
             */
            public Long zReverseRank(String key, String value) {
                return commands.zrevrank(key, value);
            }

            /**
             * 统计分数范围内的成员数量
             *
             * <p>统计有序集合中分数在指定范围内的成员数量。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 指定分数区间内的成员数量
             */
            public Long zCount(String key, double min, double max) {
                return commands.zcount(key, Range.create(min, max));
            }

            /**
             * 获取有序集合的成员数量
             *
             * <p>返回指定有序集合中包含的成员总数。
             *
             * @param key 有序集合的键名
             * @return 成员数量
             */
            public Long zSize(String key) {
                return commands.zcard(key);
            }

            /**
             * 获取有序集合的成员数量（别名方法）
             *
             * <p>与 zSize 方法相同，返回指定有序集合中包含的成员数量。
             *
             * @param key 有序集合的键名
             * @return 成员数量
             */
            public Long zZCard(String key) {
                return zSize(key);
            }

            /**
             * 获取成员的分数
             *
             * <p>获取指定成员在有序集合中的分数值。
             *
             * @param key   有序集合的键名
             * @param value 要查询分数的成员
             * @return 成员的分数，如果成员不存在返回 null
             */
            public Double zScore(String key, String value) {
                return commands.zscore(key, value);
            }

            /**
             * 计算两个有序集合的并集并存储到目标集合
             *
             * <p>计算两个有序集合的并集，并将结果存储到指定目标集合中。
             *
             * @param key      第一个有序集合的键名
             * @param otherKey 第二个有序集合的键名
             * @param destKey  存储并集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long zUnionAndStore(String key, String otherKey, String destKey) {
                return commands.zunionstore(destKey, key, otherKey);
            }

            /**
             * 计算多个有序集合的并集并存储到目标集合
             *
             * <p>计算多个有序集合的并集，并将结果存储到指定目标集合中。
             *
             * @param key       第一个有序集合的键名
             * @param otherKeys 其他有序集合的键集合
             * @param destKey   存储并集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
                List<String> keysToUnion = new ArrayList<>();
                keysToUnion.add(key);
                keysToUnion.addAll(otherKeys);
                return commands.zunionstore(destKey, keysToUnion.toArray(new String[0]));
            }

            /**
             * 计算两个有序集合的交集并存储到目标集合
             *
             * <p>计算两个有序集合的交集，并将结果存储到指定目标集合中。
             *
             * @param key      第一个有序集合的键名
             * @param otherKey 第二个有序集合的键名
             * @param destKey  存储交集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long zIntersectAndStore(String key, String otherKey, String destKey) {
                return commands.zinterstore(destKey, key, otherKey);
            }

            /**
             * 计算多个有序集合的交集并存储到目标集合
             *
             * <p>计算多个有序集合的交集，并将结果存储到指定目标集合中。
             *
             * @param key       第一个有序集合的键名
             * @param otherKeys 其他有序集合的键集合
             * @param destKey   存储交集结果的目标集合键名
             * @return 存储到目标集合中的成员数量
             */
            public Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
                List<String> keysToIntersect = new ArrayList<>();
                keysToIntersect.add(key);
                keysToIntersect.addAll(otherKeys);
                return commands.zinterstore(destKey, keysToIntersect.toArray(new String[0]));
            }

            /**
             * 扫描有序集合中的成员和分数
             *
             * <p>采用扫描方式迭代获取指定有序集合中的成员及其分数，适用于数据量较大时分批处理。
             *
             * @param key     有序集合的键名
             * @param options 扫描选项
             * @return 扫描结果的游标
             */
            public ScoredValueScanCursor<String> zScan(String key, ScanArgs options) {
                return commands.zscan(key, options);
            }

            /**
             * 从有序集合中移除一个或多个成员
             *
             * <p>从指定有序集合中移除传入的一个或多个成员，返回被移除的数量。
             *
             * @param key    有序集合的键名
             * @param values 要移除的成员数组
             * @return 实际移除的成员数量
             */
            public Long zRemove(String key, String... values) {
                return commands.zrem(key, values);
            }

            /**
             * 增加成员的分数
             *
             * <p>为指定有序集合中的成员增加指定的分数，并返回新的分数值。
             *
             * @param key   有序集合的键名
             * @param value 要增加分数的成员
             * @param delta 要增加的分数值
             * @return 增加后的新分数
             */
            public Double zIncrementScore(String key, String value, double delta) {
                return commands.zincrby(key, delta, value);
            }

            /**
             * 按排名范围移除成员
             *
             * <p>移除指定有序集合中排名在给定范围内的所有成员。
             *
             * @param key   有序集合的键名
             * @param start 排名起始位置
             * @param end   排名结束位置
             * @return 被移除的成员数量
             */
            public Long zRemoveRange(String key, long start, long end) {
                return commands.zremrangebyrank(key, start, end);
            }

            /**
             * 按分数范围移除成员
             *
             * <p>移除指定有序集合中分数在给定范围内的所有成员。
             *
             * @param key 有序集合的键名
             * @param min 分数下限
             * @param max 分数上限
             * @return 被移除的成员数量
             */
            public Long zRemoveRangeByScore(String key, double min, double max) {
                return commands.zremrangebyscore(key, Range.create(min, max));
            }
        }

        // ==================== LinkApi2：链式静态API ======================

        // 位图
        public class BitmapOps {

            /**
             * 统计位图中设置为1的位数量
             *
             * <p>统计指定键对应的位图中所有被设置为1的位的数量。
             *
             * @param key 位图的键名
             * @return 被设置为1的位的数量
             */
            public Long bitCount(String key) {
                return commands.bitcount(key);
            }

            /**
             * 统计位图指定字节范围内设置为1的位数量
             *
             * <p>统计位图在指定字节范围内被设置为1的位数量。
             *
             * @param key   位图的键名
             * @param start 字节范围起始位置
             * @param end   字节范围结束位置
             * @return 指定字节范围内被设置为1的位的数量
             */
            public Long bitCount(String key, long start, long end) {
                return commands.bitcount(key, start, end);
            }

            /**
             * 查找第一个指定状态的位位置
             *
             * <p>查找位图中第一个值为指定状态（true 表示1、false 表示0）的位的位置。
             *
             * @param key   位图的键名
             * @param state 要查找的状态，true 或 false
             * @return 第一个符合条件的位的位置，如果不存在返回 -1
             */
            public Long bitPos(String key, boolean state) {
                return commands.bitpos(key, state);
            }

            /**
             * 从指定位置开始查找第一个指定状态的位位置
             *
             * <p>从指定的起始字节位置开始，查找位图中第一个值为指定状态的位的位置。
             *
             * @param key   位图的键名
             * @param state 要查找的状态，true 或 false
             * @param start 起始字节位置
             * @return 第一个符合条件的位的位置，如果不存在返回 -1
             */
            public Long bitPos(String key, boolean state, long start) {
                return commands.bitpos(key, state, start);
            }

            /**
             * 对多个位图执行AND运算并存储结果
             *
             * <p>对多个位图执行逻辑与运算，并将结果存储到指定目标键。
             *
             * @param destination 目标位图的键名
             * @param keys        参与运算的位图键数组
             * @return 目标位图中被设置为1的位数量
             */
            public Long bitOpAnd(String destination, String... keys) {
                return commands.bitopAnd(destination, keys);
            }

            /**
             * 对多个位图执行OR运算并存储结果
             *
             * <p>对多个位图执行逻辑或运算，并将结果存储到指定目标键。
             *
             * @param destination 目标位图的键名
             * @param keys        参与运算的位图键数组
             * @return 目标位图中被设置为1的位数量
             */
            public Long bitOpOr(String destination, String... keys) {
                return commands.bitopOr(destination, keys);
            }

            /**
             * 对多个位图执行XOR运算并存储结果
             *
             * <p>对多个位图执行逻辑异或运算，并将结果存储到指定目标键。
             *
             * @param destination 目标位图的键名
             * @param keys        参与运算的位图键数组
             * @return 目标位图中被设置为1的位数量
             */
            public Long bitOpXor(String destination, String... keys) {
                return commands.bitopXor(destination, keys);
            }

            /**
             * 对位图执行NOT运算并存储结果
             *
             * <p>对指定位图执行逻辑非运算，并将结果存储到目标键。
             *
             * @param destination 目标位图的键名
             * @param source      源位图的键名
             * @return 目标位图中被设置为1的位数量
             */
            public Long bitOpNot(String destination, String source) {
                return commands.bitopNot(destination, source);
            }
        }

        // 基数统计
        public class HyperLogLogOps {

            /**
             * 添加元素到 HyperLogLog
             *
             * <p>将一个或多个元素添加到 HyperLogLog 数据结构中。
             * 如果添加后基数估算值发生了变化，返回 true；否则返回 false。
             *
             * @param key      HyperLogLog 的键名
             * @param elements 要添加的元素数组
             * @return 如果基数估算值发生变化则返回 true
             */
            public boolean pfAdd(String key, Object... elements) {
                RHyperLogLog<Object> hyperLogLog = RedissonCore.getClient(dbKey).getHyperLogLog(key);
                return hyperLogLog.addAll(java.util.Arrays.asList(elements));
            }

            /**
             * 计算 HyperLogLog 的基数估算值
             *
             * <p>返回给定 HyperLogLog 的基数估算值。如果指定了多个键，
             * 则返回这些 HyperLogLog 的并集的基数估算值。
             *
             * @param keys 一个或多个 HyperLogLog 键名
             * @return 基数的估算值
             */
            public long pfCount(String... keys) {
                RHyperLogLog<Object> hyperLogLog = RedissonCore.getClient(dbKey).getHyperLogLog(keys[0]);
                if (keys.length > 1) {
                    String[] otherKeys = new String[keys.length - 1];
                    System.arraycopy(keys, 1, otherKeys, 0, keys.length - 1);
                    return hyperLogLog.countWith(otherKeys);
                }
                return hyperLogLog.count();
            }

            /**
             * 合并多个 HyperLogLog
             *
             * <p>将多个 HyperLogLog 合并到目标 HyperLogLog 中。
             * 合并后的结果相当于所有源 HyperLogLog 的并集。
             *
             * @param destKey    目标 HyperLogLog 键名
             * @param sourceKeys 源 HyperLogLog 键名数组
             */
            public void pfMerge(String destKey, String... sourceKeys) {
                RHyperLogLog<Object> destHyperLogLog = RedissonCore.getClient(dbKey).getHyperLogLog(destKey);
                destHyperLogLog.mergeWith(sourceKeys);
            }
        }

        // 地理空间
        public class GeoOps {

            /**
             * 添加单个地理位置
             *
             * @param key       地理空间集合的键名
             * @param longitude 经度（-180 到 180）
             * @param latitude  纬度（-85.05112878 到 85.05112878）
             * @param member    位置标识符
             * @return 新添加的位置数量
             */
            public Long geoAdd(String key, double longitude, double latitude, Object member) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.add(longitude, latitude, member);
            }

            /**
             * 批量添加地理位置
             *
             * @param key        地理空间集合的键名
             * @param geoEntries 位置信息映射表
             * @return 新添加的位置数量
             */
            public Long geoAdd(String key, Map<Object, org.redisson.api.GeoPosition> geoEntries) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.add(geoEntries.entrySet().stream()
                    .map(e -> new org.redisson.api.GeoEntry(e.getValue().getLongitude(), e.getValue().getLatitude(), e.getKey()))
                    .toArray(org.redisson.api.GeoEntry[]::new));
            }

            /**
             * 获取成员的地理位置坐标
             *
             * @param key     地理空间集合的键名
             * @param members 成员标识符数组
             * @return 成员与其坐标的映射表
             */
            public Map<Object, org.redisson.api.GeoPosition> geoPos(String key, Object... members) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.pos(members);
            }

            /**
             * 计算两个成员间的地理距离
             *
             * @param key     地理空间集合的键名
             * @param member1 第一个成员
             * @param member2 第二个成员
             * @param unit    距离单位
             * @return 两点间的距离
             */
            public Double geoDist(String key, Object member1, Object member2, GeoUnit unit) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.dist(member1, member2, unit);
            }

            /**
             * 基于坐标进行半径查询（返回距离）
             *
             * @param key       地理空间集合的键名
             * @param longitude 中心点经度
             * @param latitude  中心点纬度
             * @param radius    查询半径
             * @param unit      距离单位
             * @return 成员与距离的映射表
             */
            public Map<Object, Double> geoRadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.radiusWithDistance(longitude, latitude, radius, unit);
            }

            /**
             * 基于坐标进行半径查询（限制数量）
             *
             * @param key       地理空间集合的键名
             * @param longitude 中心点经度
             * @param latitude  中心点纬度
             * @param radius    查询半径
             * @param unit      距离单位
             * @param count     返回结果的最大数量
             * @return 符合条件的成员列表
             */
            public List<Object> geoRadius(String key, double longitude, double latitude, double radius, GeoUnit unit, int count) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.radius(longitude, latitude, radius, unit, count);
            }

            /**
             * 基于成员进行半径查询（返回距离）
             *
             * @param key    地理空间集合的键名
             * @param member 中心成员
             * @param radius 查询半径
             * @param unit   距离单位
             * @return 成员与距离的映射表
             */
            public Map<Object, Double> geoRadiusByMember(String key, Object member, double radius, GeoUnit unit) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.radiusWithDistance(member, radius, unit);
            }

            /**
             * 基于成员进行半径查询（限制数量）
             *
             * @param key    地理空间集合的键名
             * @param member 中心成员
             * @param radius 查询半径
             * @param unit   距离单位
             * @param count  返回结果的最大数量
             * @return 符合条件的成员列表
             */
            public List<Object> geoRadiusByMember(String key, Object member, double radius, GeoUnit unit, int count) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.radius(member, radius, unit, count);
            }

            /**
             * 高级地理空间搜索
             *
             * @param key  地理空间集合的键名
             * @param args 搜索参数
             * @return 符合条件的成员列表
             */
            public List<Object> geoSearch(String key, GeoSearchArgs args) {
                RGeo<Object> geo = RedissonCore.getClient(dbKey).getGeo(key);
                return geo.search(args);
            }
        }

        // 布隆过滤器
        public class BloomFilterOps {

            /**
             * 向布隆过滤器添加元素
             *
             * <p>向指定的布隆过滤器中添加一个元素。
             *
             * @param key     布隆过滤器的键名
             * @param element 要添加的元素
             * @return 如果元素被成功添加返回 true，否则返回 false
             */
            public boolean add(String key, Object element) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).add(element);
            }

            /**
             * 检查元素是否可能存在于布隆过滤器中
             *
             * <p>检查指定元素是否可能存在于布隆过滤器中。
             * 如果返回 false，则元素一定不存在；如果返回 true，则元素可能存在。
             *
             * @param key     布隆过滤器的键名
             * @param element 要检查的元素
             * @return 如果元素可能存在返回 true，一定不存在返回 false
             */
            public boolean contains(String key, Object element) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).contains(element);
            }

            /**
             * 初始化布隆过滤器
             *
             * <p>使用指定的预期插入数量和误判率初始化布隆过滤器。
             *
             * @param key                    布隆过滤器的键名
             * @param expectedInsertions     预期插入的元素数量
             * @param falseProbability       误判率（0.0 到 1.0 之间）
             * @return 如果初始化成功返回 true，否则返回 false
             */
            public boolean tryInit(String key, long expectedInsertions, double falseProbability) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).tryInit(expectedInsertions, falseProbability);
            }

            /**
             * 获取布隆过滤器的预期插入数量
             *
             * <p>获取布隆过滤器配置的预期插入元素数量。
             *
             * @param key 布隆过滤器的键名
             * @return 预期插入数量
             */
            public long getExpectedInsertions(String key) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).getExpectedInsertions();
            }

            /**
             * 获取布隆过滤器的误判率
             *
             * <p>获取布隆过滤器配置的误判率。
             *
             * @param key 布隆过滤器的键名
             * @return 误判率
             */
            public double getFalseProbability(String key) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).getFalseProbability();
            }

            /**
             * 获取布隆过滤器的哈希函数数量
             *
             * <p>获取布隆过滤器使用的哈希函数数量。
             *
             * @param key 布隆过滤器的键名
             * @return 哈希函数数量
             */
            public long getHashIterations(String key) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).getHashIterations();
            }

            /**
             * 获取布隆过滤器的位数组大小
             *
             * <p>获取布隆过滤器底层位数组的大小。
             *
             * @param key 布隆过滤器的键名
             * @return 位数组大小
             */
            public long getSize(String key) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).getSize();
            }

            /**
             * 获取布隆过滤器中已添加的元素数量
             *
             * <p>获取布隆过滤器中已添加的元素数量（近似值）。
             *
             * @param key 布隆过滤器的键名
             * @return 已添加的元素数量
             */
            public long count(String key) {
                return RedissonCore.getClient(dbKey).getBloomFilter(key).count();
            }
        }

        // ==================== LinkApi3：链式静态API ======================

        // 服务器
        public class ServerOps {

            /**
             * 测试与 Redis 服务器的连接
             *
             * <p>测试当前连接与 Redis 服务器之间的连通性。
             *
             * @return 返回服务器响应的字符串，例如 "PONG"
             */
            public String ping() {
                return commands.ping();
            }

            /**
             * 获取当前数据库中键的数量
             *
             * <p>获取指定数据库中所有键的数量。
             *
             * @return 当前数据库中键的数量
             */
            public Long dbSize() {
                return commands.dbsize();
            }

            /**
             * 清空当前数据库
             *
             * <p>清空当前所选数据库中的所有键及其数据。
             *
             * @return 返回执行结果字符串
             */
            public String flushDb() {
                return commands.flushdb();
            }

            /**
             * 清空所有数据库
             *
             * <p>清空 Redis 服务器中所有数据库的所有键及其数据。
             *
             * @return 返回执行结果字符串
             */
            public String flushAll() {
                return commands.flushall();
            }

            /**
             * 获取 Redis 服务器信息
             *
             * <p>获取 Redis 服务器的详细信息，包含运行状态和统计数据。
             *
             * @return Redis 服务器信息字符串
             */
            public String info() {
                return commands.info();
            }

            /**
             * 获取 Redis 服务器指定部分的信息
             *
             * <p>获取 Redis 服务器的指定部分信息，例如 "cpu"，"memory" 等。
             *
             * @param section 要获取信息的部分
             * @return 指定部分的服务器信息字符串
             */
            public String info(String section) {
                return commands.info(section);
            }

            /**
             * 获取服务器配置参数
             *
             * <p>获取 Redis 服务器中符合指定参数的配置项及其当前值。
             *
             * @param parameter 要查询的配置参数名
             * @return 返回配置参数的键值对映射
             */
            public Map<String, String> configGet(String parameter) {
                return commands.configGet(parameter);
            }

            /**
             * 设置服务器配置参数
             *
             * <p>设置 Redis 服务器中指定配置参数的新值。
             *
             * @param parameter 要设置的配置参数名
             * @param value     新的配置值
             * @return 返回执行结果字符串
             */
            public String configSet(String parameter, String value) {
                return commands.configSet(parameter, value);
            }
        }

        // Lua脚本
        public class ScriptOps {

            /**
             * 执行 Lua 脚本
             *
             * <p>在 Redis 服务器上直接执行给定的 Lua 脚本，并返回执行结果。
             *
             * @param <T>    返回值的类型
             * @param script 要执行的 Lua 脚本代码
             * @param type   脚本输出类型
             * @param keys   脚本涉及的键名数组
             * @param values 脚本涉及的参数数组
             * @return 脚本执行的结果
             */
            @SuppressWarnings("unchecked")
            public <T> T eval(String script, ScriptOutputType type, String[] keys, String... values) {
                return (T) commands.eval(script, type, keys, values);
            }

            /**
             * 通过 SHA1 值执行已缓存的 Lua 脚本
             *
             * <p>基于 SHA1 值在 Redis 服务器已缓存的脚本中执行，并返回执行结果。
             *
             * @param <T>    返回值的类型
             * @param sha    脚本的 SHA1 值
             * @param type   脚本输出类型
             * @param keys   脚本涉及的键名数组
             * @param values 脚本涉及的参数数组
             * @return 脚本执行的结果
             */
            @SuppressWarnings("unchecked")
            public <T> T evalSha(String sha, ScriptOutputType type, String[] keys, String... values) {
                return (T) commands.evalsha(sha, type, keys, values);
            }

            /**
             * 将脚本加载到服务器缓存并返回 SHA1 值
             *
             * <p>将指定的 Lua 脚本加载到 Redis 服务器缓存中，并返回其 SHA1 值以便后续使用。
             *
             * @param script 要加载的 Lua 脚本代码
             * @return 脚本对应的 SHA1 值
             */
            public String scriptLoad(String script) {
                return commands.scriptLoad(script);
            }

            /**
             * 检查指定 SHA1 值的脚本是否存在于缓存中
             *
             * <p>检查一个或多个脚本的 SHA1 值是否存在于 Redis 服务器的脚本缓存中。
             *
             * @param sha1 脚本的 SHA1 值数组
             * @return 每个脚本 SHA1 值是否存在的布尔列表
             */
            public List<Boolean> scriptExists(String... sha1) {
                return commands.scriptExists(sha1);
            }

            /**
             * 清空服务器端的脚本缓存
             *
             * <p>清空 Redis 服务器中所有已加载的 Lua 脚本缓存。
             *
             * @return 返回执行结果字符串
             */
            public String scriptFlush() {
                return commands.scriptFlush();
            }

            /**
             * 终止当前正在执行的脚本
             *
             * <p>终止当前在 Redis 服务器上执行的 Lua 脚本操作，通常用于解除阻塞。
             *
             * @return 返回执行结果字符串
             */
            public String scriptKill() {
                return commands.scriptKill();
            }
        }

        // Steam流
        public class StreamOps {

            /**
             * 添加单个字段消息到流
             *
             * @param key   流的键名
             * @param field 消息字段名
             * @param value 消息字段值
             * @return 生成的消息 ID
             */
            public StreamMessageId add(String key, String field, Object value) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.add(field, value);
            }

            /**
             * 添加多字段消息到流
             *
             * @param key    流的键名
             * @param fields 消息字段映射表
             * @return 生成的消息 ID
             */
            public StreamMessageId addAll(String key, Map<String, Object> fields) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.addAll(fields);
            }

            /**
             * 从指定 ID 开始读取消息
             *
             * @param key     流的键名
             * @param startId 起始消息 ID
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> read(String key, StreamMessageId startId) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.read(startId);
            }

            /**
             * 从指定 ID 开始读取指定数量的消息
             *
             * @param key     流的键名
             * @param count   读取消息数量
             * @param startId 起始消息 ID
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> read(String key, int count, StreamMessageId startId) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.read(count, startId);
            }

            /**
             * 读取指定范围内的消息
             *
             * @param key     流的键名
             * @param startId 起始消息 ID
             * @param endId   结束消息 ID
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> readRange(String key, StreamMessageId startId, StreamMessageId endId) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.range(startId, endId);
            }

            /**
             * 读取指定范围内的指定数量消息
             *
             * @param key     流的键名
             * @param count   读取消息数量
             * @param startId 起始消息 ID
             * @param endId   结束消息 ID
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> readRange(String key, int count, StreamMessageId startId, StreamMessageId endId) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.range(count, startId, endId);
            }

            /**
             * 创建消费者组（从流的末尾开始）
             *
             * @param key       流的键名
             * @param groupName 消费者组名称
             */
            public void createGroup(String key, String groupName) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                stream.createGroup(groupName);
            }

            /**
             * 创建消费者组（从指定消息 ID 开始）
             *
             * @param key       流的键名
             * @param groupName 消费者组名称
             * @param id        起始消息 ID
             */
            public void createGroup(String key, String groupName, StreamMessageId id) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                stream.createGroup(groupName, id);
            }

            /**
             * 消费者组读取新消息
             *
             * @param key          流的键名
             * @param groupName    消费者组名称
             * @param consumerName 消费者名称
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.readGroup(groupName, consumerName);
            }

            /**
             * 消费者组读取指定数量的新消息
             *
             * @param key          流的键名
             * @param groupName    消费者组名称
             * @param consumerName 消费者名称
             * @param count        读取消息数量
             * @return 消息 ID 与消息内容的映射表
             */
            public Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName, int count) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.readGroup(groupName, consumerName, count);
            }

            /**
             * 确认消息已被处理
             *
             * @param key       流的键名
             * @param groupName 消费者组名称
             * @param ids       要确认的消息 ID 数组
             * @return 成功确认的消息数量
             */
            public long ack(String key, String groupName, StreamMessageId... ids) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.ack(groupName, ids);
            }

            /**
             * 删除指定的消息
             *
             * @param key 流的键名
             * @param ids 要删除的消息 ID 数组
             * @return 成功删除的消息数量
             */
            public long delete(String key, StreamMessageId... ids) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.remove(ids);
            }

            /**
             * 获取流中消息的数量
             *
             * @param key 流的键名
             * @return 消息总数
             */
            public long length(String key) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.size();
            }

            /**
             * 修剪流，保留指定数量的最新消息
             *
             * @param key   流的键名
             * @param count 要保留的消息数量
             * @return 被删除的消息数量
             */
            public long trim(String key, int count) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.trim(count);
            }

            /**
             * 非严格修剪流（性能更好，但可能保留稍多的消息）
             *
             * @param key   流的键名
             * @param count 大约要保留的消息数量
             * @return 被删除的消息数量
             */
            public long trimNonStrict(String key, int count) {
                RStream<String, Object> stream = RedissonCore.getClient(dbKey).getStream(key);
                return stream.trimNonStrict(count);
            }
        }

        // 发布订阅
        public class PubSubOps {

            /**
             * 向指定主题发布消息
             *
             * <p>将消息发布到指定的主题上，所有订阅该主题的客户端将接收到该消息。
             *
             * @param topic   主题名
             * @param message 要发布的消息对象
             * @return 发布消息后返回接收到消息的订阅者数量
             */
            public long publish(String topic, Object message) {
                RTopic rTopic = RedissonCore.getClient(dbKey).getTopic(topic);
                return rTopic.publish(message);
            }

            /**
             * 订阅指定主题并设置消息监听器
             *
             * <p>订阅指定主题，设置消息监听器以处理接收到的消息。
             *
             * @param <T>      消息的类型
             * @param topic    主题名
             * @param listener 消息监听器
             * @return 返回监听器的标识 ID
             */
            public <T> int subscribe(String topic, MessageListener<T> listener) {
                RTopic rTopic = RedissonCore.getClient(dbKey).getTopic(topic);
                return rTopic.addListener(Object.class, (channel, msg) -> {
                    try {
                        @SuppressWarnings("unchecked")
                        T typedMsg = (T) msg;
                        listener.onMessage(channel, typedMsg);
                    } catch (ClassCastException e) {
                        LoggerCore.log("ERROR", "Pub/Sub 消息类型转换失败 on topic '" + topic + "': " + e.getMessage());
                    }
                });
            }

            /**
             * 取消对指定主题的订阅
             *
             * <p>取消指定主题的消息订阅，根据监听器标识 ID执行取消操作。
             *
             * @param topic      主题名
             * @param listenerId 监听器标识 ID
             */
            public void unsubscribe(String topic, int listenerId) {
                RTopic rTopic = RedissonCore.getClient(dbKey).getTopic(topic);
                rTopic.removeListener(listenerId);
            }
        }

        // ==================== LinkApi4：链式静态API ======================

        // 可重入锁
        public class ReentrantLockOps {

            /**
             * 获取可重入锁（阻塞直到获取成功）
             *
             * <p>获取指定键对应的可重入锁，阻塞等待直到成功获取，支持自动续期。
             *
             * @param key 可重入锁的键名
             */
            public void lock(String key) {
                RedissonCore.getClient(dbKey).getLock(key).lock();
            }

            /**
             * 获取可重入锁并设置自动释放时间
             *
             * <p>获取指定键对应的可重入锁，并设置锁的自动释放时间。
             *
             * @param key       可重入锁的键名
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             */
            public void lock(String key, long leaseTime, TimeUnit unit) {
                RedissonCore.getClient(dbKey).getLock(key).lock(leaseTime, unit);
            }

            /**
             * 尝试获取可重入锁（非阻塞）
             *
             * <p>尝试获取指定键对应的可重入锁，若锁被占用则立即返回。
             *
             * @param key 可重入锁的键名
             * @return 如果获取锁成功返回 true，否则返回 false
             */
            public boolean tryLock(String key) {
                return RedissonCore.getClient(dbKey).getLock(key).tryLock();
            }

            /**
             * 尝试获取可重入锁（支持等待时间和自动释放时间）
             *
             * <p>尝试获取可重入锁，支持等待指定时间后成功获取，并设置锁的自动释放时间。
             *
             * @param key       可重入锁的键名
             * @param waitTime  等待时间
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             * @return 如果获取锁成功返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getLock(key).tryLock(waitTime, leaseTime, unit);
            }

            /**
             * 释放可重入锁（仅锁持有者可释放）
             *
             * <p>释放指定键对应的可重入锁，只有持有该锁的线程才允许执行释放操作。
             *
             * @param key 可重入锁的键名
             */
            public void unlock(String key) {
                RLock lock = RedissonCore.getClient(dbKey).getLock(key);
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        // 公平锁
        public class FairLockOps {

            /**
             * 获取公平锁（阻塞直到获取成功）
             *
             * <p>获取指定键对应的公平锁，保证等待最久的线程先获取锁。
             *
             * @param key 公平锁的键名
             */
            public void lock(String key) {
                RedissonCore.getClient(dbKey).getFairLock(key).lock();
            }

            /**
             * 获取公平锁并设置自动释放时间
             *
             * <p>获取指定键对应的公平锁，并设置锁的自动释放时间。
             *
             * @param key       公平锁的键名
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             */
            public void lock(String key, long leaseTime, TimeUnit unit) {
                RedissonCore.getClient(dbKey).getFairLock(key).lock(leaseTime, unit);
            }

            /**
             * 尝试获取公平锁（非阻塞）
             *
             * <p>尝试获取指定键对应的公平锁，若锁被占用则立即返回。
             *
             * @param key 公平锁的键名
             * @return 如果获取锁成功返回 true，否则返回 false
             */
            public boolean tryLock(String key) {
                return RedissonCore.getClient(dbKey).getFairLock(key).tryLock();
            }

            /**
             * 尝试获取公平锁（支持等待时间和自动释放时间）
             *
             * <p>尝试获取公平锁，支持等待指定时间后成功获取，并设置锁的自动释放时间。
             *
             * @param key       公平锁的键名
             * @param waitTime  等待时间
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             * @return 如果获取锁成功返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getFairLock(key).tryLock(waitTime, leaseTime, unit);
            }

            /**
             * 释放公平锁
             *
             * <p>释放指定键对应的公平锁，只有持有该锁的线程才允许执行释放操作。
             *
             * @param key 公平锁的键名
             */
            public void unlock(String key) {
                org.redisson.api.RLock lock = RedissonCore.getClient(dbKey).getFairLock(key);
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        // 读写锁
        public class ReadWriteLockOps {

            /**
             * 获取读锁（阻塞直到获取成功）
             *
             * <p>获取指定键对应的读锁，允许多个线程同时持有读锁。
             *
             * @param key 读写锁的键名
             */
            public void readLock(String key) {
                RedissonCore.getClient(dbKey).getReadWriteLock(key).readLock().lock();
            }

            /**
             * 获取读锁并设置自动释放时间
             *
             * <p>获取指定键对应的读锁，并设置锁的自动释放时间。
             *
             * @param key       读写锁的键名
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             */
            public void readLock(String key, long leaseTime, TimeUnit unit) {
                RedissonCore.getClient(dbKey).getReadWriteLock(key).readLock().lock(leaseTime, unit);
            }

            /**
             * 尝试获取读锁（非阻塞）
             *
             * <p>尝试获取指定键对应的读锁，若无法获取则立即返回。
             *
             * @param key 读写锁的键名
             * @return 如果获取锁成功返回 true，否则返回 false
             */
            public boolean tryReadLock(String key) {
                return RedissonCore.getClient(dbKey).getReadWriteLock(key).readLock().tryLock();
            }

            /**
             * 尝试获取读锁（支持等待时间和自动释放时间）
             *
             * <p>尝试获取读锁，支持等待指定时间后成功获取，并设置锁的自动释放时间。
             *
             * @param key       读写锁的键名
             * @param waitTime  等待时间
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             * @return 如果获取锁成功返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean tryReadLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getReadWriteLock(key).readLock().tryLock(waitTime, leaseTime, unit);
            }

            /**
             * 释放读锁
             *
             * <p>释放指定键对应的读锁，只有持有该锁的线程才允许执行释放操作。
             *
             * @param key 读写锁的键名
             */
            public void unlockRead(String key) {
                org.redisson.api.RLock readLock = RedissonCore.getClient(dbKey).getReadWriteLock(key).readLock();
                if (readLock.isLocked() && readLock.isHeldByCurrentThread()) {
                    readLock.unlock();
                }
            }

            /**
             * 获取写锁（阻塞直到获取成功）
             *
             * <p>获取指定键对应的写锁，只允许一个线程持有写锁。
             *
             * @param key 读写锁的键名
             */
            public void writeLock(String key) {
                RedissonCore.getClient(dbKey).getReadWriteLock(key).writeLock().lock();
            }

            /**
             * 获取写锁并设置自动释放时间
             *
             * <p>获取指定键对应的写锁，并设置锁的自动释放时间。
             *
             * @param key       读写锁的键名
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             */
            public void writeLock(String key, long leaseTime, TimeUnit unit) {
                RedissonCore.getClient(dbKey).getReadWriteLock(key).writeLock().lock(leaseTime, unit);
            }

            /**
             * 尝试获取写锁（非阻塞）
             *
             * <p>尝试获取指定键对应的写锁，若无法获取则立即返回。
             *
             * @param key 读写锁的键名
             * @return 如果获取锁成功返回 true，否则返回 false
             */
            public boolean tryWriteLock(String key) {
                return RedissonCore.getClient(dbKey).getReadWriteLock(key).writeLock().tryLock();
            }

            /**
             * 尝试获取写锁（支持等待时间和自动释放时间）
             *
             * <p>尝试获取写锁，支持等待指定时间后成功获取，并设置锁的自动释放时间。
             *
             * @param key       读写锁的键名
             * @param waitTime  等待时间
             * @param leaseTime 锁的自动释放时间
             * @param unit      时间单位
             * @return 如果获取锁成功返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean tryWriteLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getReadWriteLock(key).writeLock().tryLock(waitTime, leaseTime, unit);
            }

            /**
             * 释放写锁
             *
             * <p>释放指定键对应的写锁，只有持有该锁的线程才允许执行释放操作。
             *
             * @param key 读写锁的键名
             */
            public void unlockWrite(String key) {
                org.redisson.api.RLock writeLock = RedissonCore.getClient(dbKey).getReadWriteLock(key).writeLock();
                if (writeLock.isLocked() && writeLock.isHeldByCurrentThread()) {
                    writeLock.unlock();
                }
            }
        }

        // 信号量
        public class SemaphoreOps {

            /**
             * 获取信号量许可（阻塞直到获取成功）
             *
             * <p>从指定的信号量中获取一个许可，如果没有可用许可则阻塞等待。
             *
             * @param key 信号量的键名
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public void acquire(String key) throws InterruptedException {
                RedissonCore.getClient(dbKey).getSemaphore(key).acquire();
            }

            /**
             * 获取指定数量的信号量许可（阻塞直到获取成功）
             *
             * <p>从指定的信号量中获取指定数量的许可，如果没有足够许可则阻塞等待。
             *
             * @param key     信号量的键名
             * @param permits 要获取的许可数量
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public void acquire(String key, int permits) throws InterruptedException {
                RedissonCore.getClient(dbKey).getSemaphore(key).acquire(permits);
            }

            /**
             * 尝试获取信号量许可（非阻塞）
             *
             * <p>尝试从指定的信号量中获取一个许可，如果没有可用许可则立即返回。
             *
             * @param key 信号量的键名
             * @return 如果获取许可成功返回 true，否则返回 false
             */
            public boolean tryAcquire(String key) {
                return RedissonCore.getClient(dbKey).getSemaphore(key).tryAcquire();
            }

            /**
             * 尝试获取指定数量的信号量许可（非阻塞）
             *
             * <p>尝试从指定的信号量中获取指定数量的许可，如果没有足够许可则立即返回。
             *
             * @param key     信号量的键名
             * @param permits 要获取的许可数量
             * @return 如果获取许可成功返回 true，否则返回 false
             */
            public boolean tryAcquire(String key, int permits) {
                return RedissonCore.getClient(dbKey).getSemaphore(key).tryAcquire(permits);
            }

            /**
             * 尝试获取信号量许可（支持超时）
             *
             * <p>尝试从指定的信号量中获取一个许可，在指定时间内等待。
             *
             * @param key     信号量的键名
             * @param timeout 等待超时时间
             * @param unit    时间单位
             * @return 如果获取许可成功返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean tryAcquire(String key, long timeout, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getSemaphore(key).tryAcquire(timeout, unit);
            }

            /**
             * 释放信号量许可
             *
             * <p>向指定的信号量释放一个许可。
             *
             * @param key 信号量的键名
             */
            public void release(String key) {
                RedissonCore.getClient(dbKey).getSemaphore(key).release();
            }

            /**
             * 释放指定数量的信号量许可
             *
             * <p>向指定的信号量释放指定数量的许可。
             *
             * @param key     信号量的键名
             * @param permits 要释放的许可数量
             */
            public void release(String key, int permits) {
                RedissonCore.getClient(dbKey).getSemaphore(key).release(permits);
            }

            /**
             * 设置信号量的许可数量
             *
             * <p>设置指定信号量的总许可数量。
             *
             * @param key     信号量的键名
             * @param permits 许可数量
             * @return 如果设置成功返回 true，否则返回 false
             */
            public boolean trySetPermits(String key, int permits) {
                return RedissonCore.getClient(dbKey).getSemaphore(key).trySetPermits(permits);
            }
        }

        // 倒计时器
        public class CountDownLatchOps {

            /**
             * 等待倒计时器归零（阻塞直到计数为0）
             *
             * <p>等待指定的倒计时器计数归零，如果计数不为0则阻塞等待。
             *
             * @param key 倒计时器的键名
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public void await(String key) throws InterruptedException {
                RedissonCore.getClient(dbKey).getCountDownLatch(key).await();
            }

            /**
             * 等待倒计时器归零（支持超时）
             *
             * <p>等待指定的倒计时器计数归零，在指定时间内等待。
             *
             * @param key     倒计时器的键名
             * @param timeout 等待超时时间
             * @param unit    时间单位
             * @return 如果在超时前计数归零返回 true，否则返回 false
             * @throws InterruptedException 如果在等待过程中被中断抛出异常
             */
            public boolean await(String key, long timeout, TimeUnit unit) throws InterruptedException {
                return RedissonCore.getClient(dbKey).getCountDownLatch(key).await(timeout, unit);
            }

            /**
             * 倒计时器计数减一
             *
             * <p>将指定倒计时器的计数减一，如果计数归零则唤醒所有等待的线程。
             *
             * @param key 倒计时器的键名
             */
            public void countDown(String key) {
                RedissonCore.getClient(dbKey).getCountDownLatch(key).countDown();
            }

            /**
             * 获取倒计时器当前计数
             *
             * <p>获取指定倒计时器的当前计数值。
             *
             * @param key 倒计时器的键名
             * @return 当前计数值
             */
            public long getCount(String key) {
                return RedissonCore.getClient(dbKey).getCountDownLatch(key).getCount();
            }

            /**
             * 设置倒计时器的初始计数
             *
             * <p>设置指定倒计时器的初始计数值。
             *
             * @param key   倒计时器的键名
             * @param count 初始计数值
             * @return 如果设置成功返回 true，否则返回 false
             */
            public boolean trySetCount(String key, long count) {
                return RedissonCore.getClient(dbKey).getCountDownLatch(key).trySetCount(count);
            }
        }
    }

    // 获取一个操作默认数据源的链式访问器
    public static RedisAccessor accessor() {
        return new RedisAccessor(null);
    }

    // 获取一个操作指定数据源的链式访问器
    public static RedisAccessor accessor(String dbKey) {
        return new RedisAccessor(dbKey);
    }

    // ==================== TopApi1：顶层静态API ======================

    // 键
    public static class KeyOps {

        /**
         * 异步删除指定的键（推荐使用，性能更好）
         *
         * @param keys 要删除的一个或多个键
         * @return 被成功删除的键的数量
         */
        public static Long unlink(String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.unlink(keys);
            }
        }

        /**
         * 更新键的最后访问时间，不存在的键会被忽略
         *
         * @param keys 要更新的一个或多个键
         * @return 成功更新的键的数量
         */
        public static Long touch(String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.touch(keys);
            }
        }

        /**
         * 复制键到新的键名
         *
         * @param source      源键
         * @param destination 目标键
         * @return 如果复制成功，返回 true；否则返回 false
         */
        public static Boolean copy(String source, String destination) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.copy(source, destination);
            }
        }

        /**
         * 同步删除指定的键
         *
         * @param keys 要删除的一个或多个键
         * @return 被成功删除的键的数量
         */
        public static Long delete(String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.delete(keys);
            }
        }

        /**
         * 同步删除指定的键集合
         *
         * @param keys 要删除的键的集合
         * @return 被成功删除的键的数量
         */
        public static Long delete(Collection<String> keys) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.delete(keys);
            }
        }

        /**
         * 序列化给定键，返回被序列化的值
         *
         * @param key 要序列化的键
         * @return 序列化后的字节数组，如果键不存在则返回 null
         */
        public static byte[] dump(String key) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.dump(key);
            }
        }

        /**
         * 检查键是否存在
         *
         * @param key 要检查的键
         * @return 如果键存在，返回 true；否则返回 false
         */
        public static Boolean hasKey(String key) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.hasKey(key);
            }
        }

        /**
         * 设置键的过期时间
         *
         * @param key     目标键
         * @param timeout 过期时间数值
         * @param unit    时间单位
         * @return 如果成功设置过期时间，返回 true；否则返回 false
         */
        public static Boolean expire(String key, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.expire(key, timeout, unit);
            }
        }

        /**
         * 设置键在指定日期过期
         *
         * @param key  目标键
         * @param date 过期的具体日期
         * @return 如果成功设置过期时间，返回 true；否则返回 false
         */
        public static Boolean expireAt(String key, Date date) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.expireAt(key, date);
            }
        }

        /**
         * 查找匹配模式的所有键
         *
         * @param pattern 匹配模式
         * @return 匹配到的键的集合
         */
        public static Set<String> keys(String pattern) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.keys(pattern);
            }
        }

        /**
         * 将键移动到指定数据库
         *
         * @param key     要移动的键
         * @param dbIndex 目标数据库的索引
         * @return 如果移动成功，返回 true；否则返回 false
         */
        public static Boolean move(String key, int dbIndex) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.move(key, dbIndex);
            }
        }

        /**
         * 移除键的过期时间，使其永久有效
         *
         * @param key 目标键
         * @return 如果成功移除过期时间，返回 true；否则返回 false
         */
        public static Boolean persist(String key) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.persist(key);
            }
        }

        /**
         * 获取键的剩余生存时间
         *
         * @param key  目标键
         * @param unit 返回的时间单位
         * @return 键的剩余生存时间，以指定单位表示
         */
        public static Long getExpire(String key, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.getExpire(key, unit);
            }
        }

        /**
         * 获取键的剩余生存时间（秒）
         *
         * @param key 目标键
         * @return 键的剩余生存时间（以秒为单位）
         */
        public static Long getExpire(String key) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.getExpire(key);
            }
        }

        /**
         * 随机返回一个键
         *
         * @return 随机返回的键名，如果数据库为空则返回 null
         */
        public static String randomKey() {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.randomKey();
            }
        }

        /**
         * 重命名键
         *
         * @param oldKey 旧键名
         * @param newKey 新键名
         */
        public static void rename(String oldKey, String newKey) {
            try (RedisAccessor a = accessor()) {
                a.KeyOps.rename(oldKey, newKey);
            }
        }

        /**
         * 仅当新键名不存在时重命名键
         *
         * @param oldKey 旧键名
         * @param newKey 新键名
         * @return 如果重命名成功，返回 true；否则返回 false
         */
        public static Boolean renameIfAbsent(String oldKey, String newKey) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.renameIfAbsent(oldKey, newKey);
            }
        }

        /**
         * 获取键的数据类型
         *
         * @param key 目标键
         * @return 键的数据类型 (e.g., "string", "list", "set")
         */
        public static String type(String key) {
            try (RedisAccessor a = accessor()) {
                return a.KeyOps.type(key);
            }
        }
    }

    // 字符串
    public static class StringOps {

        /**
         * 获取指定键的值
         *
         * @param key 目标键
         * @return 键对应的值，如果键不存在则返回 null
         */
        public static String get(String key) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.get(key);
            }
        }

        /**
         * 获取字符串指定范围的子串
         *
         * @param key   目标键
         * @param start 起始偏移量（包含）
         * @param end   结束偏移量（包含）
         * @return 指定范围的子串
         */
        public static String getRange(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.getRange(key, start, end);
            }
        }

        /**
         * 设置新值并返回旧值
         *
         * @param key   目标键
         * @param value 新值
         * @return 键的旧值，如果键不存在则返回 null
         */
        public static String getAndSet(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.getAndSet(key, value);
            }
        }

        /**
         * 获取值并删除键
         *
         * @param key 目标键
         * @return 键的值，在获取后键被删除
         */
        public static String getDel(String key) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.getDel(key);
            }
        }

        /**
         * 获取值并设置过期时间
         *
         * @param key     目标键
         * @param timeout 过期时间
         * @param unit    时间单位
         * @return 键的值
         */
        public static String getEx(String key, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.getEx(key, timeout, unit);
            }
        }

        /**
         * 获取指定偏移量的位值
         *
         * @param key    目标键
         * @param offset 位的偏移量
         * @return 如果位值为1，返回 true；如果为0，返回 false
         */
        public static Boolean getBit(String key, long offset) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.getBit(key, offset);
            }
        }

        /**
         * 批量获取多个键的值
         *
         * @param keys 要获取值的键的集合
         * @return 包含键值对的 Map
         */
        public static Map<String, String> multiGet(Collection<String> keys) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.multiGet(keys);
            }
        }

        /**
         * 设置键的值
         *
         * @param key   目标键
         * @param value 要设置的值
         */
        public static void set(String key, String value) {
            try (RedisAccessor a = accessor()) {
                a.StringOps.set(key, value);
            }
        }

        /**
         * 设置指定偏移量的位值
         *
         * @param key    目标键
         * @param offset 位的偏移量
         * @param value  要设置的位值 (true for 1, false for 0)
         * @return 该位在设置前的旧值
         */
        public static boolean setBit(String key, long offset, boolean value) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.setBit(key, offset, value);
            }
        }

        /**
         * 设置键的值并指定过期时间
         *
         * @param key     目标键
         * @param value   要设置的值
         * @param timeout 过期时间
         * @param unit    时间单位
         */
        public static void setEx(String key, String value, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                a.StringOps.setEx(key, value, timeout, unit);
            }
        }

        /**
         * 仅当键不存在时设置值
         *
         * @param key   目标键
         * @param value 要设置的值
         * @return 如果成功设置，返回 true；如果键已存在，返回 false
         */
        public static boolean setIfAbsent(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.setIfAbsent(key, value);
            }
        }

        /**
         * 在指定偏移量处覆写字符串的一部分
         *
         * @param key    目标键
         * @param value  要覆写的值
         * @param offset 起始偏移量
         */
        public static void setRange(String key, String value, long offset) {
            try (RedisAccessor a = accessor()) {
                a.StringOps.setRange(key, value, offset);
            }
        }

        /**
         * 批量设置多个键值对
         *
         * @param maps 包含键值对的 Map
         */
        public static void multiSet(Map<String, String> maps) {
            try (RedisAccessor a = accessor()) {
                a.StringOps.multiSet(maps);
            }
        }

        /**
         * 仅当所有键都不存在时批量设置
         *
         * @param maps 包含键值对的 Map
         * @return 如果所有键都成功设置，返回 true；否则返回 false
         */
        public static boolean multiSetIfAbsent(Map<String, String> maps) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.multiSetIfAbsent(maps);
            }
        }

        /**
         * 在字符串末尾追加值
         *
         * @param key   目标键
         * @param value 要追加的值
         * @return 追加后字符串的总长度
         */
        public static Integer append(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.append(key, value);
            }
        }

        /**
         * 将键的值增加指定的整数
         *
         * @param key       目标键
         * @param increment 增加的量
         * @return 增加后的值
         */
        public static Long incrBy(String key, long increment) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.incrBy(key, increment);
            }
        }

        /**
         * 将字符串值按指定浮点数增量递增
         *
         * @param key       目标键
         * @param increment 增加的浮点数量
         * @return 增加后的值
         */
        public static Double incrByFloat(String key, double increment) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.incrByFloat(key, increment);
            }
        }

        /**
         * 获取字符串值的长度
         *
         * @param key 目标键
         * @return 字符串的长度
         */
        public static Long size(String key) {
            try (RedisAccessor a = accessor()) {
                return a.StringOps.size(key);
            }
        }
    }

    // 哈希
    public static class HashOps {

        /**
         * 获取哈希表中指定字段的值
         *
         * @param key   哈希表的键
         * @param field 字段名
         * @return 字段的值，如果键或字段不存在则返回 null
         */
        public static String hGet(String key, String field) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hGet(key, field);
            }
        }

        /**
         * 获取哈希表中所有字段和值
         *
         * @param key 哈希表的键
         * @return 包含所有字段和值的 Map
         */
        public static Map<String, String> hGetAll(String key) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hGetAll(key);
            }
        }

        /**
         * 批量获取哈希表中多个字段的值
         *
         * @param key    哈希表的键
         * @param fields 要获取的字段名的集合
         * @return 包含字段和值的 Map
         */
        public static Map<String, String> hMultiGet(String key, Collection<String> fields) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hMultiGet(key, fields);
            }
        }

        /**
         * 设置哈希表中字段的值
         *
         * @param key     哈希表的键
         * @param hashKey 字段名
         * @param value   字段值
         */
        public static void hPut(String key, String hashKey, String value) {
            try (RedisAccessor a = accessor()) {
                a.HashOps.hPut(key, hashKey, value);
            }
        }

        /**
         * 批量设置哈希表中多个字段的值
         *
         * @param key  哈希表的键
         * @param maps 包含字段和值的 Map
         */
        public static void hPutAll(String key, Map<String, String> maps) {
            try (RedisAccessor a = accessor()) {
                a.HashOps.hPutAll(key, maps);
            }
        }

        /**
         * 仅当字段不存在时设置字段的值
         *
         * @param key     哈希表的键
         * @param hashKey 字段名
         * @param value   字段值
         * @return 如果字段是新创建的并成功设置，返回 true；如果字段已存在，返回 false
         */
        public static Boolean hPutIfAbsent(String key, String hashKey, String value) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hPutIfAbsent(key, hashKey, value);
            }
        }

        /**
         * 删除哈希表中一个或多个字段
         *
         * @param key    哈希表的键
         * @param fields 要删除的一个或多个字段名
         * @return 被成功删除的字段数量
         */
        public static Long hDelete(String key, String... fields) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hDelete(key, fields);
            }
        }

        /**
         * 检查哈希表中是否存在指定字段
         *
         * @param key   哈希表的键
         * @param field 字段名
         * @return 如果字段存在，返回 true；否则返回 false
         */
        public static boolean hExists(String key, String field) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hExists(key, field);
            }
        }

        /**
         * 将哈希表中字段的整数值增加指定增量
         *
         * @param key       哈希表的键
         * @param field     字段名
         * @param increment 增加的量
         * @return 增加后的值
         */
        public static Long hIncrBy(String key, String field, long increment) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hIncrBy(key, field, increment);
            }
        }

        /**
         * 将哈希表中字段的浮点数值增加指定增量
         *
         * @param key   哈希表的键
         * @param field 字段名
         * @param delta 增加的浮点数量
         * @return 增加后的值
         */
        public static Double hIncrByFloat(String key, String field, double delta) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hIncrByFloat(key, field, delta);
            }
        }

        /**
         * 获取哈希表中所有字段名
         *
         * @param key 哈希表的键
         * @return 所有字段名的集合
         */
        public static Set<String> hKeys(String key) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hKeys(key);
            }
        }

        /**
         * 获取哈希表中字段的数量
         *
         * @param key 哈希表的键
         * @return 字段的数量
         */
        public static Long hSize(String key) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hSize(key);
            }
        }

        /**
         * 获取哈希表中所有字段的值
         *
         * @param key 哈希表的键
         * @return 所有字段值的列表
         */
        public static List<String> hValues(String key) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hValues(key);
            }
        }

        /**
         * 扫描哈希表中的字段和值
         *
         * @param key     哈希表的键
         * @param options 扫描选项
         * @return 包含扫描结果的游标对象
         */
        public static io.lettuce.core.MapScanCursor<String, String> hScan(String key, ScanArgs options) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hScan(key, options);
            }
        }

        /**
         * 随机返回哈希表中的一个字段名
         *
         * @param key 哈希表的键
         * @return 随机返回的字段名
         */
        public static String hRandField(String key) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hRandField(key);
            }
        }

        /**
         * 随机返回哈希表中指定数量的字段名
         *
         * @param key   哈希表的键
         * @param count 要返回的字段数量
         * @return 随机返回的字段名列表
         */
        public static List<String> hRandField(String key, long count) {
            try (RedisAccessor a = accessor()) {
                return a.HashOps.hRandField(key, count);
            }
        }
    }

    // 列表
    public static class ListOps {

        /**
         * 获取列表中指定索引的元素
         *
         * @param key   列表的键
         * @param index 元素的索引
         * @return 指定索引的元素，如果索引超出范围则返回 null
         */
        public static String lIndex(String key, long index) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lIndex(key, index);
            }
        }

        /**
         * 获取列表指定范围内的元素
         *
         * @param key   列表的键
         * @param start 起始索引
         * @param end   结束索引
         * @return 指定范围内的元素列表
         */
        public static List<String> lRange(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRange(key, start, end);
            }
        }

        /**
         * 从列表左侧推入一个元素
         *
         * @param key   列表的键
         * @param value 要推入的元素
         * @return 推入后列表的长度
         */
        public static Long lLeftPush(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPush(key, value);
            }
        }

        /**
         * 从列表左侧推入多个元素
         *
         * @param key   列表的键
         * @param value 要推入的一个或多个元素
         * @return 推入后列表的长度
         */
        public static Long lLeftPushAll(String key, String... value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPushAll(key, value);
            }
        }

        /**
         * 从列表左侧推入集合中的所有元素
         *
         * @param key   列表的键
         * @param value 包含要推入的元素的集合
         * @return 推入后列表的长度
         */
        public static Long lLeftPushAll(String key, Collection<String> value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPushAll(key, value);
            }
        }

        /**
         * 仅当列表存在时从左侧推入元素
         *
         * @param key   列表的键
         * @param value 要推入的元素
         * @return 推入后列表的长度，如果列表不存在则返回 0
         */
        public static Long lLeftPushIfPresent(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPushIfPresent(key, value);
            }
        }

        /**
         * 在列表中指定元素前插入新元素
         *
         * @param key   列表的键
         * @param pivot 参考元素
         * @param value 要插入的新元素
         * @return 插入后列表的长度，如果参考元素不存在则返回 -1
         */
        public static Long lLeftPush(String key, String pivot, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPush(key, pivot, value);
            }
        }

        /**
         * 从列表右侧推入一个元素
         *
         * @param key   列表的键
         * @param value 要推入的元素
         * @return 推入后列表的长度
         */
        public static Long lRightPush(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPush(key, value);
            }
        }

        /**
         * 从列表右侧推入多个元素
         *
         * @param key   列表的键
         * @param value 要推入的一个或多个元素
         * @return 推入后列表的长度
         */
        public static Long lRightPushAll(String key, String... value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPushAll(key, value);
            }
        }

        /**
         * 从列表右侧推入集合中的所有元素
         *
         * @param key   列表的键
         * @param value 包含要推入的元素的集合
         * @return 推入后列表的长度
         */
        public static Long lRightPushAll(String key, Collection<String> value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPushAll(key, value);
            }
        }

        /**
         * 仅当列表存在时从右侧推入元素
         *
         * @param key   列表的键
         * @param value 要推入的元素
         * @return 推入后列表的长度，如果列表不存在则返回 0
         */
        public static Long lRightPushIfPresent(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPushIfPresent(key, value);
            }
        }

        /**
         * 在列表中指定元素后插入新元素
         *
         * @param key   列表的键
         * @param pivot 参考元素
         * @param value 要插入的新元素
         * @return 插入后列表的长度，如果参考元素不存在则返回 -1
         */
        public static Long lRightPush(String key, String pivot, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPush(key, pivot, value);
            }
        }

        /**
         * 设置列表中指定索引的元素值
         *
         * @param key   列表的键
         * @param index 元素的索引
         * @param value 新的元素值
         */
        public static void lSet(String key, long index, String value) {
            try (RedisAccessor a = accessor()) {
                a.ListOps.lSet(key, index, value);
            }
        }

        /**
         * 从列表左侧弹出一个元素
         *
         * @param key 列表的键
         * @return 弹出的元素，如果列表为空则返回 null
         */
        public static String lLeftPop(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLeftPop(key);
            }
        }

        /**
         * 从列表左侧阻塞式弹出一个元素
         *
         * @param key     列表的键
         * @param timeout 阻塞等待时间
         * @param unit    时间单位
         * @return 弹出的元素，如果超时则返回 null
         */
        public static String lBLeftPop(String key, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lBLeftPop(key, timeout, unit);
            }
        }

        /**
         * 从列表右侧弹出一个元素
         *
         * @param key 列表的键
         * @return 弹出的元素，如果列表为空则返回 null
         */
        public static String lRightPop(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPop(key);
            }
        }

        /**
         * 从列表右侧阻塞式弹出一个元素
         *
         * @param key     列表的键
         * @param timeout 阻塞等待时间
         * @param unit    时间单位
         * @return 弹出的元素，如果超时则返回 null
         */
        public static String lBRightPop(String key, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lBRightPop(key, timeout, unit);
            }
        }

        /**
         * 从源列表右侧弹出一个元素，并推入到目标列表左侧
         *
         * @param sourceKey      源列表的键
         * @param destinationKey 目标列表的键
         * @return 被移动的元素
         */
        public static String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRightPopAndLeftPush(sourceKey, destinationKey);
            }
        }

        /**
         * 从源列表右侧阻塞式弹出一个元素，并推入到目标列表左侧
         *
         * @param sourceKey      源列表的键
         * @param destinationKey 目标列表的键
         * @param timeout        阻塞等待时间
         * @param unit           时间单位
         * @return 被移动的元素，如果超时则返回 null
         */
        public static String lBRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lBRightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
            }
        }

        /**
         * 从列表中移除指定数量的匹配元素
         *
         * @param key   列表的键
         * @param count 移除数量 (count > 0: 从头到尾; count < 0: 从尾到头; count = 0: 移除所有)
         * @param value 要移除的元素
         * @return 实际被移除的元素数量
         */
        public static Long lRemove(String key, long count, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lRemove(key, count, value);
            }
        }

        /**
         * 修剪列表，只保留指定范围内的元素
         *
         * @param key   列表的键
         * @param start 起始索引
         * @param end   结束索引
         */
        public static void lTrim(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                a.ListOps.lTrim(key, start, end);
            }
        }

        /**
         * 获取列表的长度
         *
         * @param key 列表的键
         * @return 列表的长度
         */
        public static Long lLen(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ListOps.lLen(key);
            }
        }
    }

    // 集合
    public static class SetOps {

        /**
         * 向集合添加一个或多个成员
         *
         * @param key    集合的键
         * @param values 要添加的一个或多个成员
         * @return 成功添加到集合中的新成员数量
         */
        public static Long sAdd(String key, String... values) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sAdd(key, values);
            }
        }

        /**
         * 获取集合中的所有成员
         *
         * @param key 集合的键
         * @return 包含所有成员的集合
         */
        public static Set<String> sMembers(String key) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sMembers(key);
            }
        }

        /**
         * 获取集合中成员的数量
         *
         * @param key 集合的键
         * @return 集合的基数（成员数量）
         */
        public static Long sSize(String key) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sSize(key);
            }
        }

        /**
         * 判断成员是否在集合中
         *
         * @param key   集合的键
         * @param value 要判断的成员
         * @return 如果成员是集合的元素，返回 true；否则返回 false
         */
        public static Boolean sIsMember(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sIsMember(key, value);
            }
        }

        /**
         * 随机返回集合中的一个成员
         *
         * @param key 集合的键
         * @return 随机返回的成员，如果集合为空则返回 null
         */
        public static String sRandomMember(String key) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sRandomMember(key);
            }
        }

        /**
         * 随机返回集合中指定数量的成员（可重复）
         *
         * @param key   集合的键
         * @param count 要返回的成员数量
         * @return 随机返回的成员列表
         */
        public static List<String> sRandomMembers(String key, long count) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sRandomMembers(key, count);
            }
        }

        /**
         * 随机返回集合中指定数量的不重复成员
         *
         * @param key   集合的键
         * @param count 要返回的成员数量
         * @return 随机返回的唯一成员集合
         */
        public static Set<String> sDistinctRandomMembers(String key, long count) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sDistinctRandomMembers(key, count);
            }
        }

        /**
         * 扫描集合中的成员
         *
         * @param key     集合的键
         * @param options 扫描选项
         * @return 包含扫描结果的游标对象
         */
        public static ValueScanCursor<String> sScan(String key, ScanArgs options) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sScan(key, options);
            }
        }

        /**
         * 计算两个集合的交集
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @return 两个集合交集的成员集合
         */
        public static Set<String> sIntersect(String key, String otherKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sIntersect(key, otherKey);
            }
        }

        /**
         * 计算多个集合的交集
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @return 多个集合交集的成员集合
         */
        public static Set<String> sIntersect(String key, Collection<String> otherKeys) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sIntersect(key, otherKeys);
            }
        }

        /**
         * 计算两个集合的交集并存储到新集合
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @param destKey  存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sIntersectAndStore(String key, String otherKey, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sIntersectAndStore(key, otherKey, destKey);
            }
        }

        /**
         * 计算多个集合的交集并存储到新集合
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @param destKey   存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sIntersectAndStore(key, otherKeys, destKey);
            }
        }

        /**
         * 计算两个集合的并集
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @return 两个集合并集的成员集合
         */
        public static Set<String> sUnion(String key, String otherKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sUnion(key, otherKey);
            }
        }

        /**
         * 计算多个集合的并集
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @return 多个集合并集的成员集合
         */
        public static Set<String> sUnion(String key, Collection<String> otherKeys) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sUnion(key, otherKeys);
            }
        }

        /**
         * 计算两个集合的并集并存储到新集合
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @param destKey  存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sUnionAndStore(String key, String otherKey, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sUnionAndStore(key, otherKey, destKey);
            }
        }

        /**
         * 计算多个集合的并集并存储到新集合
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @param destKey   存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sUnionAndStore(key, otherKeys, destKey);
            }
        }

        /**
         * 计算两个集合的差集
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @return 差集（在第一个集合但不在第二个集合）的成员集合
         */
        public static Set<String> sDifference(String key, String otherKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sDifference(key, otherKey);
            }
        }

        /**
         * 计算多个集合的差集
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @return 差集（在第一个集合但不在任何其他集合）的成员集合
         */
        public static Set<String> sDifference(String key, Collection<String> otherKeys) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sDifference(key, otherKeys);
            }
        }

        /**
         * 计算两个集合的差集并存储到新集合
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @param destKey  存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sDifferenceAndStore(String key, String otherKey, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sDifferenceAndStore(key, otherKey, destKey);
            }
        }

        /**
         * 计算多个集合的差集并存储到新集合
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @param destKey   存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long sDifferenceAndStore(String key, Collection<String> otherKeys, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sDifferenceAndStore(key, otherKeys, destKey);
            }
        }

        /**
         * 从集合中移除一个或多个成员
         *
         * @param key    集合的键
         * @param values 要移除的一个或多个成员
         * @return 成功移除的成员数量
         */
        public static Long sRemove(String key, String... values) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sRemove(key, values);
            }
        }

        /**
         * 从集合中随机移除并返回一个成员
         *
         * @param key 集合的键
         * @return 被移除的成员，如果集合为空则返回 null
         */
        public static String sPop(String key) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sPop(key);
            }
        }

        /**
         * 将指定成员从源集合移动到目标集合
         *
         * @param key     源集合的键
         * @param value   要移动的成员
         * @param destKey 目标集合的键
         * @return 如果移动成功，返回 true；否则返回 false
         */
        public static Boolean sMove(String key, String value, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.SetOps.sMove(key, value, destKey);
            }
        }
    }

    // 有序集合
    public static class ZSetOps {

        /**
         * 向有序集合添加一个成员
         *
         * @param key   有序集合的键
         * @param value 成员
         * @param score 分数
         * @return 如果成员是新添加的，返回 true；如果是更新分数，返回 false
         */
        public static Boolean zAdd(String key, String value, double score) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zAdd(key, value, score);
            }
        }

        /**
         * 向有序集合添加多个成员
         *
         * @param key    有序集合的键
         * @param values 包含成员和分数的 ScoredValue 集合
         * @return 成功添加的新成员数量
         */
        public static Long zAdd(String key, Set<ScoredValue<String>> values) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zAdd(key, values);
            }
        }

        /**
         * 按索引范围获取有序集合的成员
         *
         * @param key   有序集合的键
         * @param start 起始索引
         * @param end   结束索引
         * @return 指定索引范围内的成员集合
         */
        public static Set<String> zRange(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRange(key, start, end);
            }
        }

        /**
         * 按索引范围获取有序集合的成员及其分数
         *
         * @param key   有序集合的键
         * @param start 起始索引
         * @param end   结束索引
         * @return 指定索引范围内的 ScoredValue 集合
         */
        public static Set<ScoredValue<String>> zRangeWithScores(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRangeWithScores(key, start, end);
            }
        }

        /**
         * 按分数范围获取有序集合的成员
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 指定分数范围内的成员集合
         */
        public static Set<String> zRangeByScore(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRangeByScore(key, min, max);
            }
        }

        /**
         * 按分数范围获取有序集合的成员及其分数
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 指定分数范围内的 ScoredValue 集合
         */
        public static Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRangeByScoreWithScores(key, min, max);
            }
        }

        /**
         * 按分数范围和分页获取有序集合的成员及其分数
         *
         * @param key   有序集合的键
         * @param min   最小分数
         * @param max   最大分数
         * @param start 偏移量
         * @param end   数量
         * @return 指定分数范围和分页的 ScoredValue 集合
         */
        public static Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRangeByScoreWithScores(key, min, max, start, end);
            }
        }

        /**
         * 按索引范围逆序获取有序集合的成员
         *
         * @param key   有序集合的键
         * @param start 起始索引
         * @param end   结束索引
         * @return 指定索引范围内的成员集合（逆序）
         */
        public static Set<String> zReverseRange(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRange(key, start, end);
            }
        }

        /**
         * 按索引范围逆序获取有序集合的成员及其分数
         *
         * @param key   有序集合的键
         * @param start 起始索引
         * @param end   结束索引
         * @return 指定索引范围内的 ScoredValue 集合（逆序）
         */
        public static Set<ScoredValue<String>> zReverseRangeWithScores(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRangeWithScores(key, start, end);
            }
        }

        /**
         * 按分数范围逆序获取有序集合的成员
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 指定分数范围内的成员集合（逆序）
         */
        public static Set<String> zReverseRangeByScore(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRangeByScore(key, min, max);
            }
        }

        /**
         * 按分数范围逆序获取有序集合的成员及其分数
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 指定分数范围内的 ScoredValue 集合（逆序）
         */
        public static Set<ScoredValue<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRangeByScoreWithScores(key, min, max);
            }
        }

        /**
         * 按分数范围和分页逆序获取有序集合的成员
         *
         * @param key   有序集合的键
         * @param min   最小分数
         * @param max   最大分数
         * @param start 偏移量
         * @param end   数量
         * @return 指定分数范围和分页的成员集合（逆序）
         */
        public static Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRangeByScore(key, min, max, start, end);
            }
        }

        /**
         * 获取成员在有序集合中的排名（升序）
         *
         * @param key   有序集合的键
         * @param value 成员
         * @return 成员的排名（从0开始），如果成员不存在则返回 null
         */
        public static Long zRank(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRank(key, value);
            }
        }

        /**
         * 获取成员在有序集合中的排名（降序）
         *
         * @param key   有序集合的键
         * @param value 成员
         * @return 成员的排名（从0开始），如果成员不存在则返回 null
         */
        public static Long zReverseRank(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zReverseRank(key, value);
            }
        }

        /**
         * 计算有序集合中指定分数区间的成员数量
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 指定分数区间的成员数量
         */
        public static Long zCount(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zCount(key, min, max);
            }
        }

        /**
         * 获取有序集合的成员数量
         *
         * @param key 有序集合的键
         * @return 成员数量
         */
        public static Long zSize(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zSize(key);
            }
        }

        /**
         * 获取有序集合的基数（成员数量）
         *
         * @param key 有序集合的键
         * @return 成员数量
         */
        public static Long zZCard(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zZCard(key);
            }
        }

        /**
         * 获取有序集合中指定成员的分数
         *
         * @param key   有序集合的键
         * @param value 成员
         * @return 成员的分数，如果成员不存在则返回 null
         */
        public static Double zScore(String key, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zScore(key, value);
            }
        }

        /**
         * 计算两个有序集合的并集并存储到新集合
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @param destKey  存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long zUnionAndStore(String key, String otherKey, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zUnionAndStore(key, otherKey, destKey);
            }
        }

        /**
         * 计算多个有序集合的并集并存储到新集合
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @param destKey   存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zUnionAndStore(key, otherKeys, destKey);
            }
        }

        /**
         * 计算两个有序集合的交集并存储到新集合
         *
         * @param key      第一个集合的键
         * @param otherKey 第二个集合的键
         * @param destKey  存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long zIntersectAndStore(String key, String otherKey, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zIntersectAndStore(key, otherKey, destKey);
            }
        }

        /**
         * 计算多个有序集合的交集并存储到新集合
         *
         * @param key       第一个集合的键
         * @param otherKeys 其他集合的键的集合
         * @param destKey   存储结果的目标集合的键
         * @return 结果集合中的成员数量
         */
        public static Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zIntersectAndStore(key, otherKeys, destKey);
            }
        }

        /**
         * 扫描有序集合中的成员和分数
         *
         * @param key     有序集合的键
         * @param options 扫描选项
         * @return 包含扫描结果的游标对象
         */
        public static ScoredValueScanCursor<String> zScan(String key, ScanArgs options) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zScan(key, options);
            }
        }

        /**
         * 从有序集合中移除一个或多个成员
         *
         * @param key    有序集合的键
         * @param values 要移除的一个或多个成员
         * @return 成功移除的成员数量
         */
        public static Long zRemove(String key, String... values) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRemove(key, values);
            }
        }

        /**
         * 为有序集合中指定成员的分数增加一个增量
         *
         * @param key   有序集合的键
         * @param value 成员
         * @param delta 增加的分数
         * @return 成员的新分数
         */
        public static Double zIncrementScore(String key, String value, double delta) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zIncrementScore(key, value, delta);
            }
        }

        /**
         * 移除有序集合中指定排名范围的成员
         *
         * @param key   有序集合的键
         * @param start 起始排名
         * @param end   结束排名
         * @return 被移除的成员数量
         */
        public static Long zRemoveRange(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRemoveRange(key, start, end);
            }
        }

        /**
         * 移除有序集合中指定分数范围的成员
         *
         * @param key 有序集合的键
         * @param min 最小分数
         * @param max 最大分数
         * @return 被移除的成员数量
         */
        public static Long zRemoveRangeByScore(String key, double min, double max) {
            try (RedisAccessor a = accessor()) {
                return a.ZSetOps.zRemoveRangeByScore(key, min, max);
            }
        }
    }

    // ==================== TopApi2：顶层静态API ======================

    // 位图
    public static class BitmapOps {

        /**
         * 统计位图中设置为1的位数量
         *
         * @param key 位图的键
         * @return 位图中被设置为 1 的位的数量
         */
        public static Long bitCount(String key) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitCount(key);
            }
        }

        /**
         * 统计位图指定范围内设置为1的位数量
         *
         * @param key   位图的键
         * @param start 起始字节索引
         * @param end   结束字节索引
         * @return 指定范围内被设置为 1 的位的数量
         */
        public static Long bitCount(String key, long start, long end) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitCount(key, start, end);
            }
        }

        /**
         * 查找位图中第一个指定状态（0或1）的位的位置
         *
         * @param key   位图的键
         * @param state 要查找的位状态 (true for 1, false for 0)
         * @return 第一个匹配状态的位的位置，如果不存在则返回 -1
         */
        public static Long bitPos(String key, boolean state) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitPos(key, state);
            }
        }

        /**
         * 从指定位置开始，查找位图中第一个指定状态（0或1）的位的位置
         *
         * @param key   位图的键
         * @param state 要查找的位状态 (true for 1, false for 0)
         * @param start 起始字节索引
         * @return 第一个匹配状态的位的位置，如果不存在则返回 -1
         */
        public static Long bitPos(String key, boolean state, long start) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitPos(key, state, start);
            }
        }

        /**
         * 对多个位图执行 AND 运算，并将结果保存到目标位图
         *
         * @param destination 存储结果的目标位图的键
         * @param keys        参与运算的一个或多个源位图的键
         * @return 目标位图的长度（以字节为单位）
         */
        public static Long bitOpAnd(String destination, String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitOpAnd(destination, keys);
            }
        }

        /**
         * 对多个位图执行 OR 运算，并将结果保存到目标位图
         *
         * @param destination 存储结果的目标位图的键
         * @param keys        参与运算的一个或多个源位图的键
         * @return 目标位图的长度（以字节为单位）
         */
        public static Long bitOpOr(String destination, String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitOpOr(destination, keys);
            }
        }

        /**
         * 对多个位图执行 XOR 运算，并将结果保存到目标位图
         *
         * @param destination 存储结果的目标位图的键
         * @param keys        参与运算的一个或多个源位图的键
         * @return 目标位图的长度（以字节为单位）
         */
        public static Long bitOpXor(String destination, String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitOpXor(destination, keys);
            }
        }

        /**
         * 对一个位图执行 NOT 运算，并将结果保存到目标位图
         *
         * @param destination 存储结果的目标位图的键
         * @param source      源位图的键
         * @return 目标位图的长度（以字节为单位）
         */
        public static Long bitOpNot(String destination, String source) {
            try (RedisAccessor a = accessor()) {
                return a.BitmapOps.bitOpNot(destination, source);
            }
        }
    }

    // 基数统计
    public static class HyperLogLogOps {

        /**
         * 向HyperLogLog添加元素
         *
         * @param key      HyperLogLog 的键
         * @param elements 要添加的一个或多个元素
         * @return 如果 HyperLogLog 的内部寄存器至少被修改了一个，则返回 true
         */
        public static boolean pfAdd(String key, Object... elements) {
            try (RedisAccessor a = accessor()) {
                return a.HyperLogLogOps.pfAdd(key, elements);
            }
        }

        /**
         * 获取一个或多个HyperLogLog的并集的基数估算值
         *
         * @param keys 一个或多个 HyperLogLog 的键
         * @return 这些 HyperLogLog 并集的基数估算值
         */
        public static long pfCount(String... keys) {
            try (RedisAccessor a = accessor()) {
                return a.HyperLogLogOps.pfCount(keys);
            }
        }

        /**
         * 合并多个HyperLogLog到目标键
         *
         * @param destKey    目标 HyperLogLog 的键
         * @param sourceKeys 一个或多个源 HyperLogLog 的键
         */
        public static void pfMerge(String destKey, String... sourceKeys) {
            try (RedisAccessor a = accessor()) {
                a.HyperLogLogOps.pfMerge(destKey, sourceKeys);
            }
        }
    }

    // 地理空间
    public static class GeoOps {

        /**
         * 添加地理空间位置信息
         *
         * @param key       地理空间索引的键
         * @param longitude 经度
         * @param latitude  纬度
         * @param member    成员对象
         * @return 成功添加的成员数量
         */
        public static Long geoAdd(String key, double longitude, double latitude, Object member) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoAdd(key, longitude, latitude, member);
            }
        }

        /**
         * 批量添加地理空间位置信息
         *
         * @param key        地理空间索引的键
         * @param geoEntries 包含成员和其地理位置的 Map
         * @return 成功添加的成员数量
         */
        public static Long geoAdd(String key, Map<Object, org.redisson.api.GeoPosition> geoEntries) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoAdd(key, geoEntries);
            }
        }

        /**
         * 获取指定成员的地理位置坐标
         *
         * @param key     地理空间索引的键
         * @param members 一个或多个成员对象
         * @return 包含成员和其地理位置的 Map
         */
        public static Map<Object, org.redisson.api.GeoPosition> geoPos(String key, Object... members) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoPos(key, members);
            }
        }

        /**
         * 计算两个成员之间的距离
         *
         * @param key     地理空间索引的键
         * @param member1 第一个成员
         * @param member2 第二个成员
         * @param unit    距离单位
         * @return 两个成员之间的距离，如果任一成员不存在则返回 null
         */
        public static Double geoDist(String key, Object member1, Object member2, org.redisson.api.GeoUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoDist(key, member1, member2, unit);
            }
        }

        /**
         * 根据坐标查找指定半径内的成员及距离
         *
         * @param key       地理空间索引的键
         * @param longitude 中心点经度
         * @param latitude  中心点纬度
         * @param radius    半径
         * @param unit      半径单位
         * @return 包含成员及其距离的 Map
         */
        public static Map<Object, Double> geoRadius(String key, double longitude, double latitude, double radius, org.redisson.api.GeoUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoRadius(key, longitude, latitude, radius, unit);
            }
        }

        /**
         * 根据坐标查找指定半径内的成员（限制数量）
         *
         * @param key       地理空间索引的键
         * @param longitude 中心点经度
         * @param latitude  中心点纬度
         * @param radius    半径
         * @param unit      半径单位
         * @param count     返回的最大成员数量
         * @return 成员对象列表
         */
        public static List<Object> geoRadius(String key, double longitude, double latitude, double radius, org.redisson.api.GeoUnit unit, int count) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoRadius(key, longitude, latitude, radius, unit, count);
            }
        }

        /**
         * 根据成员查找指定半径内的其他成员及距离
         *
         * @param key    地理空间索引的键
         * @param member 中心点成员
         * @param radius 半径
         * @param unit   半径单位
         * @return 包含成员及其距离的 Map
         */
        public static Map<Object, Double> geoRadiusByMember(String key, Object member, double radius, org.redisson.api.GeoUnit unit) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoRadiusByMember(key, member, radius, unit);
            }
        }

        /**
         * 根据成员查找指定半径内的其他成员（限制数量）
         *
         * @param key    地理空间索引的键
         * @param member 中心点成员
         * @param radius 半径
         * @param unit   半径单位
         * @param count  返回的最大成员数量
         * @return 成员对象列表
         */
        public static List<Object> geoRadiusByMember(String key, Object member, double radius, org.redisson.api.GeoUnit unit, int count) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoRadiusByMember(key, member, radius, unit, count);
            }
        }

        /**
         * 使用高级参数搜索地理空间数据
         *
         * @param key  地理空间索引的键
         * @param args 搜索参数对象
         * @return 匹配搜索条件的成员列表
         */
        public static List<Object> geoSearch(String key, org.redisson.api.geo.GeoSearchArgs args) {
            try (RedisAccessor a = accessor()) {
                return a.GeoOps.geoSearch(key, args);
            }
        }
    }

    // 布隆过滤器
    public static class BloomFilterOps {

        /**
         * 向布隆过滤器添加元素
         *
         * @param key     布隆过滤器的键名
         * @param element 要添加的元素
         * @return 如果元素被成功添加返回 true，否则返回 false
         */
        public static boolean add(String key, Object element) {
            return RedissonCore.getClient().getBloomFilter(key).add(element);
        }

        /**
         * 检查元素是否可能存在于布隆过滤器中
         *
         * @param key     布隆过滤器的键名
         * @param element 要检查的元素
         * @return 如果元素可能存在返回 true，一定不存在返回 false
         */
        public static boolean contains(String key, Object element) {
            return RedissonCore.getClient().getBloomFilter(key).contains(element);
        }

        /**
         * 初始化布隆过滤器
         *
         * @param key                    布隆过滤器的键名
         * @param expectedInsertions     预期插入的元素数量
         * @param falseProbability       误判率（0.0 到 1.0 之间）
         * @return 如果初始化成功返回 true，否则返回 false
         */
        public static boolean tryInit(String key, long expectedInsertions, double falseProbability) {
            return RedissonCore.getClient().getBloomFilter(key).tryInit(expectedInsertions, falseProbability);
        }

        /**
         * 获取布隆过滤器的预期插入数量
         *
         * @param key 布隆过滤器的键名
         * @return 预期插入数量
         */
        public static long getExpectedInsertions(String key) {
            return RedissonCore.getClient().getBloomFilter(key).getExpectedInsertions();
        }

        /**
         * 获取布隆过滤器的误判率
         *
         * @param key 布隆过滤器的键名
         * @return 误判率
         */
        public static double getFalseProbability(String key) {
            return RedissonCore.getClient().getBloomFilter(key).getFalseProbability();
        }

        /**
         * 获取布隆过滤器的哈希函数数量
         *
         * @param key 布隆过滤器的键名
         * @return 哈希函数数量
         */
        public static long getHashIterations(String key) {
            return RedissonCore.getClient().getBloomFilter(key).getHashIterations();
        }

        /**
         * 获取布隆过滤器的位数组大小
         *
         * @param key 布隆过滤器的键名
         * @return 位数组大小
         */
        public static long getSize(String key) {
            return RedissonCore.getClient().getBloomFilter(key).getSize();
        }

        /**
         * 获取布隆过滤器中已添加的元素数量
         *
         * @param key 布隆过滤器的键名
         * @return 已添加的元素数量
         */
        public static long count(String key) {
            return RedissonCore.getClient().getBloomFilter(key).count();
        }
    }

    // ==================== TopApi3：顶层静态API ======================

    // 服务器
    public static class ServerOps {

        /**
         * 测试与 Redis 服务器的连接
         *
         * @return 如果连接正常，通常返回 "PONG"
         */
        public static String ping() {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.ping();
            }
        }

        /**
         * 获取当前数据库中的键的数量
         *
         * @return 键的数量
         */
        public static Long dbSize() {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.dbSize();
            }
        }

        /**
         * 清空当前数据库中的所有键
         *
         * @return 操作结果字符串，通常为 "OK"
         */
        public static String flushDb() {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.flushDb();
            }
        }

        /**
         * 清空所有数据库中的所有键
         *
         * @return 操作结果字符串，通常为 "OK"
         */
        public static String flushAll() {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.flushAll();
            }
        }

        /**
         * 获取 Redis 服务器的信息和统计数据
         *
         * @return 包含服务器信息的字符串
         */
        public static String info() {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.info();
            }
        }

        /**
         * 获取 Redis 服务器指定部分的信息
         *
         * @param section 要获取信息的部分 (e.g., "cpu", "memory")
         * @return 包含指定部分信息的字符串
         */
        public static String info(String section) {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.info(section);
            }
        }

        /**
         * 获取 Redis 服务器的配置参数
         *
         * @param parameter 配置参数的模式 (e.g., "timeout", "*-max-*")
         * @return 包含配置参数和值的 Map
         */
        public static Map<String, String> configGet(String parameter) {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.configGet(parameter);
            }
        }

        /**
         * 动态修改 Redis 服务器的配置参数
         *
         * @param parameter 配置参数名
         * @param value     新的参数值
         * @return 操作结果字符串，通常为 "OK"
         */
        public static String configSet(String parameter, String value) {
            try (RedisAccessor a = accessor()) {
                return a.ServerOps.configSet(parameter, value);
            }
        }
    }

    // Lua脚本
    public static class ScriptOps {

        /**
         * 执行 Lua 脚本
         *
         * @param <T>    返回类型
         * @param script Lua 脚本内容
         * @param type   脚本的返回类型
         * @param keys   脚本中使用的键
         * @param values 脚本中使用的参数
         * @return 脚本的执行结果
         */
        public static <T> T eval(String script, ScriptOutputType type, String[] keys, String... values) {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.eval(script, type, keys, values);
            }
        }

        /**
         * 通过 SHA1 校验和执行已缓存的 Lua 脚本
         *
         * @param <T>    返回类型
         * @param sha    脚本的 SHA1 校验和
         * @param type   脚本的返回类型
         * @param keys   脚本中使用的键
         * @param values 脚本中使用的参数
         * @return 脚本的执行结果
         */
        public static <T> T evalSha(String sha, ScriptOutputType type, String[] keys, String... values) {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.evalSha(sha, type, keys, values);
            }
        }

        /**
         * 将 Lua 脚本加载到 Redis 脚本缓存中
         *
         * @param script Lua 脚本内容
         * @return 脚本的 SHA1 校验和
         */
        public static String scriptLoad(String script) {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.scriptLoad(script);
            }
        }

        /**
         * 检查指定的脚本是否存在于缓存中
         *
         * @param sha1 要检查的一个或多个脚本的 SHA1 校验和
         * @return 一个布尔值列表，对应每个 SHA1 是否存在
         */
        public static List<Boolean> scriptExists(String... sha1) {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.scriptExists(sha1);
            }
        }

        /**
         * 清空 Redis 服务器上的所有 Lua 脚本缓存
         *
         * @return 操作结果字符串，通常为 "OK"
         */
        public static String scriptFlush() {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.scriptFlush();
            }
        }

        /**
         * 终止当前正在执行的 Lua 脚本
         *
         * @return 操作结果字符串，通常为 "OK"
         */
        public static String scriptKill() {
            try (RedisAccessor a = accessor()) {
                return a.ScriptOps.scriptKill();
            }
        }
    }

    // Steam流
    public static class StreamOps {

        /**
         * 添加单个字段消息到流
         *
         * @param key   流的键
         * @param field 消息的字段
         * @param value 消息的值
         * @return 生成的消息 ID
         */
        public static StreamMessageId add(String key, String field, Object value) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.add(key, field, value);
            }
        }

        /**
         * 添加多个字段消息到流
         *
         * @param key    流的键
         * @param fields 包含消息字段和值的 Map
         * @return 生成的消息 ID
         */
        public static StreamMessageId addAll(String key, Map<String, Object> fields) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.addAll(key, fields);
            }
        }

        /**
         * 从流中读取消息
         *
         * @param key     流的键
         * @param startId 起始消息 ID
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> read(String key, StreamMessageId startId) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.read(key, startId);
            }
        }

        /**
         * 从流中读取指定数量的消息
         *
         * @param key     流的键
         * @param count   要读取的消息数量
         * @param startId 起始消息 ID
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> read(String key, int count, StreamMessageId startId) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.read(key, count, startId);
            }
        }

        /**
         * 读取流中指定范围的消息
         *
         * @param key     流的键
         * @param startId 起始消息 ID（包含）
         * @param endId   结束消息 ID（包含）
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> readRange(String key, StreamMessageId startId, StreamMessageId endId) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.readRange(key, startId, endId);
            }
        }

        /**
         * 读取流中指定范围和数量的消息
         *
         * @param key     流的键
         * @param count   最大消息数量
         * @param startId 起始消息 ID（包含）
         * @param endId   结束消息 ID（包含）
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> readRange(String key, int count, StreamMessageId startId, StreamMessageId endId) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.readRange(key, count, startId, endId);
            }
        }

        /**
         * 创建消费者组
         *
         * @param key       流的键
         * @param groupName 消费者组名称
         */
        public static void createGroup(String key, String groupName) {
            try (RedisAccessor a = accessor()) {
                a.StreamOps.createGroup(key, groupName);
            }
        }

        /**
         * 从指定消息 ID 开始创建消费者组
         *
         * @param key       流的键
         * @param groupName 消费者组名称
         * @param id        起始消息 ID
         */
        public static void createGroup(String key, String groupName, StreamMessageId id) {
            try (RedisAccessor a = accessor()) {
                a.StreamOps.createGroup(key, groupName, id);
            }
        }

        /**
         * 以消费者组成员身份读取消息
         *
         * @param key          流的键
         * @param groupName    消费者组名称
         * @param consumerName 消费者名称
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.readGroup(key, groupName, consumerName);
            }
        }

        /**
         * 以消费者组成员身份读取指定数量的消息
         *
         * @param key          流的键
         * @param groupName    消费者组名称
         * @param consumerName 消费者名称
         * @param count        读取数量
         * @return 包含消息 ID 和消息内容的 Map
         */
        public static Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName, int count) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.readGroup(key, groupName, consumerName, count);
            }
        }

        /**
         * 确认消息已被处理
         *
         * @param key       流的键
         * @param groupName 消费者组名称
         * @param ids       一个或多个要确认的消息 ID
         * @return 成功确认的消息数量
         */
        public static long ack(String key, String groupName, StreamMessageId... ids) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.ack(key, groupName, ids);
            }
        }

        /**
         * 从流中删除指定消息
         *
         * @param key 流的键
         * @param ids 一个或多个要删除的消息 ID
         * @return 成功删除的消息数量
         */
        public static long delete(String key, StreamMessageId... ids) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.delete(key, ids);
            }
        }

        /**
         * 获取流中的消息数量
         *
         * @param key 流的键
         * @return 消息数量
         */
        public static long length(String key) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.length(key);
            }
        }

        /**
         * 修剪流，只保留最新的指定数量的消息
         *
         * @param key   流的键
         * @param count 要保留的消息数量
         * @return 被删除的消息数量
         */
        public static long trim(String key, int count) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.trim(key, count);
            }
        }

        /**
         * 以非精确方式修剪流，效率更高
         *
         * @param key   流的键
         * @param count 大约要保留的消息数量
         * @return 被删除的消息数量
         */
        public static long trimNonStrict(String key, int count) {
            try (RedisAccessor a = accessor()) {
                return a.StreamOps.trimNonStrict(key, count);
            }
        }
    }

    // 发布订阅
    public static class PubSubOps {

        /**
         * 向指定主题发布消息
         *
         * @param topic   目标主题
         * @param message 要发布的消息
         * @return 接收到消息的客户端数量
         */
        public static long publish(String topic, Object message) {
            try (RedisAccessor a = accessor()) {
                return a.PubSubOps.publish(topic, message);
            }
        }

        /**
         * 订阅一个主题以接收消息
         * <p>
         * 注意：这是一个长期操作，不应使用 try-with-resources 自动关闭。
         *
         * @param <T>      消息类型
         * @param topic    要订阅的主题
         * @param listener 消息监听器
         * @return 监听器的注册 ID，可用于取消订阅
         */
        public static <T> int subscribe(String topic, MessageListener<T> listener) {
            // 订阅操作是长期的，不应使用 try-with-resources
            // 直接使用默认数据源的 Redisson 客户端
            RTopic rTopic = RedissonCore.getClient().getTopic(topic);
            return rTopic.addListener(Object.class, (channel, msg) -> {
                try {
                    @SuppressWarnings("unchecked")
                    T typedMsg = (T) msg;
                    listener.onMessage(channel, typedMsg);
                } catch (ClassCastException e) {
                    LoggerCore.log("ERROR", "Pub/Sub 消息类型转换失败 on topic '" + topic + "': " + e.getMessage());
                }
            });
        }

        /**
         * 取消对主题的订阅
         *
         * @param topic      目标主题
         * @param listenerId 要移除的监听器的注册 ID
         */
        public static void unsubscribe(String topic, int listenerId) {
            // 取消订阅操作也不需要 accessor
            RTopic rTopic = RedissonCore.getClient().getTopic(topic);
            rTopic.removeListener(listenerId);
        }
    }

    // ==================== TopApi4：顶层静态API ======================

    // 可重入锁
    public static class ReentrantLockOps {

        /**
         * 获取可重入锁（阻塞直到获取成功）
         *
         * @param key 可重入锁的键名
         */
        public static void lock(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReentrantLockOps.lock(key);
            }
        }

        /**
         * 获取可重入锁并指定租约时间
         *
         * @param key       可重入锁的键名
         * @param leaseTime 锁的租约时间
         * @param unit      时间单位
         */
        public static void lock(String key, long leaseTime, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                a.ReentrantLockOps.lock(key, leaseTime, unit);
            }
        }

        /**
         * 尝试获取可重入锁（非阻塞）
         *
         * @param key 可重入锁的键名
         * @return 如果成功获取锁，返回 true；否则返回 false
         */
        public static boolean tryLock(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ReentrantLockOps.tryLock(key);
            }
        }

        /**
         * 尝试获取可重入锁（指定等待时间和租约时间）
         *
         * @param key       可重入锁的键名
         * @param waitTime  最长等待时间
         * @param leaseTime 锁的租约时间
         * @param unit      waitTime 和 leaseTime 的时间单位
         * @return 如果成功获取锁，返回 true；否则返回 false
         * @throws InterruptedException 如果当前线程在等待期间被中断
         */
        public static boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.ReentrantLockOps.tryLock(key, waitTime, leaseTime, unit);
            }
        }

        /**
         * 释放可重入锁
         *
         * @param key 可重入锁的键名
         */
        public static void unlock(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReentrantLockOps.unlock(key);
            }
        }
    }

    // 公平锁
    public static class FairLockOps {

        /**
         * 获取公平锁（阻塞直到获取成功）
         *
         * @param key 公平锁的键名
         */
        public static void lock(String key) {
            try (RedisAccessor a = accessor()) {
                a.FairLockOps.lock(key);
            }
        }

        /**
         * 获取公平锁并设置自动释放时间
         *
         * @param key       公平锁的键名
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         */
        public static void lock(String key, long leaseTime, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                a.FairLockOps.lock(key, leaseTime, unit);
            }
        }

        /**
         * 尝试获取公平锁（非阻塞）
         *
         * @param key 公平锁的键名
         * @return 如果获取锁成功返回 true，否则返回 false
         */
        public static boolean tryLock(String key) {
            try (RedisAccessor a = accessor()) {
                return a.FairLockOps.tryLock(key);
            }
        }

        /**
         * 尝试获取公平锁（支持等待时间和自动释放时间）
         *
         * @param key       公平锁的键名
         * @param waitTime  等待时间
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         * @return 如果获取锁成功返回 true，否则返回 false
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.FairLockOps.tryLock(key, waitTime, leaseTime, unit);
            }
        }

        /**
         * 释放公平锁
         *
         * @param key 公平锁的键名
         */
        public static void unlock(String key) {
            try (RedisAccessor a = accessor()) {
                a.FairLockOps.unlock(key);
            }
        }
    }

    // 读写锁
    public static class ReadWriteLockOps {

        /**
         * 获取读锁（阻塞直到获取成功）
         *
         * @param key 读写锁的键名
         */
        public static void readLock(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.readLock(key);
            }
        }

        /**
         * 获取读锁并设置自动释放时间
         *
         * @param key       读写锁的键名
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         */
        public static void readLock(String key, long leaseTime, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.readLock(key, leaseTime, unit);
            }
        }

        /**
         * 尝试获取读锁（非阻塞）
         *
         * @param key 读写锁的键名
         * @return 如果获取锁成功返回 true，否则返回 false
         */
        public static boolean tryReadLock(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ReadWriteLockOps.tryReadLock(key);
            }
        }

        /**
         * 尝试获取读锁（支持等待时间和自动释放时间）
         *
         * @param key       读写锁的键名
         * @param waitTime  等待时间
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         * @return 如果获取锁成功返回 true，否则返回 false
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static boolean tryReadLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.ReadWriteLockOps.tryReadLock(key, waitTime, leaseTime, unit);
            }
        }

        /**
         * 释放读锁
         *
         * @param key 读写锁的键名
         */
        public static void unlockRead(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.unlockRead(key);
            }
        }

        /**
         * 获取写锁（阻塞直到获取成功）
         *
         * @param key 读写锁的键名
         */
        public static void writeLock(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.writeLock(key);
            }
        }

        /**
         * 获取写锁并设置自动释放时间
         *
         * @param key       读写锁的键名
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         */
        public static void writeLock(String key, long leaseTime, TimeUnit unit) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.writeLock(key, leaseTime, unit);
            }
        }

        /**
         * 尝试获取写锁（非阻塞）
         *
         * @param key 读写锁的键名
         * @return 如果获取锁成功返回 true，否则返回 false
         */
        public static boolean tryWriteLock(String key) {
            try (RedisAccessor a = accessor()) {
                return a.ReadWriteLockOps.tryWriteLock(key);
            }
        }

        /**
         * 尝试获取写锁（支持等待时间和自动释放时间）
         *
         * @param key       读写锁的键名
         * @param waitTime  等待时间
         * @param leaseTime 锁的自动释放时间
         * @param unit      时间单位
         * @return 如果获取锁成功返回 true，否则返回 false
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static boolean tryWriteLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.ReadWriteLockOps.tryWriteLock(key, waitTime, leaseTime, unit);
            }
        }

        /**
         * 释放写锁
         *
         * @param key 读写锁的键名
         */
        public static void unlockWrite(String key) {
            try (RedisAccessor a = accessor()) {
                a.ReadWriteLockOps.unlockWrite(key);
            }
        }
    }

    // 信号量
    public static class SemaphoreOps {

        /**
         * 获取信号量许可（阻塞直到获取成功）
         *
         * @param key 信号量的键名
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static void acquire(String key) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                a.SemaphoreOps.acquire(key);
            }
        }

        /**
         * 获取指定数量的信号量许可（阻塞直到获取成功）
         *
         * @param key     信号量的键名
         * @param permits 要获取的许可数量
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static void acquire(String key, int permits) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                a.SemaphoreOps.acquire(key, permits);
            }
        }

        /**
         * 尝试获取信号量许可（非阻塞）
         *
         * @param key 信号量的键名
         * @return 如果获取许可成功返回 true，否则返回 false
         */
        public static boolean tryAcquire(String key) {
            try (RedisAccessor a = accessor()) {
                return a.SemaphoreOps.tryAcquire(key);
            }
        }

        /**
         * 尝试获取指定数量的信号量许可（非阻塞）
         *
         * @param key     信号量的键名
         * @param permits 要获取的许可数量
         * @return 如果获取许可成功返回 true，否则返回 false
         */
        public static boolean tryAcquire(String key, int permits) {
            try (RedisAccessor a = accessor()) {
                return a.SemaphoreOps.tryAcquire(key, permits);
            }
        }

        /**
         * 尝试获取信号量许可（支持超时）
         *
         * @param key     信号量的键名
         * @param timeout 等待超时时间
         * @param unit    时间单位
         * @return 如果获取许可成功返回 true，否则返回 false
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static boolean tryAcquire(String key, long timeout, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.SemaphoreOps.tryAcquire(key, timeout, unit);
            }
        }

        /**
         * 释放信号量许可
         *
         * @param key 信号量的键名
         */
        public static void release(String key) {
            try (RedisAccessor a = accessor()) {
                a.SemaphoreOps.release(key);
            }
        }

        /**
         * 释放指定数量的信号量许可
         *
         * @param key     信号量的键名
         * @param permits 要释放的许可数量
         */
        public static void release(String key, int permits) {
            try (RedisAccessor a = accessor()) {
                a.SemaphoreOps.release(key, permits);
            }
        }

        /**
         * 设置信号量的许可数量
         *
         * @param key     信号量的键名
         * @param permits 许可数量
         * @return 如果设置成功返回 true，否则返回 false
         */
        public static boolean trySetPermits(String key, int permits) {
            try (RedisAccessor a = accessor()) {
                return a.SemaphoreOps.trySetPermits(key, permits);
            }
        }
    }

    // 倒计时器
    public static class CountDownLatchOps {

        /**
         * 等待倒计时器归零（阻塞直到计数为0）
         *
         * @param key 倒计时器的键名
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static void await(String key) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                a.CountDownLatchOps.await(key);
            }
        }

        /**
         * 等待倒计时器归零（支持超时）
         *
         * @param key     倒计时器的键名
         * @param timeout 等待超时时间
         * @param unit    时间单位
         * @return 如果在超时前计数归零返回 true，否则返回 false
         * @throws InterruptedException 如果在等待过程中被中断抛出异常
         */
        public static boolean await(String key, long timeout, TimeUnit unit) throws InterruptedException {
            try (RedisAccessor a = accessor()) {
                return a.CountDownLatchOps.await(key, timeout, unit);
            }
        }

        /**
         * 倒计时器计数减一
         *
         * @param key 倒计时器的键名
         */
        public static void countDown(String key) {
            try (RedisAccessor a = accessor()) {
                a.CountDownLatchOps.countDown(key);
            }
        }

        /**
         * 获取倒计时器当前计数
         *
         * @param key 倒计时器的键名
         * @return 当前计数值
         */
        public static long getCount(String key) {
            try (RedisAccessor a = accessor()) {
                return a.CountDownLatchOps.getCount(key);
            }
        }

        /**
         * 设置倒计时器的初始计数
         *
         * @param key   倒计时器的键名
         * @param count 初始计数值
         * @return 如果设置成功返回 true，否则返回 false
         */
        public static boolean trySetCount(String key, long count) {
            try (RedisAccessor a = accessor()) {
                return a.CountDownLatchOps.trySetCount(key, count);
            }
        }
    }
}
