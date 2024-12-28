/* eslint-disable react/prop-types */
import React from "react";
import Reviews from "./Reviews";
import { BasketballCourt } from "../interfaces";

/**
 * Search Component
 * 
 * @param {Object} props  – Component props.
 * @param {number} props.key – index
 * @param {BasketballCourt} props.value – Basketball court
 * @param {number} props.distance – Distance from original coordinates
 * @param {string} props.username
 * @returns {JSX.Element}
 */
export default function Placecard(
    props : {
        key : number,
        value: BasketballCourt,
        distance: number,
        username: string
    }) : JSX.Element {
    const [address, setAddress] = React.useState("") // Address state 
    const [amenity, setAmenity] = React.useState("") // Amenity state, in the case of a park/community centre
    const [windowType, setWindowType] = React.useState("Overview") // Sets the window state (Overview or Review)
    const [loading, setLoading] = React.useState(false) // Shows a loading sign while the API search is running

    // Fetches the location information using reverse geocoding with the nominatim API
    async function fetchLocation(){
        try {

            setLoading(true);
            const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${props.value.lat}&lon=${props.value.lon}`, {
                headers: {
                  Referer: 'https://jinhakimgh.github.io/Basketball-Court-Finder', 
                  'User-Agent': 'BasketballCourtFinder/1.0'
                }
              });

            const data = await response.json();

            setAddress(`${'house_number' in data.address ? data.address.house_number + " " : ""}
                        ${'road' in data.address ? data.address.road + ", " : ""}
                        ${'city' in data.address ? data.address.city + ", " : ""}
                        ${'state' in data.address ? data.address.state + " " : ""}${'postcode' in data.address ? data.address.postcode : ""}`)
            setAmenity(`${'amenity' in data.address ? data.address.amenity : ""}`)
            setLoading(false);

        }catch(error){
            console.error("Error fetching location:", error)
        }
    }

    // Fetches the location information when component is mounted
    React.useEffect(() => {
        fetchLocation()
    }, [])

    // Changes window from Overview to Review
    function changeWindow(event : React.MouseEvent<HTMLParagraphElement, MouseEvent>){
        setWindowType((event.target as HTMLParagraphElement).className)
    }

    const activeStyle = {"borderBottom": "2px solid #FF8B36", "color": "#FF8B36"} // Sets the style of the active window
    return(
        <div className="placecard">
            {amenity !== "" ? <p className="name">{amenity}</p> : ('name' in props.value ? <p className="name">{props.value.name}</p> :  <p className="name">Outdoor Court</p>)}
            
            <div className="choose-window">
                <p className="Overview" onClick={changeWindow} style={windowType === "Overview" ? activeStyle : {}}>Overview</p>
                <p className="Review" onClick={changeWindow} style={windowType === "Review" ? activeStyle : {}}>Reviews</p>
            </div>

            {windowType === "Overview" && loading === false ? <div className="overview-panel">
                <p className="distance"><img src="./assets/distanceIcon.png" className="icons"></img>{` Distance: ${Math.round(props.distance * 100) / 100} km`}</p>
                <p className="address"><i className="fa fa-map-marker"></i>{address !== "" && " " + address}</p>
                {props.value.hoops && <p className="info"><img src="./assets/hoop.png" className="icons"></img>{" " + props.value.hoops} hoop{props.value.hoops > 1 ? "s" : ""}</p>}
                {props.value.surface && <p className="info"><img src="./assets/ground.png" className="icons"></img>{" " + props.value.surface.charAt(0).toUpperCase() + props.value.surface.slice(1)} Surface</p>}
            </div> : ""}

            {loading == true ? <img src="./assets/loading.gif" className="loading"></img> : ""}

            {windowType == "Review" && <Reviews className="review" username={props.username} placeId={props.value.id}/>}

        </div>
    )
    

}