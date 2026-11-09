package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 事务管理功能集成测试 (DatabaseAccessor for DatabaseUtil32)
 *
 * <p>
 * 该测试类全面验证 {@link DatabaseUtil32.DatabaseAccessor} 的事务管理功能。
 * 它使用H2内存数据库，通过模拟银行转账等场景，覆盖了事务的提交、回滚、影响行数获取及状态检查等核心场景。
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("事务管理功能集成测试 (DatabaseAccessor for DatabaseUtil32)")
public class Test03_TransactionTesting {

    private static final String H2_DB_TX = "h2_tx_test_db_util2";

    @BeforeAll
    void setupAll() {
        System.out.println("==================== 🎬 环境初始化开始 (Transaction for DatabaseUtil32) ====================");
//        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        DatabaseUtil32.DataSourceCore.register(H2_DB_TX, "jdbc:h2:mem:tx_test_util2;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_TX);
        System.out.println("  ✓ 默认数据源已设置为: " + H2_DB_TX);

        String createTableSql = "CREATE TABLE IF NOT EXISTS accounts (id INT PRIMARY KEY, balance DECIMAL(10, 2))";
        try {
            DatabaseUtil32.executeDDL(createTableSql);
            System.out.println("  ✓ 测试表 'accounts' 创建成功");
        } catch (Exception e) {
            throw new RuntimeException("测试环境初始化失败", e);
        }
        System.out.println("==================== ✅ 环境初始化完成 (Transaction for DatabaseUtil32) ====================\n");
    }

    @AfterAll
    void tearDownAll() {
        System.out.println("\n==================== 🎬 环境清理开始 (Transaction for DatabaseUtil32) ====================");
        try {
            DatabaseUtil32.executeDDL("DROP TABLE accounts");
            System.out.println("  ✓ 测试表 'accounts' 已删除");
        } catch (Exception e) {
            System.err.println("  ❌ 删除测试表失败: " + e.getMessage());
        }
        DatabaseUtil32.DataSourceCore.closeAllDatabases();
        System.out.println("  ✓ 所有H2数据源已关闭");
        System.out.println("==================== ✅ 环境清理完成 (Transaction for DatabaseUtil32) ====================");
    }

    @BeforeEach
    void setupEach() {
        System.out.println("--- 刷新测试数据 (DatabaseUtil32) ---");
        DatabaseUtil32.executeByMp("DELETE FROM accounts", null);
        DatabaseUtil32.executeByMp("INSERT INTO accounts (id, balance) VALUES (1, 1000.00)", null);
        DatabaseUtil32.executeByMp("INSERT INTO accounts (id, balance) VALUES (2, 500.00)", null);
        System.out.println("  ✓ 测试表 'accounts' 数据已重置 (1: 1000.00, 2: 500.00)");
    }

