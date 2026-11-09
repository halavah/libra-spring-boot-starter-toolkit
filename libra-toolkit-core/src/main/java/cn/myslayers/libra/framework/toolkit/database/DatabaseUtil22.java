package cn.myslayers.libra.framework.toolkit.database;

import com.alibaba.druid.pool.DruidDataSource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * 数据库工具类：
 * 1. 日志调试：LoggerCore.setDebugLogEnabled(true);
 * 2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
 * 3. 事务管理：setTransaction() / commit() / rollback() / result()
 * 4. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
 */
public class DatabaseUtil22 {

    // ==================== 常量定义 ====================

    // 默认SQL目录
    public static final String[] XML_DIRECTORIES = {"src/main/resources/sql/", "resources/sql/",};

    // 日志开关：控制调试输出
    private static volatile boolean DEBUG_LOG_ENABLED = false;

    // SQL执行日志开关：控制SQL执行详情输出
    private static volatile boolean SQL_DEBUG_LOG_ENABLED = false;

    // 源注册：yml文件
    public static final String[] DATABASE_YML_CONNECTIONS = {"master", "second"};

    // 源注册：手动配置
    public static final String[][] DATABASE_MANAGE_CONNECTIONS = {
        {"default_mysql1", "jdbc:mysql://localhost:3306/libra-demo-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true", "demo_user", "demo_password", "com.mysql.cj.jdbc.Driver"},
//          {"default_mysql2", "jdbc:mysql://localhost:3307/libra-demo-db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true", "another_user", "another_password", "com.mysql.cj.jdbc.Driver"},
//          {"default_mysql3", "jdbc:mysql://localhost:3308/libra-demo-db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true", "third_user", "third_password", "com.mysql.cj.jdbc.Driver"}
    };

    // 数据源类型配置：true=使用Druid连接池，false=使用简单数据源
    public static final boolean USE_DRUID_DATASOURCE = true;

