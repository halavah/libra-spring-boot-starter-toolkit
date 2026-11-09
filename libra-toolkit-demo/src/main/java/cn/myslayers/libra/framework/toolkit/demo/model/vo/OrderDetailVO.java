package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单详情VO
 * 用于测试主从表结果组装、订单详情展示等场景
 */
public class OrderDetailVO {

    private Long id;                    // 订单ID
    private String orderNo;             // 订单号
    private String customerName;        // 客户姓名
    private String customerPhone;       // 客户电话
    private BigDecimal totalAmount;     // 订单总额
    private BigDecimal discountAmount;  // 折扣金额
    private BigDecimal actualAmount;    // 实付金额
    private String status;              // 状态
    private String paymentStatus;       // 支付状态
    private String paymentMethod;       // 支付方式
    private Date orderDate;             // 下单时间
    private Date payDate;               // 支付时间
    private Date shipDate;              // 发货时间
    private Date deliveryDate;          // 收货时间
    private Address shippingAddress;    // 收货地址（嵌套对象）
    private String remarks;             // 订单备注
    private List<OrderItemDetail> items; // 订单明细列表

    // 内部类：订单明细详情
    public static class OrderItemDetail {
        private Long id;                // 明细ID
        private String productCode;     // 商品编码
        private String productName;     // 商品名称
        private String specification;   // 规格
        private BigDecimal unitPrice;   // 单价
        private Integer quantity;       // 数量
        private BigDecimal totalPrice;  // 小计
        private BigDecimal discountAmount; // 明细折扣
        private String status;          // 明细状态

        public OrderItemDetail() {
        }

        public OrderItemDetail(String productCode, String productName, BigDecimal unitPrice, Integer quantity) {
            this.productCode = productCode;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        // Getter 和 Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
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

        @Override
        public String toString() {
            return "OrderItemDetail{" +
                    "id=" + id +
                    ", productCode='" + productCode + '\'' +
                    ", productName='" + productName + '\'' +
                    ", specification='" + specification + '\'' +
                    ", unitPrice=" + unitPrice +
                    ", quantity=" + quantity +
                    ", totalPrice=" + totalPrice +
                    ", discountAmount=" + discountAmount +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    // 默认构造函数
    public OrderDetailVO() {
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

    public List<OrderItemDetail> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetail> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OrderDetailVO{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
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
                ", items=" + items +
                '}';
    }
}