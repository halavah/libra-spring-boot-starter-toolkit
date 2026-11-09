package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Redis HyperLogLog 功能测试")
public class Test23_RedisHyperLogLogTesting {

    private static final String HLL_KEY_1 = "hll:test:1";
    private static final String HLL_KEY_2 = "hll:test:2";
    private static final String HLL_MERGED_KEY = "hll:test:merged";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 HyperLogLog 测试 ======");
        RedissonUtil.KeyOps.delete(HLL_KEY_1, HLL_KEY_2, HLL_MERGED_KEY);
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.KeyOps.delete(HLL_KEY_1, HLL_KEY_2, HLL_MERGED_KEY);
        System.out.println("====== HyperLogLog 测试结束 ======");
    }

    @Test
    @Order(1)
    @DisplayName("1. pfAdd: 向 HyperLogLog 添加元素")
    void testPfAdd() {
        System.out.println("--- 测试 pfAdd ---");
        boolean result1 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_1, "user1", "user2", "user3");
        boolean result2 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_2, "user3", "user4", "user5");
        System.out.println("向 " + HLL_KEY_1 + " 添加3个元素，结果: " + result1);
        System.out.println("向 " + HLL_KEY_2 + " 添加3个元素，结果: " + result2);
    }

    @Test
    @Order(2)
    @DisplayName("2. pfCount: 估算单个和多个 HyperLogLog 的基数")
    void testPfCount() {
        System.out.println("--- 测试 pfCount ---");
        long count1 = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_1);
        System.out.println(HLL_KEY_1 + " 的基数估算值: " + count1);

        long count2 = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_2);
        System.out.println(HLL_KEY_2 + " 的基数估算值: " + count2);

        // 预期并集基数为 5 (user1, user2, user3, user4, user5)
        long unionCount = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_1, HLL_KEY_2);
        System.out.println(HLL_KEY_1 + " 和 " + HLL_KEY_2 + " 的并集基数估算值: " + unionCount);
    }

    @Test
    @Order(3)
    @DisplayName("3. pfMerge: 合并多个 HyperLogLog")
    void testPfMerge() {
        System.out.println("--- 测试 pfMerge ---");
        RedissonUtil.HyperLogLogOps.pfMerge(HLL_MERGED_KEY, HLL_KEY_1, HLL_KEY_2);
        System.out.println("已将 " + HLL_KEY_1 + " 和 " + HLL_KEY_2 + " 合并到 " + HLL_MERGED_KEY);

        long mergedCount = RedissonUtil.HyperLogLogOps.pfCount(HLL_MERGED_KEY);
        System.out.println("合并后的 " + HLL_MERGED_KEY + " 的基数估算值: " + mergedCount);
    }
}
