export default function About(){
    return(
    <div id="about" className="about">
        <p>About Me</p>
        <h1>{"Hi, I'm Jinha Kim"}</h1>
        <p>A student currently studying at the University of Waterloo in the Computer Engineering Program</p>
        <div className="contact">
            <a href="mailto: j733kim@uwaterloo.ca" target="_blank" rel="noopener noreferrer"><img src="./assets/mail.png" className="contact-icon"></img></a>
            <a href="https://github.com/JinhaKimGH" target="_blank" rel="noreferrer"><img src="./assets/github.png" className="contact-icon"></img></a>
            <a href="https://www.linkedin.com/in/jinha-kim/" target="_blank" rel="noreferrer"><img src="./assets/linkedin.png" className="contact-icon"></img></a>
        </div>
    </div>
    )
}