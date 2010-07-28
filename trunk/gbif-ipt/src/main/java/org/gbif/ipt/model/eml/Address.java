package org.gbif.ipt.model.eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * This class is used to represent an address with address, city, province and
 * postal-code information.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class Address implements Serializable {
  private static final long serialVersionUID = 3617859655330969141L;
  private String address;
  private String city;
  private String province;
  private String country;
  private String postalCode;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Address)) {
      return false;
    }
    Address o = (Address) other;
    return equal(address, o.address) && equal(city, o.city)
        && equal(province, o.province) && equal(country, o.country)
        && equal(postalCode, o.postalCode);
  }

  public String getAddress() {
    return address;
  }

  public String getCity() {
    if(city==null || city.length()==0) return null;
    return city;
  }

  public String getCountry() {
    if(country==null || country.length()==0) return null;
    return country;
  }

  public String getPostalCode() {
    if(postalCode==null || postalCode.length()==0) return null;
    return postalCode;
  }

  public String getProvince() {
    if(province==null || province.length()==0) return null;
    return province;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(address, city, province, country, postalCode);
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  /**
   * Returns a multi-line String with key=value pairs.
   * 
   * @return a String representation of this class.
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append(
        "country", this.country).append("address", this.address).append(
        "province", this.province).append("postalCode", this.postalCode).append(
        "city", this.city).toString();
  }
}
