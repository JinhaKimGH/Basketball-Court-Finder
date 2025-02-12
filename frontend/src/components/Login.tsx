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
import React from "react";

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

  const [emailValid, setEmailValid] = React.useState(true);

  const [passwordValid, setPasswordValid] = React.useState(true);

  return(
    <DialogRoot 
      size={{ base: "sm", md: "sm", lg: "md" }}
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
          <Fieldset.Root size={{base: "sm", md: "lg"}}>
            <Fieldset.Content>
              <Field label="Email Address" invalid={!emailValid}>
                <Input name="email" />
              </Field>

              <Field label="Password" invalid={!passwordValid}>
                <Input name="password" type="password"/>
              </Field>
            </Fieldset.Content>

            {
              (!emailValid || !passwordValid) &&
              <Fieldset.ErrorText>
                Some fields are invalid. Please check them.
              </Fieldset.ErrorText>
            }
          </Fieldset.Root>
        </DialogBody>

        <DialogCloseTrigger />
        <DialogFooter>
          <DialogActionTrigger asChild>
            <Button variant="outline">Cancel</Button>
          </DialogActionTrigger>
          <Button>Login</Button>
        </DialogFooter>
      </DialogContent>
    </DialogRoot>
    )
}