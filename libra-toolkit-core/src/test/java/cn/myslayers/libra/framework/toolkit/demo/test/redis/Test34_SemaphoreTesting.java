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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis 信号量功能测试
 *
 * <p>测试 RedisUtil 中的信号量功能，包括：
 * <ul>
 *   <li>信号量的获取和释放</li>
 *   <li>许可证数量控制</li>
 *   <li>并发访问控制验证</li>
 *   <li>超时获取测试</li>
 *   <li>资源池模拟场景</li>
 * </ul>
 */
public class Test34_SemaphoreTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 信号量功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试基础信号量功能")
    void testBasicSemaphore() {
        System.out.println("\n--- 测试基础信号量功能 ---");

        String semaphoreKey = "test:semaphore:basic";
        int permits = 3;

        // 测试链式 API
        System.out.println("测试链式 API 信号量:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 设置许可证数量
            accessor.SemaphoreOps.trySetPermits(semaphoreKey, permits);
            System.out.println("设置信号量许可证数量: " + permits);

            // 注意：Redisson 3.17.7 的 RSemaphore 没有 availablePermits 方法
            System.out.println("设置许可证数量完成");

            // 尝试获取许可证
            boolean acquired = accessor.SemaphoreOps.tryAcquire(semaphoreKey);
            System.out.println("获取许可证结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取许可证，执行业务逻辑...");
                System.out.println("许可证获取成功");

                Thread.sleep(100); // 模拟业务处理

                // 释放许可证
                accessor.SemaphoreOps.release(semaphoreKey);
                System.out.println("释放许可证完成");

                System.out.println("许可证释放完成");
            }
        } catch (Exception e) {
            System.out.println("信号量测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API
        System.out.println("\n测试顶层静态 API 信号量:");
        try {
            RedissonUtil.SemaphoreOps.trySetPermits(semaphoreKey + "_static", permits);

            boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey + "_static", 5, TimeUnit.SECONDS);
            System.out.println("获取许可证结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取许可证，执行业务逻辑...");
                Thread.sleep(100);
                RedissonUtil.SemaphoreOps.release(semaphoreKey + "_static");
                System.out.println("释放许可证完成");
            }
        } catch (Exception e) {
            System.out.println("信号量测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试多许可证获取")
    void testMultiplePermits() {
        System.out.println("\n--- 测试多许可证获取 ---");

        String semaphoreKey = "test:semaphore:multiple";
        int totalPermits = 10;
        int acquireCount = 3;

        try {
            // 设置信号量许可证数量
            RedissonUtil.SemaphoreOps.trySetPermits(semaphoreKey, totalPermits);
            System.out.println("设置信号量总许可证数量: " + totalPermits);

            System.out.println("信号量设置完成");

            // 一次性获取多个许可证
            boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey, acquireCount);
            System.out.println("尝试获取 " + acquireCount + " 个许可证结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取 " + acquireCount + " 个许可证");

                // 模拟业务处理
                Thread.sleep(200);

                // 释放多个许可证
                RedissonUtil.SemaphoreOps.release(semaphoreKey, acquireCount);
                System.out.println("释放 " + acquireCount + " 个许可证完成");

                System.out.println("许可证释放完成");
            }

            // 测试获取超过可用数量的许可证
            System.out.println("\n测试获取超过可用数量的许可证:");
            boolean overAcquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey, totalPermits + 1);
            System.out.println("尝试获取 " + (totalPermits + 1) + " 个许可证结果: " + overAcquired);

        } catch (Exception e) {
            System.out.println("多许可证测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试并发访问控制")
    void testConcurrentAccessControl() {
        System.out.println("\n--- 测试并发访问控制 ---");

        String semaphoreKey = "test:semaphore:concurrent";
        String counterKey = "test:semaphore:active_count";
        int maxConcurrent = 3; // 最大并发数
        int threadCount = 8;   // 总线程数

        // 初始化信号量和计数器
        RedissonUtil.SemaphoreOps.trySetPermits(semaphoreKey, maxConcurrent);
        RedissonUtil.StringOps.set(counterKey, "0");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger maxObservedConcurrent = new AtomicInteger(0);

        System.out.println("启动 " + threadCount + " 个线程，最大并发限制: " + maxConcurrent);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备就绪

                    System.out.println("线程 " + threadId + " 尝试获取信号量许可证...");
                    boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey, 10, TimeUnit.SECONDS);

                    if (acquired) {
                        // 增加活跃计数
                        int current = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(current + 1));
                        int newCount = current + 1;

                        maxObservedConcurrent.updateAndGet(max -> Math.max(max, newCount));

                        System.out.println("线程 " + threadId + " 获取许可证成功，当前活跃数: " + newCount);

                        // 模拟业务处理
                        Thread.sleep(500);

                        // 减少活跃计数
                        current = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(current - 1));

                        RedissonUtil.SemaphoreOps.release(semaphoreKey);
                        System.out.println("线程 " + threadId + " 释放许可证，当前活跃数: " + (current - 1));
                    } else {
                        System.out.println("线程 " + threadId + " 获取许可证失败（超时）");
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
            boolean finished = endLatch.await(60, TimeUnit.SECONDS);
            System.out.println("并发控制测试完成: " + finished);
            System.out.println("观察到的最大并发数: " + maxObservedConcurrent.get());
            System.out.println("验证：最大并发数 <= " + maxConcurrent + " ? " + (maxObservedConcurrent.get() <= maxConcurrent));

        } catch (InterruptedException e) {
            System.out.println("并发控制测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试资源池模拟场景")
    void testResourcePoolSimulation() {
        System.out.println("\n--- 测试资源池模拟场景 ---");

        String poolKey = "test:semaphore:pool";
        String resourceKey = "test:semaphore:resource";
        int poolSize = 5;
        int clientCount = 10;

        // 初始化资源池
        RedissonUtil.SemaphoreOps.trySetPermits(poolKey, poolSize);
        System.out.println("初始化资源池，大小: " + poolSize);

        // 初始化资源使用计数
        for (int i = 1; i <= poolSize; i++) {
            RedissonUtil.StringOps.set(resourceKey + "_" + i, "available");
        }

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(clientCount);

        System.out.println("启动 " + clientCount + " 个客户端模拟资源池使用:");

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    System.out.println("客户端 " + clientId + " 请求资源...");
                    boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire(poolKey, 8, TimeUnit.SECONDS);

                    if (acquired) {
                        System.out.println("客户端 " + clientId + " 获取资源成功");

                        // 查找可用资源
                        int resourceId = -1;
                        for (int j = 1; j <= poolSize; j++) {
                            String status = RedissonUtil.StringOps.get(resourceKey + "_" + j);
                            if ("available".equals(status)) {
                                RedissonUtil.StringOps.set(resourceKey + "_" + j, "used_by_client_" + clientId);
                                resourceId = j;
                                break;
                            }
                        }

                        if (resourceId > 0) {
                            System.out.println("客户端 " + clientId + " 使用资源 " + resourceId);

                            // 模拟资源使用
                            Thread.sleep(300 + (int)(Math.random() * 200));

                            // 释放资源
                            RedissonUtil.StringOps.set(resourceKey + "_" + resourceId, "available");
                            System.out.println("客户端 " + clientId + " 释放资源 " + resourceId);
                        }

                        RedissonUtil.SemaphoreOps.release(poolKey);
                        System.out.println("客户端 " + clientId + " 归还资源池许可证");
                    } else {
                        System.out.println("客户端 " + clientId + " 获取资源失败（资源池已满）");
                    }

                } catch (Exception e) {
                    System.out.println("客户端 " + clientId + " 异常: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        try {
            startLatch.countDown(); // 启动所有客户端
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            System.out.println("资源池模拟测试完成: " + finished);

            // 检查资源状态
            System.out.println("\n最终资源状态:");
            for (int i = 1; i <= poolSize; i++) {
                String status = RedissonUtil.StringOps.get(resourceKey + "_" + i);
                System.out.println("资源 " + i + ": " + status);
            }

            System.out.println("资源池测试完成");

        } catch (InterruptedException e) {
            System.out.println("资源池模拟测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试信号量超时和阻塞行为")
    void testSemaphoreTimeoutAndBlocking() {
        System.out.println("\n--- 测试信号量超时和阻塞行为 ---");

        String semaphoreKey = "test:semaphore:timeout";
        int permits = 1;

        try {
            // 设置只有1个许可证的信号量
            RedissonUtil.SemaphoreOps.trySetPermits(semaphoreKey, permits);
            System.out.println("设置信号量许可证数量: " + permits);

            // 第一个线程获取许可证并持有
            System.out.println("主线程获取许可证...");
            boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey);
            System.out.println("主线程获取许可证结果: " + acquired);

            if (acquired) {
                // 启动另一个线程尝试获取许可证（应该超时）
                ExecutorService executor = Executors.newSingleThreadExecutor();
                CountDownLatch testLatch = new CountDownLatch(1);

                executor.submit(() -> {
                    try {
                        System.out.println("子线程尝试获取许可证（2秒超时）...");
                        long startTime = System.currentTimeMillis();

                        boolean subAcquired = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey, 2, TimeUnit.SECONDS);

                        long elapsed = System.currentTimeMillis() - startTime;
                        System.out.println("子线程获取结果: " + subAcquired + "，耗时: " + elapsed + " ms");

                        if (!subAcquired) {
                            System.out.println("验证成功：子线程因超时未能获取许可证");
                        }

                    } catch (Exception e) {
                        System.out.println("子线程异常: " + e.getMessage());
                    } finally {
                        testLatch.countDown();
                    }
                });

                // 等待子线程完成超时测试
                testLatch.await(5, TimeUnit.SECONDS);

                // 主线程释放许可证
                RedissonUtil.SemaphoreOps.release(semaphoreKey);
                System.out.println("主线程释放许可证");

                // 验证许可证已释放
                boolean canAcquireAgain = RedissonUtil.SemaphoreOps.tryAcquire(semaphoreKey);
                System.out.println("验证许可证释放: " + canAcquireAgain);

                if (canAcquireAgain) {
                    RedissonUtil.SemaphoreOps.release(semaphoreKey);
                }

                executor.shutdown();
            }

        } catch (Exception e) {
            System.out.println("超时测试异常: " + e.getMessage());
        }
    }
}
