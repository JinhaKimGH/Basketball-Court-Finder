import CourtCard from "@/components/CourtCard";
import Map from "@/components/Map";
import SearchBar from "@/components/SearchBar";
import { BasketballCourt } from "@/interfaces";
import { LatLngTuple } from "leaflet";
import React from "react";
import {AnimatePresence, motion} from "framer-motion";
import { Toaster } from "@/components/ui/toaster";

export default function Home() {

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  // The selected court
  const [selected, setSelected] = React.useState(-1);

  // Coordinates that the map is centered on
  const [coordinates, setCoordinates] = React.useState<LatLngTuple>([43.65, -79.3832]);
  const [mapCenter, setMapCenter] = React.useState<LatLngTuple>([43.65, -79.3832]);

  // Basketball Courts
  const [courts, setCourts] = React.useState<Array<BasketballCourt>>([]);

  // Fetch courts when the map center updates
  React.useEffect(() => {
    const params = new URLSearchParams({
      latitude: coordinates[0].toString(),
      longitude: coordinates[1].toString(),
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

  }, [baseApiUrl, coordinates])

  const closeCard = (): void => {
    setSelected(-1);
  }

  return (
    <>
      <Toaster/>
      <SearchBar setCoordinates={setCoordinates} currentMapCenter={mapCenter}/>
      <Map 
        coordinates={coordinates} 
        courts={courts} 
        setSelected={setSelected}
        selected={selected}
        onCenterChange={(center) => setMapCenter(center)}
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