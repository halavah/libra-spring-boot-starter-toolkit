package cn.myslayers.libra.framework.toolkit.demo.mapper;

import java.util.List;
import java.util.Map;

/**
 * Demo1Mapper - 基础和动态SQL标签示例接口
 * 对应 Demo1Mapper.xml 中的所有SQL定义
 */
public interface Demo1Mapper {

    // ================================ 基础标签示例 ================================

    // 基础查询
    List<Map<String, Object>> selectUserById(Map<String, Object> params);

    // 基础插入
    int insertUser(Map<String, Object> user);

    // 基础更新
    int updateUser(Map<String, Object> user);

    // 基础删除
    int deleteUser(Map<String, Object> params);

    // ================================ 动态SQL标签示例 ================================

    // if标签：条件判断
    List<Map<String, Object>> selectUsersByCondition(Map<String, Object> params);

    // where+if标签
    List<Map<String, Object>> selectUsersWithWhere(Map<String, Object> params);

    // set标签
    int updateUserSelective(Map<String, Object> params);

    // choose(when,otherwise) 语句
    List<Map<String, Object>> selectUsersByType(Map<String, Object> params);

    // trim标签
    List<Map<String, Object>> selectUsersWithTrim(Map<String, Object> params);

    // foreach标签 - 批量查询
    List<Map<String, Object>> selectUsersByIds(Map<String, Object> params);

    // foreach标签 - 批量插入
    int batchInsertUsers(Map<String, Object> params);

    // foreach标签 - 批量更新
    int batchUpdateUsers(Map<String, Object> params);

    // foreach标签 - 复杂条件
    List<Map<String, Object>> selectUsersByComplexCondition(Map<String, Object> params);

    // ================================ 高级映射示例 ================================

    // 使用resultMap
    List<Map<String, Object>> selectUsersWithResultMap(Map<String, Object> params);

    // 关联查询
    List<Map<String, Object>> selectUsersWithRoles(Map<String, Object> params);

    // ================================ SQL片段复用示例 ================================

    // include标签 - 引用SQL片段
    List<Map<String, Object>> selectUsersWithInclude(Map<String, Object> params);

    // ================================ 特殊功能示例 ================================

    // bind标签 - 变量绑定
    List<Map<String, Object>> selectUsersWithBind(Map<String, Object> params);

    // 特殊字符：CDATA区段
    List<Map<String, Object>> selectUsersWithCDATA(Map<String, Object> params);

    // 特殊字符：XML实体
    List<Map<String, Object>> selectUsersWithXmlEntity(Map<String, Object> params);

    // ================================ 实用场景示例 ================================

    // 动态表关联
    List<Map<String, Object>> selectUsersWithDynamicJoin(Map<String, Object> params);

    // 批量操作优化 - 分页查询
    List<Map<String, Object>> selectUsersWithPagination(Map<String, Object> params);

    // 批量操作优化 - 统计查询
    Long countUsers(Map<String, Object> params);

    // 复杂动态SQL - 多条件组合
    List<Map<String, Object>> selectUsersComplex(Map<String, Object> params);

    // 动态字段查询
    List<Map<String, Object>> selectUsersWithDynamicFields(Map<String, Object> params);

    // 动态排序
    List<Map<String, Object>> selectUsersWithDynamicOrder(Map<String, Object> params);
}
