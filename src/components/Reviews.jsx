import React from "react"
import firebase from '../firebase.jsx';
import Stars from './Stars.jsx'
import { getFirestore, collection, getDoc, query, where, getDocs, setDoc, doc, updateDoc } from 'firebase/firestore';

export default function Reviews(props){
    const [data, setData] = React.useState({}); // The review data from firestore
    const [userRating, setUserRating] = React.useState(0); // The rating that the user enters
    const [userReRating, setUserReRating] = React.useState(0); // If the user decides to re-rate 
    const [doesExist, setDoesExist] = React.useState(false); // Checks if the review document actually exists in firebase

    // Obtains the review ratings for this specific place ID from firestore
    async function getReviews(){
        try{
            const db = getFirestore(firebase);
            const reviewsRef = doc(collection(db, 'reviews'), props.placeId.toString())

            const reviewSnap = await getDoc(reviewsRef);

            if(reviewSnap.exists()){
                setData(reviewSnap.data());
                if(data.length !== 0){
                    setDoesExist(true);
                }
            }
        }
        catch (error){
            console.error("Error retrieving review: ", error)
        }

    }

    // Calls the review-obtaining function when the component is mounted
    React.useEffect(() => {
        getReviews();
    }, [])

    // Creates a custom document in the Firestore database if this is the first instance of a review for a place
    async function createCustomDocument(newData){
        try{
            const db = getFirestore(firebase);
            const reviewsRef = collection(db, 'reviews');
            const customDocId = `${props.placeId}`;

            const docRef = doc(reviewsRef, customDocId);

            await setDoc(docRef, newData);
        }

        catch (error){
            console.error("Error creating Document: ", error)
        }
    }

    // Updates the user's review data, keeps track of what the user has reviewed
    // Adds a never before reviewed place to the user's list of reviews
    async function updateUserdata(nextReviewData){
        const db = getFirestore(firebase);
        try{
            const q = query(collection(db, 'users'), where("email", '==', `${props.username}`));
            
            const querySnapshot = await getDocs(q);
            querySnapshot.forEach(async (document) => {
                const userRef = doc(db, 'users', document.id);
                await updateDoc(userRef, { ["reviews"]: nextReviewData });
              });
        }
        
        catch (error) {
            console.error('Error updating review field:', error);
        }
    }

    // Writes a review to the already existing document
    async function writeReview(){
        try{
            const db = getFirestore(firebase);
            const reviewsRef = collection(db, 'reviews');
            const reviewDoc = doc(reviewsRef, `${props.placeId}`);

            await setDoc(reviewDoc, data)
        }

        catch (error){
            console.error("Error writing review", error)
        }
    }

    // Effect called when userRating changes, updates the database and the rating state
    React.useEffect(() => {
        if(userRating == 0){
            return
        }
        if(doesExist == false){
            const newData = {
                "1": 0,
                "2": 0,
                "3": 0,
                "4": 0,
                "5": 0, 
            };
            newData[`${userRating}`] += 1

            createCustomDocument(newData);
            
            setData(newData)
            setDoesExist(true)
        }

        else{
            const newData = data;
            newData[`${userRating}`] += 1;
            setData(newData);
            updateRatingAverage(newData)
            writeReview();
        }

        if(!props.reviewData.includes(props.placeId)){
            let nextReviewData = [...props.reviewData,  {[props.placeId]: userRating}]
            updateUserdata(nextReviewData);
            props.handleReviewData(nextReviewData);
        }

    }, [userRating])

    // Effect called when userReRating changes, updates database and rating state
    // Takes away previous rating from the place's reviews, and adds the new one back
    // Allows for user to change/update their previous rating
    React.useEffect(() =>{
        if(userReRating == 0){
            return
        }

        if(doesExist){
            const newData = data;
            let placeReviews = props.reviewData.filter(function (o) {
                return o.hasOwnProperty(props.placeId);
            });

            newData[`${placeReviews[0][props.placeId]}`] -= 1;
            newData[`${userReRating}`] += 1;
            setData(newData);
            updateRatingAverage(newData)
            writeReview();

            const nextReviewData = props.reviewData.map((obj) => {
                if(props.placeId.toString() in obj){
                    return {...obj, [props.placeId]: userReRating}
                }else{
                    return obj
                }
            });
            props.handleReviewData(nextReviewData)

            updateUserdata(nextReviewData);
        }   

    }, [userReRating])

    // Calculates the average rating from the database to display it on the page
    function calculateAverage(){
        if(doesExist == false){
            return 0;
        }

        let totalReviews = data["1"] + data["2"] + data["3"] + data["4"] + data["5"];

        let average = 1 * data["1"] + 2 * data["2"] + 3 * data["3"] + 4 * data["4"] + 5 * data["5"];
        average = average / totalReviews;

        return average
    }

    const [rating, setRating] = React.useState(0); // The rating displayed on the page
    const [numPeople, setNumPeople] = React.useState(0) // The number of reviewers displayed on the page

    // Function to update rating and numpeople states
    function updateRatingAverage(newInfo){
        setRating(calculateAverage().toFixed(2));
        setNumPeople(newInfo["1"] + newInfo["2"] + newInfo["3"] + newInfo["4"] + newInfo["5"]);
    }

    // When the data changes, the rating and numPeople states are set
    React.useEffect(() => {
        updateRatingAverage(data);
      }, [data]);

    return(
        <div>
            <div className="average">{doesExist ? <div>{`${rating} (${numPeople})`}</div> : <p>No reviews exist yet</p> }</div>
            <Stars rating={rating} handleRating={setUserRating} reviewData={props.reviewData} username={props.username} placeId={props.placeId} handleReRating={setUserReRating}/>
        </div>
    )
}