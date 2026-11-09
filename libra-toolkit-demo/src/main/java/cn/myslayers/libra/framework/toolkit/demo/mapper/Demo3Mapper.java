package cn.myslayers.libra.framework.toolkit.demo.mapper;

import java.util.List;
import java.util.Map;

/**
 * Demo3Mapper - <include> 标签高级用法专题接口
 * 对应 Demo3Mapper.xml 中的所有SQL定义
 */
public interface Demo3Mapper {

    // ================================ <include> 标签测试用例 ================================

    // 测试用例 2.1: 简单引用
    // 目标: 测试最基本的 <include> 功能
    List<Map<String, Object>> findSimple(Map<String, Object> parameters);

    // 测试用例 2.2: 引用包含动态标签的SQL片段
    // 目标: 测试 <include> 后，内部的 <if>, <where> 是否能正确解析
    List<Map<String, Object>> findWithDynamicConditions(Map<String, Object> parameters);

    // 测试用例 2.3: 引用包含 ${} 占位符的SQL片段
    // 目标: 测试 <include> 后，内部的 ${} 是否能被正确替换
    List<Map<String, Object>> findWithDynamicOrdering(Map<String, Object> parameters);

    // ================================ <include> 与 <property> 组合测试 ================================

    // 测试用例 3.1: 使用 <property> 动态传递表名和列名
    // 目标: 测试 <include> 的 <property> 子元素是否能正确传递和替换值
    List<Map<String, Object>> findWithProperties(Map<String, Object> parameters);

    // ================================ 嵌套 <include> 测试 ================================

    // 测试用例 4.1: 嵌套引用多个SQL片段
    // 目标: 测试解析器能否正确处理链式和嵌套的 <include> 引用
    List<Map<String, Object>> findWithNestedInclude(Map<String, Object> parameters);
}
