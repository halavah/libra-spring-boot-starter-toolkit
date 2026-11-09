package cn.myslayers.libra.framework.toolkit.demo.test.redis;

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
@DisplayName("Redisson 功能模块测试套件")
public class Total99_WorkCastSuiting {

    @Test
    @Order(1)
    @DisplayName("执行 RedisRedisson 测试")
    void runRedisRedissonTests() {
        executeTestClass(Test01_RedisRedissonTesting.class);
    }

    @Test
    @Order(2)
    @DisplayName("执行 RedisDataSource 测试")
    void runRedisDataSourceTests() {
        executeTestClass(Test02_RedisDataSourceTesting.class);
    }

    @Test
    @Order(3)
    @DisplayName("执行 RedisAccessor 测试")
    void runRedisAccessorTests() {
        executeTestClass(Test03_RedisAccessorTesting.class);
    }

    @Test
    @Order(4)
    @DisplayName("执行 Transaction 测试")
    void runTransactionTests() {
        executeTestClass(Test04_RedisTransactionTesting.class);
    }

    @Test
    @Order(11)
    @DisplayName("执行 RedisKeyOps 测试")
    void runRedisKeyOpsTests() {
        executeTestClass(Test11_RedisKeyOpsTesting.class);
    }

    @Test
    @Order(12)
    @DisplayName("执行 RedisStringOps 测试")
    void runRedisStringOpsTests() {
        executeTestClass(Test12_RedisStringOpsTesting.class);
    }

    @Test
    @Order(13)
    @DisplayName("执行 RedisHashOps 测试")
    void runRedisHashOpsTests() {
        executeTestClass(Test13_RedisHashOpsTesting.class);
    }

    @Test
    @Order(14)
    @DisplayName("执行 RedisListOps 测试")
    void runRedisListOpsTests() {
        executeTestClass(Test14_RedisListOpsTesting.class);
    }

    @Test
    @Order(15)
    @DisplayName("执行 RedisSetOps 测试")
    void runRedisSetOpsTests() {
        executeTestClass(Test15_RedisSetOpsTesting.class);
    }

    @Test
    @Order(16)
    @DisplayName("执行 RedisZSetOps 测试")
    void runRedisZSetOpsTests() {
        executeTestClass(Test16_RedisZSetOpsTesting.class);
    }

    @Test
    @Order(17)
    @DisplayName("执行 RedisBitmap 测试")
    void runRedisBitmapTests() {
        executeTestClass(Test17_RedisBitmapTesting.class);
    }

    @Test
    @Order(18)
    @DisplayName("执行 RedisServer 测试")
    void runRedisServerTests() {
        executeTestClass(Test18_RedisServerTesting.class);
    }

    @Test
    @Order(21)
    @DisplayName("执行 RedisPubSub 测试")
    void runRedisPubSubTests() {
        executeTestClass(Test21_RedisPubSubTesting.class);
    }

    @Test
    @Order(22)
    @DisplayName("执行 RedisScripting 测试")
    void runRedisScriptingTests() {
        executeTestClass(Test22_RedisScriptingTesting.class);
    }

    @Test
    @Order(23)
    @DisplayName("执行 RedisHyperLogLog 测试")
    void runRedisHyperLogLogTests() {
        executeTestClass(Test23_RedisHyperLogLogTesting.class);
    }

    @Test
    @Order(24)
    @DisplayName("执行 RedisGeoOps 测试")
    void runRedisGeoOpsTests() {
        executeTestClass(Test24_RedisGeoOpsTesting.class);
    }

    @Test
    @Order(25)
    @DisplayName("执行 RedisStreamOps 测试")
    void runRedisStreamOpsTests() {
        executeTestClass(Test25_RedisStreamOpsTesting.class);
    }

    @Test
    @Order(26)
    @DisplayName("执行 RedisHyperLogLog 扩展测试")
    void runRedisHyperLogLogExtendedTests() {
        executeTestClass(Test26_RedisHyperLogLogTesting.class);
    }

    @Test
    @Order(27)
    @DisplayName("执行 RedisBloomFilter 测试")
    void runRedisBloomFilterTests() {
        executeTestClass(Test27_RedisBloomFilterTesting.class);
    }

    @Test
    @Order(31)
    @DisplayName("执行 ReentrantLock 测试")
    void runReentrantLockTests() {
        executeTestClass(Test31_ReentrantLockTesting.class);
    }

    @Test
    @Order(32)
    @DisplayName("执行 FairLock 测试")
    void runFairLockTests() {
        executeTestClass(Test32_FairLockTesting.class);
    }

    @Test
    @Order(33)
    @DisplayName("执行 ReadWriteLock 测试")
    void runReadWriteLockTests() {
        executeTestClass(Test33_ReadWriteLockTesting.class);
    }

    @Test
    @Order(34)
    @DisplayName("执行 Semaphore 测试")
    void runSemaphoreTests() {
        executeTestClass(Test34_SemaphoreTesting.class);
    }

    @Test
    @Order(35)
    @DisplayName("执行 CountDownLatch 测试")
    void runCountDownLatchTests() {
        executeTestClass(Test35_CountDownLatchTesting.class);
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
