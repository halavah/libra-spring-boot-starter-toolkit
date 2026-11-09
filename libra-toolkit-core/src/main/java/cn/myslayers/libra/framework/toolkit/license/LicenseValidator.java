package cn.myslayers.libra.framework.toolkit.license;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * License 验证器
 * <p>
 * 用于验证用户的 License 密钥，只有持有有效 License 的用户才能使用本工具包。
 * </p>
 *
 * @author Halavah
 * @version 1.0.0
 * @since 2025-11-13
 */
public class LicenseValidator {

    /**
     * RSA 公钥（用于验证签名）
     * 注意：这是公钥，可以公开。私钥由你保管，用于生成 License。
     */
    private static final String PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjF5zYmw62WO3SQ2LOcLD" +
            "QE8kiKCv+S5tKdqPacYslNKV9v58ldmO/wegm4KRNA0RsnGdGNwz+PGCGUGplRYz" +
            "discIYYl6wthWbjSc8/gVRE37wB5AeHF/DIvTOAtrRu5XwJ+1Iieo/JU/2OnAd0Z" +
            "esRTNG2QGXC3Ae+bcN6F86GST+Om0jAHXShB0MViZpmPrn+P/ZF/KZe/Hsm5eWa0" +
            "ErVz5w2ja4MZkErt4S/G/A/nmIM4l7Tbam6gSx5X4q6hVGOhkV1th9tkOD6f0+ik" +
            "JqR12dJ2qsJ7kaVWkfA31S3MoEtF3puABA2NcjjNNfno8htOvhl55V1SgzaWe8lp" +
            "5wIDAQAB";

    /**
     * 是否已验证
     */
    private static volatile boolean validated = false;

    /**
     * License 信息
     */
    private static String licenseInfo = null;

