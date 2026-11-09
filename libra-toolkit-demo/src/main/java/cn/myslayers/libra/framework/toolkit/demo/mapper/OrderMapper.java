package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.OrderSubmitDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Order;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.OrderDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 * 对应 OrderMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface OrderMapper {

    /**
     * 根据ID查询订单基础信息
     * @param orderId 订单ID
     * @return 订单实体
     */
    Order selectOrderById(Long orderId);

    /**
     * 查询订单详细信息（包含订单项）
     * @param orderId 订单ID
     * @return 订单详情VO
     */
    OrderDetailVO selectOrderDetail(Long orderId);

    /**
     * 订单列表查询（支持多条件过滤和分页）
     * @param orderSubmitDTO 订单提交DTO
     * @return 订单详情VO列表
     */
    List<OrderDetailVO> selectOrderList(OrderSubmitDTO orderSubmitDTO);

    /**
     * 创建订单
     * @param order 订单实体
     * @return 影响行数
     */
    int insertOrder(Order order);

    /**
     * 批量插入订单项
     * @param orderItems 订单项列表
     * @return 影响行数
     */
    int batchInsertOrderItems(List<Map<String, Object>> orderItems);

    /**
     * 更新订单信息
     * @param order 订单实体
     * @return 影响行数
     */
    int updateOrder(Order order);

    /**
     * 批量更新订单状态
     * @param status 状态
     * @param orderIds 订单ID列表
     * @return 影响行数
     */
    int batchUpdateOrderStatus(@Param("status") String status, @Param("orderIds") List<Long> orderIds);

    /**
     * 更新订单支付状态
     * @param orderId 订单ID
     * @param paymentStatus 支付状态
     * @param payDate 支付日期
     * @return 影响行数
     */
    int updatePaymentStatus(@Param("orderId") Long orderId, @Param("paymentStatus") String paymentStatus, @Param("payDate") String payDate);

    /**
     * 删除订单（软删除）
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteOrder(Long orderId);

    /**
     * 物理删除订单
     * @param orderId 订单ID
     * @return 影响行数
     */
    int physicalDeleteOrder(Long orderId);

    /**
     * 订单统计分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果Map列表
     */
    List<Map<String, Object>> getOrderStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 按天统计订单销售趋势
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售趋势Map列表
     */
    List<Map<String, Object>> getOrderSalesTrendByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 按月统计订单销售趋势
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 销售趋势Map列表
     */
    List<Map<String, Object>> getOrderSalesTrendByMonth(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    /**
     * 商品销售排行分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制条数
     * @return 商品销售排行Map列表
     */
    List<Map<String, Object>> getProductSalesRanking(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") Integer limit);

    /**
     * 客户消费分析
     * @param customerId 客户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 客户消费分析Map列表
     */
    List<Map<String, Object>> getCustomerConsumptionAnalysis(@Param("customerId") Long customerId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 订单状态流转分析
     * @param orderId 订单ID
     * @return 状态流转分析Map列表
     */
    List<Map<String, Object>> getOrderStatusFlowAnalysis(@Param("orderId") Long orderId);

    /**
     * 配送效率分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 配送效率分析Map列表
     */
    List<Map<String, Object>> getShippingEfficiencyAnalysis(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 订单完整报表
     * @param orderId 订单ID
     * @return 订单完整报表Map
     */
    Map<String, Object> selectOrderCompleteReport(Long orderId);
}