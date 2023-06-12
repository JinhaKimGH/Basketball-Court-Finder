import React from "react"
import firebase from '../firebase.jsx';
import { getFirestore, collection, getDocs } from 'firebase/firestore';

export default function LoginForm(props){
    const [email, setEmail] = React.useState(""); // State for the user's email
    const [password, setPassword] = React.useState(""); // State for the user's password
    const [data, setData] = React.useState([]) // State for the data of all the user's in the database

    // Fetches the data for all the user's in the database
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

    // Closes/unmounts component when 'x' is clicked
    function closeForm(){
        props.handleClick("")
    }

    // Handles the login, checks email + password validity
    async function handleLogin(event){
        event.preventDefault();
        
        let confirmPassword = document.getElementById("password")
        confirmPassword.setCustomValidity("")
        document.getElementById("email").setCustomValidity("")
        for(let i = 0; i < data.length; i++){
            if(data[i].email == email){
                if(data[i].password == password){
                    props.handleUser(email)
                    props.handleArr(data[i].reviews)
                    props.handleClick("")
                    return
                }
                else{
                    confirmPassword.setCustomValidity("Incorrect Password")
                    return
                }
            }
        }

        document.getElementById("email").setCustomValidity("This email is not associated with an account")
        return
    }
    
    return(
        <div className="login-signup-form">
            <div className="form-base-login">
                <button onClick={closeForm} className="close-button">&times;</button>
                <form className="form-login" onSubmit={handleLogin}>
                    <h1 className="login-title">Sign In</h1>
                    <label>Email</label><br></br>
                    <input id="email" type="email" name="email" value={email} onChange={(event) => setEmail(event.target.value)} className="input-box" required/><br></br>
                    <label>Password</label><br></br>
                    <input id="password" type="password" name="password" value={password} onChange={(event) => setPassword(event.target.value)} className="input-box" required/><br></br>
                    <input type="submit" value="Login" className="submit-button"/><br></br>
                </form>
            </div>
        </div>
    )
}