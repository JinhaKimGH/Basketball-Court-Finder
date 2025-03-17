import { 
  Button, 
  Fieldset, 
  Flex, 
  Image, 
  Card, 
  Heading, 
  Box, 
  Link,
} from "@chakra-ui/react";
import {CustomInput as Input} from "@/components/ui/custom_input";
import { validateEmail } from "../utils";
import React, {useContext} from "react";
import { AuthContext } from "@/context/AuthContext";
import { useNavigate } from "react-router-dom";

/**
 * Login Component
 * 
 * @param props - Component props
 * @param {React.Dispatch<React.SetStateAction<boolean>>} props.setOpen - Function to set the open state of the form
 * 
 * @returns {JSX.Element} - The rendered login form
 */
export default function Login() : JSX.Element{


  // Used to navigate routes
  const navigate = useNavigate();

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  // Auth State setter
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }
  const { setAuthState, isLoggedIn } = authContext;

  // Form login data
  const [loginData, setLoginData] = React.useState({
    email: "",
    password: ""
  });
  
  const [emailValid, setEmailValid] = React.useState(true);

  const [errorMessage, setErrorMessage] = React.useState('');

  // Loading state
  const [isLoading, setIsLoading] = React.useState(false);

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
      setErrorMessage('Email must be valid.');
      return;
    }
    setIsLoading(true);

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
          return res.json();
        } else {
          switch (res.status) {
            case 401:
              setErrorMessage("Incorrect email or password.");
              break;
            default:
              setErrorMessage('An unexpected error occurred.');
          }
          setIsLoading(false);
          throw new Error(`HTTP error! status: ${res.status}`);
        }
      })
      .then(async (data) => {
        setIsLoading(false);
        await setAuthState({isLoggedIn: true, user: data});
        return navigate("/");
      })
      .catch((error) => {
        console.error("Error logging in: ", error);
        // TODO: Replace with logging later
      });
  }

  // On page load, if user is logged in redirect to home
  React.useEffect(() => {
    if (isLoggedIn) {
      navigate("/");
    }
  }, [isLoggedIn, navigate]);

  return(
     <Card.Root
      position="fixed"
      top={{base: "0", md: "50%"}}
      left={{base: "0", md: "50%"}}
      transform={{base: "none", md: "translate(-50%, -50%)"}}
      boxShadow={{base: "none", md: "md"}}
      width={{base: "100%", md: "60%", lg: "700px"}}
      height={{lg:"330px"}}
      border={{base: "none", md: "1px solid"}}
      borderColor={{base: "transparent", md: "gray.200"}}
     >
      <Card.Header>
        <Image
          src="./assets/basket.png"
          alt="Basket Icon"
          boxSize="50px"
          objectFit="cover"
        />
      </Card.Header>
      <Card.Body
        padding="6"
      >
        <Flex
          direction={{base: "column", lg: "row"}}
          justifyContent="space-between"
          gap={7}
        >
          <Flex
            direction="column"
            gap="2"
          >
            <Heading fontWeight={500} fontSize="4xl">Log In</Heading>
            <p>to continue to Basketball Court Finder</p>
          </Flex>
          <Box
            width={{base: "100%", md: "100%", lg: "60%"}}
          >
            <Fieldset.Root size={{base: "sm", md: "lg"}} invalid>
              <Fieldset.Content gap={5}>
                <Input
                  name="email"
                  placeholder="Email"
                  label="Email"
                  required
                  onChange={handleChange}
                  value={loginData.email}
                  invalid={!emailValid}
                />

                <Input
                  name="password"
                  placeholder="Password"
                  label="Password"
                  required
                  onChange={handleChange}
                  value={loginData.password}
                  invalid={false}
                  type="password"
                />
              </Fieldset.Content>
              <Fieldset.ErrorText marginTop={4}>
                {errorMessage}
              </Fieldset.ErrorText>
            </Fieldset.Root>
          </Box>
        </Flex>
      </Card.Body>

      <Flex
        justifyContent={{base: "space-between", md: "flex-end"}}
        padding="6"
        paddingTop="0"
        gap="6"
      >
        <Link 
          href="/sign-up" 
          variant="underline" 
          border="none"
          _focus={{ 
            boxShadow: 'none',
            outline: 'none' 
          }}
        >
          Create an Account
        </Link>
        <Button onClick={handleLogin} type="submit" loading={isLoading} width="90px" backgroundColor={"orange.500"}>Login</Button>
      </Flex>
    </Card.Root>
  )
}