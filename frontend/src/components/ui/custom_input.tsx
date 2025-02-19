import { Box, Field, Input, defineStyle } from "@chakra-ui/react"

export const custom_input = (props: {
  name: string,
  placeholder: string
  label: string,
  required: boolean,
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void,
  value: string,
  invalid: boolean,
  type?: string
}) => {
  return (
    <Field.Root invalid={props.invalid} required={props.required}>
      <Box pos="relative" w="full">
        <Input 
          className="peer"
          placeholder=""
          name={props.name}
          value={props.value}
          onChange={props.onChange}
          type={props.type}
        />
        <Field.Label css={floatingStyles}>{props.label}</Field.Label>
      </Box>
    </Field.Root>
  )
}

const floatingStyles = defineStyle({
  pos: "absolute",
  bg: "bg",
  px: "0.5",
  top: "-3",
  insetStart: "2",
  fontWeight: "normal",
  pointerEvents: "none",
  transition: "position",
  _peerPlaceholderShown: {
    color: "fg.muted",
    top: "2.5",
    insetStart: "3",
  },
  _peerFocusVisible: {
    color: "fg",
    top: "-3",
    insetStart: "2",
  },
})