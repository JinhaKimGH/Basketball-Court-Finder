package com.basketballcourtfinder.entity;

import jakarta.persistence.*;

@Entity
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

    private int hoops;

    private String surface;

    private String street_address;
    private String city;
    private String state;

    private String country;

    private String postal_code;


    private String description;

    private String features;

    public BasketballCourt(long id, double lat, double lon,
                           String name, String surface, String street_address,
                           String city, String state, String country,
                           String postal_code, String description, String features) {
        this(id, lat, lon, name, 0, surface, street_address, city, state, country, postal_code, description, features);
    }

    public BasketballCourt(long id, double lat, double lon, String name, int hoops, String surface,
                           String street_address, String city, String state, String country, String postal_code,
                           String description, String features) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.hoops = hoops;
        this.surface = surface;
        this.street_address = street_address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postal_code = postal_code;
        this.description = description;
        this.features = features;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHoops() {
        return hoops;
    }

    public void setHoops(int hoops) {
        this.hoops = hoops;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String address() {
        return street_address + ", " + city + ", " + state + ", " + country + ", " + postal_code;
    }

    public String getStreet_address() {
        return street_address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostal_code() {
        return postal_code;
    }
}
