package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil 服务器管理命令测试")
public class Test18_RedisServerTesting {

    private static final String TEST_DB_KEY = "db1_for_test";

    @BeforeAll
    static void setUp() {
        System.out.println("--- 开始 RedisUtil 服务器管理命令测试 ---");
        RedissonUtil.setDebugLogEnabled(true);
        // 确保测试在专用的测试数据库上进行
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(TEST_DB_KEY)) {
            accessor.ServerOps.flushDb();
            System.out.println("测试环境准备：已清空数据库 " + TEST_DB_KEY);
        }
    }

    @AfterAll
    static void tearDown() {
        // 清理测试数据
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(TEST_DB_KEY)) {
            accessor.ServerOps.flushDb();
            System.out.println("测试环境清理：已清空数据库 " + TEST_DB_KEY);
        }
        RedissonUtil.setDebugLogEnabled(false);
        System.out.println("--- 结束 RedisUtil 服务器管理命令测试 ---");
    }

    @Test
    @Order(1)
    @DisplayName("测试 PING 命令")
    void testPing() {
        System.out.println("--- 测试 1: ping ---");
        String result = RedissonUtil.ServerOps.ping();
        System.out.println("Ping 命令返回: " + result);
        if ("PONG".equalsIgnoreCase(result)) {
            System.out.println("验证成功: ping 按预期工作。");
        } else {
            System.out.println("验证失败: ping 返回值不是 PONG。");
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试 DBSIZE 命令")
    void testDbSize() {
        System.out.println("--- 测试 2: dbSize ---");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(TEST_DB_KEY)) {
            long initialSize = accessor.ServerOps.dbSize();
            System.out.println("初始 key 数量: " + initialSize);

            accessor.StringOps.set("test:server:dbsize1", "v1");
            accessor.StringOps.set("test:server:dbsize2", "v2");

            long newSize = accessor.ServerOps.dbSize();
            System.out.println("添加2个key后的数量: " + newSize);

            if (newSize == initialSize + 2) {
                System.out.println("验证成功: dbSize 按预期工作。");
            } else {
                System.out.println("验证失败: dbSize 未正确反映 key 的数量变化。");
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试 INFO 命令")
    void testInfo() {
        System.out.println("--- 测试 3: info ---");
        String info = RedissonUtil.ServerOps.info();
        System.out.println("获取到 INFO 信息 (部分): " + info.substring(0, Math.min(info.length(), 100)) + "...");
        if (info != null && info.contains("# Server")) {
            System.out.println("验证成功: info 命令成功获取服务器信息。");
        } else {
            System.out.println("验证失败: info 命令未能获取有效信息。");
        }

        String serverInfo = RedissonUtil.ServerOps.info("server");
        System.out.println("获取到 INFO SERVER 信息 (部分): " + serverInfo.substring(0, Math.min(serverInfo.length(), 100)) + "...");
        if (serverInfo != null && serverInfo.contains("redis_version")) {
            System.out.println("验证成功: info(section) 按预期工作。");
        } else {
            System.out.println("验证失败: info(section) 未能获取指定部分的信息。");
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试 CONFIG GET/SET 命令")
    void testConfig() {
        System.out.println("--- 测试 4: config get/set ---");
        String param = "timeout";
        Map<String, String> originalConfig = RedissonUtil.ServerOps.configGet(param);
        String originalValue = originalConfig.get(param);
        System.out.println("原始 '" + param + "' 配置: " + originalValue);

        String newValue = "200";
        RedissonUtil.ServerOps.configSet(param, newValue);
        System.out.println("设置 '" + param + "' 为: " + newValue);

        Map<String, String> newConfig = RedissonUtil.ServerOps.configGet(param);
        System.out.println("新的 '" + param + "' 配置: " + newConfig.get(param));

        if (newValue.equals(newConfig.get(param))) {
            System.out.println("验证成功: config set/get 按预期工作。");
        } else {
            System.out.println("验证失败: config set/get 未能正确修改和获取配置。");
        }

        // 恢复原始配置
        RedissonUtil.ServerOps.configSet(param, originalValue);
        System.out.println("恢复 '" + param + "' 为原始值: " + originalValue);
    }

    @Test
    @Order(5)
    @DisplayName("测试 FLUSHDB 命令")
    void testFlushDb() {
        System.out.println("--- 测试 5: flushDb (在测试数据库上) ---");
        try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor(TEST_DB_KEY)) {
            accessor.StringOps.set("test:server:flushkey", "some value");
            long sizeBeforeFlush = accessor.ServerOps.dbSize();
            System.out.println("清空前 key 数量: " + sizeBeforeFlush);

            if (sizeBeforeFlush > 0) {
                String result = accessor.ServerOps.flushDb();
                System.out.println("flushDb 命令返回: " + result);
                long sizeAfterFlush = accessor.ServerOps.dbSize();
                System.out.println("清空后 key 数量: " + sizeAfterFlush);

                if ("OK".equalsIgnoreCase(result) && sizeAfterFlush == 0) {
                    System.out.println("验证成功: flushDb 按预期工作。");
                } else {
                    System.out.println("验证失败: flushDb 未能清空数据库。");
                }
            } else {
                System.out.println("跳过验证，因为清空前数据库已为空。");
            }
        }
    }
}
