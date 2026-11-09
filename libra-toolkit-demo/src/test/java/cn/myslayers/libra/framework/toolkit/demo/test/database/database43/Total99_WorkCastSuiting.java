package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Database43 功能模块测试套件")
public class Total99_WorkCastSuiting {

    @Test
    @Order(1)
    @DisplayName("执行 ObjectMapper 测试")
    void runObjectMapperTests() {
        executeTestClass(Test01_ObjectMapperTesting.class);
    }

    @Test
    @Order(2)
    @DisplayName("执行 MyBatisSqlExecutorEnhanced 测试")
    void runMyBatisSqlExecutorEnhancedTests() {
        executeTestClass(Test02_MyBatisSqlExecutorEnhancedTesting.class);
    }

    @Test
    @Order(3)
    @DisplayName("执行 PaginationHandlerEnhanced 测试")
    void runPaginationHandlerEnhancedTests() {
        executeTestClass(Test03_PaginationHandlerEnhancedTesting.class);
    }

    @Test
    @Order(11)
    @DisplayName("执行 DepartmentMapper 测试")
    void runDepartmentMapperTests() {
        executeTestClass(Test11_DepartmentMapperTesting.class);
    }

    @Test
    @Order(12)
    @DisplayName("执行 EmployeeMapper 测试")
    void runEmployeeMapperTests() {
        executeTestClass(Test12_EmployeeMapperTesting.class);
    }

    @Test
    @Order(13)
    @DisplayName("执行 OrderMapper 测试")
    void runOrderMapperTests() {
        executeTestClass(Test13_OrderMapperTesting.class);
    }

    @Test
    @Order(14)
    @DisplayName("执行 ProjectMapper 测试")
    void runProjectMapperTests() {
        executeTestClass(Test14_ProjectMapperTesting.class);
    }

    @Test
    @Order(15)
    @DisplayName("执行 ReportMapper 测试")
    void runReportMapperTests() {
        executeTestClass(Test15_ReportMapperTesting.class);
    }

    @Test
    @Order(16)
    @DisplayName("执行 SalaryMapper 测试")
    void runSalaryMapperTests() {
        executeTestClass(Test16_SalaryMapperTesting.class);
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
