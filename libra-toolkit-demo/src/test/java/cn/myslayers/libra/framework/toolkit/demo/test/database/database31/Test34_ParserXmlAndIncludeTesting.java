package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlParameterConverter;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 测试MyBatisSqlParser从XML文件和Include标签解析SQL的功能
 */
@DisplayName("Test04: XML加载与Include")
public class Test34_ParserXmlAndIncludeTesting {

    @TempDir
    Path tempDir;

    private Map<String, Object> testParams;

    @BeforeEach
    void setUp() {
        testParams = new HashMap<>();
        testParams.put("id", 1);
        testParams.put("name", "张三");
        testParams.put("age", 25);
        testParams.put("email", "zhangsan@example.com");
        testParams.put("status", "ACTIVE");
        testParams.put("active", true);
    }

    /**
     * 创建测试XML文件
     */
    private File createTestXmlFile() throws IOException {
        File xmlFile = tempDir.resolve("UserMapper.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
            writer.write("<mapper namespace=\"cn.myslayers.device.mapper.UserMapper\">\n");
            writer.write("\n");
            writer.write("    <!-- 可重用SQL片段 -->\n");
            writer.write("    <sql id=\"userColumns\">id, name, age, email, status, active</sql>\n");
            writer.write("\n");
            writer.write("    <!-- 基础查询 -->\n");
            writer.write("    <select id=\"findById\" parameterType=\"int\" resultType=\"User\">\n");
            writer.write("        SELECT <include refid=\"userColumns\"/> FROM users WHERE id = #{id}\n");
            writer.write("    </select>\n");
            writer.write("\n");
            writer.write("    <!-- 条件查询 -->\n");
            writer.write("    <select id=\"findByCondition\" parameterType=\"map\" resultType=\"User\">\n");
            writer.write("        SELECT <include refid=\"userColumns\"/> FROM users WHERE 1=1\n");
            writer.write("        <if test=\"name != null\">\n");
            writer.write("            AND name = #{name}\n");
            writer.write("        </if>\n");
            writer.write("        <if test=\"age != null\">\n");
            writer.write("            AND age = #{age}\n");
            writer.write("        </if>\n");
            writer.write("        <if test=\"email != null\">\n");
            writer.write("            AND email = #{email}\n");
            writer.write("        </if>\n");
            writer.write("    </select>\n");
            writer.write("\n");
            writer.write("    <!-- 动态SQL -->\n");
            writer.write("    <select id=\"findByDynamicCondition\" parameterType=\"map\" resultType=\"User\">\n");
            writer.write("        SELECT <include refid=\"userColumns\"/> FROM users\n");
            writer.write("        <where>\n");
            writer.write("            <if test=\"name != null and name != ''\">\n");
            writer.write("                AND name LIKE CONCAT('%', #{name}, '%')\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"age != null\">\n");
            writer.write("                AND age >= #{age}\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"status != null\">\n");
            writer.write("                AND status = #{status}\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"active != null\">\n");
            writer.write("                AND active = #{active}\n");
            writer.write("            </if>\n");
            writer.write("        </where>\n");
            writer.write("        ORDER BY id DESC\n");
            writer.write("    </select>\n");
            writer.write("\n");
            writer.write("    <!-- 插入操作 -->\n");
            writer.write("    <insert id=\"insertUser\" parameterType=\"User\">\n");
            writer.write("        INSERT INTO users (name, age, email, status, active)\n");
            writer.write("        VALUES (#{name}, #{age}, #{email}, #{status}, #{active})\n");
            writer.write("    </insert>\n");
            writer.write("\n");
            writer.write("    <!-- 更新操作 -->\n");
            writer.write("    <update id=\"updateUser\" parameterType=\"User\">\n");
            writer.write("        UPDATE users\n");
            writer.write("        <set>\n");
            writer.write("            <if test=\"name != null\">\n");
            writer.write("                name = #{name},\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"age != null\">\n");
            writer.write("                age = #{age},\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"email != null\">\n");
            writer.write("                email = #{email},\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"status != null\">\n");
            writer.write("                status = #{status},\n");
            writer.write("            </if>\n");
            writer.write("            <if test=\"active != null\">\n");
            writer.write("                active = #{active}\n");
            writer.write("            </if>\n");
            writer.write("        </set>\n");
            writer.write("        WHERE id = #{id}\n");
            writer.write("    </update>\n");
            writer.write("\n");
            writer.write("    <!-- 删除操作 -->\n");
            writer.write("    <delete id=\"deleteUser\" parameterType=\"int\">\n");
            writer.write("        DELETE FROM users WHERE id = #{id}\n");
            writer.write("    </delete>\n");
            writer.write("\n");
            writer.write("    <!-- 批量查询 -->\n");
            writer.write("    <select id=\"findByIds\" parameterType=\"map\" resultType=\"User\">\n");
            writer.write("        SELECT <include refid=\"userColumns\"/> FROM users WHERE id IN\n");
            writer.write("        <foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n");
            writer.write("            #{id}\n");
            writer.write("        </foreach>\n");
            writer.write("    </select>\n");
            writer.write("\n");
            writer.write("    <!-- 复杂条件查询 -->\n");
            writer.write("    <select id=\"findByComplexCondition\" parameterType=\"map\" resultType=\"User\">\n");
            writer.write("        SELECT <include refid=\"userColumns\"/> FROM users WHERE 1=1\n");
            writer.write("        <choose>\n");
            writer.write("            <when test=\"name != null and name != ''\">\n");
            writer.write("                AND name = #{name}\n");
            writer.write("            </when>\n");
            writer.write("            <when test=\"email != null and email != ''\">\n");
            writer.write("                AND email = #{email}\n");
            writer.write("            </when>\n");
            writer.write("            <otherwise>\n");
            writer.write("                AND status = 'ACTIVE'\n");
            writer.write("            </otherwise>\n");
            writer.write("        </choose>\n");
            writer.write("        <if test=\"age != null\">\n");
            writer.write("            AND age >= #{age}\n");
            writer.write("        </if>\n");
            writer.write("    </select>\n");
            writer.write("\n");
            writer.write("</mapper>\n");
            writer.flush();
        }
        return xmlFile;
    }

