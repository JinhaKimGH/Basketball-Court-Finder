import { Button, Flex, Text, VStack } from "@chakra-ui/react";
import { HiOutlinePencilSquare } from "react-icons/hi2";
import { BsSortDown } from "react-icons/bs";
import {
  MenuContent,
  MenuItem,
  MenuRoot,
  MenuTrigger,
} from "@/components/ui/menu";
import { DialogContent, DialogRoot, DialogTrigger } from "@/components/ui/dialog";
import ReviewForm from "./ReviewForm";
import { useState } from "react";

export default function Reviews(
  props: {
    courtId : number,
    courtName: string,
  }
) {

  const [open, setOpen] = useState(false);

  const [value, setValue] = useState(null); // Get court and display/set button text to edit and be an edit form.

  const fetchExistingReview = async () => {

  }
  
  return (
    <VStack>
      <Flex
        justifyContent={"space-between"}
        width={{base: "100%", md: "70%", lg: "100%"}}
      >
        <DialogRoot lazyMount open={open} onOpenChange={(e) => setOpen(e.open)}>
          <DialogTrigger asChild>
            <Button
              rounded="full"
              variant="outline"
              color="orange.500"
            >
              <HiOutlinePencilSquare/>
              <Text
                color="gray.700"
              >
                Write a review
              </Text>
            </Button>
          </DialogTrigger>

          <DialogContent maxWidth={{base: "80%", md: "500px", lg: "500px"}}>
            <ReviewForm courtName={props.courtName} courtId={props.courtId} setOpen={setOpen}/>
          </DialogContent>
        </DialogRoot>
        <MenuRoot>
          <MenuTrigger asChild>
            <Button
              rounded="full"
              variant="outline"
              color="orange.500"
            >
              <BsSortDown/>
              <Text
                color="gray.700"
              >
                Sort
              </Text>
            </Button>
          </MenuTrigger>
          <MenuContent>
            <MenuItem value="new">
              Newest
            </MenuItem>
            <MenuItem value="rating-low">
              Lowest rating
            </MenuItem>
            <MenuItem value="rating-high">
              Highest rating
            </MenuItem>
          </MenuContent>
        </MenuRoot>
      </Flex>
    </VStack>
  )
}