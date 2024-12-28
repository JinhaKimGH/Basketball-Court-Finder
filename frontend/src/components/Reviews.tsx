import React from "react"
import Stars from './Stars'

/**
 * Reviews Component
 * 
 * @param {Object} props  – Component props.
 * @param {string} props.className – class name
 * @param {string} props.username – User's username
 * @param {number} props.placeId – id of basketball court
 * @returns {JSX.Element}
 */
export default function Reviews(
    props : {
        className: string,
        username: string,
        placeId: number
    }) : JSX.Element {
    const [data, setData] = React.useState({}); // The review data from firestore
    const [userRating, setUserRating] = React.useState(0); // The rating that the user enters
    const [userReRating, setUserReRating] = React.useState(0); // If the user decides to re-rate 
    const [doesExist, setDoesExist] = React.useState(false); // Checks if the review document actually exists in firebase
    const [loading, setLoading] = React.useState(false) // Shows a loading sign while the API search is running

    // Obtains the review ratings for this specific place ID from 
    // TODO: Update with new Backend
    async function getReviews(){
        try{
            setLoading(true);

            setLoading(false);
        }
        catch (error){
            console.error("Error retrieving review: ", error)
        }

    }

    // Calls the review-obtaining function when the component is mounted
    React.useEffect(() => {
        getReviews();
    }, [])

    // TODO: Add POI/Court to DB if no reviews -> probably doesn't exist

    // TODO: Writes a review to the already existing court

    // async function writeReview(){
    //     try{
    //     }

    //     catch (error){
    //         console.error("Error writing review", error)
    //     }
    // }

    // Effect called when userRating changes, updates the database and the rating state
    // React.useEffect(() => {
    //     if(userRating == 0){
    //         return
    //     }
    //     if(doesExist == false){
    //         const newData = {
    //             "1": 0,
    //             "2": 0,
    //             "3": 0,
    //             "4": 0,
    //             "5": 0, 
    //         };
    //         newData[`${userRating}`] += 1

    //         createCustomDocument(newData);
            
    //         setData(newData)
    //         setDoesExist(true)
    //     }

    //     else{
    //         const newData = data;
    //         newData[`${userRating}`] += 1;
    //         setData(newData);
    //         updateRatingAverage(newData)
    //         writeReview();
    //     }

    //     if(!props.reviewData.includes(props.placeId)){
    //         let nextReviewData = [...props.reviewData,  {[props.placeId]: userRating}]
    //         updateUserdata(nextReviewData);
    //         props.handleReviewData(nextReviewData);
    //     }

    // }, [userRating])

    // Effect called when userReRating changes, updates database and rating state
    // Takes away previous rating from the place's reviews, and adds the new one back
    // Allows for user to change/update their previous rating
    // React.useEffect(() =>{
    //     if(userReRating == 0){
    //         return
    //     }

    //     if(doesExist){
    //         const newData = data;
    //         let placeReviews = props.reviewData.filter(function (o) {
    //             return o.hasOwnProperty(props.placeId);
    //         });

    //         newData[`${placeReviews[0][props.placeId]}`] -= 1;
    //         newData[`${userReRating}`] += 1;
    //         setData(newData);
    //         updateRatingAverage(newData)
    //         writeReview();

    //         const nextReviewData = props.reviewData.map((obj) => {
    //             if(props.placeId.toString() in obj){
    //                 return {...obj, [props.placeId]: userReRating}
    //             }else{
    //                 return obj
    //             }
    //         });
    //         props.handleReviewData(nextReviewData)

    //         updateUserdata(nextReviewData);
    //     }   

    // }, [userReRating])

    // Calculates the average rating from the database to display it on the page
    // function calculateAverage(){
    //     if(doesExist == false){
    //         return 0;
    //     }

    //     let totalReviews = data["1"] + data["2"] + data["3"] + data["4"] + data["5"];

    //     let average = 1 * data["1"] + 2 * data["2"] + 3 * data["3"] + 4 * data["4"] + 5 * data["5"];
    //     average = average / totalReviews;

    //     return average
    // }

    const [rating, setRating] = React.useState(0); // The rating displayed on the page
    const [numPeople, setNumPeople] = React.useState(0) // The number of reviewers displayed on the page

    // Function to update rating and numpeople states
    // function updateRatingAverage(newInfo){
    //     setRating(calculateAverage().toFixed(2));
    //     setNumPeople(newInfo["1"] + newInfo["2"] + newInfo["3"] + newInfo["4"] + newInfo["5"]);
    // }

    // When the data changes, the rating and numPeople states are set
    // React.useEffect(() => {
    //     updateRatingAverage(data);
    //   }, [data]);

    return(
        <div>
            {loading == false ? <div className="average">{doesExist ? <div>{`${rating} (${numPeople})`}</div> : <p>No reviews exist yet</p> }</div> : ""}
            {loading == false ? <Stars rating={rating} handleRating={setUserRating} username={props.username} placeId={props.placeId}/> : ""}
            {loading &&  <img src="./assets/loading.gif" className="loading"></img>}
        </div>
    )
}