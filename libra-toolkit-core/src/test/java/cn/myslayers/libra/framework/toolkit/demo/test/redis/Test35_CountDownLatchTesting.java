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
 * Redis 倒计时器功能测试
 *
 * <p>测试 RedisUtil 中的倒计时器功能，包括：
 * <ul>
 *   <li>倒计时器的创建和初始化</li>
 *   <li>计数递减和等待机制</li>
 *   <li>多线程协调场景</li>
 *   <li>超时等待测试</li>
 *   <li>分布式任务同步场景</li>
 * </ul>
 */
public class Test35_CountDownLatchTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 倒计时器功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试基础倒计时器功能")
    void testBasicCountDownLatch() {
        System.out.println("\n--- 测试基础倒计时器功能 ---");

        String latchKey = "test:latch:basic";
        int initialCount = 3;

        // 测试链式 API
        System.out.println("测试链式 API 倒计时器:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 尝试设置倒计时器
            boolean set = accessor.CountDownLatchOps.trySetCount(latchKey, initialCount);
            System.out.println("设置倒计时器初始值 " + initialCount + " 结果: " + set);

            if (set) {
                // 获取当前计数
                long currentCount = accessor.CountDownLatchOps.getCount(latchKey);
                System.out.println("当前倒计时器计数: " + currentCount);

                // 递减计数
                for (int i = 0; i < initialCount; i++) {
                    accessor.CountDownLatchOps.countDown(latchKey);
                    currentCount = accessor.CountDownLatchOps.getCount(latchKey);
                    System.out.println("递减后计数: " + currentCount);
                }

                System.out.println("倒计时器已归零");
            }
        } catch (Exception e) {
            System.out.println("倒计时器测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API
        System.out.println("\n测试顶层静态 API 倒计时器:");
        try {
            String staticKey = latchKey + "_static";
            boolean set = RedissonUtil.CountDownLatchOps.trySetCount(staticKey, initialCount);
            System.out.println("设置倒计时器初始值 " + initialCount + " 结果: " + set);

            if (set) {
                long currentCount = RedissonUtil.CountDownLatchOps.getCount(staticKey);
                System.out.println("当前倒计时器计数: " + currentCount);

                // 递减多次
                RedissonUtil.CountDownLatchOps.countDown(staticKey);
                RedissonUtil.CountDownLatchOps.countDown(staticKey);
                currentCount = RedissonUtil.CountDownLatchOps.getCount(staticKey);
                System.out.println("递减2次后计数: " + currentCount);

                RedissonUtil.CountDownLatchOps.countDown(staticKey);
                currentCount = RedissonUtil.CountDownLatchOps.getCount(staticKey);
                System.out.println("最终计数: " + currentCount);
            }
        } catch (Exception e) {
            System.out.println("倒计时器测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试等待机制")
    void testAwaitMechanism() {
        System.out.println("\n--- 测试等待机制 ---");

        String latchKey = "test:latch:await";
        int workerCount = 3;

        try {
            // 设置倒计时器
            RedissonUtil.CountDownLatchOps.trySetCount(latchKey, workerCount);
            System.out.println("设置倒计时器，等待 " + workerCount + " 个工作线程完成");

            ExecutorService executor = Executors.newFixedThreadPool(workerCount + 1);
            CountDownLatch testLatch = new CountDownLatch(1);

            // 主等待线程
            executor.submit(() -> {
                try {
                    System.out.println("主线程开始等待倒计时器归零...");
                    long startTime = System.currentTimeMillis();

                    boolean completed = RedissonUtil.CountDownLatchOps.await(latchKey, 10, TimeUnit.SECONDS);

                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("等待结果: " + completed + "，耗时: " + elapsed + " ms");

                    if (completed) {
                        System.out.println("所有工作线程已完成，主线程继续执行");
                    }

                } catch (Exception e) {
                    System.out.println("主等待线程异常: " + e.getMessage());
                } finally {
                    testLatch.countDown();
                }
            });

            // 等待主线程开始等待
            Thread.sleep(100);

            // 工作线程
            for (int i = 0; i < workerCount; i++) {
                final int workerId = i + 1;
                executor.submit(() -> {
                    try {
                        System.out.println("工作线程 " + workerId + " 开始执行任务...");

                        // 模拟工作
                        Thread.sleep(500 + workerId * 200);

                        System.out.println("工作线程 " + workerId + " 完成任务，递减计数器");
                        RedissonUtil.CountDownLatchOps.countDown(latchKey);

                        long remaining = RedissonUtil.CountDownLatchOps.getCount(latchKey);
                        System.out.println("工作线程 " + workerId + " 完成，剩余计数: " + remaining);

                    } catch (Exception e) {
                        System.out.println("工作线程 " + workerId + " 异常: " + e.getMessage());
                    }
                });
            }

            // 等待测试完成
            boolean finished = testLatch.await(15, TimeUnit.SECONDS);
            System.out.println("等待机制测试完成: " + finished);

            executor.shutdown();

        } catch (Exception e) {
            System.out.println("等待机制测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试多阶段任务协调")
    void testMultiPhaseTaskCoordination() {
        System.out.println("\n--- 测试多阶段任务协调 ---");

        String phase1Key = "test:latch:phase1";
        String phase2Key = "test:latch:phase2";
        String resultKey = "test:latch:results";
        int taskCount = 4;

        try {
            // 初始化两个阶段的倒计时器
            RedissonUtil.CountDownLatchOps.trySetCount(phase1Key, taskCount);
            RedissonUtil.CountDownLatchOps.trySetCount(phase2Key, taskCount);
            RedissonUtil.StringOps.set(resultKey, "");

            System.out.println("初始化多阶段任务协调，任务数: " + taskCount);

            ExecutorService executor = Executors.newFixedThreadPool(taskCount + 1);
            CountDownLatch testLatch = new CountDownLatch(1);

            // 协调器线程
            executor.submit(() -> {
                try {
                    System.out.println("协调器：等待第一阶段完成...");
                    boolean phase1Done = RedissonUtil.CountDownLatchOps.await(phase1Key, 10, TimeUnit.SECONDS);

                    if (phase1Done) {
                        System.out.println("协调器：第一阶段完成，开始第二阶段处理");
                        String phase1Results = RedissonUtil.StringOps.get(resultKey);
                        System.out.println("协调器：第一阶段结果: " + phase1Results);

                        // 为第二阶段准备数据
                        RedissonUtil.StringOps.set(resultKey + "_phase2", "Phase2_Ready");

                        System.out.println("协调器：等待第二阶段完成...");
                        boolean phase2Done = RedissonUtil.CountDownLatchOps.await(phase2Key, 10, TimeUnit.SECONDS);

                        if (phase2Done) {
                            System.out.println("协调器：所有阶段完成，任务协调成功");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("协调器异常: " + e.getMessage());
                } finally {
                    testLatch.countDown();
                }
            });

            // 任务线程
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i + 1;
                executor.submit(() -> {
                    try {
                        // 第一阶段
                        System.out.println("任务 " + taskId + " 执行第一阶段...");
                        Thread.sleep(200 + taskId * 100);

                        String currentResults = RedissonUtil.StringOps.get(resultKey);
                        RedissonUtil.StringOps.set(resultKey, currentResults + "T" + taskId + "_P1,");

                        System.out.println("任务 " + taskId + " 完成第一阶段");
                        RedissonUtil.CountDownLatchOps.countDown(phase1Key);

                        // 等待协调器准备第二阶段
                        while (RedissonUtil.StringOps.get(resultKey + "_phase2") == null) {
                            Thread.sleep(50);
                        }

                        // 第二阶段
                        System.out.println("任务 " + taskId + " 执行第二阶段...");
                        Thread.sleep(150 + taskId * 50);

                        System.out.println("任务 " + taskId + " 完成第二阶段");
                        RedissonUtil.CountDownLatchOps.countDown(phase2Key);

                    } catch (Exception e) {
                        System.out.println("任务 " + taskId + " 异常: " + e.getMessage());
                    }
                });
            }

            // 等待测试完成
            boolean finished = testLatch.await(30, TimeUnit.SECONDS);
            System.out.println("多阶段任务协调测试完成: " + finished);

            String finalResults = RedissonUtil.StringOps.get(resultKey);
            System.out.println("最终结果: " + finalResults);

            executor.shutdown();

        } catch (Exception e) {
            System.out.println("多阶段任务协调测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试超时等待")
    void testTimeoutAwait() {
        System.out.println("\n--- 测试超时等待 ---");

        String latchKey = "test:latch:timeout";
        int initialCount = 5;

        try {
            // 设置倒计时器
            RedissonUtil.CountDownLatchOps.trySetCount(latchKey, initialCount);
            System.out.println("设置倒计时器初始值: " + initialCount);

            // 测试短超时等待（应该超时）
            System.out.println("测试短超时等待（1秒）...");
            long startTime = System.currentTimeMillis();

            boolean completed = RedissonUtil.CountDownLatchOps.await(latchKey, 1, TimeUnit.SECONDS);

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("等待结果: " + completed + "，耗时: " + elapsed + " ms");

            if (!completed) {
                System.out.println("验证成功：等待超时");
            }

            // 部分递减计数
            System.out.println("\n部分递减计数器...");
            RedissonUtil.CountDownLatchOps.countDown(latchKey);
            RedissonUtil.CountDownLatchOps.countDown(latchKey);
            RedissonUtil.CountDownLatchOps.countDown(latchKey);
            long remaining = RedissonUtil.CountDownLatchOps.getCount(latchKey);
            System.out.println("递减3次后剩余计数: " + remaining);

            // 测试较长超时等待
            System.out.println("测试较长超时等待（3秒）...");
            ExecutorService executor = Executors.newSingleThreadExecutor();

            // 启动线程在1秒后完成剩余计数
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("后台线程完成剩余计数");
                    RedissonUtil.CountDownLatchOps.countDown(latchKey);
                    RedissonUtil.CountDownLatchOps.countDown(latchKey);
                } catch (Exception e) {
                    System.out.println("后台线程异常: " + e.getMessage());
                }
            });

            startTime = System.currentTimeMillis();
            completed = RedissonUtil.CountDownLatchOps.await(latchKey, 3, TimeUnit.SECONDS);
            elapsed = System.currentTimeMillis() - startTime;

            System.out.println("等待结果: " + completed + "，耗时: " + elapsed + " ms");

            if (completed) {
                System.out.println("验证成功：在超时前完成等待");
            }

            executor.shutdown();

        } catch (Exception e) {
            System.out.println("超时等待测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试分布式服务启动协调")
    void testDistributedServiceStartup() {
        System.out.println("\n--- 测试分布式服务启动协调 ---");

        String startupLatchKey = "test:latch:startup";
        String readyLatchKey = "test:latch:ready";
        String statusKey = "test:latch:service_status";
        int serviceCount = 3;

        try {
            // 初始化启动协调器
            RedissonUtil.CountDownLatchOps.trySetCount(startupLatchKey, serviceCount);
            RedissonUtil.CountDownLatchOps.trySetCount(readyLatchKey, 1); // 等待所有服务就绪信号

            System.out.println("模拟 " + serviceCount + " 个分布式服务启动协调");

            ExecutorService executor = Executors.newFixedThreadPool(serviceCount + 1);
            CountDownLatch testLatch = new CountDownLatch(1);
            AtomicInteger readyServices = new AtomicInteger(0);

            // 服务协调器
            executor.submit(() -> {
                try {
                    System.out.println("服务协调器：等待所有服务启动完成...");
                    boolean allStarted = RedissonUtil.CountDownLatchOps.await(startupLatchKey, 15, TimeUnit.SECONDS);

                    if (allStarted) {
                        System.out.println("服务协调器：所有服务启动完成，发送就绪信号");
                        RedissonUtil.StringOps.set(statusKey, "ALL_SERVICES_READY");
                        RedissonUtil.CountDownLatchOps.countDown(readyLatchKey);

                        System.out.println("服务协调器：系统启动完成，可以开始处理请求");
                    } else {
                        System.out.println("服务协调器：服务启动超时");
                    }

                } catch (Exception e) {
                    System.out.println("服务协调器异常: " + e.getMessage());
                } finally {
                    testLatch.countDown();
                }
            });

            // 模拟服务实例
            for (int i = 0; i < serviceCount; i++) {
                final int serviceId = i + 1;
                executor.submit(() -> {
                    try {
                        System.out.println("服务 " + serviceId + " 开始启动...");

                        // 模拟服务启动时间
                        Thread.sleep(500 + serviceId * 300);

                        System.out.println("服务 " + serviceId + " 启动完成，通知协调器");
                        RedissonUtil.CountDownLatchOps.countDown(startupLatchKey);

                        // 等待所有服务就绪
                        System.out.println("服务 " + serviceId + " 等待系统就绪信号...");
                        boolean systemReady = RedissonUtil.CountDownLatchOps.await(readyLatchKey, 10, TimeUnit.SECONDS);

                        if (systemReady) {
                            readyServices.incrementAndGet();
                            System.out.println("服务 " + serviceId + " 收到就绪信号，开始提供服务");
                        } else {
                            System.out.println("服务 " + serviceId + " 等待就绪信号超时");
                        }

                    } catch (Exception e) {
                        System.out.println("服务 " + serviceId + " 异常: " + e.getMessage());
                    }
                });
            }

            // 等待测试完成
            boolean finished = testLatch.await(30, TimeUnit.SECONDS);
            System.out.println("分布式服务启动协调测试完成: " + finished);

            String finalStatus = RedissonUtil.StringOps.get(statusKey);
            System.out.println("最终系统状态: " + finalStatus);
            System.out.println("就绪服务数量: " + readyServices.get() + "/" + serviceCount);

            executor.shutdown();

        } catch (Exception e) {
            System.out.println("分布式服务启动协调测试异常: " + e.getMessage());
        }
    }
}
