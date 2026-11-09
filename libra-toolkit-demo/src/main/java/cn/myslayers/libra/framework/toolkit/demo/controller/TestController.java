package cn.myslayers.libra.framework.toolkit.demo.controller;

import cn.myslayers.libra.framework.toolkit.demo.service.TestService;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.Result;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private TestService testService;

    /**
     * 测试1
     */
    @GetMapping("/demo1")
    public Result<String> demo1Request() {
        return Result.ok("测试成功！");
    }

    /**
     * 测试2
     */
    @PostMapping("/demo2")
    public Result<String> demo2Request(@RequestBody Map<String, Object> params) {
        return Result.ok(JSON.toJSONString(params));
    }

    /**
     * demo3 - 使用Service层查询数据（修复SqlRunner问题）
     */
    @GetMapping("/demo3")
    public Result<List<Map<String, Object>>> demo3Request() {
        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("all", true); // 查询所有记录的标识
            Result<List<Map<String, Object>>> result = testService.queryOrderMainList(queryParams);

            if (result.isSuccess() && result.getResult() != null) {
                log.info("demo3查询成功，返回{}条记录", result.getResult().size());
                return Result.ok(result.getResult());
            } else {
                return Result.error(500, "查询失败");
            }
        } catch (Exception e) {
            log.error("demo3查询异常：", e);
            return Result.error(500, "查询异常：" + e.getMessage());
        }
    }

    /**
     * libra_order_main
     */
    @PostMapping("/demo4")
    public Result<List<Map<String, Object>>> demo4Request(@RequestBody Map<String, Object> acceptDto) {
        // 增强参数校验逻辑
        if (acceptDto == null || acceptDto.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }

        return testService.queryOrderMainList(acceptDto);
    }

    /**
     * libra_order_main
     */
    @PostMapping("/demo4_page")
    public Result<IPage<Map<String, Object>>> demo4PageRequest(@RequestBody Map<String, Object> acceptDto,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 自定义参数校验逻辑
        if (acceptDto == null || acceptDto.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return testService.queryOrderMainListPage(page, acceptDto);
    }
}
