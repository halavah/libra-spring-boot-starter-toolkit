package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

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
 * DatabaseUtil31 完整功能测试套件
 * <p>
 * 该测试套件通过自定义的测试运行器，按功能模块顺序执行所有相关的测试类。
 * 这种结构化的方法确保了从底层核心功能到高层API的全面、有序的验证。
 * </p>
 *
 * 测试类覆盖范围：
 * - SimpleSqlParser：直接SQL和XML文件SQL解析器测试
 * - SimpleSqlExecutor：直接SQL和XML文件SQL执行器测试
 * - MyBatisSqlParser：基础语法、条件标签、迭代标签、XML和Include测试
 * - MyBatisSqlExecutor：MyBatis风格的SQL执行器测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 完整功能测试套件")
public class Total99_WorkCastSuiting {

    /**
     * ==================== 1. SimpleSqlParser 解析器测试 ====================
     */
    @Nested
    @DisplayName("分类1：SimpleSqlParser 解析器测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SimpleSqlParserTests {
        @Test @Order(1) @DisplayName("Test11: SimpleSqlParser - 直接SQL解析测试")
        void runSimpleSqlParserDirectSqlTest() {
            executeTestClass(Test11_SimpleSqlParser_DirectSqlTesting.class);
        }

        @Test @Order(2) @DisplayName("Test12: SimpleSqlParser - XML文件SQL解析测试")
        void runSimpleSqlParserXmlFileTest() {
            executeTestClass(Test12_SimpleSqlParser_XmlFileSqlTesting.class);
        }
    }

    /**
     * ==================== 2. SimpleSqlExecutor 执行器测试 ====================
     */
    @Nested
    @DisplayName("分类2：SimpleSqlExecutor 执行器测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SimpleSqlExecutorTests {
        @Test @Order(1) @DisplayName("Test21: SimpleSqlExecutor - 直接SQL执行器测试")
        void runSimpleSqlExecutorDirectSqlTest() {
            executeTestClass(Test21_SimpleSqlExecutor_DirectSqlTesting.class);
        }

        @Test @Order(2) @DisplayName("Test22: SimpleSqlExecutor - XML文件SQL执行器测试")
        void runSimpleSqlExecutorXmlFileTest() {
            executeTestClass(Test22_SimpleSqlExecutor_XmlFileSqlTesting.class);
        }
    }

    /**
     * ==================== 3. MyBatisSqlParser 语法和标签测试 ====================
     */
    @Nested
    @DisplayName("分类3：MyBatisSqlParser 语法和标签测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ParserSyntaxTests {
        @Test @Order(1) @DisplayName("Test31: Parser - 基础语法测试")
        void runParserBasicSyntaxTest() {
            executeTestClass(Test31_ParserBasicSyntaxTesting.class);
        }

        @Test @Order(2) @DisplayName("Test32: Parser - 条件标签测试 (if/choose/when/otherwise)")
        void runParserConditionalTagsTest() {
            executeTestClass(Test32_ParserConditionalTagsTesting.class);
        }

        @Test @Order(3) @DisplayName("Test33: Parser - 迭代标签测试 (foreach/trim/set/where)")
        void runParserIterativeTagsTest() {
            executeTestClass(Test33_ParserIterativeTagsTesting.class);
        }

        @Test @Order(4) @DisplayName("Test34: Parser - XML和Include标签测试")
        void runParserXmlAndIncludeTest() {
            executeTestClass(Test34_ParserXmlAndIncludeTesting.class);
        }
    }

    /**
     * ==================== 4. MyBatisSqlExecutor 高级执行器测试 ====================
     */
    @Nested
    @DisplayName("分类4：MyBatisSqlExecutor 高级执行器测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MyBatisSqlExecutorTests {
        @Test @Order(1) @DisplayName("Test41: MyBatisSqlExecutor - 执行器测试1")
        void runMyBatisSqlExecutorTest1() {
            executeTestClass(Test41_MyBatisSqlExecutorTesting.class);
        }

        @Test @Order(2) @DisplayName("Test42: MyBatisSqlExecutor - 执行器测试2")
        void runMyBatisSqlExecutorTest2() {
            executeTestClass(Test42_MyBatisSqlExecutorTesting.class);
        }

        @Test @Order(3) @DisplayName("Test43: MyBatis - 参数处理测试")
        void runMyBatisParametersTest() {
            executeTestClass(Test43_MyBatisParametersTesting.class);
        }

        @Test @Order(4) @DisplayName("Test44: MyBatis - JSqlParser集成测试")
        void runMyBatisJSqlParserTest() {
            executeTestClass(Test44_MyBatisJSqlParserTesting.class);
        }
    }

    /**
     * 执行指定的测试类
     */
    private void executeTestClass(Class<?> testClass) {
        try {
            System.out.println("🔍 正在执行测试类: " + testClass.getSimpleName());

            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();

            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            launcher.execute(request, listener);
            TestExecutionSummary summary = listener.getSummary();

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
