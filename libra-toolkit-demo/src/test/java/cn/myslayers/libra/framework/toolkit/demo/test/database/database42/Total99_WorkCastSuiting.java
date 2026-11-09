package cn.myslayers.libra.framework.toolkit.demo.test.database.database42;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * 测试套件
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 完整功能测试套件")
public class Total99_WorkCastSuiting {

    @Test
    @Order(1)
    @DisplayName("执行Druid注册与配置测试")
    void runDruidRegistrationAndConfigTests() {
        System.out.println("📋 开始执行: Druid注册与配置测试");
        executeTestClass(Test01_DruidRegistrationAndConfigTesting.class);
    }

    @Test
    @Order(2)
    @DisplayName("执行Druid连接处理测试")
    void runDruidConnectionHandlingTests() {
        System.out.println("📋 开始执行: Druid连接处理测试");
        executeTestClass(Test02_DruidConnectionHandlingTesting.class);
    }

    @Test
    @Order(3)
    @DisplayName("执行Druid连接池状态测试")
    void runDruidPoolStateTests() {
        System.out.println("📋 开始执行: Druid连接池状态测试");
        executeTestClass(Test03_DruidPoolStateTesting.class);
    }

    @Test
    @Order(4)
    @DisplayName("执行Druid数据源切换测试")
    void runDruidDataSourceSwitchingTests() {
        System.out.println("📋 开始执行: Druid数据源切换测试");
        executeTestClass(Test04_DruidDataSourceSwitchingTesting.class);
    }

    @Test
    @Order(5)
    @DisplayName("执行Druid数据源关闭测试")
    void runDruidDataSourceClosingTests() {
        System.out.println("\n🎯 执行Druid数据源关闭测试...");
        executeTestClass(Test05_DruidDataSourceClosingTesting.class);
        System.out.println("✅ Druid数据源关闭测试执行完成");
    }

    @Test
    @Order(6)
    @DisplayName("执行Druid并发测试")
    void runDruidConcurrencyTests() {
        System.out.println("\n🔒 执行Druid并发测试...");
        executeTestClass(Test06_DruidConcurrencyTesting.class);
        System.out.println("✅ Druid并发测试执行完成");
    }

    /**
     * 执行指定的测试类
     */
    private void executeTestClass(Class<?> testClass) {
        try {
            System.out.println("🔍 正在执行测试类: " + testClass.getSimpleName());

            // 创建测试发现请求
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();

            // 创建启动器
            Launcher launcher = LauncherFactory.create();

            // 创建监听器来收集测试结果
            SummaryGeneratingListener listener = new SummaryGeneratingListener();

            // 执行测试
            launcher.execute(request, listener);

            // 获取测试执行摘要
            TestExecutionSummary summary = listener.getSummary();

            // 输出测试结果统计
            System.out.println("📈 " + testClass.getSimpleName() + " 执行结果:");
            System.out.println("   ✅ 成功: " + summary.getTestsSucceededCount());
            System.out.println("   ❌ 失败: " + summary.getTestsFailedCount());
            System.out.println("   ⏭️ 跳过: " + summary.getTestsSkippedCount());

            if (summary.getTestsFailedCount() > 0) {
                System.out.println("   🔥 失败详情:");
                summary.getFailures().forEach(failure -> {
                    System.out.println("      - " + failure.getTestIdentifier().getDisplayName());
                    System.out.println("        " + failure.getException().getMessage());
                });
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 执行测试类 " + testClass.getSimpleName() + " 时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
