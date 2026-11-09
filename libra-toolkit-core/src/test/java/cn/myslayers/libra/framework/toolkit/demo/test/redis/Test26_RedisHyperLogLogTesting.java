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
@DisplayName("Redis HyperLogLog 基数统计功能测试")
public class Test26_RedisHyperLogLogTesting {

    private static final String HLL_KEY_1 = "hll:users:page1";
    private static final String HLL_KEY_2 = "hll:users:page2";
    private static final String HLL_KEY_MERGED = "hll:users:merged";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 HyperLogLog 测试 ======");
        // 清理可能存在的测试数据
        try {
            RedissonUtil.KeyOps.delete(HLL_KEY_1, HLL_KEY_2, HLL_KEY_MERGED);
        } catch (Exception e) {
            // 忽略删除错误
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            RedissonUtil.KeyOps.delete(HLL_KEY_1, HLL_KEY_2, HLL_KEY_MERGED);
        } catch (Exception e) {
            // 忽略删除错误
        }
        System.out.println("====== HyperLogLog 测试结束 ======");
    }

    @Test
    @Order(1)
    @DisplayName("1. pfAdd: 添加元素到 HyperLogLog")
    void testPfAdd() {
        System.out.println("--- 测试 HyperLogLog pfAdd ---");
        
        // 向第一个 HLL 添加用户ID
        boolean result1 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_1, "user1", "user2", "user3", "user4", "user5");
        System.out.println("向 " + HLL_KEY_1 + " 添加5个用户，结果: " + result1);
        
        // 再次添加相同的用户（应该不会增加基数）
        boolean result2 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_1, "user1", "user2", "user3");
        System.out.println("重复添加已存在的用户，结果: " + result2);
        
        // 添加新用户
        boolean result3 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_1, "user6", "user7");
        System.out.println("添加新用户 user6, user7，结果: " + result3);
        
        // 向第二个 HLL 添加用户ID（部分重复）
        boolean result4 = RedissonUtil.HyperLogLogOps.pfAdd(HLL_KEY_2, "user3", "user4", "user8", "user9", "user10");
        System.out.println("向 " + HLL_KEY_2 + " 添加5个用户（部分重复），结果: " + result4);
    }

    @Test
    @Order(2)
    @DisplayName("2. pfCount: 统计 HyperLogLog 基数")
    void testPfCount() {
        System.out.println("--- 测试 HyperLogLog pfCount ---");
        
        // 统计单个 HLL 的基数
        long count1 = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_1);
        System.out.println(HLL_KEY_1 + " 的基数估计值: " + count1);
        
        long count2 = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_2);
        System.out.println(HLL_KEY_2 + " 的基数估计值: " + count2);
        
        // 统计多个 HLL 的并集基数
        long unionCount = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_1, HLL_KEY_2);
        System.out.println(HLL_KEY_1 + " 和 " + HLL_KEY_2 + " 的并集基数估计值: " + unionCount);
        
        // 验证基数的合理性
        System.out.println("基数验证:");
        System.out.println("  " + HLL_KEY_1 + " 应该有7个不同用户 (user1-user7)");
        System.out.println("  " + HLL_KEY_2 + " 应该有5个不同用户 (user3,user4,user8,user9,user10)");
        System.out.println("  并集应该有10个不同用户 (user1-user10)");
    }

    @Test
    @Order(3)
    @DisplayName("3. pfMerge: 合并多个 HyperLogLog")
    void testPfMerge() {
        System.out.println("--- 测试 HyperLogLog pfMerge ---");
        
        // 合并两个 HLL 到新的 HLL
        RedissonUtil.HyperLogLogOps.pfMerge(HLL_KEY_MERGED, HLL_KEY_1, HLL_KEY_2);
        System.out.println("已将 " + HLL_KEY_1 + " 和 " + HLL_KEY_2 + " 合并到 " + HLL_KEY_MERGED);
        
        // 统计合并后的基数
        long mergedCount = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_MERGED);
        System.out.println("合并后的基数估计值: " + mergedCount);
        
        // 验证合并结果
        long originalUnionCount = RedissonUtil.HyperLogLogOps.pfCount(HLL_KEY_1, HLL_KEY_2);
        System.out.println("原始并集基数: " + originalUnionCount);
        System.out.println("合并后基数: " + mergedCount);
        System.out.println("两者应该相等或非常接近");
    }

    @Test
    @Order(4)
    @DisplayName("4. 大量数据测试: 验证 HyperLogLog 的准确性")
    void testLargeDataSet() {
        System.out.println("--- 大量数据测试 ---");
        
        String largeHllKey = "hll:large_test";
        
        try {
            // 清理测试数据
            RedissonUtil.KeyOps.delete(largeHllKey);
            
            // 添加大量不同的元素
            int totalElements = 10000;
            System.out.println("添加 " + totalElements + " 个不同元素...");
            
            for (int i = 0; i < totalElements; i += 100) {
                String[] batch = new String[100];
                for (int j = 0; j < 100; j++) {
                    batch[j] = "element_" + (i + j);
                }
                RedissonUtil.HyperLogLogOps.pfAdd(largeHllKey, (Object[]) batch);
            }
            
            // 统计基数
            long estimatedCount = RedissonUtil.HyperLogLogOps.pfCount(largeHllKey);
            System.out.println("实际元素数量: " + totalElements);
            System.out.println("HyperLogLog 估计数量: " + estimatedCount);
            
            // 计算误差率
            double errorRate = Math.abs(estimatedCount - totalElements) / (double) totalElements * 100;
            System.out.println("误差率: " + String.format("%.2f", errorRate) + "%");
            System.out.println("HyperLogLog 的标准误差率约为 0.81%");
            
            // 清理测试数据
            RedissonUtil.KeyOps.delete(largeHllKey);
            
        } catch (Exception e) {
            System.out.println("大量数据测试出现异常: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. 重复元素测试: 验证去重功能")
    void testDuplicateElements() {
        System.out.println("--- 重复元素测试 ---");
        
        String duplicateHllKey = "hll:duplicate_test";
        
        try {
            // 清理测试数据
            RedissonUtil.KeyOps.delete(duplicateHllKey);
            
            // 添加相同元素多次
            System.out.println("多次添加相同的元素...");
            for (int i = 0; i < 10; i++) {
                RedissonUtil.HyperLogLogOps.pfAdd(duplicateHllKey, "duplicate1", "duplicate2", "duplicate3");
                long count = RedissonUtil.HyperLogLogOps.pfCount(duplicateHllKey);
                System.out.println("第 " + (i + 1) + " 次添加后，基数: " + count);
            }
            
            // 最终基数应该仍然是3
            long finalCount = RedissonUtil.HyperLogLogOps.pfCount(duplicateHllKey);
            System.out.println("最终基数: " + finalCount + " (应该约等于3)");
            
            // 清理测试数据
            RedissonUtil.KeyOps.delete(duplicateHllKey);
            
        } catch (Exception e) {
            System.out.println("重复元素测试出现异常: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. 综合应用场景: 网站UV统计")
    void testWebsiteUVScenario() {
        System.out.println("--- 综合应用场景: 网站UV统计 ---");
        
        String dailyUvKey = "hll:uv:2025-07-26";
        String weeklyUvKey = "hll:uv:week30";
        String monthlyUvKey = "hll:uv:2025-07";
        
        try {
            // 清理测试数据
            RedissonUtil.KeyOps.delete(dailyUvKey, weeklyUvKey, monthlyUvKey);
            
            // 模拟一天的用户访问
            System.out.println("1. 模拟一天的用户访问:");
            String[] users = {"user001", "user002", "user003", "user001", "user004", "user002", "user005"};
            for (String user : users) {
                RedissonUtil.HyperLogLogOps.pfAdd(dailyUvKey, user);
                System.out.println("  用户 " + user + " 访问网站");
            }
            
            long dailyUv = RedissonUtil.HyperLogLogOps.pfCount(dailyUvKey);
            System.out.println("  今日UV: " + dailyUv + " (实际不重复用户: 5)");
            
            // 模拟周统计
            System.out.println("2. 模拟周统计:");
            RedissonUtil.HyperLogLogOps.pfAdd(weeklyUvKey, "user001", "user006", "user007", "user008");
            RedissonUtil.HyperLogLogOps.pfMerge(weeklyUvKey, dailyUvKey);
            
            long weeklyUv = RedissonUtil.HyperLogLogOps.pfCount(weeklyUvKey);
            System.out.println("  本周UV: " + weeklyUv);
            
            // 模拟月统计
            System.out.println("3. 模拟月统计:");
            RedissonUtil.HyperLogLogOps.pfAdd(monthlyUvKey, "user009", "user010", "user011");
            RedissonUtil.HyperLogLogOps.pfMerge(monthlyUvKey, weeklyUvKey);
            
            long monthlyUv = RedissonUtil.HyperLogLogOps.pfCount(monthlyUvKey);
            System.out.println("  本月UV: " + monthlyUv);
            
            // 内存使用情况
            System.out.println("4. HyperLogLog 优势:");
            System.out.println("  - 固定内存使用: 每个HLL约12KB");
            System.out.println("  - 支持大基数统计: 可统计2^64个不同元素");
            System.out.println("  - 标准误差: 约0.81%");
            System.out.println("  - 支持合并操作: 适合分布式统计");
            
            // 清理测试数据
            RedissonUtil.KeyOps.delete(dailyUvKey, weeklyUvKey, monthlyUvKey);
            
        } catch (Exception e) {
            System.out.println("UV统计场景测试出现异常: " + e.getMessage());
        }
    }
}
