package cn.myslayers.libra.framework.toolkit.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.yaml.snakeyaml.Yaml;

/**
 * DatabaseUtil11 - 多数据源数据库工具类
 *
 * 主要功能：
 * 1. 多数据源管理 - 支持同时连接多个数据库
 * 2. 连接池管理 - 基于HikariCP的高性能连接池
 * 3. SQL注入防护 - 使用预编译语句确保安全
 * 4. 事务管理 - 支持编程式事务控制
 * 5. 自动资源管理 - 自动关闭连接和释放资源
 * 6. 配置模式 - 支持自定义配置和YAML配置文件
 * 7. 线程安全 - 所有操作均为线程安全
 *
 * 使用方式：
 * // 1. 自定义配置模式 / YAML配置模式
 * DatabaseUtil11.registerDatabase("testdb", new DatabaseConfig(url, username, password, driver));
 * DatabaseUtil11.registerYml("master");
 *
 * // 2. 执行查询
 * List<Map<String, Object>> results = DatabaseUtil11.selectListByMp("SELECT * FROM users WHERE age > ?", 18);
 *
 * // 3. 执行事务
 * DatabaseUtil11.executeInTransaction(conn -> {
 * // 事务内的数据库操作
 * return result;
 * });
 *
 * @author Halavah
 * @version 2.0.0
 */
public final class DatabaseUtil11 {

    //=====================================================================
    // 静态变量区域 - 全局配置和缓存管理
    //=====================================================================

    /**
     * 数据源连接池缓存
     * 作用：存储所有已注册的数据源连接池，支持多数据源管理
     * 键值：数据库名称 -> HikariDataSource对象
     */
    private static final Map<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    /**
     * 默认数据库名称
     * 作用：指定默认使用的数据库，简化API调用
     * 注意：在未指定数据库名称时，将使用此默认数据库
     */
    private static volatile String defaultDbName = null;

    //=====================================================================
    // YAML配置数据源信息类
    //=====================================================================

    /**
     * YAML配置数据源信息类
     * 作用：存储从配置文件读取的数据源信息
     * 用法：通过getter/setter方法访问属性
     */
    public static class YmlDataSourceInfo {

        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public YmlDataSourceInfo() {
        }

        public YmlDataSourceInfo(String url, String username, String password, String driverClassName) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }

    //=====================================================================
    // 数据库配置类 - 连接池配置管理
    //=====================================================================

    /**
     * 数据库配置类
     * 作用：封装数据库连接参数和连接池配置
     * 用法：通过构造函数创建配置对象，支持默认和自定义连接池参数
     */
    public static final class DatabaseConfig {

        private final String url;
        private final String username;
        private final String password;
        private final String driverClass;
        private final int maximumPoolSize;
        private final int minimumIdle;
        private final long connectionTimeout;
        private final long idleTimeout;
        private final long maxLifetime;

        /**
         * 创建数据库配置（使用默认连接池参数）
         * 作用：构造基本数据库配置，使用优化后的默认连接池参数
         * 用法：new DatabaseConfig(url, username, password, driverClass)
         *
         * @param url         数据库连接URL
         * @param username    用户名
         * @param password    密码
         * @param driverClass 驱动类名
         */
        public DatabaseConfig(String url, String username, String password, String driverClass) {
            this(url, username, password, driverClass, 10, 5, 30000, 600000, 1800000);
        }

