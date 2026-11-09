package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单提交DTO
 * 用于测试订单主从表提交、复杂业务逻辑等场景
 */
public class OrderSubmitDTO {

    private Long customerId;            // 客户ID
    private String customerName;        // 客户姓名
    private String customerPhone;       // 客户电话
    private String paymentMethod;       // 支付方式
    private BigDecimal discountAmount;  // 折扣金额
    private Address shippingAddress;    // 收货地址（嵌套对象）
    private String remarks;             // 订单备注
    private List<OrderItemData> items;  // 订单明细列表

    // 内部类：订单明细数据
    public static class OrderItemData {
        private String productCode;     // 商品编码
        private String productName;     // 商品名称
        private String specification;   // 规格
        private BigDecimal unitPrice;   // 单价
        private Integer quantity;       // 数量

        public OrderItemData() {
        }

        public OrderItemData(String productCode, String productName, BigDecimal unitPrice, Integer quantity) {
            this.productCode = productCode;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        // Getter 和 Setter
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
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "OrderItemData{" +
                    "productCode='" + productCode + '\'' +
                    ", productName='" + productName + '\'' +
                    ", specification='" + specification + '\'' +
                    ", unitPrice=" + unitPrice +
                    ", quantity=" + quantity +
                    '}';
        }
    }

    // 默认构造函数
    public OrderSubmitDTO() {
    }

    // 便捷构造函数
    public OrderSubmitDTO(Long customerId, String customerName, List<OrderItemData> items) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
    }

    // Getter 和 Setter 方法
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
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

    public List<OrderItemData> getItems() {
        return items;
    }

    public void setItems(List<OrderItemData> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OrderSubmitDTO{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", discountAmount=" + discountAmount +
                ", shippingAddress=" + shippingAddress +
                ", remarks='" + remarks + '\'' +
                ", items=" + items +
                '}';
    }
}