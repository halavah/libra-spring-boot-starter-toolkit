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
@DisplayName("RedisUtil 位图(Bitmap)新命令测试")
public class Test17_RedisBitmapTesting {

    private static final String BITMAP_KEY_1 = "test:bitmap:key1"; // for bitcount, bitpos
    private static final String BITMAP_KEY_2 = "test:bitmap:key2"; // for bitop
    private static final String BITMAP_KEY_3 = "test:bitmap:key3"; // for bitop
    private static final String BITMAP_DEST_KEY = "test:bitmap:dest";

    @BeforeAll
    static void setUp() {
        System.out.println("--- 开始 RedisUtil 位图(Bitmap)新命令测试 ---");
        RedissonUtil.KeyOps.delete(BITMAP_KEY_1, BITMAP_KEY_2, BITMAP_KEY_3, BITMAP_DEST_KEY);

        // 准备数据: key1 -> "foobar"
        // f (01100110), o (01101111), o (01101111), b (01100010), a (01100001), r (01110010)
        // 总共有 26 个 '1'
        RedissonUtil.StringOps.set(BITMAP_KEY_1, "foobar");

        // 准备数据: key2 -> 1010, key3 -> 0110
        RedissonUtil.StringOps.setBit(BITMAP_KEY_2, 0, true);
        RedissonUtil.StringOps.setBit(BITMAP_KEY_2, 2, true);
        RedissonUtil.StringOps.setBit(BITMAP_KEY_3, 1, true);
        RedissonUtil.StringOps.setBit(BITMAP_KEY_3, 2, true);
    }

    @AfterAll
    static void tearDown() {
        RedissonUtil.KeyOps.delete(BITMAP_KEY_1, BITMAP_KEY_2, BITMAP_KEY_3, BITMAP_DEST_KEY);
        System.out.println("--- 结束 RedisUtil 位图(Bitmap)新命令测试 ---");
    }

    @Test
    @Order(1)
    @DisplayName("BitmapOps: 测试 bitCount 命令")
    void test_BitmapOps_BitCount() {
        System.out.println("--- 测试 1: bitCount ---");
        Long count = RedissonUtil.BitmapOps.bitCount(BITMAP_KEY_1);
        System.out.println("'" + BITMAP_KEY_1 + "' 中 '1' 的数量: " + count);
        if (count == 26) {
            System.out.println("验证成功: bitCount 结果正确。");
        } else {
            System.out.println("验证失败: bitCount 结果应为 26。");
        }

        Long partialCount = RedissonUtil.BitmapOps.bitCount(BITMAP_KEY_1, 0, 0); // 'f' -> 01100110, 4个1
        System.out.println("'" + BITMAP_KEY_1 + "' 第一个字节中 '1' 的数量: " + partialCount);
        if (partialCount == 4) {
            System.out.println("验证成功: bitCount (带范围) 结果正确。");
        } else {
            System.out.println("验证失败: bitCount (带范围) 结果应为 4。");
        }
    }

    @Test
    @Order(2)
    @DisplayName("BitmapOps: 测试 bitPos 命令")
    void test_BitmapOps_BitPos() {
        System.out.println("--- 测试 2: bitPos ---");
        // "foobar" -> 01100110... 第一个 1 在位置 1
        Long pos1 = RedissonUtil.BitmapOps.bitPos(BITMAP_KEY_1, true);
        System.out.println("'" + BITMAP_KEY_1 + "' 中第一个 '1' 的位置: " + pos1);
        if (pos1 == 1) {
            System.out.println("验证成功: bitPos(true) 结果正确。");
        } else {
            System.out.println("验证失败: bitPos(true) 结果应为 1。");
        }

        // "foobar" -> 01100110... 第一个 0 在位置 0
        Long pos0 = RedissonUtil.BitmapOps.bitPos(BITMAP_KEY_1, false);
        System.out.println("'" + BITMAP_KEY_1 + "' 中第一个 '0' 的位置: " + pos0);
        if (pos0 == 0) {
            System.out.println("验证成功: bitPos(false) 结果正确。");
        } else {
            System.out.println("验证失败: bitPos(false) 结果应为 0。");
        }
    }

