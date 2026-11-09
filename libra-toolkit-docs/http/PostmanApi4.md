# 教程：构建单文件微服务——整合三层架构与动态SQL

## 引言

在现代Java开发中，我们常常追求敏捷和高效。传统的项目结构虽然清晰，但在快速原型验证或开发小型、独立的业务功能时，可能会显得过于繁重。本教程将向您展示一种创新的开发模式：如何利用 `PostmanUtil` 和 `DatabaseUtil` 这两个强大的工具，将一个完整的功能模块（包含Controller、Service、Mapper三层）聚合到**单个Java文件**中，从而实现一个“单文件微服务”。

这种模式的**核心优势**在于：
- **高度内聚**: 所有相关的代码都集中在一个文件中，便于理解和维护。
- **快速启动**: 无需部署整个应用，只需运行该文件的`main`方法即可启动一个功能完备的HTTP服务。
- **独立测试**: 可以针对单个功能进行独立的开发和单元测试，不受其他模块干扰。
- **动态SQL能力**: 即便在单个Java文件中，也能利用`DatabaseUtil`执行包含MyBatis动态标签的复杂SQL。

接下来，我们将以一个“查询员工电子合同信息”的功能为例，分步构建这个单文件微服务。

---

## 第一步：定义数据模型 (DTO & VO)

在开始编码前，首先要明确接口的输入和输出。我们将它们定义为静态内部类，以确保所有代码都包含在单个文件中。

- **DTO (Data Transfer Object)**: 用于封装客户端传入的参数。
- **VO (View Object)**: 用于封装返回给客户端的数据结构。

我们将这两个类放在主类的底部。

```java
// 位于主类底部
/**
 * 5. DTO 和 VO 定义：人事员工数据传输对象，用于电子合同查询
 */
public static class HrEmployeeDTO implements Serializable {
    // ... DTO 字段和方法 ...
}

/**
 * 5. DTO 和 VO 定义：电子合同VO
 */
public static class ContractVO implements Serializable {
    // ... VO 字段和方法 ...
}
```

## 第二步：构建数据访问层 (Mapper)

Mapper层负责与数据库直接交互。在这个模式中，我们不使用传统的XML文件，而是将SQL语句直接写在Java方法中。

#### 关键实现：
1.  **创建`ContractMapper`静态内部类**。
2.  在其中定义`queryCommonContractInfoByMp`方法，该方法接收一个`HrEmployeeDTO`对象。
3.  **内置动态SQL**: 将包含复杂`WITH`子句和MyBatis动态标签（如`#{empNo}`）的SQL查询语句，作为一个多行字符串直接定义在方法内部。
4.  **调用`DatabaseUtil`**: 使用`DatabaseUtil.accessor().selectListByMpEnhanced()`方法来执行这个SQL。这个方法能够解析字符串中的MyBatis标签，并根据传入的DTO对象自动填充参数。

```java
/**
 * 4. Mapper 层：作为静态内部类，负责数据访问
 */
public static class ContractMapper {
    public List<ContractVO> queryCommonContractInfoByMp(HrEmployeeDTO employeeDTO) {
        // 将包含 MyBatis 动态标签的 SQL 硬编码在方法中
        String sql =
            "WITH params AS (" +
            "    SELECT #{empNo} AS p_empNo, #{oid} AS p_openid FROM DUAL" +
            "), ..."; // (此处省略完整SQL)

        // 直接调用DatabaseUtil执行动态SQL，并自动将结果映射到ContractVO列表
        return DatabaseUtil.accessor().selectListByMpEnhanced(sql, employeeDTO, ContractVO.class);
    }
}
```

## 第三步：实现业务逻辑层 (Service)

Service层负责封装核心业务逻辑，它是Controller和Mapper之间的桥梁。

#### 关键实现：
1.  **创建`ContractService`静态内部类**。
2.  在内部实例化一个`ContractMapper`成员变量。
3.  定义`queryContractByEmpId`方法，它接收DTO，调用`contractMapper`的方法获取数据。
4.  可以根据需要添加其他业务方法，例如`isOperationValid`用于进行权限校验。

```java
/**
 * 3. Service 层：作为静态内部类，负责处理业务逻辑
 */
public static class ContractService {
    private final ContractMapper contractMapper = new ContractMapper();

    public List<ContractVO> queryContractByEmpId(HrEmployeeDTO employeeDTO) {
        // 调用Mapper层获取数据
        List<ContractVO> contractList = contractMapper.queryCommonContractInfoByMp(employeeDTO);
        // (此处可添加更多业务处理)
        return contractList;
    }

    public boolean isOperationValid(String oid) {
        // 模拟权限校验
        return true;
    }
}
```

