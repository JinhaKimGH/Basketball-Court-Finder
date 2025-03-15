import { MapContainer, TileLayer, Marker, ZoomControl, useMapEvents } from 'react-leaflet';
import L, { LatLngBoundsExpression, LatLngTuple } from 'leaflet'
import 'leaflet/dist/leaflet.css';
import { Box } from '@chakra-ui/react';
import React from 'react';
import { BasketballCourt } from '@/interfaces';

const marker = L.icon({
  iconUrl: './assets/pin.png',
  iconSize: [60, 60],
  iconAnchor: [30,60],
})

const selectedMarker = L.icon({
  iconUrl: './assets/selected.png',
  iconSize: [60, 60],
  iconAnchor: [30,60],
})

function MapEventHandler({ onMoveEnd }: { onMoveEnd: (center: LatLngTuple) => void }) {
  const map = useMapEvents({
    moveend: () => {
      if (map && onMoveEnd) {  // Add null check
        const center = map.getCenter();
        onMoveEnd([center.lat, center.lng]);
      }
    },
  });
  return null;
}

export default function Map(
  props: {
    coordinates: LatLngTuple,
    courts: Array<BasketballCourt>,
    setSelected: React.Dispatch<React.SetStateAction<number>>,
    selected: number,
    onCenterChange: (center: LatLngTuple) => void
  }
) : JSX.Element {
  const [key, setKey] = React.useState(0); // Key property to force map container update

  // World boundaries in [south, west, north, east] format
  const worldBounds: LatLngBoundsExpression = [
    [-90, -180], // Southwest coordinates
    [90, 180]    // Northeast coordinates
  ];
  
  const defaultZoom = 16; // The zoom level of the map
  const minZoom = 4; // The minimum zoom level of the map

  React.useEffect(() => {
    setKey(prevKey => prevKey + 1);
  }, [props.coordinates])

  return (
    <Box
      position="fixed"
      top={0}
      left={0}
      right={0}
      bottom={0}
      padding={0}
      margin={0}
      height="100vh"
      width="100vw"
      maxWidth="100vw"
      overflow="hidden"
    >
      <MapContainer 
        key={key}
        center={props.coordinates} 
        minZoom={minZoom}
        zoom={defaultZoom} 
        maxBounds={worldBounds}
        maxBoundsViscosity={1.0}
        style={{
          height: "100%",
          width: "100%",
          position: "absolute",
          top: 0,
          left: 0
        }}
        zoomControl={false}
      >
        <MapEventHandler onMoveEnd={props.onCenterChange} />
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          bounds={worldBounds}
          noWrap={true}
        />
        <ZoomControl position="bottomright" />
        {
          props.courts.map((pin, idx) => (
            <Marker 
              key={pin.id} 
              position={[pin.lat, pin.lon]} 
              icon={
                props.selected === idx ? selectedMarker : marker
              }
              eventHandlers={{click: () => props.setSelected(idx)}}
            />
          ))
        }
      </MapContainer>
    </Box>
  )
}