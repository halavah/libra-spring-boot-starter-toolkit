package cn.myslayers.libra.framework.toolkit.demo.test.workspace.erp;

import cn.hutool.core.date.DateUtil;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.BaseController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanController;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.PostmanMapping;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.RequestMethod;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.Result;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 演示案例：简易企业 ERP 系统 (Simple ERP)
 * <p>
 * 场景特点：
 * 1. 强一致性：员工入职涉及多表操作，必须使用事务
 * 2. 批量处理：每月薪资计算涉及大量数据 update/insert
 * 3. 复杂查询：报表统计
 * </p>
 */
@Slf4j
public class SimpleERPExample {

    public static void main(String[] args) {
        initInfrastructure();

        PostmanUtil.setPort(8083);
        PostmanUtil.HttpCore.startServer(HRController.class);

        System.out.println("🏢 ERP System started at http://localhost:8083");
    }

    private static void initInfrastructure() {
        // Init DB
        DatabaseUtil.DataSourceCore.register("erp_db",
                "jdbc:h2:mem:erp_db;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");
        DatabaseUtil.DataSourceCore.setDefaultDataSource("erp_db");

        // 员工基本表
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS employees (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "emp_no VARCHAR(20) UNIQUE, " +
                "name VARCHAR(50), " +
                "dept_id INT, " +
                "base_salary DECIMAL(10,2), " +
                "status VARCHAR(10), " +
                "join_date DATETIME)");

        // 薪资记录表
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS salaries (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "emp_id BIGINT, " +
                "pay_month VARCHAR(7), " + // 2023-01
                "amount DECIMAL(10,2), " +
                "paid_at DATETIME)");

        // 部门表
        DatabaseUtil.executeDDL("CREATE TABLE IF NOT EXISTS departments (" +
                "id INT PRIMARY KEY, " +
                "name VARCHAR(50))");

        // 预置部门数据
        try {
            DatabaseUtil.executeDML("INSERT INTO departments VALUES (101, 'Tech'), (102, 'Sales'), (103, 'HR')");
        } catch (Exception ignored) {} // Ignore if exists
    }

    // 辅助方法
    private static Map<String, Object> mapParams(Object... args) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            map.put("p" + (i + 1), args[i]);
        }
        return map;
    }

    // ================== DTOs ==================
    @Data
    static class EmployeeOnboardDTO {
        private String empNo; // generated
        private String name;
        private Integer deptId;
        private BigDecimal baseSalary;
        private String status; // generated
        private Date joinDate; // generated
    }

    @Data
    static class SalaryRecordDTO {
        private Long empId;
        private String payMonth;
        private BigDecimal amount;
        private Date paidAt;
    }

    // ================== Controller ==================
    @PostmanController("/api/erp")
    public static class HRController extends BaseController {

        /**
         * 员工入职 (事务演示)
         * 必须同时成功：生成工号 -> 插入员工表 -> 初始化薪资档案(模拟)
         */
        @PostmanMapping(value = "/employees/onboard", method = RequestMethod.POST)
        public Result<String> onboard() {
            EmployeeOnboardDTO dto = getBody(EmployeeOnboardDTO.class);

            // 手动管理事务
            DatabaseUtil.DatabaseAccessor db = DatabaseUtil.accessor();

            try {
                db.setTransaction(); // 开启事务

                String empNo = "EMP" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomInt(100, 999);
                dto.setEmpNo(empNo);
                dto.setStatus("ACTIVE");
                dto.setJoinDate(new Date());

                // 1. 插入员工表
                String sqlEmp = "INSERT INTO employees (emp_no, name, dept_id, base_salary, status, join_date) VALUES (#{empNo}, #{name}, #{deptId}, #{baseSalary}, #{status}, #{joinDate})";
                db.executeByMpEnhanced(sqlEmp, dto);

                // 2. 模拟抛出异常来测试回滚 (如果名字叫 "ERROR_TEST")
                if ("ERROR_TEST".equals(dto.name)) {
                    throw new RuntimeException("Simulated Trace Error");
                }

                db.commit(); // 提交事务
                return Result.ok("Employee onboarded successfully.");

            } catch (Exception e) {
                db.rollback(); // 回滚事务
                return Result.error(500, "Onboarding failed: " + e.getMessage());
            }
        }

        /**
         * 批量发放薪资 (Batch Update 演示)
         * 假设每月底给所有 ACTIVE 员工发放当月薪资
         */
        @PostmanMapping(value = "/salaries/calculate", method = RequestMethod.POST)
        public Result<String> calculateSalaries() {
            String month = getParams().get("month"); // e.g., 2023-10

            // 1. 查出所有在职员工
            List<Map<String, Object>> activeEmps = DatabaseUtil.selectListByMp(
                    "SELECT id, base_salary FROM employees WHERE status = 'ACTIVE'", null
            );

            if (activeEmps.isEmpty()) {
                return Result.ok("No active employees found.");
            }

            // 批量插入 - 使用事务包裹循环
            DatabaseUtil.DatabaseAccessor db = DatabaseUtil.accessor();
            db.setTransaction();

            int count = 0;
            try {
                String sqlInsert = "INSERT INTO salaries (emp_id, pay_month, amount, paid_at) VALUES (#{empId}, #{payMonth}, #{amount}, #{paidAt})";

                for (Map<String, Object> emp : activeEmps) {
                    Long empId = Long.parseLong(emp.get("id").toString());
                    BigDecimal salary = (BigDecimal) emp.get("base_salary");

                    // 模拟简单的绩效浮动 (-500 ~ +500)
                    BigDecimal bonus = new BigDecimal(RandomUtil.randomInt(-500, 500));
                    BigDecimal finalPay = salary.add(bonus);

                    SalaryRecordDTO record = new SalaryRecordDTO();
                    record.setEmpId(empId);
                    record.setPayMonth(month);
                    record.setAmount(finalPay);
                    record.setPaidAt(new Date());

                    db.executeByMpEnhanced(sqlInsert, record);
                    count++;
                }

                db.commit();
                return Result.ok("Processed payroll for " + count + " employees. Month: " + month);

            } catch (Exception e) {
                db.rollback();
                return Result.error(500, "Payroll failed: " + e.getMessage());
            }
        }

        /**
         * 部门薪资统计报表 (复杂查询演示)
         */
        @PostmanMapping(value = "/reports/salary-by-dept", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> salaryReport() {
            String sql = "SELECT d.name AS dept_name, COUNT(e.id) AS emp_count, SUM(s.amount) AS total_payout " +
                         "FROM salaries s " +
                         "JOIN employees e ON s.emp_id = e.id " +
                         "JOIN departments d ON e.dept_id = d.id " +
                         "GROUP BY d.name " +
                         "ORDER BY total_payout DESC";

            return Result.ok(DatabaseUtil.selectListByMp(sql, null));
        }

        @PostmanMapping(value = "/employees", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> listEmployees() {
            return Result.ok(DatabaseUtil.selectListByMp("SELECT * FROM employees", null));
        }
    }

    // 简单的工具类模拟
    static class RandomUtil {
        static int randomInt(int min, int max) {
            return new Random().nextInt(max - min) + min;
        }
    }
}
