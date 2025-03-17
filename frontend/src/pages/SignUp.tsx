import React from "react"
import { validateEmail } from "../utils";
import { 
    Fieldset, 
    Image, 
    Heading,
    Flex,
    Box,
    Card,
    Button,
    Link
} from "@chakra-ui/react";
import {CustomInput as CInput} from "@/components/ui/custom_input";
import { LuCircleCheckBig } from "react-icons/lu";

/**
 * SignUp component 
 * 
 * @returns {JSX.Element} - The rendered sign-up form or a success message upon successful sign-up
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

  // Signed up Successfully
  const [signedUp, setSignedUp] = React.useState(false);
  
  // Error message for fields
  const [errorMessage, setErrorMessage] = React.useState('');

  // Loading state
  const [isLoading, setIsLoading] = React.useState(false);

  // Update signup data from fields
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setSignupData((prev) => ({...prev, [name]: value}));
  }

  // Signup Submission
  const handleSignUp = (e: React.FormEvent) => {
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

    setIsLoading(true);
    // Proceed with API call for sign-up
    fetch(`${baseApiUrl}/api/users/sign-up`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(signupData),
    }).then(
      (res) => {
        if (res.status === 201) {
          // Successful creation -> Hide the form
          setSignedUp(true);
          setIsLoading(false);
          return;
        }
        return res.text().then(text => {
          switch (res.status) {
            case 409:
              setErrorMessage(text ||  "User with that email already exists.");
              break;
            case 400:
              setErrorMessage(text || "Email or passwords are invalid.");
              break;
            default:
              setErrorMessage('An unexpected error occurred.');
          }
          setIsLoading(false);
          throw new Error(`HTTP error! status: ${res.status}, message: ${text}`);
        });
      })
      .catch((error) => {
        console.error("Error signing up: ", error);
        // TODO: Replace with logging later
      });
  }

  return(
    <Card.Root
      position="fixed"
      top={{base: "0", md: "50%"}}
      left={{base: "0", md: "50%"}}
      transform={{base: "none", md: "translate(-50%, -50%)"}}
      boxShadow={{base: "none", md: "md"}}
      width={{base: "100%", md: "60%", lg: "400px"}}
      height={{lg: signedUp ? "230px" : "550px"}}
      border={{base: "none", md: "1px solid"}}
      borderColor={{base: "transparent", md: "gray.200"}}
    >
      { !signedUp &&
        <Card.Header>
          <Image
            src="/assets/basket.png"
            alt="Basket Icon"
            boxSize="50px"
            objectFit="cover"
          />
        </Card.Header>
      }
        <Card.Body
          padding="6"
        >
          { signedUp ?
            <Flex direction="column" justifyContent="center" alignItems="center" gap="3">
              <LuCircleCheckBig color="#81C784" size="50"/>
              <Heading textAlign="center" fontSize="18px" fontWeight="400">
                You have successfully signed up!
              </Heading>
              <p>
                Please proceed with login.
              </p>
            </Flex> :
            <Flex
              direction="column"
              justifyContent="space-between"
              gap={7}
            >
              <Heading fontWeight={500} fontSize="3xl" lineHeight={"32px"}>Create an Account!</Heading>
              <Box
                width={{base: "100%"}}
              >
                <Fieldset.Root size={{base: "sm", md: "lg"}} invalid>
                  <Fieldset.Content gap={5}>
                    <CInput
                      name="email"
                      placeholder="Email"
                      label="Email"
                      required
                      onChange={handleChange}
                      value={signupData.email}
                      invalid={errorMessage.includes('Email')}
                    />

                    <CInput
                      name="displayName"
                      placeholder="Display Name"
                      label="Display Name"
                      required
                      onChange={handleChange}
                      value={signupData.displayName}
                      invalid={errorMessage.includes('Display name')}
                    />

                    <CInput
                      name="password"
                      placeholder="Password"
                      label="Password"
                      required
                      onChange={handleChange}
                      value={signupData.password}
                      invalid={errorMessage.includes('Password')}
                      type="password"
                    />

                    <CInput
                      name="reenterPassword"
                      placeholder="Re-enter Password"
                      label="Re-enter Password"
                      required
                      onChange={handleChange}
                      value={signupData.reenterPassword}
                      invalid={errorMessage.includes('Password')}
                      type="password"
                    />
                  </Fieldset.Content>
                  <Fieldset.ErrorText>
                    {errorMessage}
                  </Fieldset.ErrorText>
                </Fieldset.Root> 
              </Box>
            </Flex>
          }
        </Card.Body>
        <Flex
          justifyContent={{base: "center", md: signedUp ? "center" : "flex-end"}}
          padding="6"
          paddingTop="0"
          gap="6"
        >
          {signedUp ? 
            <Link 
              href="/log-in" 
              variant="underline" 
              _focus={{ 
                boxShadow: 'none',
                outline: 'none' 
              }}
            >
              Continue to Login
            </Link> :
            <Button 
              backgroundColor={"orange.500"} 
              onClick={handleSignUp} 
              type="submit" 
              loading={isLoading} 
              width="90px"
            >
              Sign Up
            </Button> 
          }
        </Flex>
    </Card.Root>
  )
}
