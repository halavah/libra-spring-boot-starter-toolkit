package cn.myslayers.libra.framework.toolkit.demo.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

/**
 * 测试接口
 */
public interface TestMapper {
    /**
     * libra_order_main
     */
    List<Map<String, Object>> queryOrderMainList(Map<String, Object> acceptDto);

    /**
     * libra_order_main 分页查询
     */
    IPage<Map<String, Object>> queryOrderMainListPage(Page<Map<String, Object>> page, Map<String, Object> acceptDto);
}