    @Test
    @Order(3)
    @DisplayName("BitmapOps: 测试 bitOpAnd 命令")
    void test_BitmapOps_BitOpAnd() {
        System.out.println("--- 测试 3: bitOpAnd ---");
        // key2 (1010) AND key3 (0110) -> 0010
        RedissonUtil.BitmapOps.bitOpAnd(BITMAP_DEST_KEY, BITMAP_KEY_2, BITMAP_KEY_3);
        Long count = RedissonUtil.BitmapOps.bitCount(BITMAP_DEST_KEY);
        System.out.println("AND 操作后 '1' 的数量: " + count);
        boolean bit2 = RedissonUtil.StringOps.getBit(BITMAP_DEST_KEY, 2);
        System.out.println("AND 操作后第2位的值: " + bit2);
        if (count == 1 && bit2) {
            System.out.println("验证成功: bitOpAnd 结果正确。");
        } else {
            System.out.println("验证失败: bitOpAnd 结果不正确。");
        }
        RedissonUtil.KeyOps.delete(BITMAP_DEST_KEY);
    }

    @Test
    @Order(4)
    @DisplayName("BitmapOps: 测试 bitOpOr 命令")
    void test_BitmapOps_BitOpOr() {
        System.out.println("--- 测试 4: bitOpOr ---");
        // key2 (1010) OR key3 (0110) -> 1110
        RedissonUtil.BitmapOps.bitOpOr(BITMAP_DEST_KEY, BITMAP_KEY_2, BITMAP_KEY_3);
        Long count = RedissonUtil.BitmapOps.bitCount(BITMAP_DEST_KEY);
        System.out.println("OR 操作后 '1' 的数量: " + count);
        if (count == 3) {
            System.out.println("验证成功: bitOpOr 结果正确。");
        } else {
            System.out.println("验证失败: bitOpOr 结果不正确。");
        }
        RedissonUtil.KeyOps.delete(BITMAP_DEST_KEY);
    }

    @Test
    @Order(5)
    @DisplayName("BitmapOps: 测试 bitOpXor 命令")
    void test_BitmapOps_BitOpXor() {
        System.out.println("--- 测试 5: bitOpXor ---");
        // key2 (1010) XOR key3 (0110) -> 1100
        RedissonUtil.BitmapOps.bitOpXor(BITMAP_DEST_KEY, BITMAP_KEY_2, BITMAP_KEY_3);
        Long count = RedissonUtil.BitmapOps.bitCount(BITMAP_DEST_KEY);
        System.out.println("XOR 操作后 '1' 的数量: " + count);
        if (count == 2) {
            System.out.println("验证成功: bitOpXor 结果正确。");
        } else {
            System.out.println("验证失败: bitOpXor 结果不正确。");
        }
        RedissonUtil.KeyOps.delete(BITMAP_DEST_KEY);
    }

    @Test
    @Order(6)
    @DisplayName("BitmapOps: 测试 bitOpNot 命令")
    void test_BitmapOps_BitOpNot() {
        System.out.println("--- 测试 6: bitOpNot ---");
        // NOT "foobar"
        RedissonUtil.BitmapOps.bitOpNot(BITMAP_DEST_KEY, BITMAP_KEY_1);
        String originalValue = RedissonUtil.StringOps.get(BITMAP_KEY_1);
        String notValue = RedissonUtil.StringOps.get(BITMAP_DEST_KEY);
        System.out.println("NOT 操作前的值长度: " + originalValue.length());
        System.out.println("NOT 操作后的值长度: " + notValue.length());
        // NOT 操作是逐字节的，所以长度应该相等
        if (originalValue.length() == notValue.length()) {
            System.out.println("验证成功: bitOpNot 结果长度正确。");
        } else {
            System.out.println("验证失败: bitOpNot 结果长度不正确。");
        }
        RedissonUtil.KeyOps.delete(BITMAP_DEST_KEY);
    }
}
