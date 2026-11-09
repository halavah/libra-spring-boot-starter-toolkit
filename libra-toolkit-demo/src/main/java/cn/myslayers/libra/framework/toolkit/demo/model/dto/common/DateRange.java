package cn.myslayers.libra.framework.toolkit.demo.model.dto.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期范围通用DTO
 * 用于测试日期类型对象映射和动态SQL中的日期条件处理
 */
public class DateRange {
    
    private Date startDate;             // 开始日期
    private Date endDate;               // 结束日期
    private String startDateStr;        // 开始日期字符串
    private String endDateStr;          // 结束日期字符串
    private String timeZone;            // 时区
    private Boolean includeTime;        // 是否包含时间

    // 默认构造函数
    public DateRange() {
        this.includeTime = false;
        this.timeZone = "Asia/Shanghai";
    }

    // 便捷构造函数 - 使用Date对象
    public DateRange(Date startDate, Date endDate) {
        this();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 便捷构造函数 - 使用字符串
    public DateRange(String startDateStr, String endDateStr) {
        this();
        this.startDateStr = startDateStr;
        this.endDateStr = endDateStr;
        parseStringDates();
    }

    // 静态工厂方法 - 创建日期范围
    public static DateRange of(String startDate, String endDate) {
        return new DateRange(startDate, endDate);
    }

    // 静态工厂方法 - 本月
    public static DateRange ofThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        return new DateRange(startDate, endDate);
    }

    // 静态工厂方法 - 本季度
    public static DateRange ofThisQuarter() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int quarterStartMonth = (currentMonth / 3) * 3; // 0,3,6,9

        // 季度开始
        cal.set(Calendar.MONTH, quarterStartMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // 季度结束
        cal.add(Calendar.MONTH, 3);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        return new DateRange(startDate, endDate);
    }

    // 静态工厂方法 - 本年
    public static DateRange ofThisYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        return new DateRange(startDate, endDate);
    }

    // 解析字符串日期
    private void parseStringDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                this.startDate = sdf.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                this.endDate = sdf.parse(endDateStr);
            }
        } catch (ParseException e) {
            // 忽略解析错误，保持Date字段为null
        }
    }

    // 检查日期范围是否有效
    public boolean isValid() {
        return startDate != null && endDate != null && !startDate.after(endDate);
    }

    // 获取天数差
    public long getDaysBetween() {
        if (!isValid()) {
            return 0;
        }
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }

    // Getter 和 Setter 方法
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
        if (startDateStr != null) {
            parseStringDates();
        }
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
        if (endDateStr != null) {
            parseStringDates();
        }
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getIncludeTime() {
        return includeTime;
    }

    public void setIncludeTime(Boolean includeTime) {
        this.includeTime = includeTime;
    }

    @Override
    public String toString() {
        return "DateRange{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", startDateStr='" + startDateStr + '\'' +
                ", endDateStr='" + endDateStr + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", includeTime=" + includeTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateRange dateRange = (DateRange) o;

        if (startDate != null ? !startDate.equals(dateRange.startDate) : dateRange.startDate != null) return false;
        if (endDate != null ? !endDate.equals(dateRange.endDate) : dateRange.endDate != null) return false;
        if (startDateStr != null ? !startDateStr.equals(dateRange.startDateStr) : dateRange.startDateStr != null)
            return false;
        if (endDateStr != null ? !endDateStr.equals(dateRange.endDateStr) : dateRange.endDateStr != null)
            return false;
        if (timeZone != null ? !timeZone.equals(dateRange.timeZone) : dateRange.timeZone != null) return false;
        return includeTime != null ? includeTime.equals(dateRange.includeTime) : dateRange.includeTime == null;
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (startDateStr != null ? startDateStr.hashCode() : 0);
        result = 31 * result + (endDateStr != null ? endDateStr.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (includeTime != null ? includeTime.hashCode() : 0);
        return result;
    }
} 