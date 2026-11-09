package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单明细实体类
 * 用于测试主从表关联、明细数据处理等场景
 */
public class OrderItem {

    private Long id;                    // 明细ID
    private Long orderId;               // 订单ID
    private String productCode;         // 商品编码
    private String productName;         // 商品名称
    private String specification;       // 规格
    private BigDecimal unitPrice;       // 单价
    private Integer quantity;           // 数量
    private BigDecimal totalPrice;      // 小计
    private BigDecimal discountAmount;  // 明细折扣
    private String status;              // 明细状态
    private Date createdTime;           // 创建时间

    // 默认构造函数
    public OrderItem() {
    }

    // 便捷构造函数
    public OrderItem(Long orderId, String productCode, String productName, BigDecimal unitPrice, Integer quantity) {
        this.orderId = orderId;
        this.productCode = productCode;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        // 自动计算小计
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        // 重新计算小计
        recalculateTotalPrice();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // 重新计算小计
        recalculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    // 业务方法：重新计算小计
    private void recalculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }

    // 业务方法：获取实际金额（扣除折扣后）
    public BigDecimal getActualAmount() {
        BigDecimal actual = totalPrice != null ? totalPrice : BigDecimal.ZERO;
        if (discountAmount != null) {
            actual = actual.subtract(discountAmount);
        }
        return actual;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", specification='" + specification + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", discountAmount=" + discountAmount +
                ", status='" + status + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItem orderItem = (OrderItem) o;

        return id != null ? id.equals(orderItem.id) : orderItem.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}