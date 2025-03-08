import CourtCard from "@/components/CourtCard";
import Map from "@/components/Map";
import SearchBar from "@/components/SearchBar";
import { BasketballCourt } from "@/interfaces";
import { LatLngTuple } from "leaflet";
import React from "react";
import {AnimatePresence, motion} from "framer-motion";

export default function Home() {

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  // The selected court
  const [selected, setSelected] = React.useState(-1);

  // Coordinates that the map is centered on
  const [mapCenter, setMapCenter] = React.useState<LatLngTuple>([43.65, -79.3832]);

  // Basketball Courts
  const [courts, setCourts] = React.useState<Array<BasketballCourt>>([{
    id: 12,
    lat: 43.65,
    lon: -79.3832,
    name: "Test Court",
    hoops: 2,
    surface: "Pavement",
    address: {
      house_number: "123",
      street: "street",
      city: "Toronto",
      state: "Ontario",
      country: "Canada",
      postcode: "ZZZ 123",
      incomplete: false,
    },
    amenity: "Park",
    website: "www.google.com",
    opening_hours: "Mo-Fr 07:30-22:00; Sa 09:00-23:00; Su 10:00-18:00",
    phone: "647 - 999 - 9999",
    indoor: false
  }]);

  // Fetch courts when the map center updates
  React.useEffect(() => {
    const params = new URLSearchParams({
      latitude: mapCenter[0].toString(),
      longitude: mapCenter[1].toString(),
      range: '2000', // TODO: Set as constant for now, update later?
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

  }, [baseApiUrl, mapCenter])

  const closeCard = (): void => {
    setSelected(-1);
  }

  return (
    <>
      <SearchBar setCoordinates={setMapCenter}/>
      <Map 
        coordinates={mapCenter} 
        courts={courts} 
        setSelected={setSelected}
        selected={selected}
      />
      <AnimatePresence>
        {(0 <= selected && selected < courts.length) &&
          <motion.div
            key="court-card"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.2 }}
          >
            <CourtCard 
              court={courts[selected]} 
              setCourts={setCourts} 
              index={selected}
              closeCard={closeCard}
            />
          </motion.div>
        }
      </AnimatePresence>
    </>
  )
}