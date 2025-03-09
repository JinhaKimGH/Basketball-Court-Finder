import { useContext, useRef, useState } from "react";
import { DialogBody, DialogFooter } from "./ui/dialog";
import { Avatar, Button, Fieldset, Flex, RatingGroup, Text, Textarea } from "@chakra-ui/react";
import { AuthContext } from "@/context/AuthContext";
import { pickPalette } from "@/utils";
import { InfoTip } from "./ui/toggle-tip";
import { Review, ReviewData } from "@/interfaces";

export default function ReviewForm(
  props: {
    courtName : string,
    courtId: number,
    setOpen: React.Dispatch<React.SetStateAction<boolean>>,
    existingReview?: Review,
    setReviewData: React.Dispatch<React.SetStateAction<ReviewData>>,
  }
) {
  // User Authentication Information
  const authContext = useContext(AuthContext);
  if (!authContext) {
    throw new Error("AuthContext is null");
  }
  const displayName = authContext.user?.displayName || "Guest";
  
  const formRef = useRef<HTMLFormElement>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(""); 

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (formRef.current) {
      setErrorMessage("");
      const formData = new FormData(formRef.current);
      const values = {...Object.fromEntries(formData.entries()), courtId: props.courtId};
      
      setIsLoading(true);
      if (!props.existingReview) {
        try {
          const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/api/review`,
            {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              credentials: 'include',
              body: JSON.stringify(values)
            }
          );
  
          if (response.status == 201) {
            setIsLoading(false);
            props.setOpen(false);
          } else {
            response.text().then(text => {
              setIsLoading(false);
              setErrorMessage(text);
              throw new Error(text || "Rating fetch issue");
            })
          }
        } catch (e) {
          console.error("Error posting review:", e);
        }
      }
      else {
        fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/api/review/${props.existingReview.reviewId}`,
          {
            method: 'PATCH',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(values)
          }
        ).then(
          (res) => {
            if (res.ok) {
              return res.json();
            } else {
              setIsLoading(false);
              setErrorMessage("Error updating review.");
              throw new Error(`HTTP error! status: ${res.status}`)
            }
          }
        ).then(async (data) => {
          props.setReviewData((prev: ReviewData) => ({ ...prev, userReview: data }))
          setIsLoading(false);
          props.setOpen(false);
        }).catch((e) => {
          console.error("Error updating review: ", e);
          //TODO: Replace with newrelic logging
        });
      }
    } 
  }
  
  return (
    <form ref={formRef} onSubmit={handleSubmit}>
      <DialogBody p={5} pb={0}>
        <Fieldset.Root invalid>
          <Fieldset.Legend fontSize={"20px"} fontWeight={300} textAlign={"center"}>
            {
              `${props.existingReview ? 'Edit' : 'Add'} Review for ${props.courtName}`
            }
          </Fieldset.Legend>

          <Fieldset.Content width="100%" justifyContent="space-around" gap={7} mt={7}>

            <Flex alignItems="center" gap={3}>
              <Avatar.Root colorPalette={pickPalette(displayName)} size="lg">
                <Avatar.Fallback name={displayName} tabIndex={0} />
              </Avatar.Root>
              <Text fontSize="18px">
                {displayName}
              </Text>

              <InfoTip content="Posts will appear publicly with your profile name and picture." />
            </Flex>
            
            <RatingGroup.Root 
              count={5} 
              size="lg"
              colorPalette="yellow"
              justifyContent="space-around"
              defaultValue={props?.existingReview?.rating}
            >
              <RatingGroup.HiddenInput name="rating" />
              <RatingGroup.Control>
                {Array.from({ length: 5 }).map((_, index) => (
                  <RatingGroup.Item key={index} index={index + 1} cursor={"pointer"}>
                    <RatingGroup.ItemIndicator />
                  </RatingGroup.Item>
                ))}
              </RatingGroup.Control>
            </RatingGroup.Root>

            <Textarea
              size="lg"
              name="body"
              defaultValue={props?.existingReview?.content}
            />
            <Fieldset.ErrorText>
              {errorMessage}
            </Fieldset.ErrorText>
          </Fieldset.Content>
        </Fieldset.Root>
      </DialogBody>
      <DialogFooter>
        <Button type="submit" loading={isLoading} backgroundColor="orange.500">Post</Button>
      </DialogFooter>
    </form>
  )
}