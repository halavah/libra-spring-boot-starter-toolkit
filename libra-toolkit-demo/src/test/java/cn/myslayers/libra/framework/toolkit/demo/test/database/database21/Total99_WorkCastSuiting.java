package cn.myslayers.libra.framework.toolkit.demo.test.database.database21;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

// 测试套件，用于按顺序执行所有 DataSourceCore 相关的测试
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 完整功能测试套件")
class Total99_WorkCastSuiting {

    @Test
    @Order(1)
    @DisplayName("1. 注册功能测试")
    void runRegistrationTests() {
        System.out.println("\n🔄 执行注册功能测试...");
        executeTestClass(Test01_DataSourceRegistrationTesting.class);
        System.out.println("✅ 注册功能测试执行完成");
    }

    @Test
    @Order(2)
    @DisplayName("2. 连接处理测试")
    void runConnectionHandlingTests() {
        System.out.println("\n🔄 执行连接处理测试...");
        executeTestClass(Test02_ConnectionHandlingTesting.class);
        System.out.println("✅ 连接处理测试执行完成");
    }

    @Test
    @Order(3)
    @DisplayName("3. 动态切换测试")
    void runSwitchingTests() {
        System.out.println("\n🔄 执行动态切换测试...");
        executeTestClass(Test03_DataSourceSwitchingTesting.class);
        System.out.println("✅ 动态切换测试执行完成");
    }

    @Test
    @Order(4)
    @DisplayName("4. 关闭与注销测试")
    void runClosingTests() {
        System.out.println("\n🔄 执行关闭与注销测试...");
        executeTestClass(Test04_DataSourceClosingTesting.class);
        System.out.println("✅ 关闭与注销测试执行完成");
    }

    @Test
    @Order(5)
    @DisplayName("5. SimpleDataSource 实现测试")
    void runSimpleDataSourceImplTests() {
        System.out.println("\n🔄 执行 SimpleDataSource 实现测试...");
        executeTestClass(Test05_SimpleDataSourceImplTesting.class);
        System.out.println("✅ SimpleDataSource 实现测试执行完成");
    }

    @Test
    @Order(6)
    @DisplayName("6. 并发安全测试")
    void runConcurrencyTests() {
        System.out.println("\n🔄 执行并发安全测试...");
        executeTestClass(Test06_ConcurrencyTesting.class);
        System.out.println("✅ 并发安全测试执行完成");
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
