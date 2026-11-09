package cn.myslayers.libra.framework.toolkit.demo.mapper;

import java.util.List;
import java.util.Map;

/**
 * Demo4Mapper - 边缘案例与特殊语法专题接口
 * 对应 Demo4Mapper.xml 中的所有SQL定义
 */
public interface Demo4Mapper {

    // ================================ 特殊字符与XML实体测试 ================================

    // 测试用例 1.1: OGNL表达式中使用XML实体字符
    // 目标: 验证 test 属性中的 &, <, > 等能否被正确解析
    List<Map<String, Object>> findWithXmlEntitiesInTest(Map<String, Object> parameters);

    // 测试用例 1.2: 使用 CDATA 块包裹动态SQL
    // 目标: 验证解析器能否正确处理 CDATA 块内的逻辑
    List<Map<String, Object>> findWithCdataBlock(Map<String, Object> parameters);

    // ================================ 边缘情况测试 ================================

    // 测试用例 2.1: 空的动态标签
    // 目标: 验证空的 <where>, <set>, <trim> 标签不会导致解析错误
    List<Map<String, Object>> findWithEmptyDynamicTags(Map<String, Object> parameters);

    // 更新操作 - 空的set标签
    int updateWithEmptySetTag(Map<String, Object> parameters);

    // 测试用例 2.2: 动态标签内容解析后为空
    // 目标: 验证 <if> 条件不满足时，<where> 标签能正确处理空内容
    List<Map<String, Object>> findWithResultingEmptyWhere(Map<String, Object> parameters);

    // ================================ 复杂OGNL表达式测试 ================================

    // 测试用例 3.1: 简单布尔值判断
    // 目标: 验证 test 属性中可以直接判断Map中的布尔值
    List<Map<String, Object>> findByUserMethod(Map<String, Object> parameters);

    // 测试用例 3.2: 使用三元运算符
    // 目标: 验证 test 属性中支持三元运算符
    List<Map<String, Object>> findByTernaryOperator(Map<String, Object> parameters);

    // 测试用例 3.3: 复杂的逻辑与算术运算
    // 目标: 验证 test 属性中支持复杂的混合运算
    List<Map<String, Object>> findByComplexExpression(Map<String, Object> parameters);

    // ================================ MyBatis内置变量测试 ================================

    // 测试用例 4.1: 使用 _parameter 内置变量
    // 目标: 验证当参数为简单类型时，_parameter 可以被正确引用
    List<Map<String, Object>> findBySimpleParameter(String parameter);

    // 测试用例 4.2: 使用 _databaseId 内置变量 (模拟)
    // 目标: 验证 _databaseId 可以在逻辑判断中使用
    List<Map<String, Object>> findByDatabaseId(Map<String, Object> parameters);
}
