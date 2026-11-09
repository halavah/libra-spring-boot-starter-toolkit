package cn.myslayers.libra.framework.toolkit.demo.test.database.database41;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil41;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// 直接测试 SimpleDataSource 内部实现类
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - SimpleDataSource 实现测试")
class Test05_SimpleDataSourceImplTesting {

    private static DatabaseUtil41.DataSourceCore.DatabaseConfig h2Config;

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil41.LoggerCore.setDebugLogEnabled(true);
        System.out.println("==================== 测试开始：SimpleDataSource实现 ====================");
        h2Config = new DatabaseUtil41.DataSourceCore.DatabaseConfig(
            "jdbc:h2:mem:sds_test;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 辅助方法：通过反射创建 SimpleDataSource 实例
    private DataSource createSimpleDataSource(DatabaseUtil41.DataSourceCore.DatabaseConfig config) throws Exception {
        // SimpleDataSource 是一个私有内部类，需要使用反射来创建实例
        Class<?> simpleDataSourceClass = Class.forName("cn.myslayers.app.utils.DatabaseUtil41$DataSourceCore$SimpleDataSource");
        Constructor<?> constructor = simpleDataSourceClass.getDeclaredConstructor(DatabaseUtil41.DataSourceCore.DatabaseConfig.class);
        constructor.setAccessible(true); // 提升访问权限
        return (DataSource) constructor.newInstance(config);
    }

    // 测试 SimpleDataSource 的基本连接获取
    @Test
    @Order(1)
    @DisplayName("1. 测试 SimpleDataSource.getConnection()")
    void testGetConnection() {
        System.out.println("\n--- 测试 1：SimpleDataSource.getConnection() ---");
        try {
            DataSource sds = createSimpleDataSource(h2Config);
            try (Connection conn = sds.getConnection()) {
                boolean isValid = conn != null && !conn.isClosed();
                System.out.println("获取的连接是否有效: " + isValid);
                if (isValid) {
                    System.out.println("成功：SimpleDataSource.getConnection() 返回了有效连接。");
                } else {
                    System.out.println("失败：SimpleDataSource.getConnection() 未能返回有效连接。");
                }
            }
        } catch (Exception e) {
            System.out.println("异常：测试 getConnection() 时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试带用户名和密码的连接获取
    @Test
    @Order(2)
    @DisplayName("2. 测试 SimpleDataSource.getConnection(user, pass)")
    void testGetConnectionWithCredentials() {
        System.out.println("\n--- 测试 2：SimpleDataSource.getConnection(user, pass) ---");
        try {
            DataSource sds = createSimpleDataSource(h2Config);
            try (Connection conn = sds.getConnection("sa", "")) {
                boolean isValid = conn != null && !conn.isClosed();
                System.out.println("使用凭证获取的连接是否有效: " + isValid);
                if (isValid) {
                    System.out.println("成功：SimpleDataSource.getConnection(user, pass) 返回了有效连接。");
                } else {
                    System.out.println("失败：SimpleDataSource.getConnection(user, pass) 未能返回有效连接。");
                }
            }
        } catch (Exception e) {
            System.out.println("异常：测试 getConnection(user, pass) 时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试使用无效驱动类名时构造函数的行为
    @Test
    @Order(3)
    @DisplayName("3. 测试无效驱动类名")
    void testInvalidDriverClassName() {
        System.out.println("\n--- 测试 3：使用无效驱动类名 ---");
        DatabaseUtil41.DataSourceCore.DatabaseConfig invalidConfig = new DatabaseUtil41.DataSourceCore.DatabaseConfig(
            "jdbc:h2:mem:invalid", "sa", "", "com.invalid.Driver"
        );
        try {
            createSimpleDataSource(invalidConfig);
            System.out.println("失败：使用无效驱动类名时，没有抛出预期的异常。");
        } catch (Exception e) {
            // 反射调用会包装原始异常，需要检查根本原因
            Throwable cause = e.getCause();
            System.out.println("成功：捕获到预期异常: " + cause.getClass().getSimpleName() + " - " + cause.getMessage());
            if (cause instanceof RuntimeException && cause.getMessage().contains("无法加载数据库驱动")) {
                System.out.println("验证成功：异常类型和消息内容符合预期。");
            } else {
                System.out.println("验证失败：异常类型或消息内容不符合预期。");
                e.printStackTrace();
            }
        }
    }

    // 测试其他未实现的方法是否按预期抛出异常或返回默认值
    @Test
    @Order(4)
    @DisplayName("4. 测试未实现的方法")
    void testUnimplementedMethods() {
        System.out.println("\n--- 测试 4：测试其他未实现的方法 ---");
        try {
            DataSource sds = createSimpleDataSource(h2Config);
            // 测试 getParentLogger
            try {
                sds.getParentLogger();
                System.out.println("失败：getParentLogger() 未抛出 SQLFeatureNotSupportedException。");
            } catch (java.sql.SQLFeatureNotSupportedException e) {
                System.out.println("成功：getParentLogger() 按预期抛出 SQLFeatureNotSupportedException。");
            }
            // 测试 getLogWriter
            if (sds.getLogWriter() == null) {
                 System.out.println("成功：getLogWriter() 按预期返回 null。");
            } else {
                 System.out.println("失败：getLogWriter() 未返回 null。");
            }
        } catch (Exception e) {
            System.out.println("异常：测试未实现的方法时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