## 第四步：创建接口暴露层 (Controller)

Controller层是面向客户端的入口，负责接收HTTP请求，并将其分发给Service层处理。

#### 关键实现：
1.  **创建`ContractWebEndpoint`静态内部类**。
2.  使用`@PostmanController("/contract")`注解来定义该控制器的根路径。
3.  在内部实例化一个`ContractService`成员变量。
4.  定义`queryContractByEmpNo`方法，并使用`@PostmanMapping`注解来指定其路径 (`/queryByEmpNo`) 和请求方法 (`POST`)。
5.  方法体内部完成参数解析、服务调用和响应构建的完整流程。

```java
/**
 * 2. Controller 层：作为静态内部类，负责处理 HTTP 请求
 */
@PostmanController("/contract")
public static class ContractWebEndpoint extends PostmanUtil.BaseController {
    private final ContractService contractService = new ContractService();

    @PostmanMapping(value = "/queryByEmpNo", method = RequestMethod.POST)
    public Result<List<ContractVO>> queryContractByEmpNo() {
        try {
            // 1. 解析请求参数
            HrEmployeeDTO employeeDTO = getBody(HrEmployeeDTO.class);
            // 2. 参数校验
            if (employeeDTO == null || employeeDTO.getEmpNo() == null || employeeDTO.getEmpNo().isEmpty()) {
                return Result.error(3000, "请求参数错误：员工工号不能为空");
            }
            // ... (其他校验)
            // 3. 调用服务层
            List<ContractVO> contractData = contractService.queryContractByEmpId(employeeDTO);
            // 4. 返回成功响应
            return Result.success(contractData);
        } catch (Exception e) {
            // 5. 返回错误响应
            return Result.error(3000, "查询失败：" + e.getMessage());
        }
    }
}
```

## 第五步：定义自定义响应体 (Result)

为了让API的响应格式更加灵活和标准化，我们在主类中定义一个自定义的`Result`静态内部类。`PostmanUtil`的智能响应机制会自动识别并直接序列化这个对象，而不会进行二次包装。

#### 关键实现：
1.  **创建`Result<T>`静态内部类**。
2.  定义`code`, `message`, `data`等核心字段。
3.  提供静态工厂方法 `success(T data)` 和 `error(int code, String message)`，方便快速创建响应实例。

```java
/**
 * 5. DTO 和 VO 定义：通用返回结果类
 */
public static class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    // ... Getters and Setters ...
}
```

## 第六步：编写启动入口 (main 方法)

最后，我们需要一个`main`方法作为整个单文件服务的启动入口。

#### 关键实现：
1.  **配置`PostmanUtil`**: 可以按需设置日志开关等。
2.  **启动服务器**: 调用`PostmanUtil.HttpCore.startServer()`。
3.  **指定目标Controller**: 在`startServer`方法中，传入我们刚刚创建的`ContractController.ContractWebEndpoint.class`。这告诉`PostmanUtil`，在本次启动中，**只加载并注册这一个Controller**，实现了完美的隔离。

```java
/**
 * 1. 主入口，用于启动 PostmanUtil 服务器
 */
public static void main(String[] args) throws Exception {
    System.out.println("=== PostmanUtil [单例模式] 启动 ===");
    // ... 日志配置 ...

    // 启动服务器，并明确指定只加载ContractWebEndpoint这个控制器
    PostmanUtil.HttpCore.startServer(ContractController.ContractWebEndpoint.class);

    // ... 添加ShutdownHook以优雅关闭 ...
}
```

---

## 完整代码示例

