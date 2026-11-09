package cn.myslayers.libra.framework.toolkit.license;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * License 生成器
 * <p>
 * 用于生成 License 密钥，只有项目维护者可以使用此工具生成 License。
 * </p>
 *
 * @author Halavah
 * @version 1.0.0
 * @since 2025-11-13
 */
public class LicenseGenerator {

    /**
     * 生成 RSA 密钥对
     *
     * @return KeyPair
     * @throws Exception 异常
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * 打印密钥对（用于初次生成密钥）
     *
     * @param keyPair 密钥对
     */
    public static void printKeyPair(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        System.out.println("\n" +
                "╔════════════════════════════════════════════════════════════════╗\n" +
                "║                    RSA 密钥对生成成功                          ║\n" +
                "╠════════════════════════════════════════════════════════════════╣\n" +
                "║                                                                ║\n" +
                "║  公钥 (Public Key)：                                           ║\n" +
                "║  请将以下内容复制到 LicenseValidator.java 中                   ║\n" +
                "║                                                                ║\n" +
                "╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("private static final String PUBLIC_KEY =");
        printInChunks(publicKeyStr, 64, true);
        System.out.println();

        System.out.println("\n" +
                "╔════════════════════════════════════════════════════════════════╗\n" +
                "║  私钥 (Private Key)：                                          ║\n" +
                "║  ⚠️  请妥善保管，不要泄露！                                     ║\n" +
                "║  用于生成 License，保存到安全的地方                            ║\n" +
                "║                                                                ║\n" +
                "╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("private static final String PRIVATE_KEY =");
        printInChunks(privateKeyStr, 64, true);
        System.out.println();
    }

    /**
     * 分块打印字符串（格式化输出）
     */
    private static void printInChunks(String str, int chunkSize, boolean asJavaString) {
        int length = str.length();
        for (int i = 0; i < length; i += chunkSize) {
            String chunk = str.substring(i, Math.min(length, i + chunkSize));
            if (asJavaString) {
                if (i + chunkSize < length) {
                    System.out.println("        \"" + chunk + "\" +");
                } else {
                    System.out.println("        \"" + chunk + "\";");
                }
            } else {
                System.out.println(chunk);
            }
        }
    }

    /**
     * 生成 License
     *
     * @param privateKeyStr 私钥字符串（Base64）
     * @param company       公司名称
     * @param expirationDate 过期时间（null 表示永久）
     * @param features      功能列表（可选）
     * @return License 字符串
     * @throws Exception 异常
     */
    public static String generateLicense(String privateKeyStr, String company, Date expirationDate, String features) throws Exception {
        // 1. 构造 License 数据
        String expireDateStr;
        if (expirationDate == null) {
            expireDateStr = "UNLIMITED";
        } else {
            expireDateStr = String.valueOf(expirationDate.getTime());
        }

        String licenseData = company + "|libra-spring-boot-starter-toolkit|" + expireDateStr;
        if (features != null && !features.trim().isEmpty()) {
            licenseData += "|" + features;
        }

        // 2. 获取私钥
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(spec);

        // 3. 使用私钥加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedData = cipher.doFinal(licenseData.getBytes(StandardCharsets.UTF_8));

        // 4. Base64 编码
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 验证 License 是否有效（测试用）
     *
     * @param publicKeyStr 公钥字符串（Base64）
     * @param license      License 字符串
     * @return 是否有效
     */
    public static boolean verifyLicense(String publicKeyStr, String license) {
        try {
            // 1. Base64 解码
            byte[] encryptedData = Base64.getDecoder().decode(license);

            // 2. 获取公钥
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            // 3. 使用公钥解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 4. 解析 License 数据
            String licenseData = new String(decryptedData, StandardCharsets.UTF_8);
            String[] parts = licenseData.split("\\|");

            if (parts.length < 3) {
                return false;
            }

            // 5. 验证产品名称
            String product = parts[1];
            if (!"libra-spring-boot-starter-toolkit".equals(product)) {
                return false;
            }

            // 6. 验证过期时间
            String expireDateStr = parts[2];
            if (!"UNLIMITED".equals(expireDateStr)) {
                long expireDate = Long.parseLong(expireDateStr);
                long now = System.currentTimeMillis();
                if (now > expireDate) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 打印 License 信息
     */
    public static void printLicenseInfo(String publicKeyStr, String license) {
        try {
            // 1. Base64 解码
            byte[] encryptedData = Base64.getDecoder().decode(license);

            // 2. 获取公钥
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            // 3. 使用公钥解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 4. 解析 License 数据
            String licenseData = new String(decryptedData, StandardCharsets.UTF_8);
            String[] parts = licenseData.split("\\|");

            String company = parts[0];
            String product = parts[1];
            String expireDateStr = parts[2];
            String features = parts.length > 3 ? parts[3] : "All Features";

            System.out.println("\n" +
                    "╔════════════════════════════════════════════════════════════════╗\n" +
                    "║                    License 信息                                ║\n" +
                    "╠════════════════════════════════════════════════════════════════╣\n" +
                    "║                                                                ║\n" +
                    "║  公司/用户: " + String.format("%-50s", company) + " ║\n" +
                    "║  产品名称: " + String.format("%-51s", product) + " ║\n" +
                    "║  功能列表: " + String.format("%-51s", features) + " ║\n" +
                    "║  过期时间: " + String.format("%-51s",
                    "UNLIMITED".equals(expireDateStr)
                            ? "永久有效"
                            : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(expireDateStr)))
            ) + " ║\n" +
                    "║                                                                ║\n" +
                    "╚════════════════════════════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ 无法解析 License: " + e.getMessage());
        }
    }

    /**
     * 主方法（命令行工具）
     */
    public static void main(String[] args) {
        try {
            System.out.println("\n" +
                    "╔════════════════════════════════════════════════════════════════╗\n" +
                    "║         Libra Toolkit - License Generator v1.0.0              ║\n" +
                    "╠════════════════════════════════════════════════════════════════╣\n" +
                    "║                                                                ║\n" +
                    "║  选择操作：                                                     ║\n" +
                    "║  1. 生成 RSA 密钥对                                            ║\n" +
                    "║  2. 生成 License                                               ║\n" +
                    "║  3. 验证 License                                               ║\n" +
                    "║                                                                ║\n" +
                    "╚════════════════════════════════════════════════════════════════╝\n");

            // 示例：生成密钥对
            if (args.length == 0 || "1".equals(args[0])) {
                System.out.println("正在生成 RSA 密钥对...\n");
                KeyPair keyPair = generateKeyPair();
                printKeyPair(keyPair);
            }

            // 示例：生成 License
            else if ("2".equals(args[0])) {
                if (args.length < 3) {
                    System.err.println("用法: java LicenseGenerator 2 <私钥> <公司名称> [过期天数] [功能列表]");
                    System.err.println("示例: java LicenseGenerator 2 \"私钥Base64\" \"MyCompany\" 365 \"All Features\"");
                    return;
                }

                String privateKey = args[1];
                String company = args[2];
                Date expirationDate = null;

                // 计算过期时间
                if (args.length > 3 && !args[3].equals("0")) {
                    int days = Integer.parseInt(args[3]);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, days);
                    expirationDate = cal.getTime();
                }

                String features = args.length > 4 ? args[4] : null;

                System.out.println("正在生成 License...\n");
                String license = generateLicense(privateKey, company, expirationDate, features);

                System.out.println("\n" +
                        "╔════════════════════════════════════════════════════════════════╗\n" +
                        "║                  ✅ License 生成成功                           ║\n" +
                        "╠════════════════════════════════════════════════════════════════╣\n" +
                        "║                                                                ║\n" +
                        "║  请将以下 License 提供给客户：                                 ║\n" +
                        "║                                                                ║\n" +
                        "╚════════════════════════════════════════════════════════════════╝\n");

                System.out.println(license);
                System.out.println();

                // 打印使用说明
                System.out.println("\n" +
                        "╔════════════════════════════════════════════════════════════════╗\n" +
                        "║  客户使用方式：                                                 ║\n" +
                        "╠════════════════════════════════════════════════════════════════╣\n" +
                        "║                                                                ║\n" +
                        "║  方式 1: 系统属性                                              ║\n" +
                        "║  java -Dlibra.license=<上面的License> -jar app.jar            ║\n" +
                        "║                                                                ║\n" +
                        "║  方式 2: 环境变量                                              ║\n" +
                        "║  export LIBRA_LICENSE=<上面的License>                         ║\n" +
                        "║                                                                ║\n" +
                        "║  方式 3: 创建文件                                              ║\n" +
                        "║  在 classpath 根目录创建 license.key 文件                      ║\n" +
                        "║  内容为上面的 License                                          ║\n" +
                        "║                                                                ║\n" +
                        "╚════════════════════════════════════════════════════════════════╝\n");
            }

            // 示例：验证 License
            else if ("3".equals(args[0])) {
                if (args.length < 3) {
                    System.err.println("用法: java LicenseGenerator 3 <公钥> <License>");
                    return;
                }

                String publicKey = args[1];
                String license = args[2];

                System.out.println("正在验证 License...\n");
                boolean valid = verifyLicense(publicKey, license);

                if (valid) {
                    System.out.println("✅ License 有效！\n");
                    printLicenseInfo(publicKey, license);
                } else {
                    System.out.println("❌ License 无效！\n");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
