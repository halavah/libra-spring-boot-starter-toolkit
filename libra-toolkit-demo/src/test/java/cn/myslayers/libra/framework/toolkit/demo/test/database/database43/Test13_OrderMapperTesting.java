package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test13_OrderMapperTesting - OrderMapper.xml 强类型测试")
public class Test13_OrderMapperTesting {

    // ==================== DTO/VO/Entity 内部定义 ====================

    public static class Order {
        private Long orderId;
        private String orderNo;
        private String customerName;
        private String customerPhone;
        private BigDecimal finalAmount;
        private String status;
        private Date orderDate;

        // Getters & Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getOrderDate() { return orderDate; }
        public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

        @Override
        public String toString() { return JSON.toJSONString(this); }
    }

    public static class OrderItem {
        private Long itemId;
        private Long orderId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String productCategory;

        // Getters & Setters
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public String getProductCategory() { return productCategory; }
        public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    }

    public static class OrderDetailVO extends Order {
        private List<OrderItem> items;
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }

    public static class OrderSubmitDTO extends Order {
        private String customerEmail;
        private Address shippingAddress;
        private List<OrderItem> items;
        private BigDecimal shippingFee;
        private BigDecimal discountAmount;
        private String paymentMethod;
        private String paymentStatus;
        private String shippingMethod;
        private String remarks;

