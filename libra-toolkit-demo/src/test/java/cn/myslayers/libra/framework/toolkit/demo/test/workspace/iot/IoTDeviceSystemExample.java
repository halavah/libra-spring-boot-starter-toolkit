package cn.myslayers.libra.framework.toolkit.demo.test.workspace.iot;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 演示案例：物联网设备监控系统 (IoT Monitor)
 * <p>
 * 场景特点：
 * 1. 高频写入：设备心跳和数据上报 (Redis)
 * 2. 状态管理：实时监控设备在线状态
 * 3. 持久化：定期归档数据到数据库
 * </p>
 */
@Slf4j
public class IoTDeviceSystemExample {

    // ================== 启动入口 ==================
    public static void main(String[] args) {
        initInfrastructure();

        PostmanUtil.setPort(8082);
        PostmanUtil.HttpCore.startServer(DeviceController.class);

        System.out.println("🌐 IoT Monitor System started at http://localhost:8082");
    }

    private static void initInfrastructure() {
        // 初始化 DB
        DatabaseUtil.DataSourceCore.register("iot_db",
                "jdbc:h2:mem:iot_db;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");
        DatabaseUtil.DataSourceCore.setDefaultDataSource("iot_db");

        // 设备表
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS devices (" +
                "device_id VARCHAR(50) PRIMARY KEY, " +
                "type VARCHAR(20), " +
                "location VARCHAR(100), " +
                "status VARCHAR(10), " +
                "last_active DATETIME)");

        // 告警日志表
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS alarms (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "device_id VARCHAR(50), " +
                "message VARCHAR(200), " +
                "created_at DATETIME)");

        // 初始化 Redis
        try {
            RedissonUtil.RedisDataCore.register("iot_redis", "127.0.0.1", 6379, "", 0);
            RedissonUtil.RedisDataCore.setDefaultKey("iot_redis");
        } catch (Exception ignored) {}
    }

    // Redis Accessor Helper
    private static RedissonUtil.RedisAccessor redis() {
        return RedissonUtil.RedisDataCore.getConfig("iot_redis").accessor();
    }

    // 辅助方法
    private static Map<String, Object> mapParams(Object... args) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            map.put("p" + (i + 1), args[i]);
        }
        return map;
    }

    // ================== DTOs ==================
    @Data
    static class DeviceRegDTO {
        private String deviceId;
        private String type;
        private String location;
        private String status;       // Added for insert
        private Date lastActive;     // Added for insert
    }

    @Data
    static class TelemetryDTO {
        private String deviceId;
        private Double temperature;
        private Double humidity;
        private Double battery;
    }

    // ================== Controller ==================
    @PostmanController("/api/iot")
    public static class DeviceController extends BaseController {

        /**
         * 设备注册 (写入 DB)
         */
        @PostmanMapping(value = "/devices/register", method = RequestMethod.POST)
        public Result<String> register() {
            DeviceRegDTO dto = getBody(DeviceRegDTO.class);
            dto.setStatus("OFFLINE");
            dto.setLastActive(new Date());

            DatabaseUtil.executeByMpEnhanced(
                    "INSERT INTO devices (device_id, type, location, status, last_active) VALUES (#{deviceId}, #{type}, #{location}, #{status}, #{lastActive})",
                    dto
            );
            return Result.ok("Device " + dto.deviceId + " registered.");
        }

        /**
         * 设备遥测数据上报 (高频接口 -> 写入 Redis)
         */
        @PostmanMapping(value = "/telemetry", method = RequestMethod.POST)
        public Result<String> reportTelemetry() {
            TelemetryDTO dto = getBody(TelemetryDTO.class);

            // 1. 更新 Redis 里的实时状态 (HashSet)
            // Key: iot:device:status, Field: deviceId, Value: JSON数据
            String cacheKey = "iot:device:status";
            String statusJson = String.format("{\"temp\":%.1f, \"hum\":%.1f, \"bat\":%.1f, \"ts\":%d}",
                    dto.temperature, dto.humidity, dto.battery, System.currentTimeMillis());

            try {
                redis().HashOps.hPut(cacheKey, dto.deviceId, statusJson);
                // 设置过期时间，模拟心跳超时（例如 60秒无上报则认为离线，需配合定期清理逻辑，此处简化）
            } catch (Exception ignored) {}

            // 2. 简单的规则引擎：温度超过 80 度触发告警
            if (dto.temperature > 80.0) {
                 Map<String, Object> alarm = new HashMap<>();
                 alarm.put("deviceId", dto.deviceId);
                 alarm.put("msg", "High Temperature Alarm: " + dto.temperature + "°C");
                 alarm.put("time", new Date());

                DatabaseUtil.executeByMp("INSERT INTO alarms (device_id, message, created_at) VALUES (#{deviceId}, #{msg}, #{time})",
                        alarm);
                return Result.ok("Data received (ALARM TRIGGERED)");
            }

            return Result.ok("Data received");
        }

        /**
         * 获取实时监控大屏数据 (从 Redis 读取)
         */
        @PostmanMapping(value = "/dashboard", method = RequestMethod.GET)
        public Result<Map<String, String>> dashboard() {
            try {
                // 获取所有在线设备的最新状态
                Map<String, String> allStatus = redis().HashOps.hGetAll("iot:device:status");
                return Result.ok(allStatus);
            } catch (Exception e) {
                return Result.ok(new HashMap<>());
            }
        }

        /**
         * 模拟生成设备数据 (辅助测试)
         */
        @PostmanMapping(value = "/simulate", method = RequestMethod.POST)
        public Result<String> simulate() {
            String deviceId = "SENSOR-" + RandomUtil.randomInt(1000, 9999);

            DeviceRegDTO dto = new DeviceRegDTO();
            dto.setDeviceId(deviceId);
            dto.setType("TEMP_SENSOR");
            dto.setLocation("Zone-A");
            dto.setStatus("ONLINE");
            dto.setLastActive(new Date());

            // 自动注册并上报一条
            // INSERT IGNORE not fully standard, trying normal insert, catch ignore
            try {
                DatabaseUtil.executeByMpEnhanced(
                    "INSERT INTO devices (device_id, type, location, status, last_active) VALUES (#{deviceId}, #{type}, #{location}, #{status}, #{lastActive})",
                    dto
                );
            } catch(Exception e) { /* ignore duplicate */ }

            // 上报 Redis
            String cacheKey = "iot:device:status";
            try {
                 redis().HashOps.hPut(cacheKey, deviceId, "{\"temp\":25.5, \"hum\":60.0, \"status\":\"OK\"}");
            } catch (Exception ignored) {}

            return Result.ok("Simulated " + deviceId);
        }

        /**
         * 获取设备列表
         */
        @PostmanMapping(value = "/devices", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> devices() {
            return Result.ok(DatabaseUtil.selectListByMp("SELECT * FROM devices", null));
        }

        /**
         * 获取告警记录
         */
        @PostmanMapping(value = "/alarms", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> alarms() {
            return Result.ok(DatabaseUtil.selectListByMp("SELECT * FROM alarms ORDER BY created_at DESC", null));
        }
    }
}
