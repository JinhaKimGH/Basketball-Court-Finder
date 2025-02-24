import { Box, Field, Input, defineStyle } from "@chakra-ui/react";
import { useState } from "react";
import { InputGroup } from "./input-group";
import React from "react";
import { VisibilityTrigger } from "./visibility-trigger";
import { LuEye, LuEyeOff } from "react-icons/lu";

export const CustomInput = (props: {
  name: string,
  placeholder: string
  label: string,
  required: boolean,
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void,
  value: string,
  invalid: boolean,
  type?: string,
  disabled?: boolean,
}) => {
  const [visible, setVisible] = useState(false);
  
  return (
    <Field.Root invalid={props.invalid} required={props.required}>
      <Box pos="relative" w="full">
        {props.type === "password" ? (
          <InputGroup
            pos="relative"
            width="full"
            endElement={
              <VisibilityTrigger
                onPointerDown={(e) => {
                  if (e.button !== 0) return
                  e.preventDefault()
                  setVisible(!visible)
                }}
              >
                {visible ? <LuEyeOff/> : <LuEye/>}
              </VisibilityTrigger>
            }
          >
            <Box pos="relative" w="full">
              <Input
                className="peer"
                placeholder=" "
                name={props.name}
                value={props.value}
                onChange={props.onChange}
                disabled={props.disabled}
                type={visible ? "text" : "password"}
              />
              <Field.Label css={floatingStyles}>{props.label}</Field.Label>
            </Box>
          </InputGroup>
        ) : (
          <>
            <Input 
              className="peer"
              placeholder=""
              name={props.name}
              value={props.value}
              onChange={props.onChange}
              type={props.type}
              disabled={props.disabled}
            />
            <Field.Label css={floatingStyles}>{props.label}</Field.Label>
          </>
        )}
      </Box>
    </Field.Root>
  )
}

CustomInput.defaultProps = {
  disabled: false,
  required: false,
}

const floatingStyles = defineStyle({
  pos: "absolute",
  bg: "bg",
  px: "0.5",
  top: "-3",
  insetStart: "2",
  fontWeight: "normal",
  pointerEvents: "none",
  transition: "all 0.2s",
  transform: "translateY(0)",
  '.peer:placeholder-shown ~ &': {
    color: "fg.muted",
    transform: "translateY(24px)",
    insetStart: "3",
  },
  '.peer:focus ~ &': {
    color: "fg",
    transform: "translateY(0)",
    insetStart: "2",
  },
})