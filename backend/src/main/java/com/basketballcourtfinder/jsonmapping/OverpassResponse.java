package com.basketballcourtfinder.jsonmapping;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OverpassResponse {
    private double version;
    private String generator;
    private Osm3s osm3s;
    private List<Element> elements;

    @Data
    public static class Osm3s {
        private String timestamp_osm_base;
        private String copyright;

        // Getters and setters
    }

    @Data
    public static class Element {
        private String type;
        private long id;
        private Center center; // For the "center" object
        private Map<String, String> tags;

        @Data
        public static class Center {
            private double lat;
            private double lon;
        }

        public String getTag(String key) {
            return (tags != null && tags.containsKey(key)) ? tags.get(key) : "";
        }
    }
}
