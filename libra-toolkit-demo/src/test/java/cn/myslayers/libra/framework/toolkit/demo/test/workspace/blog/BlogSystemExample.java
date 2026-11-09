package cn.myslayers.libra.framework.toolkit.demo.test.workspace.blog;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 演示案例：简易博客系统 API
 * <p>
 * 展示如何使用 Toolkit 构建一个包含用户、文章功能的完整服务。
 * 集成了：
 * 1. DatabaseUtil: 数据库操作
 * 2. PostmanUtil: HTTP 接口服务
 * 3. RedissonUtil: 缓存与热度排行
 * </p>
 */
@Slf4j
public class BlogSystemExample {

    // ================== 启动入口 ==================
    public static void main(String[] args) {
        // 1. 初始化基础设施 (模拟环境)
        initInfrastructure();

        // 2. 启动 HTTP 服务器
        PostmanUtil.setPort(8081); // 使用 8081 端口
        PostmanUtil.HttpCore.startServer(
                UserController.class,
                PostController.class
        );

        System.out.println("🚀 Blog System started at http://localhost:8081");
    }

    private static void initInfrastructure() {
        // 初始化数据库 (使用 H2 内存数据库演示)
        DatabaseUtil.DataSourceCore.register("blog_db",
                "jdbc:h2:mem:blog_db;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "sa", "", "org.h2.Driver");
        DatabaseUtil.DataSourceCore.setDefaultDataSource("blog_db");

        // 初始化表结构
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100), " +
                "created_at DATETIME)");

        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS posts (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT, " +
                "title VARCHAR(200), " +
                "content TEXT, " +
                "views INT DEFAULT 0, " +
                "created_at DATETIME)");

        // 初始化 Redis (演示环境下若无 Redis 可注释掉或 mock，此处假设本地有 Redis)
        try {
            RedissonUtil.RedisDataCore.register("blog_redis", "127.0.0.1", 6379, "", 0);
            RedissonUtil.RedisDataCore.setDefaultKey("blog_redis");
            System.out.println("✅ Redis connected");
        } catch (Exception e) {
            System.err.println("⚠️ Redis init failed (ignore if no local redis): " + e.getMessage());
        }
    }

    // Redis Accessor Helper
    private static RedissonUtil.RedisAccessor redis() {
        return RedissonUtil.RedisDataCore.getConfig("blog_redis").accessor();
    }

    // 辅助方法：构建参数 Map
    private static Map<String, Object> mapParams(Object... args) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            map.put("p" + (i + 1), args[i]);
        }
        return map;
    }

    // ================== DTOs ==================
    @Data
    static class UserDTO {
        private String username;
        private String email;
        private Date createdAt; // Added for insert
    }

    @Data
    static class PostDTO {
        private Long userId;
        private String title;
        private String content;
        private Date createdAt; // Added for insert
    }

    // ================== Controllers ==================

    /**
     * 用户服务控制器
     */
    @PostmanController("/api/users")
    public static class UserController extends BaseController {

        @PostmanMapping(value = "/register", method = RequestMethod.POST)
        public Result<Long> register() {
            UserDTO dto = getBody(UserDTO.class);
            dto.setCreatedAt(new Date());

            // 存入数据库
            String sql = "INSERT INTO users (username, email, created_at) VALUES (#{username}, #{email}, #{createdAt})";
            DatabaseUtil.executeByMpEnhanced(sql, dto);

            // H2 获取刚刚插入的 ID (简化演示)
            Map<String, Object> idRes = DatabaseUtil.selectOneByMp("SELECT SCOPE_IDENTITY() as id", null);
            Long id = Long.parseLong(idRes.get("id").toString());

            return Result.ok(id);
        }

        @PostmanMapping(value = "/list", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> list() {
            List<Map<String, Object>> users = DatabaseUtil.selectListByMp("SELECT * FROM users ORDER BY id DESC", null);
            return Result.ok(users);
        }
    }

    /**
     * 文章服务控制器
     */
    @PostmanController("/api/posts")
    public static class PostController extends BaseController {

        @PostmanMapping(value = "/publish", method = RequestMethod.POST)
        public Result<String> publish() {
            PostDTO dto = getBody(PostDTO.class);
            dto.setCreatedAt(new Date());

            // 1. 保存文章
            String sql = "INSERT INTO posts (user_id, title, content, created_at) VALUES (#{userId}, #{title}, #{content}, #{createdAt})";
            DatabaseUtil.executeByMpEnhanced(sql, dto);

            Map<String, Object> idRes = DatabaseUtil.selectOneByMp("SELECT SCOPE_IDENTITY() as id", null);
            Long postId = Long.parseLong(idRes.get("id").toString());

            // 2. 存入 Redis 最新文章列表 (List)
            try {
                String cacheKey = "blog:posts:latest";
                redis().ListOps.lLeftPush(cacheKey, "Post: " + dto.title + " (ID:" + postId + ")");
                redis().ListOps.lTrim(cacheKey, 0, 9); // 只保留最新的 10 条
            } catch (Exception ignored) {}

            return Result.ok("Published successfully, ID: " + postId);
        }

        @PostmanMapping(value = "/{id}", method = RequestMethod.GET)
        public Result<Map<String, Object>> detail() {
            Long id = Long.parseLong(getParams().get("id"));

            // 1. 增加阅读数 (DB)
            DatabaseUtil.executeByMp("UPDATE posts SET views = views + 1 WHERE id = #{p1}", mapParams(id));

            // 2. 增加热度 (Redis ZSet)
            try {
                // zIncrementScore(key, value, delta)
                redis().ZSetOps.zIncrementScore("blog:posts:hot", String.valueOf(id), 1.0);
            } catch (Exception ignored) {}

            // 3. 查询详情
            Map<String, Object> post = DatabaseUtil.selectOneByMp("SELECT * FROM posts WHERE id = #{p1}", mapParams(id));
            return Result.ok(post);
        }

        @PostmanMapping(value = "/hot-rank", method = RequestMethod.GET)
        public Result<Set<String>> hotRank() {
            // 获取 redis 缓存的前 5 名热度文章 ID
            try {
                // 倒序获取 (分数高的在前)
                Set<String> topIds = redis().ZSetOps.zReverseRange("blog:posts:hot", 0, 4);
                return Result.ok(topIds);
            } catch (Exception e) {
                return Result.ok(Collections.emptySet());
            }
        }
    }
}
