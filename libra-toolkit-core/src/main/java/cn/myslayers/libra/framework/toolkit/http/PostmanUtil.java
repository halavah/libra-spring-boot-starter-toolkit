package cn.myslayers.libra.framework.toolkit.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostmanUtil - 一个轻量级的Java后端API模拟和测试工具:
 * 1. 嵌入式HTTP服务器：基于Java内置HttpServer，无需外部容器。
 * 2. 注解驱动的路由：使用 @PostmanController 和 @PostmanMapping 注解快速定义API接口。
 * 3. 自动扫描与注册：通过包扫描或手动注册，自动发现并加载Controller。
 * 4. 灵活的日志系统：可独立控制调试日志、启动日志和详细的请求日志。
 * 5. 统一的响应格式：内置Result<T>类，标准化API的成功和错误响应。
 * 6. 可插拔的认证策略：支持Bearer Token和HMAC签名等多种认证方式，并可轻松扩展。
 * 7. 详细的请求追踪：自动为每个请求生成curl命令，方便复现和调试。
 */
public class PostmanUtil {

    // ==================== 全局核心配置 ====================

    // 服务器监听端口
    private static int PORT = 5176;
    // 是否开启核心调试日志
    private static volatile boolean DEBUG_LOG_ENABLED = true;
    // 是否在启动时打印已注册的接口列表
    private static volatile boolean STARTUP_LOG_ENABLED = true;
    // 是否开启请求详情日志（包括curl命令、出入参等）
    private static volatile boolean REQUEST_DETAIL_LOG_ENABLED = true;

    // 设置服务器端口
    public static void setPort(int port) {
        PORT = port;
    }

    // 设置是否开启调试日志
    public static void setDebugLogEnabled(boolean enabled) {
        DEBUG_LOG_ENABLED = enabled;
    }

    // 设置是否开启启动日志
    public static void setStartupLogEnabled(boolean enabled) {
        STARTUP_LOG_ENABLED = enabled;
    }

    // 设置是否开启请求详情日志
    public static void setRequestDetailLogEnabled(boolean enabled) {
        REQUEST_DETAIL_LOG_ENABLED = enabled;
    }

    // 私有构造函数，防止工具类被实例化
    private PostmanUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 枚举与注解定义 ====================

    // HTTP请求方法枚举
    public enum RequestMethod {GET, POST, PUT, DELETE, PATCH, OPTIONS}

    // Controller注解，用于标记一个类为控制器
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface PostmanController {

        // Controller的顶层路径
        String value() default "/";
    }

    // 接口映射注解，用于标记一个方法为HTTP接口
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PostmanMapping {

        // 接口的路径
        String value();

        // HTTP请求方法，默认为GET
        RequestMethod method() default RequestMethod.GET;
    }

    // 认证注解，用于标记需要认证的接口
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RequiresAuth {

        // 指定一个或多个认证策略
        Class<? extends AuthManager.Strategy>[] value();
    }

    // ==================== 核心内部类 ====================

    // 日志核心类，负责日志的记录
    public static class LoggerCore {

        // slf4j日志记录器
        private static final Logger logger = LoggerFactory.getLogger(LoggerCore.class);

