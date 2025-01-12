package com.basketballcourtfinder.entity;

import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
public class BasketballCourt {
    @Id
    @Column(nullable = false)
    private long id;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    @Column(nullable = false)
    private String name;

    private int hoops;

    private String surface;

    @Embedded
    private Address address;

    private String amenity;

    private String website;

    private String leisure;

    private String opening_hours;

    private String phone;
    public BasketballCourt(OverpassResponse.Element element) {
        this.id = element.getId();
        if (element.getCenter() != null) {
            this.lat = element.getCenter().getLat();
            this.lon = element.getCenter().getLon();
        }
        this.name = element.getTag("name");
        this.hoops = Objects.equals(element.getTag("hoops"), "") ? 0 : Integer.parseInt(element.getTag("hoops"));
        this.surface = element.getTag("surface");
        this.address = new Address(element);
        this.amenity = element.getTag("amenity");
        this.website = element.getTag("website");
        this.leisure = element.getTag("leisure");
        this.opening_hours = element.getTag("opening_hours");
        this.phone = element.getTag("phone");
    }


    public BasketballCourt(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setAddress(Map<String, String> map ) { this.address = new Address(map); }
}

