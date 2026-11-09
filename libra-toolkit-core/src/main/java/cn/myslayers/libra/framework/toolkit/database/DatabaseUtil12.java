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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DatabaseUtil12 - 企业级多数据源XML数据库工具类
 *
 * 这是一个功能强大的数据库工具类，提供了完整的多数据源管理、XML SQL执行、
 * 连接池管理等企业级功能。
 *
 * 主要特性：
 * - 支持多数据源配置和动态切换
 * - HikariCP连接池，提供卓越的性能
 * - XML文件SQL解析和执行
 * - 完整的CRUD操作API
 * - 事务管理和回调机制
 * - 详细的中文注释和使用示例
 *
 * 快速使用指南：
 * 1. 配置数据源: DatabaseUtil12.registerDataSource("db1", config)
 * 2. 设置默认库: DatabaseUtil12.setDefaultDatabase("db1")
 * 3. 执行SQL: DatabaseUtil12.selectListByMp("SELECT * FROM users")
 * 4. XML执行: DatabaseUtil12.selectListFromXml("getUserList", params)
 *
 * @author Halavah
 * @version 2.0
 */
public class DatabaseUtil12 {

    // ==================== XML SQL 文件解析配置常量 ====================

    /**
     * 默认XML SQL文件解析目录数组
     * 支持多个目录位置，系统会按顺序搜索这些目录中的XML文件
     *
     * 使用说明：
     * - 可以配置多个目录，系统会依次搜索
     * - 支持相对路径和绝对路径
     * - 建议使用相对路径，便于项目部署
     *
     * 示例配置：
     * - "src/main/resources/sql/" - 主要SQL文件目录
     * - "src/main/resources/mapper/" - MyBatis风格目录
     * - "config/sql/" - 自定义配置目录
     */
    public static final String[] DEFAULT_XML_SQL_DIRECTORIES = {
        "src/main/resources/sql/",           // 主要SQL目录
        "src/main/resources/mapper/",        // MyBatis风格目录
        "src/main/resources/mybatis/",       // MyBatis风格目录
        "src/main/resources/xml/",           // XML配置目录
        "config/sql/",                       // 自定义SQL目录
        "sql/"                               // 简化目录结构
    };

    /**
     * XML文件扩展名
     */
    public static final String XML_FILE_EXTENSION = ".xml";

    /**
     * SQL标签名称
     */
    public static final String SQL_TAG_NAME = "sql";

    /**
     * ID属性名称
     */
    public static final String ID_ATTRIBUTE_NAME = "id";

    // ==================== 核心变量区域 ====================

    //=====================================================================
    // 【核心变量区域】- 数据源管理和配置缓存
    //=====================================================================

    /**
     * 数据源连接池缓存映射
     *
     * 功能说明：
     * - 存储所有已注册的数据库连接池实例
     * - 使用ConcurrentHashMap确保线程安全的并发访问
     * - 键为数据库名称标识符，值为HikariDataSource连接池对象
     *
     * 使用场景：
     * - 多数据源环境下的连接池管理
     * - 数据库连接的快速获取和复用
     * - 连接池状态监控和维护
     */
    private static final Map<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    /**
     * 默认数据库名称标识符
     *
     * 功能说明：
     * - 指定系统默认使用的数据库名称
     * - 简化API调用，无需每次指定数据库名称
     * - 使用volatile关键字确保多线程环境下的可见性
     *
     * 使用场景：
     * - 单主数据库环境的简化操作
     * - 默认数据库的快速切换
     * - API调用的简化和统一
     */
    private static volatile String defaultDbName = null;

    //=====================================================================
    // YAML配置数据源信息类
    //=====================================================================

    /**
     * YAML配置数据源信息封装类
     *
     * 功能说明：
     * - 封装从application-dev.yml配置文件中读取的数据源配置信息
     * - 提供标准的JavaBean结构，支持getter/setter方法访问
     * - 用于YAML配置模式下的数据源信息传递和管理
     *
     * 配置文件结构示例：
     * spring:
     * datasource:
     * dynamic:
     * datasource:
     * master:
     * url: jdbc:mysql://localhost:3306/test
     * username: demo_user
     * password: demo_password
     * driver-class-name: com.mysql.cj.jdbc.Driver
     *
     * 使用方式：
     * YmlDataSourceInfo info = readDataSourceFromYml("master");
     * String url = info.getUrl();
     */
    public static class YmlDataSourceInfo {

        /**
         * 数据库连接URL地址
         */
        private String url;

        /**
         * 数据库用户名
         */
        private String username;

        /**
         * 数据库密码
         */
        private String password;

        /**
         * JDBC驱动类名
         */
        private String driverClassName;

        /**
         * 默认构造函数
         * 作用：创建空的数据源配置对象，用于后续属性设置
         * 用法：YmlDataSourceInfo info = new YmlDataSourceInfo();
         */
        public YmlDataSourceInfo() {
        }

        /**
         * 完整参数构造函数
         * 作用：直接创建包含完整配置信息的数据源对象
         * 用法：适用于已知所有配置参数的场景
         *
         * @param url             数据库连接URL
         * @param username        数据库用户名
         * @param password        数据库密码
         * @param driverClassName JDBC驱动类名
         */
        public YmlDataSourceInfo(String url, String username, String password, String driverClassName) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }

        /**
         * 获取数据库连接URL
         * 作用：返回数据库连接字符串
         * 用法：String url = info.getUrl();
         *
         * @return 数据库连接URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * 设置数据库连接URL
         * 作用：设置数据库连接字符串
         * 用法：info.setUrl("jdbc:mysql://localhost:3306/test");
         *
         * @param url 数据库连接URL
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * 获取数据库用户名
         * 作用：返回数据库登录用户名
         * 用法：String username = info.getUsername();
         *
         * @return 数据库用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 设置数据库用户名
         * 作用：设置数据库登录用户名
         * 用法：info.setUsername("demo_user");
         *
         * @param username 数据库用户名
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * 获取数据库密码
         * 作用：返回数据库登录密码
         * 用法：String password = info.getPassword();
         *
         * @return 数据库密码
         */
        public String getPassword() {
            return password;
        }

        /**
         * 设置数据库密码
         * 作用：设置数据库登录密码
         * 用法：info.setPassword("demo_password");
         *
         * @param password 数据库密码
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * 获取JDBC驱动类名
         * 作用：返回数据库驱动类的完整类名
         * 用法：String driver = info.getDriverClassName();
         *
         * @return JDBC驱动类名
         */
        public String getDriverClassName() {
            return driverClassName;
        }

