import axios from 'axios'
import React from 'react'
import Placecard from './Placecard';
import Map from './Map.jsx'

export default function Search(props){
    const [basketballCourts, setBasketballCourts] = React.useState([]); // Array state of the basketball courts in the area
    const [location, setLocation] = React.useState("6464 Yonge St, North York"); // State of the address
    const [coordinates, setCoordinates] = React.useState({lat: "43.796656647925026", lon: "-79.42200704246716"}) // State of the coordinates
    const [range, setRange] = React.useState(2000); // State of the set range
    const [index, setIndex] = React.useState(0); // State of the place that the user is looking at in the array of basketball courts
    const [isFound, setIsFound] = React.useState(true) // State that checks if the place has courts nearby
    
    // Calculates distance w/ haversine formula
    function calcDistance(lat1, lon1, lat2, lon2){
        const earthRadius = 6371
        const dLat = (lat2 - lat1) * (Math.PI / 180)
        const dLon = (lon2 - lon1) * (Math.PI / 180)
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * (Math.PI / 180)) *
            Math.cos(lat2 * (Math.PI / 180)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        const distance = earthRadius * c;

        return distance;
    }

    // Fetches the basketball courts in an area whenever the coordinates and range state is updated
    React.useEffect(() => {
        const fetchPOIs = async () => {
          try {
            const latitude = coordinates.lat;
            const longitude = coordinates.lon; 
    
            const response = await axios.get('https://overpass-api.de/api/interpreter', {
              params: {
                data: `[out:json];(way(around:${range},${latitude},${longitude})["amenity"="community_centre"];way(around:${range},${latitude},${longitude})["leisure"="pitch"]["sport"="basketball"];way(around:${range},${latitude},${longitude})["amenity"="school"]["sport"="basketball"];);out center;`,
              },
            });

            // Sorts the basketball courts by distance
            setBasketballCourts(sortCourts(response, latitude, longitude));
            
          } catch (error) {
            console.error('Error fetching Basketball Courts:', error);
          }
        };
    
        fetchPOIs();
    }, [coordinates, range]);

    // Sorts basketball courts by distance
    function sortCourts(response, latitude, longitude){
        return response.data.elements.sort((court1, court2) => {
            const distance1 = calcDistance(latitude, longitude, court1.center.lat, court1.center.lon);
            const distance2 = calcDistance(latitude, longitude, court2.center.lat, court2.center.lon);

            return distance1 - distance2
        })
    }
    
    // Updates the states for inputs
    function handleChange(event){
        if(event.target.className == "form-input"){
            const {value} = event.target
            setLocation(value);
        }

        if(event.target.className == "form-select"){
            const {value} = event.target
            setIndex(0);
            setRange(value);
        }
    }

    // Searches if enter is pressed
    function handleKeyDown(event){
        if(event.key == 'Enter'){
            calculateBoundary();
        }
    }
    
    // Calculates the latitude/longitude on submit (geocode with nominatim)
    async function calculateBoundary(){
        setIndex(0);
        try {
            const response = await axios.get(`https://nominatim.openstreetmap.org/search?q=${location}&format=json`, {
                headers: {
                    Referer: 'https://jinhakimgh.github.io/Basketball-Court-Finder', // Set the Referer header
                    'User-Agent': 'BasketballCourtFinder/1.0' // Set the User-Agent header
                }
            });

        if (response.data.length > 0){
            const {lat, lon} = response.data[0];
            setCoordinates({lat: lat, lon: lon});
            setIsFound(true)
        }

        else{
            setIsFound(false)
        }
        }catch(error){
            console.error("Error geocoding location:", error)
        }
    }   

    // Function that changes the place being viewed with the click of an arrow
    function handleArrow(event){
        if(event.target.name == "left"){
            if(index == 0){
                setIndex(basketballCourts.length - 1)
            }
            else{
                setIndex(prevIndex => prevIndex - 1)
            }
        }

        else{
            if(index == basketballCourts.length - 1){
                setIndex(0)
            }
            else{
                setIndex(prevIndex => prevIndex + 1)
            }
        }
    }

    return(
        <div>
            <div className="form">
                <input type="text" placeholder={location} onChange={handleChange} onKeyDown={handleKeyDown} className="form-input"></input>
                <select id="range" name="range" className="form-select" onChange={handleChange}>
                    <option value="1000">1 km</option>
                    <option value="5000">5 km</option>
                    <option value="10000">10 km</option>
                    <option value="20000">20 km</option>
                </select>
                <button className="submit" onClick={calculateBoundary}><i className="fa fa-search"></i></button>
            </div>

            {(basketballCourts.length > 0 & isFound) ? <Map value={basketballCourts} index={basketballCourts[index].id} handleClick={setIndex}/> : ""}
        
            {(isFound & basketballCourts.length > 0) ? <div className="view-court">
                <button className="arrows" name="left" onClick={handleArrow}><i className="arrow left" name="left"></i></button>
                {basketballCourts.length > 0 && <Placecard key={basketballCourts[index].id} 
                                                    value={basketballCourts[index]} 
                                                    distance={calcDistance(coordinates.lat, coordinates.lon, basketballCourts[index].center.lat, basketballCourts[index].center.lon)}
                                                    username={props.username}
                                                    reviewData={props.reviewData}
                                                    handleReviewData={props.handleReviewData}/>}
                <button className="arrows" name="right" onClick={handleArrow}><i className="arrow right" name="right"></i></button>
            </div> : ""}

            {(isFound == false || basketballCourts.length == 0) && <div className="error">
                No results found from <em>{location}</em>
                </div>}
        </div>
    )

}