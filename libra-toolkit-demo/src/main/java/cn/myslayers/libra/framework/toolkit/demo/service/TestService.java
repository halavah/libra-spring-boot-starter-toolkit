package cn.myslayers.libra.framework.toolkit.demo.service;

import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.Result;
import cn.myslayers.libra.framework.toolkit.demo.mapper.TestMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 测试接口（合并后的单一实现）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {

    private final TestMapper testMapper;

    /**
     * libra_order_main
     *
     * @param acceptDto 查询参数
     * @return 查询结果
     */
    public Result<List<Map<String, Object>>> queryOrderMainList(Map<String, Object> acceptDto) {
        try {
            List<Map<String, Object>> list = testMapper.queryOrderMainList(acceptDto);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return Result.error(500, "查询订单列表失败: " + e.getMessage());
        }
    }

    /**
     * libra_order_main 分页查询
     *
     * @param page      分页对象
     * @param acceptDto 查询参数
     * @return 分页查询结果
     */
    public Result<IPage<Map<String, Object>>> queryOrderMainListPage(Page<Map<String, Object>> page, Map<String, Object> acceptDto) {
        try {
            IPage<Map<String, Object>> resultPage = testMapper.queryOrderMainListPage(page, acceptDto);
            return Result.ok(resultPage);
        } catch (Exception e) {
            log.error("分页查询订单列表失败", e);
            return Result.error(500, "分页查询订单列表失败: " + e.getMessage());
        }
    }
}
