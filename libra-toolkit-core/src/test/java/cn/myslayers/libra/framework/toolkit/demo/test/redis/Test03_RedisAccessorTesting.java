package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil (v4) 功能测试")
public class Test03_RedisAccessorTesting {

    private static final String TEST_KEY_STATIC = "test:static_api";
    private static final String TEST_KEY_ACCESSOR = "test:accessor_api";
    private static final String TEST_KEY_DB1 = "test:db1_api";
    private static final String TEST_KEY_TRANSACTION = "test:transaction";
    private static final String TEST_KEY_LOCK = "test:lock";

    @BeforeAll
    static void setUp() {
        System.out.println("====== RedisUtil 测试开始 ======");
        // 开启调试日志，方便观察
        RedissonUtil.setDebugLogEnabled(true);
        // 清理可能存在的旧数据
        RedissonUtil.KeyOps.delete(TEST_KEY_STATIC);
        RedissonUtil.KeyOps.delete(TEST_KEY_ACCESSOR);
        RedissonUtil.KeyOps.delete(TEST_KEY_TRANSACTION);
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor("db1_for_test")) {
            accessor.KeyOps.delete(TEST_KEY_DB1);
        }
    }

    @AfterAll
    static void tearDown() {
        // 清理测试数据
        System.out.println("清理测试数据...");
        RedissonUtil.KeyOps.delete(TEST_KEY_STATIC);
        RedissonUtil.KeyOps.delete(TEST_KEY_ACCESSOR);
        RedissonUtil.KeyOps.delete(TEST_KEY_TRANSACTION);
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor("db1_for_test")) {
            accessor.KeyOps.delete(TEST_KEY_DB1);
        }
        System.out.println("====== RedisUtil 测试结束 ======");
    }

    @Test
    @Order(1)
    @DisplayName("1. 静态API - 基本操作 (Set/Get)")
    void testStaticApi() {
        System.out.println("--- 测试静态API ---");
        String value = "hello_static";
        RedissonUtil.StringOps.set(TEST_KEY_STATIC, value);
        System.out.println("静态Set: key=" + TEST_KEY_STATIC + ", value=" + value);
        String retrievedValue = RedissonUtil.StringOps.get(TEST_KEY_STATIC);
        System.out.println("静态Get: " + retrievedValue);
        System.out.println("验证结果: " + value.equals(retrievedValue));
    }

    @Test
    @Order(2)
    @DisplayName("2. 链式访问器 - 基本操作 (Set/Get)")
    void testAccessorApi() {
        System.out.println("--- 测试链式访问器 ---");
        String value = "hello_accessor";
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            accessor.StringOps.set(TEST_KEY_ACCESSOR, value);
            System.out.println("访问器Set: key=" + TEST_KEY_ACCESSOR + ", value=" + value);
            String retrievedValue = accessor.StringOps.get(TEST_KEY_ACCESSOR);
            System.out.println("访问器Get: " + retrievedValue);
            System.out.println("验证结果: " + value.equals(retrievedValue));
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 多数据源 - 操作DB1")
    void testMultiDataSource() {
        System.out.println("--- 测试多数据源 (db1_for_test) ---");
        String value = "hello_db1";
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor("db1_for_test")) {
            accessor.StringOps.set(TEST_KEY_DB1, value);
            System.out.println("DB1 Set: key=" + TEST_KEY_DB1 + ", value=" + value);
            String retrievedValue = accessor.StringOps.get(TEST_KEY_DB1);
            System.out.println("DB1 Get: " + retrievedValue);
            System.out.println("验证结果: " + value.equals(retrievedValue));
        }
        // 验证默认DB中不存在此key
        Boolean existsInDefaultDb = RedissonUtil.KeyOps.hasKey(TEST_KEY_DB1);
        System.out.println("在默认DB中检查key '" + TEST_KEY_DB1 + "': " + (existsInDefaultDb ? "存在" : "不存在"));
        System.out.println("验证结果: " + !existsInDefaultDb);
    }

    @Test
    @Order(4)
    @DisplayName("4. 事务 - MULTI/EXEC")
    void testTransaction() {
        System.out.println("--- 测试事务 ---");
        String value1 = "transaction_val_1";
        String value2 = "transaction_val_2";
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            // 开启事务
            accessor.multi();
            accessor.StringOps.set(TEST_KEY_TRANSACTION, value1);
            accessor.StringOps.set(TEST_KEY_TRANSACTION, value2); // 后一个会覆盖前一个
            // 执行事务
            List<Object> results = accessor.exec();
            System.out.println("事务执行结果: " + results);
        }
        // 验证最终结果
        String finalValue = RedissonUtil.StringOps.get(TEST_KEY_TRANSACTION);
        System.out.println("事务后Get: " + finalValue);
        System.out.println("验证结果: " + value2.equals(finalValue));
    }

    @Test
    @Order(5)
    @DisplayName("5. 分布式锁 - Lock/Unlock")
    void testDistributedLock() throws InterruptedException {
        System.out.println("--- 测试分布式锁 ---");
        System.out.println("尝试获取锁: " + TEST_KEY_LOCK);
        RedissonUtil.ReentrantLockOps.lock(TEST_KEY_LOCK, 10, TimeUnit.SECONDS);
        System.out.println("锁已获取。");
        try {
            // 模拟业务操作
            System.out.println("持有锁，执行业务逻辑...");
            Thread.sleep(100);
        } finally {
            RedissonUtil.ReentrantLockOps.unlock(TEST_KEY_LOCK);
            System.out.println("锁已释放。");
        }
        // 验证锁是否真的被释放
        boolean canLockAgain = RedissonUtil.ReentrantLockOps.tryLock(TEST_KEY_LOCK);
        System.out.println("再次尝试获取锁: " + (canLockAgain ? "成功" : "失败"));
        if (canLockAgain) {
            RedissonUtil.ReentrantLockOps.unlock(TEST_KEY_LOCK);
            System.out.println("再次获取的锁已释放。");
        }
        System.out.println("验证结果: " + canLockAgain);
    }
}