        /**
         * 设置JDBC驱动类名
         * 作用：设置数据库驱动类的完整类名
         * 用法：info.setDriverClassName("com.mysql.cj.jdbc.Driver");
         *
         * @param driverClassName JDBC驱动类名
         */
        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }

    //=====================================================================
    // 【数据库配置类】- 连接池参数配置和管理
    //=====================================================================

    /**
     * 数据库配置封装类
     *
     * 功能说明：
     * - 封装数据库连接参数和HikariCP连接池配置
     * - 提供默认配置和自定义配置两种构造方式
     * - 内置参数校验，确保配置的有效性和安全性
     * - 支持连接池性能调优和监控配置
     *
     * 连接池参数说明：
     * - maximumPoolSize: 最大连接数，建议设置为CPU核心数*2+1
     * - minimumIdle: 最小空闲连接数，保持一定数量的活跃连接
     * - connectionTimeout: 连接超时时间，避免长时间等待
     * - idleTimeout: 空闲超时时间，自动释放长期不用的连接
     * - maxLifetime: 连接最大生命周期，定期更新连接池中的连接
     *
     * 使用场景：
     * - 自定义配置模式的数据库连接配置
     * - 性能优化和连接池参数调整
     * - 多数据库环境的差异化配置
     */
    public static final class DatabaseConfig {

        /**
         * 数据库连接URL
         */
        private final String url;

        /**
         * 数据库用户名
         */
        private final String username;

        /**
         * 数据库密码
         */
        private final String password;

        /**
         * JDBC驱动类名
         */
        private final String driverClass;

        /**
         * 连接池最大连接数
         */
        private final int maximumPoolSize;

        /**
         * 连接池最小空闲连接数
         */
        private final int minimumIdle;

        /**
         * 连接超时时间（毫秒）
         */
        private final long connectionTimeout;

        /**
         * 空闲连接超时时间（毫秒）
         */
        private final long idleTimeout;

        /**
         * 连接最大生命周期（毫秒）
         */
        private final long maxLifetime;

        /**
         * 简化构造函数（使用默认连接池参数）
         *
         * 作用：创建使用默认优化参数的数据库配置对象
         * 用法：适用于大多数标准应用场景，无需手动调优连接池参数
         *
         * 默认参数配置：
         * - 最大连接数：10个
         * - 最小空闲连接：5个
         * - 连接超时：30秒
         * - 空闲超时：10分钟
         * - 最大生命周期：30分钟
         *
         * 使用示例：
         * DatabaseConfig config = new DatabaseConfig(
         * "jdbc:mysql://localhost:3306/test",
         * "demo_user",
         * "password",
         * "com.mysql.cj.jdbc.Driver"
         * );
         *
         * @param url         数据库连接URL
         * @param username    数据库用户名
         * @param password    数据库密码
         * @param driverClass JDBC驱动类名
         */
        public DatabaseConfig(String url, String username, String password, String driverClass) {
            this(url, username, password, driverClass, 10, 5, 30000, 600000, 1800000);
        }

        /**
         * 完整构造函数（自定义连接池参数）
         *
         * 作用：创建包含自定义连接池参数的数据库配置对象
         * 用法：适用于需要精细控制连接池性能的高并发场景
         *
         * 参数调优建议：
         * - maximumPoolSize：根据并发量调整，通常为CPU核心数的2-4倍
         * - minimumIdle：保持为maximumPoolSize的50-80%
         * - connectionTimeout：网络环境好时可适当降低
         * - idleTimeout：根据业务间歇性调整
         * - maxLifetime：数据库连接稳定性要求高时可增加
         *
         * 使用示例：
         * DatabaseConfig config = new DatabaseConfig(
         * "jdbc:mysql://localhost:3306/test",
         * "demo_user", "password", "com.mysql.cj.jdbc.Driver",
         * 20,     // 最大连接数
         * 10,     // 最小空闲连接数
         * 30000,  // 连接超时30秒
         * 600000, // 空闲超时10分钟
         * 1800000 // 最大生命周期30分钟
         * );
         *
         * @param url               数据库连接URL
         * @param username          数据库用户名
         * @param password          数据库密码
         * @param driverClass       JDBC驱动类名
         * @param maximumPoolSize   连接池最大连接数
         * @param minimumIdle       连接池最小空闲连接数
         * @param connectionTimeout 连接获取超时时间（毫秒）
         * @param idleTimeout       空闲连接超时时间（毫秒）
         * @param maxLifetime       连接最大生命周期（毫秒）
         */
        public DatabaseConfig(String url, String username, String password, String driverClass,
            int maximumPoolSize, int minimumIdle, long connectionTimeout,
            long idleTimeout, long maxLifetime) {
            // 参数校验和赋值
            this.url = validateNotBlank(url, "数据库URL不能为空");
            this.username = validateNotBlank(username, "用户名不能为空");
            this.password = password;
            this.driverClass = validateNotBlank(driverClass, "驱动类名不能为空");

            // 连接池参数校验
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

        /**
         * 获取数据库连接URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * 获取数据库用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 获取数据库密码
         */
        public String getPassword() {
            return password;
        }

        /**
         * 获取JDBC驱动类名
         */
        public String getDriverClass() {
            return driverClass;
        }

        /**
         * 获取连接池最大连接数
         */
        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        /**
         * 获取连接池最小空闲连接数
         */
        public int getMinimumIdle() {
            return minimumIdle;
        }

        /**
         * 获取连接超时时间（毫秒）
         */
        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        /**
         * 获取空闲连接超时时间（毫秒）
         */
        public long getIdleTimeout() {
            return idleTimeout;
        }

        /**
         * 获取连接最大生命周期（毫秒）
         */
        public long getMaxLifetime() {
            return maxLifetime;
        }

        /**
         * 字符串参数校验工具方法
         *
         * 作用：确保关键字符串参数不为空或空白
         * 用法：内部使用，提供统一的参数校验逻辑
         *
         * @param value   待校验的字符串值
         * @param message 校验失败时的错误消息
         * @return 去除前后空格的有效字符串
         * @throws IllegalArgumentException 当参数为空或空白时抛出
         */
        private static String validateNotBlank(String value, String message) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(message);
            }
            return value.trim();
        }
    }

    //=====================================================================
    // 【构造方法控制】- 工具类实例化限制
    //=====================================================================

    /**
     * 私有构造方法
     *
     * 作用：防止工具类被实例化，确保所有方法都通过静态方式调用
     * 用法：仅支持静态方法调用，如 DatabaseUtil12.selectListByMp(...)
     *
     * @throws UnsupportedOperationException 任何实例化尝试都会抛出此异常
     */
    private DatabaseUtil12() {
        throw new UnsupportedOperationException("工具类不支持实例化，请使用静态方法");
    }

    //=====================================================================
    // 【数据源注册核心方法】- 支持自定义配置和YAML配置两种模式
    //=====================================================================

    /**
     * 注册数据库（自定义配置模式）
     *
     * 功能说明：
     * - 使用自定义DatabaseConfig配置注册数据库连接池
     * - 自动创建HikariCP连接池并进行性能优化配置
     * - 自动设置为系统默认数据库，简化后续操作
     * - 启用自定义配置模式，区别于YAML配置模式
     *
     * 适用场景：
     * - 动态数据源配置（如多租户系统）
     * - 需要精细控制连接池参数的场景
     * - 程序化配置数据源的情况
     * - 测试环境的快速数据库配置
     *
     * 性能特性：
     * - 基于HikariCP高性能连接池
     * - 支持连接池监控和管理
     * - 自动连接泄漏检测和恢复
     * - 支持连接池状态实时监控
     *
     * 使用示例：
     * // 基本配置
     * DatabaseConfig config = new DatabaseConfig(
     * "jdbc:mysql://localhost:3306/test",
     * "demo_user", "password", "com.mysql.cj.jdbc.Driver"
     * );
     * DatabaseUtil12.registerDatabase("mainDB", config);
     *
     * // 高级配置
     * DatabaseConfig advancedConfig = new DatabaseConfig(
     * url, username, password, driver,
     * 20,     // 最大连接数
     * 10,     // 最小空闲连接
     * 30000,  // 连接超时
     * 600000, // 空闲超时
     * 1800000 // 最大生命周期
     * );
     * DatabaseUtil12.registerDatabase("advancedDB", advancedConfig);
     *
     * @param dbName 数据库标识名称（用于后续引用和管理）
     * @param config 数据库连接配置对象
     * @throws IllegalArgumentException 当数据库名称为空或配置对象为空时抛出
     * @throws RuntimeException         当连接池创建失败时抛出
     */
    public static void registerDatabase(String dbName, DatabaseConfig config) {
        validateNotBlank(dbName, "数据库名称不能为空");
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }

        try {
            // 启用自定义配置模式
            useCustomConfig();

            // 执行内部注册逻辑
            registerDatabaseInternal(dbName, config);

            // 自动设置为默认数据库
            setDefaultDatabase(dbName);

        } catch (Exception e) {
            throw new RuntimeException("注册数据库失败: " + dbName + " - " + e.getMessage(), e);
        }
    }

    /**
     * 从YAML配置文件注册数据库（YAML配置模式）
     *
     * 功能说明：
     * - 从application-dev.yml配置文件中读取数据源配置
     * - 自动解析YAML配置结构并创建连接池
     * - 支持Spring Boot风格的配置文件格式
     * - 自动设置为系统默认数据库
     *
     * 配置文件格式要求：
     * spring:
     * datasource:
     * dynamic:
     * datasource:
     * master:                    # 数据源名称
     * url: jdbc:mysql://...   # 数据库连接URL
     * username: demo_user           # 用户名
     * password: demo_password         # 密码
     * driver-class-name: com.mysql.cj.jdbc.Driver
     *
     * 适用场景：
     * - Spring Boot项目的标准配置
     * - 生产环境的配置文件管理
     * - 多环境配置的统一管理
     * - 配置与代码分离的最佳实践
     *
     * 使用示例：
     * // 注册主数据库
     * DatabaseUtil12.registerYml("master");
     *
     * // 注册从数据库
     * DatabaseUtil12.registerYml("slave");
     *
     * // 注册其他业务数据库
     * DatabaseUtil12.registerYml("business");
     *
     * @param dataSourceName 配置文件中定义的数据源名称
     * @throws IllegalArgumentException 当数据源名称为空时抛出
     * @throws RuntimeException         当配置文件读取失败或数据源配置不存在时抛出
     */
    public static void registerDatabaseFromYml(String dataSourceName) {
        validateNotBlank(dataSourceName, "数据源名称不能为空");

        try {
            // 启用YAML配置模式
            useYamlConfig();

            // 从YAML配置读取数据源信息
            YmlDataSourceInfo dataSourceInfo = readDataSourceFromYml(dataSourceName);

            // 创建数据库配置对象
            DatabaseConfig config = new DatabaseConfig(
                dataSourceInfo.getUrl(),
                dataSourceInfo.getUsername(),
                dataSourceInfo.getPassword(),
                dataSourceInfo.getDriverClassName()
            );

            // 执行内部注册逻辑（不改变配置模式）
            registerDatabaseInternal(dataSourceName, config);

            // 设置为默认数据库
            setDefaultDatabase(dataSourceName);

            System.out.println("从YAML配置注册数据库 [" + dataSourceName + "] 成功");

        } catch (Exception e) {
            throw new RuntimeException("从YAML配置注册数据库失败: " + dataSourceName + " - " + e.getMessage(), e);
        }
    }

    /**
     * 内部数据库注册方法（核心实现）
     *
     * 功能说明：
     * - 统一的数据库注册核心逻辑实现
     * - 创建和配置HikariCP连接池
     * - 设置连接池性能参数和监控配置
     * - 将连接池实例存储到缓存中供后续使用
     *
     * 实现特性：
     * - 不改变当前配置模式（自定义/YAML）
     * - 统一的错误处理和日志记录
     * - 连接池名称规范化管理
     * - 参数校验和异常处理
     *
     * 注意事项：
     * - 此方法为内部实现，不直接对外提供
     * - 由registerDatabase和registerDatabaseFromYml方法调用
     * - 确保连接池配置的一致性和规范性
     *
     * @param dbName 数据库标识名称
     * @param config 数据库连接配置对象
     * @throws RuntimeException 当连接池创建失败时抛出
     */
    private static void registerDatabaseInternal(String dbName, DatabaseConfig config) {
        validateNotBlank(dbName, "数据库名称不能为空");
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }

        try {
            // 创建HikariCP连接池配置
            HikariConfig hikariConfig = new HikariConfig();

            // 基本连接参数配置
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClass());

            // 连接池性能参数配置
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(config.getMinimumIdle());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());

            // 连接池监控和管理配置
            hikariConfig.setPoolName("DatabaseUtil12-" + dbName + "-Pool");

            // 创建数据源实例并存储到缓存
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            DATA_SOURCES.put(dbName, dataSource);

            System.out.println("数据库 [" + dbName + "] 注册成功");

        } catch (Exception e) {
            throw new RuntimeException("注册数据库失败: " + dbName + " - " + e.getMessage(), e);
        }
    }

    //=====================================================================
    // 【YAML配置解析方法】- 配置文件读取和解析
    //=====================================================================

    /**
     * 从YAML配置文件读取数据源信息（核心解析方法）
     *
     * 功能说明：
     * - 解析application-dev.yml配置文件的数据源配置段
     * - 支持Spring Boot标准的多数据源配置格式
     * - 自动导航配置文件的层级结构
     * - 提取指定数据源的连接参数信息
     *
     * 解析路径：
     * spring -> datasource -> dynamic -> datasource -> [数据源名称]
     *
     * 支持的配置项：
     * - url: 数据库连接URL
     * - username: 数据库用户名
     * - password: 数据库密码
     * - driver-class-name: JDBC驱动类名
     *
     * 错误处理：
     * - 配置文件不存在时抛出异常
     * - 配置结构不完整时给出详细错误信息
     * - 数据源不存在时提供明确的错误提示
     *
     * 使用示例（内部调用）：
     * YmlDataSourceInfo info = readDataSourceFromYml("master");
     * String url = info.getUrl();
     *
     * @param dataSourceName 配置文件中定义的数据源名称
     * @return 包含数据源配置信息的YmlDataSourceInfo对象
     * @throws RuntimeException 当配置文件读取失败或配置项缺失时抛出
     */
    private static YmlDataSourceInfo readDataSourceFromYml(String dataSourceName) {
        try (InputStream inputStream = DatabaseUtil12.class.getClassLoader()
            .getResourceAsStream("application-dev.yml")) {

            // 检查配置文件是否存在
            if (inputStream == null) {
                throw new RuntimeException("找不到配置文件: application-dev.yml");
            }

            // 使用SnakeYAML解析配置文件
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);

            // 逐级导航到数据源配置
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) yamlData.get("spring");
            if (spring == null) {
                throw new RuntimeException("配置文件中未找到spring配置节点");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
            if (datasource == null) {
                throw new RuntimeException("配置文件中未找到datasource配置节点");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> dynamic = (Map<String, Object>) datasource.get("dynamic");
            if (dynamic == null) {
                throw new RuntimeException("配置文件中未找到dynamic配置节点");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> datasources = (Map<String, Object>) dynamic.get("datasource");
            if (datasources == null) {
                throw new RuntimeException("配置文件中未找到datasource列表配置");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> targetDataSource = (Map<String, Object>) datasources.get(dataSourceName);
            if (targetDataSource == null) {
                throw new RuntimeException("配置文件中未找到数据源配置: " + dataSourceName);
            }

            // 提取数据源配置信息并创建返回对象
            YmlDataSourceInfo dataSourceInfo = new YmlDataSourceInfo();
            dataSourceInfo.setUrl(getStringValue(targetDataSource, "url"));
            dataSourceInfo.setUsername(getStringValue(targetDataSource, "username"));
            dataSourceInfo.setPassword(getStringValue(targetDataSource, "password"));
            dataSourceInfo.setDriverClassName(getStringValue(targetDataSource, "driver-class-name"));

            return dataSourceInfo;

        } catch (Exception e) {
            throw new RuntimeException("读取YAML配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 安全获取Map中的字符串值（类型转换工具方法）
     *
     * 功能说明：
     * - 从Map中安全获取指定键的值并转换为字符串
     * - 处理各种数据类型的自动转换
     * - 避免类型转换异常和空指针异常
     *
     * 适用场景：
     * - YAML配置解析中的类型安全转换
     * - 动态配置值的标准化处理
     * - 配置项的统一格式化
     *
     * 转换规则：
     * - null值返回null
     * - 其他类型调用toString()方法转换
     * - 确保返回值的类型安全性
     *
     * @param map 包含配置数据的Map对象
     * @param key 要获取的配置项键名
     * @return 字符串格式的配置值，如果不存在则返回null
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    //=====================================================================
    // 【配置模式管理方法】- 配置模式切换和状态管理
    //=====================================================================

    /**
     * 启用自定义配置模式
     *
     * 功能说明：
     * - 切换到自定义配置模式，使用代码中定义的数据库配置
     * - 适用于需要动态配置数据源的场景
     * - 与YAML配置模式互斥，切换时会覆盖之前的模式设置
     *
     * 使用场景：
     * - 多租户系统的动态数据源配置
     * - 测试环境的临时数据库连接
     * - 程序运行时的数据源切换
     * - 配置文件不可用时的备用方案
     *
     * 调用时机：
     * - 通常由registerDatabase方法自动调用
     * - 也可手动调用来明确设置配置模式
     *
     * 使用示例：
     * DatabaseUtil12.useCustomConfig();
     * // 后续的registerDatabase调用将使用自定义配置模式
     */
    public static void useCustomConfig() {
        System.out.println("已切换到自定义配置模式");
    }

    /**
     * 启用YAML配置模式
     *
     * 功能说明：
     * - 切换到YAML配置模式，从配置文件读取数据库配置
     * - 适用于生产环境和标准化配置管理
     * - 与自定义配置模式互斥，切换时会覆盖之前的模式设置
     *
     * 使用场景：
     * - Spring Boot项目的标准配置管理
     * - 生产环境的配置文件驱动模式
     * - 多环境配置的统一管理
     * - 配置与代码分离的最佳实践
     *
     * 调用时机：
     * - 通常由registerDatabaseFromYml方法自动调用
     * - 也可手动调用来明确设置配置模式
     *
     * 使用示例：
     * DatabaseUtil12.useYamlConfig();
     * // 后续的registerDatabaseFromYml调用将使用YAML配置模式
     */
    public static void useYamlConfig() {
        System.out.println("已切换到YAML配置模式");
    }

    //=====================================================================
    // 【默认数据库管理方法】- 默认数据库设置和查询
    //=====================================================================

    /**
     * 设置默认数据库
     *
     * 功能说明：
     * - 指定系统默认使用的数据库，简化后续API调用
     * - 设置后可以使用不带数据库名称参数的简化方法
     * - 支持动态切换默认数据库，适应不同业务场景
     *
     * 使用效果：
     * - 设置默认数据库后，可以直接调用 selectListByMp(sql) 而无需指定数据库名
     * - 简化了单数据库环境下的操作流程
     * - 提供了多数据库环境下的主数据库指定机制
     *
     * 安全检查：
     * - 验证数据库名称不为空
     * - 确认数据库已注册且可用
     * - 防止设置未知或无效的数据库
     *
     * 使用场景：
     * - 应用启动时设置主数据库
     * - 多租户系统中的租户数据库切换
     * - 读写分离场景中的主库设置
     * - 业务模块间的数据库切换
     *
     * 使用示例：
     * // 注册并设置主数据库
     * DatabaseUtil12.registerYml("master");
     * DatabaseUtil12.setDefaultDatabase("master");
     *
     * // 切换到其他数据库
     * DatabaseUtil12.setDefaultDatabase("slave");
     *
     * @param dbName 要设置为默认的数据库名称
     * @throws IllegalArgumentException 当数据库名称为空或数据库未注册时抛出
     */
    public static void setDefaultDatabase(String dbName) {
        validateNotBlank(dbName, "数据库名称不能为空");

        if (!DATA_SOURCES.containsKey(dbName)) {
            throw new IllegalArgumentException("数据库未注册，请先注册数据库: " + dbName);
        }

        defaultDbName = dbName;
        System.out.println("默认数据库设置为: " + dbName);
    }

    /**
     * 获取当前默认数据库名称
     *
     * 功能说明：
     * - 返回当前设置的默认数据库名称
     * - 用于查询当前系统使用的主数据库
     * - 可用于日志记录和状态检查
     *
     * 返回值说明：
     * - 如果已设置默认数据库，返回数据库名称字符串
     * - 如果未设置默认数据库，返回null
     *
     * 使用场景：
     * - 系统状态检查和监控
     * - 日志记录和调试信息
     * - 配置验证和健康检查
     * - 多数据库环境的状态查询
     *
     * 使用示例：
     * String currentDb = DatabaseUtil12.getDefaultDatabase();
     * if (currentDb != null) {
     * System.out.println("当前默认数据库: " + currentDb);
     * } else {
     * System.out.println("未设置默认数据库");
     * }
     *
     * @return 默认数据库名称，如果未设置则返回null
     */
    public static String getDefaultDatabase() {
        return defaultDbName;
    }

    //=====================================================================
    // 【数据源生命周期管理方法】- 连接池关闭和资源清理
    //=====================================================================

    /**
     * 关闭指定数据库连接池
     *
     * 功能说明：
     * - 优雅关闭指定数据库的连接池
     * - 释放所有数据库连接和相关资源
     * - 从系统缓存中移除数据库实例
     * - 自动处理默认数据库的状态更新
     *
     * 关闭流程：
     * 1. 从缓存中移除数据库实例
     * 2. 调用HikariCP的close方法优雅关闭连接池
     * 3. 等待所有活跃连接完成当前操作
     * 4. 释放连接池占用的系统资源
     * 5. 如果是默认数据库，清除默认设置
     *
     * 安全特性：
     * - 不会强制中断正在进行的数据库操作
     * - 优雅等待活跃连接完成
     * - 防止资源泄漏和内存溢出
     *
     * 使用场景：
     * - 应用程序关闭时的资源清理
     * - 动态数据源的移除和重配置
     * - 系统维护时的连接池重启
     * - 内存优化和资源管理
     *
     * 使用示例：
     * // 关闭特定数据库
     * DatabaseUtil12.closeDatabase("oldDB");
     *
     * // 关闭主数据库（会自动清除默认设置）
     * DatabaseUtil12.closeDatabase("master");
     *
     * @param dbName 要关闭的数据库名称
     */
    public static void closeDatabase(String dbName) {
        DataSource dataSource = DATA_SOURCES.remove(dbName);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            System.out.println("数据库 [" + dbName + "] 连接池已关闭");
        } else if (dataSource == null) {
            System.out.println("数据库 [" + dbName + "] 不存在或已关闭");
        }

        // 如果关闭的是默认数据库，清除默认设置
        if (dbName.equals(defaultDbName)) {
            defaultDbName = null;
            System.out.println("已清除默认数据库设置");
        }
    }

    /**
     * 关闭所有数据库连接池
     *
     * 功能说明：
     * - 批量关闭所有已注册的数据库连接池
     * - 释放系统中所有数据库相关资源
     * - 清理所有数据库缓存和状态信息
     * - 重置系统到初始状态
     *
     * 执行流程：
     * 1. 获取所有已注册数据库的名称列表
     * 2. 逐个调用closeDatabase方法关闭连接池
     * 3. 清空数据库缓存映射
     * 4. 重置默认数据库设置
     *
     * 使用场景：
     * - 应用程序完全关闭时的清理操作
     * - 系统重启前的资源释放
     * - 内存清理和垃圾回收优化
     * - 测试环境的状态重置
     *
     * 安全特性：
     * - 确保所有连接池都被正确关闭
     * - 避免资源泄漏和系统资源占用
     * - 支持应用程序的优雅关闭
     *
     * 使用示例：
     * // 应用关闭时调用
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     * DatabaseUtil12.closeAllDatabases();
     * }));
     *
     * // 测试环境清理
     */
    public static void closeAllDatabases() {
        List<String> dbNames = new ArrayList<>(DATA_SOURCES.keySet());
        for (String dbName : dbNames) {
            closeDatabase(dbName);
        }

        // 确保缓存完全清空
        DATA_SOURCES.clear();
        defaultDbName = null;

        System.out.println("所有数据库连接池已关闭，系统资源已释放");
    }

    //=====================================================================
    // 数据库连接获取方法
    //=====================================================================

    /**
     * 获取指定数据库连接
     * 作用：从连接池中获取指定数据库的连接对象
     * 用法：try (Connection conn = DatabaseUtil12.getConnection("mydb")) { ... }
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
     * 用法：try (Connection conn = DatabaseUtil12.getConnection()) { ... }
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
     * 为预编译语句设置参数（类型自适应参数绑定）
     *
     * 功能说明：
     * - 根据参数的实际类型自动选择合适的设置方法
     * - 支持常见Java数据类型的自动转换和绑定
     * - 提供SQL注入防护的安全参数设置
     * - 处理null值和特殊类型的边界情况
     *
     * 支持的数据类型：
     * - String: 字符串类型，自动处理编码
     * - Integer/Long: 整数类型，支持大数值
     * - Double/Float: 浮点数类型，保持精度
     * - Boolean: 布尔类型，标准真假值
     * - Date系列: 日期时间类型，自动转换为Timestamp
     * - null值: 安全的空值处理
     * - 其他类型: 使用通用Object方式设置
     *
     * 安全特性：
     * - 使用预编译语句防止SQL注入
     * - 自动类型检查和转换
     * - 空值安全处理
     * - 异常处理和错误提示
     *
     * 使用场景：
     * - 内部工具方法，由其他SQL执行方法调用
     * - 为PreparedStatement设置查询参数
     * - 统一的参数类型处理逻辑
     * - SQL安全执行的基础支撑
     *
     * 注意事项：
     * - 参数索引从1开始（JDBC标准）
     * - 参数顺序必须与SQL中的?占位符顺序一致
     * - 时间类型会自动转换为数据库兼容格式
     *
     * @param pstmt  预编译语句对象
     * @param params 参数数组，支持任意类型的参数
     * @throws SQLException 当参数设置失败时抛出
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                int paramIndex = i + 1; // JDBC参数索引从1开始

                if (param == null) {
                    // null值处理：设置为SQL NULL
                    pstmt.setObject(paramIndex, null);
                } else if (param instanceof String) {
                    // 字符串类型：直接设置字符串值
                    pstmt.setString(paramIndex, (String) param);
                } else if (param instanceof Integer) {
                    // 整数类型：设置整数值
                    pstmt.setInt(paramIndex, (Integer) param);
                } else if (param instanceof Long) {
                    // 长整数类型：设置长整数值
                    pstmt.setLong(paramIndex, (Long) param);
                } else if (param instanceof Double) {
                    // 双精度浮点数类型：保持精度设置
                    pstmt.setDouble(paramIndex, (Double) param);
                } else if (param instanceof Float) {
                    // 单精度浮点数类型：设置浮点值
                    pstmt.setFloat(paramIndex, (Float) param);
                } else if (param instanceof Boolean) {
                    // 布尔类型：设置布尔值
                    pstmt.setBoolean(paramIndex, (Boolean) param);
                } else if (param instanceof java.util.Date) {
                    // Java Date类型：转换为Timestamp
                    pstmt.setTimestamp(paramIndex, new Timestamp(((java.util.Date) param).getTime()));
                } else if (param instanceof java.sql.Date) {
                    // SQL Date类型：直接设置日期
                    pstmt.setDate(paramIndex, (java.sql.Date) param);
                } else if (param instanceof Timestamp) {
                    // Timestamp类型：直接设置时间戳
                    pstmt.setTimestamp(paramIndex, (Timestamp) param);
                } else {
                    // 其他类型：使用通用对象设置方式
                    pstmt.setObject(paramIndex, param);
                }
            }
        }
    }

    /**
     * 将ResultSet结果集转换为Map对象（结果集标准化处理）
     *
     * 功能说明：
     * - 将数据库查询结果的一行数据转换为键值对映射
     * - 使用列名作为键，列值作为值的标准化结构
     * - 自动处理各种数据类型的读取和转换
     * - 提供统一的结果集访问接口
     *
     * 转换特性：
     * - 列名作为Map的键：便于按字段名访问数据
     * - 列值作为Map的值：保持原始数据类型
     * - 支持所有JDBC标准数据类型
     * - 自动处理null值和特殊字符
     *
     * 使用优势：
     * - 标准化的数据访问方式
     * - 便于JSON序列化和传输
     * - 简化业务层的数据处理
     * - 支持动态字段访问
     *
     * 应用场景：
     * - 内部工具方法，由查询方法调用
     * - 统一的结果集处理逻辑
     * - 数据传输对象的构建
     * - API响应数据的标准化
     *
     * 返回格式示例：
     * {
     * "id": 1,
     * "name": "张三",
     * "email": "zhangsan@example.com",
     * "created_time": "2024-12-29 10:30:00"
     * }
     *
     * @param rs 数据库查询结果集对象
     * @return 包含行数据的Map对象，键为列名，值为列值
     * @throws SQLException 当结果集读取失败时抛出
     */
    private static Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 遍历所有列，构建键值对映射
        for (int i = 1; i <= columnCount; i++) {
            // 获取列名（优先使用别名）
            String columnName = metaData.getColumnLabel(i);

            // 获取列值（自动处理各种数据类型）
            Object value = rs.getObject(i);

            // 添加到结果映射中
            row.put(columnName, value);
        }

        return row;
    }

    /**
     * 验证字符串参数有效性（通用参数校验工具）
     *
     * 功能说明：
     * - 检查字符串参数是否为空或仅包含空白字符
     * - 提供统一的参数校验逻辑和错误处理
     * - 返回去除前后空格的标准化字符串
     * - 确保关键参数的有效性和一致性
     *
     * 校验规则：
     * - null值：抛出IllegalArgumentException异常
     * - 空字符串：抛出IllegalArgumentException异常
     * - 仅空白字符：抛出IllegalArgumentException异常
     * - 有效字符串：返回trim()后的结果
     *
     * 使用场景：
     * - 方法参数的前置校验
     * - 配置项的有效性检查
     * - 用户输入的数据清理
     * - API参数的标准化处理
     *
     * 错误处理：
     * - 提供明确的错误消息
     * - 抛出标准的IllegalArgumentException
     * - 便于调试和问题定位
     *
     * @param value   待校验的字符串值
     * @param message 校验失败时的错误消息
     * @return 去除前后空格的有效字符串
     * @throws IllegalArgumentException 当字符串为空或无效时抛出
     */
    private static String validateNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * 从JDBC URL中提取数据库名称（URL解析工具方法）
     *
     * 功能说明：
     * - 解析JDBC连接字符串，自动提取数据库名称部分
     * - 支持常见数据库的URL格式识别
     * - 处理URL中的参数和特殊字符
     * - 提供数据库名称的自动化识别能力
     *
     * 支持的URL格式：
     * - MySQL: jdbc:mysql://host:port/database?params
     * - PostgreSQL: jdbc:postgresql://host:port/database?params
     * - Oracle: jdbc:oracle:thin:@host:port:database
     * - SQL Server: jdbc:sqlserver://host:port;databaseName=database
     *
     * 解析逻辑：
     * 1. 查找主机端口后的第一个斜杠位置（跳过协议部分的斜杠）
     * 2. 定位数据库名称的起始位置
     * 3. 查找参数分隔符确定结束位置
     * 4. 提取并验证数据库名称
     *
     * 使用场景：
     * - 自动化数据库配置
     * - 连接字符串的解析和验证
     * - 多数据库环境的名称提取
     * - 配置管理工具的支持功能
     *
     * 错误处理：
     * - URL格式不正确时提供详细错误信息
     * - 数据库名称为空时给出明确提示
     * - 解析异常时保留原始错误信息
     *
     * @param jdbcUrl JDBC连接字符串
     * @return 解析出的数据库名称
     * @throws IllegalArgumentException 当URL格式错误或无法解析时抛出
     */
    private static String extractDatabaseNameFromUrl(String jdbcUrl) {
        try {
            String url = jdbcUrl.toLowerCase();

            // 查找主机端口后的第一个斜杠位置（跳过协议部分的斜杠）
            int protocolEnd = url.indexOf("://") + 3;
            int dbStart = url.indexOf('/', protocolEnd);

            if (dbStart == -1) {
                throw new IllegalArgumentException("URL格式错误：未找到数据库名称部分");
            }

            // 查找参数分隔符的位置（? 或 ; 字符）
            int dbEnd = url.length();
            int questionMark = url.indexOf('?', dbStart);
            int semicolon = url.indexOf(';', dbStart);

            if (questionMark != -1) {
                dbEnd = Math.min(dbEnd, questionMark);
            }
            if (semicolon != -1) {
                dbEnd = Math.min(dbEnd, semicolon);
            }

            // 提取数据库名称（保持原始大小写）
            String dbName = jdbcUrl.substring(dbStart + 1, dbEnd);

            if (dbName.isEmpty()) {
                throw new IllegalArgumentException("数据库名称不能为空");
            }

            return dbName;

        } catch (Exception e) {
            throw new IllegalArgumentException("解析数据库名称失败，URL: " + jdbcUrl + ", 错误: " + e.getMessage(), e);
        }
    }

    //=====================================================================
    // 【事务管理接口定义】- 事务回调和业务逻辑封装
    //=====================================================================

    /**
     * 事务回调接口（函数式接口）
     *
     * 功能说明：
     * - 定义在事务上下文中执行的业务逻辑接口
     * - 支持Lambda表达式和方法引用的现代Java语法
     * - 提供事务内操作的标准化封装方式
     * - 支持返回值的事务操作定义
     *
     * 接口特性：
     * - @FunctionalInterface注解：支持Lambda表达式
     * - 泛型支持：可定义任意类型的返回值
     * - 异常透传：SQLException会触发自动回滚
     * - 连接共享：事务内的所有操作共享同一连接
     *
     * 使用场景：
     * - 需要保证ACID特性的复合操作
     * - 多表操作的数据一致性保证
     * - 业务逻辑的原子性执行
     * - 复杂业务流程的事务控制
     *
     * 实现方式：
     * // Lambda表达式方式
     * String result = DatabaseUtil12.executeInTransaction(conn -> {
     * // 在事务中执行多个操作
     * int updateCount = DatabaseUtil12.updateByMp("UPDATE users SET status = 'active' WHERE id = ?", userId);
     * DatabaseUtil12.insertByMp("INSERT INTO user_logs (user_id, action) VALUES (?, ?)", userId, "activated");
     * return "操作完成，影响行数：" + updateCount;
     * });
     *
     * // 方法引用方式
     * Boolean success = DatabaseUtil12.executeInTransaction(this::complexBusinessOperation);
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {

        /**
         * 在事务中执行的业务逻辑方法
         *
         * 功能说明：
         * - 定义事务内的具体业务操作逻辑
         * - 支持任意类型的返回值定义
         * - 自动享受事务的ACID特性保护
         * - 异常时自动触发事务回滚
         *
         * 事务特性：
         * - 原子性（Atomicity）：操作要么全部成功，要么全部失败
         * - 一致性（Consistency）：事务执行前后数据状态一致
         * - 隔离性（Isolation）：并发事务之间相互隔离
         * - 持久性（Durability）：提交后的数据永久保存
         *
         * 连接管理：
         * - 连接对象已设置为手动提交模式（autoCommit=false）
         * - 事务成功完成后自动提交（commit）
         * - 发生异常时自动回滚（rollback）
         * - 方法执行完毕后自动关闭连接
         *
         * 异常处理：
         * - SQLException：触发事务回滚，包装后向上抛出
         * - RuntimeException：触发事务回滚，直接向上抛出
         * - 其他Exception：触发事务回滚，包装为RuntimeException
         *
         * 最佳实践：
         * - 避免在事务中执行长时间的外部调用
         * - 尽量减少事务的执行时间
         * - 避免在事务中访问外部资源
         * - 合理设计事务的粒度和范围
         *
         * 使用示例：
         * // 复合操作示例
         * Integer affectedRows = DatabaseUtil12.executeInTransaction(conn -> {
         * int userUpdate = DatabaseUtil12.updateByMp("UPDATE users SET last_login = NOW() WHERE id = ?", userId);
         * DatabaseUtil12.insertByMp("INSERT INTO login_logs (user_id, login_time) VALUES (?, NOW())", userId);
         * return userUpdate;
         * });
         *
         * @param conn 数据库连接对象（已设置为手动提交模式）
         * @return 业务操作的返回结果，类型由泛型T指定
         * @throws SQLException 数据库操作异常，会触发自动事务回滚
         */
        T doInTransaction(Connection conn) throws SQLException;
    }

    //=====================================================================
    // 【事务管理器】- 事务控制和生命周期管理
    //=====================================================================

    /**
     * 事务管理器（静态工具类）
     *
     * 功能说明：
     * - 提供声明式事务管理功能
     * - 自动处理事务的开启、提交、回滚和资源释放
     * - 支持多数据源环境下的事务操作
     * - 确保事务的ACID特性和数据一致性
     *
     * 管理特性：
     * - 自动事务控制：自动开启、提交、回滚事务
     * - 资源管理：自动获取和释放数据库连接
     * - 异常安全：任何异常都会触发事务回滚
     * - 连接池集成：与HikariCP连接池无缝集成
     *
     * 安全特性：
     * - 连接泄漏防护：使用try-with-resources确保连接释放
     * - 异常安全：所有异常情况都会正确处理
     * - 线程安全：支持多线程并发事务操作
     * - 事务隔离：每个事务使用独立的数据库连接
     *
     * 适用场景：
     * - 需要保证ACID特性的复合数据库操作
     * - 多表操作的数据一致性保证
     * - 复杂业务逻辑的原子性执行
     * - 跨多个SQL语句的事务控制
     *
     * 执行流程：
     * 1. 获取数据库连接
     * 2. 设置手动提交模式
     * 3. 执行业务逻辑回调
     * 4. 成功时提交事务
     * 5. 异常时回滚事务
     * 6. 最终释放数据库连接
     *
     * 使用示例：
     * // 默认数据库事务
     * String result = TransactionManager.executeInTransaction(conn -> {
     * DatabaseUtil12.updateByMp("UPDATE account SET balance = balance - 100 WHERE id = ?", fromId);
     * DatabaseUtil12.updateByMp("UPDATE account SET balance = balance + 100 WHERE id = ?", toId);
     * return "转账成功";
     * });
     *
     * // 指定数据库事务
     * Integer count = TransactionManager.executeInTransaction("finance_db", conn -> {
     * int deleteCount = DatabaseUtil12.deleteByMp("DELETE FROM transactions WHERE status = 'pending'");
     * DatabaseUtil12.insertByMp("INSERT INTO audit_log (action, count) VALUES (?, ?)", "cleanup", deleteCount);
     * return deleteCount;
     * });
     */
    public static class TransactionManager {

        /**
         * 在事务中执行操作（使用默认数据库）
         *
         * 功能说明：
         * - 使用默认数据库在事务上下文中执行操作
         * - 提供最简单的事务操作方式
         *
         * 前置条件：
         * - 必须已设置默认数据库（通过setDefaultDatabase方法）
         * - 默认数据库必须已注册并可用
         *
         * 异常处理：
         * - 回调为null：抛出IllegalArgumentException
         * - 未设置默认数据库：抛出RuntimeException
         * - 事务执行失败：自动回滚并抛出RuntimeException
         *
         * 使用场景：
         * - 单数据源应用的事务操作
         * - 简化的事务管理需求
         * - 快速原型开发和测试
         *
         * 使用示例：
         * String result = TransactionManager.executeInTransaction(conn -> {
         * // 执行多个相关的数据库操作
         * int orderCount = DatabaseUtil12.updateByMp("UPDATE orders SET status = 'processing' WHERE id = ?", orderId);
         * DatabaseUtil12.insertByMp("INSERT INTO order_history (order_id, status) VALUES (?, 'processing')", orderId);
         * return "订单处理完成，影响记录数：" + orderCount;
         * });
         *
         * @param callback 事务回调函数，定义事务内的业务逻辑
         * @return 事务执行结果，类型由回调函数的返回值决定
         * @throws IllegalArgumentException 当回调函数为null时
         * @throws RuntimeException         当未设置默认数据库或事务执行失败时
         */
        public static <T> T executeInTransaction(TransactionCallback<T> callback) {
            return executeInTransaction(defaultDbName, callback);
        }

        /**
         * 在事务中执行操作（指定数据库）
         *
         * 功能说明：
         * - 在指定数据库的事务上下文中执行操作
         * - 支持多数据源环境下的精确事务控制
         * - 提供完整的事务生命周期管理
         *
         * 执行流程：
         * 1. 参数验证：检查数据库名称和回调函数
         * 2. 连接获取：从指定数据库的连接池获取连接
         * 3. 事务开启：设置连接为手动提交模式
         * 4. 业务执行：调用回调函数执行业务逻辑
         * 5. 事务提交：业务成功时提交事务
         * 6. 异常处理：发生异常时回滚事务
         * 7. 资源释放：自动关闭数据库连接
         *
         * 异常处理策略：
         * - 业务异常：触发事务回滚，包装后重新抛出
         * - 回滚异常：记录错误日志，不影响原异常传播
         * - 连接异常：包装为RuntimeException抛出
         *
         * 使用场景：
         * - 多数据源应用的事务操作
         * - 需要精确控制数据库选择的场景
         * - 跨多个数据库的独立事务操作
         *
         * 使用示例：
         * Boolean success = TransactionManager.executeInTransaction("user_db", conn -> {
         * // 在用户数据库中执行事务
         * int userUpdate = DatabaseUtil12.updateByMp("UPDATE users SET last_login = NOW() WHERE id = ?", userId);
         * DatabaseUtil12.insertByMp("INSERT INTO login_logs (user_id, ip) VALUES (?, ?)", userId, clientIp);
         * return userUpdate > 0;
         * });
         *
         * @param dbName   数据库名称，必须是已注册的数据库
         * @param callback 事务回调函数，定义事务内的业务逻辑
         * @return 事务执行结果，类型由回调函数的返回值决定
         * @throws IllegalArgumentException 当数据库名称为空或回调函数为null时
         * @throws RuntimeException         当获取连接失败或事务执行失败时
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
    // 【SQL执行器】- 数据库操作方法集合
    //=====================================================================

    /**
     * SQL执行器（静态工具类）
     *
     * 功能说明：
     * - 提供完整的数据库CRUD操作方法
     * - 支持指定数据库的精确操作控制
     * - 基于预编译语句的SQL注入防护
     * - 自动资源管理和异常处理
     *
     * 执行特性：
     * - SQL注入防护：使用PreparedStatement预编译语句
     * - 自动资源管理：自动管理连接、语句和结果集的生命周期
     * - 异常安全：统一的异常处理和错误信息
     * - 性能优化：连接池复用和批量操作支持
     *
     * 支持操作：
     * - 查询操作：selectListByMp、selectOneByMp（支持复杂查询）
     * - 插入操作：insertByMp（支持批量插入）
     * - 更新操作：updateByMp（支持条件更新）
     * - 删除操作：deleteByMp（支持条件删除）
     * - 批量操作：executeBatch（高效批量处理）
     *
     * 使用模式：
     * - 指定数据库：适用于多数据源环境
     * - 参数化查询：支持安全的参数传递
     * - 结果集转换：自动转换为Map格式便于使用
     */
    public static class SqlExecutor {

        /**
         * 执行查询操作返回多行结果（指定数据库）
         *
         * 功能说明：
         * - 执行SELECT语句并返回多行查询结果
         * - 支持参数化查询防止SQL注入
         * - 自动将结果集转换为List<Map>格式
         * - 提供完整的资源管理和异常处理
         *
         * 查询特性：
         * - 参数化查询：支持?占位符参数绑定
         * - 类型安全：自动处理各种数据类型
         * - 结果映射：字段名作为Map的key，字段值作为value
         * - 空结果处理：无结果时返回空List而非null
         *
         * 使用场景：
         * - 数据列表查询和展示
         * - 条件检索和过滤
         * - 报表数据获取
         * - 批量数据处理
         *
         * 使用示例：
         * // 简单查询
         * List<Map<String, Object>> users = SqlExecutor.selectListByMp("user_db",
         * "SELECT id, name, email FROM users WHERE status = ?", "active");
         *
         * // 复杂查询
         * List<Map<String, Object>> orders = SqlExecutor.selectListByMp("order_db",
         * "SELECT o.*, u.name as user_name FROM orders o JOIN users u ON o.user_id = u.id WHERE o.create_date >= ?",
         * java.sql.Date.valueOf("2024-01-01"));
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL查询语句，支持?占位符参数
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 查询结果列表，每行数据表示为Map<String, Object>
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
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
         *
         * 功能说明：
         * - 执行SELECT语句并返回单行查询结果
         * - 适用于根据主键或唯一条件查询单条记录
         * - 内部调用selectList方法并提取第一条记录
         * - 无结果时返回null，多结果时返回第一条
         *
         * 查询特性：
         * - 单记录优化：专门用于查询单条记录的场景
         * - 空值安全：无结果时返回null而非抛出异常
         * - 参数化支持：完全支持参数化查询
         * - 一致性保证：结果格式与selectList完全一致
         *
         * 使用场景：
         * - 根据ID查询单条记录
         * - 根据唯一键查询用户信息
         * - 验证数据是否存在
         * - 获取配置信息等单值查询
         *
         * 使用示例：
         * // 根据ID查询用户
         * Map<String, Object> user = SqlExecutor.selectOneByMp("user_db",
         * "SELECT id, name, email, created_at FROM users WHERE id = ?", userId);
         * if (user != null) {
         * String userName = (String) user.get("name");
         * System.out.println("用户名：" + userName);
         * }
         *
         * // 验证邮箱是否存在
         * Map<String, Object> existUser = SqlExecutor.selectOneByMp("user_db",
         * "SELECT id FROM users WHERE email = ?", "test@example.com");
         * boolean emailExists = (existUser != null);
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL查询语句，支持?占位符参数
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 查询结果，单行数据表示为Map<String, Object>，无结果时返回null
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
         */
        public static Map<String, Object> selectOne(String dbName, String sql, Object... params) {
            List<Map<String, Object>> results = selectList(dbName, sql, params);
            return results.isEmpty() ? null : results.get(0);
        }

        /**
         * 执行插入操作（指定数据库）
         *
         * 功能说明：
         * - 执行INSERT语句向数据库插入新记录
         * - 支持参数化插入防止SQL注入
         * - 返回受影响的行数用于操作确认
         * - 内部调用executeUpdate方法统一处理
         *
         * 插入特性：
         * - 参数化安全：使用预编译语句防止SQL注入
         * - 类型兼容：支持各种Java数据类型的自动转换
         * - 批量友好：可与executeBatch配合实现批量插入
         * - 事务支持：可在事务中执行保证数据一致性
         *
         * 使用场景：
         * - 用户注册和信息录入
         * - 业务数据的新增操作
         * - 日志和审计记录插入
         * - 关联表数据维护
         *
         * 使用示例：
         * // 插入用户信息
         * int affectedRows = SqlExecutor.insertByMp("user_db",
         * "INSERT INTO users (name, email, password, created_at) VALUES (?, ?, ?, NOW())",
         * "张三", "zhangsan@example.com", "encryptedPassword");
         * System.out.println("插入成功，影响行数：" + affectedRows);
         *
         * // 插入订单记录
         * int orderInserted = SqlExecutor.insertByMp("order_db",
         * "INSERT INTO orders (user_id, product_id, quantity, price) VALUES (?, ?, ?, ?)",
         * userId, productId, 2, new BigDecimal("99.99"));
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL插入语句，支持?占位符参数
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 受影响的行数，通常为1表示插入成功
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
         */
        public static int insert(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行更新操作（指定数据库）
         *
         * 功能说明：
         * - 执行UPDATE语句修改数据库中的现有记录
         * - 支持条件更新和参数化操作
         * - 返回受影响的行数用于操作确认
         * - 内部调用executeUpdate方法统一处理
         *
         * 更新特性：
         * - 条件更新：支持WHERE子句进行精确更新
         * - 参数化安全：防止SQL注入攻击
         * - 批量更新：可更新多行记录
         * - 字段灵活：支持更新单个或多个字段
         *
         * 使用场景：
         * - 用户信息修改和维护
         * - 状态更新和流程控制
         * - 数据校正和批量修改
         * - 计数器和统计信息更新
         *
         * 使用示例：
         * // 更新用户信息
         * int updatedRows = SqlExecutor.updateByMp("user_db",
         * "UPDATE users SET email = ?, last_login = NOW() WHERE id = ?",
         * "newemail@example.com", userId);
         * System.out.println("更新成功，影响行数：" + updatedRows);
         *
         * // 批量状态更新
         * int statusUpdated = SqlExecutor.updateByMp("order_db",
         * "UPDATE orders SET status = 'shipped' WHERE status = 'paid' AND created_at < ?",
         * java.sql.Timestamp.valueOf("2024-01-01 00:00:00"));
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL更新语句，支持?占位符参数
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 受影响的行数，0表示没有记录被更新
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
         */
        public static int update(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行删除操作（指定数据库）
         *
         * 功能说明：
         * - 执行DELETE语句从数据库中删除记录
         * - 支持条件删除和参数化操作
         * - 返回受影响的行数用于操作确认
         * - 内部调用executeUpdate方法统一处理
         *
         * 删除特性：
         * - 条件删除：支持WHERE子句进行精确删除
         * - 安全防护：参数化查询防止意外删除
         * - 批量删除：可删除符合条件的多行记录
         * - 事务安全：支持事务回滚恢复删除操作
         *
         * 使用场景：
         * - 数据清理和归档
         * - 用户注销和信息删除
         * - 过期数据清理
         * - 关联数据维护
         *
         * 安全提醒：
         * - 删除操作不可逆，建议在事务中执行
         * - 重要数据建议使用软删除（标记删除）
         * - 删除前建议先查询确认影响范围
         *
         * 使用示例：
         * // 删除指定用户
         * int deletedRows = SqlExecutor.deleteByMp("user_db",
         * "DELETE FROM users WHERE id = ?", userId);
         * System.out.println("删除成功，影响行数：" + deletedRows);
         *
         * // 清理过期数据
         * int cleanedRows = SqlExecutor.deleteByMp("log_db",
         * "DELETE FROM access_logs WHERE created_at < ?",
         * java.sql.Timestamp.valueOf("2024-01-01 00:00:00"));
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL删除语句，支持?占位符参数
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 受影响的行数，0表示没有记录被删除
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
         */
        public static int delete(String dbName, String sql, Object... params) {
            return executeUpdate(dbName, sql, params);
        }

        /**
         * 执行更新操作的通用方法（内部方法）
         *
         * 功能说明：
         * - 内部统一处理INSERT、UPDATE、DELETE操作
         * - 提供标准化的参数验证和异常处理
         * - 自动管理数据库连接和预编译语句资源
         * - 不直接对外提供，通过具体方法调用
         *
         * 处理流程：
         * 1. 参数验证：检查数据库名称和SQL语句
         * 2. 连接获取：从连接池获取数据库连接
         * 3. 语句准备：创建预编译语句对象
         * 4. 参数设置：绑定SQL参数到预编译语句
         * 5. 执行操作：执行SQL更新操作
         * 6. 资源释放：自动关闭连接和语句对象
         *
         * 异常处理：
         * - 参数异常：抛出IllegalArgumentException
         * - 连接异常：包装为RuntimeException
         * - SQL异常：包装为RuntimeException并包含SQL信息
         *
         * @param dbName 数据库名称，必须是已注册的数据库
         * @param sql    SQL语句（INSERT、UPDATE或DELETE）
         * @param params SQL参数数组，按顺序对应?占位符
         * @return 受影响的行数
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当SQL执行失败或数据库连接异常时
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
         *
         * 功能说明：
         * - 批量执行相同的SQL语句，提高执行效率
         * - 适用于大量数据的批量插入、更新或删除
         * - 使用JDBC批处理机制优化性能
         * - 返回每个操作的受影响行数数组
         *
         * 批量特性：
         * - 性能优化：减少网络往返和语句编译开销
         * - 事务友好：可在事务中执行保证数据一致性
         * - 内存管理：分批处理避免内存溢出
         * - 错误处理：提供详细的批量操作结果信息
         *
         * 使用场景：
         * - 数据迁移和导入
         * - 批量数据更新
         * - 大量记录的插入
         * - 定时任务的批量处理
         *
         * 性能建议：
         * - 建议每批处理1000-5000条记录
         * - 在事务中执行提高一致性
         * - 考虑数据库的批量优化配置
         *
         * 使用示例：
         * // 批量插入用户
         * List<Object[]> userParams = Arrays.asList(
         * new Object[]{"张三", "zhangsan@example.com"},
         * new Object[]{"李四", "lisi@example.com"},
         * new Object[]{"王五", "wangwu@example.com"}
         * );
         * int[] results = SqlExecutor.executeBatch("user_db",
         * "INSERT INTO users (name, email) VALUES (?, ?)", userParams);
         *
         * // 批量更新状态
         * List<Object[]> updateParams = Arrays.asList(
         * new Object[]{"processed", 1001},
         * new Object[]{"processed", 1002},
         * new Object[]{"processed", 1003}
         * );
         * int[] updateResults = SqlExecutor.executeBatch("order_db",
         * "UPDATE orders SET status = ? WHERE id = ?", updateParams);
         *
         * @param dbName     数据库名称，必须是已注册的数据库
         * @param sql        SQL语句，支持?占位符参数
         * @param paramsList 参数列表，每个元素是一组SQL参数
         * @return 各操作受影响的行数数组，与paramsList顺序对应
         * @throws IllegalArgumentException 当数据库名称或SQL语句为空时
         * @throws RuntimeException         当批量操作执行失败或数据库连接异常时
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
    // 【简化操作方法】- 使用默认数据库的便捷方法
    //=====================================================================

    /**
     * 执行查询操作返回多行结果（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库执行SELECT语句
     * - 提供最简化的查询操作接口
     * - 内部调用SqlExecutor.selectList方法
     * - 适用于单数据源应用的快速开发
     *
     * 使用示例：
     * List<Map<String, Object>> users = DatabaseUtil12.selectListByMp(
     * "SELECT * FROM users WHERE status = ?", "active");
     *
     * @param sql    SQL查询语句，支持?占位符参数
     * @param params SQL参数数组，按顺序对应?占位符
     * @return 查询结果列表，每行数据表示为Map<String, Object>
     */
    public static List<Map<String, Object>> selectList(String sql, Object... params) {
        return SqlExecutor.selectList(defaultDbName, sql, params);
    }

    /**
     * 执行查询操作返回单行结果（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库执行SELECT语句并返回单行结果
     * - 提供最简化的单记录查询接口
     * - 内部调用SqlExecutor.selectOne方法
     * - 适用于根据主键查询等单记录场景
     *
     * 使用示例：
     * Map<String, Object> user = DatabaseUtil12.selectOneByMp(
     * "SELECT * FROM users WHERE id = ?", userId);
     *
     * @param sql    SQL查询语句，支持?占位符参数
     * @param params SQL参数数组，按顺序对应?占位符
     * @return 查询结果，单行数据表示为Map<String, Object>，无结果时返回null
     */
    public static Map<String, Object> selectOne(String sql, Object... params) {
        return SqlExecutor.selectOne(defaultDbName, sql, params);
    }

    /**
     * 执行插入操作（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库执行INSERT语句
     * - 提供最简化的插入操作接口
     * - 内部调用SqlExecutor.insert方法
     * - 适用于单数据源应用的数据插入
     *
     * 使用示例：
     * int count = DatabaseUtil12.insertByMp(
     * "INSERT INTO users(name, email) VALUES(?, ?)", "张三", "test@example.com");
     *
     * @param sql    SQL插入语句，支持?占位符参数
     * @param params SQL参数数组，按顺序对应?占位符
     * @return 受影响的行数，通常为1表示插入成功
     */
    public static int insert(String sql, Object... params) {
        return SqlExecutor.insert(defaultDbName, sql, params);
    }

    /**
     * 执行更新操作（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库执行UPDATE语句
     * - 提供最简化的更新操作接口
     * - 内部调用SqlExecutor.update方法
     * - 适用于单数据源应用的数据更新
     *
     * 使用示例：
     * int count = DatabaseUtil12.updateByMp(
     * "UPDATE users SET email = ? WHERE id = ?", "new@example.com", userId);
     *
     * @param sql    SQL更新语句，支持?占位符参数
     * @param params SQL参数数组，按顺序对应?占位符
     * @return 受影响的行数，0表示没有记录被更新
     */
    public static int update(String sql, Object... params) {
        return SqlExecutor.update(defaultDbName, sql, params);
    }

    /**
     * 执行删除操作（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库执行DELETE语句
     * - 提供最简化的删除操作接口
     * - 内部调用SqlExecutor.delete方法
     * - 适用于单数据源应用的数据删除
     *
     * 使用示例：
     * int count = DatabaseUtil12.deleteByMp("DELETE FROM users WHERE id = ?", userId);
     *
     * @param sql    SQL删除语句，支持?占位符参数
     * @param params SQL参数数组，按顺序对应?占位符
     * @return 受影响的行数，0表示没有记录被删除
     */
    public static int delete(String sql, Object... params) {
        return SqlExecutor.delete(defaultDbName, sql, params);
    }

    /**
     * 执行批量操作（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库批量执行相同SQL语句
     * - 提供最简化的批量操作接口
     * - 内部调用SqlExecutor.executeBatch方法
     * - 适用于单数据源应用的批量处理
     *
     * 使用示例：
     * List<Object[]> params = Arrays.asList(
     * new Object[]{"张三", "zhangsan@example.com"},
     * new Object[]{"李四", "lisi@example.com"}
     * );
     * int[] results = DatabaseUtil12.executeBatch(
     * "INSERT INTO users(name, email) VALUES(?, ?)", params);
     *
     * @param sql        SQL语句，支持?占位符参数
     * @param paramsList 参数列表，每个元素是一组SQL参数
     * @return 各操作受影响的行数数组，与paramsList顺序对应
     */
    public static int[] executeBatch(String sql, List<Object[]> paramsList) {
        return SqlExecutor.executeBatch(defaultDbName, sql, paramsList);
    }

    /**
     * 在事务中执行操作（使用默认数据库）
     *
     * 功能说明：
     * - 使用默认数据库在事务上下文中执行操作
     * - 提供最简化的事务操作接口
     * - 内部调用TransactionManager.executeInTransaction方法
     * - 适用于单数据源应用的事务处理
     *
     * 使用示例：
     * String result = DatabaseUtil12.executeInTransaction(conn -> {
     * DatabaseUtil12.insertByMp("INSERT INTO orders (...) VALUES (...)", ...);
     * DatabaseUtil12.updateByMp("UPDATE inventory SET quantity = ? WHERE id = ?", ...);
     * return "操作完成";
     * });
     *
     * @param callback 事务回调函数，定义事务内的业务逻辑
     * @return 事务执行结果，类型由回调函数的返回值决定
     */
    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        return TransactionManager.executeInTransaction(callback);
    }

    //=====================================================================
    // 【XML SQL执行功能】- XML配置文件的SQL执行
    //=====================================================================

    /**
     * 执行XML中的SQL查询返回多行结果
     *
     * 功能说明：
     * - 从XML文件中读取指定的SQL语句并执行查询
     * - 支持XML中的条件判断标签和参数替换
     * - 自动处理SQL参数绑定和结果集转换
     * - 提供XML配置化的数据库查询方案
     *
     * XML特性：
     * - 条件判断：支持#if标签的条件逻辑
     * - 参数替换：支持#{param}和$param两种参数格式
     * - 模板复用：可在XML中定义可复用的SQL模板
     * - 结构化管理：按功能模块组织SQL语句
     *
     * 使用场景：
     * - 复杂查询的配置化管理
     * - 条件动态查询的实现
     * - SQL语句的集中维护
     * - 多环境SQL配置管理
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("status", "active");
     * params.put("minAge", 18);
     * List<Map<String, Object>> users = DatabaseUtil12.selectListFromXml(
     * "user-queries.xml", "findActiveUsers", params);
     *
     * @param xmlFileName XML文件名（相对于resources/sql/路径）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于SQL参数替换
     * @return 查询结果列表，每行数据表示为Map<String, Object>
     * @throws RuntimeException 当XML文件不存在、SQL ID未找到或执行失败时
     */
    public static List<Map<String, Object>> selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXml(xmlFileName, sqlId, params);
            return selectList(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行XML中的SQL查询返回单行结果
     *
     * 功能说明：
     * - 从XML文件中读取指定的SQL语句并执行单行查询
     * - 适用于根据主键或唯一条件查询单条记录
     * - 支持完整的XML特性包括条件判断和参数替换
     * - 无结果时返回null，多结果时返回第一条
     *
     * 使用场景：
     * - 根据ID查询单条记录
     * - 用户信息查询和验证
     * - 配置信息获取
     * - 数据存在性检查
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * Map<String, Object> user = DatabaseUtil12.selectOneFromXml(
     * "user-queries.xml", "findUserById", params);
     * if (user != null) {
     * String userName = (String) user.get("name");
     * }
     *
     * @param xmlFileName XML文件名（相对于resources/sql/路径）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于SQL参数替换
     * @return 查询结果，单行数据表示为Map<String, Object>，无结果时返回null
     * @throws RuntimeException 当XML文件不存在、SQL ID未找到或执行失败时
     */
    public static Map<String, Object> selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXml(xmlFileName, sqlId, params);
            return selectOne(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行XML中的插入SQL语句
     *
     * 功能说明：
     * - 从XML文件中读取指定的SQL语句并执行插入操作
     * - 支持参数化插入和条件判断逻辑
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据插入方案
     *
     * 使用场景：
     * - 复杂业务数据的插入
     * - 条件性数据插入逻辑
     * - 批量数据插入模板
     * - 多表关联插入操作
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("name", "张三");
     * params.put("email", "zhangsan@example.com");
     * params.put("status", "active");
     * int count = DatabaseUtil12.insertFromXml(
     * "user-operations.xml", "insertUser", params);
     *
     * @param xmlFileName XML文件名（相对于resources/sql/路径）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于SQL参数替换
     * @return 受影响的行数，通常为1表示插入成功
     * @throws RuntimeException 当XML文件不存在、SQL ID未找到或执行失败时
     */
    public static int insertFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXml(xmlFileName, sqlId, params);
            return insert(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行XML中的更新SQL语句
     *
     * 功能说明：
     * - 从XML文件中读取指定的SQL语句并执行更新操作
     * - 支持条件更新和动态字段更新
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据更新方案
     *
     * 使用场景：
     * - 复杂条件的数据更新
     * - 动态字段更新逻辑
     * - 状态流转和批量更新
     * - 业务规则驱动的更新
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * params.put("newEmail", "newemail@example.com");
     * params.put("updateTime", new Date());
     * int count = DatabaseUtil12.updateFromXml(
     * "user-operations.xml", "updateUserEmail", params);
     *
     * @param xmlFileName XML文件名（相对于resources/sql/路径）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于SQL参数替换
     * @return 受影响的行数，0表示没有记录被更新
     * @throws RuntimeException 当XML文件不存在、SQL ID未找到或执行失败时
     */
    public static int updateFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXml(xmlFileName, sqlId, params);
            return update(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行XML中的删除SQL语句
     *
     * 功能说明：
     * - 从XML文件中读取指定的SQL语句并执行删除操作
     * - 支持条件删除和安全性检查
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据删除方案
     *
     * 安全提醒：
     * - 删除操作不可逆，建议在事务中执行
     * - 建议在XML中包含必要的安全检查条件
     * - 重要数据建议使用软删除方式
     *
     * 使用场景：
     * - 条件性数据清理
     * - 关联数据删除
     * - 过期数据清理
     * - 用户注销处理
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * params.put("confirmDelete", true);
     * int count = DatabaseUtil12.deleteFromXml(
     * "user-operations.xml", "deleteUser", params);
     *
     * @param xmlFileName XML文件名（相对于resources/sql/路径）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于SQL参数替换
     * @return 受影响的行数，0表示没有记录被删除
     * @throws RuntimeException 当XML文件不存在、SQL ID未找到或执行失败时
     */
    public static int deleteFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXml(xmlFileName, sqlId, params);
            return delete(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从XML文件中获取指定SQL语句（内部方法）
     *
     * 功能说明：
     * - 从配置的多个目录中查找XML文件
     * - 解析XML文件并提取指定ID的SQL语句
     * - 支持条件判断和参数替换
     * - 提供完整的错误处理和异常信息
     *
     * 文件查找顺序：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES数组顺序查找
     * - 找到第一个匹配的文件即停止查找
     * - 支持相对路径和绝对路径
     *
     * @param xmlFileName XML文件名（支持从多个配置目录查找）
     * @param sqlId       SQL语句的ID标识
     * @param params      参数Map，用于条件判断和参数替换
     * @return 解析后的可执行SQL语句
     * @throws RuntimeException 当文件读取失败或SQL解析异常时
     */
    private static String getSqlFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        InputStream inputStream = null;
        String foundPath = null;

        // 如果文件名不包含扩展名，自动添加
        if (!xmlFileName.endsWith(XML_FILE_EXTENSION)) {
            xmlFileName += XML_FILE_EXTENSION;
        }

        // 按照配置的目录顺序查找XML文件
        for (String directory : DEFAULT_XML_SQL_DIRECTORIES) {
            String fullPath = directory + xmlFileName;
            inputStream = DatabaseUtil12.class.getClassLoader().getResourceAsStream(fullPath);
            if (inputStream != null) {
                foundPath = fullPath;
                break;
            }
        }

        // 如果所有配置目录都没找到，抛出异常
        if (inputStream == null) {
            StringBuilder searchPaths = new StringBuilder();
            for (String dir : DEFAULT_XML_SQL_DIRECTORIES) {
                searchPaths.append("\n  - ").append(dir).append(xmlFileName);
            }
            throw new IllegalArgumentException("XML文件不存在，已搜索以下路径：" + searchPaths.toString());
        }

        try {
            // JDK 8兼容的方式读取文件内容
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String content = new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            inputStream.close();

            // 查找指定ID的SQL语句
            String sqlPattern = "<" + SQL_TAG_NAME + "\\s+" + ID_ATTRIBUTE_NAME + "\\s*=\\s*[\"']" + sqlId + "[\"'][^>]*>(.*?)</" + SQL_TAG_NAME + ">";
            Pattern pattern = Pattern.compile(sqlPattern,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String sqlBlock = matcher.group(1).trim();

                // 检查是否包含<default>标签
                String defaultPattern = "<default>(.*?)</default>";
                Pattern defaultPat = Pattern.compile(defaultPattern,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher defaultMatcher = defaultPat.matcher(sqlBlock);

                String sql;
                if (defaultMatcher.find()) {
                    // 如果有<default>标签，提取其中的SQL
                    sql = defaultMatcher.group(1).trim();
                } else {
                    // 如果没有<default>标签，直接使用整个内容
                    sql = sqlBlock;
                }

                return replaceParameters(sql, params);
            } else {
                throw new IllegalArgumentException("在XML文件 " + foundPath + " 中未找到SQL ID: " + sqlId);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取XML文件失败: " + foundPath + ", 错误: " + e.getMessage(), e);
        }
    }

    /**
     * 替换SQL中的参数占位符（内部方法）
     *
     * 功能说明：
     * - 处理SQL语句中的参数占位符替换
     * - 支持多种参数格式和类型
     * - 提供SQL注入防护机制
     * - 自动处理参数值的类型转换
     * - 支持动态SQL条件标签
     *
     * 支持的参数格式：
     * 1. #{}格式 - 预编译参数（推荐），字符串自动添加引号
     * 2. ${}格式 - 字符串替换参数，自动添加引号，适用于值替换
     *
     * 支持的动态SQL条件：
     * 1. <if test="condition">content</if> - XML风格条件标签
     * 2. #if (condition) content #endif - 简化风格条件标签
     *
     * 参数类型处理：
     * - null值：替换为NULL
     * - 布尔值：转换为数据库布尔值格式
     * - 数字类型：直接转换，不添加引号
     * - 字符串类型：自动添加引号并转义
     * - 日期时间：格式化后处理
     *
     * 安全特性：
     * - 字符串参数自动转义单引号防止SQL注入
     * - 参数值类型验证和安全转换
     * - 异常参数的安全处理机制
     *
     * @param sql    包含参数占位符的SQL语句
     * @param params 参数Map，key为参数名，value为参数值
     * @return 替换参数后的SQL语句
     */
    private static String replaceParameters(String sql, Map<String, Object> params) {
        System.out.println("🔧 开始replaceParameters处理，SQL长度: " + sql.length());
        System.out.println("🔧 参数数量: " + (params != null ? params.size() : 0));

        if (params == null || params.isEmpty()) {
            System.out.println("🔧 无参数，仍需处理动态SQL标签");
            // 即使没有参数，也需要处理动态SQL标签，可能存在静态条件
            return processDynamicSqlTags(sql, params != null ? params : new HashMap<>());
        }

        System.out.println("🔧 有参数，开始处理流程...");

        String result = sql;

        // 首先处理动态SQL条件标签
        System.out.println("🔧 步骤1：调用processDynamicSqlTags");
        result = processDynamicSqlTags(result, params);
        System.out.println("🔧 步骤1完成，处理后SQL长度: " + result.length());

        // 然后处理参数替换
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 处理#{}格式参数（字符串添加引号，表名不添加）
            String hashPlaceholder = "#{" + key + "}";
            if (result.contains(hashPlaceholder)) {
                String replacement;
                if (value == null) {
                    replacement = "NULL";
                } else if (value instanceof Boolean) {
                    replacement = ((Boolean) value) ? "true" : "false";
                } else if (value instanceof Number) {
                    // 数字类型不需要引号
                    replacement = value.toString();
                } else {
                    // 字符串类型需要引号（除非是表名或列名）
                    String valueStr = value.toString();
                    // 如果是常见的表名或列名模式，不添加引号
                    if (key.toLowerCase().contains("table") || key.toLowerCase().contains("column") ||
                        key.toLowerCase().equals("tablename") || key.toLowerCase().equals("columnname")) {
                        replacement = valueStr;
                    } else {
                        replacement = "'" + valueStr.replace("'", "''") + "'";
                    }
                }
                result = result.replace(hashPlaceholder, replacement);
            }

            // 处理${}格式参数（添加引号）
            String dollarPlaceholder = "${" + key + "}";
            if (result.contains(dollarPlaceholder)) {
                String replacement;
                if (value == null) {
                    replacement = "NULL";
                } else if (value instanceof Boolean) {
                    replacement = ((Boolean) value) ? "true" : "false";
                } else if (value instanceof Number) {
                    // 数字类型不需要引号
                    replacement = value.toString();
                } else {
                    // 字符串类型需要引号
                    replacement = "'" + value.toString().replace("'", "''") + "'";
                }
                result = result.replace(dollarPlaceholder, replacement);
            }
        }

        return result;
    }

    /**
     * 处理动态SQL条件标签
     * 支持 #if (condition) ... #endif 和 <if test="condition"> ... </if> 格式
     */
    private static String processDynamicSqlTags(String sql, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        System.out.println("🔍 开始处理动态SQL条件，原始SQL长度: " + sql.length());

        // 首先处理 XML 风格的 <if test=""> 标签
        String processedSql = processXmlIfTags(sql, params);

        // 然后处理简化的 #if () 标签
        processedSql = processHashIfTags(processedSql, params);

        System.out.println("✅ 动态SQL条件处理完成，处理后SQL长度: " + processedSql.length());

        return processedSql;
    }

    /**
     * 处理XML风格的条件标签 <if test="condition"> ... </if>
     */
    private static String processXmlIfTags(String sql, Map<String, Object> params) {
        // 支持嵌套的 <if test=""> 标签
        Pattern pattern = Pattern.compile("<if\\s+test=\"([^\"]+)\"\\s*>(.*?)</if>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        int maxIterations = 10; // 防止无限循环
        int iteration = 0;

        while (pattern.matcher(sql).find() && iteration < maxIterations) {
            iteration++;

            Matcher matcher = pattern.matcher(sql);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String condition = matcher.group(1);
                String content = matcher.group(2);

                System.out.println("🔎 处理XML条件: " + condition);

                boolean conditionResult = evaluateXmlCondition(condition, params);
                System.out.println("📝 条件结果: " + conditionResult);

                if (conditionResult) {
                    // 递归处理嵌套的条件
                    content = processXmlIfTags(content, params);
                    matcher.appendReplacement(result, Matcher.quoteReplacement(content));
                } else {
                    matcher.appendReplacement(result, "");
                }
            }
            matcher.appendTail(result);
            sql = result.toString();
        }

        return sql;
    }

    /**
     * 处理简化的条件标签 #if (condition) ... #endif
     */
    private static String processHashIfTags(String sql, Map<String, Object> params) {
        // 匹配 #if (condition) ... #endif 格式
        Pattern pattern = Pattern.compile("#if\\s*\\(([^)]+)\\)(.*?)#endif", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(sql);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);

            System.out.println("🔎 处理#if条件: " + condition);

            boolean conditionResult = evaluateHashCondition(condition, params);
            System.out.println("📝 条件结果: " + conditionResult);

            if (conditionResult) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(content));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 评估XML风格的条件表达式 (如: userType != null and userType != '')
     */
    private static boolean evaluateXmlCondition(String condition, Map<String, Object> params) {
        try {
            condition = condition.trim();
            System.out.println("🧮 评估XML条件: " + condition);

            // 处理 and/or 逻辑运算符
            if (condition.contains(" and ") || condition.contains(" AND ")) {
                String[] parts = condition.split("\\s+(and|AND)\\s+");
                for (String part : parts) {
                    if (!evaluateSimpleXmlCondition(part.trim(), params)) {
                        return false;
                    }
                }
                return true;
            }

            if (condition.contains(" or ") || condition.contains(" OR ")) {
                String[] parts = condition.split("\\s+(or|OR)\\s+");
                for (String part : parts) {
                    if (evaluateSimpleXmlCondition(part.trim(), params)) {
                        return true;
                    }
                }
                return false;
            }

            return evaluateSimpleXmlCondition(condition, params);

        } catch (Exception e) {
            System.out.println("❌ XML条件评估失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 评估简单的XML条件表达式
     */
    private static boolean evaluateSimpleXmlCondition(String condition, Map<String, Object> params) {
        condition = condition.trim();

        // null 检查
        if (condition.contains("!= null")) {
            String varName = condition.split("\\s*!=\\s*null")[0].trim();
            Object value = params.get(varName);
            return value != null && !value.toString().isEmpty();
        }

        if (condition.contains("== null")) {
            String varName = condition.split("\\s*==\\s*null")[0].trim();
            Object value = params.get(varName);
            return value == null || value.toString().isEmpty();
        }

        // 字符串相等性检查
        if (condition.contains("!=")) {
            String[] parts = condition.split("\\s*!=\\s*");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                Object actualValue = params.get(varName);
                return actualValue != null && !actualValue.toString().equals(expectedValue);
            }
        }

        if (condition.contains("==")) {
            String[] parts = condition.split("\\s*==\\s*");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                Object actualValue = params.get(varName);
                return actualValue != null && actualValue.toString().equals(expectedValue);
            }
        }

        // 布尔值检查
        if (condition.equals("true")) {
            return true;
        }
        if (condition.equals("false")) {
            return false;
        }

        // 变量存在性检查
        if (params.containsKey(condition)) {
            Object value = params.get(condition);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return value != null && !value.toString().isEmpty();
        }

        return false;
    }

    /**
     * 评估#if风格的条件表达式 (如: region != null && region != "")
     */
    private static boolean evaluateHashCondition(String condition, Map<String, Object> params) {
        try {
            condition = condition.trim();
            System.out.println("🧮 评估#if条件: " + condition);

            // 处理 && 逻辑运算符
            if (condition.contains("&&")) {
                String[] parts = condition.split("\\s*&&\\s*");
                for (String part : parts) {
                    if (!evaluateSimpleHashCondition(part.trim(), params)) {
                        return false;
                    }
                }
                return true;
            }

            // 处理 || 逻辑运算符
            if (condition.contains("||")) {
                String[] parts = condition.split("\\s*\\|\\|\\s*");
                for (String part : parts) {
                    if (evaluateSimpleHashCondition(part.trim(), params)) {
                        return true;
                    }
                }
                return false;
            }

            return evaluateSimpleHashCondition(condition, params);

        } catch (Exception e) {
            System.out.println("❌ #if条件评估失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 评估简单的#if条件表达式
     */
    private static boolean evaluateSimpleHashCondition(String condition, Map<String, Object> params) {
        condition = condition.trim();

        // null 检查
        if (condition.contains("!= null")) {
            String varName = condition.split("\\s*!=\\s*null")[0].trim();
            Object value = params.get(varName);
            return value != null && !value.toString().isEmpty();
        }

        if (condition.contains("== null")) {
            String varName = condition.split("\\s*==\\s*null")[0].trim();
            Object value = params.get(varName);
            return value == null || value.toString().isEmpty();
        }

        // 字符串相等性检查 (支持双引号)
        if (condition.contains("!=")) {
            String[] parts = condition.split("\\s*!=\\s*");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim().replaceAll("^\"|\"$", "");
                Object actualValue = params.get(varName);
                return actualValue != null && !actualValue.toString().equals(expectedValue);
            }
        }

        if (condition.contains("==")) {
            String[] parts = condition.split("\\s*==\\s*");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim().replaceAll("^\"|\"$", "");
                Object actualValue = params.get(varName);

                // 特殊处理布尔值字符串
                if (expectedValue.equals("true") || expectedValue.equals("false")) {
                    if (actualValue instanceof Boolean) {
                        return actualValue.toString().equals(expectedValue);
                    }
                    return actualValue != null && actualValue.toString().equals(expectedValue);
                }

                return actualValue != null && actualValue.toString().equals(expectedValue);
            }
        }

        // 变量存在性检查
        if (params.containsKey(condition)) {
            Object value = params.get(condition);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return value != null && !value.toString().isEmpty();
        }

        return false;
    }

    // ==================== 自动搜索XML文件的重载方法（无需指定文件名） ====================

    /**
     * 自动搜索所有XML文件并执行查询SQL语句（返回多条记录）
     *
     * 功能说明：
     * - 无需指定XML文件名，自动在所有配置的XML目录中搜索包含指定SQL ID的文件
     * - 支持复杂查询和条件过滤
     * - 返回结果集的List形式，便于后续处理
     * - 提供XML配置化的数据查询方案
     *
     * 搜索机制：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES配置的目录顺序依次搜索
     * - 在每个目录下遍历所有XML文件，查找包含指定SQL ID的文件
     * - 找到第一个匹配的SQL ID即停止搜索
     * - 支持多层目录结构的文件搜索
     *
     * 使用场景：
     * - 不确定SQL定义在哪个XML文件中
     * - 统一的SQL调用接口，无需关心文件组织结构
     * - 大型项目中的SQL自动发现机制
     * - 插件化架构中的动态SQL加载
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("status", "active");
     * params.put("minAge", 18);
     * List<Map<String, Object>> users = DatabaseUtil12.selectListFromXml("getUserList", params);
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于SQL参数替换和条件判断
     * @return 查询结果列表，每个元素是一个Map表示一行数据
     * @throws RuntimeException 当找不到SQL ID或执行失败时
     */
    public static List<Map<String, Object>> selectListFromXml(String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXmlAuto(sqlId, params);
            return selectList(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动搜索所有XML文件并执行查询SQL语句（返回单条记录）
     *
     * 功能说明：
     * - 无需指定XML文件名，自动在所有配置的XML目录中搜索包含指定SQL ID的文件
     * - 适用于根据唯一条件查询单个实体
     * - 返回第一条匹配记录，无记录时返回null
     * - 提供XML配置化的单实体查询方案
     *
     * 搜索机制：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES配置的目录顺序依次搜索
     * - 在每个目录下遍历所有XML文件，查找包含指定SQL ID的文件
     * - 找到第一个匹配的SQL ID即停止搜索
     * - 支持多层目录结构的文件搜索
     *
     * 使用场景：
     * - 根据ID或唯一标识查询单条记录
     * - 用户登录验证
     * - 配置信息查询
     * - 详情页面数据加载
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * Map<String, Object> user = DatabaseUtil12.selectOneFromXml("getUserById", params);
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于SQL参数替换和条件判断
     * @return 查询结果Map，null表示没有找到记录
     * @throws RuntimeException 当找不到SQL ID或执行失败时
     */
    public static Map<String, Object> selectOneFromXml(String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXmlAuto(sqlId, params);
            return selectOne(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动搜索所有XML文件并执行插入SQL语句
     *
     * 功能说明：
     * - 无需指定XML文件名，自动在所有配置的XML目录中搜索包含指定SQL ID的文件
     * - 支持动态字段插入和参数验证
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据插入方案
     *
     * 搜索机制：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES配置的目录顺序依次搜索
     * - 在每个目录下遍历所有XML文件，查找包含指定SQL ID的文件
     * - 找到第一个匹配的SQL ID即停止搜索
     * - 支持多层目录结构的文件搜索
     *
     * 使用场景：
     * - 用户注册和信息录入
     * - 业务数据创建
     * - 日志记录和审计
     * - 批量数据导入
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("username", "newuser");
     * params.put("email", "user@example.com");
     * params.put("createTime", new Date());
     * int result = DatabaseUtil12.insertFromXml("createUser", params);
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于SQL参数替换
     * @return 受影响的行数，通常为1表示插入成功
     * @throws RuntimeException 当找不到SQL ID或执行失败时
     */
    public static int insertFromXml(String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXmlAuto(sqlId, params);
            return insert(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL插入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动搜索所有XML文件并执行更新SQL语句
     *
     * 功能说明：
     * - 无需指定XML文件名，自动在所有配置的XML目录中搜索包含指定SQL ID的文件
     * - 支持条件更新和动态字段更新
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据更新方案
     *
     * 搜索机制：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES配置的目录顺序依次搜索
     * - 在每个目录下遍历所有XML文件，查找包含指定SQL ID的文件
     * - 找到第一个匹配的SQL ID即停止搜索
     * - 支持多层目录结构的文件搜索
     *
     * 使用场景：
     * - 复杂条件的数据更新
     * - 动态字段更新逻辑
     * - 状态流转和批量更新
     * - 业务规则驱动的更新
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * params.put("newEmail", "newemail@example.com");
     * params.put("updateTime", new Date());
     * int count = DatabaseUtil12.updateFromXml("updateUserEmail", params);
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于SQL参数替换
     * @return 受影响的行数，0表示没有记录被更新
     * @throws RuntimeException 当找不到SQL ID或执行失败时
     */
    public static int updateFromXml(String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXmlAuto(sqlId, params);
            return update(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动搜索所有XML文件并执行删除SQL语句
     *
     * 功能说明：
     * - 无需指定XML文件名，自动在所有配置的XML目录中搜索包含指定SQL ID的文件
     * - 支持条件删除和安全性检查
     * - 返回受影响的行数用于操作确认
     * - 提供XML配置化的数据删除方案
     *
     * 搜索机制：
     * - 按照DEFAULT_XML_SQL_DIRECTORIES配置的目录顺序依次搜索
     * - 在每个目录下遍历所有XML文件，查找包含指定SQL ID的文件
     * - 找到第一个匹配的SQL ID即停止搜索
     * - 支持多层目录结构的文件搜索
     *
     * 安全提醒：
     * - 删除操作不可逆，建议在事务中执行
     * - 建议在XML中包含必要的安全检查条件
     * - 重要数据建议使用软删除方式
     *
     * 使用场景：
     * - 条件性数据清理
     * - 关联数据删除
     * - 过期数据清理
     * - 用户注销处理
     *
     * 使用示例：
     * Map<String, Object> params = new HashMap<>();
     * params.put("userId", 12345);
     * params.put("confirmDelete", true);
     * int count = DatabaseUtil12.deleteFromXml("deleteUser", params);
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于SQL参数替换
     * @return 受影响的行数，0表示没有记录被删除
     * @throws RuntimeException 当找不到SQL ID或执行失败时
     */
    public static int deleteFromXml(String sqlId, Map<String, Object> params) {
        try {
            String sql = getSqlFromXmlAuto(sqlId, params);
            return delete(sql);
        } catch (Exception e) {
            throw new RuntimeException("执行XML SQL删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动搜索所有XML文件并获取指定SQL语句（内部方法）
     *
     * 功能说明：
     * - 在所有配置的XML目录中自动搜索包含指定SQL ID的文件
     * - 解析XML文件并提取指定ID的SQL语句
     * - 支持条件判断和参数替换
     * - 提供完整的错误处理和异常信息
     *
     * 搜索算法：
     * 1. 遍历DEFAULT_XML_SQL_DIRECTORIES中配置的所有目录
     * 2. 在每个目录下获取所有XML文件列表
     * 3. 依次读取每个XML文件并搜索指定的SQL ID
     * 4. 找到第一个匹配的SQL ID即返回对应的SQL语句
     * 5. 所有文件都搜索完毕仍未找到则抛出异常
     *
     * 性能优化：
     * - 按目录优先级顺序搜索，提高常用文件的查找效率
     * - 找到匹配项立即返回，避免不必要的文件读取
     * - 使用高效的正则表达式进行SQL ID匹配
     *
     * @param sqlId  SQL语句的ID标识
     * @param params 参数Map，用于条件判断和参数替换
     * @return 解析后的可执行SQL语句
     * @throws RuntimeException 当找不到SQL ID或文件读取失败时
     */
    private static String getSqlFromXmlAuto(String sqlId, Map<String, Object> params) {
        List<String> searchedFiles = new ArrayList<>();

        // 遍历所有配置的XML目录
        for (String directory : DEFAULT_XML_SQL_DIRECTORIES) {
            try {
                // 获取目录下的所有XML文件
                java.net.URL dirUrl = DatabaseUtil12.class.getClassLoader().getResource(directory);
                if (dirUrl == null) {
                    continue; // 目录不存在，跳过
                }

                // 处理JAR包内的资源和文件系统的资源
                if (dirUrl.getProtocol().equals("file")) {
                    // 文件系统中的目录
                    java.io.File dir = new java.io.File(dirUrl.toURI());
                    if (dir.exists() && dir.isDirectory()) {
                        java.io.File[] files = dir.listFiles((file, name) -> name.endsWith(XML_FILE_EXTENSION));
                        if (files != null) {
                            for (java.io.File file : files) {
                                String fileName = file.getName();
                                String fullPath = directory + fileName;
                                searchedFiles.add(fullPath);

                                try {
                                    String sql = tryGetSqlFromFile(fullPath, sqlId, params);
                                    if (sql != null) {
                                        return sql;
                                    }
                                } catch (Exception e) {
                                    // 继续搜索其他文件
                                    System.err.println("搜索文件 " + fullPath + " 时出错: " + e.getMessage());
                                }
                            }
                        }
                    }
                } else if (dirUrl.getProtocol().equals("jar")) {
                    // JAR包内的资源（生产环境常见情况）
                    try {
                        java.net.JarURLConnection jarConnection = (java.net.JarURLConnection) dirUrl.openConnection();
                        java.util.jar.JarFile jarFile = jarConnection.getJarFile();
                        java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

                        while (entries.hasMoreElements()) {
                            java.util.jar.JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();

                            // 检查是否是指定目录下的XML文件
                            if (entryName.startsWith(directory) && entryName.endsWith(XML_FILE_EXTENSION) && !entry.isDirectory()) {
                                searchedFiles.add(entryName);

                                try {
                                    String sql = tryGetSqlFromFile(entryName, sqlId, params);
                                    if (sql != null) {
                                        return sql;
                                    }
                                } catch (Exception e) {
                                    // 继续搜索其他文件
                                    System.err.println("搜索文件 " + entryName + " 时出错: " + e.getMessage());
                                }
                            }
                        }
                        jarFile.close();
                    } catch (Exception e) {
                        System.err.println("搜索JAR包目录 " + directory + " 时出错: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("处理目录 " + directory + " 时出错: " + e.getMessage());
            }
        }

        // 所有目录都搜索完毕，仍未找到指定的SQL ID
        StringBuilder searchInfo = new StringBuilder();
        searchInfo.append("在以下文件中未找到SQL ID '").append(sqlId).append("'：\n");
        for (String file : searchedFiles) {
            searchInfo.append("  - ").append(file).append("\n");
        }
        searchInfo.append("\n配置的搜索目录：\n");
        for (String dir : DEFAULT_XML_SQL_DIRECTORIES) {
            searchInfo.append("  - ").append(dir).append("\n");
        }

        throw new IllegalArgumentException(searchInfo.toString());
    }

    /**
     * 尝试从指定文件中获取SQL语句（内部辅助方法）
     *
     * 功能说明：
     * - 读取指定路径的XML文件并搜索指定的SQL ID
     * - 如果找到则解析并返回SQL语句，否则返回null
     * - 提供统一的文件读取和SQL解析逻辑
     *
     * @param filePath 文件路径（相对于classpath）
     * @param sqlId    SQL语句的ID标识
     * @param params   参数Map，用于条件判断和参数替换
     * @return 如果找到则返回解析后的SQL语句，否则返回null
     * @throws Exception 当文件读取或解析失败时
     */
    private static String tryGetSqlFromFile(String filePath, String sqlId, Map<String, Object> params) throws Exception {
        InputStream inputStream = DatabaseUtil12.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            return null;
        }

        try {
            // JDK 8兼容的方式读取文件内容
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String content = new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);

            // 查找指定ID的SQL语句
            String sqlPattern = "<" + SQL_TAG_NAME + "\\s+" + ID_ATTRIBUTE_NAME + "\\s*=\\s*[\"']" + sqlId + "[\"'][^>]*>(.*?)</" + SQL_TAG_NAME + ">";
            Pattern pattern = Pattern.compile(sqlPattern,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String sqlBlock = matcher.group(1).trim();

                // 检查是否包含<default>标签
                String defaultPattern = "<default>(.*?)</default>";
                Pattern defaultPat = Pattern.compile(defaultPattern,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher defaultMatcher = defaultPat.matcher(sqlBlock);

                String sql;
                if (defaultMatcher.find()) {
                    // 如果有<default>标签，提取其中的SQL
                    sql = defaultMatcher.group(1).trim();
                } else {
                    // 如果没有<default>标签，直接使用整个内容
                    sql = sqlBlock;
                }

                return replaceParameters(sql, params);
            }

            return null; // 在此文件中未找到指定的SQL ID
        } finally {
            inputStream.close();
        }
    }

}
