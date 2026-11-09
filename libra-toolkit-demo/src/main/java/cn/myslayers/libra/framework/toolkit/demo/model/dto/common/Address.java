package cn.myslayers.libra.framework.toolkit.demo.model.dto.common;

/**
 * 地址信息通用DTO
 * 用于测试嵌套对象映射功能
 */
public class Address {

    private String country = "中国";     // 国家，默认值中国
    private String province;            // 省份
    private String city;                // 城市
    private String district;            // 区县
    private String street;              // 街道
    private String detailAddress;       // 详细地址
    private String postalCode;          // 邮政编码
    private Double latitude;            // 纬度
    private Double longitude;           // 经度

    // 默认构造函数
    public Address() {
    }

    // 便捷构造函数 - 基础地址信息
    public Address(String province, String city, String district, String detailAddress) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
    }

    // 全参数构造函数
    public Address(String country, String province, String city, String district, String street,
                  String detailAddress, String postalCode, Double latitude, Double longitude) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.detailAddress = detailAddress;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 地址格式化方法
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (country != null && !country.isEmpty()) {
            sb.append(country);
        }
        if (province != null && !province.isEmpty()) {
            sb.append(province);
        }
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (district != null && !district.isEmpty()) {
            sb.append(district);
        }
        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }
        if (detailAddress != null && !detailAddress.isEmpty()) {
            sb.append(detailAddress);
        }
        return sb.toString();
    }

    // 检查地址是否完整
    public boolean isComplete() {
        return province != null && !province.isEmpty() &&
               city != null && !city.isEmpty() &&
               district != null && !district.isEmpty() &&
               detailAddress != null && !detailAddress.isEmpty();
    }

    // Getter 和 Setter 方法
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Address{" +
                "country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", street='" + street + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (country != null ? !country.equals(address.country) : address.country != null) return false;
        if (province != null ? !province.equals(address.province) : address.province != null) return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (district != null ? !district.equals(address.district) : address.district != null) return false;
        if (street != null ? !street.equals(address.street) : address.street != null) return false;
        if (detailAddress != null ? !detailAddress.equals(address.detailAddress) : address.detailAddress != null)
            return false;
        if (postalCode != null ? !postalCode.equals(address.postalCode) : address.postalCode != null) return false;
        if (latitude != null ? !latitude.equals(address.latitude) : address.latitude != null) return false;
        return longitude != null ? longitude.equals(address.longitude) : address.longitude == null;
    }

    @Override
    public int hashCode() {
        int result = country != null ? country.hashCode() : 0;
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (detailAddress != null ? detailAddress.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        return result;
    }
}