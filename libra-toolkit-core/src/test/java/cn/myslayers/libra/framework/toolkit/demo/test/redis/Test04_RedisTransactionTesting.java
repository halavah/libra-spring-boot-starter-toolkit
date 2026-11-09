package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil 事务功能测试")
public class Test04_RedisTransactionTesting {

    private static final String TEST_KEY_COUNTER = "test:transaction:counter";
    private static final String TEST_KEY_OTHER = "test:transaction:other";

    @BeforeAll
    static void setUp() {
        System.out.println("--- 开始 RedisUtil 事务测试 ---");
        RedissonUtil.KeyOps.delete(TEST_KEY_COUNTER, TEST_KEY_OTHER);
        RedissonUtil.StringOps.set(TEST_KEY_COUNTER, "100");
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.KeyOps.delete(TEST_KEY_COUNTER, TEST_KEY_OTHER);
        System.out.println("--- 结束 RedisUtil 事务测试 ---");
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试标准事务 (MULTI/EXEC)")
    void test_StandardTransaction_ShouldSucceed() {
        System.out.println("--- 测试 1: 标准事务 ---");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            accessor.multi();
            accessor.StringOps.incrBy(TEST_KEY_COUNTER, 10);
            accessor.StringOps.set(TEST_KEY_OTHER, "value1");
            List<Object> results = accessor.exec();

            System.out.println("事务执行结果: " + results);
            String finalValue = RedissonUtil.StringOps.get(TEST_KEY_COUNTER);
            System.out.println("最终计数器值: " + finalValue);
            // 验证
            if (results != null && !results.isEmpty() && "110".equals(finalValue)) {
                System.out.println("验证成功: 计数器已正确增加。");
            } else {
                System.out.println("验证失败: 事务未按预期执行。");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试事务放弃 (DISCARD)")
    void test_DiscardTransaction_ShouldRevert() {
        System.out.println("--- 测试 2: 放弃事务 ---");
        String initialValue = RedissonUtil.StringOps.get(TEST_KEY_COUNTER);
        System.out.println("初始计数器值: " + initialValue);

        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            accessor.multi();
            accessor.StringOps.incrBy(TEST_KEY_COUNTER, 5);
            accessor.discard();
        }

        String finalValue = RedissonUtil.StringOps.get(TEST_KEY_COUNTER);
        System.out.println("最终计数器值: " + finalValue);
        // 验证
        if (initialValue.equals(finalValue)) {
            System.out.println("验证成功: 计数器值未改变。");
        } else {
            System.out.println("验证失败: 事务放弃后值不应改变。");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 WATCH 成功 (无并发修改)")
    void test_Watch_ShouldSucceed_WhenNotModified() {
        System.out.println("--- 测试 3: WATCH 成功 ---");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            accessor.watch(TEST_KEY_COUNTER);
            String valueBefore = accessor.StringOps.get(TEST_KEY_COUNTER);
            System.out.println("WATCH 后，读取值为: " + valueBefore);

            accessor.multi();
            accessor.StringOps.incrBy(TEST_KEY_COUNTER, 1);
            List<Object> results = accessor.exec();

            System.out.println("事务执行结果: " + results);
            // 验证
            if (results != null && !results.isEmpty()) {
                System.out.println("验证成功: 事务已成功执行。");
                String finalValue = RedissonUtil.StringOps.get(TEST_KEY_COUNTER);
                System.out.println("最终计数器值: " + finalValue);
            } else {
                System.out.println("验证失败: 事务本应成功执行。");
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 WATCH 失败 (有并发修改)")
    void test_Watch_ShouldFail_WhenModifiedConcurrently() throws InterruptedException {
        System.out.println("--- 测试 4: WATCH 因并发修改而失败 ---");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 1. 主线程监视 key
            accessor.watch(TEST_KEY_COUNTER);
            System.out.println("主线程: 已 WATCH " + TEST_KEY_COUNTER);

            // 2. 启动另一个线程，在主线程事务执行前修改 key
            executor.submit(() -> {
                System.out.println("并发线程: 开始修改 " + TEST_KEY_COUNTER);
                RedissonUtil.StringOps.set(TEST_KEY_COUNTER, "999");
                System.out.println("并发线程: 已将 " + TEST_KEY_COUNTER + " 修改为 999");
            });

            // 等待并发线程执行完毕
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            // 3. 主线程开启并执行事务
            accessor.multi();
            accessor.StringOps.incrBy(TEST_KEY_COUNTER, 1);
            List<Object> results = accessor.exec();

            System.out.println("主线程: 事务执行结果: " + results);
            // 验证
            if (results == null || results.isEmpty()) {
                System.out.println("验证成功: 事务因 WATCH 的 key 被修改而执行失败 (返回空或null)。");
            } else {
                System.out.println("验证失败: 事务本应失败。");
            }
            String finalValue = RedissonUtil.StringOps.get(TEST_KEY_COUNTER);
            System.out.println("最终计数器值: " + finalValue + " (应为并发线程修改的值)");
        }
    }
}
