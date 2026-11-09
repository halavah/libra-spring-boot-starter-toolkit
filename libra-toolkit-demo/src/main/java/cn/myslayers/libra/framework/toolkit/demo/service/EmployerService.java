package cn.myslayers.libra.framework.toolkit.demo.service;

import cn.myslayers.libra.framework.toolkit.demo.mapper.EmployerMapper;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Employer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 雇主业务逻辑类（合并后的单一实现）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerMapper employerMapper = new EmployerMapper();

    /**
     * 获取所有雇主信息
     *
     * @return 所有雇主列表
     */
    public List<Employer> getAllEmployers() {
        return employerMapper.findAll();
    }

    /**
     * 根据ID获取单个雇主信息
     *
     * @param id 雇主ID
     * @return 雇主对象，如果不存在则返回null
     */
    public Employer getEmployerById(String id) {
        return employerMapper.findById(id);
    }

    /**
     * 创建新雇主
     *
     * @param employer 要创建的雇主对象
     * @return 创建后的雇主对象（包含生成的ID）
     */
    public Employer createEmployer(Employer employer) {
        // 自动生成UUID作为主键
        employer.setId(UUID.randomUUID().toString());
        employerMapper.insert(employer);
        return employer;
    }

    /**
     * 更新雇主信息
     *
     * @param id             雇主ID
     * @param employerDetails 更新的雇主信息
     * @return 更新后的雇主对象，如果ID不存在则返回null
     */
    public Employer updateEmployer(String id, Employer employerDetails) {
        Employer existingEmployer = employerMapper.findById(id);
        if (existingEmployer != null) {
            // 更新需要修改的字段
            if (employerDetails.getName() != null) {
                existingEmployer.setName(employerDetails.getName());
            }
            if (employerDetails.getIndustry() != null) {
                existingEmployer.setIndustry(employerDetails.getIndustry());
            }
            employerMapper.update(existingEmployer);
            return existingEmployer;
        }
        return null;
    }

    /**
     * 删除雇主
     *
     * @param id 雇主ID
     * @return 是否删除成功
     */
    public boolean deleteEmployer(String id) {
        return employerMapper.deleteById(id) > 0;
    }
}
