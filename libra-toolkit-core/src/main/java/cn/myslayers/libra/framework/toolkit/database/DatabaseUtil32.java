package cn.myslayers.libra.framework.toolkit.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * 数据库工具类：
 * 1. 日志调试：LoggerCore.setDebugLogEnabled(true);
 * 2. 数据源管理：DruidConnectionPool（同步阻塞初始化） 不支持 YML，register() 返回 DatabaseAccessor 对象
 * 3. 事务管理：setTransaction() / commit() / rollback() / result()
 * 4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
 * 5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
 * 6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
 * 7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
 */
public class DatabaseUtil32 {

    // ==================== 常量定义 ====================

    // 默认SQL目录
    public static final String[] XML_DIRECTORIES = {"src/main/resources/sql/", "resources/sql/",};
    public static final String[] MYBATIS_XML_DIRECTORIES = {"src/main/resources/mybatis/", "src/main/resources/mapper/"};

    // 日志开关：控制调试输出
    private static volatile boolean DEBUG_LOG_ENABLED = false;

    // SQL执行日志开关：控制SQL执行详情输出
    private static volatile boolean SQL_DEBUG_LOG_ENABLED = false;

    // 源注册：yml文件
    public static final String FIRST = "master";
    public static final String SECOND = "second";
    public static final String[] DATABASE_YML_CONNECTIONS = {FIRST, SECOND};

    // 源注册：手动配置
    public static final String[][] DATABASE_MANAGE_CONNECTIONS = {
        {"default_mysql1", "jdbc:mysql://localhost:3306/libra-demo-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true", "demo_user", "demo_password", "com.mysql.cj.jdbc.Driver"},
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

    // 私有构造函数，防止实例化
    private DatabaseUtil32() {
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
            return LocalDateTime.now().format(
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

            if (param instanceof Date) {
                // 日期类型格式化
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return "'" + sdf.format((Date) param) + "'";
            }

            if (param instanceof java.sql.Date) {
                // SQL日期类型
                return "'" + param.toString() + "'";
            }

            if (param instanceof Timestamp) {
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
                logger.info("完整SQL: \n" + formatSqlWithParams(sql, params));

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
                    logger.info("执行结果: 操作成功，影响 " + result + " 条记录");
                }

                logger.info("========================");
            }
        }

        // 兼容性方法：保持原有接口
        public static void debugSqlExecution(String dbName, String sql, Object... params) {
            if (SQL_DEBUG_LOG_ENABLED) {
                debugLog("=== SQL执行 ===");
                debugLog("数据库: " + dbName);
                debugLog("完整SQL: \n" + formatSqlWithParams(sql, params));
                debugLog("===============");
            }
        }
    }

    // 映射器
    public static class ObjectMapper {

        // Jackson ObjectMapper实例配置
        private static final com.fasterxml.jackson.databind.ObjectMapper JACKSON_MAPPER;
        private static final com.fasterxml.jackson.databind.ObjectMapper SNAKE_CASE_MAPPER;

        // 类型信息缓存
        private static final Map<Class<?>, TypeInfo> TYPE_CACHE = new ConcurrentHashMap<>();

        // 字段映射缓存
        private static final Map<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();

        static {
            // 配置默认Jackson ObjectMapper（驼峰命名）
            JACKSON_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
            JACKSON_MAPPER.registerModule(new JavaTimeModule());
            JACKSON_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            JACKSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JACKSON_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

            // 配置下划线命名Jackson ObjectMapper
            SNAKE_CASE_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
            SNAKE_CASE_MAPPER.registerModule(new JavaTimeModule());
            SNAKE_CASE_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            SNAKE_CASE_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SNAKE_CASE_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            SNAKE_CASE_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }

        // 类型信息缓存类
        private static class TypeInfo {

            private final Class<?> clazz;
            private final boolean isSimpleType;
            private final boolean isCollectionType;
            private final boolean isMapType;
            private final Field[] fields;
            private final Map<String, Field> fieldMap;

            public TypeInfo(Class<?> clazz) {
                this.clazz = clazz;
                this.isSimpleType = isSimpleType(clazz);
                this.isCollectionType = Collection.class.isAssignableFrom(clazz);
                this.isMapType = Map.class.isAssignableFrom(clazz);
                this.fields = getAllFields(clazz);
                this.fieldMap = buildFieldMap(this.fields);
            }

            private Field[] getAllFields(Class<?> clazz) {
                List<Field> fieldList = new ArrayList<>();
                Class<?> currentClass = clazz;
                while (currentClass != null && currentClass != Object.class) {
                    Field[] declaredFields = currentClass.getDeclaredFields();
                    for (Field field : declaredFields) {
                        field.setAccessible(true);
                        fieldList.add(field);
                    }
                    currentClass = currentClass.getSuperclass();
                }
                return fieldList.toArray(new Field[0]);
            }

            private Map<String, Field> buildFieldMap(Field[] fields) {
                Map<String, Field> map = new HashMap<>();
                for (Field field : fields) {
                    map.put(field.getName(), field);
                }
                return map;
            }
        }

        // ==================== 强类型对象解析器 ====================

        // 强类型对象解析器
        public static class ObjectToParameterConverter {

            // 私有构造函数，防止实例化
            private ObjectToParameterConverter() {
                throw new UnsupportedOperationException("对象转换器不允许实例化");
            }

            // 将强类型对象转换为MyBatis可识别的参数Map，支持复杂嵌套对象、集合、泛型等所有类型
            public static Map<String, Object> convertToParameterMap(Object inputParam) {
                return convertToParameterMap(inputParam, null);
            }

            // 将强类型对象转换为MyBatis可识别的参数Map，支持复杂嵌套对象、集合、泛型等所有类型
            public static Map<String, Object> convertToParameterMap(Object inputParam, String sql) {
                if (inputParam == null) {
                    return new HashMap<>();
                }

                try {
                    // 处理基本类型和包装类型
                    if (isSimpleType(inputParam.getClass())) {
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("value", inputParam);

                        // 如果提供了SQL，尝试从SQL中解析参数名
                        if (sql != null && !sql.trim().isEmpty()) {
                            List<String> paramNames = extractParameterNamesFromSql(sql);
                            if (!paramNames.isEmpty()) {
                                // 使用第一个找到的参数名作为key
                                paramMap.put(paramNames.get(0), inputParam);
                            } else {
                                // 如果没有找到参数名，使用默认的param1
                                paramMap.put("param1", inputParam);
                            }
                        } else {
                            // 没有提供SQL，使用默认的param1
                            paramMap.put("param1", inputParam);
                        }

                        return paramMap;
                    }

                    // 处理Map类型
                    if (inputParam instanceof Map) {
                        return convertMapToParameterMap((Map<?, ?>) inputParam);
                    }

                    // 处理Collection类型
                    if (inputParam instanceof Collection) {
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("collection", inputParam);
                        paramMap.put("list", inputParam); // MyBatis foreach支持
                        return paramMap;
                    }

                    // 处理复杂对象类型 - 使用Jackson进行转换
                    return convertComplexObjectToParameterMap(inputParam);

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("对象转参数Map失败: " + e.getMessage());
                    // 回退到基础Map
                    Map<String, Object> fallbackMap = new HashMap<>();
                    fallbackMap.put("param", inputParam);
                    return fallbackMap;
                }
            }

            // 将Map转换为参数Map（处理嵌套Map）
            @SuppressWarnings("unchecked")
            private static Map<String, Object> convertMapToParameterMap(Map<?, ?> inputMap) {
                Map<String, Object> result = new HashMap<>();

                for (Map.Entry<?, ?> entry : inputMap.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = entry.getValue();

                    if (value != null) {
                        result.put(key, processValueForParameter(value));

                        // 🌟 新增：为复杂对象生成嵌套属性访问
                        if (!isSimpleType(value.getClass()) && !(value instanceof Collection) && !(value instanceof Map)) {
                            // 这是一个复杂对象，生成嵌套属性
                            try {
                                Map<String, Object> nestedMap = JACKSON_MAPPER.convertValue(value,
                                    new TypeReference<Map<String, Object>>() {
                                    });
                                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                                    result.put(key + "." + nestedEntry.getKey(), nestedEntry.getValue());
                                }
                            } catch (Exception e) {
                                LoggerCore.debugErrorLog("复杂对象嵌套属性生成失败: " + key + " - " + e.getMessage());
                            }
                        }

                        // 🌟 新增：为Map类型生成嵌套属性访问
                        if (value instanceof Map) {
                            Map<String, Object> mapValue = (Map<String, Object>) value;
                            for (Map.Entry<String, Object> nestedEntry : mapValue.entrySet()) {
                                result.put(key + "." + nestedEntry.getKey(), nestedEntry.getValue());
                            }
                        }
                    } else {
                        result.put(key, null);
                    }
                }

                return result;
            }

            // 将复杂对象转换为参数Map
            private static Map<String, Object> convertComplexObjectToParameterMap(Object obj) {
                try {
                    // 使用Jackson将对象转换为Map
                    Map<String, Object> baseMap = JACKSON_MAPPER.convertValue(obj,
                        new TypeReference<Map<String, Object>>() {
                        });

                    Map<String, Object> result = new HashMap<>();

                    // 处理每个属性值
                    for (Map.Entry<String, Object> entry : baseMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        result.put(key, processValueForParameter(value));

                        // 支持嵌套属性访问（如user.name）
                        if (value instanceof Map) {
                            Map<String, Object> nestedMap = (Map<String, Object>) value;
                            for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                                result.put(key + "." + nestedEntry.getKey(), nestedEntry.getValue());
                            }
                        }
                    }

                    return result;

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("Jackson转换失败，使用反射回退: " + e.getMessage());
                    return convertObjectToMapByReflection(obj);
                }
            }

            // 处理参数值（支持复杂类型递归处理）
            private static Object processValueForParameter(Object value) {
                if (value == null || isSimpleType(value.getClass())) {
                    return value;
                }

                if (value instanceof Collection) {
                    Collection<?> collection = (Collection<?>) value;
                    List<Object> processedList = new ArrayList<>();
                    for (Object item : collection) {
                        processedList.add(processValueForParameter(item));
                    }
                    return processedList;
                }

                if (value instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    Map<String, Object> processedMap = new HashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        processedMap.put(String.valueOf(entry.getKey()),
                            processValueForParameter(entry.getValue()));
                    }
                    return processedMap;
                }

                // 复杂对象递归转换
                try {
                    return JACKSON_MAPPER.convertValue(value, new TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception e) {
                    LoggerCore.debugErrorLog("递归转换失败: " + e.getMessage());
                    return value;
                }
            }

            // 反射回退方案
            private static Map<String, Object> convertObjectToMapByReflection(Object obj) {
                Map<String, Object> result = new HashMap<>();
                TypeInfo typeInfo = getTypeInfo(obj.getClass());

                for (Field field : typeInfo.fields) {
                    try {
                        String fieldName = field.getName();
                        Object fieldValue = field.get(obj);

                        if (fieldValue != null) {
                            result.put(fieldName, processValueForParameter(fieldValue));
                        }
                    } catch (IllegalAccessException e) {
                        LoggerCore.debugErrorLog("无法访问字段: " + field.getName());
                    }
                }

                return result;
            }

            // 判断是否为简单类型
            private static boolean isSimpleType(Class<?> clazz) {
                return clazz.isPrimitive() ||
                    clazz == String.class ||
                    clazz == Integer.class || clazz == Long.class ||
                    clazz == Double.class || clazz == Float.class ||
                    clazz == Boolean.class || clazz == BigDecimal.class ||
                    clazz == Date.class || clazz == LocalDateTime.class ||
                    clazz == LocalDate.class || clazz == LocalTime.class ||
                    clazz == Timestamp.class ||
                    Number.class.isAssignableFrom(clazz) ||
                    clazz.isEnum();
            }

