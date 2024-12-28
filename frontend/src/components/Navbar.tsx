/**
 * Navbar Component
 * 
 * @param {Object} props  – Component props.
 * @param {React.Dispatch<React.SetStateAction<string>>} props.handleClick – Sets signin form state
 * @param {string} props.username – Users username
 * @returns {JSX.Element}
 */
export default function Navbar(
    props: {
        handleClick: React.Dispatch<React.SetStateAction<string>>,
        username: string
    }) : JSX.Element {
    // Scrolls into the section when it is clicked on in the navbar
    function handleClickScroll(event : React.MouseEvent<HTMLAnchorElement>){
        const element = document.getElementById((event.target as HTMLAnchorElement).innerText.toLowerCase())
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        }
    }

    // Handles the login feature – sets the pop-up state to determine whether the user is signing up or logging in
    function login(event : React.MouseEvent<HTMLAnchorElement>){
        props.handleClick((event.target as HTMLAnchorElement).innerText)
    }
    return(
        <nav className="navbar">
            <img 
                src="./assets/basket.png" 
                className="navbar-image"
            />
            <h2 className="navbar-title">Basketball Court Finder</h2>
            
            <ul>
                <li><a onClick={handleClickScroll}>HOME</a></li>
                <li><a onClick={handleClickScroll}>ABOUT</a></li>
                {props.username == "" ? <li><a onClick={login}>LOGIN</a></li> : ""}
                {props.username == "" ? <li><a onClick={login}>SIGN UP</a></li> : <li>
                                                                                        <div className="profile-picture">
                                                                                            <span className="initials">{props.username[0]}</span>
                                                                                        </div>  
                                                                                    </li>}
            </ul>
        </nav>
    )
}