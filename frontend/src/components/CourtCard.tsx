import { BasketballCourt } from "@/interfaces"
import { Container, Flex, Heading, RatingGroup } from "@chakra-ui/react"
import { useState } from "react"

export default function CourtCard(
  props: {
    court: BasketballCourt
  }
) : JSX.Element{
  const [rating, setRating] = useState(4.4);

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
      <Heading size={"3xl"} fontWeight={300}>
        {props.court.name}
      </Heading>
      <Flex gap={3} marginTop={2} alignItems={"center"}>
        <p>{rating}</p>
        <RatingGroup.Root 
          count={5} 
          defaultValue={Math.round(rating * 2) / 2}
          colorPalette={"yellow"}
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
      </Flex>
    
    </Container> 
  )
}