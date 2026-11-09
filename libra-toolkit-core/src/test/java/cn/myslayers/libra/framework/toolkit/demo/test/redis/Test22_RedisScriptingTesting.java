package cn.myslayers.libra.framework.toolkit.demo.test.redis;

import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import io.lettuce.core.ScriptOutputType;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RedisUtil Scripting 功能测试")
public class Test22_RedisScriptingTesting {

    private static final String TEST_KEY_1 = "test:script:key1";
    private static final String TEST_KEY_2 = "test:script:key2";

    @BeforeAll
    static void setUp() {
        System.out.println("--- 开始 RedisUtil Scripting 功能测试 ---");
        RedissonUtil.setDebugLogEnabled(true);
        RedissonUtil.KeyOps.delete(TEST_KEY_1, TEST_KEY_2);
        // 清空脚本缓存以确保测试环境纯净
        RedissonUtil.ScriptOps.scriptFlush();
        System.out.println("测试环境准备：已删除测试键并清空脚本缓存。");
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.KeyOps.delete(TEST_KEY_1, TEST_KEY_2);
        RedissonUtil.setDebugLogEnabled(false);
        System.out.println("--- 结束 RedisUtil Scripting 功能测试 ---");
    }

    @Test
    @Order(1)
    @DisplayName("测试 EVAL 执行简单脚本")
    void testEval() {
        System.out.println("--- 测试 1: EVAL 执行简单脚本 ---");
        String script = "return 'Hello, Redis Scripting!'";
        String result = RedissonUtil.ScriptOps.eval(script, ScriptOutputType.VALUE, new String[0]);
        System.out.println("EVAL 返回: " + result);

        if ("Hello, Redis Scripting!".equals(result)) {
            System.out.println("验证成功: EVAL 成功执行并返回字符串。");
        } else {
            System.out.println("验证失败: EVAL 返回值不符合预期。");
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试 EVAL 执行带参数的脚本")
    void testEvalWithArgs() {
        System.out.println("--- 测试 2: EVAL 执行带参数的脚本 ---");
        String script = "return redis.call('set', KEYS[1], ARGV[1])";
        String result = RedissonUtil.ScriptOps.eval(script, ScriptOutputType.STATUS, new String[]{TEST_KEY_1}, "value_from_script");
        System.out.println("EVAL SET 脚本返回: " + result);

        String value = RedissonUtil.StringOps.get(TEST_KEY_1);
        System.out.println("获取 " + TEST_KEY_1 + " 的值: " + value);

        if ("OK".equalsIgnoreCase(result) && "value_from_script".equals(value)) {
            System.out.println("验证成功: 带参数的 EVAL 脚本成功执行。");
        } else {
            System.out.println("验证失败: 带参数的 EVAL 脚本执行未达到预期效果。");
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试脚本加载 (SCRIPT LOAD) 和执行 (EVALSHA)")
    void testScriptLoadAndEvalSha() {
        System.out.println("--- 测试 3: SCRIPT LOAD 和 EVALSHA ---");
        String script = "return {KEYS[1], ARGV[1]}";
        String sha1 = RedissonUtil.ScriptOps.scriptLoad(script);
        System.out.println("脚本加载后返回的 SHA1: " + sha1);

        if (sha1 != null && sha1.length() == 40) {
            System.out.println("验证成功: SCRIPT LOAD 返回了有效的 SHA1。");
        } else {
            System.out.println("验证失败: SCRIPT LOAD 未返回有效的 SHA1。");
            return;
        }

        List<String> result = RedissonUtil.ScriptOps.evalSha(sha1, ScriptOutputType.MULTI, new String[]{TEST_KEY_2}, "value_from_sha");
        System.out.println("EVALSHA 返回: " + result);

        if (result != null && result.size() == 2 && TEST_KEY_2.equals(result.get(0)) && "value_from_sha".equals(result.get(1))) {
            System.out.println("验证成功: EVALSHA 成功执行了缓存的脚本。");
        } else {
            System.out.println("验证失败: EVALSHA 执行结果不符合预期。");
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试脚本存在性检查 (SCRIPT EXISTS)")
    void testScriptExists() {
        System.out.println("--- 测试 4: SCRIPT EXISTS ---");
        String script = "return 123";
        String sha1 = RedissonUtil.ScriptOps.scriptLoad(script);
        System.out.println("加载脚本以获取 SHA1: " + sha1);

        List<Boolean> exists = RedissonUtil.ScriptOps.scriptExists(sha1, "non_existent_sha1");
        System.out.println("SCRIPT EXISTS 返回: " + exists);

        if (exists != null && exists.size() == 2 && exists.get(0) && !exists.get(1)) {
            System.out.println("验证成功: SCRIPT EXISTS 正确地检查了脚本的存在性。");
        } else {
            System.out.println("验证失败: SCRIPT EXISTS 返回结果不符合预期。");
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试脚本清空 (SCRIPT FLUSH)")
    void testScriptFlush() {
        System.out.println("--- 测试 5: SCRIPT FLUSH ---");
        String script = "return 'another script'";
        String sha1 = RedissonUtil.ScriptOps.scriptLoad(script);
        System.out.println("加载脚本以获取 SHA1: " + sha1);

        boolean existsBeforeFlush = RedissonUtil.ScriptOps.scriptExists(sha1).get(0);
        System.out.println("清空前脚本是否存在: " + existsBeforeFlush);

        String flushResult = RedissonUtil.ScriptOps.scriptFlush();
        System.out.println("SCRIPT FLUSH 命令返回: " + flushResult);

        boolean existsAfterFlush = RedissonUtil.ScriptOps.scriptExists(sha1).get(0);
        System.out.println("清空后脚本是否存在: " + existsAfterFlush);

        if (existsBeforeFlush && "OK".equalsIgnoreCase(flushResult) && !existsAfterFlush) {
            System.out.println("验证成功: SCRIPT FLUSH 成功清空了脚本缓存。");
        } else {
            System.out.println("验证失败: SCRIPT FLUSH 未达到预期效果。");
        }
    }
}
