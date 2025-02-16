import { Button, Fieldset, Input } from "@chakra-ui/react";
import { Field } from "@/components/ui/field";
import { validateEmail } from "../utils";
import {
    DialogBody,
    DialogFooter,
    DialogActionTrigger
  } from "@/components/ui/dialog";
import React, {useContext} from "react";
import { AuthContext } from "@/context/AuthContext";

/**
 * Login Component
 * 
 * @returns {JSX.Element}
 */
export default function Login(){

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
    <>
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

      <DialogFooter>
        <DialogActionTrigger asChild>
          <Button variant="outline">Cancel</Button>
        </DialogActionTrigger>
        <Button onClick={handleLogin} type="submit">Login</Button>
      </DialogFooter>
    </>
  )
}