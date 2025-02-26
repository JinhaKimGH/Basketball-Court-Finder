import Map from "@/components/Map";
import SearchBar from "@/components/SearchBar";
import { LatLngTuple } from "leaflet";
import React from "react";

export default function Home() {
  // Coordinates that the map is centered on
  const [mapCenter, setMapCenter] = React.useState<LatLngTuple>([43.65, -79.3832]);

  return (
    <>
      <SearchBar setCoordinates={setMapCenter}/>
      <Map coordinates={mapCenter}/>
    </>
  )
}