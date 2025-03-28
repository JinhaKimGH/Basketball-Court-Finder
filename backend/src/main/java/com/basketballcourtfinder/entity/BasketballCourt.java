package com.basketballcourtfinder.entity;

import com.basketballcourtfinder.jsonmapping.OverpassResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    private Integer hoops;

    private String surface;

    private Boolean indoor;

    @Min(1) @Max(3) // Don't know, none, chain, nylon
    private Integer netting;

    @Min(1 ) @Max(3) // Don't know, single, 1.5, double
    private Integer rim_type;

    private Float rim_height;

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
        this.hoops = Objects.equals(element.getTag("hoops"), "")
                ? 0
                : Integer.parseInt(element.getTag("hoops"));
        this.surface = element.getTag("surface");
        this.address = new Address(element);
        this.amenity = element.getTag("amenity");
        this.website = element.getTag("website");
        this.leisure = element.getTag("leisure");
        this.opening_hours = element.getTag("opening_hours");
        this.phone = element.getTag("phone");
        this.indoor = null;
    }


    public BasketballCourt(long id, String name) {
        this.id = id;
        this.name = name;
    }
}

