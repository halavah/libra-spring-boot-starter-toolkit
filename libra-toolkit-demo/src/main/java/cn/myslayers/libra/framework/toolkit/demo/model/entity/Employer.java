package cn.myslayers.libra.framework.toolkit.demo.model.entity;

// 雇主信息实体类
public class Employer {

    // 公司ID
    private String id;
    // 公司名称
    private String name;
    // 所属行业
    private String industry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    @Override
    public String toString() {
        return "Employer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                '}';
    }
}