        // 记录日志
        public static void log(String level, String message) {
            if (PostmanUtil.DEBUG_LOG_ENABLED) {
                String logMessage = String.format("[%s] %s %s", level, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), message);
                if ("ERROR".equals(level)) {
                    logger.error(logMessage);
                } else if ("WARN".equals(level)) {
                    logger.warn(logMessage);
                } else {
                    logger.info(logMessage);
                }
            }
        }
    }

    // HTTP服务器核心类
    public static class HttpCore {

        // 内置的HttpServer实例
        private static HttpServer server;
        // Jackson ObjectMapper，用于JSON序列化和反序列化
        private static final ObjectMapper objectMapper = new ObjectMapper();

        // 启动服务器
        public static void startServer(Class<?>... targetClasses) {
            try {
                // 强制关闭可能占用端口的进程
                forceClosePort(PostmanUtil.PORT);
                // 在启动前，先尝试停止任何可能已在运行的服务器实例
                if (server != null) {
                    stopServer();
                }
                // 扫描并注册处理器
                HandlerCore.scanAndRegisterHandlers(targetClasses);
                // 创建服务器实例
                server = HttpServer.create(new InetSocketAddress(PostmanUtil.PORT), 0);
                // 创建根上下文，所有请求都由RouteHandler处理
                server.createContext("/", new RouteHandler());
                // 设置线程池
                server.setExecutor(Executors.newFixedThreadPool(10));
                // 启动服务器
                server.start();
                LoggerCore.log("INFO", "Postman测试服务器启动成功，访问地址: http://127.0.0.1:" + PostmanUtil.PORT);
                // 打印启动日志
                printStartupLog();
            } catch (Exception e) {
                LoggerCore.log("ERROR", "启动Postman测试服务器失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 停止服务器
        public static void stopServer() {
            if (server != null) {
                server.stop(0);
                LoggerCore.log("INFO", "Postman测试服务器已停止");
                server = null;
            }
        }

        // 强制关闭占用指定端口的进程
        private static void forceClosePort(int port) {
            // 获取当前Java进程的PID
            String currentPid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            LoggerCore.log("INFO", "当前进程PID: " + currentPid);

            String os = System.getProperty("os.name").toLowerCase();
            String command;
            List<String> pidsToKill = new ArrayList<>();

            try {
                if (os.contains("win")) {
                    // Windows
                    command = "netstat -ano | findstr :" + port;
                    Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
                    try (Scanner scanner = new Scanner(process.getInputStream())) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            String[] parts = line.trim().split("\\s+");
                            if (parts.length > 0) {
                                String foundPid = parts[parts.length - 1];
                                pidsToKill.add(foundPid);
                            }
                        }
                    }
                } else {
                    // macOS / Linux
                    command = "lsof -t -i:" + port;
                    Process process = Runtime.getRuntime().exec(command);
                    try (Scanner scanner = new Scanner(process.getInputStream())) {
                        while (scanner.hasNext()) {
                            pidsToKill.add(scanner.next());
                        }
                    }
                }

                for (String pid : pidsToKill) {
                    if (pid.equals(currentPid)) {
                        LoggerCore.log("WARN", "找到的进程PID (" + pid + ") 与当前进程PID相同，跳过关闭操作。");
                        continue;
                    }

                    LoggerCore.log("INFO", "找到占用端口 " + port + " 的外部进程PID: " + pid + "，正在强制关闭...");
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "taskkill /F /PID " + pid}).waitFor();
                    } else {
                        Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
                    }
                }

                if (!pidsToKill.isEmpty()) {
                    // 等待一小段时间确保端口已释放
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                LoggerCore.log("ERROR", "强制关闭端口 " + port + " 失败: " + e.getMessage());
            }
        }

        // 打印启动时注册的接口信息
        private static void printStartupLog() {
            if (!PostmanUtil.STARTUP_LOG_ENABLED) {
                return;
            }
            System.out.println("--- PostmanUtil 已注册接口 ---");
            if (HandlerCore.getHandlerMethods().isEmpty()) {
                System.out.println("(暂无)");
            } else {
                for (HandlerCore.HandlerMethod handler : HandlerCore.getHandlerMethods().values()) {
                    System.out.println(handler.getRegistrationLog());
                }
            }
            System.out.println("------------------------------");
        }

        // 路由处理器，负责分发所有进入的HTTP请求
        static class RouteHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                long startTime = System.currentTimeMillis();
                String requestBody = "";
                Map<String, String> params = new HashMap<>();
                Object responseData = null;
                HandlerCore.HandlerMethod handlerMethod = null;
                try {
                    // 在请求开始时，将exchange存入上下文，以便在任何地方访问
                    RequestContextHolder.setHttpExchange(exchange);

                    // 根据请求方法，设置上下文
                    String requestMethod = exchange.getRequestMethod().toUpperCase();
                    if ("GET".equalsIgnoreCase(requestMethod) || "DELETE".equalsIgnoreCase(requestMethod) || "OPTIONS".equalsIgnoreCase(requestMethod)) {
                        params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                        RequestContextHolder.setQueryParams(params);
                    } else if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod) || "PATCH".equalsIgnoreCase(requestMethod)) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                            requestBody = reader.lines().collect(Collectors.joining("\n"));
                            RequestContextHolder.setRequestBody(requestBody);
                        }
                    }

                    String path = exchange.getRequestURI().getPath();
                    // 构造路由键
                    String routeKey = requestMethod + ":" + path;
                    handlerMethod = HandlerCore.getHandler(routeKey);

                    if (handlerMethod != null) {
                        // 执行认证
                        if (!handlerMethod.authenticate(exchange)) {
                            sendErrorResponse(exchange, 401, "Unauthorized");
                            return;
                        }
                        // 调用目标方法
                        responseData = handlerMethod.invoke(); // 不再传递参数

                        // 核心改造：检查返回类型
                        if (handlerMethod.method.getReturnType() != void.class) {
                            // 如果不是void，才执行JSON包装逻辑
                            if (responseData != null && (responseData instanceof Result || responseData.getClass().getSimpleName().contains("Result"))) {
                                // 如果返回的已经是任何形式的 "Result" 对象，直接序列化
                                sendJsonResponse(exchange, objectMapper.writeValueAsString(responseData));
                            } else {
                                // 对于非 Result 类型的原始数据，保持原有的包装逻辑
                                Result<Object> result = Result.ok(responseData);
                                sendJsonResponse(exchange, objectMapper.writeValueAsString(result));
                            }
                        }
                        // 如果是void，则不执行任何操作，假设响应已在方法内处理
                    } else {
                        // 提供一个默认的欢迎页面
                        if ("/".equals(path)) {
                            String welcomeMessage = "PostmanUtil Server is running.";
                            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                            exchange.sendResponseHeaders(200, welcomeMessage.getBytes(StandardCharsets.UTF_8).length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(welcomeMessage.getBytes(StandardCharsets.UTF_8));
                            }
                        } else {
                            sendErrorResponse(exchange, 404, "接口未找到: " + requestMethod + " " + exchange.getRequestURI().toString());
                        }
                    }
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "处理请求失败: " + e.getMessage());
                    e.printStackTrace();
                    sendErrorResponse(exchange, 500, "处理请求失败: " + e.getMessage());
                } finally {
                    // 打印请求日志
                    if (PostmanUtil.REQUEST_DETAIL_LOG_ENABLED && handlerMethod != null) {
                        // 从ThreadLocal获取参数用于日志记录
                        Map<String, String> logParams = RequestContextHolder.getQueryParams();
                        String logBody = RequestContextHolder.getRequestBody();
                        printRequestLog(exchange, logParams, logBody, responseData, handlerMethod, System.currentTimeMillis() - startTime);
                    }
                    // 清理上下文
                    RequestContextHolder.clear();
                }
            }
        }

        // 打印详细的请求日志
        private static void printRequestLog(HttpExchange exchange, Map<String, String> params, String requestBody, Object responseData, HandlerCore.HandlerMethod handler, long duration) {
            try {
                System.out.println("\n==================== 请求处理 ====================");
                if (handler != null) {
                    System.out.println("处理方法: " + handler.controller.getClass().getSimpleName() + " -> " + handler.method.getName());
                }
                System.out.println("请求链接: " + exchange.getRequestURI());
                System.out.println("请求类型: " + exchange.getRequestMethod());
                System.out.println("请求头: " + exchange.getRequestHeaders().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));
                StringBuilder paramsBuilder = new StringBuilder();
                if (params != null && !params.isEmpty()) {
                    paramsBuilder.append(params.toString());
                }
                if (requestBody != null && !requestBody.isEmpty()) {
                    if (paramsBuilder.length() > 0) {
                        paramsBuilder.append("; ");
                    }
                    paramsBuilder.append(requestBody.replaceAll("\\s+", " "));
                }
                if (paramsBuilder.length() > 0) {
                    System.out.println("入参: " + paramsBuilder.toString());
                }
                String responseStr = (responseData != null ? objectMapper.writeValueAsString(responseData) : "N/A");
                System.out.println("出参: " + responseStr.replaceAll("\\s+", " "));
                System.out.println(generateCurlCommand(exchange, requestBody));
                System.out.println("耗时: " + duration + "ms");
                System.out.println("==================== 请求结束 =====================");
            } catch (Exception e) {
                // 日志打印异常，忽略
            }
        }

        // 生成等效的curl命令
        private static String generateCurlCommand(HttpExchange exchange, String requestBody) {
            StringBuilder curl = new StringBuilder("curl");
            curl.append(" -X ").append(exchange.getRequestMethod());
            String fullUrl = "http://" + exchange.getLocalAddress().getHostName() + ":" + exchange.getLocalAddress().getPort() + exchange.getRequestURI().toString();
            curl.append(" '").append(fullUrl).append("'");
            for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
                if (!header.getKey().equalsIgnoreCase("Host") && !header.getKey().equalsIgnoreCase("Content-Length")) {
                    for (String value : header.getValue()) {
                        curl.append(" -H '").append(header.getKey()).append(": ").append(value.replace("'", "\\'")).append("'");
                    }
                }
            }
            if (requestBody != null && !requestBody.isEmpty()) {
                String singleLineBody = requestBody.replaceAll("\\s+", " ");
                curl.append(" -d '").append(singleLineBody.replace("'", "\\'")).append("'");
            }
            return curl.toString();
        }

        // 解析URL查询参数
        private static Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                Arrays.stream(query.split("&")).forEach(pair -> {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        try {
                            params.put(URLDecoder.decode(keyValue[0], "UTF-8"), URLDecoder.decode(keyValue[1], "UTF-8"));
                        } catch (Exception e) {
                            // 参数解析失败，忽略
                        }
                    }
                });
            }
            return params;
        }

        // 发送JSON格式的成功响应
        private static void sendJsonResponse(HttpExchange exchange, String jsonResponse) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // 允许跨域
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        // 发送JSON格式的错误响应
        private static void sendErrorResponse(HttpExchange exchange, int code, String errorMessage) throws IOException {
            Result<Object> result = Result.error(code, errorMessage);
            String jsonError = objectMapper.writeValueAsString(result);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // 允许跨域
            byte[] responseBytes = jsonError.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    // 处理器核心类，负责扫描、注册和管理所有接口处理器
    public static class HandlerCore {

        // 存储所有已注册的处理器方法
        private static final Map<String, HandlerMethod> handlerMethods = new HashMap<>();

        // 扫描并注册处理器
        public static void scanAndRegisterHandlers(Class<?>... targetClasses) {
            handlerMethods.clear();
            Set<Class<?>> controllerClasses = new HashSet<>();

            // 1. 添加手动指定的类
            if (targetClasses != null && targetClasses.length > 0) {
                controllerClasses.addAll(Arrays.asList(targetClasses));
            }

            // 2. 添加手动注册的类
            controllerClasses.addAll(ManualRegistry.getRegisteredClasses());

            // 3. 执行包扫描并添加结果
            List<String> packagesToScan = ManualRegistry.getScanPackages();
            if (!packagesToScan.isEmpty()) {
                try {
                    Reflections reflections = new Reflections(packagesToScan.toArray(new String[0]));
                    Set<Class<?>> scannedClasses = reflections.getTypesAnnotatedWith(PostmanController.class);
                    if (scannedClasses.isEmpty()) {
                        LoggerCore.log("WARN", "在包 " + packagesToScan + " 中未扫描到任何 @PostmanController 注解的类");
                    } else {
                        controllerClasses.addAll(scannedClasses);
                    }
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "Reflections包扫描失败: " + e.getMessage());
                }
            }

            // 如果没有任何指定的类或包，可以提供一个默认扫描路径作为后备
            if (controllerClasses.isEmpty() && ManualRegistry.getScanPackages().isEmpty()) {
                LoggerCore.log("INFO", "未指定Controller或扫描包，将扫描默认包 'cn.myslayers'");
                Reflections reflections = new Reflections("cn.myslayers");
                controllerClasses.addAll(reflections.getTypesAnnotatedWith(PostmanController.class));
            }

            for (Class<?> controllerClass : controllerClasses) {
                try {
                    PostmanController controllerAnnotation = controllerClass.getAnnotation(PostmanController.class);
                    String basePath = controllerAnnotation.value();
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                    Map<String, Integer> methodLineNumbers = parseSourceForMethodLines(controllerClass);

                    for (Method method : controllerClass.getMethods()) {
                        if (method.isAnnotationPresent(PostmanMapping.class)) {
                            PostmanMapping mapping = method.getAnnotation(PostmanMapping.class);
                            String methodPath = mapping.value();
                            // 规范化并组合路径
                            String fullPath = (basePath + "/" + methodPath).replaceAll("/+", "/");
                            if (fullPath.length() > 1 && fullPath.endsWith("/")) {
                                fullPath = fullPath.substring(0, fullPath.length() - 1);
                            }

                            String routeKey = mapping.method().name() + ":" + fullPath;
                            int line = methodLineNumbers.getOrDefault(method.getName(), 0);
                            handlerMethods.put(routeKey, new HandlerMethod(controllerInstance, method, line, fullPath));
                        }
                    }
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "注册控制器失败 " + controllerClass.getName() + ": " + e.getMessage());
                }
            }
        }

        // 解析源文件以获取方法的行号
        private static Map<String, Integer> parseSourceForMethodLines(Class<?> clazz) {
            Map<String, Integer> methodLines = new HashMap<>();
            String sourcePath = getSourceFilePath(clazz);
            if (sourcePath == null) {
                return methodLines;
            }
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                return methodLines;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    for (Method method : clazz.getMethods()) {
                        if (line.contains(" " + method.getName() + "(") && !methodLines.containsKey(method.getName())) {
                            methodLines.put(method.getName(), lineNumber);
                        }
                    }
                }
            } catch (IOException e) {
                LoggerCore.log("WARN", "读取源文件失败: " + sourcePath);
            }
            return methodLines;
        }

        // 获取类的源文件路径
        private static String getSourceFilePath(Class<?> clazz) {
            try {
                URI classUri = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
                String classPath = new File(classUri).getAbsolutePath();
                String sourceRoot;
                if (classPath.contains("target/test-classes")) {
                    sourceRoot = classPath.replace("target/test-classes", "src/test/java");
                } else {
                    sourceRoot = classPath.replace("target/classes", "src/main/java");
                }
                return new File(sourceRoot, clazz.getName().replace('.', '/') + ".java").getAbsolutePath();
            } catch (Exception e) {
                return null;
            }
        }

        // 根据路由键获取处理器方法
        public static HandlerMethod getHandler(String routeKey) {
            return handlerMethods.get(routeKey);
        }

        // 获取所有处理器方法
        public static Map<String, HandlerMethod> getHandlerMethods() {
            return handlerMethods;
        }

        // 封装了单个处理器方法的所有信息
        static class HandlerMethod {

            // 控制器实例
            private final Object controller;
            // 目标方法
            private final Method method;
            // 方法在源文件中的行号
            private final int lineNumber;
            // 源文件路径
            private final String sourceFilePath;
            // 完整的请求路径
            private final String fullPath;

            HandlerMethod(Object controller, Method method, int lineNumber, String fullPath) {
                this.controller = controller;
                this.method = method;
                this.lineNumber = lineNumber;
                this.sourceFilePath = getSourceFilePath(controller.getClass());
                this.fullPath = fullPath;
            }

            // 调用目标方法
            public Object invoke() throws Exception {
                // 由于参数通过ThreadLocal传递，这里不再需要处理方法参数
                return method.invoke(controller);
            }

            // 对请求进行认证
            public boolean authenticate(HttpExchange exchange) {
                if (method.isAnnotationPresent(RequiresAuth.class)) {
                    RequiresAuth authAnnotation = method.getAnnotation(RequiresAuth.class);
                    for (Class<? extends AuthManager.Strategy> strategyClass : authAnnotation.value()) {
                        try {
                            AuthManager.Strategy strategy = strategyClass.getDeclaredConstructor().newInstance();
                            if (!strategy.authenticate(exchange)) {
                                return false; // 任何一个策略失败则认证失败
                            }
                        } catch (Exception e) {
                            LoggerCore.log("ERROR", "执行认证策略失败: " + strategyClass.getName());
                            return false;
                        }
                    }
                }
                return true; // 没有认证注解或所有策略都通过
            }

            // 获取源文件链接，用于日志和调试
            public String getSourceLink() {
                return controller.getClass().getSimpleName() + " -> " + method.getName();
            }

            // 获取注册日志信息
            public String getRegistrationLog() {
                PostmanMapping mapping = method.getAnnotation(PostmanMapping.class);
                return String.format("注册%s接口: %s:%s (%s -> %s)", controller.getClass().getSimpleName(), mapping.method().name(), this.fullPath, controller.getClass().getSimpleName(), method.getName());
            }
        }
    }

    // ==================== 扩展内部类 ====================

    // 认证管理器，提供多种认证策略
    public static class AuthManager {

        // 默认的Bearer Token
        private static String validBearerToken = "default-secret-token";
        // 默认的签名密钥
        private static String signatureSecret = "default-signature-secret";

        // 设置Bearer Token
        public static void setBearerToken(String token) {
            validBearerToken = token;
        }

        // 设置签名密钥
        public static void setSignatureSecret(String secret) {
            signatureSecret = secret;
        }

        // 认证策略接口
        public interface Strategy {

            // 认证方法
            boolean authenticate(HttpExchange exchange);
        }

        // Bearer Token认证策略
        // 规则:
        // 1. 客户端需要在HTTP请求头的 `Authorization` 字段中提供一个有效的 `Bearer Token`。
        // 2. 格式为: `Authorization: Bearer <your-token>`
        // 3. 服务器端会提取 `Bearer ` 后面的token部分，并与预设的 `validBearerToken` 进行比对。
        // 4. 如果两者完全匹配，则认证通过。
        public static class BearerTokenStrategy implements Strategy {

            @Override
            public boolean authenticate(HttpExchange exchange) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return AuthManager.validBearerToken.equals(authHeader.substring(7));
                }
                return false;
            }
        }

        // 签名认证策略
        // 规则:
        // 1. 客户端需要在HTTP请求头中包含三个字段:
        //    - X-Timestamp: 当前的Unix时间戳（毫秒）。
        //    - X-Nonce: 一个随机字符串，用于防止重放攻击。
        //    - X-Api-Signature: 根据以下规则生成的签名。
        // 2. 服务器端会校验时间戳，确保请求在一定时间窗口内（默认为5分钟），防止重放。
        // 3. 签名的生成方式如下:
        //    a. 构造一个待签名的字符串 (stringToSign)，格式为:
        //       HTTP请求方法 + "\n" + 请求路径 + "\n" + X-Timestamp的值 + "\n" + X-Nonce的值
        //       例如: "POST\n/api/user/create\n1672531200000\nrandom-string-123"
        //    b. 使用HmacSHA256算法和预共享的密钥 (signatureSecret) 对 stringToSign 进行加密。
        //    c. 将加密后的二进制结果进行Base64编码，得到最终的签名字符串。
        // 4. 服务器会以同样的方式在本地生成签名，并与客户端传来的 X-Api-Signature 进行比对，如果一致则认证通过。
        public static class SignatureStrategy implements Strategy {

            // 时间戳最大差异（毫秒），例如5分钟
            private static final long MAX_TIMESTAMP_DIFF = 300_000;

            @Override
            public boolean authenticate(HttpExchange exchange) {
                String signatureHeader = exchange.getRequestHeaders().getFirst("X-Api-Signature");
                String timestampHeader = exchange.getRequestHeaders().getFirst("X-Timestamp");
                String nonce = exchange.getRequestHeaders().getFirst("X-Nonce");
                if (signatureHeader == null || timestampHeader == null || nonce == null) {
                    return false;
                }
                try {
                    // 校验时间戳
                    if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestampHeader)) > MAX_TIMESTAMP_DIFF) {
                        return false;
                    }
                    // 构造待签名字符串
                    String stringToSign = exchange.getRequestMethod() + "\n" + exchange.getRequestURI().getPath() + "\n" + timestampHeader + "\n" + nonce;
                    // 校验签名
                    return calculateHmacSha256(stringToSign, AuthManager.signatureSecret).equals(signatureHeader);
                } catch (Exception e) {
                    LoggerCore.log("ERROR", "签名认证失败: " + e.getMessage());
                    return false;
                }
            }

            // 计算HmacSHA256签名
            private String calculateHmacSha256(String data, String secret) throws Exception {
                Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                sha256_HMAC.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    // 手动注册器，用于手动添加Controller类或扫描包
    public static class ManualRegistry {

        // 存储手动注册的类
        private static final List<Class<?>> registeredClasses = new ArrayList<>();
        // 存储需要扫描的包
        private static final List<String> scanPackages = new ArrayList<>();

        // 注册一个或多个Controller类
        public static void register(Class<?>... classes) {
            registeredClasses.addAll(Arrays.asList(classes));
        }

        // 添加一个或多个要扫描的包
        public static void addScan(String... packages) {
            scanPackages.addAll(Arrays.asList(packages));
        }

        // 获取所有手动注册的类
        static List<Class<?>> getRegisteredClasses() {
            return registeredClasses;
        }

        // 获取所有要扫描的包
        static List<String> getScanPackages() {
            return scanPackages;
        }
    }

    // ==================== 响应内部类 ====================

    // 请求上下文持有者，使用ThreadLocal为每个请求线程提供独立的数据存储。
    public static class RequestContextHolder {
        private static final ThreadLocal<Map<String, String>> queryParamsThreadLocal = new ThreadLocal<>();
        private static final ThreadLocal<String> requestBodyThreadLocal = new ThreadLocal<>();
        private static final ThreadLocal<HttpExchange> httpExchangeThreadLocal = new ThreadLocal<>();

        public static void setQueryParams(Map<String, String> queryParams) {
            queryParamsThreadLocal.set(queryParams);
        }

        public static Map<String, String> getQueryParams() {
            return queryParamsThreadLocal.get();
        }

        public static void setRequestBody(String requestBody) {
            requestBodyThreadLocal.set(requestBody);
        }

        public static String getRequestBody() {
            return requestBodyThreadLocal.get();
        }

        public static void setHttpExchange(HttpExchange exchange) {
            httpExchangeThreadLocal.set(exchange);
        }

        public static HttpExchange getHttpExchange() {
            return httpExchangeThreadLocal.get();
        }

        public static void clear() {
            queryParamsThreadLocal.remove();
            requestBodyThreadLocal.remove();
            httpExchangeThreadLocal.remove();
        }
    }

    // 可继承的控制器基类，提供便捷的参数获取方法。
    public static abstract class BaseController {
        /**
         * 获取 URL 查询参数 (适用于 GET, DELETE 等方法)。
         * @return 包含所有查询参数的 Map。
         */
        protected Map<String, String> getParams() {
            return RequestContextHolder.getQueryParams();
        }

        /**
         * 获取并解析请求体 (适用于 POST, PUT, PATCH 等方法)。
         * @param clazz 目标 DTO 的 Class 对象。
         * @param <T>   目标 DTO 的类型。
         * @return 解析后的 DTO 对象。
         */
        protected <T> T getBody(Class<T> clazz) {
            String json = RequestContextHolder.getRequestBody();
            if (json == null || json.isEmpty()) {
                return null;
            }
            // 确保 fastjson 依赖已添加
            return com.alibaba.fastjson.JSON.parseObject(json, clazz);
        }
    }

    // 统一的API响应结果类
    public static class Result<T> implements Serializable {

        // 序列化ID
        private static final long serialVersionUID = 1L;
        // 是否成功
        private boolean success = true;
        // 响应消息
        private String message = "操作成功";
        // 响应码
        private Integer code = 0;
        // 响应数据
        private T result;
        // 时间戳
        private long timestamp = System.currentTimeMillis();

        // 返回成功的响应
        public static <T> Result<T> ok(T data) {
            Result<T> r = new Result<>();
            r.success = true;
            r.code = 200;
            r.result = data;
            return r;
        }

        // 返回失败的响应
        public static <T> Result<T> error(int code, String message) {
            Result<T> r = new Result<>();
            r.success = false;
            r.code = code;
            r.message = message;
            return r;
        }

        // 获取是否成功
        public boolean isSuccess() {
            return success;
        }

        // 获取响应消息
        public String getMessage() {
            return message;
        }

        // 获取响应码
        public Integer getCode() {
            return code;
        }

        // 获取响应数据
        public T getResult() {
            return result;
        }

        // 获取时间戳
        public long getTimestamp() {
            return timestamp;
        }
    }

    // ==================== 默认启动器 ====================

    // 主方法，用于独立启动
    public static void main(String[] args) {
        System.out.println("=== PostmanUtil [全局模式] 启动 ===");

        // 设置端口
        PostmanUtil.setPort(5176);

        // 开启核心调试日志 (服务器启动、扫描等)
        PostmanUtil.setDebugLogEnabled(true);
        // 开启启动时打印的接口列表
        PostmanUtil.setStartupLogEnabled(true);
        // 开启请求详情日志 (curl、出入参等)
        PostmanUtil.setRequestDetailLogEnabled(true);

        // 在全局模式下，明确指定扫描Controller所在的包
        ManualRegistry.addScan("cn.myslayers.app.controller");

        // 启动服务器
        HttpCore.startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            System.out.println("\n正在关闭Postman测试工具...");
            HttpCore.stopServer();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            HttpCore.stopServer();
        }
    }
}