    @Test
    @DisplayName("测试从XML文件解析基础查询")
    void testParseBasicSelectFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", testParams);

        System.out.println("=== 测试从XML文件解析基础查询 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含include解析后的列: " + result.getSql().contains("id, name, age, email, status, active"));
            System.out.println("SQL包含基础查询: " + result.getSql().contains("FROM users WHERE id = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析条件查询")
    void testParseConditionalSelectFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findByCondition", testParams);

        System.out.println("=== 测试从XML文件解析条件查询 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含age条件: " + result.getSql().contains("age = ?"));
            System.out.println("SQL包含email条件: " + result.getSql().contains("email = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析动态SQL")
    void testParseDynamicSqlFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findByDynamicCondition", testParams);

        System.out.println("=== 测试从XML文件解析动态SQL ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WHERE子句: " + result.getSql().contains("WHERE"));
            System.out.println("SQL包含name LIKE条件: " + result.getSql().contains("name LIKE"));
            System.out.println("SQL包含age条件: " + result.getSql().contains("age >= ?"));
            System.out.println("SQL包含status条件: " + result.getSql().contains("status = ?"));
            System.out.println("SQL包含active条件: " + result.getSql().contains("active = ?"));
            System.out.println("SQL包含ORDER BY: " + result.getSql().contains("ORDER BY id DESC"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析插入操作")
    void testParseInsertFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "insertUser", testParams);

        System.out.println("=== 测试从XML文件解析插入操作 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含INSERT语句: " + result.getSql().contains("INSERT INTO users"));
            System.out.println("SQL包含VALUES子句: " + result.getSql().contains("VALUES"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析更新操作")
    void testParseUpdateFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "updateUser", testParams);

        System.out.println("=== 测试从XML文件解析更新操作 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含UPDATE语句: " + result.getSql().contains("UPDATE users"));
            System.out.println("SQL包含SET子句: " + result.getSql().contains("SET"));
            System.out.println("SQL包含WHERE条件: " + result.getSql().contains("WHERE id = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析删除操作")
    void testParseDeleteFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "deleteUser", testParams);

        System.out.println("=== 测试从XML文件解析删除操作 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含DELETE语句: " + result.getSql().contains("DELETE FROM users"));
            System.out.println("SQL包含WHERE条件: " + result.getSql().contains("WHERE id = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析批量查询")
    void testParseBatchSelectFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        Map<String, Object> params = new HashMap<>();
        params.put("ids", java.util.Arrays.asList(1, 2, 3, 4, 5));

        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findByIds", params);

        System.out.println("=== 测试从XML文件解析批量查询 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含IN子句: " + result.getSql().contains("IN"));
            System.out.println("SQL包含参数占位符: " + result.getSql().contains("(?,?,?,?,?)"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析复杂条件查询")
    void testParseComplexConditionFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findByComplexCondition", testParams);

        System.out.println("=== 测试从XML文件解析复杂条件查询 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含age条件: " + result.getSql().contains("age >= ?"));
            System.out.println("SQL不包含email条件: " + !result.getSql().contains("email = ?"));
            System.out.println("SQL不包含默认status条件: " + !result.getSql().contains("status = 'ACTIVE'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试解析不存在的SQL ID")
    void testParseNonExistentSqlId() throws IOException {
        File xmlFile = createTestXmlFile();

        System.out.println("=== 测试解析不存在的SQL ID ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("查询的SQL ID: nonExistentId");

        try {
            MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "nonExistentId", testParams);
            System.out.println("解析结果为空: " + (result == null));
        } catch (RuntimeException e) {
            System.out.println("预期异常: " + e.getMessage());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试解析不存在的XML文件")
    void testParseNonExistentXmlFile() {
        String nonExistentPath = "/path/to/nonexistent/file.xml";

        System.out.println("=== 测试解析不存在的XML文件 ===");
        System.out.println("XML文件路径: " + nonExistentPath);

        try {
            MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(nonExistentPath, "findById", testParams);
            System.out.println("解析结果为空: " + (result == null));
        } catch (RuntimeException e) {
            System.out.println("预期异常: " + e.getMessage());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试XML文件缓存功能")
    void testXmlFileCaching() throws IOException {
        File xmlFile = createTestXmlFile();

        // 第一次解析
        long startTime1 = System.currentTimeMillis();
        MyBatisSqlWithParams result1 = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", testParams);
        long endTime1 = System.currentTimeMillis();
        long duration1 = endTime1 - startTime1;

        // 第二次解析（应该使用缓存）
        long startTime2 = System.currentTimeMillis();
        MyBatisSqlWithParams result2 = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", testParams);
        long endTime2 = System.currentTimeMillis();
        long duration2 = endTime2 - startTime2;

        System.out.println("=== 测试XML文件缓存功能 ===");
        System.out.println("第一次解析时间: " + duration1 + "ms");
        System.out.println("第二次解析时间: " + duration2 + "ms");
        System.out.println("两次解析结果相同: " + (result1 != null && result2 != null && result1.getSql().equals(result2.getSql())));
        System.out.println();
    }

    @Test
    @DisplayName("测试空参数解析")
    void testParseWithEmptyParams() throws IOException {
        File xmlFile = createTestXmlFile();
        Map<String, Object> emptyParams = new HashMap<>();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", emptyParams);

        System.out.println("=== 测试空参数解析 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试null参数解析")
    void testParseWithNullParams() throws IOException {
        File xmlFile = createTestXmlFile();

        System.out.println("=== 测试null参数解析 ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());

        try {
            MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", null);
            System.out.println("解析结果不为空: " + (result != null));
            if (result != null) {
                System.out.println("解析后的SQL: " + result.getSql());
                System.out.println("参数数量: " + result.getParams().size());
                System.out.println("参数内容: " + result.getParams());
            }
        } catch (RuntimeException e) {
            System.out.println("预期异常: " + e.getMessage());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试从XML文件解析带include标签的SQL")
    void testParseWithIncludeTagFromXml() throws IOException {
        File xmlFile = createTestXmlFile();
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertXmlFile(xmlFile.getAbsolutePath(), "findById", testParams);

        System.out.println("=== 测试从XML文件解析带include标签的SQL ===");
        System.out.println("XML文件路径: " + xmlFile.getAbsolutePath());
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含include解析后的列: " + result.getSql().contains("id, name, age, email, status, active"));
            System.out.println("SQL不包含<include>标签: " + !result.getSql().contains("<include"));
        }
        System.out.println();
    }
}
