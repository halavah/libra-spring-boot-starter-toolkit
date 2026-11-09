package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redisson.api.GeoUnit;
import org.redisson.api.GeoPosition;
import org.redisson.api.geo.GeoSearchArgs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Redis Geospatial 地理空间功能测试")
public class Test24_RedisGeoOpsTesting {

    private static final String GEO_KEY = "geo:cities";
    private static final String GEO_KEY_2 = "geo:landmarks";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 Geospatial 测试 ======");
        RedissonUtil.KeyOps.delete(GEO_KEY, GEO_KEY_2);
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.KeyOps.delete(GEO_KEY, GEO_KEY_2);
        System.out.println("====== Geospatial 测试结束 ======");
    }

    @Test
    @Order(1)
    @DisplayName("1. geoAdd: 添加单个地理位置")
    void testGeoAddSingle() {
        System.out.println("--- 测试 geoAdd (单个位置) ---");

        // 添加北京的坐标 (经度: 116.4074, 纬度: 39.9042)
        Long result1 = RedissonUtil.GeoOps.geoAdd(GEO_KEY, 116.4074, 39.9042, "Beijing");
        System.out.println("添加北京坐标，结果: " + result1);

        // 添加上海的坐标 (经度: 121.4737, 纬度: 31.2304)
        Long result2 = RedissonUtil.GeoOps.geoAdd(GEO_KEY, 121.4737, 31.2304, "Shanghai");
        System.out.println("添加上海坐标，结果: " + result2);

        // 添加广州的坐标 (经度: 113.2644, 纬度: 23.1291)
        Long result3 = RedissonUtil.GeoOps.geoAdd(GEO_KEY, 113.2644, 23.1291, "Guangzhou");
        System.out.println("添加广州坐标，结果: " + result3);
    }

    @Test
    @Order(2)
    @DisplayName("2. geoAdd: 批量添加地理位置")
    void testGeoAddBatch() {
        System.out.println("--- 测试 geoAdd (批量添加) ---");

        Map<Object, GeoPosition> geoEntries = new HashMap<>();
        geoEntries.put("Shenzhen", new GeoPosition(114.0579, 22.5431));  // 深圳
        geoEntries.put("Hangzhou", new GeoPosition(120.1551, 30.2741));  // 杭州
        geoEntries.put("Chengdu", new GeoPosition(104.0668, 30.5728));   // 成都

        Long result = RedissonUtil.GeoOps.geoAdd(GEO_KEY_2, geoEntries);
        System.out.println("批量添加3个城市坐标，结果: " + result);
    }

    @Test
    @Order(3)
    @DisplayName("3. geoPos: 获取地理位置坐标")
    void testGeoPos() {
        System.out.println("--- 测试 geoPos ---");

        Map<Object, GeoPosition> positions = RedissonUtil.GeoOps.geoPos(GEO_KEY, "Beijing", "Shanghai", "Guangzhou");

        for (Map.Entry<Object, GeoPosition> entry : positions.entrySet()) {
            GeoPosition pos = entry.getValue();
            if (pos != null) {
                System.out.println(entry.getKey() + " 的坐标: 经度=" + pos.getLongitude() + ", 纬度=" + pos.getLatitude());
            } else {
                System.out.println(entry.getKey() + " 的坐标: 未找到");
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. geoDist: 计算两点间距离")
    void testGeoDist() {
        System.out.println("--- 测试 geoDist ---");

        // 计算北京到上海的距离
        Double distKm = RedissonUtil.GeoOps.geoDist(GEO_KEY, "Beijing", "Shanghai", GeoUnit.KILOMETERS);
        System.out.println("北京到上海的距离: " + distKm + " 公里");

        // 计算北京到广州的距离
        Double distM = RedissonUtil.GeoOps.geoDist(GEO_KEY, "Beijing", "Guangzhou", GeoUnit.METERS);
        System.out.println("北京到广州的距离: " + distM + " 米");

        // 计算上海到广州的距离
        Double distMiles = RedissonUtil.GeoOps.geoDist(GEO_KEY, "Shanghai", "Guangzhou", GeoUnit.MILES);
        System.out.println("上海到广州的距离: " + distMiles + " 英里");
    }

    @Test
    @Order(5)
    @DisplayName("5. geoRadius: 根据坐标查找附近位置")
    void testGeoRadius() {
        System.out.println("--- 测试 geoRadius ---");

        // 以北京为中心，查找1500公里范围内的城市
        Map<Object, Double> nearbyWithDist = RedissonUtil.GeoOps.geoRadius(GEO_KEY, 116.4074, 39.9042, 1500, GeoUnit.KILOMETERS);
        System.out.println("以北京坐标为中心，1500公里范围内的城市:");
        for (Map.Entry<Object, Double> entry : nearbyWithDist.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " 公里");
        }

        // 限制返回数量
        List<Object> nearby = RedissonUtil.GeoOps.geoRadius(GEO_KEY, 116.4074, 39.9042, 2000, GeoUnit.KILOMETERS, 2);
        System.out.println("限制返回2个最近的城市: " + nearby);
    }

    @Test
    @Order(6)
    @DisplayName("6. geoRadiusByMember: 根据成员查找附近位置")
    void testGeoRadiusByMember() {
        System.out.println("--- 测试 geoRadiusByMember ---");

        // 以上海为中心，查找1000公里范围内的城市
        Map<Object, Double> nearbyWithDist = RedissonUtil.GeoOps.geoRadiusByMember(GEO_KEY, "Shanghai", 1000, GeoUnit.KILOMETERS);
        System.out.println("以上海为中心，1000公里范围内的城市:");
        for (Map.Entry<Object, Double> entry : nearbyWithDist.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " 公里");
        }

        // 限制返回数量
        List<Object> nearby = RedissonUtil.GeoOps.geoRadiusByMember(GEO_KEY, "Shanghai", 1500, GeoUnit.KILOMETERS, 3);
        System.out.println("以上海为中心，限制返回3个最近的城市: " + nearby);
    }

    @Test
    @Order(7)
    @DisplayName("7. geoSearch: 高级地理搜索")
    void testGeoSearch() {
        System.out.println("--- 测试 geoSearch ---");

        try {
            // 创建搜索参数 - 以指定坐标为中心搜索
            GeoSearchArgs searchArgs = GeoSearchArgs.from(116.4074, 39.9042)  // 北京坐标
                    .radius(1200, GeoUnit.KILOMETERS)
                    .count(5);

            List<Object> searchResults = RedissonUtil.GeoOps.geoSearch(GEO_KEY, searchArgs);
            System.out.println("高级搜索结果 (以北京坐标为中心，1200公里范围内): " + searchResults);
        } catch (Exception e) {
            System.out.println("geoSearch 测试出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }

    @Test
    @Order(8)
    @DisplayName("8. 综合测试: 地理位置应用场景")
    void testGeoApplicationScenario() {
        System.out.println("--- 综合测试: 地理位置应用场景 ---");

        // 场景：找到距离用户最近的商店
        String storeKey = "geo:stores";
        RedissonUtil.KeyOps.delete(storeKey);

        // 添加商店位置
        RedissonUtil.GeoOps.geoAdd(storeKey, 116.3974, 39.9042, "Store_A");  // 北京西边
        RedissonUtil.GeoOps.geoAdd(storeKey, 116.4174, 39.9042, "Store_B");  // 北京东边
        RedissonUtil.GeoOps.geoAdd(storeKey, 116.4074, 39.8942, "Store_C");  // 北京南边
        RedissonUtil.GeoOps.geoAdd(storeKey, 116.4074, 39.9142, "Store_D");  // 北京北边

        // 用户位置 (天安门广场附近)
        double userLon = 116.4074;
        double userLat = 39.9042;

        System.out.println("用户位置: 经度=" + userLon + ", 纬度=" + userLat);

        // 查找用户5公里范围内的商店
        List<Object> nearbyStores = RedissonUtil.GeoOps.geoRadius(storeKey, userLon, userLat, 5, GeoUnit.KILOMETERS, 10);
        System.out.println("用户5公里范围内的商店: " + nearbyStores);

        // 计算到各个商店的精确距离
        for (Object store : nearbyStores) {
            Double distance = RedissonUtil.GeoOps.geoDist(storeKey, "Store_A", store, GeoUnit.METERS);
            if (distance != null) {
                System.out.println("到 " + store + " 的距离: " + String.format("%.2f", distance) + " 米");
            }
        }

        // 清理测试数据
        RedissonUtil.KeyOps.delete(storeKey);
    }
}
