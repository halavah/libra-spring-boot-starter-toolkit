package cn.myslayers.libra.framework.toolkit.demo.mapper;

import java.util.List;
import java.util.Map;

/**
 * Demo2Mapper - 高级动态SQL示例接口
 * 对应 Demo2Mapper.xml 中的所有SQL定义
 */
public interface Demo2Mapper {

    // ================================ 高级动态SQL示例 ================================

    // 多表关联动态查询
    List<Map<String, Object>> selectUsersWithDepartmentAndRole(Map<String, Object> params);

    // 复杂条件组合查询
    List<Map<String, Object>> selectUsersAdvancedSearch(Map<String, Object> params);

    // 动态分组统计
    List<Map<String, Object>> getUserStatistics(Map<String, Object> params);

    // ================================ 批量操作示例 ================================

    // 条件批量更新
    int batchUpdateUserStatus(Map<String, Object> params);

    // 条件批量删除
    int batchDeleteUsers(Map<String, Object> params);

    // 根据条件插入不同字段
    int insertUserDynamic(Map<String, Object> params);

    // ================================ 高级查询示例 ================================

    // 复杂子查询
    List<Map<String, Object>> selectUsersWithSubquery(Map<String, Object> params);

    // 动态UNION查询
    List<Map<String, Object>> selectUsersUnion(Map<String, Object> params);

    // 动态CTE查询
    List<Map<String, Object>> selectUsersWithCTE(Map<String, Object> params);

    // 动态窗口函数查询
    List<Map<String, Object>> selectUsersWithRanking(Map<String, Object> params);

    // 动态JSON查询
    List<Map<String, Object>> selectUsersWithJsonQuery(Map<String, Object> params);
}
