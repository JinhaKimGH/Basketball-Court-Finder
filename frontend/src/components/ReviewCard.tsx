import { Review } from "@/interfaces";
import { pickPalette, timeAgo } from "@/utils";
import { Box, Flex, Text, Avatar, RatingGroup, Icon } from "@chakra-ui/react";
import { LuChevronDown, LuChevronUp } from "react-icons/lu";


export default function ReviewCard(
  props: {
    review: Review
  }
) {
  const {review} = props;

  const addVote = async (isUpvote : boolean) => {
    // add state setting
    try {
      const response = await fetch(`${import.meta.env.VITE_APP_API_BASE_URL}/api/${review.reviewId}/${
        isUpvote ? "upvote" : "downvote"}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
        }
      );

      if (!response.ok) {
        response.text().then(text => {
          throw new Error(text || 'Voting issue');
        });
      }
    } catch(e) {
      console.error("Error voting on review:", e)
    }
  }
  return (
    <Box
      width="100%"
      pt="20px"
      pb="20px"
      borderBottom="1px solid rgb(211, 211, 211)"
    >
      <Flex alignItems="center" gap={3}>
        <Avatar.Root colorPalette={pickPalette(review.authorDisplayName)} size="sm">
          <Avatar.Fallback name={review.authorDisplayName} tabIndex={0} />
        </Avatar.Root>
        <Flex
          flexDirection={"column"}
        >
          <Text fontSize="15px" lineHeight={"17px"}>
            {review.authorDisplayName}
          </Text>
          <Text fontSize="14px" lineHeight={"17px"} color={"rgb(94, 94, 94)"}>
            <Text as="span" 
              color={
                review.authorTrustScore >= 20
                  ? "#08d408"
                  : review.authorTrustScore <= -20
                  ? "#d40808"
                  : "#d4d408"
              }
            >
              {review.authorTrustScore}
            </Text> Trust Score
          </Text>
        </Flex>
      </Flex>
      <Flex
        alignItems={"center"}
        mt="5"
        gap="2"
        height={"10px"}
      >
        <RatingGroup.Root 
          count={5} 
          value={review.rating}
          colorPalette={"yellow"}
          size={"xs"}
          readOnly
        >
          <RatingGroup.HiddenInput />
          <RatingGroup.Control alignItems={"center"}>
            {Array.from({ length: 5 }).map((_, index) => (
              <RatingGroup.Item key={index} index={index + 1}>
                <RatingGroup.ItemIndicator />
              </RatingGroup.Item>
            ))}
          </RatingGroup.Control>
        </RatingGroup.Root>

        <Text 
          fontSize="14px" 
          lineHeight={"15px"} 
          color={"rgb(94, 94, 94)"}
        >
          {timeAgo(review.createdAt)}
          {review.isEdited && " (edited)"}
        </Text>
      </Flex>
      <Text
        fontSize="14px"
        mt="2"
      >
        {review.content}
      </Text>
      <Flex
        alignItems="center"
        gap={2}
        mt={2}
      >
        <Icon
          as={LuChevronUp}
          boxSize="20px"
          color={review.isUpvoted ? "#08d408" : "rgb(94, 94, 94)"}
          cursor="pointer"
          opacity={1}
          _hover={{
            opacity: 0.6
          }}
        />
        <Text fontSize="14px">{review.totalVotes}</Text>
        <Icon
          as={LuChevronDown}
          boxSize="20px"
          color={review.isDownvoted ? "#d40808" : "rgb(94, 94, 94)"}
          cursor="pointer"
          opacity={1}
          _hover={{
            opacity: 0.6
          }}
        />
      </Flex>
    </Box>
  );
}