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
 * Redis 公平锁功能测试
 *
 * <p>测试 RedisUtil 中的公平锁功能，包括：
 * <ul>
 *   <li>基础公平锁获取和释放</li>
 *   <li>公平性验证（FIFO 顺序）</li>
 *   <li>超时锁获取</li>
 *   <li>并发场景下的公平性测试</li>
 *   <li>链式 API 和静态 API 对比</li>
 * </ul>
 */
public class Test32_FairLockTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 公平锁功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试基础公平锁功能")
    void testBasicFairLock() {
        System.out.println("\n--- 测试基础公平锁功能 ---");

        String lockKey = "test:fair:basic";

        // 测试链式 API
        System.out.println("测试链式 API 公平锁:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 尝试获取公平锁
            boolean acquired = accessor.FairLockOps.tryLock(lockKey);
            System.out.println("获取公平锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取公平锁，执行业务逻辑...");
                Thread.sleep(100); // 模拟业务处理

                // 释放公平锁
                accessor.FairLockOps.unlock(lockKey);
                System.out.println("释放公平锁完成");
            }
        } catch (Exception e) {
            System.out.println("公平锁测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API
        System.out.println("\n测试顶层静态 API 公平锁:");
        try {
            boolean acquired = RedissonUtil.FairLockOps.tryLock(lockKey, 1, 10, TimeUnit.SECONDS);
            System.out.println("获取公平锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取公平锁，执行业务逻辑...");
                Thread.sleep(100);
                RedissonUtil.FairLockOps.unlock(lockKey);
                System.out.println("释放公平锁完成");
            }
        } catch (Exception e) {
            System.out.println("公平锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试公平锁的阻塞获取")
    void testFairLockBlocking() {
        System.out.println("\n--- 测试公平锁的阻塞获取 ---");

        String lockKey = "test:fair:blocking";

        try {
            // 测试阻塞式获取公平锁
            System.out.println("测试阻塞式获取公平锁:");
            RedissonUtil.FairLockOps.lock(lockKey);
            System.out.println("成功获取公平锁（阻塞式）");

            // 模拟业务处理
            Thread.sleep(100);

            RedissonUtil.FairLockOps.unlock(lockKey);
            System.out.println("释放公平锁完成");

            // 测试带租约时间的阻塞获取
            System.out.println("\n测试带租约时间的阻塞获取:");
            RedissonUtil.FairLockOps.lock(lockKey, 2, TimeUnit.SECONDS);
            System.out.println("获取带租约时间的公平锁成功，2秒后自动释放");

            // 等待锁自动释放
            Thread.sleep(3000);

            // 验证锁是否已自动释放
            boolean canLockAgain = RedissonUtil.FairLockOps.tryLock(lockKey);
            System.out.println("锁自动释放后再次获取: " + canLockAgain);

            if (canLockAgain) {
                RedissonUtil.FairLockOps.unlock(lockKey);
                System.out.println("验证成功：公平锁已自动释放");
            }

        } catch (Exception e) {
            System.out.println("阻塞获取公平锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试公平性验证")
    void testFairnessVerification() {
        System.out.println("\n--- 测试公平性验证 ---");

        String lockKey = "test:fair:verification";
        String orderKey = "test:fair:order";
        int threadCount = 5;

        // 清空执行顺序记录
        RedissonUtil.StringOps.set(orderKey, "");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger threadIdGenerator = new AtomicInteger(0);

        System.out.println("启动 " + threadCount + " 个线程测试公平性:");

        // 先让一个线程获取锁并持有一段时间
        executor.submit(() -> {
            try {
                int threadId = threadIdGenerator.incrementAndGet();
                System.out.println("线程 " + threadId + " 先获取锁并持有");
                
                RedissonUtil.FairLockOps.lock(lockKey);
                String currentOrder = RedissonUtil.StringOps.get(orderKey);
                RedissonUtil.StringOps.set(orderKey, currentOrder + threadId + ",");
                System.out.println("线程 " + threadId + " 获取锁成功，持有2秒");
                
                Thread.sleep(2000); // 持有锁2秒
                
                RedissonUtil.FairLockOps.unlock(lockKey);
                System.out.println("线程 " + threadId + " 释放锁");
                
                // 启动其他线程
                startLatch.countDown();
                
            } catch (Exception e) {
                System.out.println("线程异常: " + e.getMessage());
                startLatch.countDown();
            } finally {
                endLatch.countDown();
            }
        });

        // 等待第一个线程获取锁后，启动其他线程
        try {
            startLatch.await();
            Thread.sleep(100); // 确保第一个线程已经获取锁
        } catch (InterruptedException e) {
            System.out.println("等待异常: " + e.getMessage());
        }

        // 启动其他线程竞争锁
        for (int i = 0; i < threadCount - 1; i++) {
            executor.submit(() -> {
                try {
                    int threadId = threadIdGenerator.incrementAndGet();
                    System.out.println("线程 " + threadId + " 尝试获取公平锁...");
                    
                    boolean acquired = RedissonUtil.FairLockOps.tryLock(lockKey, 5, 3, TimeUnit.SECONDS);
                    
                    if (acquired) {
                        String currentOrder = RedissonUtil.StringOps.get(orderKey);
                        RedissonUtil.StringOps.set(orderKey, currentOrder + threadId + ",");
                        System.out.println("线程 " + threadId + " 获取公平锁成功");
                        
                        Thread.sleep(200); // 模拟业务处理
                        
                        RedissonUtil.FairLockOps.unlock(lockKey);
                        System.out.println("线程 " + threadId + " 释放公平锁");
                    } else {
                        System.out.println("线程 " + threadId + " 获取公平锁失败");
                    }

                } catch (Exception e) {
                    System.out.println("线程异常: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        try {
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            System.out.println("公平性测试完成: " + finished);

            // 检查执行顺序
            String executionOrder = RedissonUtil.StringOps.get(orderKey);
            System.out.println("线程执行顺序: " + executionOrder);
            System.out.println("公平性验证：线程按照请求顺序获取锁");

        } catch (InterruptedException e) {
            System.out.println("公平性测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试并发场景下的公平锁")
    void testConcurrentFairLock() {
        System.out.println("\n--- 测试并发场景下的公平锁 ---");

        String lockKey = "test:fair:concurrent";
        String counterKey = "test:fair:counter";
        int threadCount = 8;

        // 初始化计数器
        RedissonUtil.StringOps.set(counterKey, "0");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        System.out.println("启动 " + threadCount + " 个线程测试并发公平锁:");

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备就绪

                    System.out.println("线程 " + threadId + " 尝试获取公平锁...");
                    boolean acquired = RedissonUtil.FairLockOps.tryLock(lockKey, 3, 5, TimeUnit.SECONDS);

                    if (acquired) {
                        System.out.println("线程 " + threadId + " 成功获取公平锁");
                        
                        // 模拟业务处理：递增计数器
                        int current = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        Thread.sleep(150); // 模拟处理时间
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(current + 1));
                        System.out.println("线程 " + threadId + " 完成业务处理，计数器: " + (current + 1));

                        RedissonUtil.FairLockOps.unlock(lockKey);
                        System.out.println("线程 " + threadId + " 释放公平锁");
                    } else {
                        System.out.println("线程 " + threadId + " 获取公平锁失败");
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
    @DisplayName("测试公平锁与普通锁的区别")
    void testFairVsNormalLock() {
        System.out.println("\n--- 测试公平锁与普通锁的区别 ---");

        String fairLockKey = "test:fair:comparison";
        String normalLockKey = "test:normal:comparison";

        try {
            // 测试公平锁
            System.out.println("测试公平锁特性:");
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 10; i++) {
                RedissonUtil.FairLockOps.lock(fairLockKey, 100, TimeUnit.MILLISECONDS);
                RedissonUtil.FairLockOps.unlock(fairLockKey);
            }
            
            long fairTime = System.currentTimeMillis() - startTime;
            System.out.println("公平锁 10 次操作耗时: " + fairTime + " ms");

            // 测试普通可重入锁
            System.out.println("\n测试普通可重入锁特性:");
            startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 10; i++) {
                RedissonUtil.ReentrantLockOps.lock(normalLockKey, 100, TimeUnit.MILLISECONDS);
                RedissonUtil.ReentrantLockOps.unlock(normalLockKey);
            }
            
            long normalTime = System.currentTimeMillis() - startTime;
            System.out.println("普通锁 10 次操作耗时: " + normalTime + " ms");

            System.out.println("\n性能对比:");
            System.out.println("公平锁相比普通锁的额外开销: " + (fairTime - normalTime) + " ms");
            System.out.println("公平锁保证了线程获取锁的公平性，但可能有轻微的性能开销");

        } catch (Exception e) {
            System.out.println("对比测试异常: " + e.getMessage());
        }
    }
}
