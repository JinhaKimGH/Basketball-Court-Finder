import { Button, Fieldset, Stack } from "@chakra-ui/react";
import { useRef } from "react";
import { DialogBody, DialogFooter, DialogCloseTrigger } from "./ui/dialog";
import { NumberInputField, NumberInputRoot } from "./ui/number-input";

export default function CourtEditForm() {
  const formRef = useRef<HTMLFormElement>(null)

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (formRef.current) {
      const formData = new FormData(formRef.current);
      const values = Object.fromEntries(formData.entries());
      console.log(values); // Logs form data as an object
    }
  };

  // surface: string,
  // address: Address,
  // amenity: string,
  // website: string,
  // opening_hours: string,
  // netting?: number,
  // rim_type?: number,
  // rim_height?: number,
  // phone: string,
  // indoor: boolean

  return (
    <form ref={formRef} onSubmit={handleSubmit}>
      <DialogBody>
        <Fieldset.Root pt={2}>
          <Stack>
            <Fieldset.Legend>Court details</Fieldset.Legend>
            <Fieldset.HelperText>
              Provide ammendments or new details for courts. Please only update fields that you know of and leave other fields blank or filled.
            </Fieldset.HelperText>
          </Stack>

          <Fieldset.Content>
            <label htmlFor="hoops">Number of Hoops</label>
            <NumberInputRoot min={0} max={100}>
              <NumberInputField id="hoops" name="hoops"/>
            </NumberInputRoot>
          </Fieldset.Content>
        </Fieldset.Root>
      </DialogBody>
      <DialogFooter>
        <Button type="submit">Submit</Button>
      </DialogFooter>
      <DialogCloseTrigger />
    </form>
  )
}