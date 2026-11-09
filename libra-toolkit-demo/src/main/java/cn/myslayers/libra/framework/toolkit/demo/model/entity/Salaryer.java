package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

// 薪资信息实体类
public class Salaryer {

    // 薪资记录ID
    private String id;
    // 用户ID
    private String userId;
    // 薪资数额
    private BigDecimal amount;
    // 发薪日期
    private LocalDate payDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    @Override
    public String toString() {
        return "Salary{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", payDate=" + payDate +
                '}';
    }
}
