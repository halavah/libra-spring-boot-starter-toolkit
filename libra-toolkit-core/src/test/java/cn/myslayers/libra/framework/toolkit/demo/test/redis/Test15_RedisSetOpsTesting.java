package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ValueScanCursor;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.SetOps 功能测试")
public class Test15_RedisSetOpsTesting {

    private static final String KEY_PREFIX = "test:setops:";
    private static final String SET_KEY = KEY_PREFIX + "myset";
    private static final String SET_KEY_OTHER = KEY_PREFIX + "myset_other";
    private static final String SET_KEY_INTER = KEY_PREFIX + "myset_inter";
    private static final String SET_KEY_UNION = KEY_PREFIX + "myset_union";
    private static final String SET_KEY_DIFF = KEY_PREFIX + "myset_diff";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 SetOps 测试 ======");
        cleanUp();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
        System.out.println("====== 结束 SetOps 测试 ======");
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
    @DisplayName("1. 测试 sAdd 和 sMembers")
    void testSAddAndSMembers() {
        System.out.println("--- 测试 sAdd 和 sMembers ---");
        RedissonUtil.SetOps.sAdd(SET_KEY, "a", "b", "c", "c");
        Set<String> members = RedissonUtil.SetOps.sMembers(SET_KEY);
        System.out.println("sAdd 'a','b','c','c' 后 sMembers: " + members);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 sSize 和 sIsMember")
    void testSSizeAndSIsMember() {
        System.out.println("--- 测试 sSize 和 sIsMember ---");
        System.out.println("sSize('" + SET_KEY + "'): " + RedissonUtil.SetOps.sSize(SET_KEY));
        System.out.println("sIsMember('a'): " + RedissonUtil.SetOps.sIsMember(SET_KEY, "a"));
        System.out.println("sIsMember('d'): " + RedissonUtil.SetOps.sIsMember(SET_KEY, "d"));
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 sPop 和 sRandomMember")
    void testSPopAndSRandomMember() {
        System.out.println("--- 测试 sPop 和 sRandomMember ---");
        System.out.println("sRandomMember: " + RedissonUtil.SetOps.sRandomMember(SET_KEY));
        System.out.println("sPop: " + RedissonUtil.SetOps.sPop(SET_KEY));
        System.out.println("sPop 后 sMembers: " + RedissonUtil.SetOps.sMembers(SET_KEY));
        // 重新添加，保证后续测试数据一致
        RedissonUtil.SetOps.sAdd(SET_KEY, "a", "b", "c");
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 sRandomMembers 和 sDistinctRandomMembers")
    void testSRandomMembers() {
        System.out.println("--- 测试 sRandomMembers 和 sDistinctRandomMembers ---");
        List<String> randomMembers = RedissonUtil.SetOps.sRandomMembers(SET_KEY, 2);
        System.out.println("sRandomMembers(2): " + randomMembers);
        Set<String> distinctRandomMembers = RedissonUtil.SetOps.sDistinctRandomMembers(SET_KEY, 2);
        System.out.println("sDistinctRandomMembers(2): " + distinctRandomMembers);
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 sRemove")
    void testSRemove() {
        System.out.println("--- 测试 sRemove ---");
        long removedCount = RedissonUtil.SetOps.sRemove(SET_KEY, "b", "d");
        System.out.println("sRemove 'b', 'd' 数量: " + removedCount);
        System.out.println("sRemove 后 sMembers: " + RedissonUtil.SetOps.sMembers(SET_KEY));
        // 恢复数据
        RedissonUtil.SetOps.sAdd(SET_KEY, "b");
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 sMove")
    void testSMove() {
        System.out.println("--- 测试 sMove ---");
        System.out.println("sMove 前 " + SET_KEY + ": " + RedissonUtil.SetOps.sMembers(SET_KEY));
        System.out.println("sMove 前 " + SET_KEY_OTHER + ": " + RedissonUtil.SetOps.sMembers(SET_KEY_OTHER));
        boolean moved = RedissonUtil.SetOps.sMove(SET_KEY, "c", SET_KEY_OTHER);
        System.out.println("sMove 'c' to other 结果: " + moved);
        System.out.println("sMove 后 " + SET_KEY + ": " + RedissonUtil.SetOps.sMembers(SET_KEY));
        System.out.println("sMove 后 " + SET_KEY_OTHER + ": " + RedissonUtil.SetOps.sMembers(SET_KEY_OTHER));
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试集合运算 (Intersect, Union, Difference)")
    void testSetOperations() {
        System.out.println("--- 测试集合运算 ---");
        RedissonUtil.SetOps.sAdd(SET_KEY, "a", "b");
        RedissonUtil.SetOps.sAdd(SET_KEY_OTHER, "b", "c");
        RedissonUtil.SetOps.sAdd(SET_KEY_INTER, "b", "d");

        // Intersect
        System.out.println("Intersect " + SET_KEY + " & " + SET_KEY_OTHER + ": " + RedissonUtil.SetOps.sIntersect(SET_KEY, SET_KEY_OTHER));
        System.out.println("Intersect " + SET_KEY + " & others: " + RedissonUtil.SetOps.sIntersect(SET_KEY, Arrays.asList(SET_KEY_OTHER, SET_KEY_INTER)));

        // Union
        System.out.println("Union " + SET_KEY + " & " + SET_KEY_OTHER + ": " + RedissonUtil.SetOps.sUnion(SET_KEY, SET_KEY_OTHER));

        // Difference
        System.out.println("Difference " + SET_KEY + " - " + SET_KEY_OTHER + ": " + RedissonUtil.SetOps.sDifference(SET_KEY, SET_KEY_OTHER));
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试集合运算并存储")
    void testSetOperationsAndStore() {
        System.out.println("--- 测试集合运算并存储 ---");
        // Intersect and Store
        RedissonUtil.SetOps.sIntersectAndStore(SET_KEY, SET_KEY_OTHER, SET_KEY_INTER);
        System.out.println("sIntersectAndStore 后 " + SET_KEY_INTER + ": " + RedissonUtil.SetOps.sMembers(SET_KEY_INTER));

        // Union and Store
        RedissonUtil.SetOps.sUnionAndStore(SET_KEY, SET_KEY_OTHER, SET_KEY_UNION);
        System.out.println("sUnionAndStore 后 " + SET_KEY_UNION + ": " + RedissonUtil.SetOps.sMembers(SET_KEY_UNION));

        // Difference and Store
        RedissonUtil.SetOps.sDifferenceAndStore(SET_KEY, SET_KEY_OTHER, SET_KEY_DIFF);
        System.out.println("sDifferenceAndStore 后 " + SET_KEY_DIFF + ": " + RedissonUtil.SetOps.sMembers(SET_KEY_DIFF));
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试 sScan")
    void testSScan() {
        System.out.println("--- 测试 sScan ---");
        String scanKey = KEY_PREFIX + "scanset";
        for (int i = 0; i < 100; i++) {
            RedissonUtil.SetOps.sAdd(scanKey, "member_" + i);
        }
        ValueScanCursor<String> cursor = RedissonUtil.SetOps.sScan(scanKey, ScanArgs.Builder.limit(10));
        System.out.println("sScan 第一次迭代 (limit 10): " + cursor.getValues());
        System.out.println("sScan 第一次迭代后 isFinished: " + cursor.isFinished());
    }
}
