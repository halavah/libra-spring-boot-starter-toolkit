package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil Pub/Sub 功能测试")
public class Test21_RedisPubSubTesting {

    private static final String TEST_TOPIC = "test-topic-chat";

    @BeforeAll
    static void setUp() {
        System.out.println("--- 开始 RedisUtil Pub/Sub 功能测试 ---");
        RedissonUtil.setDebugLogEnabled(true);
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.setDebugLogEnabled(false);
        System.out.println("--- 结束 RedisUtil Pub/Sub 功能测试 ---");
    }

    // 自定义消息对象，必须可序列化
    static class MyMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        public String content;

        public MyMessage(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "MyMessage{" + "content='" + content + '\'' + '}';
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试消息发布与订阅")
    void testPublishAndSubscribe() throws InterruptedException {
        System.out.println("--- 测试 1: 消息发布与订阅 ---");
        final CountDownLatch latch = new CountDownLatch(1);
        final MyMessage receivedMessage = new MyMessage(null);

        RedissonUtil.MessageListener<MyMessage> listener = (channel, msg) -> {
            System.out.println("接收到消息 -> Channel: " + channel + ", Message: " + msg);
            receivedMessage.content = msg.content;
            latch.countDown();
        };

        int listenerId = RedissonUtil.PubSubOps.subscribe(TEST_TOPIC, listener);
        System.out.println("已订阅主题 '" + TEST_TOPIC + "'，监听器ID: " + listenerId);

        MyMessage messageToSend = new MyMessage("Hello, Redis Pub/Sub!");
        System.out.println("准备发布消息: " + messageToSend);
        RedissonUtil.PubSubOps.publish(TEST_TOPIC, messageToSend);

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);

        if (messageReceived && messageToSend.content.equals(receivedMessage.content)) {
            System.out.println("验证成功: 订阅者成功接收到发布的消息。");
        } else {
            System.out.println("验证失败: 未在规定时间内接收到正确的消息。");
        }

        // 清理
        RedissonUtil.PubSubOps.unsubscribe(TEST_TOPIC, listenerId);
        System.out.println("已取消订阅，监听器ID: " + listenerId);
    }

    @Test
    @Order(2)
    @DisplayName("测试取消订阅")
    void testUnsubscribe() throws InterruptedException {
        System.out.println("--- 测试 2: 取消订阅 ---");
        final CountDownLatch latch = new CountDownLatch(1);

        RedissonUtil.MessageListener<String> listener = (channel, msg) -> {
            System.out.println("这是一个不应该被触发的监听器！");
            latch.countDown(); // 如果被触发，latch 会减一
        };

        int listenerId = RedissonUtil.PubSubOps.subscribe(TEST_TOPIC + ":unsubscribe", listener);
        System.out.println("已订阅主题，监听器ID: " + listenerId);

        RedissonUtil.PubSubOps.unsubscribe(TEST_TOPIC + ":unsubscribe", listenerId);
        System.out.println("立即取消订阅，监听器ID: " + listenerId);

        RedissonUtil.PubSubOps.publish(TEST_TOPIC + ":unsubscribe", "This message should not be received.");
        System.out.println("已发布一条消息到已取消订阅的主题。");

        // 等待一段时间，确保消息没有被接收
        boolean listenerTriggered = latch.await(2, TimeUnit.SECONDS);

        if (!listenerTriggered) {
            System.out.println("验证成功: 取消订阅后，监听器未收到消息。");
        } else {
            System.out.println("验证失败: 取消订阅后，监听器仍然收到了消息。");
        }
    }
}
