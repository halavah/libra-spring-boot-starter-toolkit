package cn.myslayers.libra.framework.toolkit.demo.test.http.test;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequestMethod;
import java.util.HashMap;
import java.util.Map;

// 薪资业务模拟控制器
@PostmanController("/salary")
public class SalaryController extends PostmanUtil.BaseController {
    private static final Map<String, Map<String, Object>> database = new HashMap<>();

    // 初始化模拟数据库
    static {
        Map<String, Object> user1 = new HashMap<>();
        user1.put("empNo", "J001");
        user1.put("name", "Alice");
        user1.put("salary", 12000.0);
        database.put("J001", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("empNo", "J002");
        user2.put("name", "Bob");
        user2.put("salary", 15000.0);
        database.put("J002", user2);
    }

    // 测试GET请求：带参数，返回单个用户
    @PostmanMapping(value = "/user", method = RequestMethod.GET)
    public PostmanUtil.Result<Map<String, Object>> getUser() {
        Map<String, String> params = getParams();
        String empNo = params.get("empNo");
        if (empNo == null || !database.containsKey(empNo)) {
            return PostmanUtil.Result.error(404, "用户未找到: " + empNo);
        }
        return PostmanUtil.Result.ok(database.get(empNo));
    }

    // 测试GET请求：无参数，返回所有用户
    @PostmanMapping(value = "/users", method = RequestMethod.GET)
    public PostmanUtil.Result<Map<String, Map<String, Object>>> getAllUsers() {
        return PostmanUtil.Result.ok(database);
    }

    // 测试POST请求：创建新用户
    @PostmanMapping(value = "/user", method = RequestMethod.POST)
    public PostmanUtil.Result<Map<String, Object>> createUser() {
        try {
            Map<String, Object> newUser = getBody(Map.class);
            String empNo = (String) newUser.get("empNo");
            if (empNo == null || empNo.isEmpty()) {
                return PostmanUtil.Result.error(400, "empNo 不能为空");
            }
            if (database.containsKey(empNo)) {
                return PostmanUtil.Result.error(409, "用户已存在: " + empNo);
            }
            database.put(empNo, newUser);
            return PostmanUtil.Result.ok(newUser);
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // 测试PUT请求：更新用户信息
    @PostmanMapping(value = "/user", method = RequestMethod.PUT)
    public PostmanUtil.Result<Map<String, Object>> updateUser() {
        try {
            Map<String, Object> updatedUser = getBody(Map.class);
            String empNo = (String) updatedUser.get("empNo");
            if (empNo == null || !database.containsKey(empNo)) {
                return PostmanUtil.Result.error(404, "需要更新的用户未找到: " + empNo);
            }
            database.put(empNo, updatedUser);
            return PostmanUtil.Result.ok(updatedUser);
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // 测试DELETE请求：删除用户
    @PostmanMapping(value = "/user", method = RequestMethod.DELETE)
    public PostmanUtil.Result<Map<String, Object>> deleteUser() {
        Map<String, String> params = getParams();
        String empNo = params.get("empNo");
        if (empNo == null || !database.containsKey(empNo)) {
            return PostmanUtil.Result.error(404, "需要删除的用户未找到: " + empNo);
        }
        Map<String, Object> removedUser = database.remove(empNo);
        return PostmanUtil.Result.ok(removedUser);
    }


    // --- 单体调试模式入口 ---
    public static void main(String[] args) {
        PostmanUtil.HttpCore.startServer(SalaryController.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PostmanUtil.HttpCore.stopServer();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            PostmanUtil.HttpCore.stopServer();
        }
    }
}
