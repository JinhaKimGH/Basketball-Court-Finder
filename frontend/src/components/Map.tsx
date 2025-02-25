import { MapContainer, TileLayer, Marker, ZoomControl } from 'react-leaflet';
import L, { LatLngBoundsExpression, LatLngTuple } from 'leaflet'
import 'leaflet/dist/leaflet.css';
import React from 'react'
import { Box } from '@chakra-ui/react';

export default function Map() : JSX.Element {
  // Coordinates that the map is centered on
  const [mapCenter, setMapCenter] = React.useState<LatLngTuple>([43.65, -79.3832]);

  // World boundaries in [south, west, north, east] format
  const worldBounds: LatLngBoundsExpression = [
    [-90, -180], // Southwest coordinates
    [90, 180]    // Northeast coordinates
  ];
  
  const defaultZoom = 12; // The zoom level of the map
  const minZoom = 4; // The minimum zoom level of the map

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
        center={mapCenter} 
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
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          bounds={worldBounds}
          noWrap={true}
        />
        <ZoomControl position="bottomright" />
      </MapContainer>
    </Box>
  )
}