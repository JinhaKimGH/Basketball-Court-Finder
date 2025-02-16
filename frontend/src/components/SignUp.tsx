import React from "react"
import { validateEmail } from "../utils";
import { Field } from "@/components/ui/field";
import { 
    DialogBody, 
    Fieldset, 
    Input, 
    DialogCloseTrigger, 
    DialogFooter, 
    DialogActionTrigger, 
    Button 
} from "@chakra-ui/react";

/**
 * SignUpForm Component
 * 
 * @returns {JSX.Element}
 */
export default function SignUp() : JSX.Element{

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  // Sign up form data
  const [signupData, setSignupData] = React.useState({
    email: "",
    displayName: "",
    password: "",
    reenterPassword: ""
  });
  
  // Error message for fields
  const [errorMessage, setErrorMessage] = React.useState('');

  // Update signup data from fields
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setSignupData((prev) => ({...prev, [name]: value}));
  }

  // Signup Submission
  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!validateEmail(signupData.email)) {
      setErrorMessage('Email must be valid.'); 
      return;
    }

    // Display Name Validation
    if (signupData.displayName.trim().length < 3 || signupData.displayName.length > 20) {
      setErrorMessage('Display name must be between 3 and 20 characters.');
      return;
    }
    if (!/^[a-zA-Z0-9 ]+$/.test(signupData.displayName)) {
      setErrorMessage('Display name can only contain letters, numbers, and spaces.');
      return;
    }

    // Password Validation
    if (signupData.password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long.');
      return;
    }
    if (!/[A-Z]/.test(signupData.password) || !/[a-z]/.test(signupData.password) || !/[0-9]/.test(signupData.password) || !/[\W_]/.test(signupData.password)) {
      setErrorMessage('Password must include uppercase, lowercase, number, and special character.');
      return;
    }

    // Confirm Password Match
    if (signupData.password !== signupData.reenterPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }

    // Proceed with API call for sign-up
  }

  return(
    <>
      <DialogBody>
        <Fieldset.Root size={{base: "sm", md: "lg"}} invalid>
          <Fieldset.Content>
            <Field label="Email Address" required>
              <Input 
                name="email"
                value={signupData.email}
                onChange={handleChange}
              />
            </Field>

            <Field label="Display Name" required>
              <Input 
                name="displayName"
                value={signupData.displayName}
                onChange={handleChange}
              />
            </Field>

            <Field label="Password" required>
              <Input 
                name="password" 
                value={signupData.password}
                onChange={handleChange}
                type="password"
              />
            </Field>

            <Field label="Re-enter Password" required>
              <Input 
                name="reenterPassword" 
                value={signupData.reenterPassword}
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
        <Button onClick={handleLogin} type="submit">Sign Up</Button>
      </DialogFooter>
    </>
  )
}