import { Review } from "@/interfaces";
import { pickPalette, timeAgo } from "@/utils";
import { Box, Flex, Text, Avatar, RatingGroup, Icon } from "@chakra-ui/react";
import { LuChevronDown, LuChevronUp } from "react-icons/lu";
import { useState } from 'react';

export default function ReviewCard(
  props: {
    isUserReview: boolean,
    review: Review
  }
) {
  const {isUserReview, review} = props;
  const [localVoteState, setLocalVoteState] = useState({
    upvoted: review.upvoted,
    downvoted: review.downvoted,
    totalVotes: review.totalVotes
  });

  const handleVote = async (targetVote: 'up' | 'down' | 'remove') => {
    // Optimistically update local state
    const oldState = { ...localVoteState };
    const newState = {
      upvoted: targetVote === 'up',
      downvoted: targetVote === 'down',
      totalVotes: localVoteState.totalVotes + (
        targetVote === 'remove' ? (oldState.upvoted ? -1 : 1) :
        !oldState.upvoted && !oldState.downvoted ? (targetVote === 'up' ? 1 : -1) :
        oldState.upvoted && targetVote === 'down' ? -2 :
        oldState.downvoted && targetVote === 'up' ? 2 : 0
      )
    };
    setLocalVoteState(newState);

    try {
      const endpoint = targetVote === 'remove' 
        ? `${review.reviewId}`
        : `${review.reviewId}/${targetVote === 'up' ? 'upvote' : 'downvote'}`;
      
      const response = await fetch(
        `${import.meta.env.VITE_APP_API_BASE_URL}/api/vote/${endpoint}`,
        {
          method: targetVote === 'remove' ? 'DELETE' : 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
        }
      );
  
      if (!response.ok) {
        // Revert on error
        setLocalVoteState(oldState);
        throw new Error(await response.text() || 'Voting issue');
      }
    } catch (e) {
      console.error("Error voting on review:", e);
    }
  };

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
          color={localVoteState.upvoted ? "#08d408" : "rgb(94, 94, 94)"}
          cursor={!isUserReview ? "pointer" : "default"}
          opacity={1}
          _hover={{
            opacity: 0.6
          }}
          onClick={() => {
            if (isUserReview) return;
            handleVote(localVoteState.upvoted ? 'remove' : 'up');
          }}
        />
        <Text fontSize="14px">{localVoteState.totalVotes}</Text>
        <Icon
          as={LuChevronDown}
          boxSize="20px"
          color={localVoteState.downvoted ? "#d40808" : "rgb(94, 94, 94)"}
          cursor={!isUserReview ? "pointer" : "default"}
          opacity={1}
          _hover={{
            opacity: 0.6
          }}
          onClick={() => {
            if (isUserReview) return;
            handleVote(localVoteState.downvoted ? 'remove' : 'down');
          }}
        />
      </Flex>
    </Box>
  );
}