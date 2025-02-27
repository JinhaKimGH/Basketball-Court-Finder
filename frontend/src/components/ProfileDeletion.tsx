
import { AuthContext } from "@/context/AuthContext";
import { Button, Flex, Heading, Text } from "@chakra-ui/react";
import React from "react";
import { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { Checkbox } from "@/components/ui/checkbox";


export default function ProfileDeletion() {
  const [isLoading, setIsLoading] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState('');

  const [isChecked, setIsChecked] = React.useState(false);

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  const navigate = useNavigate();

  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }

  const { setAuthState } = authContext;

  // Delete Submission
  const handleDelete = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isChecked) {
      setErrorMessage('Please check the box to proceed.');
      return;
    }
    
    setIsLoading(true);

    fetch(`${baseApiUrl}/api/users`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    }).then(
      (res) => {
        if (res.ok) {
          return;
        } else {
            setErrorMessage('An unexpected error occurred.');
          setIsLoading(false);
          throw new Error(`HTTP error! status: ${res.status}`);
        }
      })
      .then(async () => {
        setIsLoading(false);
        await setAuthState({isLoggedIn: false, user: null});
        return navigate("/log-in");
      })
      .catch((error) => {
        console.error("Error deletign account in: ", error);
        // TODO: Replace with logging later
      });
  }
  

  return (
    <Flex
      direction={"column"}
    >
      <Flex
        padding={5}
        direction={"column"}
      > 
        <Heading size="lg">Delete Profile</Heading>
        <p>Deleting your profile will remove your user details from the system, but your reviews and ratings will remain.</p>
      </Flex>
      <Text textStyle="xs" color={"#ef4444"} paddingLeft={5}>{errorMessage}</Text>
      <Flex 
        padding={5}
        marginTop={{base: "0", lg: "60px"}}
        width="100%" 
        justifyContent={"flex-end"}
        flex="1"
      >
        <Checkbox 
          variant="subtle"
          checked={isChecked}
          onChange={() => setIsChecked(prev => !prev)} // Toggle checkbox state
        >
          I understand & confirm.
        </Checkbox>
        <Button 
          colorPalette="red" 
          marginLeft={"auto"} 
          loading={isLoading}
          onClick={handleDelete}
          disabled={!isChecked}
        >
          Delete
        </Button>
      </Flex>
    </Flex>
  );
}
