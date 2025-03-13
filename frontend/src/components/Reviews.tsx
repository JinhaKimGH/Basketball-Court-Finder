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
import { useEffect, useState } from "react";
import { ReviewData } from "@/interfaces";
import ReviewCard from "./ReviewCard";

export default function Reviews(
  props: {
    courtId : number,
    courtName: string,
  }
) {

  const [open, setOpen] = useState(false);

  const [reviewData, setReviewData] = useState<ReviewData>({otherReviews: [{
    reviewId: 1,
    content: "I love this court!",
    totalVotes: 5,
    authorDisplayName: "Jeff",
    authorTrustScore: 52,
    upvoted: true,
    downvoted: false,
    createdAt: new Date(),
    isEdited: true,
    rating: 5
  }]}); // Get court and display/set button text to edit and be an edit form.

  const fetchExistingReview = async () => {
    fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/api/review?courtId=${props.courtId}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
    }).then(
      (res) => {
        if(res.ok) {
          return res.json();
        } else {
          throw new Error(`HTTP error! status: ${res.status}`)
        }
      })
      .then(async (data) => {
        setReviewData(data);
    }).catch((error) => {
      console.error("Error fetching review data: ", error);
      //TODO: Replace with newrelic logging
    })
  }

  // Fetch on component load
  useEffect(() => {
    fetchExistingReview();
  }, [props.courtId])
  
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
                {reviewData.userReview ?
                  'Edit your review' 
                  : 'Write a review'
                }
              </Text>
            </Button>
          </DialogTrigger>

          <DialogContent maxWidth={{base: "80%", md: "500px", lg: "500px"}}>
            <ReviewForm 
              courtName={props.courtName} 
              courtId={props.courtId} 
              setOpen={setOpen} 
              existingReview={reviewData?.userReview}
              setReviewData={setReviewData}
            />
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

      { reviewData.userReview &&
        <ReviewCard review={reviewData.userReview} setReviewData={setReviewData} isUserReview/>
      }

      {reviewData.otherReviews.map(review => (
        <ReviewCard key={review.reviewId} review={review} setReviewData={setReviewData} isUserReview={false}/>
      ))}
      
    </VStack>
  )
}