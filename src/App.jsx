/* eslint-disable no-unused-vars */
import './App.css'
import Navbar from './components/Navbar'
import Search from "./components/search"
import About from './components/About'
import LoginForm from "./components/LoginForm"
import SignUpForm from "./components/SignUpForm"
import React from "react"

export default function App() {
  const [popup, setPopup] = React.useState(""); // Popup window state (login/sign-up)
  const [username, setUsername] = React.useState("") // The username/email of the current user
  const [reviewData, setReviewData] = React.useState({}); // The review data of the current user

  return (
    <div id="home">
      <Navbar handleClick={setPopup} username={username}/>
      {popup == "LOGIN" ? <LoginForm handleClick={setPopup} handleUser={setUsername} handleArr={setReviewData}/> : ""}
      {popup == "SIGN UP" ? <SignUpForm handleClick={setPopup}/> : ""}
      <Search username={username} reviewData={reviewData} handleReviewData={setReviewData}/>
      <About />
      <footer><p className="footer">© 2023 Basketball Court Finder</p></footer>
    </div>
  )
}
