package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil.RedisDataCore;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.DataSourceCore 核心功能测试")
public class Test02_RedisDataSourceTesting {

    private static final String KEY_PREFIX = "test:datasourcecore:";
    private static final String CONCURRENT_KEY = KEY_PREFIX + "concurrent_key";
    private static final String THREAD_SWITCH_KEY = KEY_PREFIX + "thread_switch_key";

    // 使用一个在 RedisUtil 中未预先配置的数据源进行测试，以触发初始化逻辑
    private static final String DYNAMIC_DB = "db_for_core_test";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 DataSourceCore 测试 ======");
        // 使用新的 register API 动态注册一个用于测试的数据源
        RedisDataCore.register(DYNAMIC_DB, "127.0.0.1", 6379, "", 2);
        System.out.println("已动态注册测试数据源: " + DYNAMIC_DB);

        // 清理可能存在的历史数据
        // 清理默认数据源
        cleanUpTestData(null);
        // 清理动态测试数据源
        cleanUpTestData(DYNAMIC_DB);
    }

    @AfterAll
    static void tearDown() {
        // 再次清理
        cleanUpTestData(null);
        cleanUpTestData(DYNAMIC_DB);
        System.out.println("====== 结束 DataSourceCore 测试 ======");
    }

    private static void cleanUpTestData(String dbKey) {
        // 使用 try-with-resources 和新的 accessor() 方法确保连接正确关闭
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(dbKey)) {
            String currentDb = (dbKey == null) ? "默认" : dbKey;
            Set<String> keysToDelete = accessor.KeyOps.keys(KEY_PREFIX + "*");
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                System.out.println("在数据源 [" + currentDb + "] 中清理 keys: " + keysToDelete);
                accessor.KeyOps.delete(keysToDelete);
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试连接池并发初始化")
    void testConcurrentInitialization() throws InterruptedException {
        System.out.println("--- 测试连接池并发初始化 ---");
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        // 用于确保所有线程尽可能同时开始
        CountDownLatch startLatch = new CountDownLatch(1);
        // 用于等待所有线程执行完毕
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        // 用于记录初始化代码块被执行的次数（理论上应为1次）
        // 注意：这里我们无法直接测试私有方法，但可以通过观察日志或行为来推断。
        // 在这个测试中，我们主要验证在高并发下获取连接和执行命令的正确性。
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    // 使用 try-with-resources 和新的 accessor(dbKey) 方法
                    try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(DYNAMIC_DB)) {
                        // Lettuce 的 PING 命令在 RedisAccessor 中没有直接暴露，但我们可以通过执行任何一个简单命令来验证连接
                        String val = accessor.StringOps.get("ping_test_key"); // 任何读操作都可以
                        successCount.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + " 连接成功，操作已执行。");
                    }
                } catch (Exception e) {
                    System.err.println(Thread.currentThread().getName() + " 执行失败: " + e.getMessage());
                } finally {
                    endLatch.countDown(); // 完成一个
                }
            });
        }

        startLatch.countDown(); // 所有线程，开始！
        endLatch.await(); // 等待所有线程结束
        executor.shutdown();

        System.out.println("并发测试完成，成功 PING 的线程数: " + successCount.get());
        System.out.println("预期成功数: " + threadCount);
        // 如果所有线程都成功，说明并发初始化和连接获取是线程安全的
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试线程安全的数据源切换")
    void testThreadLocalDataSourceSwitching() throws InterruptedException {
        System.out.println("--- 测试线程安全的数据源切换 ---");

        // 1. 主线程在默认数据源上设置一个值
        String mainThreadValue = "value_from_main_thread";
        RedissonUtil.StringOps.set(THREAD_SWITCH_KEY, mainThreadValue);
        System.out.println("主线程在默认DB设置: " + THREAD_SWITCH_KEY + " -> " + mainThreadValue);

        // 2. 创建一个子线程，切换到动态DB进行操作
        String subThreadValue = "value_from_sub_thread";

        Thread subThread = new Thread(() -> {
            // 使用新的链式API，注册并设置线程上下文
            try (RedissonUtil.RedisAccessor accessor = RedisDataCore.register(DYNAMIC_DB, "127.0.0.1", 6379, "", 2).setThreadDataSourceContext()) {
                System.out.println("子线程已切换到DB: " + DYNAMIC_DB);
                accessor.StringOps.set(THREAD_SWITCH_KEY, subThreadValue);
                System.out.println("子线程在 " + DYNAMIC_DB + " 设置: " + THREAD_SWITCH_KEY + " -> " + subThreadValue);

                String valueInSubThread = accessor.StringOps.get(THREAD_SWITCH_KEY);
                System.out.println("子线程在 " + DYNAMIC_DB + " 读取值: " + valueInSubThread);
                if (!subThreadValue.equals(valueInSubThread)) {
                    System.err.println("子线程数据验证失败！");
                }
            } finally {
                // try-with-resources 会自动关闭 accessor，但线程上下文需要手动清理
                RedissonUtil.LettuceCore.clear();
                System.out.println("子线程清理上下文");
            }
        });

        subThread.start();
        subThread.join(); // 等待子线程执行完毕

        // 3. 回到主线程，验证默认数据源的值没有被子线程污染
        String valueInMainThread = RedissonUtil.StringOps.get(THREAD_SWITCH_KEY);
        System.out.println("主线程在子线程执行后，再次读取默认DB的值: " + valueInMainThread);
        if (!mainThreadValue.equals(valueInMainThread)) {
            System.err.println("主线程数据被污染！");
        }

        // 4. 验证子线程在另一个DB上设置的值确实存在
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(DYNAMIC_DB)) {
            String valueInDynamicDB = accessor.StringOps.get(THREAD_SWITCH_KEY);
            System.out.println("主线程切换到 " + DYNAMIC_DB + " 读取值: " + valueInDynamicDB);
            if (!subThreadValue.equals(valueInDynamicDB)) {
                System.err.println("动态DB数据验证失败！");
            }
        }
    }
}
