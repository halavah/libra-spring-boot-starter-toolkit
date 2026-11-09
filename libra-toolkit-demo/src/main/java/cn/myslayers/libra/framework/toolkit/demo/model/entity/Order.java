package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 * 用于测试主从表结构、状态流转、电商场景等复杂业务
 */
public class Order {

    private Long id;                    // 订单ID
    private String orderNo;             // 订单号
    private Long customerId;            // 客户ID
    private String customerName;        // 客户姓名
    private String customerPhone;       // 客户电话
    private BigDecimal totalAmount;     // 订单总额
    private BigDecimal discountAmount;  // 折扣金额
    private BigDecimal actualAmount;    // 实付金额
    private String status;              // 状态：PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String paymentStatus;       // 支付状态：UNPAID, PAID, REFUNDED
    private String paymentMethod;       // 支付方式
    private Date orderDate;             // 下单时间
    private Date payDate;               // 支付时间
    private Date shipDate;              // 发货时间
    private Date deliveryDate;          // 收货时间
    private Address shippingAddress;    // 收货地址（嵌套对象）
    private String remarks;             // 订单备注
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间

    // 默认构造函数
    public Order() {
    }

    // 便捷构造函数
    public Order(String orderNo, Long customerId, String customerName) {
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", actualAmount=" + actualAmount +
                ", status='" + status + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", orderDate=" + orderDate +
                ", payDate=" + payDate +
                ", shipDate=" + shipDate +
                ", deliveryDate=" + deliveryDate +
                ", shippingAddress=" + shippingAddress +
                ", remarks='" + remarks + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return id != null ? id.equals(order.id) : order.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}