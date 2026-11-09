package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.StringOps 功能测试")
public class Test12_RedisStringOpsTesting {

    private static final String KEY_PREFIX = "test:stringops:";
    private static final String KEY_GET = KEY_PREFIX + "get";
    private static final String KEY_SET = KEY_PREFIX + "set";
    private static final String KEY_GETSET = KEY_PREFIX + "getset";
    private static final String KEY_MGET_1 = KEY_PREFIX + "mget:1";
    private static final String KEY_MGET_2 = KEY_PREFIX + "mget:2";
    private static final String KEY_SETEX = KEY_PREFIX + "setex";
    private static final String KEY_SETNX = KEY_PREFIX + "setnx";
    private static final String KEY_APPEND = KEY_PREFIX + "append";
    private static final String KEY_INCR = KEY_PREFIX + "incr";
    private static final String KEY_INCR_FLOAT = KEY_PREFIX + "incr_float";
    private static final String KEY_RANGE = KEY_PREFIX + "range";
    private static final String KEY_BIT = KEY_PREFIX + "bit";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 StringOps 测试 ======");
        cleanUp();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
        System.out.println("====== 结束 StringOps 测试 ======");
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
    @DisplayName("1. 测试 set(key, value) 和 get(key)")
    void testSetAndGet() {
        System.out.println("--- 测试 set 和 get ---");
        RedissonUtil.StringOps.set(KEY_SET, "hello world");
        String value = RedissonUtil.StringOps.get(KEY_SET);
        System.out.println("set 后 get('" + KEY_SET + "'): " + value);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 getAndSet(key, value)")
    void testGetAndSet() {
        System.out.println("--- 测试 getAndSet ---");
        RedissonUtil.StringOps.set(KEY_GETSET, "initial_value");
        System.out.println("getAndSet 前 get('" + KEY_GETSET + "'): " + RedissonUtil.StringOps.get(KEY_GETSET));
        String oldValue = RedissonUtil.StringOps.getAndSet(KEY_GETSET, "new_value");
        System.out.println("getAndSet 返回的旧值: " + oldValue);
        System.out.println("getAndSet 后 get('" + KEY_GETSET + "'): " + RedissonUtil.StringOps.get(KEY_GETSET));
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 multiSet(maps) 和 multiGet(keys)")
    void testMultiSetAndMultiGet() {
        System.out.println("--- 测试 multiSet 和 multiGet ---");
        Map<String, String> map = new HashMap<>();
        map.put(KEY_MGET_1, "value1");
        map.put(KEY_MGET_2, "value2");
        RedissonUtil.StringOps.multiSet(map);
        Map<String, String> resultMap = RedissonUtil.StringOps.multiGet(Arrays.asList(KEY_MGET_1, KEY_MGET_2, "non_existent_key"));
        System.out.println("multiGet 结果 (只应包含存在的键): " + resultMap);
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 setIfAbsent(key, value)")
    void testSetIfAbsent() {
        System.out.println("--- 测试 setIfAbsent ---");
        boolean result1 = RedissonUtil.StringOps.setIfAbsent(KEY_SETNX, "first_time");
        System.out.println("第一次 setIfAbsent('" + KEY_SETNX + "'): " + result1);
        System.out.println("第一次后 get('" + KEY_SETNX + "'): " + RedissonUtil.StringOps.get(KEY_SETNX));
        boolean result2 = RedissonUtil.StringOps.setIfAbsent(KEY_SETNX, "second_time");
        System.out.println("第二次 setIfAbsent('" + KEY_SETNX + "'): " + result2);
        System.out.println("第二次后 get('" + KEY_SETNX + "'): " + RedissonUtil.StringOps.get(KEY_SETNX));
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 setEx(key, value, timeout, unit)")
    void testSetEx() throws InterruptedException {
        System.out.println("--- 测试 setEx ---");
        RedissonUtil.StringOps.setEx(KEY_SETEX, "expire_me", 2, TimeUnit.SECONDS);
        System.out.println("setEx 后 get('" + KEY_SETEX + "'): " + RedissonUtil.StringOps.get(KEY_SETEX));
        Thread.sleep(2100);
        System.out.println("2.1s 后 get('" + KEY_SETEX + "'): " + RedissonUtil.StringOps.get(KEY_SETEX));
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 incrBy(key, delta) 和 incrByFloat(key, delta)")
    void testIncr() {
        System.out.println("--- 测试 incrBy 和 incrByFloat ---");
        RedissonUtil.StringOps.set(KEY_INCR, "10");
        long longVal = RedissonUtil.StringOps.incrBy(KEY_INCR, 5);
        System.out.println("incrBy 5 结果: " + longVal);

        RedissonUtil.StringOps.set(KEY_INCR_FLOAT, "10.5");
        double floatVal = RedissonUtil.StringOps.incrByFloat(KEY_INCR_FLOAT, 1.2);
        System.out.println("incrByFloat 1.2 结果: " + floatVal);
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试 append(key, value)")
    void testAppend() {
        System.out.println("--- 测试 append ---");
        RedissonUtil.StringOps.set(KEY_APPEND, "Hello");
        RedissonUtil.StringOps.append(KEY_APPEND, " World");
        System.out.println("append 后 get('" + KEY_APPEND + "'): " + RedissonUtil.StringOps.get(KEY_APPEND));
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试 getRange(key, start, end) 和 setRange(key, value, offset)")
    void testRange() {
        System.out.println("--- 测试 getRange 和 setRange ---");
        RedissonUtil.StringOps.set(KEY_RANGE, "Hello World");
        System.out.println("getRange(0, 3): " + RedissonUtil.StringOps.getRange(KEY_RANGE, 0, 3));
        System.out.println("getRange(-5, -1): " + RedissonUtil.StringOps.getRange(KEY_RANGE, -5, -1));
        RedissonUtil.StringOps.setRange(KEY_RANGE, "Redis", 6);
        System.out.println("setRange('Redis', 6) 后 get('" + KEY_RANGE + "'): " + RedissonUtil.StringOps.get(KEY_RANGE));
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试 size(key)")
    void testSize() {
        System.out.println("--- 测试 size ---");
        RedissonUtil.StringOps.set(KEY_GET, "test_size");
        System.out.println("size('" + KEY_GET + "'): " + RedissonUtil.StringOps.size(KEY_GET));
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试 setBit(key, offset, value) 和 getBit(key, offset)")
    void testBit() {
        System.out.println("--- 测试 setBit 和 getBit ---");
        // 'a' -> 01100001
        RedissonUtil.StringOps.set(KEY_BIT, "a");
        System.out.println("getBit(1): " + RedissonUtil.StringOps.getBit(KEY_BIT, 1));
        System.out.println("getBit(2): " + RedissonUtil.StringOps.getBit(KEY_BIT, 2));
        RedissonUtil.StringOps.setBit(KEY_BIT, 2, false);
        System.out.println("setBit(2, false) 后 getBit(2): " + RedissonUtil.StringOps.getBit(KEY_BIT, 2));
        // 'a' -> 01000001 -> 'A'
        System.out.println("修改后 get('" + KEY_BIT + "'): " + RedissonUtil.StringOps.get(KEY_BIT));
    }
}
