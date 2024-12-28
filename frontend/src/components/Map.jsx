import { MapContainer, TileLayer, Marker } from 'react-leaflet';
import L from 'leaflet'
import 'leaflet/dist/leaflet.css';
import React from 'react'

// The red (active) marker icon on the map
const redMarker = L.icon({
    iconUrl: './assets/red-map-marker.png',
    iconSize: [30, 30],
    iconAnchor: [12, 41],
})

// The blue (inactive) marker icon on the map
const blueMarker = L.icon({
    iconUrl: './assets/blue-map-marker.png',
    iconSize: [30, 30],
    iconAnchor: [12, 41],
})

export default function Map(props) {
    const [mapCenter, setMapCenter] = React.useState([0,0]); // Sets center of the map to the coordinates of the given court
    const [key, setKey] = React.useState(0); // Key property to force map container update
    const zoomLevel = 12; // The zoom level of the map

    // Effect to update the map's center state when the user searches for a new location
    React.useEffect(() => {
      if(props.value.length > 0){
        setMapCenter([props.value[0].center.lat, props.value[0].center.lon]);
        setKey(prevKey => prevKey + 1);
      }
    }, [props.value])
    
    // Updates the index when clicking into another place
    function changeIndex(id){
        let newIndex = 0

        for(let i = 0; i < props.value.length; i++){
            if(props.value[i].id == id){
                newIndex = i;
                break;
            }
        }


        props.handleClick(newIndex)
    }
  
    return (
      <MapContainer key={key} center={mapCenter} zoom={zoomLevel} className="map">
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {props.value.map(pin => (
          <Marker key={pin.id} position={[pin.center.lat, pin.center.lon]} icon={pin.id == props.index ? redMarker : blueMarker} eventHandlers={{click: () => changeIndex(pin.id)}}>
          </Marker>
        ))}
      </MapContainer>
    );
  }