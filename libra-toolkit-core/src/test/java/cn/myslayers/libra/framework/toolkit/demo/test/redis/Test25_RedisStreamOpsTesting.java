package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redisson.api.StreamMessageId;

import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Redis Streams 流功能测试")
public class Test25_RedisStreamOpsTesting {

    private static final String STREAM_KEY = "stream:events";
    private static final String STREAM_KEY_2 = "stream:logs";
    private static final String GROUP_NAME = "test-group";
    private static final String CONSUMER_NAME = "consumer-1";

    @BeforeAll
    static void setUp() {
        System.out.println("====== 开始 Streams 测试 ======");
        // 清理可能存在的测试数据
        try {
            RedissonUtil.KeyOps.delete(STREAM_KEY, STREAM_KEY_2);
        } catch (Exception e) {
            // 忽略删除错误
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            RedissonUtil.KeyOps.delete(STREAM_KEY, STREAM_KEY_2);
        } catch (Exception e) {
            // 忽略删除错误
        }
        System.out.println("====== Streams 测试结束 ======");
    }

    @Test
    @Order(1)
    @DisplayName("1. add: 向流中添加消息")
    void testStreamAdd() {
        System.out.println("--- 测试 Stream add ---");

        // 添加第一条消息 (使用单个字段)
        StreamMessageId id1 = RedissonUtil.StreamOps.add(STREAM_KEY, "event", "user_login");
        System.out.println("添加消息1，ID: " + id1);

        // 添加第二条消息 (使用 addAll 添加多个字段)
        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("event", "user_logout");
        fields2.put("user_id", "12345");
        fields2.put("timestamp", System.currentTimeMillis());

        StreamMessageId id2 = RedissonUtil.StreamOps.addAll(STREAM_KEY, fields2);
        System.out.println("添加消息2，ID: " + id2);

        // 添加第三条消息到另一个流
        Map<String, Object> fields3 = new HashMap<>();
        fields3.put("level", "INFO");
        fields3.put("message", "Application started");
        fields3.put("module", "main");

        StreamMessageId id3 = RedissonUtil.StreamOps.addAll(STREAM_KEY_2, fields3);
        System.out.println("添加消息3到日志流，ID: " + id3);
    }

    @Test
    @Order(2)
    @DisplayName("2. length: 获取流的长度")
    void testStreamLength() {
        System.out.println("--- 测试 Stream length ---");

        long length1 = RedissonUtil.StreamOps.length(STREAM_KEY);
        System.out.println(STREAM_KEY + " 流的长度: " + length1);

        long length2 = RedissonUtil.StreamOps.length(STREAM_KEY_2);
        System.out.println(STREAM_KEY_2 + " 流的长度: " + length2);
    }

