import { Button, Fieldset, Flex, Input, NativeSelect, Stack } from "@chakra-ui/react";
import { useRef, useState } from "react";
import { DialogBody, DialogFooter, DialogCloseTrigger } from "./ui/dialog";
import { NumberInputField, NumberInputRoot } from "./ui/number-input";
import { isValidPhoneNumber, isValidWebsite } from "@/utils";
import { withMask } from "use-mask-input";

export type FieldType = 'website' | 'phone' | 'opening_hours' | 'hoops' | 'surface' | 
                 'indoor' | 'netting' | 'rim_type' | 'rim_height' | 'amenity'; 
                 
//TODO:  OPENING HOURS. Add error messages (required inputs)

export default function CourtEditForm(
  props: {
    field: FieldType,
    id: number
  }
) {
  const [isLoading, setIsLoading] = useState(false);
  const formRef = useRef<HTMLFormElement>(null)

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (formRef.current) {
      const formData = new FormData(formRef.current);
      const values = Object.fromEntries(formData.entries());

      // Convert empty strings to null for numeric fields
      const processedValues = Object.fromEntries(
        Object.entries(values).map(([key, value]) => [
          key,
          value === "" ? null : value,
        ])
      );

      if(processedValues.website && !isValidWebsite(processedValues.website as string)) {
        console.log("FAIL");
        return;
      }

      if(processedValues.phone && !isValidPhoneNumber(processedValues.phone as string)) {
        console.log("Fail");
        return;
      }
      setIsLoading(true);
      try {
        const response = await fetch(`${baseApiUrl}/api/courts/${props.id}`,
          {
            method: 'PATCH',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(processedValues)
          }
        );
  
        if (response.ok) {
          // handle successful update -> maybe update the court's state?
        } else {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      } catch (e) {
        console.error("Error updating basketball court information: ", e); // todo better logging
      }
      setIsLoading(false);
    }
  };

  const renderField = (fieldName: FieldType) => {
    switch (fieldName) {
      case 'website':
        return (
          <div>
            <label htmlFor="website">Website</label>
            <Input id="website" name="website"/>
          </div>
        );
      case 'phone':
        return (
          <div>
            <label htmlFor="phone">Phone Number</label>
            <Input id="phone" name="phone" ref={withMask("999-999-9999")}/>
          </div>
        );
      case 'hoops':
        return (
          <div>
            <label htmlFor="hoops">Number of Hoops</label>
            <NumberInputRoot min={0} max={100}>
              <NumberInputField id="hoops" name="hoops"/>
            </NumberInputRoot>
          </div>
        );
      case 'surface':
        return (
          <div>
            <label htmlFor="surface">Surface Type</label>
            <Input id="surface" name="surface"/>
          </div>
        );
      case 'amenity':
        return (
          <div>
            <label htmlFor="amenity">Amenity</label>
            <Input id="amenity" name="amenity"/>
          </div>
        );
      case 'netting':
        return (
          <div>
            <label htmlFor="netting">Net Type</label>
            <NativeSelect.Root>
              <NativeSelect.Field id="netting" name="netting">
                <option value={0}>Not sure</option>
                <option value={1}>None</option>
                <option value={2}>Chain</option>
                <option value={3}>Nylon</option>
              </NativeSelect.Field>
              <NativeSelect.Indicator/>
            </NativeSelect.Root>
          </div>
        );
      case 'rim_type':
        return (
          <div>
            <label htmlFor="rim_type">Rim Type</label>
            <NativeSelect.Root>
              <NativeSelect.Field id="rim_type" name="rim_type">
                <option value={0}>Not sure</option>
                <option value={1}>Single</option>
                <option value={2}>1.5</option>
                <option value={3}>Double</option>
              </NativeSelect.Field>
              <NativeSelect.Indicator/>
            </NativeSelect.Root>
          </div>
        );
      case 'indoor':
        return (
          <>
            <label>Indoor or Outdoor</label>
            <Flex>
              <input type="radio" id="indoor" name="indoor" value={"true"}/>
              <label htmlFor="indoor" style={{marginLeft: "10px", marginRight: "20px"}}>Indoor</label>
              <input type="radio" id="outdoor" name="indoor" value={"false"}/>
              <label htmlFor="outdoor" style={{marginLeft: "10px"}}>Outdoor</label>
            </Flex>
          </>
        );
      case 'rim_height':
        return (
          <div>
            <label htmlFor="rim_height">Rim Height (ft)</label>
            <NumberInputRoot min={4} max={13}>
              <NumberInputField id="rim_height" name="rim_height"/>
            </NumberInputRoot>
          </div>
        );
      // Add other cases for each field type
      default:
        return null;
    }
  };

  return (
    <form ref={formRef} onSubmit={handleSubmit}>
      <DialogBody>
        <Fieldset.Root pt={5}>
          <Stack>
            <Fieldset.Legend>{`Update ${props.field.split("_").join(' ')}`}</Fieldset.Legend>
            <Fieldset.HelperText>
              {`Please provide the correct ${props.field.split("_").join(' ')} information.`}
            </Fieldset.HelperText>
          </Stack>

          <Fieldset.Content>
            {renderField(props.field)}
          </Fieldset.Content>
        </Fieldset.Root>
      </DialogBody>
      <DialogFooter>
        <Button type="submit" loading={isLoading}>Submit</Button>
      </DialogFooter>
      <DialogCloseTrigger />
    </form>
  )
}
