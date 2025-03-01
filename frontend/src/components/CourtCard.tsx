import { BasketballCourt } from "@/interfaces"
import { Container, Flex, Heading, RatingGroup, Text, Tabs, CloseButton } from "@chakra-ui/react"
import { useState } from "react"
import CourtOverview from "./CourtOverview";

export default function CourtCard(
  props: {
    court: BasketballCourt,
    setCourts: React.Dispatch<React.SetStateAction<BasketballCourt[]>>,
    index: number,
    closeCard: () => void
  }
) : JSX.Element{
  const [ratingStats, setRatingStats] = useState({
    rating: 4.4,
    reviews: 873
  });

  return (
    <Container
      position={"absolute"}
      backgroundColor={"white"}
      width={{lg: "464px"}}
      height={"100vh"}
      zIndex={5}
      boxShadow={"rgba(60, 64, 67, 0.3) 0px 1px 2px 0px, rgba(60, 64, 67, 0.15) 0px 2px 6px 2px"}
      paddingTop={"100px"}
    >
      <Flex justifyContent={"space-between"}>
        <Heading size={"3xl"} fontWeight={300}>
          {props.court.name}
        </Heading>
        <CloseButton onClick={() => props.closeCard()}/>
      </Flex>
      <Flex gap={2} marginTop={2} alignItems={"center"}>
        <Heading size={"sm"} fontWeight={300}>
          {ratingStats.rating}
        </Heading>
        <RatingGroup.Root 
          count={5} 
          defaultValue={Math.round(ratingStats.rating * 2) / 2}
          colorPalette={"yellow"}
          size={"xs"}
          allowHalf
          readOnly
        >
          <RatingGroup.HiddenInput />
          <RatingGroup.Control>
            {Array.from({ length: 5 }).map((_, index) => (
              <RatingGroup.Item key={index} index={index + 1}>
                <RatingGroup.ItemIndicator />
              </RatingGroup.Item>
            ))}
          </RatingGroup.Control>
        </RatingGroup.Root>

        <Heading size={"sm"} fontWeight={300}>
          ({ratingStats.reviews})
        </Heading>
      </Flex>
      {props.court.amenity && 
        <Text 
          textStyle="sm"
          marginTop={1}
        >
          {props.court.amenity.charAt(0).toUpperCase() + props.court.amenity.slice(1)}
        </Text>
      }
      
      <Tabs.Root defaultValue="overview" marginTop={5}>
        <Tabs.List justifyContent="space-around">
          <Tabs.Trigger value="overview">
            Overview
          </Tabs.Trigger>
          <Tabs.Trigger value="reviews">
            Reviews
          </Tabs.Trigger>
        </Tabs.List>
        <Tabs.Content value="overview">
          <CourtOverview court={props.court} setCourts={props.setCourts} index={props.index}/>
        </Tabs.Content>
        <Tabs.Content value="reviews">Manage your projects</Tabs.Content>
      </Tabs.Root>
    </Container> 
  )
}