        // Getters & Setters
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String email) { this.customerEmail = email; }
        public Address getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(Address address) { this.shippingAddress = address; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal fee) { this.shippingFee = fee; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal amount) { this.discountAmount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String method) { this.paymentMethod = method; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String status) { this.paymentStatus = status; }
        public String getShippingMethod() { return shippingMethod; }
        public void setShippingMethod(String method) { this.shippingMethod = method; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class Address {
        private String province;
        private String city;
        private String district;
        private String detailAddress;

        // Getters & Setters
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test13_OrderMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `orders`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `order_item`;");

        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
            "order_id BIGINT PRIMARY KEY AUTO_INCREMENT, order_no VARCHAR(255), customer_id BIGINT, customer_name VARCHAR(255), " +
            "customer_phone VARCHAR(255), customer_email VARCHAR(255), total_amount DECIMAL(10, 2), discount_amount DECIMAL(10, 2), " +
            "shipping_fee DECIMAL(10, 2), final_amount DECIMAL(10, 2), status VARCHAR(50), payment_status VARCHAR(50), " +
            "payment_method VARCHAR(50), order_date TIMESTAMP, pay_date TIMESTAMP, ship_date TIMESTAMP, delivery_date TIMESTAMP, " +
            "shipping_address VARCHAR(1000), shipping_method VARCHAR(50), tracking_number VARCHAR(255), remarks VARCHAR(1000), " +
            "created_time TIMESTAMP, updated_time TIMESTAMP);";
        String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_item (" +
            "item_id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT, product_name VARCHAR(255), product_sku VARCHAR(255), " +
            "quantity INT, unit_price DECIMAL(10, 2), total_price DECIMAL(10, 2), product_category VARCHAR(255));";

        DatabaseUtil43.executeDDL(createOrdersTable);
        DatabaseUtil43.executeDDL(createOrderItemsTable);
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `orders`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `order_item`;");

        System.out.println("=== Test13_OrderMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        DatabaseUtil43.executeDML("DELETE FROM orders;");
        DatabaseUtil43.executeDML("DELETE FROM order_item;");
        DatabaseUtil43.executeDML("INSERT INTO orders (order_id, order_no, customer_name, final_amount, status, order_date, tracking_number) VALUES " +
            "(1, 'ORD001', '张三', 150.00, 'DELIVERED', '2023-10-01', NULL)," +
            "(2, 'ORD002', '李四', 200.50, 'SHIPPED', '2023-10-02', NULL);");
        DatabaseUtil43.executeDML("INSERT INTO order_item (item_id, order_id, product_name, quantity, unit_price, total_price) VALUES " +
            "(1, 1, '商品A', 1, 150.00, 150.00)," +
            "(2, 2, '商品B', 2, 100.25, 200.50);");
    }

    // ==================== 测试方法 ====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("insertOrder & batchInsertOrderItems - 创建订单和订单项")
    void testInsertOrderAndItems() {
        System.out.println("\n--- [Order 1] 测试 insertOrder & batchInsertOrderItems ---");
        OrderSubmitDTO newOrder = new OrderSubmitDTO();
        newOrder.setOrderNo("ORD003");
        newOrder.setCustomerName("王五");
        newOrder.setCustomerPhone("13800138002");
        newOrder.setOrderDate(new Date());
        newOrder.setFinalAmount(new BigDecimal("399.90"));
        newOrder.setStatus("PENDING");
        newOrder.setPaymentStatus("PENDING");
        Address addr = new Address();
        addr.setProvince("浙江");
        addr.setCity("杭州");
        addr.setDistrict("西湖区");
        addr.setDetailAddress("文三路");
        newOrder.setShippingAddress(addr);

        // 1. 插入订单
        // 注意：H2的useGeneratedKeys可能不直接通过此API返回，我们通过查询来验证
        int orderResult = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("insertOrder", newOrder);
        System.out.println("验证订单插入影响行数是否为1: " + (orderResult == 1));

        // 通过OrderNo查回刚插入的订单以获取ID
        Order insertedOrder = DatabaseUtil43.selectOneByMpEnhanced("SELECT * FROM orders WHERE order_no = #{value}", "ORD003", Order.class);
        System.out.println("验证新插入订单是否能查到: " + (insertedOrder != null));
        Long newOrderId = insertedOrder.getOrderId();
        System.out.println("新订单ID: " + newOrderId);

        // 2. 批量插入订单项
        OrderItem item1 = new OrderItem();
        item1.setProductName("高端键盘");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("399.90"));
        item1.setTotalPrice(new BigDecimal("399.90"));

        Map<String, Object> itemParams = new HashMap<>();
        itemParams.put("orderId", newOrderId);
        itemParams.put("items", Collections.singletonList(item1));

        int itemResult = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("batchInsertOrderItems", itemParams);
        System.out.println("批量插入订单项影响行数: " + itemResult);
        System.out.println("验证订单项插入影响行数是否为1: " + (itemResult == 1));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("selectOrderById - 根据ID查询订单")
    void testSelectOrderById() {
        System.out.println("\n--- [Order 2] 测试 selectOrderById ---");
        Order order = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectOrderById", 1L, Order.class);
        System.out.println("查询结果: " + order);
        System.out.println("验证查询结果是否非空: " + (order != null));
        if (order != null) {
            System.out.println("验证订单号是否为'ORD001': " + "ORD001".equals(order.getOrderNo()));
        }
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("updateOrder - 更新订单状态")
    void testUpdateOrder() {
        System.out.println("\n--- [Order 3] 测试 updateOrder ---");
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", 2L);
        params.put("status", "DELIVERED");
        params.put("trackingNumber", "SF123456789");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("updateOrder", params);
        System.out.println("更新影响行数: " + result);
        System.out.println("验证更新影响行数是否为1: " + (result == 1));

        Order updatedOrder = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectOrderById", 2L, Order.class);
        System.out.println("更新后查询结果: " + updatedOrder);
        if (updatedOrder != null) {
            System.out.println("验证更新后状态是否为'DELIVERED': " + "DELIVERED".equals(updatedOrder.getStatus()));
        }
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("selectOrderList - 多条件查询订单列表")
    void testSelectOrderList() {
        System.out.println("\n--- [Order 4] 测试 selectOrderList ---");
        OrderSubmitDTO query = new OrderSubmitDTO();
        query.setStatus("DELIVERED");

        List<OrderDetailVO> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectOrderList", query, OrderDetailVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为1: " + (result != null && result.size() == 1));
        if (result != null && !result.isEmpty()) {
            System.out.println("验证查询结果订单号是否为'ORD001': " + "ORD001".equals(result.get(0).getOrderNo()));
        }
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("getOrderStatistics - 获取订单统计信息")
    void testGetOrderStatistics() {
        System.out.println("\n--- [Order 5] 测试 getOrderStatistics ---");
        Map<String, Object> stats = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("getOrderStatistics", null, Map.class);
        System.out.println("统计结果: " + JSON.toJSONString(stats));
        System.out.println("验证统计结果是否非空: " + (stats != null));
        if (stats != null) {
            System.out.println("验证总订单数是否为2: " + Long.valueOf(2L).equals(stats.get("total_orders")));
            System.out.println("验证已送达订单数是否为1: " + Long.valueOf(1L).equals(stats.get("delivered_orders")));
        }
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("deleteOrder - 删除订单（软删除）")
    void testDeleteOrder() {
        System.out.println("\n--- [Order 6] 测试 deleteOrder ---");
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", 2L);
        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("deleteOrder", params);
        System.out.println("删除影响行数: " + result);
        System.out.println("验证删除影响行数是否为1: " + (result == 1));

        Order deletedOrder = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectOrderById", 2L, Order.class);
        System.out.println("删除后查询结果: " + deletedOrder);
        if (deletedOrder != null) {
            System.out.println("验证删除后状态是否为'CANCELLED': " + "CANCELLED".equals(deletedOrder.getStatus()));
        }
    }
}
