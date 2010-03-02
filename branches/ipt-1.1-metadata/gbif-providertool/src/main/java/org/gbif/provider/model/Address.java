package org.gbif.provider.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class is used to represent an address with address,
 * city, province and postal-code information.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
@Embeddable
/**
 * TODO: Documentation.
 * 
 */
public class Address implements Serializable {
    private static final long serialVersionUID = 3617859655330969141L;
    private String address;
    private String city;
    private String province;
    private String country;
    private String postalCode;

    @Column(length=150)
    public String getAddress() {
        return address;
    }

    @Column(length=50)
    public String getCity() {
        return city;
    }

    @Column(length=100)
    public String getProvince() {
        return province;
    }

    @Column(length=100)
    public String getCountry() {
        return country;
    }

    @Column(name="postal_code",length=15)
    public String getPostalCode() {
        return postalCode;
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
     * Overridden equals method for object comparison. Compares based on hashCode.
     * @param o Object to compare
     * @return true/false based on hashCode
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }

        final Address address1 = (Address) o;

        return this.hashCode() == address1.hashCode();
    }

    /**
     * Overridden hashCode method - compares on address, city, province, country and postal code.
     * @return hashCode
     */
    public int hashCode() {
        int result;
        result = (address != null ? address.hashCode() : 0);
        result = 29 * result + (city != null ? city.hashCode() : 0);
        result = 29 * result + (province != null ? province.hashCode() : 0);
        result = 29 * result + (country != null ? country.hashCode() : 0);
        result = 29 * result + (postalCode != null ? postalCode.hashCode() : 0);
        return result;
    }

    /**
     * Returns a multi-line String with key=value pairs.
     * @return a String representation of this class.
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("country", this.country)
                .append("address", this.address)
                .append("province", this.province)
                .append("postalCode", this.postalCode)
                .append("city", this.city).toString();
    }
}
