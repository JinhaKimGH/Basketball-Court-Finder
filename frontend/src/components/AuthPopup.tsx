import { 
    DialogContent, 
    DialogRoot, 
    DialogTrigger, 
    Tabs, 
    DialogHeader } from "@chakra-ui/react";
import { LuCircleUserRound } from "react-icons/lu";
import { Tooltip } from "@/components/ui/tooltip";
import Login from "./Login";
import SignUp from "./SignUp";

export default function AuthPopup(
  props : {
    iconSize: number
  }
) {
  return (
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
        style={{
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          zIndex: 1000, // Ensure it's above other elements
        }}
        height={{base: "100%", "md": "inherit"}}
        minWidth={{base: "100%", "md": "inherit", "lg": "400px"}}
        maxWidth={{"md": "400px"}}
      >
        <DialogHeader>
          <Tabs.Root defaultValue="login" variant="line">
            <Tabs.List>
              <Tabs.Trigger value="login">
                Login
              </Tabs.Trigger>
              <Tabs.Trigger value="signup">
                Sign-Up
              </Tabs.Trigger>
            </Tabs.List>
            <Tabs.Content 
              value="login"
              style={{
                fontSize: "inherit",
                fontWeight: "inherit",
                fontFamily: "inherit",
                color: "inherit",
              }}
            >
              <Login/>
            </Tabs.Content>
            <Tabs.Content 
              value="signup"
              style={{
                fontSize: "inherit",
                fontWeight: "inherit",
                fontFamily: "inherit",
                color: "inherit",
              }}
            >
              <SignUp/>
            </Tabs.Content>
          </Tabs.Root>
        </DialogHeader>
      </DialogContent>
    </DialogRoot>
  )
}