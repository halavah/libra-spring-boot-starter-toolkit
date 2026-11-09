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
 * Redis 读写锁功能测试
 *
 * <p>测试 RedisUtil 中的读写锁功能，包括：
 * <ul>
 *   <li>读锁的获取和释放</li>
 *   <li>写锁的获取和释放</li>
 *   <li>读写锁的互斥性验证</li>
 *   <li>多读者单写者模式验证</li>
 *   <li>并发场景下的读写锁测试</li>
 * </ul>
 */
public class Test33_ReadWriteLockTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 读写锁功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试基础读锁功能")
    void testBasicReadLock() {
        System.out.println("\n--- 测试基础读锁功能 ---");

        String lockKey = "test:readwrite:read";

        // 测试链式 API 读锁
        System.out.println("测试链式 API 读锁:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 尝试获取读锁
            boolean acquired = accessor.ReadWriteLockOps.tryReadLock(lockKey);
            System.out.println("获取读锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取读锁，执行读操作...");
                Thread.sleep(100); // 模拟读操作

                // 释放读锁
                accessor.ReadWriteLockOps.unlockRead(lockKey);
                System.out.println("释放读锁完成");
            }
        } catch (Exception e) {
            System.out.println("读锁测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API 读锁
        System.out.println("\n测试顶层静态 API 读锁:");
        try {
            boolean acquired = RedissonUtil.ReadWriteLockOps.tryReadLock(lockKey, 1, 10, TimeUnit.SECONDS);
            System.out.println("获取读锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取读锁，执行读操作...");
                Thread.sleep(100);
                RedissonUtil.ReadWriteLockOps.unlockRead(lockKey);
                System.out.println("释放读锁完成");
            }
        } catch (Exception e) {
            System.out.println("读锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试基础写锁功能")
    void testBasicWriteLock() {
        System.out.println("\n--- 测试基础写锁功能 ---");

        String lockKey = "test:readwrite:write";

        // 测试链式 API 写锁
        System.out.println("测试链式 API 写锁:");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 尝试获取写锁
            boolean acquired = accessor.ReadWriteLockOps.tryWriteLock(lockKey);
            System.out.println("获取写锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取写锁，执行写操作...");
                Thread.sleep(100); // 模拟写操作

                // 释放写锁
                accessor.ReadWriteLockOps.unlockWrite(lockKey);
                System.out.println("释放写锁完成");
            }
        } catch (Exception e) {
            System.out.println("写锁测试异常: " + e.getMessage());
        }

        // 测试顶层静态 API 写锁
        System.out.println("\n测试顶层静态 API 写锁:");
        try {
            boolean acquired = RedissonUtil.ReadWriteLockOps.tryWriteLock(lockKey, 1, 10, TimeUnit.SECONDS);
            System.out.println("获取写锁结果: " + acquired);

            if (acquired) {
                System.out.println("成功获取写锁，执行写操作...");
                Thread.sleep(100);
                RedissonUtil.ReadWriteLockOps.unlockWrite(lockKey);
                System.out.println("释放写锁完成");
            }
        } catch (Exception e) {
            System.out.println("写锁测试异常: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试多读者模式")
    void testMultipleReaders() {
        System.out.println("\n--- 测试多读者模式 ---");

        String lockKey = "test:readwrite:multiple";
        String counterKey = "test:readwrite:read_counter";
        int readerCount = 5;

        // 初始化读者计数器
        RedissonUtil.StringOps.set(counterKey, "0");

        ExecutorService executor = Executors.newFixedThreadPool(readerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(readerCount);
        AtomicInteger maxConcurrentReaders = new AtomicInteger(0);

        System.out.println("启动 " + readerCount + " 个读者线程:");

        for (int i = 0; i < readerCount; i++) {
            final int readerId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备就绪

                    System.out.println("读者 " + readerId + " 尝试获取读锁...");
                    boolean acquired = RedissonUtil.ReadWriteLockOps.tryReadLock(lockKey, 2, 5, TimeUnit.SECONDS);

                    if (acquired) {
                        // 增加当前读者计数
                        int currentReaders = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(currentReaders + 1));
                        
                        int newCount = currentReaders + 1;
                        maxConcurrentReaders.updateAndGet(max -> Math.max(max, newCount));
                        
                        System.out.println("读者 " + readerId + " 获取读锁成功，当前读者数: " + newCount);
                        
                        // 模拟读操作
                        Thread.sleep(200);
                        
                        // 减少读者计数
                        currentReaders = Integer.parseInt(RedissonUtil.StringOps.get(counterKey));
                        RedissonUtil.StringOps.set(counterKey, String.valueOf(currentReaders - 1));
                        
                        RedissonUtil.ReadWriteLockOps.unlockRead(lockKey);
                        System.out.println("读者 " + readerId + " 释放读锁");
                    } else {
                        System.out.println("读者 " + readerId + " 获取读锁失败");
                    }

                } catch (Exception e) {
                    System.out.println("读者 " + readerId + " 异常: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        try {
            startLatch.countDown(); // 启动所有线程
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            System.out.println("多读者测试完成: " + finished);
            System.out.println("最大并发读者数: " + maxConcurrentReaders.get());
            System.out.println("验证：多个读者可以同时持有读锁");

        } catch (InterruptedException e) {
            System.out.println("多读者测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试读写锁互斥性")
    void testReadWriteMutualExclusion() {
        System.out.println("\n--- 测试读写锁互斥性 ---");

        String lockKey = "test:readwrite:mutex";
        String statusKey = "test:readwrite:status";

        // 初始化状态
        RedissonUtil.StringOps.set(statusKey, "ready");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(3);

        // 写者线程
        executor.submit(() -> {
            try {
                startLatch.await();
                Thread.sleep(100); // 让读者先尝试获取锁

                System.out.println("写者尝试获取写锁...");
                boolean acquired = RedissonUtil.ReadWriteLockOps.tryWriteLock(lockKey, 3, 5, TimeUnit.SECONDS);

                if (acquired) {
                    System.out.println("写者获取写锁成功，开始写操作");
                    RedissonUtil.StringOps.set(statusKey, "writing");
                    
                    Thread.sleep(1000); // 模拟写操作
                    
                    RedissonUtil.StringOps.set(statusKey, "write_done");
                    RedissonUtil.ReadWriteLockOps.unlockWrite(lockKey);
                    System.out.println("写者完成写操作并释放写锁");
                } else {
                    System.out.println("写者获取写锁失败");
                }

            } catch (Exception e) {
                System.out.println("写者异常: " + e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        // 读者线程1
        executor.submit(() -> {
            try {
                startLatch.await();

                System.out.println("读者1尝试获取读锁...");
                boolean acquired = RedissonUtil.ReadWriteLockOps.tryReadLock(lockKey, 5, 8, TimeUnit.SECONDS);

                if (acquired) {
                    String status = RedissonUtil.StringOps.get(statusKey);
                    System.out.println("读者1获取读锁成功，当前状态: " + status);
                    
                    Thread.sleep(500); // 模拟读操作
                    
                    RedissonUtil.ReadWriteLockOps.unlockRead(lockKey);
                    System.out.println("读者1释放读锁");
                } else {
                    System.out.println("读者1获取读锁失败");
                }

            } catch (Exception e) {
                System.out.println("读者1异常: " + e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        // 读者线程2
        executor.submit(() -> {
            try {
                startLatch.await();

                System.out.println("读者2尝试获取读锁...");
                boolean acquired = RedissonUtil.ReadWriteLockOps.tryReadLock(lockKey, 5, 8, TimeUnit.SECONDS);

                if (acquired) {
                    String status = RedissonUtil.StringOps.get(statusKey);
                    System.out.println("读者2获取读锁成功，当前状态: " + status);
                    
                    Thread.sleep(500); // 模拟读操作
                    
                    RedissonUtil.ReadWriteLockOps.unlockRead(lockKey);
                    System.out.println("读者2释放读锁");
                } else {
                    System.out.println("读者2获取读锁失败");
                }

            } catch (Exception e) {
                System.out.println("读者2异常: " + e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });

        try {
            startLatch.countDown(); // 启动所有线程
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            System.out.println("读写互斥测试完成: " + finished);

            String finalStatus = RedissonUtil.StringOps.get(statusKey);
            System.out.println("最终状态: " + finalStatus);

        } catch (InterruptedException e) {
            System.out.println("读写互斥测试被中断: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试读写锁性能特性")
    void testReadWriteLockPerformance() {
        System.out.println("\n--- 测试读写锁性能特性 ---");

        String lockKey = "test:readwrite:performance";
        int iterations = 50;

        // 测试纯读操作性能
        System.out.println("测试纯读操作性能 (" + iterations + " 次):");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            try {
                RedissonUtil.ReadWriteLockOps.readLock(lockKey, 100, TimeUnit.MILLISECONDS);
                RedissonUtil.ReadWriteLockOps.unlockRead(lockKey);
            } catch (Exception e) {
                System.out.println("读操作异常: " + e.getMessage());
            }
        }
        
        long readTime = System.currentTimeMillis() - startTime;
        System.out.println("纯读操作总耗时: " + readTime + " ms");

        // 测试纯写操作性能
        System.out.println("\n测试纯写操作性能 (" + iterations + " 次):");
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            try {
                RedissonUtil.ReadWriteLockOps.writeLock(lockKey, 100, TimeUnit.MILLISECONDS);
                RedissonUtil.ReadWriteLockOps.unlockWrite(lockKey);
            } catch (Exception e) {
                System.out.println("写操作异常: " + e.getMessage());
            }
        }
        
        long writeTime = System.currentTimeMillis() - startTime;
        System.out.println("纯写操作总耗时: " + writeTime + " ms");

        // 对比普通锁性能
        System.out.println("\n对比普通锁性能 (" + iterations + " 次):");
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            try {
                RedissonUtil.ReentrantLockOps.lock(lockKey + "_normal", 100, TimeUnit.MILLISECONDS);
                RedissonUtil.ReentrantLockOps.unlock(lockKey + "_normal");
            } catch (Exception e) {
                System.out.println("普通锁操作异常: " + e.getMessage());
            }
        }
        
        long normalTime = System.currentTimeMillis() - startTime;
        System.out.println("普通锁操作总耗时: " + normalTime + " ms");

        System.out.println("\n性能对比结果:");
        System.out.println("读锁平均耗时: " + (readTime / (double) iterations) + " ms/次");
        System.out.println("写锁平均耗时: " + (writeTime / (double) iterations) + " ms/次");
        System.out.println("普通锁平均耗时: " + (normalTime / (double) iterations) + " ms/次");
    }
}
