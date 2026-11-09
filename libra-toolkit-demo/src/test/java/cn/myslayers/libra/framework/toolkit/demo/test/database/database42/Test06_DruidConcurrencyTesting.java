package cn.myslayers.libra.framework.toolkit.demo.test.database.database42;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil42;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// 测试 Druid 连接池的并发性能与稳定性
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 并发安全测试")
class Test06_DruidConcurrencyTesting {

    private static final int THREAD_COUNT = 20; // 增加并发线程数
    private static ExecutorService executor;

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil42.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试套件开始：并发安全 ====================");
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        // 清理并预注册数据源
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        DatabaseUtil42.DataSourceCore.register("master", "jdbc:h2:mem:druid_master;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        DatabaseUtil42.DataSourceCore.register("second", "jdbc:h2:mem:druid_second;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        executor.shutdown();
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试套件结束：并发安全 ====================\n");
    }

    // 测试多个线程同时从Druid连接池获取连接
    @Test
    @Order(1)
    @DisplayName("1. 多线程并发获取连接")
    void testConcurrentGetConnection() throws InterruptedException {
        System.out.println("--- 测试 1：多线程并发获取连接 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try (Connection conn = DatabaseUtil42.DataSourceCore.getConnection("master")) {
                    if (conn != null && !conn.isClosed()) {
                        successCount.incrementAndGet();
                        // System.out.println(Thread.currentThread().getName() + " 成功获取连接。");
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

        latch.await(15, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 成功次数: " + successCount.get() + ", 失败次数: " + failureCount.get());
        if (failureCount.get() == 0 && successCount.get() == THREAD_COUNT) {
            System.out.println("成功：所有线程均成功从Druid连接池获取到连接。");
        } else {
            System.out.println("失败：部分线程未能成功获取连接。");
        }
    }

    // 测试多线程并发使用线程级数据源上下文
    @Test
    @Order(2)
    @DisplayName("2. 多线程并发使用线程上下文")
    void testConcurrentThreadContext() throws InterruptedException {
        System.out.println("--- 测试 2：多线程并发使用线程上下文 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String dbName = (i % 2 == 0) ? "master" : "second";
            executor.submit(() -> {
                try {
                    DatabaseUtil42.DataSourceCore.setThreadDataSourceContext(dbName);
                    String currentDb = DatabaseUtil42.DataSourceCore.getCurrentDataSourceName();

                    if (!dbName.equals(currentDb)) {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    DatabaseUtil42.DataSourceCore.clearThreadDataSourceContext();
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 线程上下文错误次数: " + errorCount.get());
        if (errorCount.get() == 0) {
            System.out.println("成功：所有线程均能正确、独立地使用线程级数据源上下文。");
        } else {
            System.out.println("失败：在并发环境下，线程级数据源上下文存在问题。");
        }
    }

    // 模拟读写锁场景：一个线程注册/关闭，多个线程获取连接
    @Test
    @Order(3)
    @DisplayName("3. 读写锁并发测试")
    void testReadWriteLocking() throws InterruptedException {
        System.out.println("--- 测试 3：读写锁并发测试 ---");
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT + 1);
        AtomicInteger readFailures = new AtomicInteger(0);

        // 启动多个读线程
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        try (Connection conn = DatabaseUtil42.DataSourceCore.getConnection("master")) {
                            if (conn == null) readFailures.incrementAndGet();
                        }
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    readFailures.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 启动一个写线程
        executor.submit(() -> {
            try {
                Thread.sleep(20);
                System.out.println("写线程：开始注册新数据源 'h2_concurrent'...");
                DatabaseUtil42.DataSourceCore.register("h2_concurrent", "jdbc:h2:mem:concurrent;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
                System.out.println("写线程：注册完成。");
                Thread.sleep(20);
                System.out.println("写线程：开始关闭数据源 'h2_concurrent'...");
                DatabaseUtil42.DataSourceCore.closeDatabase("h2_concurrent");
                System.out.println("写线程：关闭完成。");
            } catch (Exception e) {
                System.out.println("写线程异常: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await(20, TimeUnit.SECONDS);
        System.out.println("测试结果 -> 读操作失败次数: " + readFailures.get());
        if (readFailures.get() == 0) {
            System.out.println("成功：读写锁机制表现正常，读写操作在并发下未发生严重冲突。");
        } else {
            System.out.println("失败：读写锁机制可能存在问题，读操作在写操作期间失败。");
        }
    }
}
