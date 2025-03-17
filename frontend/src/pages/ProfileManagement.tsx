import { AuthContext } from "@/context/AuthContext";
import { Avatar, Card, DataList, Flex, Heading, Tabs } from "@chakra-ui/react";
import React from "react";
import { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { pickPalette, trustSymbol } from "@/utils";
import ProfileEdit from "@/components/ProfileEdit";
import { LuChartLine, LuUserRound, LuUserRoundPen, LuUserRoundX, LuMail, LuTally5 } from "react-icons/lu";
import { InfoTip } from "@/components/ui/toggle-tip";
import ProfileDeletion from "@/components/ProfileDeletion";

export default function ProfileManagement() : JSX.Element {

  // Used to navigate routes
  const navigate = useNavigate();

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;
  
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }

  const { isLoggedIn } = authContext;
  const displayName = authContext.user?.displayName || "Guest";
  const email = authContext.user?.email || "guest@email.com";

  // User statistics data
  const [userStats, setUserStats] = React.useState({
    "trust": 0,
    "review_count": 0,
  });

  // On page load, if user is logged in redirect to home
  React.useEffect(() => {
    if (!isLoggedIn) {
      navigate("/log-in");
    }
  }, [isLoggedIn, navigate]);

  // Get user stats on page load
  React.useEffect(() => {
    fetch(`${baseApiUrl}/api/users/stats`, {
      method: "GET",
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    }).then(
      (res) => {
        if (res.ok) {
          return res.json();
        } else {
          throw new Error(`HTTP error! status: ${res.status}`);
        }
    }).then(
      (data) => {
        setUserStats(data);
      }
    ).catch((error) => {
        console.error('Error getting statistics:', error);
        //TODO: REPLACE WITH LOGGING LATER
      });
  }, []);

  return (
    <Card.Root
      position="fixed"
      top={{base: "0", md: "50%"}}
      left={{base: "0", md: "50%"}}
      transform={{base: "none", md: "translate(-50%, -50%)"}}
      boxShadow={{base: "none", md: "md"}}
      width={{base: "100%", md: "60%", lg: "700px"}}
      height={{lg:"450px"}}
      border={{base: "none", md: "1px solid"}}
      borderColor={{base: "transparent", md: "gray.200"}}
    >
      <Card.Header>
        <Flex
          justifyContent="space-between"
        >
          <div>
            <Heading size="2xl">Profile Details</Heading>
            <p>Manage and view your profile</p>
          </div>
          <Avatar.Root colorPalette={pickPalette(displayName)} size="2xl">
            <Avatar.Fallback name={displayName} />
          </Avatar.Root>
        </Flex>
      </Card.Header>
      <Card.Body>
        <Tabs.Root defaultValue="stats" variant={"outline"} height="100%">
          <Tabs.List>
            <Tabs.Trigger value="stats">
              <LuChartLine/>
              User Stats
            </Tabs.Trigger>
            <Tabs.Trigger value="edit">
              <LuUserRoundPen/>
              Edit Profile
            </Tabs.Trigger>
            <Tabs.Trigger value="delete">
              <LuUserRoundX/>
              Delete Profile
            </Tabs.Trigger>
          </Tabs.List>
          <Tabs.Content value="stats">
            <Flex
              gap={20}
              padding={5}
            >  
              <DataList.Root size="lg">
                <DataList.Item key="name">
                  <DataList.ItemLabel>
                    Username
                    <LuUserRound/>
                  </DataList.ItemLabel>
                  <DataList.ItemValue>{displayName}</DataList.ItemValue>
                </DataList.Item>
                <DataList.Item key="email">
                  <DataList.ItemLabel>
                    Email
                    <LuMail/>
                  </DataList.ItemLabel>
                  <DataList.ItemValue>{email}</DataList.ItemValue>
                </DataList.Item>
              </DataList.Root>

              <DataList.Root size="lg">
                <DataList.Item key="trust">
                  <DataList.ItemLabel>
                    Trust
                    {trustSymbol(userStats.trust)}
                    <InfoTip>A metric that scores the reliability of your reviews.</InfoTip>
                  </DataList.ItemLabel>
                  <DataList.ItemValue>{userStats.trust}</DataList.ItemValue>
                </DataList.Item>
                <DataList.Item key="review_count">
                  <DataList.ItemLabel>
                    Reviews
                    <LuTally5/>
                  </DataList.ItemLabel>
                  <DataList.ItemValue>{userStats.review_count}</DataList.ItemValue>
                </DataList.Item>
              </DataList.Root>
            </Flex>
          </Tabs.Content>
          <Tabs.Content value="edit" marginTop={4}>
            <ProfileEdit displayName={displayName} email={email}/>
          </Tabs.Content>
          <Tabs.Content value="delete" height="90%">
            <ProfileDeletion/>
          </Tabs.Content>
        </Tabs.Root>
      </Card.Body>
    </Card.Root>
  )
}