    // 线程级Druid配置开关：允许不同线程使用不同的数据源类型
    private static final ThreadLocal<Boolean> THREAD_DRUID_ENABLED = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return USE_DRUID_DATASOURCE; // 默认使用全局配置
        }
    };

    static {
        // 源注册：yml文件
        for (String dbConfig : DATABASE_YML_CONNECTIONS) {
            DataSourceCore.registerYml(dbConfig);
        }

        // 源注册：手动配置
        for (String[] dbConfig : DATABASE_MANAGE_CONNECTIONS) {
            String name = dbConfig[0];
            String url = dbConfig[1];
            String username = dbConfig[2];
            String password = dbConfig[3];
            String driver = dbConfig[4];
            DataSourceCore.register(name, url, username, password, driver);
        }
    }

    // DatabaseUtil：私有构造函数，防止实例化
    private DatabaseUtil22() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== Core：核心配置 ====================

    // 日志
    public static class LoggerCore {

        // 标准日志记录器
        private static final Logger logger = LoggerFactory.getLogger(LoggerCore.class);

        // 私有构造函数，防止实例化
        private LoggerCore() {
            throw new UnsupportedOperationException("日志工具类不允许实例化");
        }

        // 设置普通日志开关
        public static void setDebugLogEnabled(boolean enabled) {
            DEBUG_LOG_ENABLED = enabled;
        }

        // 设置SQL执行日志开关
        public static void setSqlDebugLogEnabled(boolean enabled) {
            SQL_DEBUG_LOG_ENABLED = enabled;
        }

        // 检查普通日志开关状态
        public static boolean isDebugLogEnabled() {
            return DEBUG_LOG_ENABLED;
        }

        // 检查SQL执行日志开关状态
        public static boolean isSqlDebugLogEnabled() {
            return SQL_DEBUG_LOG_ENABLED;
        }

        // 调试日志输出方法
        public static void debugLog(String message) {
            if (DEBUG_LOG_ENABLED) {
                logger.info("[DEBUG] " + getCurrentTimestamp() + " " + message);
            }
        }

        // 错误日志输出方法
        public static void debugErrorLog(String message) {
            if (DEBUG_LOG_ENABLED) {
                logger.error("[ERROR] " + getCurrentTimestamp() + " " + message);
            }
        }

        // 信息日志输出方法
        public static void infoLog(String message) {
            if (DEBUG_LOG_ENABLED) {
                logger.info("[INFO] " + getCurrentTimestamp() + " " + message);
            }
        }

        // 警告日志输出方法
        public static void warnLog(String message) {
            if (DEBUG_LOG_ENABLED) {
                logger.warn("[WARN] " + getCurrentTimestamp() + " " + message);
            }
        }

        // 获取当前时间戳
        private static String getCurrentTimestamp() {
            return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            );
        }

        // 格式化异常信息
        public static void logException(String context, Exception e) {
            if (DEBUG_LOG_ENABLED) {
                debugErrorLog("异常发生在: " + context);
                debugErrorLog("异常类型: " + e.getClass().getSimpleName());
                debugErrorLog("异常消息: " + e.getMessage());
                if (e.getCause() != null) {
                    debugErrorLog("根本原因: " + e.getCause().getMessage());
                }
            }
        }

        // SQL格式化方法：将参数化SQL转换为填充后的完整SQL
        public static String formatSqlWithParams(String sql, Object... params) {
            if (sql == null) {
                return "null";
            }

            if (params == null || params.length == 0) {
                return sql;
            }

            String formattedSql = sql;
            int paramIndex = 0;

            // 查找所有的 ? 占位符并替换
            while (formattedSql.contains("?") && paramIndex < params.length) {
                Object param = params[paramIndex];
                String paramValue = formatParamValue(param);

                // 找到第一个 ? 的位置
                int questionMarkIndex = formattedSql.indexOf("?");
                if (questionMarkIndex != -1) {
                    formattedSql = formattedSql.substring(0, questionMarkIndex) +
                        paramValue +
                        formattedSql.substring(questionMarkIndex + 1);
                }
                paramIndex++;
            }

            return formattedSql;
        }

        // 格式化单个参数值
        private static String formatParamValue(Object param) {
            if (param == null) {
                return "NULL";
            }

            if (param instanceof String) {
                // 字符串类型用单引号包围，并转义内部的单引号
                String stringValue = (String) param;
                stringValue = stringValue.replace("'", "''"); // SQL单引号转义
                return "'" + stringValue + "'";
            }

            if (param instanceof java.util.Date) {
                // 日期类型格式化
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return "'" + sdf.format((java.util.Date) param) + "'";
            }

            if (param instanceof java.sql.Date) {
                // SQL日期类型
                return "'" + param.toString() + "'";
            }

            if (param instanceof java.sql.Timestamp) {
                // 时间戳类型
                return "'" + param.toString() + "'";
            }

            if (param instanceof Boolean) {
                // 布尔类型转换为数字（Oracle风格）
                return ((Boolean) param) ? "1" : "0";
            }

            if (param instanceof Number) {
                // 数字类型直接返回
                return param.toString();
            }

            // 其他类型转换为字符串并用单引号包围
            String stringValue = param.toString();
            stringValue = stringValue.replace("'", "''");
            return "'" + stringValue + "'";
        }

        // 输出完整格式化SQL的专用方法
        public static void debugSqlWithParams(String title, String sql, Object... params) {
            if (SQL_DEBUG_LOG_ENABLED) {
                String formattedSql = formatSqlWithParams(sql, params);
                debugLog(title + ": " + formattedSql);
            }
        }

        // SQL执行专用调试方法 - 优化版本，支持操作类型和结果显示
        public static void debugSqlExecution(String operationType, String dbName, String sql, Object[] params, Object result) {
            if (SQL_DEBUG_LOG_ENABLED) {
                logger.info("=== SQL类型" + operationType + "===");
                logger.info("数据库: " + dbName);

                // 显示参数值详情
                if (params != null && params.length > 0) {
                    logger.info("参数值详情:");
                    for (int i = 0; i < params.length; i++) {
                        Object param = params[i];
                        String paramType = param != null ? param.getClass().getSimpleName() : "null";
                        String paramValue = param != null ? param.toString() : "null";

                        // 为字符串类型添加引号显示
                        if (param instanceof String) {
                            paramValue = "'" + paramValue + "'";
                        }

                        logger.info("  参数[" + (i + 1) + "]: " + paramValue +
                            (param != null ? " (类型: " + paramType + ")" : ""));
                    }
                } else {
                    logger.info("参数值详情: 无");
                }

                // 显示完整SQL
                logger.info("完整SQL: \n\t\t\t\t" + formatSqlWithParams(sql, params));

                // 显示执行结果
                if (result instanceof List) {
                    List<?> list = (List<?>) result;
                    logger.info("执行结果: 查询成功，返回 " + list.size() + " 条记录");
                } else if (result instanceof Integer) {
                    logger.info("执行结果: 操作成功，影响 " + result + " 行");
                } else if (result instanceof Map) {
                    logger.info("执行结果: 查询成功，返回 1 条记录");
                } else if (result == null) {
                    logger.info("执行结果: 查询成功，返回 0 条记录");
                } else {
                    logger.info("执行结果: " + result);
                }

                logger.info("========================");
            }
        }

        // 兼容性方法：保持原有接口
        public static void debugSqlExecution(String dbName, String sql, Object... params) {
            if (SQL_DEBUG_LOG_ENABLED) {
                debugLog("=== SQL执行 ===");
                debugLog("数据库: " + dbName);
                debugLog("完整SQL: \n\t\t\t\t" + formatSqlWithParams(sql, params));
                debugLog("===============");
            }
        }
    }

    // 解析器
    public static class SqlParser {

        // SQL缓存：用于AUTO XML模式
        private static final Map<String, String> SQL_CACHE = new ConcurrentHashMap<>();
        private static volatile boolean CACHE_INITIALIZED = false;

        // 参数：封装类
        public static class SqlWithParams {

            public final String sql;
            public final Object[] params;

            SqlWithParams(String sql, Object[] params) {
                this.sql = sql;
                this.params = params;
            }
        }

        // 初始化SQL缓存：扫描全部XML文件
        private static synchronized void initializeSqlCache() {
            if (CACHE_INITIALIZED) {
                return;
            }

            LoggerCore.debugLog("🔄 开始初始化SQL缓存...");
            int totalSqlCount = 0;

            try {
                for (String dir : XML_DIRECTORIES) {
                    File directory = new File(dir);
                    if (directory.exists() && directory.isDirectory()) {
                        File[] xmlFiles = directory.listFiles((d, name) -> name.endsWith(".xml"));
                        if (xmlFiles != null) {
                            for (File xmlFile : xmlFiles) {
                                int fileCount = scanXmlFileToCache(xmlFile);
                                totalSqlCount += fileCount;
                                LoggerCore.debugLog("✓ 扫描文件: " + xmlFile.getName() + " (发现 " + fileCount + " 个SQL)");
                            }
                        }
                    }
                }

                CACHE_INITIALIZED = true;
                LoggerCore.debugLog("✅ SQL缓存初始化完成，共缓存 " + totalSqlCount + " 个SQL语句");

            } catch (Exception e) {
                LoggerCore.debugErrorLog("❌ SQL缓存初始化失败: " + e.getMessage());
                throw new RuntimeException("SQL缓存初始化失败", e);
            }
        }

        // 扫描单个XML文件到缓存
        private static int scanXmlFileToCache(File xmlFile) {
            int count = 0;
            try {
                String content = new String(Files.readAllBytes(xmlFile.toPath()), "UTF-8");

                // 查找所有SQL标签
                Pattern pattern = Pattern.compile("<sql\\s+id\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>([^<]*(?:<(?!/sql>)[^<]*)*)</sql>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(content);

                while (matcher.find()) {
                    String sqlId = matcher.group(1);
                    String sqlContent = matcher.group(2).trim();

                    // 检查ID是否已存在
                    if (SQL_CACHE.containsKey(sqlId)) {
                        LoggerCore.debugLog("⚠️ 发现重复SQL ID: " + sqlId + " (文件: " + xmlFile.getName() + ")");
                    }

                    SQL_CACHE.put(sqlId, sqlContent);
                    count++;
                }

            } catch (Exception e) {
                LoggerCore.debugErrorLog("⚠️ 扫描XML文件失败: " + xmlFile.getName() + " - " + e.getMessage());
            }

            return count;
        }

        // 从缓存获取SQL（AUTO XML模式）
        private static String getSqlFromCache(String sqlId) {
            if (!CACHE_INITIALIZED) {
                initializeSqlCache();
            }

            String sql = SQL_CACHE.get(sqlId);
            if (sql == null) {
                throw new RuntimeException("未在缓存中找到SQL ID: " + sqlId);
            }

            LoggerCore.debugLog("📋 从缓存获取SQL: " + sqlId);
            return sql;
        }

        // 统一参数转换监听器
        public static class SqlParameterConverter {

            // 参数转换类型枚举
            public enum ConversionType {
                DIRECT_SQL,     // 直接SQL处理
                XML_FILE,       // 指定XML文件
                XML_AUTO        // 自动搜索XML
            }

            // 统一转换方法：根据类型处理参数
            public static SqlWithParams convert(ConversionType type, String primaryParam, String secondaryParam, Map<String, Object> params) {
                switch (type) {
                    case DIRECT_SQL:
                        // primaryParam是SQL语句
                        return processMapParametersForDirectSql(primaryParam, params);

                    case XML_FILE:
                        // primaryParam是xmlFileName，secondaryParam是sqlId
                        return getSqlWithParamsFromXml(primaryParam, secondaryParam, params);

                    case XML_AUTO:
                        // primaryParam是sqlId
                        return getSqlWithParamsFromXmlAuto(primaryParam, params);

                    default:
                        throw new IllegalArgumentException("不支持的转换类型: " + type);
                }
            }

            // 便捷方法：直接SQL转换
            public static SqlWithParams convertDirectSql(String sql, Map<String, Object> params) {
                return convert(ConversionType.DIRECT_SQL, sql, null, params);
            }

            // 便捷方法：指定XML文件转换
            public static SqlWithParams convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params) {
                return convert(ConversionType.XML_FILE, xmlFileName, sqlId, params);
            }

            // 便捷方法：自动搜索XML转换
            public static SqlWithParams convertXmlAuto(String sqlId, Map<String, Object> params) {
                return convert(ConversionType.XML_AUTO, sqlId, null, params);
            }
        }

        // 参数：指定具体XML（每次扫描指定文件）
        private static SqlWithParams getSqlWithParamsFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            try {
                LoggerCore.debugLog("🔍 扫描指定XML文件: " + xmlFileName + " 查找SQL ID: " + sqlId);

                for (String dir : XML_DIRECTORIES) {
                    String filePath = dir + xmlFileName;
                    if (!filePath.endsWith(".xml")) {
                        filePath += ".xml";
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        throw new RuntimeException("文件不存在: " + filePath);
                    }
                    // 简单的XML解析实现
                    String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");

                    // 查找指定的SQL ID
                    String pattern = "<sql\\s+id\\s*=\\s*[\"']" + sqlId + "[\"'][^>]*>";
                    Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = p.matcher(content);

                    if (matcher.find()) {
                        int start = matcher.end();
                        int end = content.indexOf("</sql>", start);
                        if (end == -1) {
                            throw new RuntimeException("SQL标签未正确闭合: " + sqlId);
                        }

                        String originalSql = content.substring(start, end).trim();
                        LoggerCore.debugLog("✓ 在文件 " + xmlFileName + " 中找到SQL: " + sqlId);

                        // 处理动态SQL标签
                        String processedSql = SqlParser.processDynamicSqlTags(originalSql, params);

                        // 提取参数并替换占位符
                        return SqlParser.extractParametersAndReplacePlaceholders(processedSql, params);
                    }
                }
                throw new RuntimeException("未找到XML文件: " + xmlFileName + " 或SQL ID: " + sqlId);
            } catch (Exception e) {
                throw new RuntimeException("获取SQL失败: " + e.getMessage(), e);
            }
        }

        // 参数：自动搜索XML（使用缓存）
        private static SqlWithParams getSqlWithParamsFromXmlAuto(String sqlId, Map<String, Object> params) {
            try {
                // 从缓存获取SQL
                String originalSql = getSqlFromCache(sqlId);

                // 处理动态SQL标签
                String processedSql = SqlParser.processDynamicSqlTags(originalSql, params);

                // 提取参数并替换占位符
                return SqlParser.extractParametersAndReplacePlaceholders(processedSql, params);

            } catch (Exception e) {
                throw new RuntimeException("从缓存获取SQL失败: " + sqlId + " - " + e.getMessage(), e);
            }
        }

        // 参数：链式访问器、默认数据库
        private static SqlWithParams processMapParametersForDirectSql(String sql, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                return new SqlWithParams(sql, new Object[0]);
            }

            // 1. 先处理${...}直接文本替换
            String sqlAfterTextReplace = replaceDirectTextParameters(sql, params);

            // 2. 找出所有#{...}和:paramName参数占位符，按出现顺序
            List<String> parameterNames = new ArrayList<>();
            List<Object> parameterValues = new ArrayList<>();

            // 处理#{...}格式
            Pattern hashPattern = Pattern.compile("#\\{([^}]+)\\}");
            Matcher hashMatcher = hashPattern.matcher(sqlAfterTextReplace);

            while (hashMatcher.find()) {
                String paramName = hashMatcher.group(1).trim();
                parameterNames.add(paramName);
                Object paramValue = params.get(paramName);
                parameterValues.add(paramValue);
            }

            // 处理:paramName格式
            Pattern colonPattern = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");
            Matcher colonMatcher = colonPattern.matcher(sqlAfterTextReplace);

            while (colonMatcher.find()) {
                String paramName = colonMatcher.group(1);
                if (!parameterNames.contains(paramName)) { // 避免重复
                    parameterNames.add(paramName);
                    Object paramValue = params.get(paramName);
                    parameterValues.add(paramValue);
                }
            }

            // 3. 替换所有参数占位符为?
            String finalSql = sqlAfterTextReplace.replaceAll("#\\{[^}]+\\}", "?");
            finalSql = finalSql.replaceAll(":[a-zA-Z_][a-zA-Z0-9_]*", "?");

            return new SqlWithParams(finalSql, parameterValues.toArray());
        }

        // 提取参数并替换占位符的核心方法
        private static SqlWithParams extractParametersAndReplacePlaceholders(String sql, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                // 如果没有参数，直接处理${...}替换
                String processedSql = replaceDirectTextParameters(sql, params);
                // 智能处理SQL末尾的分号
                processedSql = smartTrimSqlSemicolon(processedSql);
                return new SqlWithParams(processedSql, new Object[0]);
            }

            // 1. 先处理${...}直接文本替换
            String sqlAfterTextReplace = replaceDirectTextParameters(sql, params);

            // 2. 找出所有#{...}参数占位符，按出现顺序
            List<String> parameterNames = new ArrayList<>();
            List<Object> parameterValues = new ArrayList<>();
            List<String> missingParams = new ArrayList<>();

            Pattern pattern = Pattern.compile("#\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(sqlAfterTextReplace);

            while (matcher.find()) {
                String paramName = matcher.group(1).trim();
                parameterNames.add(paramName);

                // 获取参数值，增强处理
                Object paramValue = null;
                if (params.containsKey(paramName)) {
                    paramValue = params.get(paramName);
                    // 如果参数存在但值为null，保持null
                    if (paramValue == null) {
                        LoggerCore.debugLog("警告: 参数 '" + paramName + "' 存在但值为 null");
                    }
                } else {
                    // 参数不存在，记录到缺失列表，值设为null
                    missingParams.add(paramName);
                    LoggerCore.debugLog("警告: 参数 '" + paramName + "' 不存在，将使用 null 值");
                }

                parameterValues.add(paramValue);
            }

            // 3. 将所有#{...}替换为?
            String finalSql = sqlAfterTextReplace.replaceAll("#\\{[^}]+\\}", "?");

            // 4. 智能处理SQL末尾的分号
            finalSql = smartTrimSqlSemicolon(finalSql);

            // 新的格式化方法用于增强调试输出
            LoggerCore.debugLog("=== SQL参数处理调试信息 ===");
            LoggerCore.debugLog("原始SQL长度: " + sql.length());
            LoggerCore.debugLog("找到参数: " + parameterNames);
            LoggerCore.debugLog("输入参数Map: " + params.keySet());

            if (!missingParams.isEmpty()) {
                LoggerCore.debugLog("❌ 缺失参数: " + missingParams);
            }

            LoggerCore.debugLog("参数值详情:");
            for (int i = 0; i < parameterNames.size(); i++) {
                String paramName = parameterNames.get(i);
                Object paramValue = parameterValues.get(i);
                LoggerCore.debugLog("  参数[" + (i + 1) + "]: " + paramName + " = " +
                    (paramValue == null ? "null" : "'" + paramValue + "' (类型: " + paramValue.getClass().getSimpleName() + ")"));
            }
            LoggerCore.debugLog("========================");

            return new SqlWithParams(finalSql, parameterValues.toArray());
        }

        // 智能处理SQL末尾的分号
        private static String smartTrimSqlSemicolon(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return sql;
            }

            String trimmed = sql.trim();

            // 检查是否以分号结尾
            if (trimmed.endsWith(";")) {
                String withoutSemicolon = trimmed.substring(0, trimmed.length() - 1).trim();
                LoggerCore.debugLog("✓ 智能处理: 自动移除SQL末尾的分号以避免ORA-00933错误");
                LoggerCore.debugLog("   原始SQL末尾: \"..." + trimmed.substring(Math.max(0, trimmed.length() - 20)) + "\"");
                LoggerCore.debugLog("   处理后末尾: \"..." + withoutSemicolon.substring(Math.max(0, withoutSemicolon.length() - 20)) + "\"");
                return withoutSemicolon;
            }

            return trimmed;
        }

        // 处理动态SQL标签
        private static String processDynamicSqlTags(String sql, Map<String, Object> params) {
            sql = processXmlIfTags(sql, params);
            sql = processHashIfTags(sql, params);
            return sql;
        }

        // 处理${...}直接文本替换
        private static String replaceDirectTextParameters(String sql, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                return sql;
            }

            String result = sql;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                result = result.replace("${" + key + "}", value);
            }
            return result;
        }

        // 处理XML风格的if标签
        private static String processXmlIfTags(String sql, Map<String, Object> params) {
            Pattern pattern = Pattern.compile(
                "<if\\s+test\\s*=\\s*[\"']([^\"']+)[\"']\\s*>([^<]*)</if>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String condition = matcher.group(1);
                String content = matcher.group(2);

                if (evaluateXmlCondition(condition, params)) {
                    matcher.appendReplacement(sb, content);
                } else {
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理#if标签
        private static String processHashIfTags(String sql, Map<String, Object> params) {
            Pattern pattern = Pattern.compile(
                "#if\\s*\\(\\s*([^)]+)\\s*\\)([^#]*)#end",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String condition = matcher.group(1);
                String content = matcher.group(2);

                if (evaluateHashCondition(condition, params)) {
                    matcher.appendReplacement(sb, content);
                } else {
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 评估XML风格条件
        private static boolean evaluateXmlCondition(String condition, Map<String, Object> params) {
            // 简单的条件评估实现
            condition = condition.trim();

            // 处理 != null 条件
            if (condition.contains("!= null")) {
                String key = condition.replace("!= null", "").trim();
                Object value = params.get(key);
                return value != null;
            }

            // 处理 != '' 条件
            if (condition.contains("!= ''")) {
                String key = condition.replace("!= ''", "").trim();
                Object value = params.get(key);
                return value != null && !value.toString().trim().isEmpty();
            }

            // 默认检查参数是否存在且不为空
            Object value = params.get(condition);
            return value != null && !value.toString().trim().isEmpty();
        }

        // 评估#if风格条件
        private static boolean evaluateHashCondition(String condition, Map<String, Object> params) {
            // 简单的条件评估实现
            condition = condition.trim();

            // 处理字符串空值检查
            if (condition.contains("\"$!") && condition.contains("\" != \"\"")) {
                Pattern pattern = Pattern.compile("\"\\$!([^\"]+)\"\\s*!=\\s*\"\"");
                Matcher matcher = pattern.matcher(condition);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    Object value = params.get(key);
                    return value != null && !value.toString().trim().isEmpty();
                }
            }

            // 处理 && 连接的条件
            if (condition.contains("&&")) {
                String[] parts = condition.split("&&");
                for (String part : parts) {
                    if (!evaluateHashCondition(part.trim(), params)) {
                        return false;
                    }
                }
                return true;
            }

            // 默认检查参数是否存在且不为空
            if (condition.startsWith("\"$!") && condition.endsWith("\"")) {
                String key = condition.substring(3, condition.length() - 1);
                Object value = params.get(key);
                return value != null && !value.toString().trim().isEmpty();
            }

            return false;
        }
    }

    // 执行器
    public static class SqlExecutor {

        // 设置PreparedStatement参数
        private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
        }

        // 全部：执行器
        public static List<Map<String, Object>> selectList(String dbName, String sql, Object... params) {
            List<Map<String, Object>> results = new ArrayList<>();
            // 智能处理SQL末尾的分号
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (Connection conn = DataSourceCore.getConnection(dbName);
                PreparedStatement pstmt = conn.prepareStatement(processedSql)) {

                LoggerCore.debugLog("当前数据源： " + dbName);
                LoggerCore.debugLog("全部注册源： " + DataSourceCore.DATA_SOURCES.keySet());

                setParameters(pstmt, params);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        // 将ResultSet转换为Map
                        Map<String, Object> map = new HashMap<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            map.put(columnName, value);
                        }
                        results.add(map);
                    }
                }

                // 使用新的SQL调试方法输出详细信息
                LoggerCore.debugSqlExecution("selectListByMp", dbName, processedSql, params, results);

                LoggerCore.debugLog("查询完成，返回 " + results.size() + " 条记录，数据库: " + dbName);
                return results;

            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                // 使用新的格式化方法显示错误时的完整SQL
                LoggerCore.debugSqlWithParams("错误SQL", processedSql, params);
                LoggerCore.debugErrorLog("===================");
                throw new RuntimeException("查询执行失败 [" + e.getErrorCode() + "/" + e.getSQLState() + "]: " + e.getMessage(), e);
            }
        }

        // 单条：执行器
        public static Map<String, Object> selectOne(String dbName, String sql, Object... params) {
            // 智能处理SQL末尾的分号
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (Connection conn = DataSourceCore.getConnection(dbName);
                PreparedStatement pstmt = conn.prepareStatement(processedSql)) {

                LoggerCore.debugLog("当前数据源： " + dbName);
                LoggerCore.debugLog("全部注册源： " + DataSourceCore.DATA_SOURCES.keySet());

                setParameters(pstmt, params);

                Map<String, Object> result = null;
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // 将ResultSet转换为Map
                        result = new HashMap<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            result.put(columnName, value);
                        }
                    }
                }

                // 使用新的SQL调试方法输出详细信息
                LoggerCore.debugSqlExecution("selectOneByMp", dbName, processedSql, params, result);

                LoggerCore.debugLog("查询完成，返回 " + (result != null ? "1" : "0") + " 条记录，数据库: " + dbName);
                return result;

            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                // 使用新的格式化方法显示错误时的完整SQL
                LoggerCore.debugSqlWithParams("错误SQL", processedSql, params);
                LoggerCore.debugErrorLog("===================");
                throw new RuntimeException("查询执行失败 [" + e.getErrorCode() + "/" + e.getSQLState() + "]: " + e.getMessage(), e);
            }
        }

        // 插入：执行器
        public static int insert(String dbName, String sql, Object... params) {
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (Connection conn = DataSourceCore.getConnection(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                PreparedStatement pstmt = conn.prepareStatement(processedSql)) {

                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();

                // 使用新的SQL调试方法输出详细信息
                LoggerCore.debugSqlExecution("insertByMp", DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), processedSql, params, rows);

                LoggerCore.debugLog("插入完成，影响 " + rows + " 行，数据库: " + DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                return rows;

            } catch (SQLException e) {
                throw new RuntimeException("插入执行失败: " + e.getMessage(), e);
            }
        }

        // 更新：执行器
        public static int update(String dbName, String sql, Object... params) {
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (Connection conn = DataSourceCore.getConnection(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                PreparedStatement pstmt = conn.prepareStatement(processedSql)) {

                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();

                // 使用新的SQL调试方法输出详细信息
                LoggerCore.debugSqlExecution("updateByMp", DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), processedSql, params, rows);

                LoggerCore.debugLog("更新完成，影响 " + rows + " 行，数据库: " + DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                return rows;

            } catch (SQLException e) {
                throw new RuntimeException("更新执行失败: " + e.getMessage(), e);
            }
        }

        // 删除：执行器
        public static int delete(String dbName, String sql, Object... params) {
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (Connection conn = DataSourceCore.getConnection(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                PreparedStatement pstmt = conn.prepareStatement(processedSql)) {

                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();

                // 使用新的SQL调试方法输出详细信息
                LoggerCore.debugSqlExecution("deleteByMp", DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), processedSql, params, rows);

                LoggerCore.debugLog("删除完成，影响 " + rows + " 行，数据库: " + DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                return rows;

            } catch (SQLException e) {
                throw new RuntimeException("删除执行失败: " + e.getMessage(), e);
            }
        }
    }

    // 数据源
    public static class DataSourceCore {

        // 数据源列表
        public static final Map<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();

        // 线程本地数据源上下文
        private static final ThreadLocal<String> THREAD_DATASOURCE_CONTEXT = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return null; // 初始状态下不使用线程特定数据源
            }
        };

        // 原子操作：全局默认数据源名称
        public static final AtomicReference<String> ATOMIC_DEFAULT_SOURCE = new AtomicReference<>();

        // 锁优化：使用StampedLock减少锁竞争
        private static final StampedLock DATA_SOURCE_LOCK = new StampedLock();

        // ==================== Core1：源实现 =====================

        // DataSource：配置类
        public static final class DatabaseConfig {

            private final String url;

            private final String username;

            private final String password;

            private final String driverClass;

            public DatabaseConfig(String url, String username, String password, String driverClass) {
                this.url = url;
                this.username = username;
                this.password = password;
                this.driverClass = driverClass;
            }

            public String getUrl() {
                return url;
            }

            public String getUsername() {
                return username;
            }

            public String getPassword() {
                return password;
            }

            public String getDriverClass() {
                return driverClass;
            }
        }

        // DataSource：实现类
        private static class SimpleDataSource implements DataSource {

            private final DatabaseConfig config;

            public SimpleDataSource(DatabaseConfig config) {
                this.config = config;
                // 加载驱动
                try {
                    Class.forName(config.getDriverClass());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("无法加载数据库驱动: " + config.getDriverClass(), e);
                }
            }

            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return DriverManager.getConnection(config.getUrl(), username, password);
            }

            // 其他方法的默认实现
            @Override
            public java.io.PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(java.io.PrintWriter out) throws SQLException {
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {
            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
                throw new java.sql.SQLFeatureNotSupportedException();
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }
        }

        // Druid连接池管理器：内部类封装
        private static class DruidConnectionPool implements DataSource {

            private final DruidDataSource druidDataSource;
            private final DatabaseConfig config;
            private final long createTime;

            public DruidConnectionPool(DatabaseConfig config) {
                this.config = config;
                this.createTime = System.currentTimeMillis();
                this.druidDataSource = createDruidDataSource(config);

                LoggerCore.debugLog("✓ Druid连接池创建成功: " + config.getUrl());
            }

            // 创建和配置Druid数据源
            private DruidDataSource createDruidDataSource(DatabaseConfig config) {
                DruidDataSource ds = new DruidDataSource();

                // 基本连接参数
                ds.setUrl(config.getUrl());
                ds.setUsername(config.getUsername());
                ds.setPassword(config.getPassword());
                ds.setDriverClassName(config.getDriverClass());

                // 连接池配置 - 生产环境优化
                ds.setInitialSize(5);         // 初始连接数
                ds.setMinIdle(5);             // 最小空闲连接数
                ds.setMaxActive(50);          // 最大活跃连接数
                ds.setMaxWait(60000);         // 获取连接最大等待时间

                // 连接有效性检测
                ds.setTestWhileIdle(true);    // 空闲时检测
                ds.setTestOnBorrow(false);    // 借用时不检测（性能优化）
                ds.setTestOnReturn(false);    // 归还时不检测（性能优化）

                // 根据数据库类型设置正确的验证查询语句
                String validationQuery = "SELECT 1"; // 默认MySQL语法
                String url = config.getUrl().toLowerCase();
                if (url.contains("oracle")) {
                    validationQuery = "SELECT 1 FROM DUAL"; // Oracle语法
                } else if (url.contains("sqlserver")) {
                    validationQuery = "SELECT 1"; // SQL Server语法
                } else if (url.contains("postgresql")) {
                    validationQuery = "SELECT 1"; // PostgreSQL语法
                }
                ds.setValidationQuery(validationQuery);

                // 连接回收配置
                ds.setTimeBetweenEvictionRunsMillis(60000); // 回收间隔60秒
                ds.setMinEvictableIdleTimeMillis(300000);   // 空闲5分钟回收

                // 监控和日志
                ds.setLogAbandoned(true);     // 记录泄露连接
                ds.setRemoveAbandoned(true);  // 自动回收泄露连接
                ds.setRemoveAbandonedTimeout(1800); // 30分钟超时

                // 防SQL注入
                ds.setPoolPreparedStatements(true);
                ds.setMaxPoolPreparedStatementPerConnectionSize(20);

                try {
                    // H2数据库特殊处理：完全禁用Wall过滤器以支持KEY语法
                    if (url.contains("h2")) {
                        // 对于H2数据库，只启用监控统计，不启用Wall过滤器
                        ds.setFilters("stat");
                        LoggerCore.debugLog("✓ H2数据库检测到，禁用Wall过滤器以支持KEY语法");
                        LoggerCore.debugLog("✓ 设置验证查询: " + validationQuery + " (URL: " + config.getUrl() + ")");
                        LoggerCore.debugLog("✓ H2专项配置: Wall过滤器已完全禁用，支持所有H2语法包括KEY");
                    } else {
                        // 非H2数据库启用监控统计和Wall过滤器
                        ds.setFilters("stat,wall");

                        // 配置Wall过滤器允许SQL注释
                        com.alibaba.druid.wall.WallConfig wallConfig = new com.alibaba.druid.wall.WallConfig();
                        wallConfig.setInsertAllow(true); // 允许INSERT
                        wallConfig.setUpdateAllow(true); // 允许UPDATE
                        wallConfig.setDeleteAllow(true); // 允许DELETE
                        wallConfig.setCommentAllow(true);        // 允许SQL注释
                        wallConfig.setMultiStatementAllow(true); // 允许多语句
                        wallConfig.setNoneBaseStatementAllow(true); // 允许非基础语句
                        wallConfig.setMergeAllow(true);          // 允许MERGE语句
                        wallConfig.setSelectIntoAllow(true);     // 允许SELECT INTO语句

                        // 获取Wall过滤器并应用配置
                        com.alibaba.druid.wall.WallFilter wallFilter = new com.alibaba.druid.wall.WallFilter();
                        wallFilter.setConfig(wallConfig);
                        String dbType = url.contains("oracle") ? "oracle" : "mysql";
                        wallFilter.setDbType(dbType);

                        // 设置过滤器
                        ds.setProxyFilters(java.util.Arrays.asList(wallFilter));

                        // 记录详细的配置信息
                        LoggerCore.debugLog("✓ 设置验证查询: " + validationQuery + " (URL: " + config.getUrl() + ")");
                        LoggerCore.debugLog("✓ 数据库类型识别: " + dbType.toUpperCase());
                        LoggerCore.debugLog("✓ Wall过滤器配置: MERGE=" + wallConfig.isMergeAllow() +
                            ", INSERT=" + wallConfig.isInsertAllow() +
                            ", UPDATE=" + wallConfig.isUpdateAllow() +
                            ", DELETE=" + wallConfig.isDeleteAllow());
                    }

                    // 初始化连接池
                    ds.init();
                } catch (Exception e) {
                    throw new RuntimeException("Druid连接池初始化失败: " + e.getMessage(), e);
                }

                return ds;
            }

            // 获取连接
            @Override
            public Connection getConnection() throws SQLException {
                return druidDataSource.getConnection();
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return druidDataSource.getConnection(username, password);
            }

            @Override
            public java.io.PrintWriter getLogWriter() throws SQLException {
                return druidDataSource.getLogWriter();
            }

            @Override
            public void setLogWriter(java.io.PrintWriter out) throws SQLException {
                druidDataSource.setLogWriter(out);
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {
                druidDataSource.setLoginTimeout(seconds);
            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return druidDataSource.getLoginTimeout();
            }

            @Override
            public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
                return druidDataSource.getParentLogger();
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return druidDataSource.unwrap(iface);
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return druidDataSource.isWrapperFor(iface);
            }

            // 获取监控信息
            public Map<String, Object> getMonitorData() {
                Map<String, Object> data = new HashMap<>();
                data.put("activeCount", druidDataSource.getActiveCount());
                data.put("poolingCount", druidDataSource.getPoolingCount());
                data.put("connectCount", druidDataSource.getConnectCount());
                data.put("closeCount", druidDataSource.getCloseCount());
                data.put("createTime", createTime);
                data.put("url", config.getUrl());
                data.put("username", config.getUsername());
                return data;
            }

            // 连接池状态检查
            public boolean isHealthy() {
                try {
                    return !druidDataSource.isClosed();
                } catch (Exception e) {
                    return false;
                }
            }

            // 优雅关闭连接池
            public void close() {
                if (druidDataSource != null && !druidDataSource.isClosed()) {
                    druidDataSource.close();
                    LoggerCore.debugLog("✓ Druid连接池已关闭: " + config.getUrl());
                }
            }
        }

        // DataSourceCore：私有构造函数，防止实例化
        private DataSourceCore() {
            throw new UnsupportedOperationException("工具类不允许实例化");
        }

        // ==================== Core2：源注册 =====================

        // 源注册：手动配置
        public static DatabaseAccessor register(String dbName, String dbUrl, String username, String password, String driver) {
            try {
                // 创建数据库配置对象
                DatabaseConfig config = new DatabaseConfig(dbUrl, username, password, driver);
                LoggerCore.debugLog("✓ 数据库配置对象创建成功");

                // 注册配置
                registerInternal(dbName, config);
                LoggerCore.debugLog("✓ 数据库配置注册成功: " + dbName);

                // 返回链式调用
                return new DatabaseAccessor(dbName);
            } catch (Exception e) {
                LoggerCore.debugErrorLog("手动注册数据库失败: " + e.getMessage());
                throw new RuntimeException("数据库注册失败，无法使用: unknown", e);
            }
        }

        // 源注册：yml文件
        public static DatabaseAccessor registerYml(String dbName) {
            try {
                InputStream inputStream = DatabaseUtil32.class.getClassLoader().getResourceAsStream("application-dev.yml");
                // 检查配置文件是否存在
                if (inputStream == null) {
                    throw new RuntimeException("找不到YML配置文件: application-dev.yml");
                }

                // 使用SnakeYAML解析配置文件
                Yaml yaml = new Yaml();
                Map<String, Object> yamlData = yaml.load(inputStream);

                // 逐级导航到数据源配置
                @SuppressWarnings("unchecked")
                Map<String, Object> spring = (Map<String, Object>) yamlData.get("spring");
                if (spring == null) {
                    throw new RuntimeException("配置文件中未找到spring配置节点");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
                if (datasource == null) {
                    throw new RuntimeException("配置文件中未找到datasource配置节点");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> dynamic = (Map<String, Object>) datasource.get("dynamic");
                if (dynamic == null) {
                    throw new RuntimeException("配置文件中未找到dynamic配置节点");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> datasources = (Map<String, Object>) dynamic.get("datasource");
                if (datasources == null) {
                    throw new RuntimeException("配置文件中未找到datasource列表配置");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> targetDataSource = (Map<String, Object>) datasources.get(dbName);
                if (targetDataSource == null) {
                    throw new RuntimeException("配置文件中未找到数据源配置: " + dbName);
                }

                // 提取数据源
                DatabaseConfig config = new DatabaseConfig(
                    targetDataSource.get("url").toString(),
                    targetDataSource.get("username").toString(),
                    targetDataSource.get("password").toString(),
                    targetDataSource.get("driver-class-name").toString()
                );

                // 注册数据源
                registerInternal(dbName, config);
                LoggerCore.debugLog("从YAML注册数据库成功: " + dbName);

                // 返回链式调用
                return new DatabaseAccessor(dbName);
            } catch (Exception e) {
                LoggerCore.debugErrorLog("从YAML注册数据库失败: " + e.getMessage());
                throw new RuntimeException("从YAML注册数据库失败", e);
            }
        }

        // 源注册：内部实现 - 使用StampedLock减少锁竞争
        private static void registerInternal(String dbName, DatabaseConfig config) {
            long stamp = DATA_SOURCE_LOCK.writeLock(); // 获取写锁
            try {
                // 如果数据源已存在，先关闭
                DataSource existingDs = DATA_SOURCES.get(dbName);
                if (existingDs != null) {
                    LoggerCore.debugLog("替换现有数据源: " + dbName);
                    // 如果是Druid数据源，需要优雅关闭
                    if (existingDs instanceof DruidConnectionPool) {
                        ((DruidConnectionPool) existingDs).close();
                    }
                }

                // 根据配置选择数据源类型
                DataSource dataSource;
                boolean useDruid = THREAD_DRUID_ENABLED.get();

                if (useDruid) {
                    try {
                        // 尝试创建Druid数据源
                        dataSource = new DruidConnectionPool(config);
                        LoggerCore.debugLog("✓ 使用Druid连接池创建数据源: " + dbName);
                    } catch (Exception e) {
                        // 如果Druid创建失败，回退到简单数据源
                        LoggerCore.warnLog("⚠️ Druid数据源创建失败，回退到简单数据源: " + e.getMessage());
                        dataSource = new SimpleDataSource(config);
                        LoggerCore.debugLog("✓ 使用简单数据源作为备选方案: " + dbName);
                    }
                } else {
                    // 直接使用简单数据源
                    dataSource = new SimpleDataSource(config);
                    LoggerCore.debugLog("✓ 使用简单数据源创建: " + dbName);
                }

                // 释放写锁，测试连接（减少锁持有时间）
                DATA_SOURCE_LOCK.unlockWrite(stamp);
                try (Connection testConn = dataSource.getConnection()) {
                    LoggerCore.debugLog("数据源连接测试成功: " + dbName);
                }

                // 重新获取写锁进行最终注册
                stamp = DATA_SOURCE_LOCK.writeLock();

                // 添加数据源
                DATA_SOURCES.put(dbName, dataSource);

                // 原子操作更新默认数据源
                if (ATOMIC_DEFAULT_SOURCE.get() == null) {
                    ATOMIC_DEFAULT_SOURCE.set(dbName);
                    LoggerCore.debugLog("设置默认数据库: " + dbName);
                }

            } catch (Exception e) {
                throw new RuntimeException("注册数据库失败: " + dbName, e);
            } finally {
                DATA_SOURCE_LOCK.unlock(stamp); // 确保锁被释放
            }
        }

        // ==================== Core3：源连接 =====================

        // 源连接：默认 - 集成线程安全机制
        public static Connection getConnection() throws SQLException {
            return getConnection(null);
        }

        // 源连接：指定 - 使用StampedLock优化读操作
        public static Connection getConnection(String dbName) throws SQLException {
            // 获取有效的数据源名称（线程优先）
            String effectiveDbName = getEffectiveDataSourceName(dbName);

            if (effectiveDbName == null) {
                throw new SQLException("未找到有效的数据源配置");
            }

            // 使用乐观读锁
            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            DataSource dataSource = DATA_SOURCES.get(effectiveDbName);

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                // 乐观读失败，使用悲观读锁
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    dataSource = DATA_SOURCES.get(effectiveDbName);
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }

            if (dataSource == null) {
                throw new RuntimeException("数据库未注册: " + effectiveDbName);
            }

            try {
                Connection conn = dataSource.getConnection();
                LoggerCore.debugLog("✓ 获取数据库连接: " + effectiveDbName + " (线程: " + Thread.currentThread().getName() + ")");
                return conn;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ 获取数据库连接失败: " + effectiveDbName + " - " + e.getMessage());
                throw e;
            }
        }

        // ==================== Core4：源注销 =====================

        // 源注销：指定 - 添加锁保护
        public static void closeDatabase(String dbName) {
            long stamp = DATA_SOURCE_LOCK.writeLock();
            try {
                DataSource dataSource = DATA_SOURCES.remove(dbName);
                if (dataSource != null) {
                    LoggerCore.debugLog("数据库已关闭: " + dbName);

                    // 如果关闭的是默认数据源，尝试设置新的默认数据源
                    if (dbName.equals(ATOMIC_DEFAULT_SOURCE.get())) {
                        String newDefault = DATA_SOURCES.keySet().stream().findFirst().orElse(null);
                        ATOMIC_DEFAULT_SOURCE.set(newDefault);
                        LoggerCore.debugLog("默认数据源已切换至: " + newDefault);
                    }
                }
            } finally {
                DATA_SOURCE_LOCK.unlockWrite(stamp);
            }
        }

        // 源注销：全部
        public static void closeAllDatabases() {
            long stamp = DATA_SOURCE_LOCK.writeLock();
            try {
                for (Map.Entry<String, DataSource> entry : DATA_SOURCES.entrySet()) {
                    try {
                        LoggerCore.debugLog("关闭数据库: " + entry.getKey());
                    } catch (Exception e) {
                        LoggerCore.debugErrorLog("关闭数据库失败: " + entry.getKey() + ", " + e.getMessage());
                    }
                }
                DATA_SOURCES.clear();
                ATOMIC_DEFAULT_SOURCE.set(null);
                LoggerCore.debugLog("所有数据库连接已关闭");
            } finally {
                DATA_SOURCE_LOCK.unlockWrite(stamp);
            }
        }

        // ==================== Core5：源状态 =====================

        // 源状态：获取已注册的数据库列表 - 使用乐观读锁
        public static List<String> getRegisteredDatabases() {
            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            List<String> result = new ArrayList<>(DATA_SOURCES.keySet());

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                // 乐观读失败，使用悲观读锁
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    result = new ArrayList<>(DATA_SOURCES.keySet());
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }

            return result;
        }

        // 源状态：获取已注册的数据库名称集合
        public static Set<String> getDataSourceNames() {
            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            Set<String> result = DATA_SOURCES.keySet();

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                // 乐观读失败，使用悲观读锁
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    result = DATA_SOURCES.keySet();
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }
            return result;
        }

        // 源状态：获取默认数据库名称 - 无锁原子读取
        public static String getDefaultDatabaseName() {
            return ATOMIC_DEFAULT_SOURCE.get();
        }

        // 源状态：检查数据源是否已经注册 - 使用乐观读锁
        public static boolean isDatabaseRegistered(String dataSourceName) {
            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            boolean result = DATA_SOURCES.containsKey(dataSourceName);

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                // 乐观读失败，使用悲观读锁
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    result = DATA_SOURCES.containsKey(dataSourceName);
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }

            return result;
        }

        // 源状态：测试指定数据库连接 - 集成线程安全机制
        public static boolean testConnection(String dbName) {
            try (Connection conn = getConnection(dbName)) {
                return conn != null && !conn.isClosed();
            } catch (Exception e) {
                LoggerCore.debugErrorLog("测试连接失败: " + dbName + " - " + e.getMessage());
                return false;
            }
        }

        // 源状态：测试默认数据库连接
        public static boolean testConnection() {
            return testConnection(getEffectiveDataSourceName(null));
        }

        // ==================== Core6：源保护 =====================

        // 线程安全的数据源名称获取
        private static String getEffectiveDataSourceName(String requestedName) {
            // 优先使用线程上下文中的数据源
            String threadLocal = THREAD_DATASOURCE_CONTEXT.get();
            if (threadLocal != null) {
                return threadLocal;
            }

            // 其次使用传入的数据源名称
            if (requestedName != null && !requestedName.trim().isEmpty()) {
                return requestedName;
            }

            // 最后使用原子引用的默认数据源
            return ATOMIC_DEFAULT_SOURCE.get();
        }

        // 线程安全的默认数据源设置
        public static boolean setDefaultDataSource(String dataSourceName) {
            if (dataSourceName == null || !DATA_SOURCES.containsKey(dataSourceName)) {
                return false;
            }

            ATOMIC_DEFAULT_SOURCE.set(dataSourceName);
            LoggerCore.debugLog("✓ 全局默认数据源已切换至: " + dataSourceName);
            return true;
        }

        // 线程级别的数据源上下文设置
        public static void setThreadDataSourceContext(String dataSourceName) {
            if (dataSourceName != null && !DATA_SOURCES.containsKey(dataSourceName)) {
                throw new IllegalArgumentException("数据源不存在: " + dataSourceName);
            }

            THREAD_DATASOURCE_CONTEXT.set(dataSourceName);
            LoggerCore.debugLog("✓ 线程数据源上下文已设置: " + dataSourceName + " (线程: " + Thread.currentThread().getName() + ")");
        }

        // 清除线程级别的数据源上下文
        public static void clearThreadDataSourceContext() {
            THREAD_DATASOURCE_CONTEXT.remove();
            LoggerCore.debugLog("✓ 线程数据源上下文已清除 (线程: " + Thread.currentThread().getName() + ")");
        }

        // 获取当前生效的数据源名称
        public static String getCurrentDataSourceName() {
            return getEffectiveDataSourceName(null);
        }

        // ==================== Core7：数据源类型管理 =====================

        // 设置线程级别的数据源类型偏好
        public static void setThreadDruidEnabled(boolean enabled) {
            THREAD_DRUID_ENABLED.set(enabled);
            LoggerCore.debugLog("✓ 线程数据源类型已设置: " + (enabled ? "Druid" : "Simple") +
                " (线程: " + Thread.currentThread().getName() + ")");
        }

        // 清除线程级别的数据源类型偏好
        public static void clearThreadDruidEnabled() {
            THREAD_DRUID_ENABLED.remove();
            LoggerCore.debugLog("✓ 线程数据源类型已重置为全局配置 (线程: " + Thread.currentThread().getName() + ")");
        }

        // 获取当前线程的数据源类型偏好
        public static boolean isThreadDruidEnabled() {
            return THREAD_DRUID_ENABLED.get();
        }

        // 获取数据源类型信息
        public static String getDataSourceType(String dbName) {
            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            DataSource dataSource = DATA_SOURCES.get(dbName);

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    dataSource = DATA_SOURCES.get(dbName);
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }

            if (dataSource == null) {
                return "未注册";
            }

            return dataSource instanceof DruidConnectionPool ? "Druid连接池" : "简单数据源";
        }

        // 获取所有数据源的详细信息
        public static Map<String, Map<String, Object>> getAllDataSourceInfo() {
            Map<String, Map<String, Object>> result = new HashMap<>();

            long stamp = DATA_SOURCE_LOCK.tryOptimisticRead();
            Map<String, DataSource> sources = new HashMap<>(DATA_SOURCES);

            if (!DATA_SOURCE_LOCK.validate(stamp)) {
                stamp = DATA_SOURCE_LOCK.readLock();
                try {
                    sources = new HashMap<>(DATA_SOURCES);
                } finally {
                    DATA_SOURCE_LOCK.unlockRead(stamp);
                }
            }

            for (Map.Entry<String, DataSource> entry : sources.entrySet()) {
                String dbName = entry.getKey();
                DataSource dataSource = entry.getValue();

                Map<String, Object> info = new HashMap<>();
                info.put("type", dataSource instanceof DruidConnectionPool ? "Druid" : "Simple");
                info.put("isDefault", dbName.equals(ATOMIC_DEFAULT_SOURCE.get()));
                info.put("connectionTest", testConnection(dbName));

                // 如果是Druid数据源，添加监控信息
                if (dataSource instanceof DruidConnectionPool) {
                    try {
                        Map<String, Object> monitorData = ((DruidConnectionPool) dataSource).getMonitorData();
                        info.putAll(monitorData);
                    } catch (Exception e) {
                        info.put("monitorError", e.getMessage());
                    }
                }

                result.put(dbName, info);
            }

            return result;
        }

        // 强制重新创建指定数据源（用于切换数据源类型）
        public static boolean recreateDataSource(String dbName, boolean useDruid) {
            long stamp = DATA_SOURCE_LOCK.writeLock();
            try {
                DataSource existingDs = DATA_SOURCES.get(dbName);
                if (existingDs == null) {
                    LoggerCore.debugErrorLog("数据源不存在，无法重建: " + dbName);
                    return false;
                }

                // 从现有数据源中提取配置信息
                DatabaseConfig config = extractConfigFromDataSource(existingDs);
                if (config == null) {
                    LoggerCore.debugErrorLog("无法从现有数据源提取配置: " + dbName);
                    return false;
                }

                // 关闭现有数据源
                if (existingDs instanceof DruidConnectionPool) {
                    ((DruidConnectionPool) existingDs).close();
                }

                // 创建新的数据源
                DataSource newDataSource;
                if (useDruid) {
                    newDataSource = new DruidConnectionPool(config);
                    LoggerCore.debugLog("✓ " + dbName + " 已重建为Druid数据源");
                } else {
                    newDataSource = new SimpleDataSource(config);
                    LoggerCore.debugLog("✓ " + dbName + " 已重建为简单数据源");
                }

                // 测试新连接
                try (Connection testConn = newDataSource.getConnection()) {
                    LoggerCore.debugLog("新数据源连接测试成功: " + dbName);
                }

                // 替换数据源
                DATA_SOURCES.put(dbName, newDataSource);
                return true;

            } catch (Exception e) {
                LoggerCore.debugErrorLog("重建数据源失败: " + dbName + " - " + e.getMessage());
                return false;
            } finally {
                DATA_SOURCE_LOCK.unlockWrite(stamp);
            }
        }

        // 从数据源中提取配置信息的辅助方法
        private static DatabaseConfig extractConfigFromDataSource(DataSource dataSource) {
            // 这里需要根据实际情况实现配置提取逻辑
            // 由于数据源接口不提供配置信息，我们可能需要维护一个配置映射
            // 或者在创建时保存配置信息
            LoggerCore.warnLog("⚠️ 配置提取功能需要进一步实现");
            return null;
        }
    }

    // ==================== Extend：链式SQL ====================

    // 链式访问器：重构版本
    public static class DatabaseAccessor {

        private final String dataSourceName;
        private Connection transactionConnection;
        private boolean inTransaction = false;
        private int affectedRows = 0;
        private int lastAffectedRows = 0;

        public DatabaseAccessor(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        public DatabaseAccessor() {
            this.dataSourceName = null;
        }

        // ==================== 事务管理方法 ====================

        // 开启事务
        public DatabaseAccessor setTransaction() {
            this.affectedRows = 0; // 重置累计计数器
            this.lastAffectedRows = 0; // 重置单次计数器
            try {
                if (inTransaction) {
                    LoggerCore.debugLog("⚠️ 事务已经开启，无需重复开启");
                    return this;
                }

                if (dataSourceName != null) {
                    transactionConnection = DataSourceCore.getConnection(dataSourceName);
                } else {
                    transactionConnection = DataSourceCore.getConnection(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get());
                }

                transactionConnection.setAutoCommit(false);
                inTransaction = true;

                LoggerCore.debugLog("✓ 事务已开启，数据源: " + (dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get()));
                return this;

            } catch (SQLException e) {
                throw new RuntimeException("开启事务失败: " + e.getMessage(), e);
            }
        }

        // 提交事务
        public int commit() {
            try {
                if (!inTransaction || transactionConnection == null) {
                    LoggerCore.debugLog("⚠️ 没有活跃的事务需要提交");
                    return 0;
                }

                transactionConnection.commit();
                LoggerCore.debugLog("✓ 事务提交成功，总影响行数: " + affectedRows);

                int result = affectedRows;
                closeTransaction();
                return result;

            } catch (SQLException e) {
                try {
                    if (transactionConnection != null) {
                        transactionConnection.rollback();
                        LoggerCore.debugErrorLog("⚠️ 提交失败，已自动回滚");
                    }
                } catch (SQLException rollbackEx) {
                    LoggerCore.debugErrorLog("❌ 回滚也失败: " + rollbackEx.getMessage());
                }
                closeTransaction();
                throw new RuntimeException("提交事务失败: " + e.getMessage(), e);
            }
        }

        // 回滚事务
        public DatabaseAccessor rollback() {
            try {
                if (!inTransaction || transactionConnection == null) {
                    LoggerCore.debugLog("⚠️ 没有活跃的事务需要回滚");
                    return this;
                }

                transactionConnection.rollback();
                LoggerCore.debugLog("✓ 事务回滚成功");

                closeTransaction();
                return this;

            } catch (SQLException e) {
                closeTransaction();
                throw new RuntimeException("回滚事务失败: " + e.getMessage(), e);
            }
        }

        // 清理事务状态
        private void closeTransaction() {
            try {
                if (transactionConnection != null) {
                    transactionConnection.setAutoCommit(true);
                    transactionConnection.close();
                }
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("⚠️ 关闭事务连接时出错: " + e.getMessage());
            } finally {
                transactionConnection = null;
                inTransaction = false;
                affectedRows = 0; // 清理结果
                lastAffectedRows = 0;
            }
        }

        // 检查是否在事务中
        public boolean isInTransaction() {
            return inTransaction;
        }

        // 获取最近一次写操作影响的行数
        public int result() {
            return this.lastAffectedRows;
        }

        // 获取累计影响的总行数
        public int resultAll() {
            return this.affectedRows;
        }

        // ==================== 重构：数据库操作策略 ====================

        // SQL转换策略枚举
        public enum SqlStrategy {
            DIRECT_SQL,    // 直接SQL
            XML_AUTO,      // 自动搜索XML
            XML_FILE       // 指定XML文件
        }

        // 操作类型枚举
        public enum OperationType {
            SELECT_LIST,   // 查询列表
            SELECT_ONE,    // 查询单条
            INSERT,        // 插入
            UPDATE,        // 更新
            DELETE         // 删除
        }

        // 数据库操作工厂
        private static class DatabaseOperationFactory {

            // 创建SQL转换策略
            public static SqlParser.SqlWithParams createSqlWithParams(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params) {
                switch (strategy) {
                    case DIRECT_SQL:
                        return SqlParser.SqlParameterConverter.convertDirectSql(primary, params);
                    case XML_AUTO:
                        return SqlParser.SqlParameterConverter.convertXmlAuto(primary, params);
                    case XML_FILE:
                        return SqlParser.SqlParameterConverter.convertXmlFile(primary, secondary, params);
                    default:
                        throw new IllegalArgumentException("不支持的SQL策略: " + strategy);
                }
            }
        }

        // 统一的执行模板方法
        private <T> T executeOperation(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params, OperationType operationType) {
            // 1. 转换SQL
            SqlParser.SqlWithParams sqlWithParams = DatabaseOperationFactory.createSqlWithParams(strategy, primary, secondary, params);

            // 2. 选择执行模式
            if (inTransaction) {
                return executeWithTransaction(sqlWithParams, operationType);
            } else {
                return executeWithoutTransaction(sqlWithParams, operationType);
            }
        }

        // 事务模式执行
        @SuppressWarnings("unchecked")
        private <T> T executeWithTransaction(SqlParser.SqlWithParams sqlWithParams, OperationType operationType) {
            switch (operationType) {
                case SELECT_LIST:
                    return (T) executeSelectListWithConnection(transactionConnection, sqlWithParams.sql, sqlWithParams.params);
                case SELECT_ONE:
                    List<Map<String, Object>> results = executeSelectListWithConnection(transactionConnection, sqlWithParams.sql, sqlWithParams.params);
                    return (T) (results.isEmpty() ? null : results.get(0));
                case INSERT:
                case UPDATE:
                case DELETE:
                    return (T) Integer.valueOf(executeUpdateWithConnection(transactionConnection, sqlWithParams.sql, sqlWithParams.params));
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType);
            }
        }

        // 非事务模式执行
        @SuppressWarnings("unchecked")
        private <T> T executeWithoutTransaction(SqlParser.SqlWithParams sqlWithParams, OperationType operationType) {
            // 修复：使用 getEffectiveDataSourceName 确保正确处理线程级、指定级和默认数据源
            String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(this.dataSourceName);

            switch (operationType) {
                case SELECT_LIST:
                    return (T) SqlExecutor.selectList(effectiveDataSource, sqlWithParams.sql, sqlWithParams.params);
                case SELECT_ONE:
                    return (T) SqlExecutor.selectOne(effectiveDataSource, sqlWithParams.sql, sqlWithParams.params);
                case INSERT:
                    return (T) Integer.valueOf(SqlExecutor.insert(effectiveDataSource, sqlWithParams.sql, sqlWithParams.params));
                case UPDATE:
                    return (T) Integer.valueOf(SqlExecutor.update(effectiveDataSource, sqlWithParams.sql, sqlWithParams.params));
                case DELETE:
                    return (T) Integer.valueOf(SqlExecutor.delete(effectiveDataSource, sqlWithParams.sql, sqlWithParams.params));
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType);
            }
        }

        // ==================== DDL：直接操作数据库 ====================

        // DDL：直接操作数据库
        public DatabaseAccessor executeDDL(String sql) {
            String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
            try (Connection conn = DataSourceCore.getConnection(effectiveDataSource);
                Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                LoggerCore.debugLog("✓ DDL 执行成功, 数据源: " + effectiveDataSource);
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DDL 执行失败: " + e.getMessage());
                throw new RuntimeException("DDL 执行失败", e);
            }
            return this;
        }

        // ==================== API1：直接SQL ====================

        // 执行查询，返回列表
        public List<Map<String, Object>> selectList(String sql, Map<String, Object> params) {
            return executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_LIST);
        }

        // 执行查询，返回单个对象
        public Map<String, Object> selectOne(String sql, Map<String, Object> params) {
            return executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_ONE);
        }

        // 执行插入操作
        public DatabaseAccessor insert(String sql, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.INSERT);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行更新操作
        public DatabaseAccessor update(String sql, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.UPDATE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行删除操作
        public DatabaseAccessor delete(String sql, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.DELETE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // ==================== API2：指定具体XML ====================

        // 执行查询，返回列表（指定具体XML）
        public List<Map<String, Object>> selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_LIST);
        }

        // 执行查询，返回单个对象（指定具体XML）
        public Map<String, Object> selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_ONE);
        }

        // 执行插入操作（指定具体XML）
        public DatabaseAccessor insertFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.INSERT);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行更新操作（指定具体XML）
        public DatabaseAccessor updateFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.UPDATE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行删除操作（指定具体XML）
        public DatabaseAccessor deleteFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.DELETE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // ==================== API3：自动搜索XML ====================

        // 执行查询，返回列表（AUTO XML）
        public List<Map<String, Object>> selectListFromXmlAuto(String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_LIST);
        }

        // 执行查询，返回单个对象（AUTO XML）
        public Map<String, Object> selectOneFromXmlAuto(String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_ONE);
        }

        // 执行插入操作（AUTO XML）
        public DatabaseAccessor insertFromXmlAuto(String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.INSERT);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行更新操作（AUTO XML）
        public DatabaseAccessor updateFromXmlAuto(String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.UPDATE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // 执行删除操作（AUTO XML）
        public DatabaseAccessor deleteFromXmlAuto(String sqlId, Map<String, Object> params) {
            int rows = (int) executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.DELETE);
            this.lastAffectedRows = rows;
            this.affectedRows += rows;
            return this;
        }

        // ==================== 私有辅助方法 ====================

        // 设置PreparedStatement参数
        private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
        }

        // 使用指定连接执行查询
        private List<Map<String, Object>> executeSelectListWithConnection(Connection conn, String sql, Object... params) {
            List<Map<String, Object>> results = new ArrayList<>();
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                setParameters(pstmt, params);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            map.put(columnName, value);
                        }
                        results.add(map);
                    }
                }

                LoggerCore.debugLog("✓ 事务查询完成，返回 " + results.size() + " 条记录");
                return results;

            } catch (SQLException e) {
                throw new RuntimeException("事务查询执行失败: " + e.getMessage(), e);
            }
        }

        // 使用指定连接执行更新
        private int executeUpdateWithConnection(Connection conn, String sql, Object... params) {
            String processedSql = SqlParser.smartTrimSqlSemicolon(sql);

            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();

                LoggerCore.debugLog("✓ 事务更新完成，影响 " + rows + " 行");
                return rows;

            } catch (SQLException e) {
                throw new RuntimeException("事务更新执行失败: " + e.getMessage(), e);
            }
        }
    }

    // 链式访问器：默认数据源
    public static DatabaseAccessor accessor() {
        return new DatabaseAccessor();
    }

    // 链式访问器：指定数据源
    public static DatabaseAccessor accessor(String dataSourceName) {
        return new DatabaseAccessor(dataSourceName);
    }

    // ==================== DDL：直接操作数据库 ===================

    // DDL：直接操作数据库
    public static void executeDDL(String sql) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        try (Connection conn = DataSourceCore.getConnection(effectiveDataSource);
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LoggerCore.debugLog("✓ DDL 执行成功, 数据源: " + effectiveDataSource);
        } catch (SQLException e) {
            LoggerCore.debugErrorLog("❌ DDL 执行失败: " + e.getMessage());
            throw new RuntimeException("DDL 执行失败", e);
        }
    }

    // ==================== API1：默认数据库 ====================

    // 全部：默认数据库
    public static List<Map<String, Object>> selectList(String sql, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertDirectSql(sql, params);
        return SqlExecutor.selectList(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 单条：默认数据库
    public static Map<String, Object> selectOne(String sql, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertDirectSql(sql, params);
        return SqlExecutor.selectOne(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 插入：默认数据库
    public static int insert(String sql, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertDirectSql(sql, params);
        return SqlExecutor.insert(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 更新：默认数据库
    public static int update(String sql, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertDirectSql(sql, params);
        return SqlExecutor.update(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 删除：默认数据库
    public static int delete(String sql, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertDirectSql(sql, params);
        return SqlExecutor.delete(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // ==================== API2：指定具体XML ===================

    // 全部：指定具体XML
    public static List<Map<String, Object>> selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
        return SqlExecutor.selectList(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 单条：指定具体XML
    public static Map<String, Object> selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
        List<Map<String, Object>> results = SqlExecutor.selectList(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
        return results.isEmpty() ? null : results.get(0);
    }

    // 插入：指定具体XML
    public static int insertFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
        return SqlExecutor.insert(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 更新：指定具体XML
    public static int updateFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
        return SqlExecutor.update(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 删除：指定具体XML
    public static int deleteFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
        return SqlExecutor.delete(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // ==================== API3：自动搜索XML ===================

    // 全部：自动搜索XML
    public static List<Map<String, Object>> selectListFromXmlAuto(String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlAuto(sqlId, params);
        return SqlExecutor.selectList(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 单条：自动搜索XML
    public static Map<String, Object> selectOneFromXmlAuto(String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlAuto(sqlId, params);
        List<Map<String, Object>> results = SqlExecutor.selectList(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
        return results.isEmpty() ? null : results.get(0);
    }

    // 插入：自动搜索XML
    public static int insertFromXmlAuto(String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlAuto(sqlId, params);
        return SqlExecutor.insert(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 更新：自动搜索XML
    public static int updateFromXmlAuto(String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlAuto(sqlId, params);
        return SqlExecutor.update(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }

    // 删除：自动搜索XML
    public static int deleteFromXmlAuto(String sqlId, Map<String, Object> params) {
        SqlParser.SqlWithParams sqlWithParams = SqlParser.SqlParameterConverter.convertXmlAuto(sqlId, params);
        return SqlExecutor.delete(DataSourceCore.ATOMIC_DEFAULT_SOURCE.get(), sqlWithParams.sql, sqlWithParams.params);
    }
}
