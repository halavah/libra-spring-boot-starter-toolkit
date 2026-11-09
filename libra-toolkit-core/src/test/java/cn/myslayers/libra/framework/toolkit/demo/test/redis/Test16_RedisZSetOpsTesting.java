package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import io.lettuce.core.ScoredValue;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil.ZSetOps 功能测试")
public class Test16_RedisZSetOpsTesting {

    private static final String KEY_PREFIX = "test:zsetops:";
    private static final String ZSET_KEY = KEY_PREFIX + "myzset";
    private static final String ZSET_KEY_OTHER = KEY_PREFIX + "myzset_other";
    private static final String ZSET_KEY_STORE = KEY_PREFIX + "myzset_store";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 ZSetOps 测试 ======");
        cleanUp();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
        System.out.println("====== 结束 ZSetOps 测试 ======");
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
    @DisplayName("1. 测试 zAdd 和 zRange")
    void testZAddAndZRange() {
        System.out.println("--- 测试 zAdd 和 zRange ---");
        RedissonUtil.ZSetOps.zAdd(ZSET_KEY, "one", 1);
        RedissonUtil.ZSetOps.zAdd(ZSET_KEY, "two", 2);
        Set<ScoredValue<String>> scoredValues = new HashSet<>();
        scoredValues.add(ScoredValue.just(3.0, "three"));
        scoredValues.add(ScoredValue.just(4.0, "four"));
        RedissonUtil.ZSetOps.zAdd(ZSET_KEY, scoredValues);

        System.out.println("zRange(0, -1): " + RedissonUtil.ZSetOps.zRange(ZSET_KEY, 0, -1));
        System.out.println("zRangeWithScores(0, -1): " + RedissonUtil.ZSetOps.zRangeWithScores(ZSET_KEY, 0, -1));
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 zSize, zZCard, zCount")
    void testZSizeAndZCount() {
        System.out.println("--- 测试 zSize, zZCard, zCount ---");
        System.out.println("zSize: " + RedissonUtil.ZSetOps.zSize(ZSET_KEY));
        System.out.println("zZCard: " + RedissonUtil.ZSetOps.zZCard(ZSET_KEY));
        System.out.println("zCount(1, 3): " + RedissonUtil.ZSetOps.zCount(ZSET_KEY, 1, 3));
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试 zRank, zReverseRank, zScore")
    void testZRankAndZScore() {
        System.out.println("--- 测试 zRank, zReverseRank, zScore ---");
        System.out.println("zRank('two'): " + RedissonUtil.ZSetOps.zRank(ZSET_KEY, "two"));
        System.out.println("zReverseRank('two'): " + RedissonUtil.ZSetOps.zReverseRank(ZSET_KEY, "two"));
        System.out.println("zScore('two'): " + RedissonUtil.ZSetOps.zScore(ZSET_KEY, "two"));
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 zIncrementScore")
    void testZIncrementScore() {
        System.out.println("--- 测试 zIncrementScore ---");
        System.out.println("zIncrementScore('one', 1.5) 前 score: " + RedissonUtil.ZSetOps.zScore(ZSET_KEY, "one"));
        double newScore = RedissonUtil.ZSetOps.zIncrementScore(ZSET_KEY, "one", 1.5);
        System.out.println("zIncrementScore 返回的新 score: " + newScore);
        System.out.println("zIncrementScore 后 score: " + RedissonUtil.ZSetOps.zScore(ZSET_KEY, "one"));
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 zRangeByScore 和 zReverseRangeByScore")
    void testZRangeByScore() {
        System.out.println("--- 测试 zRangeByScore 和 zReverseRangeByScore ---");
        System.out.println("zRangeByScore(2, 4): " + RedissonUtil.ZSetOps.zRangeByScore(ZSET_KEY, 2, 4));
        System.out.println("zRangeByScoreWithScores(2, 4): " + RedissonUtil.ZSetOps.zRangeByScoreWithScores(ZSET_KEY, 2, 4));
        System.out.println("zReverseRangeByScore(2, 4): " + RedissonUtil.ZSetOps.zReverseRangeByScore(ZSET_KEY, 2, 4));
        System.out.println("zReverseRangeByScoreWithScores(2, 4): " + RedissonUtil.ZSetOps.zReverseRangeByScoreWithScores(ZSET_KEY, 2, 4));
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 zRemove 和 zRemoveRange")
    void testZRemove() {
        System.out.println("--- 测试 zRemove 和 zRemoveRange ---");
        System.out.println("zRemove 'four' 结果: " + RedissonUtil.ZSetOps.zRemove(ZSET_KEY, "four"));
        System.out.println("zRemove 后 zRange: " + RedissonUtil.ZSetOps.zRange(ZSET_KEY, 0, -1));
        System.out.println("zRemoveRange(0, 0) 结果: " + RedissonUtil.ZSetOps.zRemoveRange(ZSET_KEY, 0, 0));
        System.out.println("zRemoveRange 后 zRange: " + RedissonUtil.ZSetOps.zRange(ZSET_KEY, 0, -1));
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试 zIntersectAndStore 和 zUnionAndStore")
    void testZStoreOperations() {
        System.out.println("--- 测试 zIntersectAndStore 和 zUnionAndStore ---");
        RedissonUtil.ZSetOps.zAdd(ZSET_KEY_OTHER, "two", 20);
        RedissonUtil.ZSetOps.zAdd(ZSET_KEY_OTHER, "three", 30);

        // Intersect
        long interCount = RedissonUtil.ZSetOps.zIntersectAndStore(ZSET_KEY, ZSET_KEY_OTHER, ZSET_KEY_STORE);
        System.out.println("zIntersectAndStore 数量: " + interCount);
        System.out.println("zIntersectAndStore 后 " + ZSET_KEY_STORE + ": " + RedissonUtil.ZSetOps.zRangeWithScores(ZSET_KEY_STORE, 0, -1));
        RedissonUtil.KeyOps.delete(ZSET_KEY_STORE);

        // Union
        long unionCount = RedissonUtil.ZSetOps.zUnionAndStore(ZSET_KEY, ZSET_KEY_OTHER, ZSET_KEY_STORE);
        System.out.println("zUnionAndStore 数量: " + unionCount);
        System.out.println("zUnionAndStore 后 " + ZSET_KEY_STORE + ": " + RedissonUtil.ZSetOps.zRangeWithScores(ZSET_KEY_STORE, 0, -1));
    }
}
