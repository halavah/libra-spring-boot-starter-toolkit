package cn.myslayers.libra.framework.toolkit.demo.test.database.database41;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil41;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 测试 DataSourceCore 在多线程环境下的并发安全性
@DisplayName("DataSourceCore - 并发安全测试")
class Test06_ConcurrencyTesting {

    private static final int THREAD_COUNT = 10;
    private static ExecutorService executor;

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil41.LoggerCore.setDebugLogEnabled(true);
        System.out.println("==================== 测试开始：并发安全 ====================");
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        // 清理并预注册数据源
        DatabaseUtil41.DataSourceCore.closeAllDatabases();
        // 手动注册两个H2数据库用于并发测试
        DatabaseUtil41.DataSourceCore.register("h2_concurrent_1", "jdbc:h2:mem:conc1;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        DatabaseUtil41.DataSourceCore.register("h2_concurrent_2", "jdbc:h2:mem:conc2;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        executor.shutdown();
        DatabaseUtil41.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试结束：并发安全 ====================");
    }

    // 测试多个线程同时获取连接
    @Test
    @DisplayName("1. 多线程并发获取连接")
    void testConcurrentGetConnection() throws InterruptedException {
        System.out.println("\n--- 测试 1：多线程并发获取连接 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try (java.sql.Connection conn = DatabaseUtil41.DataSourceCore.getConnection("h2_concurrent_1")) {
                    if (conn != null && !conn.isClosed()) {
                        successCount.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + " 成功获取连接。");
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " 获取连接失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 成功次数: " + successCount.get() + ", 失败次数: " + failureCount.get());
        if (failureCount.get() == 0 && successCount.get() == THREAD_COUNT) {
            System.out.println("成功：所有线程均成功获取到数据库连接。");
        } else {
            System.out.println("失败：部分线程未能成功获取连接。");
        }
    }

    // 测试多线程并发设置和清理线程级数据源
    @Test
    @DisplayName("2. 多线程并发使用线程上下文")
    void testConcurrentThreadContext() throws InterruptedException {
        System.out.println("\n--- 测试 2：多线程并发使用线程上下文 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String dbName = (i % 2 == 0) ? "h2_concurrent_1" : "h2_concurrent_2";
            executor.submit(() -> {
                try {
                    // 设置线程级数据源
                    DatabaseUtil41.DataSourceCore.setThreadDataSourceContext(dbName);
                    String currentDb = DatabaseUtil41.DataSourceCore.getCurrentDataSourceName();

                    // 验证当前线程获取的数据源是否正确
                    if (!dbName.equals(currentDb)) {
                        errorCount.incrementAndGet();
                        System.out.println("错误: " + Thread.currentThread().getName() + " 期望 " + dbName + ", 实际 " + currentDb);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " 成功绑定并验证了数据源 " + dbName);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " 发生异常: " + e.getMessage());
                } finally {
                    // 清理线程上下文
                    DatabaseUtil41.DataSourceCore.clearThreadDataSourceContext();
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 错误次数: " + errorCount.get());
        if (errorCount.get() == 0) {
            System.out.println("成功：所有线程均能正确、独立地使用线程级数据源上下文。");
        } else {
            System.out.println("失败：在并发环境下，线程级数据源上下文存在问题。");
        }
    }

    // 测试一个线程写入（注册/关闭）同时多个线程读取（获取连接）
    @Test
    @DisplayName("3. 读写锁并发测试")
    void testReadWriteLocking() throws InterruptedException {
        System.out.println("\n--- 测试 3：读写锁并发测试 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT + 1);
        AtomicInteger readFailures = new AtomicInteger(0);

        // 启动多个读线程
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    // 模拟持续读取
                    for (int j = 0; j < 5; j++) {
                        try (java.sql.Connection conn = DatabaseUtil41.DataSourceCore.getConnection("h2_concurrent_1")) {
                            if (conn == null) readFailures.incrementAndGet();
                        }
                        Thread.sleep(10); // 短暂休眠
                    }
                } catch (Exception e) {
                    readFailures.incrementAndGet();
                    System.out.println("读线程异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 启动一个写线程
        executor.submit(() -> {
            try {
                Thread.sleep(20); // 确保读线程已开始
                System.out.println("写线程：开始注册新数据源 'h2_concurrent'...");
                DatabaseUtil41.DataSourceCore.register("h2_concurrent", "jdbc:h2:mem:concurrent;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
                System.out.println("写线程：注册完成。");
                Thread.sleep(20);
                System.out.println("写线程：开始关闭数据源 'h2_concurrent'...");
                DatabaseUtil41.DataSourceCore.closeDatabase("h2_concurrent");
                System.out.println("写线程：关闭完成。");
            } catch (Exception e) {
                System.out.println("写线程异常: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await(15, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 读操作失败次数: " + readFailures.get());
        if (readFailures.get() == 0) {
            System.out.println("成功：读写锁机制表现正常，读写操作在并发下未发生严重冲突。");
        } else {
            System.out.println("失败：读写锁机制可能存在问题，读操作在写操作期间失败。");
        }
    }
}
