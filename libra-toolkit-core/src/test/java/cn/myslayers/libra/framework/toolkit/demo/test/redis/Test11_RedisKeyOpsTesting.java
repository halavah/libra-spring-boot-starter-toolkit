package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.KeyOps 功能测试")
public class Test11_RedisKeyOpsTesting {

    private static final String KEY_PREFIX = "test:keyops:";
    private static final String KEY_SINGLE = KEY_PREFIX + "single";
    private static final String KEY_MULTI_1 = KEY_PREFIX + "multi:1";
    private static final String KEY_MULTI_2 = KEY_PREFIX + "multi:2";
    private static final String KEY_RENAME_OLD = KEY_PREFIX + "rename:old";
    private static final String KEY_RENAME_NEW = KEY_PREFIX + "rename:new";
    private static final String KEY_RENAME_NX_OLD = KEY_PREFIX + "renamenx:old";
    private static final String KEY_RENAME_NX_NEW = KEY_PREFIX + "renamenx:new";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 KeyOps 测试 ======");
        // 清理可能存在的旧测试数据
        Set<String> keysToDelete = RedissonUtil.KeyOps.keys(KEY_PREFIX + "*");
        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            System.out.println("清理旧的测试 keys: " + keysToDelete);
            RedissonUtil.KeyOps.delete(keysToDelete);
        }
    }

    @AfterAll
    static void tearDown() {
        // 再次清理，确保环境干净
        Set<String> keysToDelete = RedissonUtil.KeyOps.keys(KEY_PREFIX + "*");
        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            System.out.println("清理所有测试 keys: " + keysToDelete);
            RedissonUtil.KeyOps.delete(keysToDelete);
        }
        System.out.println("====== 结束 KeyOps 测试 ======");
    }

    @BeforeEach
    void initKeys() {
        // 在每个测试前确保一些基础 key 存在
        RedissonUtil.StringOps.set(KEY_SINGLE, "v1");
        RedissonUtil.StringOps.set(KEY_MULTI_1, "v2");
        RedissonUtil.StringOps.set(KEY_MULTI_2, "v3");
        RedissonUtil.StringOps.set(KEY_RENAME_OLD, "rename_value");
        RedissonUtil.StringOps.set(KEY_RENAME_NX_OLD, "renamenx_value");
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试 hasKey(key)")
    void testHasKey() {
        System.out.println("--- 测试 hasKey ---");
        System.out.println("hasKey('" + KEY_SINGLE + "'): " + RedissonUtil.KeyOps.hasKey(KEY_SINGLE));
        System.out.println("hasKey('non_existent_key'): " + RedissonUtil.KeyOps.hasKey("non_existent_key"));
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 delete(key)")
    void testDeleteSingle() {
        System.out.println("--- 测试 delete(key) ---");
        String keyToDelete = KEY_PREFIX + "to_delete";
        RedissonUtil.StringOps.set(keyToDelete, "temp");
        System.out.println("删除前 hasKey('" + keyToDelete + "'): " + RedissonUtil.KeyOps.hasKey(keyToDelete));
        RedissonUtil.KeyOps.delete(keyToDelete);
        System.out.println("删除后 hasKey('" + keyToDelete + "'): " + RedissonUtil.KeyOps.hasKey(keyToDelete));
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 delete(keys)")
    void testDeleteMultiple() {
        System.out.println("--- 测试 delete(keys) ---");
        Set<String> keysToDelete = new HashSet<>(Arrays.asList(KEY_MULTI_1, KEY_MULTI_2));
        System.out.println("删除前 hasKey('" + KEY_MULTI_1 + "'): " + RedissonUtil.KeyOps.hasKey(KEY_MULTI_1));
        System.out.println("删除前 hasKey('" + KEY_MULTI_2 + "'): " + RedissonUtil.KeyOps.hasKey(KEY_MULTI_2));
        RedissonUtil.KeyOps.delete(keysToDelete);
        System.out.println("删除后 hasKey('" + KEY_MULTI_1 + "'): " + RedissonUtil.KeyOps.hasKey(KEY_MULTI_1));
        System.out.println("删除后 hasKey('" + KEY_MULTI_2 + "'): " + RedissonUtil.KeyOps.hasKey(KEY_MULTI_2));
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 type(key)")
    void testType() {
        System.out.println("--- 测试 type ---");
        System.out.println("type('" + KEY_SINGLE + "'): " + RedissonUtil.KeyOps.type(KEY_SINGLE));
        String listKey = KEY_PREFIX + "list_type";
        RedissonUtil.ListOps.lLeftPush(listKey, "a");
        System.out.println("type('" + listKey + "'): " + RedissonUtil.KeyOps.type(listKey));
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 keys(pattern)")
    void testKeys() {
        System.out.println("--- 测试 keys ---");
        System.out.println("keys('" + KEY_PREFIX + "*'): " + RedissonUtil.KeyOps.keys(KEY_PREFIX + "*"));
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 randomKey()")
    void testRandomKey() {
        System.out.println("--- 测试 randomKey ---");
        System.out.println("randomKey(): " + RedissonUtil.KeyOps.randomKey());
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试 rename(oldKey, newKey)")
    void testRename() {
        System.out.println("--- 测试 rename ---");
        System.out.println("rename 前 old key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_OLD));
        System.out.println("rename 前 new key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NEW));
        RedissonUtil.KeyOps.rename(KEY_RENAME_OLD, KEY_RENAME_NEW);
        System.out.println("rename 后 old key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_OLD));
        System.out.println("rename 后 new key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NEW));
        System.out.println("new key 的值: " + RedissonUtil.StringOps.get(KEY_RENAME_NEW));
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试 renameIfAbsent(oldKey, newKey)")
    void testRenameIfAbsent() {
        System.out.println("--- 测试 renameIfAbsent ---");
        // 场景1: newKey 不存在，应该成功
        System.out.println("renameIfAbsent 前 old key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NX_OLD));
        System.out.println("renameIfAbsent 前 new key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NX_NEW));
        boolean result1 = RedissonUtil.KeyOps.renameIfAbsent(KEY_RENAME_NX_OLD, KEY_RENAME_NX_NEW);
        System.out.println("renameIfAbsent 结果 (newKey不存在时): " + result1);
        System.out.println("renameIfAbsent 后 old key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NX_OLD));
        System.out.println("renameIfAbsent 后 new key 存在: " + RedissonUtil.KeyOps.hasKey(KEY_RENAME_NX_NEW));

        // 场景2: newKey 已存在，应该失败
        RedissonUtil.StringOps.set(KEY_RENAME_NX_OLD, "another_value"); // 重置 old key
        boolean result2 = RedissonUtil.KeyOps.renameIfAbsent(KEY_RENAME_NX_OLD, KEY_RENAME_NX_NEW);
        System.out.println("renameIfAbsent 结果 (newKey已存在时): " + result2);
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试 expire(key, timeout, unit)")
    void testExpire() throws InterruptedException {
        System.out.println("--- 测试 expire ---");
        String key = KEY_PREFIX + "expire";
        RedissonUtil.StringOps.set(key, "expire_me");
        RedissonUtil.KeyOps.expire(key, 2, TimeUnit.SECONDS);
        System.out.println("设置 2s 过期时间后, getExpire('" + key + "'): " + RedissonUtil.KeyOps.getExpire(key));
        Thread.sleep(2100);
        System.out.println("2.1s 后, hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试 expireAt(key, date)")
    void testExpireAt() throws InterruptedException {
        System.out.println("--- 测试 expireAt ---");
        String key = KEY_PREFIX + "expire_at";
        RedissonUtil.StringOps.set(key, "expire_at_me");
        Date expireTime = new Date(System.currentTimeMillis() + 2000); // 2秒后过期
        RedissonUtil.KeyOps.expireAt(key, expireTime);
        System.out.println("设置 2s 后过期, getExpire('" + key + "'): " + RedissonUtil.KeyOps.getExpire(key));
        Thread.sleep(2100);
        System.out.println("2.1s 后, hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));
    }

    @Test
    @Order(11)
    @DisplayName("11. 测试 persist(key)")
    void testPersist() {
        System.out.println("--- 测试 persist ---");
        String key = KEY_PREFIX + "persist";
        RedissonUtil.StringOps.set(key, "persist_me");
        RedissonUtil.KeyOps.expire(key, 10, TimeUnit.SECONDS);
        System.out.println("设置过期时间后, getExpire('" + key + "'): " + RedissonUtil.KeyOps.getExpire(key));
        RedissonUtil.KeyOps.persist(key);
        System.out.println("persist 后, getExpire('" + key + "'): " + RedissonUtil.KeyOps.getExpire(key));
    }

    @Test
    @Order(12)
    @DisplayName("12. 测试 getExpire(key, unit)")
    void testGetExpireWithUnit() {
        System.out.println("--- 测试 getExpire(unit) ---");
        String key = KEY_PREFIX + "get_expire";
        RedissonUtil.StringOps.set(key, "get_expire_me");
        RedissonUtil.KeyOps.expire(key, 1, TimeUnit.MINUTES);
        System.out.println("getExpire('" + key + "', SECONDS): " + RedissonUtil.KeyOps.getExpire(key, TimeUnit.SECONDS));
        System.out.println("getExpire('" + key + "', MILLISECONDS): " + RedissonUtil.KeyOps.getExpire(key, TimeUnit.MILLISECONDS));
    }

    @Test
    @Order(13)
    @DisplayName("13. 测试 dump(key) 和 move(key, dbIndex)")
    void testDumpAndMove() {
        System.out.println("--- 测试 dump 和 move ---");
        String key = KEY_PREFIX + "dump_move";
        RedissonUtil.StringOps.set(key, "dump_move_me");
        byte[] dumpedValue = RedissonUtil.KeyOps.dump(key);
        System.out.println("dump('" + key + "') 的序列化值长度: " + (dumpedValue != null ? dumpedValue.length : "null"));

        // 注意: move 操作会将被移动的 key 从当前数据库删除。
        // 这个测试依赖于你的 Redis Server 配置，如果只有一个 DB，可能会表现不一。
        // 这里假设至少有两个 DB (0 和 1)。
        System.out.println("move 前, DB 0 hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));
        boolean moveResult = RedissonUtil.KeyOps.move(key, 1);
        System.out.println("move('" + key + "', 1) 结果: " + moveResult);
        System.out.println("move 后, DB 0 hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));

        // 切换到 DB 1 验证并移回
        RedissonUtil.LettuceCore.use("db1_for_test"); // 假设已配置
        System.out.println("切换到 DB 1, hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));
        RedissonUtil.KeyOps.move(key, 0); // 移回
        RedissonUtil.LettuceCore.clear(); // 切换回默认 DB
        System.out.println("切换回 DB 0, hasKey('" + key + "'): " + RedissonUtil.KeyOps.hasKey(key));
    }
}
