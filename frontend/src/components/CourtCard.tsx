import { BasketballCourt } from "@/interfaces"
import { Container, Flex, Heading, RatingGroup, Text, Tabs, CloseButton } from "@chakra-ui/react"
import { useEffect, useState } from "react"
import CourtOverview from "./CourtOverview";
import Reviews from "./Reviews";

export default function CourtCard(
  props: {
    court: BasketballCourt,
    setCourts: React.Dispatch<React.SetStateAction<BasketballCourt[]>>,
    index: number,
    closeCard: () => void
  }
) : JSX.Element{
  const [ratingStats, setRatingStats] = useState({
    rating: 0,
    reviews: 0
  });

  const baseApiUrl = import.meta.env.VITE_APP_API_BASE_URL;

  const fetchRatings = async () => {
    try {
      const response = await fetch(
        `${baseApiUrl}/api/review/rating?courtId=${props.court.id}`, 
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        }
      );

      if(response.ok) {
        const obj = await response.json();
        setRatingStats(obj);
      } else {
        throw new Error("Rating fetch issue")
      }
    } catch (e) {
      console.error("Error fetching rating:", e); //TODO: NewRelic logging
    }
  };

  useEffect(() => {
    fetchRatings();
  }, [props.court])

  return (
    <Container
      position={"absolute"}
      backgroundColor={"white"}
      width={{md: "400px", lg: "464px"}}
      height={"100vh"}
      zIndex={5}
      boxShadow={"rgba(60, 64, 67, 0.3) 0px 1px 2px 0px, rgba(60, 64, 67, 0.15) 0px 2px 6px 2px"}
      paddingTop={"100px"}
      paddingBottom={"100px"}
      overflowY={"auto"}
    >
      <Flex justifyContent={"space-between"}>
        <Heading size={"3xl"} fontWeight={300}>
          {props.court.name || "Unnamed Court"}
        </Heading>
        <CloseButton onClick={() => props.closeCard()}/>
      </Flex>
      <Flex gap={2} marginTop={2} alignItems={"center"}>
        <Heading size={"sm"} fontWeight={300}>
          {ratingStats.rating}
        </Heading>
        <RatingGroup.Root 
          count={5} 
          value={Math.round(ratingStats.rating * 2) / 2}
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
          {props.court.amenity.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')}
        </Text>
      }
      
      <Tabs.Root defaultValue="overview" marginTop={5}>
        <Tabs.List justifyContent="space-around" colorPalette={"orange"}>
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
        <Tabs.Content value="reviews">
          <Reviews courtName={props.court.name || "Unnamed Court"} courtId={props.court.id}/>
        </Tabs.Content>
      </Tabs.Root>
    </Container> 
  )
}