package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import org.junit.jupiter.api.*;

import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.HashOps 功能测试")
public class Test13_RedisHashOpsTesting {

    private static final String KEY_PREFIX = "test:hashops:";
    private static final String HASH_KEY = KEY_PREFIX + "myhash";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 HashOps 测试 ======");
        cleanUp();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
        System.out.println("====== 结束 HashOps 测试 ======");
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
    @DisplayName("1. 测试 hPut(key, hashKey, value) 和 hGet(key, hashKey)")
    void testHPutAndHGet() {
        System.out.println("--- 测试 hPut 和 hGet ---");
        RedissonUtil.HashOps.hPut(HASH_KEY, "field1", "value1");
        String value = RedissonUtil.HashOps.hGet(HASH_KEY, "field1");
        System.out.println("hGet('" + HASH_KEY + "', 'field1'): " + value);
        System.out.println("hGet a non-existent field: " + RedissonUtil.HashOps.hGet(HASH_KEY, "nonexistent"));
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 hPutAll(key, maps) 和 hGetAll(key)")
    void testHPutAllAndHGetAll() {
        System.out.println("--- 测试 hPutAll 和 hGetAll ---");
        Map<String, String> map = new HashMap<>();
        map.put("field2", "value2");
        map.put("field3", "value3");
        RedissonUtil.HashOps.hPutAll(HASH_KEY, map);
        Map<String, String> resultMap = RedissonUtil.HashOps.hGetAll(HASH_KEY);
        System.out.println("hGetAll('" + HASH_KEY + "'): " + resultMap);
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 hMultiGet(key, fields)")
    void testHMultiGet() {
        System.out.println("--- 测试 hMultiGet ---");
        Map<String, String> resultMap = RedissonUtil.HashOps.hMultiGet(HASH_KEY, Arrays.asList("field1", "field3", "nonexistent"));
        System.out.println("hMultiGet 结果 (只应包含存在的字段): " + resultMap);
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 hPutIfAbsent(key, hashKey, value)")
    void testHPutIfAbsent() {
        System.out.println("--- 测试 hPutIfAbsent ---");
        boolean result1 = RedissonUtil.HashOps.hPutIfAbsent(HASH_KEY, "field4", "value4");
        System.out.println("第一次 hPutIfAbsent('field4'): " + result1);
        System.out.println("第一次后 hGet('field4'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field4"));
        boolean result2 = RedissonUtil.HashOps.hPutIfAbsent(HASH_KEY, "field4", "new_value4");
        System.out.println("第二次 hPutIfAbsent('field4'): " + result2);
        System.out.println("第二次后 hGet('field4'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field4"));
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 hExists(key, field)")
    void testHExists() {
        System.out.println("--- 测试 hExists ---");
        System.out.println("hExists('field1'): " + RedissonUtil.HashOps.hExists(HASH_KEY, "field1"));
        System.out.println("hExists('nonexistent'): " + RedissonUtil.HashOps.hExists(HASH_KEY, "nonexistent"));
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 hKeys(key), hValues(key), hSize(key)")
    void testHKeysValuesSize() {
        System.out.println("--- 测试 hKeys, hValues, hSize ---");
        System.out.println("hKeys('" + HASH_KEY + "'): " + RedissonUtil.HashOps.hKeys(HASH_KEY));
        System.out.println("hValues('" + HASH_KEY + "'): " + RedissonUtil.HashOps.hValues(HASH_KEY));
        System.out.println("hSize('" + HASH_KEY + "'): " + RedissonUtil.HashOps.hSize(HASH_KEY));
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试 hIncrBy(key, field, increment) 和 hIncrByFloat(key, field, delta)")
    void testHIncr() {
        System.out.println("--- 测试 hIncrBy 和 hIncrByFloat ---");
        RedissonUtil.HashOps.hPut(HASH_KEY, "count", "10");
        long longVal = RedissonUtil.HashOps.hIncrBy(HASH_KEY, "count", 5);
        System.out.println("hIncrBy 'count' by 5: " + longVal);

        RedissonUtil.HashOps.hPut(HASH_KEY, "float_count", "10.5");
        double floatVal = RedissonUtil.HashOps.hIncrByFloat(HASH_KEY, "float_count", 1.2);
        System.out.println("hIncrByFloat 'float_count' by 1.2: " + floatVal);
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试 hScan(key, options)")
    void testHScan() {
        System.out.println("--- 测试 hScan ---");
        Map<String, String> largeMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeMap.put("scan_field_" + i, "scan_value_" + i);
        }
        RedissonUtil.HashOps.hPutAll(HASH_KEY, largeMap);

        MapScanCursor<String, String> cursor = RedissonUtil.HashOps.hScan(HASH_KEY, ScanArgs.Builder.limit(10));
        System.out.println("hScan 第一次迭代 (limit 10): " + cursor.getMap());
        System.out.println("hScan 第一次迭代后 isFinished: " + cursor.isFinished());
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试 hDelete(key, fields)")
    void testHDelete() {
        System.out.println("--- 测试 hDelete ---");
        System.out.println("hDelete 前 hGet('field1'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field1"));
        System.out.println("hDelete 前 hGet('field2'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field2"));
        long deletedCount = RedissonUtil.HashOps.hDelete(HASH_KEY, "field1", "field2", "nonexistent");
        System.out.println("hDelete 删除的数量: " + deletedCount);
        System.out.println("hDelete 后 hGet('field1'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field1"));
        System.out.println("hDelete 后 hGet('field2'): " + RedissonUtil.HashOps.hGet(HASH_KEY, "field2"));
        System.out.println("hDelete 后 hGetAll: " + RedissonUtil.HashOps.hGetAll(HASH_KEY));
    }
}
