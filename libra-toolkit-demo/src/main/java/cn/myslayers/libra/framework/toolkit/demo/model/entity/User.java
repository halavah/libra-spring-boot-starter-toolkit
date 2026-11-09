package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import org.apache.ibatis.type.Alias;

// 用户信息实体类
@Alias("ToolkitUser")
public class User {

    // 用户ID
    private String id;
    // 用户姓名
    private String name;
    // 用户邮箱
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
