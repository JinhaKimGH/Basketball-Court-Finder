package com.basketballcourtfinder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
public class BasketballCourt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    @Column(nullable = false)
    private String name;

    private int hoops = 0;

    private String surface;

    @Embedded
    private Address address;

    private String description;

    private String features;

    @ManyToOne
    @JoinColumn(name="added_by")
    private User user;

}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class Address {

    private String street_address;
    private String city;
    private String state;
    private String country;
    private String postal_code;

    @Override
    public String toString() {
        return street_address + ", " + city + ", " + state + ", " + country + ", " + postal_code;
    }
}