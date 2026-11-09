package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Redis 布隆过滤器功能测试
 *
 * <p>测试 RedisUtil 中布隆过滤器的各种操作，包括：
 * <ul>
 *   <li>布隆过滤器初始化</li>
 *   <li>元素添加和检查</li>
 *   <li>配置参数获取</li>
 *   <li>链式 API 和顶层静态 API 对比</li>
 * </ul>
 */
public class Test27_RedisBloomFilterTesting {

    @BeforeAll
    static void setUp() {
        // 启用调试日志
        RedissonUtil.setDebugLogEnabled(true);
        System.out.println("=== Redis 布隆过滤器功能测试开始 ===");
    }

    @Test
    @Order(1)
    @DisplayName("测试布隆过滤器初始化")
    void testBloomFilterInitialization() {
        System.out.println("\n--- 测试布隆过滤器初始化 ---");

        String key = "test:bloom:init";
        long expectedInsertions = 10000;
        double falseProbability = 0.01;

        // 使用链式 API 初始化布隆过滤器
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            boolean result = accessor.BloomFilterOps.tryInit(key, expectedInsertions, falseProbability);
            System.out.println("链式 API 初始化结果: " + result);

            // 获取配置参数
            long actualExpectedInsertions = accessor.BloomFilterOps.getExpectedInsertions(key);
            double actualFalseProbability = accessor.BloomFilterOps.getFalseProbability(key);
            long hashIterations = accessor.BloomFilterOps.getHashIterations(key);
            long size = accessor.BloomFilterOps.getSize(key);

            System.out.println("预期插入数量: " + actualExpectedInsertions);
            System.out.println("误判率: " + actualFalseProbability);
            System.out.println("哈希函数数量: " + hashIterations);
            System.out.println("位数组大小: " + size);
        }

        // 使用顶层静态 API 初始化另一个布隆过滤器
        String key2 = "test:bloom:init2";
        boolean result2 = RedissonUtil.BloomFilterOps.tryInit(key2, expectedInsertions, falseProbability);
        System.out.println("顶层静态 API 初始化结果: " + result2);

