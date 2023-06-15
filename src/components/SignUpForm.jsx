import React from "react"
import firebase from '../firebase.jsx';
import { getFirestore, collection, addDoc, getDocs } from 'firebase/firestore';

export default function SignUpForm(props){
    const [email, setEmail] = React.useState(""); // State for user email
    const [password, setPassword] = React.useState(""); // State for user password
    const [retypePassword, setRetypePassword] = React.useState("") // State for value of user's retyped password
    const [data, setData] = React.useState([]) // State for email+password data from database

    // Fetches email & password data from database
    async function fetchData(){
        const db = getFirestore(firebase);
        const usersRef = collection(db, 'users');
        const snapshot = await getDocs(usersRef);
        const fetchedData = snapshot.docs.map((doc) => doc.data());

        setData(fetchedData)
    }
    
    // Fetches data when component is mounted
    React.useEffect(() => {
        fetchData();
    }, [])
    
    // Unmounts component when 'x' is clicked
    function closeForm(){
        props.handleClick("")
    }

    // Handles users signup. Sets email + password and writes it to the database
    async function handleSignup(event){
        event.preventDefault();
        const db = getFirestore(firebase);
        
        let confirmEmail = document.getElementById("email-input")
        confirmEmail.setCustomValidity("")
        for(let i = 0; i < data.length; i++){
            if (email == data[i].email){
                confirmEmail.setCustomValidity("An account for that email already exists")
                return
            }
        }

        let confirmPassword = document.getElementById("confirm-password")
        if(password !== retypePassword){
            confirmPassword.setCustomValidity("Passwords Don't Match");
            return
        }
        else{
            confirmPassword.setCustomValidity('');
        }

        try{
            let reviews = []
            await addDoc(collection(db, 'users'), { email, password, reviews});
            props.handleClick("LOGIN")
        }
        catch (error){
            console.error('Error signing up', error)
        }
    }
    
    return(
        <div className="login-signup-form">
            <div className="form-base-signup" onSubmit={handleSignup}>
                <button onClick={closeForm} className="close-button">&times;</button>
                <form className="form-login">
                    <h1 className="login-title">Sign Up</h1>
                    <input placeholder="Email" id="email-input" type="email" name="email" value={email} onChange={(event) => setEmail(event.target.value)} className="input-box" required/><br></br>
                    <input placeholder="Password" id="password" type="password" name="password" value={password} onChange={(event) => setPassword(event.target.value)} className="input-box" required/><br></br>
                    <input placeholder="Resubmit Password" id="confirm-password" type="password" name="password" value={retypePassword} onChange={(event) => setRetypePassword(event.target.value)}className="input-box" required/><br></br>
                    <input type="submit" value="Sign Up" className="submit-button"/><br></br>
                </form>
            </div>
        </div>
    )
}