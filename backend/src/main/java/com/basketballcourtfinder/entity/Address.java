package com.basketballcourtfinder.entity;

import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String house_number;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postcode;

    public Address(OverpassResponse.Element element) {
        this.house_number = element.getTag("addr:housenumber");
        this.street = element.getTag("addr:street");
        this.city = element.getTag("addr:city");
        this.state = element.getTag("addr:province") != null ? element.getTag("addr:province") : element.getTag("addr:state");
        this.country = element.getTag("addr:country");
        this.postcode = element.getTag("addr:postcode");
    }

    /*
     * Checks if an address is incomplete
     * */
    public boolean isIncomplete() {
        return  Objects.equals(this.house_number, "") ||
                Objects.equals(this.city, "") || Objects.equals(this.street, "") ||
                Objects.equals(this.postcode, "");
    }
}