以下是整合了上述所有步骤的完整代码。您可以直接复制到一个名为`ContractController.java`的文件中，并运行其`main`方法来启动服务。
```java
package cn.myslayers.test.work.test;

import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.PostmanUtil;
import cn.myslayers.app.utils.PostmanUtil.PostmanController;
import cn.myslayers.app.utils.PostmanUtil.PostmanMapping;
import cn.myslayers.app.utils.PostmanUtil.RequestMethod;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @title ContractController
 * @description 将三层架构（Controller, Service, Mapper）整合到单一文件中，利用 PostmanUtil 启动 HTTP 服务，并使用 DatabaseUtil 执行动态 SQL。
 * @createTime 2025/7/22
 */
public class ContractController {

    /**
     * 1. 主入口，用于启动 PostmanUtil 服务器
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== PostmanUtil [单例模式] 启动 ===");

        // 开启核心调试日志 (服务器启动、扫描等)
        PostmanUtil.setDebugLogEnabled(true);
        // 开启启动时打印的接口列表
        PostmanUtil.setStartupLogEnabled(true);
        // 开启请求详情日志 (curl、出入参等)
        PostmanUtil.setRequestDetailLogEnabled(true);

        // 设置普通日志开关
        DatabaseUtil.LoggerCore.setDebugLogEnabled(true);
        // 检查普通日志开关状态
        DatabaseUtil.LoggerCore.setSqlDebugLogEnabled(true);

        // 启动服务器
        PostmanUtil.HttpCore.startServer(ContractController.ContractWebEndpoint.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭Postman测试工具...");
            PostmanUtil.HttpCore.stopServer();
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            PostmanUtil.HttpCore.stopServer();
        }
    }

    /**
     * 2. Controller 层：作为静态内部类，负责处理 HTTP 请求
     */
    @PostmanController("/contract")
    public static class ContractWebEndpoint extends PostmanUtil.BaseController {

        private final ContractService contractService = new ContractService();

        /**
         * 查询电子合同信息 (GET)
         */
        @PostmanMapping(value = "/queryByEmpNo", method = RequestMethod.GET)
        public Result<List<ContractVO>> queryContractByEmpNoGet() {
            try {
                // 1. 使用 getParams 获取参数
                Map<String, String> params = getParams();
                String empNo = params.get("empNo");
                String oid = params.get("oid");

                // 封装成DTO对象
                HrEmployeeDTO employeeDTO = new HrEmployeeDTO();
                employeeDTO.setEmpNo(empNo);
                employeeDTO.setOid(oid);

                // 2. 增加对DTO和工号列表的校验
                if (employeeDTO.getEmpNo() == null || employeeDTO.getEmpNo().isEmpty()) {
                    return Result.error(3000, "请求参数错误：员工工号不能为空");
                }
                if (employeeDTO.getOid() == null || employeeDTO.getOid().isEmpty()) {
                    return Result.error(3000, "请求参数错误：提交人openid不能为空");
                }
                if (!contractService.isOperationValid(employeeDTO.getOid())) {
                    return Result.error(3000, "请求参数错误：提交人openid校验非法");
                }

                // 3. 调用服务层
                List<ContractVO> contractData = contractService.queryContractByEmpId(employeeDTO);

                // 4. 直接返回成功响应
                return Result.success(contractData);
            } catch (Exception e) {
                e.printStackTrace();
                // 5. 直接返回错误响应
                return Result.error(3000, "查询失败：" + e.getMessage());
            }
        }

        /**
         * 查询电子合同信息 (POST)
         */
        @PostmanMapping(value = "/queryByEmpNo", method = RequestMethod.POST)
        public Result<List<ContractVO>> queryContractByEmpNoPost() {
            try {
                // 1. 使用 getBody 获取并解析参数
                HrEmployeeDTO employeeDTO = getBody(HrEmployeeDTO.class);

                // 2. 增加对DTO和工号列表的校验
                if (employeeDTO == null || employeeDTO.getEmpNo() == null || employeeDTO.getEmpNo().isEmpty()) {
                    return Result.error(3000, "请求参数错误：员工工号不能为空");
                }
                if (employeeDTO.getOid() == null || employeeDTO.getOid().isEmpty()) {
                    return Result.error(3000, "请求参数错误：提交人openid不能为空");
                }
                if (!contractService.isOperationValid(employeeDTO.getOid())) {
                    return Result.error(3000, "请求参数错误：提交人openid校验非法");
                }

                // 3. 调用服务层
                List<ContractVO> contractData = contractService.queryContractByEmpId(employeeDTO);

                // 4. 直接返回成功响应
                return Result.success(contractData);
            } catch (Exception e) {
                e.printStackTrace();
                // 5. 直接返回错误响应
                return Result.error(3000, "查询失败：" + e.getMessage());
            }
        }
    }

    /**
     * 3. Service 层：作为静态内部类，负责处理业务逻辑
     */
    public static class ContractService {

        private final ContractMapper contractMapper = new ContractMapper();

        /**
         * 查询电子合同信息
         */
        public List<ContractVO> queryContractByEmpId(HrEmployeeDTO employeeDTO) {
            // 批量查询员工的公共信息和合同核心信息
            List<ContractVO> contractList = contractMapper.queryCommonContractInfoByMp(employeeDTO);
            if (contractList == null || contractList.isEmpty()) {
                return contractList;
            }
            // 返回填充完毕的主VO列表
            return contractList;
        }

        /**
         * 模拟权限校验
         */
        public boolean isOperationValid(String oid) {
            // 在此可以添加真实的权限校验逻辑
            System.out.println("校验OpenID: " + oid + " 的操作权限... 权限通过。");
            return true;
        }
    }

    /**
     * 4. Mapper 层：作为静态内部类，负责数据访问
     */
    public static class ContractMapper {

        /**
         * 查询员工合同信息
         */
        public List<ContractVO> queryCommonContractInfoByMp(HrEmployeeDTO employeeDTO) {
            // 将包含 MyBatis 动态标签的 SQL 硬编码在方法中
            String sql =
                "WITH params AS (" +
                    "    SELECT #{empNo} AS p_empNo, #{oid} AS p_openid FROM DUAL" +
                    "), primary_info AS (" +
                    "    SELECT " +
                    "        empNo, compId, chiName, cellphone, idNo, " +
                    "        cellphone AS contactPhone, regiAddress, emergencyContactPerson, " +
                    "        emergencyContactTelno, contAddress " +
                    "    FROM DB.TBHRM1 m1 " +
                    "    WHERE empNo = (SELECT p_empNo FROM params)" +
                    "), subinfo AS (" +
                    "    SELECT chiName AS subEmp, cellphone AS subphone " +
                    "    FROM DB.TBHRM1 m1 " +
                    "    WHERE openid = (SELECT p_openid FROM params)" +
                    "), dept AS (" +
                    "    SELECT deptNo, deptChiName FROM DB.TBHRT1" +
                    "), posi AS (" +
                    "    SELECT position, " +
                    "           CASE WHEN INSTR(positionChiName, '-', -1) > 0 THEN SUBSTR(positionChiName, INSTR(positionChiName, '-', -1) + 1) ELSE positionChiName END AS positionName " +
                    "    FROM DB.TBHRT3" +
                    "), conTrial AS (" +
                    "    SELECT empNo, deptNo, position, salaryDate, trialPeriod " +
                    "    FROM DB.TBHRM0 " +
                    "    WHERE empNo = (SELECT p_empNo FROM params)" +
                    "), conFormal AS (" +
                    "    SELECT empNo, contractDateS, contractDateE, years " +
                    "    FROM (" +
                    "        SELECT empNo, contractDateS, contractDateE, years, " +
                    "               ROW_NUMBER() OVER(PARTITION BY empNo ORDER BY CREATEDATE DESC) AS rn " +
                    "        FROM DB.TBHRC1 " +
                    "        WHERE empNo = (SELECT p_empNo FROM params)" +
                    "    ) WHERE rn = 1" +
                    ") " +
                    "SELECT " +
                    "    primary_info.empNo AS \"empNo\", " +
                    "    primary_info.compId AS \"partyA\", " +
                    "    primary_info.chiName AS \"partyB\", " +
                    "    primary_info.cellphone AS \"phoneNumber\", " +
                    "    primary_info.idNo AS \"idCardNumber\", " +
                    "    primary_info.contactPhone AS \"contactPhone\", " +
                    "    primary_info.regiAddress AS \"contactAddress\", " +
                    "    primary_info.emergencyContactPerson AS \"emergencyContactName\", " +
                    "    primary_info.emergencyContactTelno AS \"emergencyContactPhone\", " +
                    "    primary_info.contAddress AS \"emergencyContactAddress\", " +
                    "    dept.deptChiName AS \"department\", " +
                    "    posi.positionName AS \"position\", " +
                    "    conTrial.salaryDate AS \"probationStartDate\", " +
                    "    conTrial.trialPeriod AS \"probationMonths\", " +
                    "    conFormal.contractDateS AS \"fixedTermStartDate\", " +
                    "    conFormal.contractDateE AS \"fixedTermEndDate\", " +
                    "    conFormal.years AS \"fixedTermYears\", " +
                    "    subinfo.subEmp AS \"subEmp\", " +
                    "    subinfo.subphone AS \"subPhone\" " +
                    "FROM primary_info " +
                    "INNER JOIN subinfo ON 1=1 " +
                    "LEFT JOIN conTrial ON primary_info.empNo = conTrial.empNo " +
                    "LEFT JOIN conFormal ON primary_info.empNo = conFormal.empNo " +
                    "LEFT JOIN dept ON conTrial.deptNo = dept.deptNo " +
                    "LEFT JOIN posi ON conTrial.position = posi.position";
            return DatabaseUtil.accessor().selectListByMpEnhanced(sql, employeeDTO, ContractVO.class);
        }
    }

    /**
     * 5. DTO 和 VO 定义：人事员工数据传输对象，用于电子合同查询
     */
    public static class HrEmployeeDTO implements Serializable {

        private static final long serialVersionUID = 1L;
        private String empNo;
        private String oid;

        public String getEmpNo() {
            return empNo;
        }

        public void setEmpNo(String empNo) {
            this.empNo = empNo;
        }

        public String getOid() {
            return oid;
        }

        public void setOid(String oid) {
            this.oid = oid;
        }

        @Override
        public String toString() {
            return "HrEmployeeDTO{" + "empNo='" + empNo + '\'' + ", oid='" + oid + '\'' + '}';
        }
    }

    /**
     * 5. DTO 和 VO 定义：电子合同VO
     */
    public static class ContractVO implements Serializable {

        private static final long serialVersionUID = 1L;
        private String empNo, partyA, partyB, phoneNumber, idCardNumber, contactPhone, contactAddress;
        private String emergencyContactName, emergencyContactPhone, emergencyContactAddress;
        private String department, position, probationStartDate, fixedTermStartDate, fixedTermEndDate;
        private int probationMonths, fixedTermYears;
        private String subEmp, subPhone;

        // Getters and Setters
        public String getEmpNo() {
            return empNo;
        }

        public void setEmpNo(String empNo) {
            this.empNo = empNo;
        }

        public String getPartyA() {
            return partyA;
        }

        public void setPartyA(String partyA) {
            this.partyA = partyA;
        }

        public String getPartyB() {
            return partyB;
        }

        public void setPartyB(String partyB) {
            this.partyB = partyB;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getIdCardNumber() {
            return idCardNumber;
        }

        public void setIdCardNumber(String idCardNumber) {
            this.idCardNumber = idCardNumber;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getContactAddress() {
            return contactAddress;
        }

        public void setContactAddress(String contactAddress) {
            this.contactAddress = contactAddress;
        }

        public String getEmergencyContactName() {
            return emergencyContactName;
        }

        public void setEmergencyContactName(String emergencyContactName) {
            this.emergencyContactName = emergencyContactName;
        }

        public String getEmergencyContactPhone() {
            return emergencyContactPhone;
        }

        public void setEmergencyContactPhone(String emergencyContactPhone) {
            this.emergencyContactPhone = emergencyContactPhone;
        }

        public String getEmergencyContactAddress() {
            return emergencyContactAddress;
        }

        public void setEmergencyContactAddress(String emergencyContactAddress) {
            this.emergencyContactAddress = emergencyContactAddress;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getProbationStartDate() {
            return probationStartDate;
        }

        public void setProbationStartDate(String probationStartDate) {
            this.probationStartDate = probationStartDate;
        }

        public int getProbationMonths() {
            return probationMonths;
        }

        public void setProbationMonths(int probationMonths) {
            this.probationMonths = probationMonths;
        }

        public String getFixedTermStartDate() {
            return fixedTermStartDate;
        }

        public void setFixedTermStartDate(String fixedTermStartDate) {
            this.fixedTermStartDate = fixedTermStartDate;
        }

        public String getFixedTermEndDate() {
            return fixedTermEndDate;
        }

        public void setFixedTermEndDate(String fixedTermEndDate) {
            this.fixedTermEndDate = fixedTermEndDate;
        }

        public int getFixedTermYears() {
            return fixedTermYears;
        }

        public void setFixedTermYears(int fixedTermYears) {
            this.fixedTermYears = fixedTermYears;
        }

        public String getSubEmp() {
            return subEmp;
        }

        public void setSubEmp(String subEmp) {
            this.subEmp = subEmp;
        }

        public String getSubPhone() {
            return subPhone;
        }

        public void setSubPhone(String subPhone) {
            this.subPhone = subPhone;
        }
    }

    /**
     * 5. DTO 和 VO 定义：通用返回结果类
     */
    public static class Result<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        private int code;
        private String message;
        private T data;

        public Result() {
        }

        public Result(int code, String message, T data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(200, "Success", data);
        }

        public static <T> Result<T> error(int code, String message) {
            return new Result<>(code, message, null);
        }

        // Getters and Setters
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
```
