package cn.myslayers.libra.framework.toolkit.demo.service;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import cn.myslayers.libra.framework.toolkit.demo.mapper.SalaryerMapper;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Salaryer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 薪资业务逻辑类（合并后的单一实现）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryerService {

    private final SalaryerMapper salaryerMapper = new SalaryerMapper();

    /**
     * 根据用户ID获取薪资列表
     *
     * @param userId 用户ID
     * @return 薪资列表
     */
    public List<Salaryer> getSalariesByUserId(String userId) {
        return salaryerMapper.findByUserId(userId);
    }

    /**
     * 发放薪水
     *
     * @param salary 薪资对象
     * @return 创建后的薪资对象（包含生成的ID）
     */
    public Salaryer issueSalary(Salaryer salary) {
        // 使用链式访问器来处理事务
        DatabaseUtil.DatabaseAccessor accessor = DatabaseUtil.accessor();
        try {
            // 1. 开启事务
            accessor.setTransaction();

            // 2. 生成ID并插入薪资记录
            String id = UUID.randomUUID().toString();
            salary.setId(id);
            accessor.executeFromXmlAutoByMpEnhanced("insertSalary", salary);

            // 3. (示例) 更新用户余额等其他操作...
            // accessor.executeFromXmlAutoByMpEnhanced("updateUserBalance", ...);

            // 4. 提交事务
            accessor.commit();

            return salary;
        } catch (Exception e) {
            // 5. 如果发生任何异常，执行回滚
            accessor.rollback();
            // 向上抛出异常，让Controller层知道操作失败
            throw new RuntimeException("发薪失败，事务已回滚", e);
        }
    }

    /**
     * 删除薪资记录
     *
     * @param id 薪资ID
     * @return 是否删除成功
     */
    public boolean deleteSalary(String id) {
        return salaryerMapper.deleteById(id) > 0;
    }
}
