package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.entity.Salaryer;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 薪资数据访问接口
public class SalaryerMapper {

    // 根据ID查询薪资记录
    public Salaryer findById(String id) {
        return DatabaseUtil.selectOneFromXmlByMpEnhanced("SalaryerMapper.xml","findSalaryById", id, Salaryer.class);
    }

    // 根据用户ID查询薪资记录
    public List<Salaryer> findByUserId(String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return DatabaseUtil.selectListFromXmlByMpEnhanced("SalaryerMapper.xml","findSalariesByUserId", params, Salaryer.class);
    }

    // 新增薪资记录
    public int insert(Salaryer salary) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("SalaryerMapper.xml","insertSalary", salary);
    }

    // 更新薪资记录
    public int update(Salaryer salary) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("SalaryerMapper.xml","updateSalary", salary);
    }

    // 根据ID删除薪资记录
    public int deleteById(String id) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("SalaryerMapper.xml","deleteSalaryById", id);
    }
}
