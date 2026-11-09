package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 报表数据VO
 * 用于测试复杂报表数据展示、统计聚合结果等场景
 */
public class ReportDataVO {

    private String reportType;          // 报表类型
    private String title;               // 报表标题
    private String description;         // 报表描述
    private String period;              // 统计期间
    private Map<String, Object> summary; // 汇总信息
    private List<Map<String, Object>> data; // 明细数据
    private List<ChartData> charts;     // 图表数据
    private StatisticsSummary statistics; // 统计汇总

    // 内部类：图表数据
    public static class ChartData {
        private String type;            // 图表类型：bar, line, pie
        private String title;           // 图表标题
        private List<String> labels;    // 标签列表
        private List<BigDecimal> values; // 数值列表
        private Map<String, Object> options; // 图表选项

        public ChartData() {
        }

        public ChartData(String type, String title, List<String> labels, List<BigDecimal> values) {
            this.type = type;
            this.title = title;
            this.labels = labels;
            this.values = values;
        }

        // Getter 和 Setter
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<BigDecimal> getValues() {
            return values;
        }

        public void setValues(List<BigDecimal> values) {
            this.values = values;
        }

        public Map<String, Object> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Object> options) {
            this.options = options;
        }

        @Override
        public String toString() {
            return "ChartData{" +
                    "type='" + type + '\'' +
                    ", title='" + title + '\'' +
                    ", labels=" + labels +
                    ", values=" + values +
                    ", options=" + options +
                    '}';
        }
    }

    // 内部类：统计汇总
    public static class StatisticsSummary {
        private Integer totalCount;      // 总记录数
        private BigDecimal totalAmount;  // 总金额
        private BigDecimal avgAmount;    // 平均金额
        private BigDecimal maxAmount;    // 最大金额
        private BigDecimal minAmount;    // 最小金额
        private Map<String, BigDecimal> groupSummary; // 分组汇总

        public StatisticsSummary() {
        }

        // Getter 和 Setter
        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getAvgAmount() {
            return avgAmount;
        }

        public void setAvgAmount(BigDecimal avgAmount) {
            this.avgAmount = avgAmount;
        }

        public BigDecimal getMaxAmount() {
            return maxAmount;
        }

        public void setMaxAmount(BigDecimal maxAmount) {
            this.maxAmount = maxAmount;
        }

        public BigDecimal getMinAmount() {
            return minAmount;
        }

        public void setMinAmount(BigDecimal minAmount) {
            this.minAmount = minAmount;
        }

        public Map<String, BigDecimal> getGroupSummary() {
            return groupSummary;
        }

        public void setGroupSummary(Map<String, BigDecimal> groupSummary) {
            this.groupSummary = groupSummary;
        }

        @Override
        public String toString() {
            return "StatisticsSummary{" +
                    "totalCount=" + totalCount +
                    ", totalAmount=" + totalAmount +
                    ", avgAmount=" + avgAmount +
                    ", maxAmount=" + maxAmount +
                    ", minAmount=" + minAmount +
                    ", groupSummary=" + groupSummary +
                    '}';
        }
    }

    // 默认构造函数
    public ReportDataVO() {
    }

    // Getter 和 Setter 方法
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Object> summary) {
        this.summary = summary;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<ChartData> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartData> charts) {
        this.charts = charts;
    }

    public StatisticsSummary getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticsSummary statistics) {
        this.statistics = statistics;
    }

    @Override
    public String toString() {
        return "ReportDataVO{" +
                "reportType='" + reportType + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", period='" + period + '\'' +
                ", summary=" + summary +
                ", data=" + (data != null ? data.size() + " records" : "null") +
                ", charts=" + (charts != null ? charts.size() + " charts" : "null") +
                ", statistics=" + statistics +
                '}';
    }
}