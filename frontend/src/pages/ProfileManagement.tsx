import { AuthContext } from "@/context/AuthContext";
import { Avatar, Card, Flex, Heading, Tabs } from "@chakra-ui/react";
import React from "react";
import { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { pickPalette } from "@/utils";
import { LuChartLine, LuUserRoundPen } from "react-icons/lu";

export default function ProfileManagement() : JSX.Element {

  // Used to navigate routes
  const navigate = useNavigate();
  
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }

  const { isLoggedIn } = authContext;
  const displayName = authContext.user?.displayName || "Guest";
  const email = authContext.user?.email || "guest@email.com";

  // Update form data
  const [updateData, setUpdateData] = React.useState({
    email: "",
    displayName: "",
    password: "",
  });
  
  // On page load, if user is logged in redirect to home
  React.useEffect(() => {
    if (!isLoggedIn) {
      navigate("/log-in");
    }
  }, [isLoggedIn, navigate]);

  return (
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
        <Avatar.Root colorPalette={pickPalette(displayName)} size="2xl">
          <Avatar.Fallback name={displayName} />
        </Avatar.Root>
      </Card.Header>
      <Card.Body
        padding="6"
      >
        <Tabs.Root defaultValue="stats" variant={"subtle"} orientation="vertical">
          <Tabs.List>
            <Tabs.Trigger value="stats">
              <LuChartLine/>
              User Stats
            </Tabs.Trigger>
            <Tabs.Trigger value="edit">
              <LuUserRoundPen/>
              Edit Profile
            </Tabs.Trigger>
          </Tabs.List>
          <Tabs.Content value="stats">
            User Statistics
          </Tabs.Content>
          <Tabs.Content value="edit">
            User Statistics
          </Tabs.Content>
        </Tabs.Root>
      </Card.Body>
    </Card.Root>
  )
}