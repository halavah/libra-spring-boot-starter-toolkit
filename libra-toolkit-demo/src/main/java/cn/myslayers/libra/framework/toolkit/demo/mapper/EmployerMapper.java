package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.entity.Employer;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// 雇主数据访问接口
@Mapper
public class EmployerMapper {

    // 根据ID查询雇主
    public Employer findById(String id) {
        return DatabaseUtil.selectOneFromXmlByMpEnhanced("EmployerMapper.xml","findEmployerById", id, Employer.class);
    }

    // 查询所有雇主
    public List<Employer> findAll() {
        return DatabaseUtil.selectListFromXmlByMpEnhanced("EmployerMapper.xml","findAllEmployers", null, Employer.class);
    }

    // 新增雇主
    public int insert(Employer employer) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("EmployerMapper.xml","insertEmployer", employer);
    }

    // 更新雇主信息
    public int update(Employer employer) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("EmployerMapper.xml","updateEmployer", employer);
    }

    // 根据ID删除雇主
    public int deleteById(String id) {
        return DatabaseUtil.executeFromXmlByMpEnhanced("EmployerMapper.xml","deleteEmployerById", id);
    }
}
