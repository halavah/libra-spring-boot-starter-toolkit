package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
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
 * DatabaseUtil32 完整功能测试套件
 * <p>
 * 该测试套件通过自定义的测试运行器，按功能模块顺序执行所有相关的测试类。
 * 这种结构化的方法确保了从底层核心功能到高层API的全面、有序的验证。
 * </p>
 *
 * 测试类覆盖范围：
 * - 核心组件：LoggerCore、DataSourceCore、Transaction、PaginationHandler、BatchOperationHandler
 * - SimpleSqlExecutor API：直接API、链式操作、完整性测试
 * - MyBatisSqlExecutor API：解析器、执行器、静态API测试
 * - 高级MyBatis API：解析器、执行器、顶层API测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil32 完整功能测试套件")
public class Total99_WorkCastSuiting {

    /**
     * ==================== 1. 核心组件测试 ====================
     */
    @Nested
    @DisplayName("分类1：核心组件测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CoreComponentTests {
        @Test @Order(1) @DisplayName("Test01: LoggerCore - 日志核心功能测试")
        void runLoggerCoreTest() {
            executeTestClass(Test01_LoggerCoreTesting.class);
        }

        @Test @Order(2) @DisplayName("Test02: DataSourceCore - 数据源核心功能测试")
        void runDataSourceCoreTest() {
            executeTestClass(Test02_DataSourceCoreTesting.class);
        }

        @Test @Order(3) @DisplayName("Test03: Transaction - 事务管理测试")
        void runTransactionTest() {
            executeTestClass(Test03_TransactionTesting.class);
        }

        @Test @Order(4) @DisplayName("Test04: PaginationHandler - 分页处理器测试")
        void runPaginationHandlerTest() {
            executeTestClass(Test04_PaginationHandlerTesting.class);
        }

        @Test @Order(5) @DisplayName("Test05: BatchOperationHandler - 批量操作处理器测试")
        void runBatchOperationHandlerTest() {
            executeTestClass(Test05_BatchOperationHandlerTesting.class);
        }
    }

    /**
     * ==================== 2. SimpleSqlExecutor API测试 ====================
     */
    @Nested
    @DisplayName("分类2：SimpleSqlExecutor API测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SimpleSqlExecutorApiTests {
        @Test @Order(1) @DisplayName("Test11: SimpleSqlExecutor - 基础API测试")
        void runApiSimpleSqlExecutorTest() {
            executeTestClass(Test11_ApiSimpleSqlExecutorTesting.class);
        }

        @Test @Order(2) @DisplayName("Test12: Chain Operations - 链式操作测试")
        void runApiChainOperationsTest() {
            executeTestClass(Test12_ApiChainOperationsTesting.class);
        }

        @Test @Order(3) @DisplayName("Test13: Completeness - API完整性测试")
        void runApiCompletenessTest() {
            executeTestClass(Test13_ApiCompletenessTesting.class);
        }
    }

    /**
     * ==================== 3. MyBatisSqlExecutor API测试 ====================
     */
    @Nested
    @DisplayName("分类3：MyBatisSqlExecutor API测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MyBatisSqlExecutorApiTests {
        @Test @Order(1) @DisplayName("Test21: MyBatisSqlExecutor - API测试")
        void runApiMyBatisSqlExecutorTest() {
            executeTestClass(Test21_ApiMyBatisSqlExecutorTesting.class);
        }

        @Test @Order(2) @DisplayName("Test22: Static MyBatis Parser - 静态解析器测试")
        void runApiStaticMyBatisParserTest() {
            executeTestClass(Test22_ApiStaticMyBatisParserTesting.class);
        }

        @Test @Order(3) @DisplayName("Test23: Static MyBatis Executor - 静态执行器测试")
        void runApiStaticMyBatisExecutorTest() {
            executeTestClass(Test23_ApiStaticMyBatisExecutorTesting.class);
        }
    }

    /**
     * ==================== 4. 高级MyBatis API测试 ====================
     */
    @Nested
    @DisplayName("分类4：高级MyBatis API测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AdvancedMyBatisApiTests {
        @Test @Order(1) @DisplayName("Test31: MyBatisSqlParser - 解析器API测试")
        void runApiMyBatisSqlParserTest() {
            executeTestClass(Test31_ApiMyBatisSqlParserTesting.class);
        }

        @Test @Order(2) @DisplayName("Test32: MyBatisSqlExecutor - 执行器API测试")
        void runApiMyBatisSqlExecutorApiTest() {
            executeTestClass(Test32_ApiMyBatisSqlExecutorTesting.class);
        }

        @Test @Order(3) @DisplayName("Test33: Top Level By Mp - 顶层MyBatis API测试")
        void runApiTopLevelByMpTest() {
            executeTestClass(Test33_ApiTopLevelByMpTesting.class);
        }
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