        // 获取配置参数
        long actualExpectedInsertions2 = RedissonUtil.BloomFilterOps.getExpectedInsertions(key2);
        double actualFalseProbability2 = RedissonUtil.BloomFilterOps.getFalseProbability(key2);
        System.out.println("顶层静态 API - 预期插入数量: " + actualExpectedInsertions2);
        System.out.println("顶层静态 API - 误判率: " + actualFalseProbability2);
    }

    @Test
    @Order(2)
    @DisplayName("测试布隆过滤器元素添加和检查")
    void testBloomFilterAddAndContains() {
        System.out.println("\n--- 测试布隆过滤器元素添加和检查 ---");

        String key = "test:bloom:elements";

        // 初始化布隆过滤器
        RedissonUtil.BloomFilterOps.tryInit(key, 1000, 0.01);

        // 测试数据
        String[] testElements = {"user:1001", "user:1002", "user:1003", "user:1004", "user:1005"};
        String[] nonExistentElements = {"user:9001", "user:9002", "user:9003"};

        // 使用链式 API 添加元素
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
            System.out.println("使用链式 API 添加元素:");
            for (String element : testElements) {
                boolean added = accessor.BloomFilterOps.add(key, element);
                System.out.println("添加元素 " + element + ": " + added);
            }

            System.out.println("\n检查已添加的元素:");
            for (String element : testElements) {
                boolean contains = accessor.BloomFilterOps.contains(key, element);
                System.out.println("元素 " + element + " 是否存在: " + contains);
            }

            System.out.println("\n检查未添加的元素:");
            for (String element : nonExistentElements) {
                boolean contains = accessor.BloomFilterOps.contains(key, element);
                System.out.println("元素 " + element + " 是否存在: " + contains);
            }

            // 获取当前元素数量
            long count = accessor.BloomFilterOps.count(key);
            System.out.println("当前布隆过滤器中的元素数量: " + count);
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试顶层静态 API 布隆过滤器操作")
    void testTopLevelBloomFilterOperations() {
        System.out.println("\n--- 测试顶层静态 API 布隆过滤器操作 ---");

        String key = "test:bloom:static";

        // 初始化布隆过滤器
        boolean initResult = RedissonUtil.BloomFilterOps.tryInit(key, 5000, 0.001);
        System.out.println("初始化结果: " + initResult);

        // 添加一批元素
        String[] products = {"product:A001", "product:A002", "product:A003", "product:B001", "product:B002"};

        System.out.println("\n添加产品元素:");
        for (String product : products) {
            boolean added = RedissonUtil.BloomFilterOps.add(key, product);
            System.out.println("添加产品 " + product + ": " + added);
        }

        // 检查元素存在性
        System.out.println("\n检查产品存在性:");
        String[] checkProducts = {"product:A001", "product:A999", "product:B001", "product:C001"};
        for (String product : checkProducts) {
            boolean exists = RedissonUtil.BloomFilterOps.contains(key, product);
            System.out.println("产品 " + product + " 是否存在: " + exists);
        }

        // 获取布隆过滤器统计信息
        System.out.println("\n布隆过滤器统计信息:");
        System.out.println("预期插入数量: " + RedissonUtil.BloomFilterOps.getExpectedInsertions(key));
        System.out.println("误判率: " + RedissonUtil.BloomFilterOps.getFalseProbability(key));
        System.out.println("哈希函数数量: " + RedissonUtil.BloomFilterOps.getHashIterations(key));
        System.out.println("位数组大小: " + RedissonUtil.BloomFilterOps.getSize(key));
        System.out.println("当前元素数量: " + RedissonUtil.BloomFilterOps.count(key));
    }

    @Test
    @Order(4)
    @DisplayName("测试布隆过滤器性能和误判率")
    void testBloomFilterPerformanceAndFalsePositive() {
        System.out.println("\n--- 测试布隆过滤器性能和误判率 ---");

        String key = "test:bloom:performance";
        long expectedInsertions = 10000;
        double falseProbability = 0.01;

        // 初始化布隆过滤器
        RedissonUtil.BloomFilterOps.tryInit(key, expectedInsertions, falseProbability);

        // 添加大量元素
        System.out.println("添加 1000 个元素...");
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= 1000; i++) {
            String element = "element:" + i;
            RedissonUtil.BloomFilterOps.add(key, element);
        }

        long addTime = System.currentTimeMillis() - startTime;
        System.out.println("添加 1000 个元素耗时: " + addTime + " ms");

        // 检查已添加元素的存在性
        System.out.println("\n检查已添加元素的存在性...");
        startTime = System.currentTimeMillis();
        int truePositives = 0;

        for (int i = 1; i <= 1000; i++) {
            String element = "element:" + i;
            if (RedissonUtil.BloomFilterOps.contains(key, element)) {
                truePositives++;
            }
        }

        long checkTime = System.currentTimeMillis() - startTime;
        System.out.println("检查 1000 个已添加元素耗时: " + checkTime + " ms");
        System.out.println("真正例数量: " + truePositives + "/1000");

        // 检查未添加元素的误判率
        System.out.println("\n检查未添加元素的误判率...");
        int falsePositives = 0;

        for (int i = 10001; i <= 11000; i++) {
            String element = "element:" + i;
            if (RedissonUtil.BloomFilterOps.contains(key, element)) {
                falsePositives++;
            }
        }

        double actualFalsePositiveRate = (double) falsePositives / 1000;
        System.out.println("误判数量: " + falsePositives + "/1000");
        System.out.println("实际误判率: " + String.format("%.4f", actualFalsePositiveRate));
        System.out.println("预期误判率: " + falseProbability);

        // 获取最终统计信息
        System.out.println("\n最终统计信息:");
        System.out.println("当前元素数量: " + RedissonUtil.BloomFilterOps.count(key));
    }

    @Test
    @Order(5)
    @DisplayName("测试不同数据类型的布隆过滤器")
    void testBloomFilterWithDifferentDataTypes() {
        System.out.println("\n--- 测试不同数据类型的布隆过滤器 ---");

        String key = "test:bloom:datatypes";
        RedissonUtil.BloomFilterOps.tryInit(key, 1000, 0.01);

        // 测试不同类型的数据
        System.out.println("添加不同类型的数据:");

        // 字符串
        RedissonUtil.BloomFilterOps.add(key, "string_value");
        System.out.println("添加字符串: string_value");

        // 数字
        RedissonUtil.BloomFilterOps.add(key, 12345);
        System.out.println("添加整数: 12345");

        RedissonUtil.BloomFilterOps.add(key, 123.45);
        System.out.println("添加浮点数: 123.45");

        // 检查存在性
        System.out.println("\n检查不同类型数据的存在性:");
        System.out.println("字符串 'string_value' 存在: " + RedissonUtil.BloomFilterOps.contains(key, "string_value"));
        System.out.println("整数 12345 存在: " + RedissonUtil.BloomFilterOps.contains(key, 12345));
        System.out.println("浮点数 123.45 存在: " + RedissonUtil.BloomFilterOps.contains(key, 123.45));

        // 检查不存在的数据
        System.out.println("字符串 'not_exist' 存在: " + RedissonUtil.BloomFilterOps.contains(key, "not_exist"));
        System.out.println("整数 99999 存在: " + RedissonUtil.BloomFilterOps.contains(key, 99999));
    }
}