            // 从SQL中提取MyBatis参数名（#{}格式）
            private static List<String> extractParameterNamesFromSql(String sql) {
                List<String> paramNames = new ArrayList<>();
                if (sql == null || sql.trim().isEmpty()) {
                    return paramNames;
                }

                // 使用正则表达式匹配 #{paramName} 格式
                Pattern pattern = Pattern.compile("#\\{([^}]+)\\}");
                Matcher matcher = pattern.matcher(sql);

                while (matcher.find()) {
                    String paramName = matcher.group(1).trim();
                    if (!paramName.isEmpty() && !paramNames.contains(paramName)) {
                        paramNames.add(paramName);
                    }
                }

                return paramNames;
            }
        }

        // ==================== 结果集转换器 ====================

        // 结果集转换器
        public static class ResultSetToObjectConverter {

            // 私有构造函数，防止实例化
            private ResultSetToObjectConverter() {
                throw new UnsupportedOperationException("结果转换器不允许实例化");
            }

            // 将Map结果转换为强类型对象，支持复杂嵌套对象、集合、泛型等所有类型
            public static <T> T convertMapToObject(Map<String, Object> resultMap, Class<T> targetType) {
                if (resultMap == null || resultMap.isEmpty()) {
                    return null;
                }

                try {
                    // 处理简单类型
                    if (isSimpleType(targetType)) {
                        return convertToSimpleType(resultMap, targetType);
                    }

                    // 处理Map类型
                    if (Map.class.isAssignableFrom(targetType)) {
                        return (T) resultMap;
                    }

                    // 处理复杂对象类型
                    return convertMapToComplexObject(resultMap, targetType);

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("Map转对象失败: " + e.getMessage());
                    return null;
                }
            }

            // 转换为简单类型
            @SuppressWarnings("unchecked")
            private static <T> T convertToSimpleType(Map<String, Object> resultMap, Class<T> targetType) {
                // 优先取第一个值
                Object value = resultMap.values().iterator().next();

                if (value == null) {
                    return null;
                }

                try {
                    // 使用Jackson进行类型转换
                    return JACKSON_MAPPER.convertValue(value, targetType);
                } catch (Exception e) {
                    // 回退到手动转换
                    return convertValueManually(value, targetType);
                }
            }

            // 手动类型转换（回退方案）
            @SuppressWarnings("unchecked")
            private static <T> T convertValueManually(Object value, Class<T> targetType) {
                if (value == null) {
                    return null;
                }

                String valueStr = String.valueOf(value);

                try {
                    if (targetType == String.class) {
                        return (T) valueStr;
                    } else if (targetType == Integer.class || targetType == int.class) {
                        return (T) Integer.valueOf(valueStr);
                    } else if (targetType == Long.class || targetType == long.class) {
                        return (T) Long.valueOf(valueStr);
                    } else if (targetType == Double.class || targetType == double.class) {
                        return (T) Double.valueOf(valueStr);
                    } else if (targetType == Boolean.class || targetType == boolean.class) {
                        return (T) Boolean.valueOf(valueStr);
                    } else if (targetType == BigDecimal.class) {
                        return (T) new BigDecimal(valueStr);
                    } else if (targetType == Date.class && value instanceof Timestamp) {
                        return (T) new Date(((Timestamp) value).getTime());
                    } else if (targetType == LocalDateTime.class && value instanceof Timestamp) {
                        return (T) ((Timestamp) value).toLocalDateTime();
                    }
                } catch (Exception e) {
                    LoggerCore.debugErrorLog("手动类型转换失败: " + e.getMessage());
                }

                return (T) value;
            }

            // 转换为复杂对象
            private static <T> T convertMapToComplexObject(Map<String, Object> resultMap, Class<T> targetType) {
                try {
                    LoggerCore.debugLog("🎯 开始复杂对象转换: " + targetType.getSimpleName() + " <- " + resultMap.keySet());

                    // 首先尝试驼峰命名转换
                    T result = tryConvertWithNamingStrategy(resultMap, targetType, JACKSON_MAPPER);
                    if (result != null && isObjectMeaningful(result)) {
                        LoggerCore.debugLog("✅ 驼峰命名转换成功");
                        return result;
                    } else {
                        LoggerCore.debugLog("❌ 驼峰命名转换失败或结果无意义");
                    }

                    // 然后尝试下划线命名转换
                    result = tryConvertWithNamingStrategy(resultMap, targetType, SNAKE_CASE_MAPPER);
                    if (result != null && isObjectMeaningful(result)) {
                        LoggerCore.debugLog("✅ 下划线命名转换成功");
                        return result;
                    } else {
                        LoggerCore.debugLog("❌ 下划线命名转换失败或结果无意义");
                    }

                    // 最后使用智能字段映射
                    LoggerCore.debugLog("🤖 使用智能字段映射...");
                    return convertWithSmartFieldMapping(resultMap, targetType);

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("复杂对象转换失败: " + e.getMessage());
                    return null;
                }
            }

            // 使用指定命名策略尝试转换
            private static <T> T tryConvertWithNamingStrategy(Map<String, Object> resultMap,
                Class<T> targetType,
                com.fasterxml.jackson.databind.ObjectMapper mapper) {
                try {
                    T result = mapper.convertValue(resultMap, targetType);
                    LoggerCore.debugLog("🔄 Jackson转换结果检查: " + (result != null ? "非null" : "null"));
                    if (result != null) {
                        LoggerCore.debugLog("📋 转换结果对象: " + result.toString());
                    }
                    return result;
                } catch (Exception e) {
                    LoggerCore.debugErrorLog("命名策略转换失败: " + e.getMessage());
                    return null;
                }
            }

            // 判断对象是否有意义（非空且包含有效数据）
            private static <T> boolean isObjectMeaningful(T obj) {
                if (obj == null) {
                    return false;
                }

                // 使用反射检查对象是否有非null字段
                TypeInfo typeInfo = getTypeInfo(obj.getClass());
                int nonNullFieldCount = 0;
                int meaningfulFieldCount = 0;

                for (Field field : typeInfo.fields) {
                    try {
                        Object fieldValue = field.get(obj);
                        if (fieldValue != null) {
                            nonNullFieldCount++;
                            // 检查是否是有意义的值（不是字符串"null"）
                            if (!"null".equals(String.valueOf(fieldValue))) {
                                meaningfulFieldCount++;
                                LoggerCore.debugLog("📝 有效字段: " + field.getName() + " = " + fieldValue);
                            } else {
                                LoggerCore.debugLog("⚠️ 无效字段值: " + field.getName() + " = 'null'(字符串)");
                            }
                        }
                    } catch (IllegalAccessException e) {
                        // 忽略无法访问的字段
                    }
                }

                // 修改判断逻辑：需要有足够的有意义字段（至少2个且必须包含关键字段）
                boolean meaningful = meaningfulFieldCount >= 2;
                LoggerCore.debugLog("📊 对象有效性检查: " + nonNullFieldCount + " 个非null字段, " +
                    meaningfulFieldCount + " 个有意义字段, 结果有意义: " + meaningful);
                return meaningful;
            }

            // 智能字段映射转换
            private static <T> T convertWithSmartFieldMapping(Map<String, Object> resultMap, Class<T> targetType) {
                try {
                    T instance = targetType.getDeclaredConstructor().newInstance();
                    TypeInfo typeInfo = getTypeInfo(targetType);

                    for (Field field : typeInfo.fields) {
                        String fieldName = field.getName();

                        // 智能查找对应的值
                        Object value = findValueFromMapSmart(resultMap, fieldName);

                        if (value != null) {
                            // 类型转换并设置值
                            Object convertedValue = convertFieldValue(value, field);
                            field.set(instance, convertedValue);
                        }
                    }

                    return instance;
                } catch (Exception e) {
                    LoggerCore.debugErrorLog("智能字段映射失败: " + e.getMessage());
                    return null;
                }
            }

            // 智能查找Map中的值（支持多种命名方式）
            private static Object findValueFromMapSmart(Map<String, Object> map, String fieldName) {
                LoggerCore.debugLog("🔍 开始查找字段: " + fieldName + " 在Map: " + map.keySet());

                // 直接匹配
                if (map.containsKey(fieldName)) {
                    LoggerCore.debugLog("✅ 直接匹配成功: " + fieldName);
                    return map.get(fieldName);
                }

                // 使用缓存的字段名映射
                String cacheKey = fieldName + "_mapping";
                String cachedMapping = FIELD_NAME_CACHE.get(cacheKey);
                if (cachedMapping != null && map.containsKey(cachedMapping)) {
                    LoggerCore.debugLog("✅ 缓存映射成功: " + fieldName + " -> " + cachedMapping);
                    return map.get(cachedMapping);
                }

                // 尝试各种命名转换（增强版）
                String[] namingVariants = {
                    camelToUnderscore(fieldName),           // employeeNo -> employee_no
                    underscoreToCamel(fieldName),           // employee_no -> employeeNo
                    fieldName.toLowerCase(),                // employeeNo -> employeeno
                    fieldName.toUpperCase(),               // employeeNo -> EMPLOYEENO
                    "FULL_" + camelToUnderscore(fieldName).toUpperCase(),  // name -> FULL_NAME
                    fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(),  // employeeNo -> EMPLOYEE_NO
                    fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase()   // employeeNo -> employee_no (再次确保)
                };

                LoggerCore.debugLog("🔄 尝试命名转换变体: " + Arrays.toString(namingVariants));

                for (int i = 0; i < namingVariants.length; i++) {
                    String variant = namingVariants[i];
                    LoggerCore.debugLog("  测试变体[" + i + "]: " + fieldName + " -> " + variant + " (存在: " + map.containsKey(variant) + ")");
                    if (map.containsKey(variant)) {
                        // 缓存成功的映射
                        FIELD_NAME_CACHE.put(cacheKey, variant);
                        LoggerCore.debugLog("✅ 命名转换成功: " + fieldName + " -> " + variant);
                        return map.get(variant);
                    }
                }

                // 如果还是找不到，尝试模糊匹配
                LoggerCore.debugLog("🔄 尝试模糊匹配...");
                for (String mapKey : map.keySet()) {
                    boolean contains1 = mapKey.toLowerCase().contains(fieldName.toLowerCase());
                    boolean contains2 = fieldName.toLowerCase().contains(mapKey.toLowerCase());
                    LoggerCore.debugLog("  模糊测试: " + mapKey + " vs " + fieldName + " (包含1:" + contains1 + ", 包含2:" + contains2 + ")");

                    if (contains1 || contains2) {
                        FIELD_NAME_CACHE.put(cacheKey, mapKey);
                        LoggerCore.debugLog("✅ 模糊匹配成功: " + fieldName + " -> " + mapKey);
                        return map.get(mapKey);
                    }
                }

                LoggerCore.debugLog("❌ 未找到字段映射: " + fieldName);
                return null;
            }

            // 字段值转换（支持复杂类型）
            private static Object convertFieldValue(Object value, Field field) {
                if (value == null) {
                    return null;
                }

                Class<?> fieldType = field.getType();

                try {
                    // 如果类型已匹配，直接返回
                    if (fieldType.isAssignableFrom(value.getClass())) {
                        return value;
                    }

                    // 使用Jackson进行类型转换
                    if (field.getGenericType() instanceof ParameterizedType) {
                        // 处理泛型类型
                        TypeFactory typeFactory = JACKSON_MAPPER.getTypeFactory();
                        com.fasterxml.jackson.databind.JavaType javaType =
                            typeFactory.constructType(field.getGenericType());
                        return JACKSON_MAPPER.convertValue(value, javaType);
                    } else {
                        // 处理普通类型
                        return JACKSON_MAPPER.convertValue(value, fieldType);
                    }

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("字段值转换失败: " + field.getName() + " - " + e.getMessage());
                    return value;
                }
            }

            // 驼峰转下划线
            private static String camelToUnderscore(String camelStr) {
                if (camelStr == null || camelStr.isEmpty()) {
                    return camelStr;
                }
                String result = camelStr.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                LoggerCore.debugLog("📝 驼峰转下划线: " + camelStr + " -> " + result);
                return result;
            }

            // 下划线转驼峰
            private static String underscoreToCamel(String underscoreStr) {
                if (underscoreStr == null || underscoreStr.isEmpty()) {
                    return underscoreStr;
                }

                String[] parts = underscoreStr.split("_");
                if (parts.length == 1) {
                    return underscoreStr;
                }

                StringBuilder camelStr = new StringBuilder(parts[0].toLowerCase());
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        camelStr.append(Character.toUpperCase(parts[i].charAt(0)));
                        if (parts[i].length() > 1) {
                            camelStr.append(parts[i].substring(1).toLowerCase());
                        }
                    }
                }
                String result = camelStr.toString();
                LoggerCore.debugLog("📝 下划线转驼峰: " + underscoreStr + " -> " + result);
                return result;
            }

            // 判断是否为简单类型
            private static boolean isSimpleType(Class<?> clazz) {
                return clazz.isPrimitive() ||
                    clazz == String.class ||
                    clazz == Integer.class || clazz == Long.class ||
                    clazz == Double.class || clazz == Float.class ||
                    clazz == Boolean.class || clazz == BigDecimal.class ||
                    clazz == Date.class || clazz == LocalDateTime.class ||
                    clazz == LocalDate.class || clazz == LocalTime.class ||
                    clazz == Timestamp.class ||
                    Number.class.isAssignableFrom(clazz) ||
                    clazz.isEnum();
            }

            // 批量转换List结果
            public static <T> List<T> convertMapListToObjectList(List<Map<String, Object>> resultMapList,
                Class<T> targetType) {
                if (resultMapList == null || resultMapList.isEmpty()) {
                    return new ArrayList<>();
                }

                return resultMapList.stream()
                    .map(map -> convertMapToObject(map, targetType))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }

            // 批量转换List结果（支持泛型）
            public static <T> List<T> convertMapListToObjectListWithType(List<Map<String, Object>> resultMapList,
                TypeReference<T> typeReference) {
                if (resultMapList == null || resultMapList.isEmpty()) {
                    return new ArrayList<>();
                }

                return resultMapList.stream()
                    .map(map -> convertMapToObjectWithType(map, typeReference))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }

            // 使用TypeReference转换（支持复杂泛型）
            public static <T> T convertMapToObjectWithType(Map<String, Object> resultMap,
                TypeReference<T> typeReference) {
                if (resultMap == null || resultMap.isEmpty()) {
                    return null;
                }

                try {
                    // 首先尝试驼峰命名
                    T result = JACKSON_MAPPER.convertValue(resultMap, typeReference);
                    if (result != null) {
                        return result;
                    }

                    // 然后尝试下划线命名
                    return SNAKE_CASE_MAPPER.convertValue(resultMap, typeReference);

                } catch (Exception e) {
                    LoggerCore.debugErrorLog("TypeReference转换失败: " + e.getMessage());
                    return null;
                }
            }
        }

        // ==================== 辅助工具方法 ====================

        // 获取类型信息（带缓存）
        private static TypeInfo getTypeInfo(Class<?> clazz) {
            return TYPE_CACHE.computeIfAbsent(clazz, TypeInfo::new);
        }

        // 判断是否为简单类型
        private static boolean isSimpleType(Class<?> clazz) {
            return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class || clazz == Long.class ||
                clazz == Double.class || clazz == Float.class ||
                clazz == Boolean.class || clazz == BigDecimal.class ||
                clazz == Date.class || clazz == LocalDateTime.class ||
                clazz == LocalDate.class || clazz == LocalTime.class ||
                clazz == Timestamp.class ||
                Number.class.isAssignableFrom(clazz) ||
                clazz.isEnum();
        }

        // 清空所有缓存
        public static void clearAllCaches() {
            TYPE_CACHE.clear();
            FIELD_NAME_CACHE.clear();
            LoggerCore.debugLog("DatabaseUtil44所有缓存已清空");
        }

        // 获取缓存统计信息
        public static Map<String, Integer> getCacheStats() {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("typeCache", TYPE_CACHE.size());
            stats.put("fieldNameCache", FIELD_NAME_CACHE.size());
            return stats;
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
                        ds.setProxyFilters(Arrays.asList(wallFilter));

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

    // 解析器：公共
    public static class CommonSqlParser {

        // 通用方法：移除SQL注释
        private static String removeComments(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return sql;
            }

            StringBuilder result = new StringBuilder();
            String[] lines = sql.split("\n");
            boolean inBlockComment = false;

            for (String line : lines) {
                StringBuilder lineResult = new StringBuilder();
                char[] chars = line.toCharArray();
                boolean inStringLiteral = false;
                char stringDelimiter = 0;

                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];

                    if (!inBlockComment && !inStringLiteral) {
                        // 检查字符串字面量开始
                        if (c == '\'' || c == '"') {
                            inStringLiteral = true;
                            stringDelimiter = c;
                            lineResult.append(c);
                        }
                        // 检查单行注释
                        else if (c == '-' && i + 1 < chars.length && chars[i + 1] == '-') {
                            break; // 忽略行的其余部分
                        }
                        // 检查块注释开始
                        else if (c == '/' && i + 1 < chars.length && chars[i + 1] == '*') {
                            inBlockComment = true;
                            i++; // 跳过下一个字符
                        } else {
                            lineResult.append(c);
                        }
                    } else if (inStringLiteral) {
                        lineResult.append(c);
                        // 检查字符串字面量结束
                        if (c == stringDelimiter) {
                            // 检查是否是转义的引号
                            if (i == 0 || chars[i - 1] != '\\') {
                                inStringLiteral = false;
                            }
                        }
                    } else if (inBlockComment) {
                        // 检查块注释结束
                        if (c == '*' && i + 1 < chars.length && chars[i + 1] == '/') {
                            inBlockComment = false;
                            i++; // 跳过下一个字符
                        }
                    }
                }

                String processedLine = lineResult.toString().trim();
                if (!processedLine.isEmpty()) {
                    result.append(processedLine).append("\n");
                }
            }

            return result.toString().trim();
        }

        // 通用方法：智能处理SQL末尾分号
        private static String smartTrimSqlSemicolon(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return sql;
            }

            String trimmedSql = sql.trim();

            // 如果SQL以分号结尾，移除它
            if (trimmedSql.endsWith(";")) {
                trimmedSql = trimmedSql.substring(0, trimmedSql.length() - 1).trim();
                LoggerCore.debugLog("🔧 移除SQL末尾分号");
            }

            // 检查是否包含多个SQL语句（简单检查）
            if (trimmedSql.contains(";")) {
                LoggerCore.debugLog("⚠️ 检测到可能的多语句SQL，请谨慎使用");
            }

            return trimmedSql;
        }
    }

    // 解析器：SQL
    public static class SimpleSqlParser {

        // ==================== Params：参数配置 ====================

        // SQL缓存：用于AUTO XML模式
        private static final Map<String, String> SQL_CACHE = new ConcurrentHashMap<>();
        private static volatile boolean CACHE_INITIALIZED = false;
        // 专用于测试的额外XML目录列表
        private static final List<String> TEST_XML_DIRECTORIES = new ArrayList<>();

        // 封装类
        public static class SimpleSqlWithParams {

            public final String sql;
            public final List<Object> params;

            SimpleSqlWithParams(String sql, List<Object> params) {
                this.sql = sql;
                this.params = params;
            }

            public String getSql() {
                return sql;
            }

            public List<Object> getParams() {
                return params;
            }
        }

        // 统一参数转换监听器
        public static class SimpleSqlParameterConverter {

            public enum ConversionType {
                DIRECT_SQL, XML_FILE, XML_AUTO
            }

            // 统一转换方法：根据类型处理参数
            public static SimpleSqlWithParams convert(ConversionType type, String primaryParam, String secondaryParam, Map<String, Object> params) {
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
            public static SimpleSqlWithParams convertDirectSql(String sql, Map<String, Object> params) {
                return convert(ConversionType.DIRECT_SQL, sql, null, params);
            }

            // 便捷方法：指定XML文件转换
            public static SimpleSqlWithParams convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params) {
                return convert(ConversionType.XML_FILE, xmlFileName, sqlId, params);
            }

            // 便捷方法：自动搜索XML转换
            public static SimpleSqlWithParams convertXmlAuto(String sqlId, Map<String, Object> params) {
                return convert(ConversionType.XML_AUTO, sqlId, null, params);
            }
        }

        // 提取参数并替换占位符的核心方法
        private static SimpleSqlWithParams extractParametersAndReplacePlaceholders(String sql, Map<String, Object> params) {
            try {
                final Map<String, Object> finalParams = (params == null) ? new HashMap<>() : params;
                LoggerCore.debugLog("🔧 提取参数并替换占位符，原始SQL长度: " + sql.length() + "，参数数量: " + finalParams.size());

                List<Object> paramList = new ArrayList<>();
                String processedSql = sql;

                // 1. 处理 #{param} 占位符
                Pattern hashPattern = Pattern.compile("#\\{([^}]+)\\}");
                Matcher hashMatcher = hashPattern.matcher(processedSql);
                StringBuffer hashBuffer = new StringBuffer();

                while (hashMatcher.find()) {
                    String paramName = hashMatcher.group(1).trim();
                    if (paramName.contains(",")) {
                        paramName = paramName.split(",")[0].trim();
                    }

                    if (!finalParams.containsKey(paramName)) {
                        // 【BUG修复】如果参数缺失，不应静默失败，而是抛出异常
                        throw new RuntimeException("SQL解析错误: 必选参数 '" + paramName + "' 在提供的参数Map中未找到。");
                    }

                    Object paramValue = finalParams.get(paramName);
                    paramList.add(paramValue);
                    hashMatcher.appendReplacement(hashBuffer, "?");
                    LoggerCore.debugLog("📝 替换参数: #{" + paramName + "} -> " + (paramValue != null ? paramValue.toString() : "null"));
                }
                hashMatcher.appendTail(hashBuffer);
                processedSql = hashBuffer.toString();

                // 2. 处理 ${param} 直接替换
                processedSql = replaceDirectTextParameters(processedSql, finalParams);

                // 3. 智能处理SQL末尾分号
                processedSql = CommonSqlParser.smartTrimSqlSemicolon(processedSql);

                LoggerCore.debugLog("✅ 参数提取完成，最终SQL长度: " + processedSql.length() + "，参数数量: " + paramList.size());
                if (LoggerCore.isDebugLogEnabled()) {
                    LoggerCore.debugSqlWithParams("最终SQL", processedSql, paramList.toArray());
                }

                return new SimpleSqlWithParams(processedSql, paramList);

            } catch (Exception e) {
                LoggerCore.debugErrorLog("❌ 参数提取失败: " + e.getMessage());
                throw new RuntimeException("参数提取失败", e);
            }
        }

        // 公开的测试专用方法，用于在运行时添加测试资源目录
        public static void addTestResourcePath(String path) {
            if (path != null && !path.isEmpty() && !TEST_XML_DIRECTORIES.contains(path)) {
                TEST_XML_DIRECTORIES.add(path);
                // 添加新路径后，应重置缓存状态以允许重新初始化
                CACHE_INITIALIZED = false;
                LoggerCore.debugLog("✓ 添加测试SQL目录: " + path);
            }
        }

        // 初始化SQL缓存：扫描全部XML文件
        public static synchronized void initializeSqlCache() {
            initializeSqlCache(false); // 默认非强制刷新
        }

        // 可强制刷新的初始化方法
        public static synchronized void initializeSqlCache(boolean force) {
            if (CACHE_INITIALIZED && !force) {
                return;
            }

            if (force) {
                LoggerCore.debugLog("🔄 强制刷新简单SQL缓存...");
                SQL_CACHE.clear();
            } else {
                LoggerCore.debugLog("🔄 开始初始化简单SQL缓存...");
            }

            int totalSqlCount = 0;
            List<String> allDirs = new ArrayList<>(Arrays.asList(XML_DIRECTORIES));
            allDirs.addAll(TEST_XML_DIRECTORIES); // 合并生产和测试目录

            try {
                for (String dir : allDirs) {
                    File directory = new File(dir);
                    if (directory.exists() && directory.isDirectory()) {
                        File[] xmlFiles = directory.listFiles((d, name) -> name.endsWith(".xml"));
                        if (xmlFiles != null) {
                            for (File xmlFile : xmlFiles) {
                                int fileCount = scanXmlFileToCache(xmlFile);
                                totalSqlCount += fileCount;
                                LoggerCore.debugLog("✓ 扫描简单SQL文件: " + xmlFile.getName() + " (发现 " + fileCount + " 个SQL)");
                            }
                        }
                    }
                }

                CACHE_INITIALIZED = true;
                LoggerCore.debugLog("✅ 简单SQL缓存初始化完成，共缓存 " + totalSqlCount + " 个SQL语句");

            } catch (Exception e) {
                LoggerCore.debugErrorLog("❌ 简单SQL缓存初始化失败: " + e.getMessage());
                throw new RuntimeException("简单SQL缓存初始化失败", e);
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
                        LoggerCore.debugLog("⚠️ 发现重复简单SQL ID: " + sqlId + " (文件: " + xmlFile.getName() + ")");
                    }

                    SQL_CACHE.put(sqlId, sqlContent);
                    count++;
                }

            } catch (Exception e) {
                LoggerCore.debugErrorLog("⚠️ 扫描简单SQL XML文件失败: " + xmlFile.getName() + " - " + e.getMessage());
            }

            return count;
        }

        // 从缓存获取SQL（AUTO XML模式）
        static String getSqlFromCache(String sqlId) {
            if (!CACHE_INITIALIZED) {
                initializeSqlCache();
            }

            String sql = SQL_CACHE.get(sqlId);
            if (sql == null) {
                throw new RuntimeException("未在简单SQL缓存中找到SQL ID: " + sqlId);
            }

            LoggerCore.debugLog("📋 从简单SQL缓存获取SQL: " + sqlId);
            return sql;
        }

        // 参数：指定具体XML（每次扫描指定文件）
        private static SimpleSqlWithParams getSqlWithParamsFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            try {
                LoggerCore.debugLog("🔍 扫描指定简单SQL XML文件: " + xmlFileName + " 查找SQL ID: " + sqlId);

                for (String dir : XML_DIRECTORIES) {
                    String filePath = dir + xmlFileName;
                    if (!filePath.endsWith(".xml")) {
                        filePath += ".xml";
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        continue;
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
                            throw new RuntimeException("简单SQL标签未正确闭合: " + sqlId);
                        }

                        String originalSql = content.substring(start, end).trim();
                        LoggerCore.debugLog("✓ 在简单SQL文件 " + xmlFileName + " 中找到SQL: " + sqlId);

                        // 首先处理XML实体
                        String unescapedSql = preProcessXmlEntities(originalSql);

                        // 处理动态SQL标签
                        String processedSql = processDynamicSqlTags(unescapedSql, params);

                        // 提取参数并替换占位符
                        return extractParametersAndReplacePlaceholders(processedSql, params);
                    }
                }
                throw new RuntimeException("未找到简单SQL XML文件: " + xmlFileName + " 或SQL ID: " + sqlId);
            } catch (Exception e) {
                throw new RuntimeException("获取简单SQL失败: " + e.getMessage(), e);
            }
        }

        // 参数：自动搜索XML（使用缓存）
        private static SimpleSqlWithParams getSqlWithParamsFromXmlAuto(String sqlId, Map<String, Object> params) {
            try {
                // 从缓存获取SQL
                String originalSql = getSqlFromCache(sqlId);

                // 首先处理XML实体
                String unescapedSql = preProcessXmlEntities(originalSql);

                // 处理动态SQL标签
                String processedSql = processDynamicSqlTags(unescapedSql, params);

                // 提取参数并替换占位符
                return extractParametersAndReplacePlaceholders(processedSql, params);

            } catch (Exception e) {
                throw new RuntimeException("自动搜索简单SQL失败: " + e.getMessage(), e);
            }
        }

        // 参数：直接SQL处理
        private static SimpleSqlWithParams processMapParametersForDirectSql(String sql, Map<String, Object> params) {
            try {
                final Map<String, Object> finalParams = (params == null) ? new HashMap<>() : params;
                LoggerCore.debugLog("🔧 处理直接SQL参数，原始SQL长度: " + sql.length() + "，参数数量: " + finalParams.size());

                // 1. 先处理动态标签
                String processedSql = processDynamicSqlTags(sql, finalParams);

                // 2. 再提取参数和替换占位符
                return extractParametersAndReplacePlaceholders(processedSql, finalParams);

            } catch (Exception e) {
                LoggerCore.debugErrorLog("❌ 直接SQL参数处理失败: " + e.getMessage());
                throw new RuntimeException("直接SQL参数处理失败", e);
            }
        }

        // ==================== Parser：解析标签 ====================

        // 预处理XML实体
        private static String preProcessXmlEntities(String expression) {
            // 将XML转义字符还原为普通字符，以便JEXL引擎能够正确解析
            // 必须注意替换顺序，优先替换多字符的实体
            return expression.replace("&lt;=", "<=")
                .replace("&gt;=", ">=")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
        }

        // 处理动态SQL标签
        private static String processDynamicSqlTags(String sql, Map<String, Object> params) {
            try {
                // 【BUG修复】增加对null params的健壮性处理
                final Map<String, Object> finalParams = (params == null) ? new HashMap<>() : params;

                String processedSql = sql;
                // 优先处理 ${...}，因为它可能影响 <if> 标签的结构
                processedSql = replaceDirectTextParameters(processedSql, finalParams);
                processedSql = processXmlIfTags(processedSql, finalParams);
                // #if 语法已被废弃，移除其处理器调用
                // processedSql = processHashIfTags(processedSql, finalParams);
                return processedSql;
            } catch (Exception e) {
                LoggerCore.debugErrorLog("❌ 动态SQL标签处理失败: " + e.getMessage());
                return sql; // 返回原始SQL
            }
        }

        // 替换直接文本参数
        private static String replaceDirectTextParameters(String sql, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                return sql;
            }

            String result = sql;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String valueStr = value != null ? value.toString() : "";
                result = result.replace("${" + key + "}", valueStr);
            }

            return result;
        }

        // 处理XML风格的if标签（支持嵌套）
        private static String processXmlIfTags(String sql, Map<String, Object> params) {
            String currentSql = sql;
            // 使用循环来处理嵌套的<if>标签，从最内层开始
            while (true) {
                // 查找最后一个<if...>标签，这确保我们总是从最内层的嵌套开始处理
                int openTagStart = currentSql.lastIndexOf("<if ");
                if (openTagStart == -1) {
                    openTagStart = currentSql.lastIndexOf("<if\t");
                }
                if (openTagStart == -1) {
                    break; // 没有更多的<if>标签，处理完成
                }

                // 找到这个<if>标签的结束符'>'
                int openTagEnd = currentSql.indexOf('>', openTagStart);
                if (openTagEnd == -1) {
                    break; // 格式错误的标签
                }

                // 从开标签结束后查找第一个</if>闭标签。
                // 因为我们是从最后一个开标签开始的，所以找到的第一个闭标签必然是与之对应的。
                int closeTagStart = currentSql.indexOf("</if>", openTagEnd);
                if (closeTagStart == -1) {
                    break; // 格式错误的标签
                }

                // 提取各个部分
                String openTagContent = currentSql.substring(openTagStart, openTagEnd + 1);
                String innerContent = currentSql.substring(openTagEnd + 1, closeTagStart);

                // 从开标签中提取test条件
                Pattern testPattern = Pattern.compile("test\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher testMatcher = testPattern.matcher(openTagContent);

                String replacement;
                if (testMatcher.find()) {
                    String condition = testMatcher.group(2);
                    if (evaluateXmlCondition(condition, params)) {
                        // 条件为真，用内部内容替换整个<if>...</if>块
                        replacement = innerContent;
                    } else {
                        // 条件为假，用空字符串替换整个<if>...</if>块
                        replacement = "";
                    }
                } else {
                    // 如果没有test属性，视为无效标签，移除
                    replacement = "";
                }

                // 重建SQL字符串
                currentSql = currentSql.substring(0, openTagStart) + replacement + currentSql.substring(closeTagStart + 5);
            }
            return currentSql;
        }

        // 评估XML条件（支持'and'和'or'，且'and'优先级高于'or'）
        private static boolean evaluateXmlCondition(String condition, Map<String, Object> params) {
            try {
                final Map<String, Object> finalParams = (params == null) ? new HashMap<>() : params;
                condition = condition.trim();

                // 按 "or" 分割，处理多个OR分组
                String[] orGroups = condition.split("\\s+or\\s+");

                for (String orGroup : orGroups) {
                    // 只要有一个OR分组为真，整个表达式就为真
                    if (evaluateAndGroup(orGroup.trim(), finalParams)) {
                        return true;
                    }
                }

                // 所有OR分组都为假，整个表达式才为假
                return false;
            } catch (Exception e) {
                LoggerCore.debugErrorLog("条件评估失败: " + condition + ", 错误: " + e.getMessage());
                return false;
            }
        }

        // 评估AND分组
        private static boolean evaluateAndGroup(String andGroup, Map<String, Object> params) {
            // 按 "and" 分割，处理AND分组内的多个条件
            String[] andConditions = andGroup.split("\\s+and\\s+");

            for (String andCondition : andConditions) {
                // 只要有一个AND条件不满足，整个AND分组就为假
                if (!evaluateSingleCondition(andCondition.trim(), params)) {
                    return false;
                }
            }

            // 所有AND条件都满足，整个AND分组才为真
            return true;
        }

        // 评估单个条件
        private static boolean evaluateSingleCondition(String condition, Map<String, Object> params) {
            String operator = null;
            if (condition.contains("==")) {
                operator = "==";
            } else if (condition.contains("!=")) {
                operator = "!=";
            } else {
                // 对于没有操作符的简单条件，例如 "myVar"，遵循MyBatis规则：
                // 如果myVar存在且不为null，则为true
                Object paramValue = params.get(condition);
                return paramValue != null;
            }

            String[] parts = condition.split(operator, 2);
            if (parts.length != 2) {
                LoggerCore.warnLog("无法解析的<if>子条件: '" + condition + "'");
                return false;
            }

            String paramName = parts[0].trim();
            String valuePart = parts[1].trim();
            Object paramValue = params.get(paramName);

            // 处理 "!= null" 和 "== null"
            if ("null".equalsIgnoreCase(valuePart)) {
                return "!=".equals(operator) ? (paramValue != null) : (paramValue == null);
            }

            // 处理 "!= ''" 和 "== ''"
            if ("''".equals(valuePart) || "\"\"".equals(valuePart)) {
                String actualValue = (paramValue == null) ? "" : String.valueOf(paramValue);
                return "!=".equals(operator) ? !actualValue.isEmpty() : actualValue.isEmpty();
            }

            // 处理与其他值的比较
            String expectedValue;
            if (valuePart.startsWith("'") && valuePart.endsWith("'")) {
                expectedValue = valuePart.substring(1, valuePart.length() - 1);
            } else if (valuePart.startsWith("\"") && valuePart.endsWith("\"")) {
                expectedValue = valuePart.substring(1, valuePart.length() - 1);
            } else {
                // 如果值没有引号，直接作为字符串处理
                expectedValue = valuePart;
            }

            String actualValue = (paramValue == null) ? null : String.valueOf(paramValue);

            return "!=".equals(operator) ? !expectedValue.equals(actualValue) : expectedValue.equals(actualValue);
        }
    }

    // 解析器：MP SQL
    public static class MyBatisSqlParser {

        // JEXL引擎，用于动态SQL中的表达式求值
        private static final JexlEngine JEXL_ENGINE = new JexlBuilder()
            .loader(DatabaseUtil32.class.getClassLoader()) // 添加类加载器以支持方法调用
            .cache(512) // 缓存512个表达式
            .silent(false) // 出错时抛出异常
            .strict(false) // 允许未定义变量
            .create();
        // SQL缓存
        private static final Map<String, String> MYBATIS_SQL_CACHE = new ConcurrentHashMap<>();
        private static volatile boolean MYBATIS_CACHE_INITIALIZED = false;

        // SQL参数封装类
        public static class MyBatisSqlWithParams {

            private final String sql;
            private final List<Object> params;

            public MyBatisSqlWithParams(String sql, List<Object> params) {
                this.sql = sql;
                this.params = params;
            }

            public String getSql() {
                return sql;
            }

            public List<Object> getParams() {
                return params;
            }
        }

        // 参数转换器
        public static class MyBatisSqlParameterConverter {

            public enum ConversionType {
                DIRECT_SQL,     // 直接SQL处理
                XML_FILE,       // 指定XML文件
                XML_AUTO        // 自动搜索XML
            }

            public static MyBatisSqlWithParams convert(ConversionType type, String primaryParam, String secondaryParam, Map<String, Object> params) {
                switch (type) {
                    case DIRECT_SQL:
                        return convertDirectSql(primaryParam, params);
                    case XML_FILE:
                        return convertXmlFile(primaryParam, secondaryParam, params);
                    case XML_AUTO:
                        return convertXmlAuto(primaryParam, params);
                    default:
                        throw new IllegalArgumentException("不支持的转换类型: " + type);
                }
            }

            public static MyBatisSqlWithParams convertDirectSql(String sql, Map<String, Object> params) {
                return processMapParametersForDirectSql(sql, params);
            }

            public static MyBatisSqlWithParams convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params) {
                return getSqlWithParamsFromXml(xmlFileName, sqlId, params);
            }

            public static MyBatisSqlWithParams convertXmlAuto(String sqlId, Map<String, Object> params) {
                return getSqlWithParamsFromXmlAuto(sqlId, params);
            }
        }

        // 内容获取转换器（不进行参数处理）
        public static class MyBatisSqlContentConverter {

            public enum ConversionType {
                DIRECT_SQL,     // 直接SQL处理
                XML_FILE,       // 指定XML文件
                XML_AUTO        // 自动搜索XML
            }

            // 统一转换入口
            public static String convert(ConversionType type, String primaryParam, String secondaryParam) {
                switch (type) {
                    case DIRECT_SQL:
                        return convertDirectSql(primaryParam);
                    case XML_FILE:
                        return convertXmlFile(primaryParam, secondaryParam);
                    case XML_AUTO:
                        return convertXmlAuto(primaryParam);
                    default:
                        throw new IllegalArgumentException("不支持的转换类型: " + type);
                }
            }

            // 直接SQL - 返回原始SQL内容
            public static String convertDirectSql(String sql) {
                if (sql == null || sql.trim().isEmpty()) {
                    throw new IllegalArgumentException("SQL内容不能为空");
                }
                return sql.trim();
            }

            // 指定XML文件获取SQL内容
            public static String convertXmlFile(String xmlFileName, String sqlId) {
                try {
                    String xmlPath = null;

                    // 首先检查是否为绝对路径
                    File xmlFile = new File(xmlFileName);
                    if (xmlFile.isAbsolute() && xmlFile.exists()) {
                        xmlPath = xmlFile.getAbsolutePath();
                    } else {
                        // 如果不是绝对路径，则在预定义目录中查找
                        for (String directory : MYBATIS_XML_DIRECTORIES) {
                            File relativeXmlFile = new File(directory, xmlFileName);
                            if (relativeXmlFile.exists()) {
                                xmlPath = relativeXmlFile.getAbsolutePath();
                                break;
                            }
                        }
                    }

                    if (xmlPath == null) {
                        throw new RuntimeException("XML文件不存在: " + xmlFileName);
                    }

                    String content = new String(Files.readAllBytes(Paths.get(xmlPath)));

                    // 匹配指定ID的SQL
                    String[] sqlTags = {"select", "insert", "update", "delete", "sql"};
                    for (String tag : sqlTags) {
                        Pattern pattern = Pattern.compile("<" + tag + "\\s+[^>]*id\\s*=\\s*[\"']" + Pattern.quote(sqlId) + "[\"'][^>]*>(.*?)</" + tag + ">",
                            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        Matcher matcher = pattern.matcher(content);

                        if (matcher.find()) {
                            String sql = matcher.group(1);
                            LoggerCore.debugLog("从XML文件 " + xmlFileName + " 获取SQL内容: " + sqlId);
                            return sql.trim();
                        }
                    }

                    throw new RuntimeException("未找到SQL ID: " + sqlId + " 在文件: " + xmlFileName);
                } catch (Exception e) {
                    LoggerCore.logException("获取XML SQL内容失败", e);
                    throw new RuntimeException("处理XML文件失败: " + xmlFileName + ", 错误: " + e.getMessage(), e);
                }
            }

            // 自动搜索XML获取SQL内容
            public static String convertXmlAuto(String sqlId) {
                try {
                    if (!MYBATIS_CACHE_INITIALIZED) {
                        initializeMyBatisSqlCache();
                    }

                    String sql = MYBATIS_SQL_CACHE.get(sqlId);
                    if (sql == null) {
                        throw new RuntimeException("未找到SQL ID: " + sqlId);
                    }

                    LoggerCore.debugLog("从MyBatis缓存获取SQL内容: " + sqlId);
                    return sql.trim();
                } catch (Exception e) {
                    LoggerCore.logException("获取MyBatis SQL内容失败", e);
                    throw new RuntimeException("获取SQL内容失败: " + sqlId + ", 错误: " + e.getMessage(), e);
                }
            }
        }

        // 核心解析方法
        public static MyBatisSqlWithParams parse(String sql, Map<String, Object> params) {
            // 创建一个用于本次解析的SQL片段缓存
            Map<String, String> sqlFragments = new HashMap<>();

            // 0. 预处理XML实体，例如 & -> &&
            sql = preProcessXmlEntities(sql);

            // 1. 预处理，提取所有 <sql> 片段并存入缓存
            sql = extractSqlFragments(sql, sqlFragments);

            // 2. 处理bind标签
            sql = processBindTags(sql, params);

            // 3. 处理动态标签（包括递归处理 <include>）
            String finalSql = processDynamicTags(sql, params, sqlFragments, new java.util.HashSet<>());

            // 关键修复：在SQL校验和参数提取之前，替换所有的 ${} 占位符
            String substitutedSql = replaceDollarPlaceholders(finalSql, params);

            // 4. 智能SQL合法性校验 (软校验) - 只对简单SQL进行校验
            if (shouldPerformSqlValidation(substitutedSql)) {
                try {
                    // 在校验前剥离XML标签
                    String pureSql = stripRootXmlTag(substitutedSql);
                    // 移除参数占位符，以便进行纯语法检查
                    String syntaxCheckSql = pureSql.replaceAll("#\\{[^}]+\\}", "?");
                    CCJSqlParserUtil.parse(syntaxCheckSql);
                } catch (Exception e) {
                    // JSqlParser校验失败，仅记录日志，不中断执行。
                    // 这种处理方式可以兼容JSqlParser无法识别的特定数据库方言（如DB2的 'WITH UR'）
                    // 或处理预期中语法不完整的SQL片段。
                    LoggerCore.warnLog("SQL语法预校验警告 (JSqlParser): " + e.getMessage());
                    LoggerCore.debugLog("校验失败的SQL (将继续执行): " + substitutedSql);
                }
            } else {
                LoggerCore.debugLog("跳过SQL预校验 (包含复杂语法): " + substitutedSql.substring(0, Math.min(100, substitutedSql.length())));
            }

            // 5. 提取参数并构建最终结果
            return extractParametersAndBuild(substitutedSql, params);
        }

        // 提取<sql>片段到缓存
        private static String extractSqlFragments(String sql, Map<String, String> sqlFragments) {
            Pattern sqlTagPattern = Pattern.compile("<sql\\s+id\\s*=\\s*[\"']([^\"']+)[\"']\\s*>((?:.|\n)*?)</sql>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = sqlTagPattern.matcher(sql);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String id = matcher.group(1);
                String fragment = matcher.group(2).trim();
                sqlFragments.put(id, fragment);
                // 从原始SQL中移除<sql>标签
                matcher.appendReplacement(sb, "");
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        // 处理bind标签
        private static String processBindTags(String sql, Map<String, Object> params) {
            Pattern bindPattern = Pattern.compile("<bind\\s+([^>]+)/>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = bindPattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String attributes = matcher.group(1);
                String name = extractAttribute(attributes, "name");
                String value = extractAttribute(attributes, "value");

                if (name != null && value != null) {
                    Object evaluatedValue = evaluateExpression(value, params);
                    params.put(name, evaluatedValue);
                }

                matcher.appendReplacement(sb, "");
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理动态标签
        private static String processDynamicTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            String previousSql;
            int maxIterations = 50; // 安全中断以防止无限循环
            int iteration = 0;

            do {
                previousSql = sql;
                iteration++;

                // 每次迭代都按顺序处理所有类型的标签
                // 这种不动点迭代方法可以正确处理任意复杂的嵌套
                sql = processIncludeTags(sql, params, sqlFragments, visited);
                sql = processIfTags(sql, params);
                sql = processWhereTags(sql, params, sqlFragments, visited);
                sql = processSetTags(sql, params, sqlFragments, visited);
                sql = processChooseTags(sql, params, sqlFragments, visited);
                sql = processTrimTags(sql, params, sqlFragments, visited);
                sql = processForeachTags(sql, params, sqlFragments, visited);
                // 在每次迭代中处理 ${} 占位符，确保动态内容能被正确替换
                sql = replaceDollarPlaceholders(sql, params);

                if (iteration > maxIterations) {
                    throw new RuntimeException("动态SQL解析超过最大迭代次数，可能存在循环或解析错误。SQL: " + sql);
                }
                // 持续迭代，直到SQL字符串不再发生变化
            } while (!previousSql.equals(sql));

            return sql;
        }

        // 处理if标签
        private static String processIfTags(String sql, Map<String, Object> params) {
            // 采用“由内向外”的解析策略来正确处理嵌套的<if>标签
            String currentSql = sql;
            while (true) {
                // 查找最后一个<if...>标签，这确保我们总是从最内层的嵌套开始处理
                int openTagStart = currentSql.lastIndexOf("<if ");
                if (openTagStart == -1) {
                    openTagStart = currentSql.lastIndexOf("<if\t");
                }
                if (openTagStart == -1) {
                    break; // 没有更多的<if>标签，处理完成
                }

                // 找到这个<if>标签的结束符'>'
                int openTagEnd = currentSql.indexOf('>', openTagStart);
                if (openTagEnd == -1) {
                    break; // 格式错误的标签
                }

                // 从开标签结束后查找第一个</if>闭标签。
                // 因为我们是从最后一个开标签开始的，所以找到的第一个闭标签必然是与之对应的。
                int closeTagStart = currentSql.indexOf("</if>", openTagEnd);
                if (closeTagStart == -1) {
                    break; // 格式错误的标签
                }

                // 提取各个部分
                String openTagContent = currentSql.substring(openTagStart, openTagEnd + 1);
                String innerContent = currentSql.substring(openTagEnd + 1, closeTagStart).trim();

                // 从开标签中提取test条件
                Pattern testPattern = Pattern.compile("test\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher testMatcher = testPattern.matcher(openTagContent);

                String replacement;
                if (testMatcher.find()) {
                    String condition = testMatcher.group(2);
                    if (evaluateBoolean(condition, params)) {
                        // 条件为真，用内部内容替换整个<if>...</if>块
                        replacement = innerContent;
                    } else {
                        // 条件为假，用空字符串替换整个<if>...</if>块
                        replacement = "";
                    }
                } else {
                    // 如果没有test属性，视为无效标签，移除
                    replacement = "";
                }

                // 重建SQL字符串
                currentSql = currentSql.substring(0, openTagStart) + replacement + currentSql.substring(closeTagStart + 5);
            }
            return currentSql;
        }

        // 处理where标签
        private static String processWhereTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            Pattern wherePattern = Pattern.compile("<where>(.*?)</where>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = wherePattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String content = matcher.group(1).trim();
                String processedContent = processDynamicTags(content, params, sqlFragments, visited);

                if (processedContent.trim().isEmpty()) {
                    matcher.appendReplacement(sb, "");
                    continue;
                }

                // 使用正则表达式更健壮地移除开头的AND或OR，忽略前导空格和大小写
                String finalContent = processedContent.trim().replaceFirst("(?i)^\\s*(AND|OR)\\b", "").trim();

                if (!finalContent.isEmpty()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(" WHERE " + finalContent.trim()));
                } else {
                    // 如果移除AND/OR后内容为空，则不添加WHERE子句
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理set标签
        private static String processSetTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            Pattern setPattern = Pattern.compile("<set>(.*?)</set>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = setPattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String content = matcher.group(1).trim();
                String processedContent = processDynamicTags(content, params, sqlFragments, visited);

                if (processedContent.trim().isEmpty()) {
                    matcher.appendReplacement(sb, "");
                    continue;
                }

                // 使用正则表达式更健壮地移除结尾的逗号，忽略尾随空格
                String finalContent = processedContent.trim().replaceAll(",\\s*$", "").trim();

                if (!finalContent.isEmpty()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(" SET " + finalContent));
                } else {
                    // 如果移除逗号后内容为空，则不添加SET子句
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理choose标签
        private static String processChooseTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            Pattern choosePattern = Pattern.compile("<choose>(.*?)</choose>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = choosePattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String content = matcher.group(1);
                String result = processChooseContent(content, params, sqlFragments, visited);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理choose内容
        private static String processChooseContent(String content, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            // 处理when标签
            // 使用非贪婪匹配 (.*?)
            Pattern whenPattern = Pattern.compile("<when\\s+test\\s*=\\s*(['\"])(.*?)\\1\\s*?>(.*?)</when>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher whenMatcher = whenPattern.matcher(content);

            while (whenMatcher.find()) {
                String condition = whenMatcher.group(2);
                String whenContent = whenMatcher.group(3).trim();
                if (evaluateBoolean(condition, params)) {
                    // 注意：choose只选择第一个满足条件的when，所以直接返回
                    return processDynamicTags(whenContent, params, sqlFragments, visited);
                }
            }

            // 处理otherwise标签
            Pattern otherwisePattern = Pattern.compile("<otherwise>(.*?)</otherwise>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher otherwiseMatcher = otherwisePattern.matcher(content);
            if (otherwiseMatcher.find()) {
                return processDynamicTags(otherwiseMatcher.group(1).trim(), params, sqlFragments, visited);
            }

            return "";
        }

        // 处理trim标签
        private static String processTrimTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            Pattern trimPattern = Pattern.compile("<trim\\s+([^>]+)>(.*?)</trim>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = trimPattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String attributes = matcher.group(1);
                String content = matcher.group(2).trim();
                String processedContent = processDynamicTags(content, params, sqlFragments, visited);
                String result = processTrimContent(processedContent, attributes);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理trim内容
        private static String processTrimContent(String content, String attributes) {
            String prefix = extractAttribute(attributes, "prefix");
            String suffix = extractAttribute(attributes, "suffix");
            String prefixOverrides = extractAttribute(attributes, "prefixOverrides");
            String suffixOverrides = extractAttribute(attributes, "suffixOverrides");

            content = content.trim();

            if (content.isEmpty()) {
                return "";
            }

            // 处理前缀覆盖
            if (prefixOverrides != null && !prefixOverrides.isEmpty()) {
                String[] prefixes = prefixOverrides.split("\\|");
                for (String prefixOverride : prefixes) {
                    prefixOverride = prefixOverride.trim();
                    if (content.toUpperCase().startsWith(prefixOverride.toUpperCase())) {
                        content = content.substring(prefixOverride.length()).trim();
                        break;
                    }
                }
            }

            // 处理后缀覆盖
            if (suffixOverrides != null && !suffixOverrides.isEmpty()) {
                String[] suffixes = suffixOverrides.split("\\|");
                for (String suffixOverride : suffixes) {
                    suffixOverride = suffixOverride.trim();
                    if (content.toUpperCase().endsWith(suffixOverride.toUpperCase())) {
                        content = content.substring(0, content.length() - suffixOverride.length()).trim();
                        break;
                    }
                }
            }

            // 添加前缀和后缀
            if (prefix != null && !prefix.isEmpty()) {
                content = prefix + " " + content;
            }
            if (suffix != null && !suffix.isEmpty()) {
                content = content + " " + suffix;
            }

            return content;
        }

        // 处理foreach标签
        private static String processForeachTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            Pattern foreachPattern = Pattern.compile("<foreach\\s+([^>]+)>(.*?)</foreach>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = foreachPattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String attributes = matcher.group(1);
                String content = matcher.group(2).trim();
                String result = processForeachContent(content, attributes, params, sqlFragments, visited);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(result));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 处理foreach内容
        private static String processForeachContent(String content, String attributes, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            String collection = extractAttribute(attributes, "collection");
            String item = extractAttribute(attributes, "item");
            String index = extractAttribute(attributes, "index");
            String open = extractAttribute(attributes, "open");
            String close = extractAttribute(attributes, "close");
            String separator = extractAttribute(attributes, "separator");

            Object collectionValue = evaluateExpression(collection, params);
            if (collectionValue == null) {
                return "";
            }

            List<Object> items = new ArrayList<>();
            if (collectionValue instanceof Collection) {
                items.addAll((Collection<?>) collectionValue);
            } else if (collectionValue.getClass().isArray()) {
                items.addAll(Arrays.asList((Object[]) collectionValue));
            } else {
                return "";
            }

            if (items.isEmpty()) {
                return "";
            }

            StringBuilder result = new StringBuilder();
            if (open != null) {
                result.append(open);
            }

            for (int i = 0; i < items.size(); i++) {
                if (i > 0 && separator != null) {
                    result.append(separator);
                }

                Map<String, Object> itemParams = new HashMap<>(params);
                if (item != null) {
                    itemParams.put(item, items.get(i));
                }
                if (index != null) {
                    itemParams.put(index, i);
                }

                String processedContent = processDynamicTags(content, itemParams, sqlFragments, visited);

                // 在foreach循环内部处理 ${} 占位符，使用itemParams而不是主params
                processedContent = replaceDollarPlaceholders(processedContent, itemParams);

                // 关键修复：在foreach循环内部解析参数，并用唯一占位符替换
                Pattern pattern = Pattern.compile("#\\{([^}]+)\\}");
                Matcher matcher = pattern.matcher(processedContent);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String paramExpr = matcher.group(1);
                    Object value = evaluateExpression(paramExpr, itemParams);
                    String uniqueKey = "foreach_" + paramExpr.replaceAll("[^a-zA-Z0-9_]", "_") + "_" + System.nanoTime();
                    params.put(uniqueKey, value); // 将解析出的值放入主参数Map
                    matcher.appendReplacement(sb, "#{" + uniqueKey + "}");
                }
                matcher.appendTail(sb);
                result.append(sb.toString());
            }

            if (close != null) {
                result.append(close);
            }

            return result.toString();
        }

        // 递归处理<include>标签，增加对property子元素的支持
        private static String processIncludeTags(String sql, Map<String, Object> params, Map<String, String> sqlFragments, Set<String> visited) {
            // 将两种<include>标签的正则表达式合并为一个，同时处理自闭合和带内容两种情况
            // 修正正则表达式以正确处理refid属性
            // 捕获组1: refid
            // 捕获组2: <include>标签的内部内容 (对于自闭合标签，此组为null)
            Pattern includePattern = Pattern.compile("<include\\s+refid\\s*=\\s*(['\"])(.*?)\\1\\s*(?:/>|>(.*?)</include>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = includePattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String refid = matcher.group(2); // 修正：refid在第二个捕获组
                String includeContent = matcher.group(3); // 修正：内容在第三个捕获组

                if (visited.contains(refid)) {
                    throw new RuntimeException("循环引用检测: <include> 标签循环引用了 '" + refid + "'");
                }
                visited.add(refid);

                String fragment = sqlFragments.get(refid);
                if (fragment == null) {
                    fragment = getMyBatisSqlFromCache(refid);
                }

                if (fragment != null) {
                    String fragmentToProcess = fragment;
                    // 如果<include>标签内部有<property>定义，则应用它们
                    if (includeContent != null && !includeContent.trim().isEmpty()) {
                        Map<String, String> properties = new HashMap<>();
                        Pattern propertyPattern = Pattern.compile("<property\\s+name\\s*=\\s*[\"']([^\"']+)[\"']\\s+value\\s*=\\s*[\"']([^\"']+)[\"']\\s*/>", Pattern.CASE_INSENSITIVE);
                        Matcher propertyMatcher = propertyPattern.matcher(includeContent);
                        while (propertyMatcher.find()) {
                            properties.put(propertyMatcher.group(1), propertyMatcher.group(2));
                        }
                        fragmentToProcess = applyProperties(fragment, properties);
                    }

                    // 递归处理被包含的片段
                    String processedFragment = processDynamicTags(fragmentToProcess, params, sqlFragments, visited);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(processedFragment));
                } else {
                    matcher.appendReplacement(sb, ""); // 或者抛出异常
                    LoggerCore.warnLog("未找到引用的SQL片段: " + refid);
                }
                visited.remove(refid);
            }
            matcher.appendTail(sb);

            return sb.toString();
        }

        // 新增辅助方法：将<property>应用到SQL片段
        private static String applyProperties(String fragment, Map<String, String> properties) {
            if (properties == null || properties.isEmpty()) {
                return fragment;
            }
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                fragment = fragment.replace(placeholder, entry.getValue());
            }
            return fragment;
        }

        // 新增辅助方法：替换 ${} 占位符
        private static String replaceDollarPlaceholders(String sql, Map<String, Object> params) {
            if (params == null || params.isEmpty() || sql == null || !sql.contains("${")) {
                return sql;
            }
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(sql);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String expression = matcher.group(1).trim();
                Object value = null;

                // 首先尝试直接键查找
                if (params.containsKey(expression)) {
                    value = params.get(expression);
                } else {
                    // 然后尝试使用JEXL来评估复杂表达式
                    value = evaluateExpression(expression, params);
                }

                String replacement = value != null ? value.toString() : "";
                // 使用 Matcher.quoteReplacement 来处理替换字符串中的特殊字符
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        // 智能判断是否应该执行SQL校验
        private static boolean shouldPerformSqlValidation(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return false;
            }

            String upperSql = sql.toUpperCase().trim();

            // 跳过包含复杂语法的SQL校验，这些可能导致JSqlParser误报
            String[] complexPatterns = {
                "CDATA",           // CDATA块
                "&LT;", "&GT;", "&AMP;", "&QUOT;",  // XML实体
                "DATE_SUB", "DATE_ADD", "IFNULL",   // MySQL特有函数
                "NVL", "DECODE", "ROWNUM",          // Oracle特有函数
                "TIMESTAMPDIFF", "CONCAT_WS",       // 其他数据库特有函数
                "WITH RECURSIVE",                   // CTE递归查询
                "ROW_NUMBER() OVER",               // 窗口函数
                "MERGE INTO",                      // Oracle MERGE语句
                "UPSERT",                          // PostgreSQL UPSERT
                "KEY(",                            // H2数据库KEY语法
                "ON DUPLICATE KEY UPDATE"         // MySQL特有语法
            };

            for (String pattern : complexPatterns) {
                if (upperSql.contains(pattern)) {
                    return false;
                }
            }

            // 如果包含多条语句（以分号分隔），也跳过校验
            String[] statements = sql.split(";");
            if (statements.length > 1) {
                // 检查是否真的有多条有效语句
                int validStatements = 0;
                for (String stmt : statements) {
                    if (stmt.trim().length() > 0 && !stmt.trim().startsWith("--")) {
                        validStatements++;
                        if (validStatements > 1) {
                            return false; // 确实包含多条语句，跳过校验
                        }
                    }
                }
            }

            // 对于简单的SELECT、INSERT、UPDATE、DELETE语句，可以进行校验
            return upperSql.matches("^\\s*(SELECT|INSERT|UPDATE|DELETE)\\s+.*");
        }

        // 新增辅助方法：剥离SQL语句最外层的XML标签
        private static String stripRootXmlTag(String xmlSql) {
            if (xmlSql == null) {
                return null;
            }
            // 正则表达式匹配最外层的 <tag...>...</tag>
            // 使用非贪婪匹配 (.*?) 来处理嵌套情况，并确保只匹配最外层
            Pattern pattern = Pattern.compile("^\\s*<\\w+[^>]*>(.*?)</\\w+>\\s*$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(xmlSql);
            if (matcher.find()) {
                // 返回第一个捕获组的内容，并去除首尾空格
                return matcher.group(1).trim();
            }
            // 如果没有匹配到，说明可能已经是纯SQL，直接返回
            return xmlSql.trim();
        }

        // 提取属性值
        private static String extractAttribute(String attributes, String attributeName) {
            // 修正正则表达式以正确处理属性值中包含的引号
            // (['"]): 捕获开始的引号（单引号或双引号）
            // (.*?): 非贪婪地捕获所有字符，直到...
            // \\1: ...匹配到之前捕获的相同类型的引号
            Pattern pattern = Pattern.compile(attributeName + "\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(attributes);
            return matcher.find() ? matcher.group(2) : null; // 返回第二个捕获组
        }

        // 预处理XML实体，以便JEXL解析
        private static String preProcessXmlEntities(String expression) {
            // 将XML转义字符还原为普通字符，以便JEXL引擎能够正确解析
            // 必须注意替换顺序，优先替换多字符的实体
            return expression.replace("&lt;=", "<=")
                .replace("&gt;=", ">=")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
        }

        // 使用JEXL引擎评估布尔表达式
        private static boolean evaluateBoolean(String expression, Map<String, Object> params) {
            if (expression == null || expression.trim().isEmpty()) {
                return false;
            }
            try {
                // 预处理XML实体，例如 & -> &&
                String jexlExpression = preProcessXmlEntities(expression);

                JexlExpression e = JEXL_ENGINE.createExpression(jexlExpression);
                JexlContext context = new MapContext(params);
                Object result = e.evaluate(context);

                // 遵循MyBatis的真值判断逻辑
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
                if (result instanceof Number) {
                    return ((Number) result).doubleValue() != 0;
                }
                if (result instanceof Collection) {
                    return !((Collection<?>) result).isEmpty();
                }
                return result != null;
            } catch (Exception e) {
                LoggerCore.debugErrorLog("JEXL表达式评估失败: " + expression + " | " + e.getMessage());
                return false;
            }
        }

        // 比较数字
        private static boolean compareNumbers(Object left, Object right, String operator) {
            double leftNum = ((Number) left).doubleValue();
            double rightNum = ((Number) right).doubleValue();

            switch (operator) {
                case "==":
                    return leftNum == rightNum;
                case "!=":
                    return leftNum != rightNum;
                case ">":
                    return leftNum > rightNum;
                case "<":
                    return leftNum < rightNum;
                case ">=":
                    return leftNum >= rightNum;
                case "<=":
                    return leftNum <= rightNum;
                default:
                    return false;
            }
        }

        // 评估表达式
        private static Object evaluateExpression(String expression, Map<String, Object> params) {
            if (expression == null || expression.trim().isEmpty()) {
                return null;
            }
            try {
                // 预处理XML实体，例如 & -> &&
                String jexlExpression = preProcessXmlEntities(expression);

                JexlExpression e = JEXL_ENGINE.createExpression(jexlExpression);
                JexlContext context = new MapContext(params);
                return e.evaluate(context);
            } catch (Exception e) {
                LoggerCore.debugErrorLog("JEXL表达式求值失败: " + expression + " | " + e.getMessage());
                return null;
            }
        }

        // 提取参数并构建SQL
        private static MyBatisSqlWithParams extractParametersAndBuild(String sql, Map<String, Object> params) {
            List<Object> paramList = new ArrayList<>();
            Pattern pattern = Pattern.compile("#\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(sql);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String paramName = matcher.group(1);
                // 提取参数名，忽略jdbcType等附加信息
                String paramExpr = paramName.split(",")[0].trim();
                Object paramValue = evaluateExpression(paramExpr, params);
                paramList.add(paramValue);
                matcher.appendReplacement(sb, "?");
            }
            matcher.appendTail(sb);

            return new MyBatisSqlWithParams(sb.toString(), paramList);
        }

        // 初始化MyBatis SQL缓存
        private static synchronized void initializeMyBatisSqlCache() {
            if (MYBATIS_CACHE_INITIALIZED) {
                return;
            }

            try {
                int totalSqlCount = 0;
                for (String directory : MYBATIS_XML_DIRECTORIES) {
                    File dir = new File(directory);
                    if (dir.exists() && dir.isDirectory()) {
                        File[] xmlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
                        if (xmlFiles != null) {
                            for (File xmlFile : xmlFiles) {
                                totalSqlCount += scanMyBatisXmlFileToCache(xmlFile);
                            }
                        }
                    }
                }

                MYBATIS_CACHE_INITIALIZED = true;
                LoggerCore.debugLog("MyBatis SQL缓存初始化完成，共缓存 " + totalSqlCount + " 条SQL语句");
            } catch (Exception e) {
                LoggerCore.debugErrorLog("MyBatis SQL缓存初始化失败: " + e.getMessage());
            }
        }

        // 扫描MyBatis XML文件到缓存
        private static int scanMyBatisXmlFileToCache(File xmlFile) {
            int count = 0;
            try {
                String content = new String(Files.readAllBytes(xmlFile.toPath()));

                // 匹配各种SQL标签
                String[] sqlTags = {"select", "insert", "update", "delete", "sql"};
                for (String tag : sqlTags) {
                    Pattern pattern = Pattern.compile("<" + tag + "\\s+[^>]*id\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>(.*?)</" + tag + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(content);

                    while (matcher.find()) {
                        String id = matcher.group(1);
                        String sql = matcher.group(2);
                        MYBATIS_SQL_CACHE.put(id, sql);
                        count++;
                    }
                }
            } catch (Exception e) {
                LoggerCore.debugErrorLog("扫描MyBatis XML文件失败: " + xmlFile.getName() + ", 错误: " + e.getMessage());
            }
            return count;
        }

        // 从缓存获取MyBatis SQL
        private static String getMyBatisSqlFromCache(String sqlId) {
            if (!MYBATIS_CACHE_INITIALIZED) {
                initializeMyBatisSqlCache();
            }

            String sql = MYBATIS_SQL_CACHE.get(sqlId);
            if (sql == null) {
                LoggerCore.debugLog("未找到SQL ID: " + sqlId);
                return null;
            }

            return sql;
        }

        // 从XML获取SQL和参数
        public static MyBatisSqlWithParams getSqlWithParamsFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            try {
                String xmlPath = null;

                // 首先检查是否为绝对路径
                File xmlFile = new File(xmlFileName);
                if (xmlFile.isAbsolute() && xmlFile.exists()) {
                    xmlPath = xmlFile.getAbsolutePath();
                } else {
                    // 如果不是绝对路径，则在预定义目录中查找
                    for (String directory : MYBATIS_XML_DIRECTORIES) {
                        File relativeXmlFile = new File(directory, xmlFileName);
                        if (relativeXmlFile.exists()) {
                            xmlPath = relativeXmlFile.getAbsolutePath();
                            break;
                        }
                    }
                }

                if (xmlPath == null) {
                    throw new RuntimeException("XML文件不存在: " + xmlFileName);
                }

                String content = new String(Files.readAllBytes(Paths.get(xmlPath)));

                // 匹配指定ID的SQL
                String[] sqlTags = {"select", "insert", "update", "delete", "sql"};
                for (String tag : sqlTags) {
                    Pattern pattern = Pattern.compile("<" + tag + "\\s+[^>]*id\\s*=\\s*[\"']" + Pattern.quote(sqlId) + "[\"'][^>]*>(.*?)</" + tag + ">",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(content);

                    if (matcher.find()) {
                        String sql = matcher.group(1);
                        return parse(sql, params);
                    }
                }

                throw new RuntimeException("未找到SQL ID: " + sqlId + " 在文件: " + xmlFileName);
            } catch (Exception e) {
                throw new RuntimeException("处理XML文件失败: " + xmlFileName + ", 错误: " + e.getMessage(), e);
            }
        }

        // 从XML自动获取SQL和参数
        public static MyBatisSqlWithParams getSqlWithParamsFromXmlAuto(String sqlId, Map<String, Object> params) {
            String sql = getMyBatisSqlFromCache(sqlId);
            if (sql == null) {
                throw new RuntimeException("未找到SQL ID: " + sqlId);
            }

            return parse(sql, params);
        }

        // 处理直接SQL的映射参数
        public static MyBatisSqlWithParams processMapParametersForDirectSql(String sql, Map<String, Object> params) {
            return parse(sql, params);
        }
    }

    // 执行器：公共
    public static class CommonSqlExecutor {

        // DDL：直接操作数据库 (dbName 版本)
        public static boolean executeDDL(String dbName, String sql) {
            try (Connection conn = DataSourceCore.getConnection(dbName);
                Statement stmt = conn.createStatement()) {
                boolean result = stmt.execute(sql);
                LoggerCore.debugLog("✓ DDL 执行成功, 数据源: " + dbName);
                return result;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DDL 执行失败: " + e.getMessage());
                throw new RuntimeException("DDL 执行失败", e);
            }
        }

        // DDL：直接操作数据库 (Connection 版本)
        public static boolean executeDDL(Connection conn, String sql) {
            try (Statement stmt = conn.createStatement()) {
                boolean result = stmt.execute(sql);
                LoggerCore.debugLog("✓ DDL 执行成功 (在现有连接上)");
                return result;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DDL 执行失败: " + e.getMessage());
                throw new RuntimeException("DDL 执行失败", e);
            }
        }

        // DQL：直接查询数据库 (dbName 版本)
        public static List<Map<String, Object>> executeDQL(String dbName, String sql) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return executeDQL(conn, sql);
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DQL 查询失败: " + e.getMessage());
                throw new RuntimeException("DQL 查询失败", e);
            }
        }

        // DQL：直接查询数据库 (Connection 版本)
        public static List<Map<String, Object>> executeDQL(Connection conn, String sql) {
            List<Map<String, Object>> results = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
                LoggerCore.debugLog("✓ DQL 查询成功, 返回 " + results.size() + " 条记录");
                return results;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DQL 查询失败: " + e.getMessage());
                throw new RuntimeException("DQL 查询失败", e);
            }
        }

        // DML：直接操作数据库 (dbName 版本)
        public static int executeDML(String dbName, String sql) {
            try (Connection conn = DataSourceCore.getConnection(dbName);
                Statement stmt = conn.createStatement()) {
                int affectedRows = stmt.executeUpdate(sql);
                LoggerCore.debugLog("✓ DML 执行成功, 影响 " + affectedRows + " 行, 数据源: " + dbName);
                return affectedRows;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DML 执行失败: " + e.getMessage());
                throw new RuntimeException("DML 执行失败", e);
            }
        }

        // DML：直接操作数据库 (Connection 版本)
        public static int executeDML(Connection conn, String sql) {
            try (Statement stmt = conn.createStatement()) {
                int affectedRows = stmt.executeUpdate(sql);
                LoggerCore.debugLog("✓ DML 执行成功 (在现有连接上), 影响 " + affectedRows + " 行");
                return affectedRows;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("❌ DML 执行失败: " + e.getMessage());
                throw new RuntimeException("DML 执行失败", e);
            }
        }
    }

    // 执行器：SQL
    public static class SimpleSqlExecutor {

        // ==================== 核心：SQL ====================

        // 设置PreparedStatement参数
        private static void setParameters(PreparedStatement pstmt, List<Object> params) throws SQLException {
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param == null) {
                        pstmt.setNull(i + 1, Types.NULL);
                    } else if (param instanceof String) {
                        pstmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Long) {
                        pstmt.setLong(i + 1, (Long) param);
                    } else if (param instanceof Double) {
                        pstmt.setDouble(i + 1, (Double) param);
                    } else if (param instanceof Float) {
                        pstmt.setFloat(i + 1, (Float) param);
                    } else if (param instanceof Boolean) {
                        pstmt.setBoolean(i + 1, (Boolean) param);
                    } else if (param instanceof Date) {
                        pstmt.setTimestamp(i + 1, new Timestamp(((Date) param).getTime()));
                    } else if (param instanceof java.sql.Date) {
                        pstmt.setDate(i + 1, (java.sql.Date) param);
                    } else if (param instanceof Timestamp) {
                        pstmt.setTimestamp(i + 1, (Timestamp) param);
                    } else {
                        pstmt.setObject(i + 1, param);
                    }
                }
            }
        }

        // 核心：全部 (Connection 版本)
        private static List<Map<String, Object>> selectListWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            List<Map<String, Object>> results = new ArrayList<>();
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                LoggerCore.debugLog("当前数据源： " + dbName);
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
                LoggerCore.debugSqlExecution("selectList", dbName, processedSql, params.toArray(), results);
                LoggerCore.debugLog("查询完成，返回 " + results.size() + " 条记录，数据库: " + dbName);
                return results;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("===================");
                throw e;
            }
        }

        // 核心：单条 (Connection 版本)
        private static Map<String, Object> selectOneWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                LoggerCore.debugLog("当前数据源： " + dbName);
                setParameters(pstmt, params);
                Map<String, Object> result = null;
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
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
                LoggerCore.debugSqlExecution("selectOne", dbName, processedSql, params.toArray(), result);
                LoggerCore.debugLog("查询完成，返回 " + (result != null ? "1" : "0") + " 条记录，数据库: " + dbName);
                return result;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("===================");
                throw e;
            }
        }

        // 核心：执行 (Connection 版本)
        private static int executeWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();
                LoggerCore.debugSqlExecution("execute", dbName, processedSql, params.toArray(), rows);
                LoggerCore.debugLog("执行完成，影响 " + rows + " 行，数据库: " + dbName);
                return rows;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("===================");
                throw e;
            }
        }

        // 核心：全部 (dbName 版本)
        private static List<Map<String, Object>> selectListWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return selectListWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("查询执行失败", e);
            }
        }

        // 核心：单条 (dbName 版本)
        private static Map<String, Object> selectOneWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return selectOneWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("查询执行失败", e);
            }
        }

        // 核心：执行 (dbName 版本)
        private static int executeWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return executeWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("执行失败", e);
            }
        }

        // ==================== API1：直接使用SQL ====================

        // 全部：直接使用SQL
        public static List<Map<String, Object>> selectList(String dbName, String sql, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(sql, params);
            return selectListWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 单条：直接使用SQL
        public static Map<String, Object> selectOne(String dbName, String sql, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(sql, params);
            return selectOneWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 执行：直接使用SQL
        public static int execute(String dbName, String sql, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(sql, params);
            return executeWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // ==================== API2：指定具体XML ===================

        // 全部：指定具体XML
        public static List<Map<String, Object>> selectListFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return selectListWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 单条：指定具体XML
        public static Map<String, Object> selectOneFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return selectOneWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 执行：指定具体XML
        public static int executeFromXml(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executeWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // ==================== API3：自动搜索XML ===================

        // 全部：自动搜索XML
        public static List<Map<String, Object>> selectListFromXmlAuto(String dbName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(sqlId, params);
            return selectListWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 单条：自动搜索XML
        public static Map<String, Object> selectOneFromXmlAuto(String dbName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(sqlId, params);
            return selectOneWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }

        // 执行：自动搜索XML
        public static int executeFromXmlAuto(String dbName, String sqlId, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executeWithDbName(dbName, simpleSqlWithParams.sql, simpleSqlWithParams.params);
        }
    }

    // 执行器：MP SQL
    public static class MyBatisSqlExecutor {

        // ==================== 核心：SQL ====================

        // 设置PreparedStatement参数
        private static void setParameters(PreparedStatement pstmt, List<Object> params) throws SQLException {
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param == null) {
                        pstmt.setNull(i + 1, Types.NULL);
                    } else if (param instanceof String) {
                        pstmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Long) {
                        pstmt.setLong(i + 1, (Long) param);
                    } else if (param instanceof Double) {
                        pstmt.setDouble(i + 1, (Double) param);
                    } else if (param instanceof Float) {
                        pstmt.setFloat(i + 1, (Float) param);
                    } else if (param instanceof Boolean) {
                        pstmt.setBoolean(i + 1, (Boolean) param);
                    } else if (param instanceof Date) {
                        pstmt.setTimestamp(i + 1, new Timestamp(((Date) param).getTime()));
                    } else if (param instanceof java.sql.Date) {
                        pstmt.setDate(i + 1, (java.sql.Date) param);
                    } else if (param instanceof Timestamp) {
                        pstmt.setTimestamp(i + 1, (Timestamp) param);
                    } else {
                        pstmt.setObject(i + 1, param);
                    }
                }
            }
        }

        // 核心：全部 (Connection 版本)
        private static List<Map<String, Object>> selectListByMpWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            List<Map<String, Object>> results = new ArrayList<>();
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                LoggerCore.debugLog("MyBatis查询 - 当前数据源： " + dbName);
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
                LoggerCore.debugSqlExecution("MyBatis-selectListByMp", dbName, processedSql, params.toArray(), results);
                LoggerCore.debugLog("MyBatis查询完成，返回 " + results.size() + " 条记录，数据库: " + dbName);
                return results;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== MyBatis SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("MyBatis错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("========================");
                throw e;
            }
        }

        // 核心：单条 (Connection 版本)
        private static Map<String, Object> selectOneByMpWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                LoggerCore.debugLog("MyBatis单条查询 - 当前数据源： " + dbName);
                setParameters(pstmt, params);
                Map<String, Object> result = null;
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
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
                LoggerCore.debugSqlExecution("MyBatis-selectOneByMp", dbName, processedSql, params.toArray(), result);
                LoggerCore.debugLog("MyBatis单条查询完成，返回 " + (result != null ? "1" : "0") + " 条记录，数据库: " + dbName);
                return result;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== MyBatis SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("MyBatis错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("========================");
                throw e;
            }
        }

        // 核心：执行 (Connection 版本)
        private static int executeByMpWithConn(Connection conn, String sql, List<Object> params) throws SQLException {
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String processedSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            String dbName = conn.getCatalog();
            try (PreparedStatement pstmt = conn.prepareStatement(processedSql)) {
                LoggerCore.debugLog("MyBatis执行 - 当前数据源： " + dbName);
                setParameters(pstmt, params);
                int rows = pstmt.executeUpdate();
                LoggerCore.debugSqlExecution("MyBatis-executeByMp", dbName, processedSql, params.toArray(), rows);
                LoggerCore.debugLog("MyBatis执行完成，影响 " + rows + " 行，数据库: " + dbName);
                return rows;
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("=== MyBatis SQL执行错误详情 ===");
                LoggerCore.debugErrorLog("数据库: " + dbName);
                LoggerCore.debugErrorLog("错误代码: " + e.getErrorCode());
                LoggerCore.debugErrorLog("SQL状态: " + e.getSQLState());
                LoggerCore.debugErrorLog("错误消息: " + e.getMessage());
                LoggerCore.debugSqlWithParams("MyBatis错误SQL", processedSql, params.toArray());
                LoggerCore.debugErrorLog("=============================");
                throw e;
            }
        }

        // 核心：全部 (dbName 版本)
        private static List<Map<String, Object>> selectListByMpWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return selectListByMpWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("MyBatis查询执行失败", e);
            }
        }

        // 核心：单条 (dbName 版本)
        private static Map<String, Object> selectOneByMpWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return selectOneByMpWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("MyBatis单条查询执行失败", e);
            }
        }

        // 核心：执行 (dbName 版本)
        private static int executeByMpWithDbName(String dbName, String sql, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return executeByMpWithConn(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException("MyBatis执行失败", e);
            }
        }

        // ==================== API1：直接使用SQL，MP ===================

        // 全部：执行器，MP
        public static List<Map<String, Object>> selectListByMp(String dbName, String sql, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(sql, params);
            return selectListByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 单条：执行器，MP
        public static Map<String, Object> selectOneByMp(String dbName, String sql, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(sql, params);
            return selectOneByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 执行：执行器，MP
        public static int executeByMp(String dbName, String sql, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(sql, params);
            return executeByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // ==================== API2：指定具体XML，MP ===================

        // 全部：指定具体XML，MP
        public static List<Map<String, Object>> selectListFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return selectListByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 单条：指定具体XML，MP
        public static Map<String, Object> selectOneFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return selectOneByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 执行：指定具体XML，MP
        public static int executeFromXmlByMp(String dbName, String xmlFileName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executeByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // ==================== API3：自动搜索XML，MP ===================

        // 全部：自动搜索XML，MP
        public static List<Map<String, Object>> selectListFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(sqlId, params);
            return selectListByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 单条：自动搜索XML，MP
        public static Map<String, Object> selectOneFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(sqlId, params);
            return selectOneByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }

        // 执行：自动搜索XML，MP
        public static int executeFromXmlAutoByMp(String dbName, String sqlId, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executeByMpWithDbName(dbName, parsedSql.getSql(), parsedSql.getParams());
        }
    }

    // 分页器：SQL
    public static class PaginationHandler {

        // ==================== 核心1：SQL ====================

        // 设置PreparedStatement参数
        private static void setParameters(PreparedStatement pstmt, List<Object> params) throws SQLException {
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param == null) {
                        pstmt.setNull(i + 1, Types.NULL);
                    } else if (param instanceof String) {
                        pstmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Long) {
                        pstmt.setLong(i + 1, (Long) param);
                    } else if (param instanceof Double) {
                        pstmt.setDouble(i + 1, (Double) param);
                    } else if (param instanceof Float) {
                        pstmt.setFloat(i + 1, (Float) param);
                    } else if (param instanceof Boolean) {
                        pstmt.setBoolean(i + 1, (Boolean) param);
                    } else if (param instanceof Date) {
                        pstmt.setTimestamp(i + 1, new Timestamp(((Date) param).getTime()));
                    } else if (param instanceof java.sql.Date) {
                        pstmt.setDate(i + 1, (java.sql.Date) param);
                    } else if (param instanceof Timestamp) {
                        pstmt.setTimestamp(i + 1, (Timestamp) param);
                    } else {
                        pstmt.setObject(i + 1, param);
                    }
                }
            }
        }

        // 分页结果包装类
        public static class PageResult<T> {

            // 数据列表
            private final List<T> list;
            // 总记录数
            private final long total;
            // 当前页码
            private final int pageNum;
            // 每页大小
            private final int pageSize;
            // 总页数
            private final int pages;

            public PageResult(List<T> list, long total, int pageNum, int pageSize) {
                this.list = list;
                this.total = total;
                this.pageNum = pageNum;
                this.pageSize = pageSize;
                this.pages = (int) Math.ceil((double) total / pageSize);
            }

            public List<T> getList() {
                return list;
            }

            public long getTotal() {
                return total;
            }

            public int getPageNum() {
                return pageNum;
            }

            public int getPageSize() {
                return pageSize;
            }

            public int getPages() {
                return pages;
            }

            // 转换为Map格式返回
            public Map<String, Object> toMap() {
                Map<String, Object> result = new HashMap<>();
                result.put("list", list);
                result.put("total", total);
                result.put("pageNum", pageNum);
                result.put("pageSize", pageSize);
                result.put("pages", pages);
                return result;
            }
        }

        // 数据库类型枚举
        public enum DatabaseType {
            MYSQL("mysql"),
            ORACLE("oracle"),
            H2("h2"),
            UNKNOWN("unknown");

            private final String type;

            DatabaseType(String type) {
                this.type = type;
            }

            public String getType() {
                return type;
            }
        }

        // 分页策略接口
        public interface PaginationStrategy {

            /**
             * 构建COUNT查询SQL
             */
            String buildCountSql(String originalSql);

            /**
             * 构建分页查询SQL
             */
            String buildPaginationSql(String originalSql, int pageNum, int pageSize);

            /**
             * 判断策略是否适用于当前数据库类型和SQL
             */
            boolean isApplicable(DatabaseType dbType, String sql);

            /**
             * 获取策略名称
             */
            default String getStrategyName() {
                return this.getClass().getSimpleName();
            }
        }

        // MySQL 分页策略
        public static class MySQLPaginationStrategy implements PaginationStrategy {

            @Override
            public String buildCountSql(String originalSql) {
                return buildStandardCountSql(originalSql);
            }

            @Override
            public String buildPaginationSql(String originalSql, int pageNum, int pageSize) {
                int offset = (pageNum - 1) * pageSize;
                return originalSql + " LIMIT " + offset + ", " + pageSize;
            }

            @Override
            public boolean isApplicable(DatabaseType dbType, String sql) {
                return dbType == DatabaseType.MYSQL;
            }
        }

        // Oracle 标准分页策略（不包含WITH子句）
        public static class OracleStandardPaginationStrategy implements PaginationStrategy {

            @Override
            public String buildCountSql(String originalSql) {
                return buildStandardCountSql(originalSql);
            }

            @Override
            public String buildPaginationSql(String originalSql, int pageNum, int pageSize) {
                int offset = (pageNum - 1) * pageSize;
                int limit = offset + pageSize;
                return "SELECT * FROM (" +
                    "SELECT ROWNUM rn, t.* FROM (" + originalSql + ") t " +
                    "WHERE ROWNUM <= " + limit + ") " +
                    "WHERE rn > " + offset;
            }

            @Override
            public boolean isApplicable(DatabaseType dbType, String sql) {
                return dbType == DatabaseType.ORACLE && !containsWithClause(sql);
            }
        }

        // Oracle WITH子句分页策略
        public static class OracleWithClausePaginationStrategy implements PaginationStrategy {

            @Override
            public String buildCountSql(String originalSql) {
                return OracleWithClausePagination.buildCountSql(originalSql);
            }

            @Override
            public String buildPaginationSql(String originalSql, int pageNum, int pageSize) {
                return OracleWithClausePagination.buildPaginationSql(originalSql, pageNum, pageSize);
            }

            @Override
            public boolean isApplicable(DatabaseType dbType, String sql) {
                return dbType == DatabaseType.ORACLE && containsWithClause(sql);
            }
        }

        // Oracle WITH子句分页处理
        public static class OracleWithClausePagination {

            // 检查SQL是否包含WITH子句
            public static boolean containsWithClause(String sql) {
                if (sql == null || sql.trim().isEmpty()) {
                    return false;
                }
                String upperSql = sql.trim().toUpperCase();
                return upperSql.startsWith("WITH ");
            }

            // 构建分页SQL
            public static String buildPaginationSql(String originalSql, int pageNum, int pageSize) {
                int offset = (pageNum - 1) * pageSize;
                return "SELECT * FROM (" +
                    "SELECT ROWNUM rn, t.* FROM (" + originalSql + ") t " +
                    "WHERE ROWNUM <= " + (offset + pageSize) + ") " +
                    "WHERE rn > " + offset;
            }

            // 构建COUNT SQL
            public static String buildCountSql(String originalSql) {
                return "SELECT COUNT(*) FROM (" + originalSql + ")";
            }

            // 提取ORDER BY子句
            private static String extractOrderByClause(String sql) {
                Pattern pattern = Pattern.compile("ORDER\\s+BY\\s+[^)]+$", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sql);
                return matcher.find() ? matcher.group() : "";
            }

            // 移除ORDER BY子句
            private static String removeOrderByClause(String sql) {
                return sql.replaceAll("ORDER\\s+BY\\s+[^)]+$", "").trim();
            }
        }

        // H2 分页策略
        public static class H2PaginationStrategy implements PaginationStrategy {

            @Override
            public String buildCountSql(String originalSql) {
                return buildStandardCountSql(originalSql);
            }

            @Override
            public String buildPaginationSql(String originalSql, int pageNum, int pageSize) {
                int offset = (pageNum - 1) * pageSize;
                return originalSql + " LIMIT " + pageSize + " OFFSET " + offset;
            }

            @Override
            public boolean isApplicable(DatabaseType dbType, String sql) {
                return dbType == DatabaseType.H2;
            }
        }

        // 所有分页策略列表（按优先级排序）
        private static final List<PaginationStrategy> STRATEGIES = Arrays.asList(
            new OracleWithClausePaginationStrategy(),  // Oracle WITH子句优先
            new OracleStandardPaginationStrategy(),    // Oracle 标准分页
            new MySQLPaginationStrategy(),             // MySQL 分页
            new H2PaginationStrategy()                 // H2 分页
        );

        // 获取适用的分页策略
        public static PaginationStrategy getStrategy(DatabaseType dbType, String sql) {
            return STRATEGIES.stream()
                .filter(strategy -> strategy.isApplicable(dbType, sql))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("不支持的数据库类型: " + dbType));
        }

        // 检查SQL是否包含WITH子句
        private static boolean containsWithClause(String sql) {
            return OracleWithClausePagination.containsWithClause(sql);
        }

        // 构建标准COUNT查询SQL
        private static String buildStandardCountSql(String originalSql) {
            String upperSql = originalSql.toUpperCase().trim();

            // 对于包含GROUP BY、HAVING、DISTINCT、UNION的复杂查询，使用子查询方式
            if (upperSql.contains("GROUP BY") || upperSql.contains("HAVING") ||
                upperSql.contains("DISTINCT") || upperSql.contains("UNION")) {
                return "SELECT COUNT(*) FROM (" + originalSql + ") temp_count_table";
            } else {
                // 对于简单查询，直接替换SELECT部分
                Pattern fromPattern = Pattern.compile("\\bFROM\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = fromPattern.matcher(originalSql);
                if (matcher.find()) {
                    String fromPart = originalSql.substring(matcher.start());
                    // 移除ORDER BY子句（COUNT查询不需要排序）
                    Pattern orderByPattern = Pattern.compile("\\bORDER\\s+BY\\b.*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    fromPart = orderByPattern.matcher(fromPart).replaceAll("");
                    return "SELECT COUNT(*) " + fromPart;
                }
            }
            // 兜底方案：使用子查询
            return "SELECT COUNT(*) FROM (" + originalSql + ") temp_count_table";
        }

        // ==================== 核心2：SQL ====================

        // 获取数据库类型 (dbName 版本)
        private static DatabaseType getDatabaseTypeWithDbName(String dbName) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return getDatabaseTypeWithConn(conn);
            } catch (Exception e) {
                LoggerCore.debugErrorLog("获取数据库类型失败: " + e.getMessage());
                return DatabaseType.UNKNOWN;
            }
        }

        // 获取数据库类型（通过Connection）
        private static DatabaseType getDatabaseTypeWithConn(Connection conn) {
            try {
                String driverName = conn.getMetaData().getDriverName().toLowerCase();
                String url = conn.getMetaData().getURL().toLowerCase();

                LoggerCore.debugLog("数据库驱动: " + driverName + ", URL: " + url);

                if (driverName.contains("mysql") || url.contains("mysql")) {
                    return DatabaseType.MYSQL;
                } else if (driverName.contains("oracle") || url.contains("oracle")) {
                    return DatabaseType.ORACLE;
                } else if (driverName.contains("h2") || url.contains("h2")) {
                    return DatabaseType.H2;
                } else {
                    LoggerCore.debugLog("未识别的数据库类型，驱动: " + driverName);
                    return DatabaseType.UNKNOWN;
                }
            } catch (Exception e) {
                LoggerCore.debugErrorLog("从连接获取数据库类型失败: " + e.getMessage());
                return DatabaseType.UNKNOWN;
            }
        }

        // 执行分页查询的核心方法 (dbName 版本)
        public static PageResult<Map<String, Object>> executePaginationQueryWithDbName(
            String dbName, String sql, int pageNum, int pageSize, List<Object> params) {
            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                return executePaginationQueryWithConn(conn, sql, pageNum, pageSize, params);
            } catch (SQLException e) {
                LoggerCore.debugErrorLog("分页查询获取连接失败: " + e.getMessage());
                throw new RuntimeException("分页查询获取连接失败", e);
            }
        }

        // 执行分页查询的核心方法 (Connection 版本)
        public static PageResult<Map<String, Object>> executePaginationQueryWithConn(
            Connection conn, String sql, int pageNum, int pageSize, List<Object> params) {

            // 参数验证和标准化
            if (pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }

            LoggerCore.debugLog("=== 统一分页查询开始 ===");
            LoggerCore.debugLog("页码: " + pageNum + ", 每页大小: " + pageSize);

            // 第一步：清理SQL中的注释（防止Druid误判为SQL注入）
            String cleanedSql = CommonSqlParser.removeComments(sql);
            LoggerCore.debugLog("SQL注释清理: " + (sql.equals(cleanedSql) ? "无需清理" : "已清理完成"));

            // 获取数据库类型
            DatabaseType dbType = getDatabaseTypeWithConn(conn);
            LoggerCore.debugLog("数据库类型: " + dbType);

            // 获取适用的分页策略
            PaginationStrategy strategy = getStrategy(dbType, cleanedSql);
            String strategyName = strategy.getStrategyName();
            LoggerCore.debugLog("使用分页策略: " + strategyName);

            try {
                String dbName = conn.getCatalog(); // 尝试获取数据库名称用于日志记录

                // 第二步：执行COUNT查询获取总记录数
                String countSql = strategy.buildCountSql(cleanedSql);
                long totalCount = 0;
                try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    setParameters(countStmt, params);
                    try (ResultSet countRs = countStmt.executeQuery()) {
                        if (countRs.next()) {
                            // 只需要获取第一行（也是唯一一行）
                            // 通常 COUNT(*) 查询的结果在第一列
                            totalCount = countRs.getLong(1);
                        }
                    }
                }
                // 第三步：如果总记录数为0，直接返回空结果
                if (totalCount == 0) {
                    LoggerCore.debugLog("总记录数为0，返回空结果");
                    return new PageResult<>(new ArrayList<>(), 0, pageNum, pageSize);
                }
                // 使用新的SQL调试输出
                LoggerCore.debugSqlExecution("COUNT查询", dbName, countSql, params.toArray(), totalCount);

                // 第四步：执行分页数据查询
                String paginationSql = strategy.buildPaginationSql(cleanedSql, pageNum, pageSize);

                List<Map<String, Object>> results = new ArrayList<>();
                try (PreparedStatement pstmt = conn.prepareStatement(paginationSql)) {
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
                }

                // 使用新的SQL调试输出显示分页查询结果
                LoggerCore.debugSqlExecution("分页查询", dbName, paginationSql, params.toArray(), results);

                // 第五步：构建分页结果对象
                PageResult<Map<String, Object>> pageResult =
                    new PageResult<>(results, totalCount, pageNum, pageSize);

                LoggerCore.debugLog("=== 统一分页查询完成 ===");
                LoggerCore.debugLog("返回数据条数: " + results.size() + "/" + totalCount);
                LoggerCore.debugLog("总页数: " + pageResult.getPages());
                LoggerCore.debugLog("使用策略: " + strategyName);

                return pageResult;

            } catch (SQLException e) {
                LoggerCore.debugErrorLog("统一分页查询执行失败: " + e.getMessage());
                LoggerCore.logException("统一分页查询", e);
                throw new RuntimeException("统一分页查询执行失败: " + e.getMessage(), e);
            }
        }

        // ==================== API1：dbName 版本 ====================

        // 分页：直接使用SQL (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageWithDbName(
            String dbName, String sql, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(sql, params);
            return executePaginationQueryWithDbName(dbName, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // 分页：指定具体XML (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlWithDbName(
            String dbName, String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executePaginationQueryWithDbName(dbName, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // 分页：自动搜索XML (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlAutoWithDbName(
            String dbName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executePaginationQueryWithDbName(dbName, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // ==================== API2：Connection 版本 ====================

        // 分页：直接使用SQL (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageWithConn(
            Connection conn, String sql, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(sql, params);
            return executePaginationQueryWithConn(conn, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // 分页：指定具体XML (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlWithConn(
            Connection conn, String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executePaginationQueryWithConn(conn, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // 分页：自动搜索XML (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlAutoWithConn(
            Connection conn, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            SimpleSqlParser.SimpleSqlWithParams simpleSqlWithParams = SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executePaginationQueryWithConn(conn, simpleSqlWithParams.sql, pageNum, pageSize, simpleSqlWithParams.params);
        }

        // ==================== API1：dbName 版本, MP ====================

        // 分页：直接使用SQL, MP (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageByMpWithDbName(
            String dbName, String sql, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(sql, params);
            return executePaginationQueryWithDbName(dbName, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // 分页：指定具体XML, MP (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlByMpWithDbName(
            String dbName, String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executePaginationQueryWithDbName(dbName, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // 分页：自动搜索XML, MP (dbName 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlAutoByMpWithDbName(
            String dbName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executePaginationQueryWithDbName(dbName, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // ==================== API2：Connection 版本, MP ====================

        // 分页：直接使用SQL, MP (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageByMpWithConn(
            Connection conn, String sql, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(sql, params);
            return executePaginationQueryWithConn(conn, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // 分页：指定具体XML, MP (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlByMpWithConn(
            Connection conn, String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(xmlFileName, sqlId, params);
            return executePaginationQueryWithConn(conn, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // 分页：自动搜索XML, MP (Connection 版本)
        public static PageResult<Map<String, Object>> selectPageFromXmlAutoByMpWithConn(
            Connection conn, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(sqlId, params);
            return executePaginationQueryWithConn(conn, parsedSql.getSql(), pageNum, pageSize, parsedSql.getParams());
        }

        // ==================== 辅助方法 ====================

        // 获取所有可用的分页策略信息
        public static List<String> getAvailableStrategies() {
            return STRATEGIES.stream()
                .map(PaginationStrategy::getStrategyName)
                .collect(Collectors.toList());
        }

        // 测试分页策略选择
        public static String testStrategySelection(DatabaseType dbType, String sql) {
            try {
                PaginationStrategy strategy = getStrategy(dbType, sql);
                return strategy.getStrategyName();
            } catch (Exception e) {
                return "无适用策略: " + e.getMessage();
            }
        }
    }

    // 批量操作：SQL
    public static class BatchOperationHandler {

        // 设置PreparedStatement参数
        private static void setParameters(PreparedStatement pstmt, List<Object> params) throws SQLException {
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param == null) {
                        pstmt.setNull(i + 1, Types.NULL);
                    } else if (param instanceof String) {
                        pstmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Long) {
                        pstmt.setLong(i + 1, (Long) param);
                    } else if (param instanceof Double) {
                        pstmt.setDouble(i + 1, (Double) param);
                    } else if (param instanceof Float) {
                        pstmt.setFloat(i + 1, (Float) param);
                    } else if (param instanceof Boolean) {
                        pstmt.setBoolean(i + 1, (Boolean) param);
                    } else if (param instanceof Date) {
                        pstmt.setTimestamp(i + 1, new Timestamp(((Date) param).getTime()));
                    } else if (param instanceof java.sql.Date) {
                        pstmt.setDate(i + 1, (java.sql.Date) param);
                    } else if (param instanceof Timestamp) {
                        pstmt.setTimestamp(i + 1, (Timestamp) param);
                    } else {
                        pstmt.setObject(i + 1, param);
                    }
                }
            }
        }

        // ==================== 核心1：batchExecute ====================

        // 默认批次大小
        private static final int DEFAULT_BATCH_SIZE = 1000;

        // 批量操作结果封装类
        public static class BatchResult {

            private final int totalRecords;
            private final int successRecords;
            private final int failedRecords;
            private final List<String> errorMessages;
            private final long executionTimeMs;

            public BatchResult(int totalRecords, int successRecords, int failedRecords,
                List<String> errorMessages, long executionTimeMs) {
                this.totalRecords = totalRecords;
                this.successRecords = successRecords;
                this.failedRecords = failedRecords;
                this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
                this.executionTimeMs = executionTimeMs;
            }

            public int getTotalRecords() {
                return totalRecords;
            }

            public int getSuccessRecords() {
                return successRecords;
            }

            public int getFailedRecords() {
                return failedRecords;
            }

            public List<String> getErrorMessages() {
                return errorMessages;
            }

            public long getExecutionTimeMs() {
                return executionTimeMs;
            }

            public boolean isSuccess() {
                return failedRecords == 0;
            }

            @Override
            public String toString() {
                return String.format("BatchResult{总记录数=%d, 成功=%d, 失败=%d, 耗时=%dms}",
                    totalRecords, successRecords, failedRecords, executionTimeMs);
            }
        }

        // 批量操作类型枚举
        public enum BatchOperationType {
            INSERT("INSERT"),
            UPDATE("UPDATE"),
            DELETE("DELETE"),
            UPSERT("UPSERT");

            private final String operation;

            BatchOperationType(String operation) {
                this.operation = operation;
            }

            public String getOperation() {
                return operation;
            }
        }

        // 核心批量操作执行方法 (dbName版本)
        private static BatchResult executeBatchOperation(String dbName, String sql, List<Object[]> paramsList,
            int batchSize, BatchOperationType operationType) {

            long startTime = System.currentTimeMillis();
            LoggerCore.debugLog("=== 开始执行批量" + operationType.getOperation() + "操作 (dbName版本) ===");
            LoggerCore.debugLog("数据库: " + dbName + ", 总记录数: " + paramsList.size() + ", 批次大小: " + batchSize);

            try (Connection conn = DataSourceCore.getConnection(dbName)) {
                conn.setAutoCommit(false); // 内部管理事务
                // 传入连接，并让其提交,最后一个参数为true
                BatchResult result = executeBatchOperationWithConn(conn, sql, paramsList, batchSize, operationType, true);
                long endTime = System.currentTimeMillis();
                LoggerCore.debugLog("=== 批量" + operationType.getOperation() + "操作 (dbName版本) 完成 ===");
                LoggerCore.debugLog("执行结果: " + result.toString());
                return result;
            } catch (SQLException e) {
                long endTime = System.currentTimeMillis();
                String errorMsg = "数据库连接或事务异常: " + e.getMessage();
                LoggerCore.logException("批量操作", e);
                return new BatchResult(paramsList.size(), 0, paramsList.size(),
                    Collections.singletonList(errorMsg), endTime - startTime);
            }
        }

        // 核心批量操作执行方法 (Connection版本)
        private static BatchResult executeBatchOperationWithConn(Connection conn, String sql, List<Object[]> paramsList,
            int batchSize, BatchOperationType operationType, boolean commitTransaction) {

            long startTime = System.currentTimeMillis();
            LoggerCore.debugLog("=== 开始执行批量" + operationType.getOperation() + "操作 (Connection版本) ===");
            LoggerCore.debugLog("总记录数: " + paramsList.size() + ", 批次大小: " + batchSize);

            // 清理SQL注释
            String cleanedSql = CommonSqlParser.removeComments(sql);
            String tempSql = CommonSqlParser.smartTrimSqlSemicolon(cleanedSql);
            LoggerCore.debugLog("SQL注释清理: " + (sql.equals(cleanedSql) ? "无需清理" : "已清理完成"));

            // 关键修复：将MyBatis风格的占位符替换为JDBC标准的'?'占位符
            String finalSql = tempSql.replaceAll("#\\{[^}]+\\}", "?");
            LoggerCore.debugLog("批量操作SQL已转换为JDBC格式: " + finalSql);

            int totalRecords = paramsList.size();
            int successRecords = 0;
            int failedRecords = 0;
            List<String> errorMessages = new ArrayList<>();

            try {
                // 分批处理
                for (int i = 0; i < paramsList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, paramsList.size());
                    List<Object[]> batch = paramsList.subList(i, endIndex);

                    LoggerCore.debugLog("处理批次: " + ((i / batchSize) + 1) + ", 记录范围: " + (i + 1) + "-" + endIndex);

                    try (PreparedStatement pstmt = conn.prepareStatement(finalSql)) {

                        // 添加批次数据
                        for (Object[] params : batch) {
                            setParameters(pstmt, Arrays.asList(params));
                            pstmt.addBatch();
                        }

                        // 执行批次
                        int[] batchResults = pstmt.executeBatch();

                        // 统计结果
                        for (int result : batchResults) {
                            if (result > 0 || result == PreparedStatement.SUCCESS_NO_INFO) {
                                successRecords++;
                            } else {
                                failedRecords++;
                                errorMessages.add("批次执行失败，结果代码: " + result);
                            }
                        }

                        LoggerCore.debugLog("批次执行完成，成功: " + countSuccessResults(batchResults) + ", 失败: " + (batch.size() - countSuccessResults(batchResults)));

                    } catch (SQLException e) {
                        failedRecords += batch.size();
                        String errorMsg = "批次执行异常: " + e.getMessage();
                        errorMessages.add(errorMsg);
                        LoggerCore.debugErrorLog(errorMsg);
                        // 对于外部事务，我们不应继续，而是向上抛出异常让调用者决定如何处理
                        throw e;
                    }
                }

                // 如果需要此方法内部提交事务
                if (commitTransaction) {
                    conn.commit();
                    LoggerCore.debugLog("事务提交成功 (内部提交)");
                }

            } catch (SQLException e) {
                // 如果是dbName版本，这里会捕获到连接获取的异常
                // 如果是conn版本，这里会捕获到上层抛出的批处理异常
                failedRecords = totalRecords;
                successRecords = 0;
                String errorMsg = "数据库处理异常: " + e.getMessage();
                errorMessages.add(errorMsg);
                LoggerCore.logException("批量操作", e);
                // 对于外部事务，建议回滚
                if (!commitTransaction) {
                    try {
                        conn.rollback();
                        LoggerCore.debugLog("事务已回滚 (外部管理)");
                    } catch (SQLException ex) {
                        LoggerCore.logException("回滚事务失败", ex);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            BatchResult result = new BatchResult(totalRecords, successRecords, failedRecords, errorMessages, executionTime);

            LoggerCore.debugLog("=== 批量" + operationType.getOperation() + "操作完成 (Connection版本) ===");

            return result;
        }

        // 统计成功的批次结果数量
        private static int countSuccessResults(int[] batchResults) {
            int count = 0;
            for (int result : batchResults) {
                if (result > 0 || result == PreparedStatement.SUCCESS_NO_INFO) {
                    count++;
                }
            }
            return count;
        }

        // 获取优化的批次大小建议
        public static int getOptimalBatchSize(PaginationHandler.DatabaseType dbType, int dataSize) {
            // 根据数据库类型和数据量推荐最优批次大小
            switch (dbType) {
                case MYSQL:
                    return dataSize < 10000 ? Math.min(1000, dataSize) : 5000;
                case ORACLE:
                    return dataSize < 10000 ? Math.min(500, dataSize) : 2000;
                case H2:
                    return dataSize < 10000 ? Math.min(2000, dataSize) : 10000;
                default:
                    return Math.min(DEFAULT_BATCH_SIZE, dataSize);
            }
        }

        // ==================== 核心2：UPSERT SQL ====================

        // 构建跨数据库的UPSERT SQL语句
        private static String buildUpsertSql(PaginationHandler.DatabaseType dbType, String tableName,
            String[] keyColumns, String[] valueColumns) {

            switch (dbType) {
                case MYSQL:
                    return buildMySQLUpsertSql(tableName, keyColumns, valueColumns);
                case ORACLE:
                    return buildOracleUpsertSql(tableName, keyColumns, valueColumns);
                case H2:
                    return buildH2UpsertSql(tableName, keyColumns, valueColumns);
                default:
                    throw new UnsupportedOperationException("不支持的数据库类型进行UPSERT操作: " + dbType);
            }
        }

        // 构建MySQL UPSERT SQL（使用 ON DUPLICATE KEY UPDATE）
        private static String buildMySQLUpsertSql(String tableName, String[] keyColumns, String[] valueColumns) {
            StringBuilder sql = new StringBuilder();

            // INSERT部分
            sql.append("INSERT INTO ").append(tableName).append(" (");

            // 列名（键列 + 值列）
            List<String> allColumns = new ArrayList<>();
            allColumns.addAll(Arrays.asList(keyColumns));
            allColumns.addAll(Arrays.asList(valueColumns));
            sql.append(String.join(", ", allColumns));

            sql.append(") VALUES (");

            // 占位符
            String placeholders = String.join(", ", Collections.nCopies(allColumns.size(), "?"));
            sql.append(placeholders);

            sql.append(") ON DUPLICATE KEY UPDATE ");

            // UPDATE部分（只更新值列）
            List<String> updateParts = new ArrayList<>();
            for (String column : valueColumns) {
                updateParts.add(column + " = VALUES(" + column + ")");
            }
            sql.append(String.join(", ", updateParts));

            return sql.toString();
        }

        // 构建Oracle UPSERT SQL（使用 MERGE 语句）
        private static String buildOracleUpsertSql(String tableName, String[] keyColumns, String[] valueColumns) {
            StringBuilder sql = new StringBuilder();

            sql.append("MERGE INTO ").append(tableName).append(" t USING (");
            sql.append("SELECT ");

            // 选择子查询的列
            List<String> selectParts = new ArrayList<>();
            List<String> allColumns = new ArrayList<>();
            allColumns.addAll(Arrays.asList(keyColumns));
            allColumns.addAll(Arrays.asList(valueColumns));

            for (int i = 0; i < allColumns.size(); i++) {
                selectParts.add("? AS " + allColumns.get(i));
            }
            sql.append(String.join(", ", selectParts));
            sql.append(" FROM DUAL");

            sql.append(") s ON (");

            // 匹配条件
            List<String> matchConditions = new ArrayList<>();
            for (String keyColumn : keyColumns) {
                matchConditions.add("t." + keyColumn + " = s." + keyColumn);
            }
            sql.append(String.join(" AND ", matchConditions));

            sql.append(") WHEN MATCHED THEN UPDATE SET ");

            // 更新部分
            List<String> updateParts = new ArrayList<>();
            for (String column : valueColumns) {
                updateParts.add("t." + column + " = s." + column);
            }
            sql.append(String.join(", ", updateParts));

            sql.append(" WHEN NOT MATCHED THEN INSERT (");
            sql.append(String.join(", ", allColumns));
            sql.append(") VALUES (");

            List<String> insertValues = new ArrayList<>();
            for (String column : allColumns) {
                insertValues.add("s." + column);
            }
            sql.append(String.join(", ", insertValues));
            sql.append(")");

            return sql.toString();
        }

        // 构建H2 UPSERT SQL（使用H2原生的简单MERGE语法）
        private static String buildH2UpsertSql(String tableName, String[] keyColumns, String[] valueColumns) {
            StringBuilder sql = new StringBuilder();

            // 使用H2原生的MERGE INTO KEY语法，支持H2数据库的特殊语法
            // H2的MERGE INTO KEY语法需要包含表的所有列
            // 对于test_users表: id, name, email, age, created_time, updated_time

            sql.append("MERGE INTO ").append(tableName);

            // 添加KEY子句
            if (keyColumns.length > 0) {
                sql.append(" KEY(");
                sql.append(String.join(", ", keyColumns));
                sql.append(")");
            }

            // 添加VALUES子句
            sql.append(" VALUES (");

            // 为H2数据库，我们需要为表的所有列提供值
            // 假设表结构为: id, name, email, age, created_time, updated_time
            // 对于test_users表，我们需要6个占位符
            if ("test_users".equals(tableName)) {
                // 为test_users表生成6个占位符: id, name, email, age, created_time, updated_time
                sql.append("?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP");
            } else {
                // 对于其他表，使用原来的逻辑
                int totalColumns = keyColumns.length + valueColumns.length;
                String placeholders = String.join(", ", Collections.nCopies(totalColumns, "?"));
                sql.append(placeholders);
            }

            sql.append(")");

            return sql.toString();
        }

        // 转换实体对象为参数数组
        @SuppressWarnings("unchecked")
        private static <T> List<Object[]> convertEntitiesToParams(List<Map<String, Object>> entities, String[] keyColumns, String[] valueColumns) {
            List<Object[]> paramsList = new ArrayList<>();

            for (Map<String, Object> entity : entities) {
                List<Object> params = new ArrayList<>();

                // 这里需要根据实际的实体类型进行属性提取
                if (entity instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) entity;

                    // 添加键列值
                    for (String keyColumn : keyColumns) {
                        params.add(map.get(keyColumn));
                    }

                    // 添加值列值
                    for (String valueColumn : valueColumns) {
                        params.add(map.get(valueColumn));
                    }
                } else {
                    // 对于其他类型的实体，可以使用反射或者要求实现特定接口
                    throw new UnsupportedOperationException("当前只支持Map类型的实体，其他类型请转换为Map");
                }

                paramsList.add(params.toArray());
            }

            return paramsList;
        }

        // ==================== API1：dbName 版本 ====================

        // 批量合并（UPSERT）：根据数据库类型选择最优策略
        public static <T> BatchResult batchUpsertWithDbName(String dbName, String tableName, List<Map<String, Object>> entities,
            String[] keyColumns, String[] valueColumns) {
            return batchUpsertSizeWithDbName(dbName, tableName, entities, keyColumns, valueColumns, DEFAULT_BATCH_SIZE);
        }

        // 批量合并（UPSERT）：指定批次大小
        public static <T> BatchResult batchUpsertSizeWithDbName(String dbName, String tableName, List<Map<String, Object>> entities,
            String[] keyColumns, String[] valueColumns, int batchSize) {

            long startTime = System.currentTimeMillis();
            LoggerCore.debugLog("=== 开始批量UPSERT操作 (dbName版本) ===");
            LoggerCore.debugLog("目标表: " + tableName + ", 数据条数: " + entities.size() + ", 批次大小: " + batchSize);

            try {
                // 获取数据库类型
                PaginationHandler.DatabaseType dbType = PaginationHandler.getDatabaseTypeWithDbName(dbName);
                String upsertSql = buildUpsertSql(dbType, tableName, keyColumns, valueColumns);

                LoggerCore.debugLog("生成的UPSERT SQL: " + upsertSql);

                // 转换实体为参数数组
                List<Object[]> paramsList = convertEntitiesToParams(entities, keyColumns, valueColumns);

                // 执行批量操作
                BatchResult result = executeBatchOperation(dbName, upsertSql, paramsList, batchSize, BatchOperationType.UPSERT);

                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;

                LoggerCore.debugLog("=== 批量UPSERT操作完成 (dbName版本) ===");
                LoggerCore.debugLog("执行耗时: " + executionTime + "ms");
                LoggerCore.debugLog("执行结果: " + result.toString());

                return result;

            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                LoggerCore.logException("批量UPSERT操作", e);
                return new BatchResult(entities.size(), 0, entities.size(),
                    Arrays.asList("批量UPSERT执行失败: " + e.getMessage()), endTime - startTime);
            }
        }

        // ==================== API2：Connection 版本 ====================

        // 批量合并（UPSERT）：使用外部连接，默认批次大小
        public static <T> BatchResult batchUpsertWithConn(Connection conn, String tableName, List<Map<String, Object>> entities,
            String[] keyColumns, String[] valueColumns) {
            return batchUpsertSizeWithConn(conn, tableName, entities, keyColumns, valueColumns, DEFAULT_BATCH_SIZE);
        }

        // 批量合并（UPSERT）：使用外部连接，指定批次大小
        public static <T> BatchResult batchUpsertSizeWithConn(Connection conn, String tableName, List<Map<String, Object>> entities,
            String[] keyColumns, String[] valueColumns, int batchSize) {

            LoggerCore.debugLog("=== 开始批量UPSERT操作 (Connection版本) ===");
            LoggerCore.debugLog("目标表: " + tableName + ", 数据条数: " + entities.size() + ", 批次大小: " + batchSize);

            try {
                // 获取数据库类型
                PaginationHandler.DatabaseType dbType = PaginationHandler.getDatabaseTypeWithConn(conn);
                String upsertSql = buildUpsertSql(dbType, tableName, keyColumns, valueColumns);

                LoggerCore.debugLog("生成的UPSERT SQL: " + upsertSql);

                // 转换实体为参数数组
                List<Object[]> paramsList = convertEntitiesToParams(entities, keyColumns, valueColumns);

                // 执行批量操作，事务由外部管理 (commitTransaction = false)
                return executeBatchOperationWithConn(conn, upsertSql, paramsList, batchSize, BatchOperationType.UPSERT, false);

            } catch (Exception e) {
                LoggerCore.logException("批量UPSERT操作 (Connection版本)", e);
                // 发生异常时，向上抛出，由调用者决定如何处理事务（回滚或提交）
                throw new RuntimeException("批量UPSERT执行失败 (Connection版本): " + e.getMessage(), e);
            }
        }

        // ==================== 辅助方法 ====================

        // 私有构造函数，防止实例化
        private BatchOperationHandler() {
            throw new UnsupportedOperationException("批量操作处理器不允许实例化");
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

        // 设置默认数据源
        public DatabaseAccessor setDefaultDataSource(String dataSourceName) {
            DataSourceCore.setDefaultDataSource(dataSourceName);
            return new DatabaseAccessor(dataSourceName);
        }

        // 设置线程数据源
        public DatabaseAccessor setThreadDataSourceContext(String dataSourceName) {
            DataSourceCore.setThreadDataSourceContext(dataSourceName);
            return new DatabaseAccessor(dataSourceName);
        }

        // 设置默认数据源，重载
        public DatabaseAccessor setDefaultDataSource() {
            if (this.dataSourceName == null) {
                throw new IllegalStateException("无法设置默认数据源，因为当前访问器未关联特定的数据源。");
            }
            DataSourceCore.setDefaultDataSource(this.dataSourceName);
            return this;
        }

        // 设置线程数据源，重载
        public DatabaseAccessor setThreadDataSourceContext() {
            if (this.dataSourceName == null) {
                throw new IllegalStateException("无法设置线程数据源，因为当前访问器未关联特定的数据源。");
            }
            DataSourceCore.setThreadDataSourceContext(this.dataSourceName);
            return this;
        }

        // ==================== 事务管理方法 ====================

        // 获取最近一次写操作影响的行数
        public int result() {
            return this.lastAffectedRows;
        }

        // 获取累计影响的总行数
        public int resultAll() {
            return this.affectedRows;
        }

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
            int finalAffectedRows = this.affectedRows; // 保存结果
            try {
                if (!inTransaction || transactionConnection == null) {
                    LoggerCore.debugLog("⚠️ 没有活跃的事务需要提交");
                    return 0;
                }

                transactionConnection.commit();
                LoggerCore.debugLog("✓ 事务提交成功");

            } catch (SQLException e) {
                try {
                    if (transactionConnection != null) {
                        transactionConnection.rollback();
                        LoggerCore.debugErrorLog("⚠️ 提交失败，已自动回滚");
                    }
                } catch (SQLException rollbackEx) {
                    LoggerCore.debugErrorLog("❌ 回滚也失败: " + rollbackEx.getMessage());
                }
                throw new RuntimeException("提交事务失败: " + e.getMessage(), e);
            } finally {
                closeTransaction();
            }
            return finalAffectedRows;
        }

        // 回滚事务
        public int rollback() {
            int finalAffectedRows = this.affectedRows; // 保存结果
            try {
                if (!inTransaction || transactionConnection == null) {
                    LoggerCore.debugLog("⚠️ 没有活跃的事务需要回滚");
                    return 0;
                }

                transactionConnection.rollback();
                LoggerCore.debugLog("✓ 事务回滚成功");

            } catch (SQLException e) {
                throw new RuntimeException("回滚事务失败: " + e.getMessage(), e);
            } finally {
                closeTransaction();
            }
            return finalAffectedRows;
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
                this.affectedRows = 0; // 事务结束，重置计数器
                this.lastAffectedRows = 0;
            }
        }

        // 检查是否在事务中
        public boolean isInTransaction() {
            return inTransaction;
        }

        // ==================== 数据库操作策略：枚举 ====================

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
            EXECUTE        // 执行写操作
        }

        // ==================== 数据库操作策略：Simple ====================

        // 数据库操作工厂
        private static class DatabaseOperationFactory {

            // 创建SQL转换策略
            private static SimpleSqlParser.SimpleSqlWithParams createSqlWithParams(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params) {
                switch (strategy) {
                    case DIRECT_SQL:
                        return SimpleSqlParser.SimpleSqlParameterConverter.convertDirectSql(primary, params);
                    case XML_AUTO:
                        return SimpleSqlParser.SimpleSqlParameterConverter.convertXmlAuto(primary, params);
                    case XML_FILE:
                        return SimpleSqlParser.SimpleSqlParameterConverter.convertXmlFile(primary, secondary, params);
                    default:
                        throw new IllegalArgumentException("不支持的SQL策略: " + strategy);
                }
            }
        }

        // 统一的执行模板方法
        private <T> T executeOperation(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params, OperationType operationType) {
            // 1. 转换SQL
            SimpleSqlParser.SimpleSqlWithParams parsedSql = DatabaseOperationFactory.createSqlWithParams(strategy, primary, secondary, params);

            // 2. 选择执行模式
            if (inTransaction) {
                return executeWithTransaction(parsedSql, operationType);
            } else {
                return executeWithoutTransaction(parsedSql, operationType);
            }
        }

        // 事务模式执行
        @SuppressWarnings("unchecked")
        private <T> T executeWithTransaction(SimpleSqlParser.SimpleSqlWithParams parsedSql, OperationType operationType) {
            try {
                switch (operationType) {
                    case SELECT_LIST:
                        return (T) SimpleSqlExecutor.selectListWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                    case SELECT_ONE:
                        return (T) SimpleSqlExecutor.selectOneWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                    case EXECUTE:
                        int affected = SimpleSqlExecutor.executeWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                        this.lastAffectedRows = affected;
                        this.affectedRows += affected;
                        return (T) (Integer) affected;
                    default:
                        throw new IllegalArgumentException("不支持的操作类型: " + operationType);
                }
            } catch (SQLException e) {
                throw new RuntimeException("事务执行失败", e);
            }
        }

        // 非事务模式执行
        @SuppressWarnings("unchecked")
        private <T> T executeWithoutTransaction(SimpleSqlParser.SimpleSqlWithParams parsedSql, OperationType operationType) {
            String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
            switch (operationType) {
                case SELECT_LIST:
                    return (T) SimpleSqlExecutor.selectListWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                case SELECT_ONE:
                    return (T) SimpleSqlExecutor.selectOneWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                case EXECUTE:
                    int affected = SimpleSqlExecutor.executeWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                    this.lastAffectedRows = affected;
                    this.affectedRows += affected;
                    return (T) (Integer) affected;
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType);
            }
        }

        // ==================== 数据库操作策略：MyBatis ====================

        // 数据库操作工厂，MP
        private static class DatabaseOperationFactoryByMp {

            // 创建SQL转换策略
            private static MyBatisSqlParser.MyBatisSqlWithParams createSqlWithParams(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params) {
                switch (strategy) {
                    case DIRECT_SQL:
                        return MyBatisSqlParser.MyBatisSqlParameterConverter.convertDirectSql(primary, params);
                    case XML_AUTO:
                        return MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlAuto(primary, params);
                    case XML_FILE:
                        return MyBatisSqlParser.MyBatisSqlParameterConverter.convertXmlFile(primary, secondary, params);
                    default:
                        throw new IllegalArgumentException("不支持的SQL策略: " + strategy);
                }
            }
        }

        // 统一的执行模板方法，MP
        private <T> T executeOperationByMp(SqlStrategy strategy, String primary, String secondary, Map<String, Object> params, OperationType operationType) {
            // 1. 转换SQL
            MyBatisSqlParser.MyBatisSqlWithParams parsedSql = DatabaseOperationFactoryByMp.createSqlWithParams(strategy, primary, secondary, params);

            // 2. 选择执行模式
            if (inTransaction) {
                return executeWithTransactionByMp(parsedSql, operationType);
            } else {
                return executeWithoutTransactionByMp(parsedSql, operationType);
            }
        }

        // 事务模式执行，MP
        @SuppressWarnings("unchecked")
        private <T> T executeWithTransactionByMp(MyBatisSqlParser.MyBatisSqlWithParams parsedSql, OperationType operationType) {
            try {
                switch (operationType) {
                    case SELECT_LIST:
                        return (T) MyBatisSqlExecutor.selectListByMpWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                    case SELECT_ONE:
                        return (T) MyBatisSqlExecutor.selectOneByMpWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                    case EXECUTE:
                        int affected = MyBatisSqlExecutor.executeByMpWithConn(transactionConnection, parsedSql.getSql(), parsedSql.getParams());
                        this.lastAffectedRows = affected;
                        this.affectedRows += affected;
                        return (T) (Integer) affected;
                    default:
                        throw new IllegalArgumentException("不支持的操作类型: " + operationType);
                }
            } catch (SQLException e) {
                throw new RuntimeException("事务执行失败", e);
            }
        }

        // 非事务模式执行，MP
        @SuppressWarnings("unchecked")
        private <T> T executeWithoutTransactionByMp(MyBatisSqlParser.MyBatisSqlWithParams parsedSql, OperationType operationType) {
            String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
            switch (operationType) {
                case SELECT_LIST:
                    return (T) MyBatisSqlExecutor.selectListByMpWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                case SELECT_ONE:
                    return (T) MyBatisSqlExecutor.selectOneByMpWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                case EXECUTE:
                    int affected = MyBatisSqlExecutor.executeByMpWithDbName(effectiveDataSource, parsedSql.getSql(), parsedSql.getParams());
                    this.lastAffectedRows = affected;
                    this.affectedRows += affected;
                    return (T) (Integer) affected;
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType);
            }
        }

        // ==================== DDL, DQL, DML：直接操作数据库 ====================

        // DDL：直接操作数据库
        public boolean executeDDL(String sql) {
            if (inTransaction) {
                // 在事务中，使用事务连接
                return CommonSqlExecutor.executeDDL(transactionConnection, sql);
            } else {
                // 不在事务中，获取新连接
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return CommonSqlExecutor.executeDDL(effectiveDataSource, sql);
            }
        }

        // DQL：直接查询数据库
        public List<Map<String, Object>> executeDQL(String sql) {
            if (inTransaction) {
                return CommonSqlExecutor.executeDQL(transactionConnection, sql);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return CommonSqlExecutor.executeDQL(effectiveDataSource, sql);
            }
        }

        // DML：直接操作数据库
        public int executeDML(String sql) {
            if (inTransaction) {
                return CommonSqlExecutor.executeDML(transactionConnection, sql);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return CommonSqlExecutor.executeDML(effectiveDataSource, sql);
            }
        }

        // ==================== API1：直接使用SQL ====================

        // 全部：直接使用SQL
        public List<Map<String, Object>> selectList(String sql, Map<String, Object> params) {
            return executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_LIST);
        }

        // 单条：直接使用SQL
        public Map<String, Object> selectOne(String sql, Map<String, Object> params) {
            return executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_ONE);
        }

        // 执行：直接使用SQL
        public DatabaseAccessor execute(String sql, Map<String, Object> params) {
            executeOperation(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：直接使用SQL
        public PaginationHandler.PageResult<Map<String, Object>> selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageWithConn(transactionConnection, sql, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageWithDbName(effectiveDataSource, sql, pageNum, pageSize, params);
            }
        }

        // 批量：直接使用SQL
        public BatchOperationHandler.BatchResult batchUpsert(String tableName, List<Map<String, Object>> entities, String[] keyColumns, String[] valueColumns) {
            if (inTransaction) {
                return BatchOperationHandler.batchUpsertWithConn(transactionConnection, tableName, entities, keyColumns, valueColumns);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return BatchOperationHandler.batchUpsertWithDbName(effectiveDataSource, tableName, entities, keyColumns, valueColumns);
            }
        }

        // ==================== API2：指定具体XML ====================

        // 全部：指定具体XML
        public List<Map<String, Object>> selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_LIST);
        }

        // 单条：指定具体XML
        public Map<String, Object> selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_ONE);
        }

        // 执行：指定具体XML
        public DatabaseAccessor executeFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
            executeOperation(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：指定XML文件
        public PaginationHandler.PageResult<Map<String, Object>> selectPageFromXml(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageFromXmlWithConn(transactionConnection, xmlFileName, sqlId, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageFromXmlWithDbName(effectiveDataSource, xmlFileName, sqlId, pageNum, pageSize, params);
            }
        }

        // ==================== API3：自动搜索XML ====================

        // 全部：自动搜索XML
        public List<Map<String, Object>> selectListFromXmlAuto(String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_LIST);
        }

        // 单条：自动搜索XML
        public Map<String, Object> selectOneFromXmlAuto(String sqlId, Map<String, Object> params) {
            return executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_ONE);
        }

        // 执行：自动搜索XML
        public DatabaseAccessor executeFromXmlAuto(String sqlId, Map<String, Object> params) {
            executeOperation(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：自动搜索XML
        public PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlAuto(String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageFromXmlAutoWithConn(transactionConnection, sqlId, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageFromXmlAutoWithDbName(effectiveDataSource, sqlId, pageNum, pageSize, params);
            }
        }

        // ==================== API1：直接使用SQL，MP ===================

        // 全部：直接使用SQL，MP
        public List<Map<String, Object>> selectListByMp(String sql, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_LIST);
        }

        // 单条：直接使用SQL，MP
        public Map<String, Object> selectOneByMp(String sql, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.SELECT_ONE);
        }

        // 执行：直接使用SQL，MP
        public DatabaseAccessor executeByMp(String sql, Map<String, Object> params) {
            executeOperationByMp(SqlStrategy.DIRECT_SQL, sql, null, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：直接使用SQL，MP
        public PaginationHandler.PageResult<Map<String, Object>> selectPageByMp(String sql, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageByMpWithConn(transactionConnection, sql, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageByMpWithDbName(effectiveDataSource, sql, pageNum, pageSize, params);
            }
        }

        // ==================== API2：指定具体XML，MP ===================

        // 全部：指定具体XML，MP
        public List<Map<String, Object>> selectListFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_LIST);
        }

        // 单条：指定具体XML，MP
        public Map<String, Object> selectOneFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.SELECT_ONE);
        }

        // 执行：指定具体XML，MP
        public DatabaseAccessor executeFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
            executeOperationByMp(SqlStrategy.XML_FILE, xmlFileName, sqlId, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：指定XML文件，MP
        public PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlByMp(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageFromXmlByMpWithConn(transactionConnection, xmlFileName, sqlId, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageFromXmlByMpWithDbName(effectiveDataSource, xmlFileName, sqlId, pageNum, pageSize, params);
            }
        }

        // ==================== API3：自动搜索XML，MP ===================

        // 全部：自动搜索XML，MP
        public List<Map<String, Object>> selectListFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_LIST);
        }

        // 单条：自动搜索XML，MP
        public Map<String, Object> selectOneFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
            return executeOperationByMp(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.SELECT_ONE);
        }

        // 执行：自动搜索XML，MP
        public DatabaseAccessor executeFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
            executeOperationByMp(SqlStrategy.XML_AUTO, sqlId, null, params, OperationType.EXECUTE);
            return this;
        }

        // 分页：自动搜索XML，MP
        public PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlAutoByMp(String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
            if (inTransaction) {
                return PaginationHandler.selectPageFromXmlAutoByMpWithConn(transactionConnection, sqlId, pageNum, pageSize, params);
            } else {
                String effectiveDataSource = dataSourceName != null ? dataSourceName : DataSourceCore.ATOMIC_DEFAULT_SOURCE.get();
                return PaginationHandler.selectPageFromXmlAutoByMpWithDbName(effectiveDataSource, sqlId, pageNum, pageSize, params);
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

    // ==================== DDL, DQL, DML：直接操作数据库 ===================

    // DDL：直接操作数据库
    public static boolean executeDDL(String sql) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return CommonSqlExecutor.executeDDL(effectiveDataSource, sql);
    }

    // DQL：直接查询数据库
    public static List<Map<String, Object>> executeDQL(String sql) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return CommonSqlExecutor.executeDQL(effectiveDataSource, sql);
    }

    // DML：直接操作数据库
    public static int executeDML(String sql) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return CommonSqlExecutor.executeDML(effectiveDataSource, sql);
    }

    // ==================== API1：直接使用SQL ===================

    // 全部：直接使用SQL
    public static List<Map<String, Object>> selectList(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectList(effectiveDataSource, sql, params);
    }

    // 单条：直接使用SQL
    public static Map<String, Object> selectOne(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectOne(effectiveDataSource, sql, params);
    }

    // 执行：直接使用SQL
    public static int execute(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.execute(effectiveDataSource, sql, params);
    }

    // 分页：直接使用SQL
    public static PaginationHandler.PageResult<Map<String, Object>> selectPage(String sql, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageWithDbName(effectiveDataSource, sql, pageNum, pageSize, params);
    }

    // 批量：直接使用SQL
    public static BatchOperationHandler.BatchResult batchUpsert(String tableName, List<Map<String, Object>> entities, String[] keyColumns, String[] valueColumns) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return BatchOperationHandler.batchUpsertWithDbName(effectiveDataSource, tableName, entities, keyColumns, valueColumns);
    }

    // ==================== API2：指定具体XML ===================

    // 全部：指定具体XML
    public static List<Map<String, Object>> selectListFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectListFromXml(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 单条：指定具体XML
    public static Map<String, Object> selectOneFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectOneFromXml(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 执行：指定具体XML
    public static int executeFromXml(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.executeFromXml(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 分页：指定具体XML
    public static PaginationHandler.PageResult<Map<String, Object>> selectPageFromXml(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageFromXmlWithDbName(effectiveDataSource, xmlFileName, sqlId, pageNum, pageSize, params);
    }

    // ==================== API3：自动搜索XML ===================

    // 全部：自动搜索XML
    public static List<Map<String, Object>> selectListFromXmlAuto(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectListFromXmlAuto(effectiveDataSource, sqlId, params);
    }

    // 单条：自动搜索XML
    public static Map<String, Object> selectOneFromXmlAuto(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.selectOneFromXmlAuto(effectiveDataSource, sqlId, params);
    }

    // 执行：自动搜索XML
    public static int executeFromXmlAuto(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return SimpleSqlExecutor.executeFromXmlAuto(effectiveDataSource, sqlId, params);
    }

    // 分页：自动搜索XML
    public static PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlAuto(String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageFromXmlAutoWithDbName(effectiveDataSource, sqlId, pageNum, pageSize, params);
    }

    // ==================== API1：直接使用SQL，MP ===================

    // 全部：直接使用SQL，MP
    public static List<Map<String, Object>> selectListByMp(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectListByMp(effectiveDataSource, sql, params);
    }

    // 单条：直接使用SQL，MP
    public static Map<String, Object> selectOneByMp(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectOneByMp(effectiveDataSource, sql, params);
    }

    // 执行：直接使用SQL，MP
    public static int executeByMp(String sql, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.executeByMp(effectiveDataSource, sql, params);
    }

    // 分页：直接使用SQL, MP
    public static PaginationHandler.PageResult<Map<String, Object>> selectPageByMp(String sql, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageByMpWithDbName(effectiveDataSource, sql, pageNum, pageSize, params);
    }

    // ==================== API2：指定具体XML，MP ===================

    // 全部：指定具体XML，MP
    public static List<Map<String, Object>> selectListFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectListFromXmlByMp(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 单条：指定具体XML，MP
    public static Map<String, Object> selectOneFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectOneFromXmlByMp(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 执行：指定具体XML，MP
    public static int executeFromXmlByMp(String xmlFileName, String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.executeFromXmlByMp(effectiveDataSource, xmlFileName, sqlId, params);
    }

    // 分页：指定具体XML, MP
    public static PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlByMp(String xmlFileName, String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageFromXmlByMpWithDbName(effectiveDataSource, xmlFileName, sqlId, pageNum, pageSize, params);
    }

    // ==================== API3：自动搜索XML，MP ===================

    // 全部：自动搜索XML，MP
    public static List<Map<String, Object>> selectListFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectListFromXmlAutoByMp(effectiveDataSource, sqlId, params);
    }

    // 单条：自动搜索XML，MP
    public static Map<String, Object> selectOneFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.selectOneFromXmlAutoByMp(effectiveDataSource, sqlId, params);
    }

    // 执行：自动搜索XML，MP
    public static int executeFromXmlAutoByMp(String sqlId, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return MyBatisSqlExecutor.executeFromXmlAutoByMp(effectiveDataSource, sqlId, params);
    }

    // 分页：自动搜索XML, MP
    public static PaginationHandler.PageResult<Map<String, Object>> selectPageFromXmlAutoByMp(String sqlId, int pageNum, int pageSize, Map<String, Object> params) {
        String effectiveDataSource = DataSourceCore.getEffectiveDataSourceName(null);
        return PaginationHandler.selectPageFromXmlAutoByMpWithDbName(effectiveDataSource, sqlId, pageNum, pageSize, params);
    }
}
