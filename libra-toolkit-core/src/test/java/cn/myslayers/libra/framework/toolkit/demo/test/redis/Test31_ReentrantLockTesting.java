package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redis 可重入锁功能测试
 *
 * <p>测试 RedisUtil 中的可重入锁功能，包括：
 * <ul>
 *   <li>基础锁获取和释放</li>
 *   <li>可重入性验证</li>
 *   <li>超时锁获取</li>
 *   <li>并发场景测试</li>
 *   <li>链式 API 和静态 API 对比</li>
 * </ul>
 */
public class Test31_ReentrantLockTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 可重入锁功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试基础可重入锁功能")
    void testBasicReentrantLock() {
        System.out.println("\n--- 测试基础可重入锁功能 ---");

        String lockKey = "test:reentrant:basic";

        // 测试链式 API
        System.out.println("测试链式 API 可重入锁:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 尝试获取锁
            boolean acquired = accessor.ReentrantLockOps.tryLock(lockKey);
            System.out.println("获取可重入锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取可重入锁，执行业务逻辑...");
                Thread.sleep(100); // 模拟业务处理

                // 释放锁
                accessor.ReentrantLockOps.unlock(lockKey);
                System.out.println("释放可重入锁完成");
            }
        } catch (Exception e) {
            System.out.println("可重入锁测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API
        System.out.println("\n测试顶层静态 API 可重入锁:");
        try {
            boolean acquired = RedissonUtil.ReentrantLockOps.tryLock(lockKey, 1, 10, TimeUnit.SECONDS);
            System.out.println("获取可重入锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取可重入锁，执行业务逻辑...");
                Thread.sleep(100);
                RedissonUtil.ReentrantLockOps.unlock(lockKey);
                System.out.println("释放可重入锁完成");
            }
        } catch (Exception e) {
            System.out.println("可重入锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试可重入性验证")
    void testReentrantFeature() {
        System.out.println("\n--- 测试可重入性验证 ---");

        String lockKey = "test:reentrant:feature";

        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 第一次获取锁
            boolean firstLock = accessor.ReentrantLockOps.tryLock(lockKey);
            System.out.println("第一次获取锁结果: " + firstLock);

            if (firstLock) {
                System.out.println("第一次获取锁成功");

                // 同一线程再次获取锁（测试可重入性）
                boolean secondLock = accessor.ReentrantLockOps.tryLock(lockKey);
                System.out.println("同一线程再次获取锁结果: " + secondLock);

                if (secondLock) {
                    System.out.println("可重入性验证成功！");

                    // 第三次获取锁
                    boolean thirdLock = accessor.ReentrantLockOps.tryLock(lockKey);
                    System.out.println("第三次获取锁结果: " + thirdLock);

                    if (thirdLock) {
                        System.out.println("多次可重入验证成功！");
                        accessor.ReentrantLockOps.unlock(lockKey); // 释放第三次
                    }

                    accessor.ReentrantLockOps.unlock(lockKey); // 释放第二次
                }

                accessor.ReentrantLockOps.unlock(lockKey); // 释放第一次
                System.out.println("所有锁已释放");
            }
        } catch (Exception e) {
            System.out.println("可重入性测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试超时锁获取")
    void testLockWithTimeout() {
        System.out.println("\n--- 测试超时锁获取 ---");

        String lockKey = "test:reentrant:timeout";

        try {
            // 测试带租约时间的锁
            System.out.println("测试带租约时间的锁:");
            RedissonUtil.ReentrantLockOps.lock(lockKey, 2, TimeUnit.SECONDS);
            System.out.println("获取带租约时间的锁成功，2秒后自动释放");

            // 等待锁自动释放
            Thread.sleep(3000);

            // 验证锁是否已自动释放
            boolean canLockAgain = RedissonUtil.ReentrantLockOps.tryLock(lockKey);
            System.out.println("锁自动释放后再次获取: " + canLockAgain);

            if (canLockAgain) {
                RedissonUtil.ReentrantLockOps.unlock(lockKey);
                System.out.println("验证成功：锁已自动释放");
            }

        } catch (Exception e) {
            System.out.println("超时锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试并发场景下的可重入锁")
    void testConcurrentReentrantLock() {
        System.out.println("\n--- 测试并发场景下的可重入锁 ---");

        String lockKey = "test:reentrant:concurrent";
        String counterKey = "test:reentrant:counter";
        int threadCount = 5;

        // 初始化计数器
        RedissonUtil.StringOps.set(counterKey, "0");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        System.out.println("启动 " + threadCount + " 个线程测试并发可重入锁:");

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备就绪

                    System.out.println("线程 " + threadId + " 尝试获取可重入锁...");
                    boolean acquired = RedissonUtil.ReentrantLockOps.tryLock(lockKey, 2, 5, TimeUnit.SECONDS);

                    if (acquired) {
                        System.out.println("线程 " + threadId + " 成功获取可重入锁");
                        
                        // 模拟业务处理：递增计数器
                        int current = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        Thread.sleep(100); // 模拟处理时间
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(current + 1));
                        System.out.println("线程 " + threadId + " 完成业务处理，计数器: " + (current + 1));

                        RedissonUtil.ReentrantLockOps.unlock(lockKey);
                        System.out.println("线程 " + threadId + " 释放可重入锁");
                    } else {
                        System.out.println("线程 " + threadId + " 获取可重入锁失败");
                    }

                } catch (Exception e) {
                    System.out.println("线程 " + threadId + " 异常: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        try {
            startLatch.countDown(); // 启动所有线程
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            System.out.println("并发测试完成: " + finished);

            // 验证最终计数器值
            String finalCount = RedissonUtil.StringOps.get(counterKey);
            System.out.println("最终计数器值: " + finalCount);

        } catch (InterruptedException e) {
            System.out.println("并发测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试链式 API 和静态 API 性能对比")
    void testApiPerformanceComparison() {
        System.out.println("\n--- 测试链式 API 和静态 API 性能对比 ---");

        String lockKey1 = "test:reentrant:chain";
        String lockKey2 = "test:reentrant:static";
        int iterations = 100;

        // 链式 API 性能测试
        System.out.println("链式 API 性能测试 (" + iterations + " 次):");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
                accessor.ReentrantLockOps.lock(lockKey1, 1, TimeUnit.SECONDS);
                accessor.ReentrantLockOps.unlock(lockKey1);
            } catch (Exception e) {
                System.out.println("链式 API 测试异常: " + e.getMessage());
            }
        }
        long chainTime = System.currentTimeMillis() - startTime;

        // 静态 API 性能测试
        System.out.println("静态 API 性能测试 (" + iterations + " 次):");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            try {
                RedissonUtil.ReentrantLockOps.lock(lockKey2, 1, TimeUnit.SECONDS);
                RedissonUtil.ReentrantLockOps.unlock(lockKey2);
            } catch (Exception e) {
                System.out.println("静态 API 测试异常: " + e.getMessage());
            }
        }
        long staticTime = System.currentTimeMillis() - startTime;

        System.out.println("\n性能对比结果:");
        System.out.println("链式 API 总耗时: " + chainTime + " ms");
        System.out.println("静态 API 总耗时: " + staticTime + " ms");
        System.out.println("性能差异: " + Math.abs(chainTime - staticTime) + " ms");
    }
}