        /**
         * 创建数据库配置（自定义连接池参数）
         * 作用：构造完整的数据库配置，包含详细的连接池参数
         * 用法：适用于需要精细控制连接池参数的场景
         *
         * @param url               数据库连接URL
         * @param username          用户名
         * @param password          密码
         * @param driverClass       驱动类名
         * @param maximumPoolSize   最大连接数
         * @param minimumIdle       最小空闲连接数
         * @param connectionTimeout 连接超时时间(毫秒)
         * @param idleTimeout       空闲超时时间(毫秒)
         * @param maxLifetime       连接最大生命周期(毫秒)
         */
        public DatabaseConfig(String url, String username, String password, String driverClass,
            int maximumPoolSize, int minimumIdle, long connectionTimeout,
            long idleTimeout, long maxLifetime) {
            this.url = validateNotBlank(url, "数据库URL不能为空");
            this.username = validateNotBlank(username, "用户名不能为空");
            this.password = password;
            this.driverClass = validateNotBlank(driverClass, "驱动类名不能为空");

            if (maximumPoolSize <= 0) {
                throw new IllegalArgumentException("最大连接数必须大于0");
            }
            if (minimumIdle < 0) {
                throw new IllegalArgumentException("最小空闲连接数不能小于0");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("连接超时时间必须大于0");
            }
            if (idleTimeout <= 0) {
                throw new IllegalArgumentException("空闲超时时间必须大于0");
            }
            if (maxLifetime <= 0) {
                throw new IllegalArgumentException("连接最大生命周期必须大于0");
            }

            this.maximumPoolSize = maximumPoolSize;
            this.minimumIdle = minimumIdle;
            this.connectionTimeout = connectionTimeout;
            this.idleTimeout = idleTimeout;
            this.maxLifetime = maxLifetime;
        }

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public int getMinimumIdle() {
            return minimumIdle;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public long getMaxLifetime() {
            return maxLifetime;
        }

        /**
         * 验证字符串参数不为空
         * 作用：参数校验工具方法
         * 用法：内部使用，确保关键参数不为空
         */
        private static String validateNotBlank(String value, String message) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(message);
            }
            return value.trim();
        }
    }

    //=====================================================================
    // 构造方法 - 禁止实例化
    //=====================================================================

    /**
     * 私有构造方法
     * 作用：防止工具类被实例化
     * 用法：仅支持静态方法调用
     */
    private DatabaseUtil11() {
        throw new UnsupportedOperationException("工具类不支持实例化");
    }

    //=====================================================================
    // 数据源注册方法 - 支持自定义配置和YAML配置
    //=====================================================================

    /**
     * 注册数据库配置
     * 作用：注册数据库连接配置并自动设置为默认数据库，自动启用自定义配置模式
     * 用法：DatabaseUtil11.registerDatabase("testdb", new DatabaseConfig(url, username, password, driver))
     *
     * @param dbName 数据库名称（标识符）
     * @param config 数据库连接配置
     */
    public static void registerDatabase(String dbName, DatabaseConfig config) {
        validateNotBlank(dbName, "数据库名称不能为空");
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }

        try {
            // 自动启用自定义配置模式
            useCustomConfig();

            // 创建HikariCP连接池配置
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClass());

            // 设置连接池参数
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(config.getMinimumIdle());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());

            // 设置连接池名称便于监控
            hikariConfig.setPoolName("DatabaseUtil11-" + dbName + "-Pool");

            // 创建数据源并缓存
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            DATA_SOURCES.put(dbName, dataSource);

            // 自动设置为默认数据库
            setDefaultDatabase(dbName);

            System.out.println("数据库 [" + dbName + "] 注册成功");

        } catch (Exception e) {
            throw new RuntimeException("注册数据库失败: " + dbName, e);
        }
    }

    /**
     * 内部数据库注册方法
     * 作用：注册数据库连接配置但不改变配置模式
     * 用法：内部调用，避免配置模式冲突
     *
     * @param dbName 数据库名称（标识符）
     * @param config 数据库连接配置
     */
    private static void registerDatabaseInternal(String dbName, DatabaseConfig config) {
        validateNotBlank(dbName, "数据库名称不能为空");
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }

        try {
            // 创建HikariCP连接池配置
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClass());

            // 设置连接池参数
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(config.getMinimumIdle());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());

            // 设置连接池名称便于监控
            hikariConfig.setPoolName("DatabaseUtil11-" + dbName + "-Pool");

            // 创建数据源并缓存
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            DATA_SOURCES.put(dbName, dataSource);

            System.out.println("数据库 [" + dbName + "] 注册成功");

        } catch (Exception e) {
            throw new RuntimeException("注册数据库失败: " + dbName, e);
        }
    }

    /**
     * 从YAML配置文件注册数据库
     * 作用：从application-dev.yml配置文件中读取数据源配置并注册，自动启用YAML配置模式
     * 用法：DatabaseUtil11.registerYml("master")
     *
     * @param dataSourceName 配置文件中的数据源名称
     */
    public static void registerDatabaseFromYml(String dataSourceName) {
        validateNotBlank(dataSourceName, "数据源名称不能为空");

        try {
            // 自动启用YAML配置模式
            useYamlConfig();

            // 从YAML配置读取数据源信息
            YmlDataSourceInfo dataSourceInfo = readDataSourceFromYml(dataSourceName);

            // 创建数据库配置
            DatabaseConfig config = new DatabaseConfig(
                dataSourceInfo.getUrl(),
                dataSourceInfo.getUsername(),
                dataSourceInfo.getPassword(),
                dataSourceInfo.getDriverClassName()
            );

            // 注册数据库（但不再调用useCustomConfig，因为我们已经设置了YAML模式）
            registerDatabaseInternal(dataSourceName, config);

            // 设置为默认数据库
            setDefaultDatabase(dataSourceName);

            System.out.println("从YAML配置注册数据库 [" + dataSourceName + "] 成功");

        } catch (Exception e) {
            throw new RuntimeException("从YAML配置注册数据库失败: " + dataSourceName, e);
        }
    }

    /**
     * 从YAML配置文件读取数据源信息
     * 作用：解析application-dev.yml中的数据源配置
     * 用法：内部方法，由registerDatabaseFromYml调用
     *
     * @param dataSourceName 数据源名称
     * @return 数据源配置信息
     */
    private static YmlDataSourceInfo readDataSourceFromYml(String dataSourceName) {
        try (InputStream inputStream = DatabaseUtil11.class.getClassLoader()
            .getResourceAsStream("application-dev.yml")) {

            if (inputStream == null) {
                throw new RuntimeException("找不到配置文件: application-dev.yml");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);

            // 导航到数据源配置
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) yamlData.get("spring");
            if (spring == null) {
                throw new RuntimeException("配置文件中未找到spring配置");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
            if (datasource == null) {
                throw new RuntimeException("配置文件中未找到datasource配置");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> dynamic = (Map<String, Object>) datasource.get("dynamic");
            if (dynamic == null) {
                throw new RuntimeException("配置文件中未找到dynamic配置");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> datasources = (Map<String, Object>) dynamic.get("datasource");
            if (datasources == null) {
                throw new RuntimeException("配置文件中未找到datasource列表");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> targetDataSource = (Map<String, Object>) datasources.get(dataSourceName);
            if (targetDataSource == null) {
                throw new RuntimeException("配置文件中未找到数据源: " + dataSourceName);
            }

            // 提取数据源配置信息
            YmlDataSourceInfo dataSourceInfo = new YmlDataSourceInfo();
            dataSourceInfo.setUrl(getStringValue(targetDataSource, "url"));
            dataSourceInfo.setUsername(getStringValue(targetDataSource, "username"));
            dataSourceInfo.setPassword(getStringValue(targetDataSource, "password"));
            dataSourceInfo.setDriverClassName(getStringValue(targetDataSource, "driver-class-name"));

            return dataSourceInfo;

        } catch (Exception e) {
            throw new RuntimeException("读取YAML配置失败", e);
        }
    }

    /**
     * 安全获取Map中的字符串值
     * 作用：从Map中获取值并转换为字符串，处理类型转换问题
     * 用法：内部工具方法，确保类型安全的字符串获取
     *
     * @param map 数据映射
     * @param key 键名
     * @return 字符串值
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    //=====================================================================
    // 配置模式管理方法
    //=====================================================================

    /**
     * 启用自定义配置模式
     * 作用：切换到自定义配置模式，使用代码中定义的数据库配置
     * 用法：DatabaseUtil11.useCustomConfig()
     */
    public static void useCustomConfig() {
        System.out.println("已切换到自定义配置模式");
    }

    /**
     * 启用YAML配置模式
     * 作用：切换到YAML配置模式，从配置文件读取数据库配置
     * 用法：DatabaseUtil11.useYamlConfig()
     */
    public static void useYamlConfig() {
        System.out.println("已切换到YAML配置模式");
    }

    //=====================================================================
    // 默认数据库管理方法
    //=====================================================================

    /**
     * 设置默认数据库
     * 作用：指定默认使用的数据库，简化后续操作的数据库指定
     * 用法：DatabaseUtil11.setDefaultDatabase("mydb")
     *
     * @param dbName 数据库名称
     */
    public static void setDefaultDatabase(String dbName) {
        validateNotBlank(dbName, "数据库名称不能为空");

        if (!DATA_SOURCES.containsKey(dbName)) {
            throw new IllegalArgumentException("数据库未注册: " + dbName);
        }

        defaultDbName = dbName;
        System.out.println("默认数据库设置为: " + dbName);
    }

    /**
     * 获取默认数据库名称
     * 作用：返回当前设置的默认数据库名称
     * 用法：String dbName = DatabaseUtil11.getDefaultDatabase()
     *
     * @return 默认数据库名称，如果未设置则返回null
     */
    public static String getDefaultDatabase() {
        return defaultDbName;
    }

    //=====================================================================
    // 数据源关闭和清理方法
    //=====================================================================

    /**
     * 关闭指定数据库连接池
     * 作用：关闭并释放指定数据库的连接池资源
     * 用法：DatabaseUtil11.closeDatabase("mydb")
     *
     * @param dbName 数据库名称
     */
    public static void closeDatabase(String dbName) {
        DataSource dataSource = DATA_SOURCES.remove(dbName);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            System.out.println("数据库 [" + dbName + "] 已关闭");
        }

        // 如果关闭的是默认数据库，清除默认设置
        if (dbName.equals(defaultDbName)) {
            defaultDbName = null;
        }
    }

    /**
     * 关闭所有数据库连接池
     * 作用：关闭并释放所有已注册数据库的连接池资源
     * 用法：DatabaseUtil11.closeAllDatabases()
     */
    public static void closeAllDatabases() {
        List<String> dbNames = new ArrayList<>(DATA_SOURCES.keySet());
        for (String dbName : dbNames) {
            closeDatabase(dbName);
        }
        System.out.println("所有数据库连接已关闭");
    }

    //=====================================================================
    // 数据库连接获取方法
    //=====================================================================

    /**
     * 获取指定数据库连接
     * 作用：从连接池中获取指定数据库的连接对象
     * 用法：try (Connection conn = DatabaseUtil11.getConnection("mydb")) { ... }
     *
     * @param dbName 数据库名称
     * @return 数据库连接对象
     * @throws SQLException 获取连接失败时抛出
     */
    public static Connection getConnection(String dbName) throws SQLException {
        validateNotBlank(dbName, "数据库名称不能为空");

        DataSource dataSource = DATA_SOURCES.get(dbName);
        if (dataSource == null) {
            throw new SQLException("数据库未注册: " + dbName);
        }

        return dataSource.getConnection();
    }

    /**
     * 获取默认数据库连接
     * 作用：从连接池中获取默认数据库的连接对象
     * 用法：try (Connection conn = DatabaseUtil11.getConnection()) { ... }
     *
     * @return 数据库连接对象
     * @throws SQLException 获取连接失败时抛出
     */
    public static Connection getConnection() throws SQLException {
        if (defaultDbName == null) {
            throw new SQLException("未设置默认数据库，请先调用setDefaultDatabase()方法");
        }
        return getConnection(defaultDbName);
    }

    //=====================================================================
    // SQL参数设置和结果集处理工具方法
    //=====================================================================

    /**
     * 为预编译语句设置参数
     * 作用：根据参数类型自动设置PreparedStatement的参数值
     * 用法：内部工具方法，自动处理各种数据类型
     *
     * @param pstmt  预编译语句对象
     * @param params 参数数组
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setObject(i + 1, null);
                } else if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Long) {
                    pstmt.setLong(i + 1, (Long) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) param);
                } else if (param instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) param);
                } else if (param instanceof java.util.Date) {
                    pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) param).getTime()));
                } else if (param instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) param);
                } else if (param instanceof Timestamp) {
                    pstmt.setTimestamp(i + 1, (Timestamp) param);
                } else {
                    pstmt.setObject(i + 1, param);
                }
            }
        }
    }

    /**
     * 将ResultSet转换为Map对象
     * 作用：将查询结果集的一行数据转换为键值对映射
     * 用法：内部工具方法，用于统一结果集处理
     *
     * @param rs 结果集对象
     * @return 数据映射对象
     */
    private static Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }

        return row;
    }

    /**
     * 验证字符串参数不为空
     * 作用：通用的字符串参数校验方法
     * 用法：内部工具方法，确保关键参数有效性
     *
     * @param value   待验证的字符串
     * @param message 错误消息
     * @return 去除前后空格的字符串
     */
    private static String validateNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * 从JDBC URL中提取数据库名称
     * 作用：解析JDBC连接字符串，提取数据库名称
     * 用法：内部工具方法，用于自动化数据库名称识别
     *
     * @param jdbcUrl JDBC连接字符串
     * @return 数据库名称
     */
    private static String extractDatabaseNameFromUrl(String jdbcUrl) {
        try {
            String url = jdbcUrl.toLowerCase();

            // 查找主机端口后的第一个斜杠位置
            int dbStart = url.indexOf('/', url.indexOf("://") + 3);
            if (dbStart == -1) {
                throw new IllegalArgumentException("URL中未找到数据库名称部分");
            }

            // 查找参数分隔符的位置
            int dbEnd = url.indexOf('?', dbStart);
            if (dbEnd == -1) {
                dbEnd = url.length();
            }

            // 提取数据库名称
            String dbName = jdbcUrl.substring(dbStart + 1, dbEnd);
            if (dbName.isEmpty()) {
                throw new IllegalArgumentException("数据库名称不能为空");
            }

            return dbName;

        } catch (Exception e) {
            throw new IllegalArgumentException("解析数据库名称失败: " + e.getMessage(), e);
        }
    }

    //=====================================================================
    // 事务回调接口
    //=====================================================================

    /**
     * 事务回调接口
     * 作用：定义在事务中执行的操作
     * 用法：通过lambda表达式或匿名类实现事务逻辑
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {

        /**
         * 在事务中执行的操作
         * 作用：定义事务内的业务逻辑，支持返回值
         * 用法：实现此方法来定义事务内的数据库操作
         *
         * @param conn 数据库连接对象（已设置为手动提交模式）
         * @return 操作结果
         * @throws SQLException SQL操作异常会触发事务回滚
         */
        T doInTransaction(Connection conn) throws SQLException;
    }

    //=====================================================================
    // 事务管理器 - 提供事务控制功能
    //=====================================================================

    /**
     * 事务管理器
     * 作用：提供事务控制功能，支持自动提交和回滚
     * 用法：通过静态方法执行事务操作
     */
    public static class TransactionManager {

        /**
         * 在事务中执行操作（使用默认数据库）
         * 作用：在事务上下文中执行数据库操作，自动处理提交和回滚
         * 用法：TransactionManager.executeInTransaction(conn -> { ... })
         *
         * @param callback 事务回调函数
         * @return 操作结果
         */
        public static <T> T executeInTransaction(TransactionCallback<T> callback) {
            return executeInTransaction(defaultDbName, callback);
        }

        /**
         * 在事务中执行操作（指定数据库）
         * 作用：在指定数据库的事务上下文中执行操作
         * 用法：TransactionManager.executeInTransaction("mydb", conn -> { ... })
         *
         * @param dbName   数据库名称
         * @param callback 事务回调函数
         * @return 操作结果
         */
        public static <T> T executeInTransaction(String dbName, TransactionCallback<T> callback) {
            validateNotBlank(dbName, "数据库名称不能为空");

            if (callback == null) {
                throw new IllegalArgumentException("事务回调不能为空");
            }

            try (Connection conn = getConnection(dbName)) {
                // 开启事务
                conn.setAutoCommit(false);

                try {
                    // 执行业务逻辑
                    T result = callback.doInTransaction(conn);

                    // 提交事务
                    conn.commit();
                    return result;

                } catch (Exception e) {
                    // 回滚事务
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("事务回滚失败: " + rollbackEx.getMessage());
                    }
                    throw new RuntimeException("事务执行失败", e);
                }

            } catch (SQLException e) {
                throw new RuntimeException("获取数据库连接失败", e);
            }
        }
    }

    //=====================================================================
    // SQL执行器 - 提供各种SQL操作方法
    //=====================================================================

    /**
     * SQL执行器
     * 作用：提供各种数据库操作方法，支持指定数据库执行
     * 用法：通过静态方法执行各种SQL操作
     */
    public static class SqlExecutor {

        /**
         * 执行查询操作返回多行结果（指定数据库）
         * 作用：执行SELECT语句并返回多行结果
         * 用法：SqlExecutor.selectListByMp("mydb", "SELECT * FROM users WHERE age > ?", 18)
         *
         * @param dbName 数据库名称
         * @param sql    SQL查询语句
         * @param params SQL参数
         * @return 查询结果列表
         */
        public static List<Map<String, Object>> selectList(String dbName, String sql, Object... params) {
            validateNotBlank(dbName, "数据库名称不能为空");
            validateNotBlank(sql, "SQL语句不能为空");

            List<Map<String, Object>> results = new ArrayList<>();

            try (Connection conn = getConnection(dbName);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                setParameters(pstmt, params);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(resultSetToMap(rs));
                    }
                }

                return results;

            } catch (SQLException e) {
                throw new RuntimeException("查询执行失败: " + sql, e);
            }
        }

        /**
         * 执行查询操作返回单行结果（指定数据库）
         * 作用：执行SELECT语句并返回单行结果，如果无结果返回null
         * 用法：SqlExecutor.selectOneByMp("mydb", "SELECT * FROM users WHERE id = ?", 1)
         *
         * @param dbName 数据库名称
         * @param sql    SQL查询语句
         * @param params SQL参数
         * @return 查询结果，无结果时返回null
         */
        public static Map<String, Object> selectOne(String dbName, String sql, Object... params) {
            List<Map<String, Object>> results = selectList(dbName, sql, params);
            return results.isEmpty() ? null : results.get(0);
        }

        /**
         * 执行插入操作（指定数据库）
         * 作用：执行INSERT语句并返回影响的行数
         * 用法：SqlExecutor.insertByMp("mydb", "INSERT INTO users(name, age) VALUES(?, ?)", "张三", 25)
         *
         * @param dbName 数据库名称
         * @param sql    SQL插入语句
         * @param params SQL参数
         * @return 受影响的行数
         */
        public static int insert(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行更新操作（指定数据库）
         * 作用：执行UPDATE语句并返回影响的行数
         * 用法：SqlExecutor.updateByMp("mydb", "UPDATE users SET age = ? WHERE name = ?", 26, "张三")
         *
         * @param dbName 数据库名称
         * @param sql    SQL更新语句
         * @param params SQL参数
         * @return 受影响的行数
         */
        public static int update(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行删除操作（指定数据库）
         * 作用：执行DELETE语句并返回影响的行数
         * 用法：SqlExecutor.deleteByMp("mydb", "DELETE FROM users WHERE id = ?", 1)
         *
         * @param dbName 数据库名称
         * @param sql    SQL删除语句
         * @param params SQL参数
         * @return 受影响的行数
         */
        public static int delete(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行更新操作的通用方法
         * 作用：内部方法，统一处理INSERT、UPDATE、DELETE操作
         * 用法：内部调用，不直接对外提供
         */
        private static int executeUpdate(String dbName, String sql, Object... params) {
            validateNotBlank(dbName, "数据库名称不能为空");
            validateNotBlank(sql, "SQL语句不能为空");

            try (Connection conn = getConnection(dbName);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                setParameters(pstmt, params);
                return pstmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("SQL执行失败: " + sql, e);
            }
        }

        /**
         * 执行批量操作（指定数据库）
         * 作用：批量执行相同的SQL语句，提高执行效率
         * 用法：SqlExecutor.executeBatch("mydb", sql, paramsList)
         *
         * @param dbName     数据库名称
         * @param sql        SQL语句
         * @param paramsList 参数列表
         * @return 各操作受影响的行数数组
         */
        public static int[] executeBatch(String dbName, String sql, List<Object[]> paramsList) {
            validateNotBlank(dbName, "数据库名称不能为空");
            validateNotBlank(sql, "SQL语句不能为空");

            if (paramsList == null || paramsList.isEmpty()) {
                return new int[0];
            }

            try (Connection conn = getConnection(dbName);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (Object[] params : paramsList) {
                    setParameters(pstmt, params);
                    pstmt.addBatch();
                }

                return pstmt.executeBatch();

            } catch (SQLException e) {
                throw new RuntimeException("批量操作执行失败: " + sql, e);
            }
        }
    }

    //=====================================================================
    // 简化操作方法 - 使用默认数据库的便捷方法
    //=====================================================================

    /**
     * 执行查询操作返回多行结果（使用默认数据库）
     * 作用：使用默认数据库执行SELECT语句
     * 用法：DatabaseUtil11.selectListByMp("SELECT * FROM users WHERE age > ?", 18)
     */
    public static List<Map<String, Object>> selectList(String sql, Object... params) {
        return SqlExecutor.selectList(defaultDbName, sql, params);
    }

    /**
     * 执行查询操作返回单行结果（使用默认数据库）
     * 作用：使用默认数据库执行SELECT语句并返回单行结果
     * 用法：DatabaseUtil11.selectOneByMp("SELECT * FROM users WHERE id = ?", 1)
     */
    public static Map<String, Object> selectOne(String sql, Object... params) {
        return SqlExecutor.selectOne(defaultDbName, sql, params);
    }

    /**
     * 执行插入操作（使用默认数据库）
     * 作用：使用默认数据库执行INSERT语句
     * 用法：DatabaseUtil11.insertByMp("INSERT INTO users(name, age) VALUES(?, ?)", "张三", 25)
     */
    public static int insert(String sql, Object... params) {
        return SqlExecutor.insert(defaultDbName, sql, params);
    }

    /**
     * 执行更新操作（使用默认数据库）
     * 作用：使用默认数据库执行UPDATE语句
     * 用法：DatabaseUtil11.updateByMp("UPDATE users SET age = ? WHERE name = ?", 26, "张三")
     */
    public static int update(String sql, Object... params) {
        return SqlExecutor.update(defaultDbName, sql, params);
    }

    /**
     * 执行删除操作（使用默认数据库）
     * 作用：使用默认数据库执行DELETE语句
     * 用法：DatabaseUtil11.deleteByMp("DELETE FROM users WHERE id = ?", 1)
     */
    public static int delete(String sql, Object... params) {
        return SqlExecutor.delete(defaultDbName, sql, params);
    }

    /**
     * 执行批量操作（使用默认数据库）
     * 作用：使用默认数据库批量执行相同SQL语句
     * 用法：DatabaseUtil11.executeBatch(sql, paramsList)
     */
    public static int[] executeBatch(String sql, List<Object[]> paramsList) {
        return SqlExecutor.executeBatch(defaultDbName, sql, paramsList);
    }

    /**
     * 在事务中执行操作（使用默认数据库）
     * 作用：使用默认数据库在事务上下文中执行操作
     * 用法：DatabaseUtil11.executeInTransaction(conn -> { ... })
     */
    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        return TransactionManager.executeInTransaction(callback);
    }
}
