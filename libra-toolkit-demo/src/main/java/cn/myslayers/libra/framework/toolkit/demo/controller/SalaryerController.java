package cn.myslayers.libra.framework.toolkit.demo.controller;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequestMethod;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Salaryer;
import cn.myslayers.libra.framework.toolkit.demo.service.SalaryerService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

// 薪资业务控制器
@PostmanController("/salaryer")
@RequiredArgsConstructor
public class SalaryerController extends PostmanUtil.BaseController {

    private final SalaryerService salaryerService;

    // 根据用户ID获取薪资列表
    @PostmanMapping(value = "/list", method = RequestMethod.GET)
    public PostmanUtil.Result<List<Salaryer>> getSalariesByUserId() {
        Map<String, String> params = getParams();
        String userId = params.get("userId");
        if (userId == null || userId.isEmpty()) {
            return PostmanUtil.Result.error(400, "User ID cannot be empty.");
        }
        List<Salaryer> salaries = salaryerService.getSalariesByUserId(userId);
        return PostmanUtil.Result.ok(salaries);
    }

    // 发放薪水
    @PostmanMapping(value = "/issue", method = RequestMethod.POST)
    public PostmanUtil.Result<Salaryer> issueSalary() {
        try {
            Salaryer salary = getBody(Salaryer.class);
            Salaryer issuedSalary = salaryerService.issueSalary(salary);
            return PostmanUtil.Result.ok(issuedSalary);
        } catch (Exception e) {
            // 捕获JSON解析异常和业务异常
            return PostmanUtil.Result.error(500, "Failed to issue salary: " + e.getMessage());
        }
    }

    // 删除薪资记录
    @PostmanMapping(value = "/delete", method = RequestMethod.POST)
    public PostmanUtil.Result<String> deleteSalary() {
        try {
            Map<String, String> body = getBody(Map.class);
            String id = body.get("id");
            if (id == null || id.isEmpty()) {
                return PostmanUtil.Result.error(400, "ID cannot be null or empty in request body for delete.");
            }
            boolean deleted = salaryerService.deleteSalary(id);
            if (!deleted) {
                return PostmanUtil.Result.error(404, "Salary record not found with id: " + id);
            }
            return PostmanUtil.Result.ok("Salary record with id " + id + " deleted successfully.");
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // Main入口
    public static void main(String[] args) {
        PostmanUtil.HttpCore.startServer(SalaryerController.class);
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