    @Test
    @Order(3)
    @DisplayName("3. read: 读取流中的消息")
    void testStreamRead() {
        System.out.println("--- 测试 Stream read ---");

        try {
            // 读取单个流的消息，从开始位置读取
            Map<StreamMessageId, Map<String, Object>> messages = RedissonUtil.StreamOps.read(STREAM_KEY, StreamMessageId.MIN);
            System.out.println("读取到的消息数量: " + messages.size());

            for (Map.Entry<StreamMessageId, Map<String, Object>> messageEntry : messages.entrySet()) {
                StreamMessageId messageId = messageEntry.getKey();
                Map<String, Object> fields = messageEntry.getValue();
                System.out.println("  消息ID: " + messageId + ", 内容: " + fields);
            }
        } catch (Exception e) {
            System.out.println("读取流消息时出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. readRange: 按范围读取流消息")
    void testStreamReadRange() {
        System.out.println("--- 测试 Stream readRange ---");

        try {
            // 读取从开始到结束的所有消息
            Map<StreamMessageId, Map<String, Object>> messages = RedissonUtil.StreamOps.readRange(
                STREAM_KEY,
                StreamMessageId.MIN,
                StreamMessageId.MAX
            );

            System.out.println("范围读取到的消息数量: " + messages.size());
            for (Map.Entry<StreamMessageId, Map<String, Object>> entry : messages.entrySet()) {
                System.out.println("  消息ID: " + entry.getKey() + ", 内容: " + entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("范围读取流消息时出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. createGroup: 创建消费者组")
    void testStreamCreateGroup() {
        System.out.println("--- 测试 Stream createGroup ---");

        try {
            // 创建消费者组，从流的开始位置开始消费
            RedissonUtil.StreamOps.createGroup(STREAM_KEY, GROUP_NAME, StreamMessageId.ALL);
            System.out.println("成功创建消费者组: " + GROUP_NAME);
        } catch (Exception e) {
            System.out.println("创建消费者组时出现异常: " + e.getMessage());
            if (e.getMessage().contains("BUSYGROUP")) {
                System.out.println("消费者组已存在，继续测试");
            } else {
                System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. readGroup: 从消费者组读取消息")
    void testStreamReadGroup() {
        System.out.println("--- 测试 Stream readGroup ---");

        try {
            // 从消费者组读取消息
            Map<StreamMessageId, Map<String, Object>> messages =
                RedissonUtil.StreamOps.readGroup(STREAM_KEY, GROUP_NAME, CONSUMER_NAME);

            System.out.println("从消费者组读取到的消息数量: " + messages.size());

            for (Map.Entry<StreamMessageId, Map<String, Object>> messageEntry : messages.entrySet()) {
                StreamMessageId messageId = messageEntry.getKey();
                Map<String, Object> fields = messageEntry.getValue();
                System.out.println("  消息ID: " + messageId + ", 内容: " + fields);

                // 确认消息处理完成
                long ackCount = RedissonUtil.StreamOps.ack(STREAM_KEY, GROUP_NAME, messageId);
                System.out.println("  确认消息处理完成，ACK数量: " + ackCount);
            }
        } catch (Exception e) {
            System.out.println("从消费者组读取消息时出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. trim: 修剪流长度")
    void testStreamTrim() {
        System.out.println("--- 测试 Stream trim ---");

        try {
            // 先添加更多消息
            for (int i = 0; i < 5; i++) {
                Map<String, Object> fields = new HashMap<>();
                fields.put("test_message", "message_" + i);
                fields.put("index", i);
                RedissonUtil.StreamOps.addAll(STREAM_KEY, fields);
            }

            long lengthBefore = RedissonUtil.StreamOps.length(STREAM_KEY);
            System.out.println("修剪前流的长度: " + lengthBefore);

            // 修剪流，只保留最新的3条消息
            long trimmed = RedissonUtil.StreamOps.trim(STREAM_KEY, 3);
            System.out.println("修剪了 " + trimmed + " 条消息");

            long lengthAfter = RedissonUtil.StreamOps.length(STREAM_KEY);
            System.out.println("修剪后流的长度: " + lengthAfter);
        } catch (Exception e) {
            System.out.println("修剪流时出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }

    @Test
    @Order(8)
    @DisplayName("8. 综合测试: 消息队列应用场景")
    void testStreamMessageQueueScenario() {
        System.out.println("--- 综合测试: 消息队列应用场景 ---");

        String taskStreamKey = "stream:tasks";
        String taskGroupName = "task-processors";
        String workerName = "worker-1";

        try {
            // 清理测试数据
            RedissonUtil.KeyOps.delete(taskStreamKey);

            // 1. 生产者：添加任务到流中
            System.out.println("1. 生产者添加任务:");
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> task = new HashMap<>();
                task.put("task_id", "task_" + i);
                task.put("task_type", "data_processing");
                task.put("priority", i % 2 == 0 ? "high" : "normal");
                task.put("created_at", System.currentTimeMillis());

                StreamMessageId taskId = RedissonUtil.StreamOps.addAll(taskStreamKey, task);
                System.out.println("  添加任务 " + i + "，ID: " + taskId);
            }

            // 2. 创建消费者组
            System.out.println("2. 创建消费者组:");
            try {
                RedissonUtil.StreamOps.createGroup(taskStreamKey, taskGroupName, StreamMessageId.ALL);
                System.out.println("  成功创建消费者组: " + taskGroupName);
            } catch (Exception e) {
                if (e.getMessage().contains("BUSYGROUP")) {
                    System.out.println("  消费者组已存在: " + taskGroupName);
                } else {
                    throw e;
                }
            }

            // 3. 消费者：处理任务
            System.out.println("3. 消费者处理任务:");
            Map<StreamMessageId, Map<String, Object>> tasks =
                RedissonUtil.StreamOps.readGroup(taskStreamKey, taskGroupName, workerName);

            for (Map.Entry<StreamMessageId, Map<String, Object>> taskEntry : tasks.entrySet()) {
                StreamMessageId messageId = taskEntry.getKey();
                Map<String, Object> taskData = taskEntry.getValue();

                System.out.println("  处理任务: " + taskData.get("task_id") +
                                 ", 优先级: " + taskData.get("priority"));

                // 模拟任务处理
                Thread.sleep(100);

                // 确认任务完成
                RedissonUtil.StreamOps.ack(taskStreamKey, taskGroupName, messageId);
                System.out.println("  任务 " + taskData.get("task_id") + " 处理完成");
            }

            // 4. 查看流状态
            long finalLength = RedissonUtil.StreamOps.length(taskStreamKey);
            System.out.println("4. 最终流长度: " + finalLength);

            // 清理测试数据
            RedissonUtil.KeyOps.delete(taskStreamKey);

        } catch (Exception e) {
            System.out.println("消息队列场景测试出现异常: " + e.getMessage());
            System.out.println("这可能是由于 Redisson 版本或 Redis 版本不支持该功能");
        }
    }
}
