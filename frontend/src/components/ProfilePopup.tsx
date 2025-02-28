import { AuthContext } from "@/context/AuthContext";
import { Avatar, Box, Button, Flex, Heading } from "@chakra-ui/react";
import { Tooltip } from "@/components/ui/tooltip";
import { useContext, useState } from "react";
import { LuCircleUserRound, LuLogIn, LuLogOut } from "react-icons/lu";
import { useNavigate } from "react-router-dom";
import { pickPalette } from "@/utils";

/**
 * SignOut Component
 * 
 * @returns {JSX.Element}
 */
export default function ProfilePopup(
  props : {
    iconSize: number
  }) {

  // For navigation 
  const navigate = useNavigate();

  // User Authentication Information
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }
  const isLoggedIn = authContext.isLoggedIn;
  const displayName = authContext.user?.displayName || "Guest";
  const email = authContext.user?.email || "guest@email.com";

  // State to control the visibility of the container
  const [isOpen, setIsOpen] = useState(false);

  // Toggle the container when clicking on the trigger
  const handleToggle = () => setIsOpen((prev) => !prev);

  return (
    <>
      <Tooltip content="Profile" openDelay={100}>
        <Box onClick={handleToggle} cursor="pointer">
          {
            isLoggedIn ? 
            <Avatar.Root colorPalette={pickPalette(displayName)} size={{base: "xs", lg: "md"}}>
              <Avatar.Fallback name={displayName} tabIndex={0} />
            </Avatar.Root>
            :
            <LuCircleUserRound size={props.iconSize} color="#45a2ff" cursor="pointer" />
          }
        </Box>
      </Tooltip>

      <Box
        position="fixed"
        top="0"
        left="0"
        right="0"
        bottom="0"
        bg="transparent"
        zIndex={99}
        onClick={() => setIsOpen(false)}
        display={isOpen ? "block" : "none"}
      />
      <Box
        position="fixed"
        top="calc(1rem + 60px)"
        right="2rem"
        zIndex={100}
        borderRadius="md"
        boxShadow="md"
        padding={10}
        bg="white"
        opacity={isOpen ? 1 : 0}
        transform={isOpen ? "translateY(0)" : "translateY(-10px)"}
        transition="all 0.2s ease-in-out"
        visibility={isOpen ? "visible" : "hidden"}
        pointerEvents={isOpen ? "auto" : "none"}
      >
        { isLoggedIn ? 
          <Flex direction="column" alignItems="center" gap="5">
            <Box as="p" fontSize="xs">{email}</Box>
            <Avatar.Root colorPalette={pickPalette(displayName)} size="2xl">
              <Avatar.Fallback name={displayName} />
            </Avatar.Root>
            <Heading size="xl" fontWeight={400}>Hi, {displayName}!</Heading>
            <Button 
              colorPalette="blue"
              variant="surface"
              rounded="md"
              width="100%" 
              onClick={() => navigate("/profile")}
            >
              Manage Your Profile
            </Button>
            <Button 
              colorPalette="blue"
              variant="surface"
              rounded="md"
              width="100%" 
              onClick={() => {
                handleToggle();
                authContext.logout();
              }}
            >
              <LuLogOut/> Sign Out
            </Button>
          </Flex>

          : 

          <Flex direction="column" alignItems="center" gap="5">
            <Heading size="lg" fontWeight={400}>You are not logged in.</Heading>
            <Button 
              colorPalette="blue"
              variant="surface"
              rounded="md"
              width="100%" 
              onClick={() => navigate("/log-in")}
            >
              <LuLogIn/> Log In
            </Button>
          </Flex>
        }
      </Box>
    </>
  );
}
