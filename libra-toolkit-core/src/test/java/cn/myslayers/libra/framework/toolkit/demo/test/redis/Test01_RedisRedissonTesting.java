package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.*;
import org.redisson.api.RedissonClient;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.RedissonCore 核心功能测试")
public class Test01_RedisRedissonTesting {

    private static final String KEY_PREFIX = "test:redissoncore:";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 RedissonCore 测试 ======");
        cleanUpTestData();
    }

    @AfterAll
    static void tearDown() {
        cleanUpTestData();
        System.out.println("====== 结束 RedissonCore 测试 ======");
    }

    private static void cleanUpTestData() {
        RedissonClient client = RedissonUtil.RedissonCore.getClient();
        Iterable<String> keysToDelete = client.getKeys().getKeysByPattern(KEY_PREFIX + "*");

        // 将 Iterable 转换为 List，以便后续操作
        java.util.List<String> keysList = new java.util.ArrayList<>();
        keysToDelete.forEach(keysList::add);

        if (!keysList.isEmpty()) {
            System.out.println("在 Redisson 默认数据源中清理 keys: " + keysList);
            // RKeys.delete() 方法接受一个 String 数组 (String...)
            client.getKeys().delete(keysList.toArray(new String[0]));
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试 RedissonClient 并发获取")
    void testConcurrentInitialization() throws InterruptedException {
        System.out.println("--- 测试 RedissonClient 并发获取 ---");
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    RedissonClient client = RedissonUtil.RedissonCore.getClient();
                    if (client != null && !client.isShutdown()) {
                        successCount.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + " 获取 RedissonClient 成功");
                    }
                } catch (Exception e) {
                    System.err.println(Thread.currentThread().getName() + " 执行失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        System.out.println("并发测试完成，成功获取 Client 的线程数: " + successCount.get());
        System.out.println("预期成功数: " + threadCount);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 RedissonClient 单例实例一致性")
    void testSingletonInstanceConsistency() throws InterruptedException {
        System.out.println("--- 测试 RedissonClient 单例实例一致性 ---");
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        // 使用线程安全的 Set 来存储获取到的 Client 实例的哈希码
        CopyOnWriteArraySet<Integer> clientHashCodes = new CopyOnWriteArraySet<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    RedissonClient client = RedissonUtil.RedissonCore.getClient();
                    clientHashCodes.add(System.identityHashCode(client));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("所有线程获取到的 RedissonClient 实例哈希码集合: " + clientHashCodes);
        System.out.println("集合中的实例数量: " + clientHashCodes.size());
        System.out.println("预期实例数量: 1");
        // 如果所有线程获取到的都是同一个实例，那么集合的大小应该为1
        if (clientHashCodes.size() != 1) {
            System.err.println("RedissonClient 单例模式测试失败！获取到了多个实例。");
        } else {
            System.out.println("RedissonClient 单例模式测试成功。");
        }
    }
}
