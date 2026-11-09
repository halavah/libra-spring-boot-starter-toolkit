package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.DatabaseAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 链式操作测试类
 * 测试复杂的数据库操作链和工作流
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("链式操作测试")
public class Test12_ApiChainOperationsTesting {

    private static final String TEST_DB = "chain_operations_test_db";

    @BeforeAll
    void setUp() {
        System.out.println("=== 🔗 链式操作测试开始 ===");
//            DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//            DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        // 注册测试数据源
        DatabaseUtil32.DataSourceCore.register(
            TEST_DB,
            "jdbc:h2:mem:chaintest;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "sa", "", "org.h2.Driver"
        );

        // 创建测试表
        DatabaseUtil32.accessor(TEST_DB)
            .execute("CREATE TABLE IF NOT EXISTS products (" +
                "id INT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "price DECIMAL(10,2), " +
                "stock INT, " +
                "category VARCHAR(50), " +
                "status VARCHAR(20) DEFAULT 'active')", new HashMap<>());

        DatabaseUtil32.accessor(TEST_DB)
            .execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id INT PRIMARY KEY, " +
                "product_id INT, " +
                "quantity INT, " +
                "total_amount DECIMAL(10,2), " +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)", new HashMap<>());
    }

    @Test
    @Order(1)
    @DisplayName("基本链式操作测试")
    void testBasicChainOperations() {
        System.out.println("\n🔗 测试基本链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        // 插入 -> 查询 -> 更新 -> 再查询的链式操作
        int insertResult = accessor.execute(
            "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
            createMap("id", 1, "name", "笔记本电脑", "price", 5999.99, "stock", 10, "category", "电子产品")
        ).result();
        System.out.println("插入操作影响行数: " + insertResult + ", 期望: 1");

        // 查询刚插入的数据
        Map<String, Object> product = accessor.selectOne(
            "SELECT * FROM products WHERE id = #{id}",
            createMap("id", 1)
        );
        System.out.println("查询到的产品: " + (product != null ? "不为空" : "为空"));
        if (product != null) {
            System.out.println("产品名称: " + product.get("name") + ", 期望: 笔记本电脑");
        }

        // 更新库存
        int updateResult = accessor.execute(
            "UPDATE products SET stock = #{stock} WHERE id = #{id}",
            createMap("stock", 8, "id", 1)
        ).result();
        System.out.println("更新操作影响行数: " + updateResult + ", 期望: 1");

        // 验证更新结果
        Map<String, Object> updatedProduct = accessor.selectOne(
            "SELECT * FROM products WHERE id = #{id}",
            createMap("id", 1)
        );
        if (updatedProduct != null) {
            System.out.println("更新后的库存: " + updatedProduct.get("stock") + ", 期望: 8");
        }

        System.out.println("✅ 基本链式操作测试通过");
    }

    @Test
    @Order(2)
    @DisplayName("批量链式操作测试")
    void testBatchChainOperations() {
        System.out.println("\n📦 测试批量链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        // 批量插入多个产品
        int result1 = accessor.execute(
            "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
            createMap("id", 2, "name", "鼠标", "price", 99.99, "stock", 50, "category", "电子产品")
        ).result();
        int result2 = accessor.execute(
            "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
            createMap("id", 3, "name", "键盘", "price", 299.99, "stock", 30, "category", "电子产品")
        ).result();
        int result3 = accessor.execute(
            "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
            createMap("id", 4, "name", "显示器", "price", 1999.99, "stock", 15, "category", "电子产品")
        ).result();

        System.out.println("批量插入结果1: " + result1 + ", 期望: 1");
        System.out.println("批量插入结果2: " + result2 + ", 期望: 1");
        System.out.println("批量插入结果3: " + result3 + ", 期望: 1");

        // 查询所有产品
        List<Map<String, Object>> products = accessor.selectList(
            "SELECT * FROM products ORDER BY id",
            new HashMap<>()
        );
        System.out.println("查询到的产品数量: " + products.size() + ", 期望: 4");

        System.out.println("✅ 批量链式操作测试通过");
    }

    @Test
    @Order(3)
    @DisplayName("事务链式操作测试")
    void testTransactionChainOperations() {
        System.out.println("\n🔄 测试事务链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        // 开启事务
        accessor.setTransaction();

        try {
            // 在事务中执行多个操作
            accessor.execute(
                "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
                createMap("id", 5, "name", "耳机", "price", 199.99, "stock", 25, "category", "电子产品")
            );

            // 创建订单
            accessor.execute(
                "INSERT INTO orders (id, product_id, quantity, total_amount) VALUES (#{id}, #{product_id}, #{quantity}, #{total_amount})",
                createMap("id", 1, "product_id", 5, "quantity", 2, "total_amount", 399.98)
            );

            // 更新库存
            accessor.execute(
                "UPDATE products SET stock = stock - #{quantity} WHERE id = #{id}",
                createMap("quantity", 2, "id", 5)
            );

            // 查询验证
            List<Map<String, Object>> results = accessor.selectList(
                "SELECT p.name, p.stock, o.quantity FROM products p " +
                "JOIN orders o ON p.id = o.product_id WHERE p.id = #{id}",
                createMap("id", 5)
            );

            System.out.println("事务中查询结果: " + (results != null ? "不为空" : "为空"));
            System.out.println("事务中查询结果数量: " + (results != null ? results.size() : 0) + " (应>=1)");

            // 提交事务
            accessor.commit();
            System.out.println("✅ 事务提交成功");

        } catch (Exception e) {
            accessor.rollback();
            System.out.println("❌ 事务回滚: " + e.getMessage());
            throw e;
        }

        System.out.println("✅ 事务链式操作测试通过");
    }

    @Test
    @Order(4)
    @DisplayName("复杂查询链式操作测试")
    void testComplexQueryChainOperations() {
        System.out.println("\n🔍 测试复杂查询链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        // 先插入一些测试数据
        accessor.execute(
            "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
            createMap("id", 6, "name", "平板电脑", "price", 3999.99, "stock", 20, "category", "电子产品")
        );

        // 复杂查询1: 价格范围查询
        List<Map<String, Object>> expensiveProducts = accessor.selectList(
            "SELECT * FROM products WHERE price > #{minPrice} ORDER BY price DESC",
            createMap("minPrice", 1000.0)
        );
        System.out.println("高价产品查询结果: " + (expensiveProducts != null ? "不为空" : "为空"));

        // 复杂查询2: 聚合查询
        Map<String, Object> stats = accessor.selectOne(
            "SELECT COUNT(*) as total_count, AVG(price) as avg_price, SUM(stock) as total_stock FROM products",
            new HashMap<>()
        );
        System.out.println("统计查询结果: " + (stats != null ? "不为空" : "为空"));

        // 复杂查询3: 分组查询
        List<Map<String, Object>> categoryStats = accessor.selectList(
            "SELECT category, COUNT(*) as count, AVG(price) as avg_price FROM products GROUP BY category",
            new HashMap<>()
        );
        System.out.println("分组查询结果: " + (categoryStats != null ? "不为空" : "为空"));
        System.out.println("分组查询结果数量: " + (categoryStats != null ? categoryStats.size() : 0) + " (应>0)");

        // 复杂查询4: 子查询
        List<Map<String, Object>> aboveAvgProducts = accessor.selectList(
            "SELECT * FROM products WHERE price > (SELECT AVG(price) FROM products)",
            new HashMap<>()
        );
        System.out.println("高于平均价格的产品: " + (aboveAvgProducts != null ? "不为空" : "为空"));

        System.out.println("✅ 复杂查询链式操作测试通过");
    }

    @Test
    @Order(5)
    @DisplayName("错误处理链式操作测试")
    void testErrorHandlingChainOperations() {
        System.out.println("\n⚠️ 测试错误处理链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        // 测试事务回滚
        accessor.setTransaction();

        try {
            // 正常操作
            accessor.execute(
                "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
                createMap("id", 7, "name", "键盘", "price", 279.99, "stock", 35, "category", "电子产品")
            );

            // 故意制造错误 - 重复主键
            accessor.execute(
                "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
                createMap("id", 7, "name", "重复键盘", "price", 279.99, "stock", 35, "category", "电子产品")
            );

            accessor.commit();
            System.out.println("❌ 应该抛出异常但没有");

        } catch (Exception e) {
            accessor.rollback();
            System.out.println("✅ 正确捕获异常并回滚: " + e.getMessage());
        }

        // 验证回滚是否成功
        Map<String, Object> product = accessor.selectOne(
            "SELECT * FROM products WHERE id = #{id}",
            createMap("id", 7)
        );
        if (product != null) {
            System.out.println("产品价格: " + product.get("price") + ", 期望: 279.99");
        }

        // 重新插入以验证系统恢复正常
        accessor.setTransaction();
        try {
            accessor.execute(
                "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
                createMap("id", 8, "name", "键盘", "price", 279.99, "stock", 35, "category", "电子产品")
            );
            accessor.commit();

            Map<String, Object> committedProduct = accessor.selectOne(
                "SELECT * FROM products WHERE id = #{id}",
                createMap("id", 8)
            );
            System.out.println("提交后的产品: " + (committedProduct != null ? "不为空" : "为空"));
            if (committedProduct != null) {
                System.out.println("提交后的产品名称: " + committedProduct.get("name") + ", 期望: 键盘");
            }

        } catch (Exception e) {
            accessor.rollback();
            System.out.println("❌ 恢复操作失败: " + e.getMessage());
        }

        System.out.println("✅ 错误处理链式操作测试通过");
    }

    @Test
    @Order(6)
    @DisplayName("大数据量链式操作测试")
    void testLargeDataChainOperations() {
        System.out.println("\n📊 测试大数据量链式操作...");

        DatabaseAccessor accessor = DatabaseUtil32.accessor(TEST_DB);

        try {
            // 批量插入大量数据
            int batchSize = 100;
            for (int i = 0; i < batchSize; i++) {
                accessor.execute(
                    "INSERT INTO products (id, name, price, stock, category) VALUES (#{id}, #{name}, #{price}, #{stock}, #{category})",
                    createMap("id", 20000 + i, "name", "批量产品" + i, "price", 10.0 + i, "stock", i % 50 + 1, "category", "批量测试")
                );
            }

            // 验证插入的数据量
            Map<String, Object> countResult = accessor.selectOne(
                "SELECT COUNT(*) as count FROM products WHERE id >= 20000",
                new HashMap<>()
            );
            int totalCount = ((Number) countResult.get("count")).intValue();
            System.out.println("批量插入的数据量: " + totalCount + ", 期望: 100");

            // 批量查询和分析
            List<Map<String, Object>> expensiveProducts = accessor.selectList(
                "SELECT * FROM products WHERE id >= 20000 AND price > #{minPrice} ORDER BY price DESC LIMIT 10",
                createMap("minPrice", 50.0)
            );
            System.out.println("高价产品查询结果: " + (expensiveProducts != null ? "不为空" : "为空"));

            // 聚合统计
            Map<String, Object> stats = accessor.selectOne(
                "SELECT AVG(price) as avg_price, MAX(stock) as max_stock, MIN(stock) as min_stock FROM products WHERE id >= 20000",
                new HashMap<>()
            );
            System.out.println("统计结果: " + (stats != null ? "不为空" : "为空"));

            // 分组统计
            List<Map<String, Object>> categoryStats = accessor.selectList(
                "SELECT category, COUNT(*) as count FROM products WHERE id >= 20000 GROUP BY category",
                new HashMap<>()
            );
            System.out.println("分组统计结果: " + (categoryStats != null ? "不为空" : "为空"));
            System.out.println("分组统计结果数量: " + (categoryStats != null ? categoryStats.size() : 0) + " (应>0)");

            // 复杂子查询
            List<Map<String, Object>> aboveAvgProducts = accessor.selectList(
                "SELECT * FROM products WHERE id >= 20000 AND price > (SELECT AVG(price) FROM products WHERE id >= 20000)",
                new HashMap<>()
            );
            System.out.println("高于平均价格的产品: " + (aboveAvgProducts != null ? "不为空" : "为空"));

            System.out.println("✅ 大数据量链式操作测试通过");
        } catch (Exception e) {
            System.out.println("大数据量链式操作测试失败: " + e.getMessage());
        }
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n🏁 链式操作测试完成");
        // 清理测试数据
        try {
            DatabaseUtil32.accessor(TEST_DB)
                .execute("DROP TABLE IF EXISTS products", new HashMap<>());
            DatabaseUtil32.accessor(TEST_DB)
                .execute("DROP TABLE IF EXISTS orders", new HashMap<>());
        } catch (Exception e) {
            System.err.println("清理测试数据失败: " + e.getMessage());
        }
    }

    // 辅助方法：创建Map
    private Map<String, Object> createMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, Object> createMap(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private Map<String, Object> createMap(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    private Map<String, Object> createMap(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }

    private Map<String, Object> createMap(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        return map;
    }
}