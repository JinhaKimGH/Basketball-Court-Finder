import React from "react"

export default function Stars(props){
    const [userHover, setUserHover] = React.useState(0); // The rating that appears when the user hovers over the stars
    const [error, setError] = React.useState(""); // The error message that is displayed
    // Function that updates the stars when the mouse hovers over them
    function handleMouseEnter(starRating){
        setUserHover(starRating);
    }

    // Updates the stars when the mouse is no longer hovering over it
    function handleMouseLeave(){
        setUserHover(0);
    }

    // Updates the database rating itself
    function handleClick(starRating){
        setError("")
        // User not logged in, rating isn't updated
        if(props.username == ""){
            setError("You must log in to leave a rating")
            return
        }

        // User already reviewed the place, rating is updated
        let exists = props.reviewData.filter(function (o) {
            return o.hasOwnProperty(props.placeId);
          }).length > 0;

        if(exists){
            props.handleReRating(starRating)
            return
        }

        props.handleRating(starRating)
    }
    const stars = [];

    for(let i = 0; i < 5; i++){
        const starClassName = i + 1 <= (userHover || props.rating) ? 'star filled' : 'star';

        stars.push(
            <span 
                key={i+1} 
                className={starClassName}
                onMouseEnter={() => handleMouseEnter(i+1)}
                onMouseLeave={handleMouseLeave}
                onClick={() => handleClick(i+1)}
                ></span>
        );
    }

    return (
        <div>
            <div className="stars">{stars}</div>
            {error !== "" ? <p className="rating-error">{`*${error}`}</p> : ""}
        </div>
    )
}