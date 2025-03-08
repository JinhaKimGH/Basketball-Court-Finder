import { Alert, Button, Fieldset, Flex, Input, NativeSelect, Stack } from "@chakra-ui/react";
import { useRef, useState } from "react";
import { DialogBody, DialogFooter, DialogCloseTrigger } from "./ui/dialog";
import { NumberInputField, NumberInputRoot } from "./ui/number-input";
import { isValidPhoneNumber, isValidWebsite } from "@/utils";
import { withMask } from "use-mask-input";
import { BasketballCourt } from "@/interfaces";
import OpeningHoursField from './OpeningHoursField';
import React from "react";

export type FieldType = 'website' | 'phone' | 'opening_hours' | 'hoops' | 'surface' | 
                 'indoor' | 'netting' | 'rim_type' | 'rim_height' | 'amenity' | 'name'; 

export default function CourtEditForm(
  props: {
    field: FieldType,
    id: number,
    setCourts: React.Dispatch<React.SetStateAction<BasketballCourt[]>>,
    court: BasketballCourt,
    index: number // Place in the array
  }
) {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");  
  const [showSuccess, setShowSuccess] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);

  // Add timeout ref to clean up
  const successTimeoutRef = useRef<number>();

  // Cleanup on unmount
  React.useEffect(() => {
    return () => {
      if (successTimeoutRef.current) {
        window.clearTimeout(successTimeoutRef.current);
      }
    };
  }, []);

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (formRef.current) {
      setErrorMessage("");
      const formData = new FormData(formRef.current);
      const values = Object.fromEntries(formData.entries());

      // Convert empty strings to null for numeric fields
      const processedValues = Object.fromEntries(
        Object.entries(values).map(([key, value]) => [
          key,
          value === "" ? null : value,
        ])
      );

      if (!processedValues[props.field]) {
        setErrorMessage("Field must be filled");
        return;
      }

      if(props.field == "website" && !isValidWebsite(processedValues.website as string)) {
        setErrorMessage("Invalid website.");
        return;
      }

      if(props.field == "phone" && !isValidPhoneNumber(processedValues.phone as string)) {
        setErrorMessage("Invalid phone number.");
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
          const updatedCourt = await response.json();
          // Update the courts array with the new data
          props.setCourts(prevCourts => 
            prevCourts.map((court, i) => 
              i === props.index ? updatedCourt : court
            )
          );
          setShowSuccess(true);
          
          // Auto hide after 3 seconds and close dialog
          successTimeoutRef.current = window.setTimeout(() => {
            setShowSuccess(false);
          }, 3000);
        } else {
          const text = await response.text();
          setErrorMessage(text || "Something went wrong");
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      } catch (e) {
        console.error("Error updating basketball court information: ", e); // TODO: New Relic Logging
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
            <Input id="website" name="website" borderColor={errorMessage ? "red" : "grey"}/>
          </div>
        );
      case 'phone':
        return (
          <div>
            <label htmlFor="phone">Phone Number</label>
            <Input id="phone" name="phone" ref={withMask("999-999-9999")} borderColor={errorMessage ? "red" : "grey"}/>
          </div>
        );
      case 'hoops':
        return (
          <div>
            <label htmlFor="hoops">Number of Hoops</label>
            <NumberInputRoot min={0} max={100}>
              <NumberInputField id="hoops" name="hoops" borderColor={errorMessage ? "red" : "grey"}/>
            </NumberInputRoot>
          </div>
        );
      case 'surface':
        return (
          <div>
            <label htmlFor="surface">Surface Type</label>
            <Input id="surface" name="surface" borderColor={errorMessage ? "red" : "grey"}/>
          </div>
        );
      case 'amenity':
        return (
          <div>
            <label htmlFor="amenity">Amenity</label>
            <Input id="amenity" name="amenity" borderColor={errorMessage ? "red" : "grey"}/>
          </div>
        );
      case 'netting':
        return (
          <div>
            <label htmlFor="netting">Net Type</label>
            <NativeSelect.Root>
              <NativeSelect.Field id="netting" name="netting" borderColor={errorMessage ? "red" : "grey"}>
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
              <NativeSelect.Field id="rim_type" name="rim_type" borderColor={errorMessage ? "red" : "grey"}>
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
              <NumberInputField id="rim_height" name="rim_height" borderColor={errorMessage ? "red" : "grey"}/>
            </NumberInputRoot>
          </div>
        );
      case 'name':
        return (
          <div>
            <label htmlFor="name">Name</label>
            <Input id="name" name="name" borderColor={errorMessage ? "red" : "grey"}/>
          </div>
        );
      case 'opening_hours':
        return (
          <div>
            <OpeningHoursField
              onChange={(value) => {
                if (formRef.current) {
                  const input = formRef.current.querySelector('input[name="opening_hours"]');
                  if (input) {
                    (input as HTMLInputElement).value = value;
                  }
                }
              }}
              value={props.court.opening_hours}
            />
            <input type="hidden" name="opening_hours" />
          </div>
        );
      // Add other cases for each field type
      default:
        return null;
    }
  };

  return (
    <form ref={formRef} onSubmit={handleSubmit}>
      <DialogBody pb={1}>
        <Fieldset.Root pt={5} invalid>
          <Stack>
            <Fieldset.Legend>{`Update ${props.field.split("_").join(' ')}`}</Fieldset.Legend>
            <Fieldset.HelperText>
              {`Please provide the correct ${props.field.split("_").join(' ')} information.`}
            </Fieldset.HelperText>
          </Stack>

          <Fieldset.Content width="100%">
            {renderField(props.field)}
          </Fieldset.Content>
          <Fieldset.ErrorText marginTop={4}>
            {errorMessage}
          </Fieldset.ErrorText>
        </Fieldset.Root>
        {showSuccess && (
          <Alert.Root status="success">
            <Alert.Indicator />
            <Alert.Title>Success!</Alert.Title>
              <Alert.Description>
                {`The ${props.field.split("_").join(" ")} has been updated.`}
              </Alert.Description>
          </Alert.Root>
        )}
      </DialogBody>
      <DialogFooter>
        <Button type="submit" loading={isLoading}>Submit</Button>
      </DialogFooter>
      <DialogCloseTrigger />
    </form>
  )
}
