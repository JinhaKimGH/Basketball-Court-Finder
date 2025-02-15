import { Button, Fieldset, Input } from "@chakra-ui/react";
import { Field } from "@/components/ui/field"
import {
    DialogBody,
    DialogCloseTrigger,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogRoot,
    DialogTitle,
    DialogTrigger,
    DialogActionTrigger
  } from "@/components/ui/dialog";
  import { LuCircleUserRound } from "react-icons/lu";
  import { Tooltip } from "@/components/ui/tooltip";
import React, {useContext} from "react";
import { AuthContext } from "@/context/AuthContext";

/**
 * Login Component
 * 
 * @param {Object} props  – Component props.
 * @param {number} props.iconSize – Sets sign-in icon size
 * @returns {JSX.Element}
 */
export default function Login(
  props : {
    iconSize: number
  }){

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  // Auth State setter
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }
  const { setAuthState } = authContext;

  const [loginData, setLoginData] = React.useState({
    email: "",
    password: ""
  });
  
  const [emailValid, setEmailValid] = React.useState(true);

  const [errorMessage, setErrorMessage] = React.useState('');

  // Update login data from fields
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setLoginData((prev) => ({...prev, [name]: value}));
  }

  // Helper function ensures that inputted email is a valid email
  function validateEmail(inputEmail: string){
    const emailRegex = new RegExp(/^[A-Za-z0-9_!#$%&'*+/=?`{|}~^.-]+@[A-Za-z0-9.-]+$/, 'gm');

    return emailRegex.test(inputEmail);
  }

  // Login Submission
  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setEmailValid(true);
    setErrorMessage('');

    if (!validateEmail(loginData.email)) {
      setEmailValid(false); 
      return;
    }
    
    fetch(`${baseApiUrl}/api/users/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(loginData),
    }).then(
      (res) => {
        if (res.ok) {
          // TODO: Update with some sort of indication that it succeeded -> new look now that user has logged in
          // Use AuthContext and also update Login component so that there are tabs and one can access sign-up
          return res.json();
        } else {
          switch (res.status) {
            case 401:
              setErrorMessage("Incorrect email or password.");
              break;
            default:
              setErrorMessage('An unexpected error occurred.');
          }
          throw new Error(`HTTP error! status: ${res.status}`);
        }
      })
      .then((data) => {
        setAuthState({isLoggedIn: true, user: data});
      })
      .catch((error) => {
        console.error("Error logging in: ", error);
        // TODO: Replace with logging later
      });

  }


  return(
    <DialogRoot 
      size={{ base: "md", md: "md", lg: "md" }}
      placement="center"
    >
      <Tooltip content="Sign In" openDelay={100}>
        <DialogTrigger asChild>
          <LuCircleUserRound size={props.iconSize} color="#45a2ff" cursor="pointer" />
        </DialogTrigger>
      </Tooltip>
      <DialogContent
        maxWidth="337.5px"
      >
        <DialogHeader>
          <DialogTitle>
            Login 
          </DialogTitle>
        </DialogHeader>

        <DialogBody>
          <Fieldset.Root size={{base: "sm", md: "lg"}} invalid>
            <Fieldset.Content>
              <Field label="Email Address" invalid={!emailValid} errorText={"Invalid email."}>
                <Input 
                  name="email"
                  value={loginData.email}
                  onChange={handleChange}
                />
              </Field>

              <Field label="Password">
                <Input 
                  name="password" 
                  value={loginData.password}
                  onChange={handleChange}
                  type="password"
                />
              </Field>
            </Fieldset.Content>
            {
              errorMessage &&
              <Fieldset.ErrorText>
                {errorMessage}
              </Fieldset.ErrorText>
            }

          </Fieldset.Root>
        </DialogBody>

        <DialogCloseTrigger />
        <DialogFooter>
          <DialogActionTrigger asChild>
            <Button variant="outline">Cancel</Button>
          </DialogActionTrigger>
          <Button onClick={handleLogin} type="submit">Login</Button>
        </DialogFooter>
      </DialogContent>
    </DialogRoot>
    )
}