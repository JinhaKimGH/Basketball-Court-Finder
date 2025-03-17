import { Box, Button, Flex, Spinner, Text, VStack } from "@chakra-ui/react";
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
import { Review, ReviewData } from "@/interfaces";
import ReviewCard from "./ReviewCard";
import { useInView } from 'react-intersection-observer';


export default function Reviews(
  props: {
    courtId : number,
    courtName: string,
  }
) {
  const [sortMethod, setSortMethod] = useState("NEWEST");
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const REVIEWS_PER_PAGE = 10;

  // Intersection observer for infinite scroll
  const { ref: loadMoreRef, inView } = useInView({
    threshold: 0.5,
    triggerOnce: false
  });

  const [reviewData, setReviewData] = useState<ReviewData>({otherReviews: []}); 

  const fetchReviews = async (pageNum: number) => {
    setIsLoading(true);
    fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/api/review?courtId=${props.courtId}&page=${pageNum}&limit=${REVIEWS_PER_PAGE}&sortMethod=${sortMethod}`,
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
        setReviewData(prev => ({
          ...prev,
          otherReviews: pageNum === 1 
            ? data.otherReviews 
            : [...prev.otherReviews, ...data.otherReviews],
          userReview: data.userReview,
        }));

        setHasMore(data.otherReviews.length === REVIEWS_PER_PAGE);
    }).catch((error) => {
      console.error("Error fetching review data: ", error);
      //TODO: Replace with newrelic logging
    }).finally(() => {
      setIsLoading(false);
    })
  }

  // Load more when scrolling to bottom
  useEffect(() => {
    if (inView && hasMore) {
      setPage(prev => prev + 1);
    }
  }, [inView]);

  // Fetch on component load
  useEffect(() => {
    setPage(1);
    setHasMore(false);
    fetchReviews(1);
  }, [props.courtId, sortMethod])

  // Fetch new page when page number changes
  useEffect(() => {
    if (page > 1) {
      fetchReviews(page);
    }
  }, [page]);

  const LazyReviewCard = ({ review }: { review: Review }) => {
    const { ref, inView } = useInView({
      triggerOnce: true,
      rootMargin: '100px 0px', // Start loading 100px before it enters viewport
    });

    return (
      <Box ref={ref} minHeight="150px" width="100%"> {/* Maintain layout height */}
        {inView && (
          <ReviewCard 
            review={review}
            isUserReview={false}
          />
        )}
      </Box>
    );
  };
  
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
        <MenuRoot onSelect={(details) => {
          setSortMethod(details.value);
          setPage(1);
          setHasMore(false);
          fetchReviews(1);
        }}>
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
            <MenuItem value="NEWEST">
              Newest
            </MenuItem>
            <MenuItem value="LOWEST">
              Lowest rating
            </MenuItem>
            <MenuItem value="HIGHEST">
              Highest rating
            </MenuItem>
          </MenuContent>
        </MenuRoot>
      </Flex>

      { reviewData.userReview &&
        <ReviewCard review={reviewData.userReview} isUserReview/>
      }

      {reviewData.otherReviews.map(review => (
        <LazyReviewCard key={review.reviewId} review={review} />
      ))}

      {(hasMore || isLoading)&& (
        <Box ref={loadMoreRef} py={4} w="100%" textAlign={"center"}>
          <Spinner color="orange.500"/>
        </Box>
      )}
      
    </VStack>
  )
}