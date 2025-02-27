import Map from "@/components/Map";
import SearchBar from "@/components/SearchBar";
import { BasketballCourt } from "@/interfaces";
import { LatLngTuple } from "leaflet";
import React from "react";

export default function Home() {

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;
  // Coordinates that the map is centered on
  const [mapCenter, setMapCenter] = React.useState<LatLngTuple>([43.65, -79.3832]);

  // Basketball Courts
  const [courts, setCourts] = React.useState<Array<BasketballCourt>>([]);

  // Fetch courts when the map center updates
  React.useEffect(() => {
    const params = new URLSearchParams({
      latitude: mapCenter[0].toString(),
      longitude: mapCenter[1].toString(),
      range: '1000', // TODO: Set as constant for now, update later?
    });

    fetch(`${baseApiUrl}/api/courts/around?${params.toString()}`, {
      method: 'GET',
      credentials: 'include',
    })
      .then((res) => {
        if(res.ok) {
          return res.json();
        } else {
          throw new Error(`HTTP error! Status: ${res.status}`);
        }
      })
      .then((data) => setCourts(data))
      .catch((error) => console.error(error.message)); // TODO: update later with logging

  }, [mapCenter])

  return (
    <>
      <SearchBar setCoordinates={setMapCenter}/>
      <Map coordinates={mapCenter} courts={courts}/>
    </>
  )
}