package cn.myslayers.libra.framework.toolkit.demo.test.http.test;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequestMethod;
import java.util.HashMap;
import java.util.Map;

// 雇主信息模拟控制器
@PostmanController("/employer")
public class EmployerController extends PostmanUtil.BaseController {

    private static final Map<String, Map<String, Object>> companyDatabase = new HashMap<>();

    // 初始化模拟数据库
    static {
        Map<String, Object> company1 = new HashMap<>();
        company1.put("id", "C01");
        company1.put("name", "Tech Innovators Inc.");
        company1.put("industry", "Technology");
        companyDatabase.put("C01", company1);

        Map<String, Object> company2 = new HashMap<>();
        company2.put("id", "C02");
        company2.put("name", "Green Solutions");
        company2.put("industry", "Renewable Energy");
        companyDatabase.put("C02", company2);
    }

    // 获取所有雇主信息
    @PostmanMapping(value = "/list", method = RequestMethod.GET)
    public PostmanUtil.Result<Object> getAllEmployers() {
        return PostmanUtil.Result.ok(companyDatabase.values());
    }

    // 根据ID获取单个雇主信息
     @PostmanMapping(value = "/detail", method = RequestMethod.GET)
    public PostmanUtil.Result<Map<String, Object>> getEmployerById() {
        Map<String, String> params = getParams();
        String id = params.get("id");
        if (id == null || !companyDatabase.containsKey(id)) {
            return PostmanUtil.Result.error(404, "Employer not found with id: " + id);
        }
        return PostmanUtil.Result.ok(companyDatabase.get(id));
    }

    // --- 单体调试模式入口 ---
    public static void main(String[] args) {
        PostmanUtil.HttpCore.startServer(EmployerController.class);
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