    @Test
    @Order(1)
    @DisplayName("成功转账：验证事务成功提交与数据一致性")
    void testSuccessfulTransfer() {
        System.out.println("\n【测试1】: 模拟成功转账100元");
        DatabaseUtil32.DatabaseAccessor accessor = DatabaseUtil32.accessor();
        try {
            accessor.setTransaction();
            System.out.println("  - 事务开启，准备从账户1向账户2转账100元...");
            accessor.executeByMp("UPDATE accounts SET balance = balance - 100 WHERE id = 1", null);
            accessor.executeByMp("UPDATE accounts SET balance = balance + 100 WHERE id = 2", null);
            int affectedRows = accessor.commit();
            System.out.println("  - 事务提交成功，累计影响行数: " + affectedRows);

            Map<String, Object> acc1 = DatabaseUtil32.selectOneByMp("SELECT balance FROM accounts WHERE id = 1", null);
            Map<String, Object> acc2 = DatabaseUtil32.selectOneByMp("SELECT balance FROM accounts WHERE id = 2", null);
            BigDecimal bal1 = (BigDecimal) acc1.get("BALANCE");
            BigDecimal bal2 = (BigDecimal) acc2.get("BALANCE");

            System.out.println("  - 验证账户1余额: 期望=900.00, 实际=" + bal1 + " -> " + (new BigDecimal("900.00").compareTo(bal1) == 0 ? "通过" : "失败"));
            System.out.println("  - 验证账户2余额: 期望=600.00, 实际=" + bal2 + " -> " + (new BigDecimal("600.00").compareTo(bal2) == 0 ? "通过" : "失败"));

        } catch (Exception e) {
            accessor.rollback();
            System.err.println("  - 测试意外失败，事务已回滚: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("失败转账：验证余额不足时事务回滚")
    void testFailedTransferAndRollback() {
        System.out.println("\n【测试2】: 模拟失败转账 (余额不足)，验证回滚");
        DatabaseUtil32.DatabaseAccessor accessor = DatabaseUtil32.accessor();
        try {
            accessor.setTransaction();
            System.out.println("  - 事务开启，准备从账户2向账户1转账600元 (余额不足)...");
            accessor.executeByMp("UPDATE accounts SET balance = balance - 600 WHERE id = 2", null);

            Map<String, Object> check = accessor.selectOneByMp("SELECT balance FROM accounts WHERE id = 2", null);
            if (((BigDecimal) check.get("BALANCE")).compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("余额不足，操作无法完成！");
            }
            accessor.executeByMp("UPDATE accounts SET balance = balance + 600 WHERE id = 1", null);
            accessor.commit(); // 此处不应被执行
        } catch (Exception e) {
            System.out.println("  - 捕获到预期异常: " + e.getMessage());
            accessor.rollback();
            System.out.println("  - 事务已回滚。");
        }

        Map<String, Object> acc1 = DatabaseUtil32.selectOneByMp("SELECT balance FROM accounts WHERE id = 1", null);
        Map<String, Object> acc2 = DatabaseUtil32.selectOneByMp("SELECT balance FROM accounts WHERE id = 2", null);
        BigDecimal bal1 = (BigDecimal) acc1.get("BALANCE");
        BigDecimal bal2 = (BigDecimal) acc2.get("BALANCE");

        System.out.println("  - 验证账户1余额: 期望=1000.00, 实际=" + bal1 + " -> " + (new BigDecimal("1000.00").compareTo(bal1) == 0 ? "通过" : "失败"));
        System.out.println("  - 验证账户2余额: 期望=500.00, 实际=" + bal2 + " -> " + (new BigDecimal("500.00").compareTo(bal2) == 0 ? "通过" : "失败"));
    }

    @Test
    @Order(3)
    @DisplayName("事务状态：验证isInTransaction()的正确性")
    void testTransactionStatus() {
        System.out.println("\n【测试3】: 验证 isInTransaction() 状态切换");
        DatabaseUtil32.DatabaseAccessor accessor = DatabaseUtil32.accessor();
        System.out.println("  - 事务开始前: " + accessor.isInTransaction());
        accessor.setTransaction();
        System.out.println("  - setTransaction后: " + accessor.isInTransaction());
        accessor.commit();
        System.out.println("  - commit后: " + accessor.isInTransaction());

        accessor.setTransaction();
        System.out.println("  - 再次setTransaction后: " + accessor.isInTransaction());
        accessor.rollback();
        System.out.println("  - rollback后: " + accessor.isInTransaction());
    }

    @Test
    @Order(4)
    @DisplayName("非事务模式：验证操作直接生效")
    void testNonTransactionMode() {
        System.out.println("\n【测试4】: 验证非事务模式下操作直接生效");
        DatabaseUtil32.DatabaseAccessor accessor = DatabaseUtil32.accessor();
        System.out.println("  - 当前是否在事务中: " + accessor.isInTransaction());
        accessor.executeByMp("UPDATE accounts SET balance = 123.45 WHERE id = 1", null);

        Map<String, Object> acc1 = DatabaseUtil32.selectOneByMp("SELECT balance FROM accounts WHERE id = 1", null);
        BigDecimal bal1 = (BigDecimal) acc1.get("BALANCE");
        System.out.println("  - 非事务模式更新后，账户1余额: " + bal1);
        System.out.println("  - 验证: " + (new BigDecimal("123.45").compareTo(bal1) == 0 ? "通过" : "失败"));
    }
    
    @Test
    @Order(5)
    @DisplayName("影响行数获取：验证result()和resultAll()的正确性")
    void testAffectedRowsResultAndResultAll() {
        System.out.println("\n【测试5】: 验证 result() 和 resultAll() 的新行为");
        DatabaseUtil32.DatabaseAccessor accessor = DatabaseUtil32.accessor();
        try {
            accessor.setTransaction();
            System.out.println("  - 事务开启...");

            // 第一次操作
            accessor.execute("UPDATE accounts SET balance = 800 WHERE id = 1", null);
            int lastOpRows1 = accessor.result();
            int totalRows1 = accessor.resultAll();
            System.out.println("  - 操作1后: result()=" + lastOpRows1 + ", resultAll()=" + totalRows1);
            System.out.println("  - 验证: result()期望=1 -> " + (lastOpRows1 == 1 ? "通过" : "失败"));
            System.out.println("  - 验证: resultAll()期望=1 -> " + (totalRows1 == 1 ? "通过" : "失败"));

            // 第二次操作
            accessor.execute("UPDATE accounts SET balance = 700 WHERE id = 2", null);
            int lastOpRows2 = accessor.result();
            int totalRows2 = accessor.resultAll();
            System.out.println("  - 操作2后: result()=" + lastOpRows2 + ", resultAll()=" + totalRows2);
            System.out.println("  - 验证: result()期望=1 -> " + (lastOpRows2 == 1 ? "通过" : "失败"));
            System.out.println("  - 验证: resultAll()期望=2 -> " + (totalRows2 == 2 ? "通过" : "失败"));

            // 第三次操作 (无影响)
            accessor.execute("UPDATE accounts SET balance = 0 WHERE id = 99", null);
            int lastOpRows3 = accessor.result();
            int totalRows3 = accessor.resultAll();
            System.out.println("  - 操作3后 (无影响): result()=" + lastOpRows3 + ", resultAll()=" + totalRows3);
            System.out.println("  - 验证: result()期望=0 -> " + (lastOpRows3 == 0 ? "通过" : "失败"));
            System.out.println("  - 验证: resultAll()期望=2 -> " + (totalRows3 == 2 ? "通过" : "失败"));

            int commitResult = accessor.commit();
            System.out.println("  - 事务提交，commit()返回: " + commitResult);
            System.out.println("  - 验证: commit()返回值期望=2 -> " + (commitResult == 2 ? "通过" : "失败"));

        } catch (Exception e) {
            accessor.rollback();
            System.err.println("  - 测试意外失败，事务已回滚: " + e.getMessage());
        }
    }
}
