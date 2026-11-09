package cn.myslayers.libra.framework.toolkit.demo.model.dto.common;

/**
 * 分页查询通用DTO
 * 用于测试分页参数映射和处理
 */
public class PageQuery {

    private Integer pageNum = 1;        // 页码，默认第1页
    private Integer pageSize = 10;      // 每页大小，默认10条
    private String orderBy;             // 排序字段
    private String orderDirection = "ASC"; // 排序方向，默认升序
    private Boolean countTotal = true;  // 是否统计总数，默认统计

    // 默认构造函数
    public PageQuery() {
    }

    // 便捷构造函数
    public PageQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
    }

    // 完整构造函数
    public PageQuery(Integer pageNum, Integer pageSize, String orderBy, String orderDirection) {
        this(pageNum, pageSize);
        this.orderBy = orderBy;
        this.orderDirection = orderDirection != null ? orderDirection : "ASC";
    }

    // 便捷方法：获取偏移量
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    // 便捷方法：检查分页参数是否有效
    public boolean isValid() {
        return pageNum != null && pageNum > 0 && pageSize != null && pageSize > 0;
    }

    // 便捷方法：标准化分页参数
    public void normalize() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 1000) { // 限制最大分页大小
            pageSize = 1000;
        }
        if (orderDirection == null || (!orderDirection.equalsIgnoreCase("ASC") && !orderDirection.equalsIgnoreCase("DESC"))) {
            orderDirection = "ASC";
        }
    }

    // 静态工厂方法
    public static PageQuery of(int pageNum, int pageSize) {
        return new PageQuery(pageNum, pageSize);
    }

    public static PageQuery of(int pageNum, int pageSize, String orderBy) {
        return new PageQuery(pageNum, pageSize, orderBy, "ASC");
    }

    public static PageQuery of(int pageNum, int pageSize, String orderBy, String orderDirection) {
        return new PageQuery(pageNum, pageSize, orderBy, orderDirection);
    }

    // Getter 和 Setter 方法
    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    public Boolean getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(Boolean countTotal) {
        this.countTotal = countTotal;
    }

    @Override
    public String toString() {
        return "PageQuery{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", orderBy='" + orderBy + '\'' +
                ", orderDirection='" + orderDirection + '\'' +
                ", countTotal=" + countTotal +
                ", offset=" + getOffset() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageQuery pageQuery = (PageQuery) o;

        if (pageNum != null ? !pageNum.equals(pageQuery.pageNum) : pageQuery.pageNum != null) return false;
        if (pageSize != null ? !pageSize.equals(pageQuery.pageSize) : pageQuery.pageSize != null) return false;
        if (orderBy != null ? !orderBy.equals(pageQuery.orderBy) : pageQuery.orderBy != null) return false;
        if (orderDirection != null ? !orderDirection.equals(pageQuery.orderDirection) : pageQuery.orderDirection != null)
            return false;
        return countTotal != null ? countTotal.equals(pageQuery.countTotal) : pageQuery.countTotal == null;
    }

    @Override
    public int hashCode() {
        int result = pageNum != null ? pageNum.hashCode() : 0;
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
        result = 31 * result + (orderDirection != null ? orderDirection.hashCode() : 0);
        result = 31 * result + (countTotal != null ? countTotal.hashCode() : 0);
        return result;
    }
}