import './App.css'
// import Navbar from './components/Navbar'
// import Search from "./components/Search"
// import About from './components/About'
// import LoginForm from "./components/LoginForm"
// import SignUpForm from "./components/SignUpForm"
// import React from "react"
import SearchBar from './components/SearchBar'

export default function App() {
  // const [popup, setPopup] = React.useState(""); // Popup window state (login/sign-up)
  // const [username, setUsername] = React.useState("") // The username/email of the current user

  return (
    <div id="home">
      {/* <Navbar handleClick={setPopup} username={username}/> */}
      <SearchBar/>
      {/* {popup == "LOGIN" ? <LoginForm handleClick={setPopup} handleUser={setUsername} /> : ""}
      {popup == "SIGN UP" ? <SignUpForm handleClick={setPopup}/> : ""}
      <Search username={username}/>
      <About />
      <footer><p className="footer">© 2023 Basketball Court Finder</p></footer> */}
    </div>
  )
}
