import { MapContainer, TileLayer, Marker } from 'react-leaflet';
import L from 'leaflet'
import 'leaflet/dist/leaflet.css';
import React from 'react'
import { BasketballCourt } from '../interfaces';

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

/**
 * LeafletMaps Component
 * 
 * @param {Object} props  – Component props.
 * @param {Array<BasketballCourt>} props.value – Sets signin form state
 * @param {Number} props.index – Users username
 * @param {React.Dispatch<React.SetStateAction<number>>} props.handleClick – Setting the index of the map
 * @returns {JSX.Element}
 */
export default function LeafletMap(
  props : {
    value : Array<BasketballCourt>,
    index : Number,
    handleClick : React.Dispatch<React.SetStateAction<number>>
  }) : JSX.Element {
    const [mapCenter, setMapCenter] = React.useState([0,0]); // Sets center of the map to the coordinates of the given court
    const [key, setKey] = React.useState(0); // Key property to force map container update
    const zoomLevel = 12; // The zoom level of the map

    // Effect to update the map's center state when the user searches for a new location
    React.useEffect(() => {
      if(props.value.length > 0){
        setMapCenter([props.value[0].lat, props.value[0].lon]);
        setKey(prevKey => prevKey + 1);
      }
    }, [props.value])
    
    // Updates the index when clicking into another place
    // Change to hashmap?
    function changeIndex(id : number){
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
          <Marker key={pin.id} position={[pin.lat, pin.lon]} icon={Number(pin.id) == props.index ? redMarker : blueMarker} eventHandlers={{click: () => changeIndex(pin.id)}}>
          </Marker>
        ))}
      </MapContainer>
    );
  }