    /**
     * 静态初始化块，在类加载时自动验证 License
     */
    static {
        try {
            validateLicense();
        } catch (Exception e) {
            // 验证失败，抛出运行时异常
            throw new RuntimeException("❌ License validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 License
     * <p>
     * License 来源优先级：
     * 1. 系统属性：-Dlibra.license=xxx
     * 2. 环境变量：LIBRA_LICENSE=xxx
     * 3. license.key 文件（在 classpath 根目录）
     * </p>
     *
     * @throws Exception 验证失败
     */
    private static void validateLicense() throws Exception {
        // 1. 从系统属性读取
        String license = System.getProperty("libra.license");

        // 2. 从环境变量读取
        if (license == null || license.trim().isEmpty()) {
            license = System.getenv("LIBRA_LICENSE");
        }

        // 3. 从 license.key 文件读取
        if (license == null || license.trim().isEmpty()) {
            license = readLicenseFromFile();
        }

        // 4. 如果仍然为空，抛出异常
        if (license == null || license.trim().isEmpty()) {
            throw new IllegalStateException(
                    "\n" +
                    "╔════════════════════════════════════════════════════════════════╗\n" +
                    "║                    ❌ License Not Found                        ║\n" +
                    "╠════════════════════════════════════════════════════════════════╣\n" +
                    "║                                                                ║\n" +
                    "║  Libra Toolkit requires a valid license to use.                ║\n" +
                    "║                                                                ║\n" +
                    "║  Please provide your license key in one of the following ways: ║\n" +
                    "║                                                                ║\n" +
                    "║  1. System Property:                                           ║\n" +
                    "║     java -Dlibra.license=YOUR_LICENSE_KEY -jar app.jar         ║\n" +
                    "║                                                                ║\n" +
                    "║  2. Environment Variable:                                      ║\n" +
                    "║     export LIBRA_LICENSE=YOUR_LICENSE_KEY                      ║\n" +
                    "║                                                                ║\n" +
                    "║  3. Create file 'license.key' in classpath root:               ║\n" +
                    "║     src/main/resources/license.key                             ║\n" +
                    "║                                                                ║\n" +
                    "║  To obtain a license, please contact:                          ║\n" +
                    "║  📧 Email: halavah@126.com                                     ║\n" +
                    "║  🌐 GitHub: https://github.com/halavah                         ║\n" +
                    "║                                                                ║\n" +
                    "╚════════════════════════════════════════════════════════════════╝\n"
            );
        }

        // 5. 验证 License 签名
        if (!verifyLicense(license.trim())) {
            throw new SecurityException(
                    "\n" +
                    "╔════════════════════════════════════════════════════════════════╗\n" +
                    "║                    ❌ Invalid License                          ║\n" +
                    "╠════════════════════════════════════════════════════════════════╣\n" +
                    "║                                                                ║\n" +
                    "║  The provided license key is invalid or has been tampered with.║\n" +
                    "║                                                                ║\n" +
                    "║  Please check:                                                 ║\n" +
                    "║  1. License key is copied correctly (no extra spaces)          ║\n" +
                    "║  2. License has not expired                                    ║\n" +
                    "║  3. License is issued for this product                         ║\n" +
                    "║                                                                ║\n" +
                    "║  If you believe this is an error, please contact:              ║\n" +
                    "║  📧 Email: halavah@126.com                                     ║\n" +
                    "║  🌐 GitHub: https://github.com/halavah                         ║\n" +
                    "║                                                                ║\n" +
                    "╚════════════════════════════════════════════════════════════════╝\n"
            );
        }

        // 6. 验证成功
        validated = true;
        licenseInfo = parseLicenseInfo(license.trim());

        // 7. 打印欢迎信息
        System.out.println(
                "\n" +
                "╔════════════════════════════════════════════════════════════════╗\n" +
                "║              ✅ Libra Toolkit - License Validated              ║\n" +
                "╠════════════════════════════════════════════════════════════════╣\n" +
                "║                                                                ║\n" +
                "║  " + String.format("%-60s", licenseInfo) + "  ║\n" +
                "║                                                                ║\n" +
                "║  Thank you for using Libra Toolkit!                            ║\n" +
                "║  For support: halavah@126.com                                  ║\n" +
                "║                                                                ║\n" +
                "╚════════════════════════════════════════════════════════════════╝\n"
        );
    }

    /**
     * 从 license.key 文件读取 License
     *
     * @return License 字符串，如果文件不存在返回 null
     */
    private static String readLicenseFromFile() {
        try {
            java.io.InputStream is = LicenseValidator.class.getClassLoader()
                    .getResourceAsStream("license.key");
            if (is != null) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
                String license = reader.readLine();
                reader.close();
                return license;
            }
        } catch (Exception e) {
            // 忽略异常，返回 null
        }
        return null;
    }

    /**
     * 验证 License 签名
     * <p>
     * License 格式：Base64(RSA-Encrypted(LicenseData))
     * LicenseData 格式：Company|Product|ExpireDate|Features
     * </p>
     *
     * @param license License 字符串
     * @return 是否有效
     */
    private static boolean verifyLicense(String license) {
        try {
            // 1. Base64 解码
            byte[] encryptedData = Base64.getDecoder().decode(license);

            // 2. 使用公钥解密
            PublicKey publicKey = getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 3. 解析 License 数据
            String licenseData = new String(decryptedData, StandardCharsets.UTF_8);

            // 4. 验证格式：Company|Product|ExpireDate|Features
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
                    throw new SecurityException("License has expired");
                }
            }

            return true;
        } catch (Exception e) {
            // 验证失败
            return false;
        }
    }

    /**
     * 解析 License 信息（用于显示）
     *
     * @param license License 字符串
     * @return License 信息
     */
    private static String parseLicenseInfo(String license) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(license);
            PublicKey publicKey = getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            String licenseData = new String(decryptedData, StandardCharsets.UTF_8);

            String[] parts = licenseData.split("\\|");
            String company = parts[0];
            String expireDate = parts[2];

            if ("UNLIMITED".equals(expireDate)) {
                return "Licensed to: " + company + " (Unlimited)";
            } else {
                long expire = Long.parseLong(expireDate);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return "Licensed to: " + company + " (Expires: " + sdf.format(new java.util.Date(expire)) + ")";
            }
        } catch (Exception e) {
            return "License Info: [Error parsing]";
        }
    }

    /**
     * 获取公钥对象
     *
     * @return PublicKey
     * @throws Exception 异常
     */
    private static PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * 检查是否已验证（供内部使用）
     *
     * @return 是否已验证
     */
    public static boolean isValidated() {
        return validated;
    }

    /**
     * 获取 License 信息（供内部使用）
     *
     * @return License 信息
     */
    public static String getLicenseInfo() {
        return licenseInfo;
    }

    /**
     * 私有构造函数，防止实例化
     */
    private LicenseValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
