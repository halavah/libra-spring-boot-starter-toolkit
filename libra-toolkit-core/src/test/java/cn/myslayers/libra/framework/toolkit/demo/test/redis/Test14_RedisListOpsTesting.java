package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.ListOps 功能测试")
public class Test14_RedisListOpsTesting {

    private static final String KEY_PREFIX = "test:listops:";
    private static final String LIST_KEY = KEY_PREFIX + "mylist";
    private static final String LIST_KEY_OTHER = KEY_PREFIX + "mylist_other";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 ListOps 测试 ======");
        cleanUp();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
        System.out.println("====== 结束 ListOps 测试 ======");
    }

    private static void cleanUp() {
        Set<String> keysToDelete = RedissonUtil.KeyOps.keys(KEY_PREFIX + "*");
        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            System.out.println("清理测试 keys: " + keysToDelete);
            RedissonUtil.KeyOps.delete(keysToDelete);
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试 lLeftPush 和 lRange")
    void testLLeftPushAndRange() {
        System.out.println("--- 测试 lLeftPush 和 lRange ---");
        RedissonUtil.ListOps.lLeftPush(LIST_KEY, "c");
        RedissonUtil.ListOps.lLeftPush(LIST_KEY, "b");
        RedissonUtil.ListOps.lLeftPush(LIST_KEY, "a");
        List<String> range = RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1);
        System.out.println("lLeftPush 'a','b','c' 后 lRange(0, -1): " + range);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 lRightPush")
    void testLRightPush() {
        System.out.println("--- 测试 lRightPush ---");
        RedissonUtil.ListOps.lRightPush(LIST_KEY, "d");
        List<String> range = RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1);
        System.out.println("lRightPush 'd' 后 lRange(0, -1): " + range);
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 lLen 和 lIndex")
    void testLLenAndLIndex() {
        System.out.println("--- 测试 lLen 和 lIndex ---");
        System.out.println("lLen('" + LIST_KEY + "'): " + RedissonUtil.ListOps.lLen(LIST_KEY));
        System.out.println("lIndex('" + LIST_KEY + "', 0): " + RedissonUtil.ListOps.lIndex(LIST_KEY, 0));
        System.out.println("lIndex('" + LIST_KEY + "', -1): " + RedissonUtil.ListOps.lIndex(LIST_KEY, -1));
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 lLeftPop 和 lRightPop")
    void testPops() {
        System.out.println("--- 测试 lLeftPop 和 lRightPop ---");
        String leftPopValue = RedissonUtil.ListOps.lLeftPop(LIST_KEY);
        System.out.println("lLeftPop 返回值: " + leftPopValue);
        System.out.println("lLeftPop 后 lRange(0, -1): " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
        String rightPopValue = RedissonUtil.ListOps.lRightPop(LIST_KEY);
        System.out.println("lRightPop 返回值: " + rightPopValue);
        System.out.println("lRightPop 后 lRange(0, -1): " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 lSet")
    void testLSet() {
        System.out.println("--- 测试 lSet ---");
        System.out.println("lSet 前 lIndex(0): " + RedissonUtil.ListOps.lIndex(LIST_KEY, 0));
        RedissonUtil.ListOps.lSet(LIST_KEY, 0, "b_modified");
        System.out.println("lSet 后 lIndex(0): " + RedissonUtil.ListOps.lIndex(LIST_KEY, 0));
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 lRemove")
    void testLRemove() {
        System.out.println("--- 测试 lRemove ---");
        RedissonUtil.ListOps.lRightPush(LIST_KEY, "b_modified");
        System.out.println("lRemove 前 lRange: " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
        long removedCount = RedissonUtil.ListOps.lRemove(LIST_KEY, 2, "b_modified");
        System.out.println("lRemove 'b_modified' 数量: " + removedCount);
        System.out.println("lRemove 后 lRange: " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试 lTrim")
    void testLTrim() {
        System.out.println("--- 测试 lTrim ---");
        RedissonUtil.ListOps.lRightPushAll(LIST_KEY, "x", "y", "z");
        System.out.println("lTrim 前 lRange: " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
        RedissonUtil.ListOps.lTrim(LIST_KEY, 1, 2);
        System.out.println("lTrim(1, 2) 后 lRange: " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试 lRightPopAndLeftPush")
    void testLRightPopAndLeftPush() {
        System.out.println("--- 测试 lRightPopAndLeftPush ---");
        System.out.println("操作前 " + LIST_KEY + ": " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
        System.out.println("操作前 " + LIST_KEY_OTHER + ": " + RedissonUtil.ListOps.lRange(LIST_KEY_OTHER, 0, -1));
        String movedValue = RedissonUtil.ListOps.lRightPopAndLeftPush(LIST_KEY, LIST_KEY_OTHER);
        System.out.println("移动的值: " + movedValue);
        System.out.println("操作后 " + LIST_KEY + ": " + RedissonUtil.ListOps.lRange(LIST_KEY, 0, -1));
        System.out.println("操作后 " + LIST_KEY_OTHER + ": " + RedissonUtil.ListOps.lRange(LIST_KEY_OTHER, 0, -1));
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试 lBLeftPop (阻塞)")
    void testLBLeftPop() {
        System.out.println("--- 测试 lBLeftPop ---");
        // 为了不让测试永久阻塞，我们先准备一个元素
        String key = KEY_PREFIX + "blpop";
        RedissonUtil.ListOps.lLeftPush(key, "block_pop_value");
        String value = RedissonUtil.ListOps.lBLeftPop(key, 1, TimeUnit.SECONDS);
        System.out.println("lBLeftPop (有元素时) 返回值: " + value);
        // 测试超时
        System.out.println("lBLeftPop (无元素时) 开始等待 1s...");
        String timeoutValue = RedissonUtil.ListOps.lBLeftPop(key, 1, TimeUnit.SECONDS);
        System.out.println("lBLeftPop (无元素时) 返回值: " + timeoutValue);
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试 lLeftPushAll (Collection)")
    void testLLeftPushAllCollection() {
        System.out.println("--- 测试 lLeftPushAll (Collection) ---");
        String key = KEY_PREFIX + "pushall";
        List<String> values = Arrays.asList("v1", "v2", "v3");
        RedissonUtil.ListOps.lLeftPushAll(key, values);
        System.out.println("lLeftPushAll(Collection) 后 lRange: " + RedissonUtil.ListOps.lRange(key, 0, -1));
    }
}
