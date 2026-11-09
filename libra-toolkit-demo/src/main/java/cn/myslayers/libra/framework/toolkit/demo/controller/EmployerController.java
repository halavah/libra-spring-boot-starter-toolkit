package cn.myslayers.libra.framework.toolkit.demo.controller;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequestMethod;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Employer;
import cn.myslayers.libra.framework.toolkit.demo.service.EmployerService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

// 雇主信息控制器
@PostmanController("/employer")
@RequiredArgsConstructor
public class EmployerController extends PostmanUtil.BaseController {

    private final EmployerService employerService;

    // 获取所有雇主信息
    @PostmanMapping(value = "/list", method = RequestMethod.GET)
    public PostmanUtil.Result<List<Employer>> getAllEmployers() {
        List<Employer> employers = employerService.getAllEmployers();
        return PostmanUtil.Result.ok(employers);
    }

    // 根据ID获取单个雇主信息
    @PostmanMapping(value = "/detail", method = RequestMethod.GET)
    public PostmanUtil.Result<Employer> getEmployerById() {
        Map<String, String> params = getParams();
        String id = params.get("id");
        Employer employer = employerService.getEmployerById(id);
        if (employer == null) {
            return PostmanUtil.Result.error(404, "Employer not found with id: " + id);
        }
        return PostmanUtil.Result.ok(employer);
    }

    // 创建新雇主
    @PostmanMapping(value = "/create", method = RequestMethod.POST)
    public PostmanUtil.Result<Employer> createEmployer() {
        try {
            Employer employer = getBody(Employer.class);
            Employer createdEmployer = employerService.createEmployer(employer);
            return PostmanUtil.Result.ok(createdEmployer);
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // 更新雇主信息
    @PostmanMapping(value = "/update", method = RequestMethod.POST)
    public PostmanUtil.Result<Employer> updateEmployer() {
        try {
            Employer employerDetails = getBody(Employer.class);
            // ID从请求体中获取
            String id = employerDetails.getId();
            if (id == null || id.isEmpty()) {
                return PostmanUtil.Result.error(400, "ID cannot be null or empty in request body for update.");
            }
            Employer updatedEmployer = employerService.updateEmployer(id, employerDetails);
            if (updatedEmployer == null) {
                return PostmanUtil.Result.error(404, "Employer not found with id: " + id);
            }
            return PostmanUtil.Result.ok(updatedEmployer);
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // 删除雇主
    @PostmanMapping(value = "/delete", method = RequestMethod.POST)
    public PostmanUtil.Result<String> deleteEmployer() {
        try {
            // 从请求体中解析出ID
            Map<String, String> body = getBody(Map.class);
            String id = body.get("id");
            if (id == null || id.isEmpty()) {
                return PostmanUtil.Result.error(400, "ID cannot be null or empty in request body for delete.");
            }
            boolean deleted = employerService.deleteEmployer(id);
            if (!deleted) {
                return PostmanUtil.Result.error(404, "Employer not found with id: " + id);
            }
            return PostmanUtil.Result.ok("Employer with id " + id + " deleted successfully.");
        } catch (Exception e) {
            return PostmanUtil.Result.error(400, "JSON解析或处理失败: " + e.getMessage());
        }
    }

    // Main入口
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
