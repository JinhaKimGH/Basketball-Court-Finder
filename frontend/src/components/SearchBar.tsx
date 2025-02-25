import { Box, Container, Flex, Image, Input, useBreakpointValue } from "@chakra-ui/react";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "@/components/ui/input-group";
import { Tooltip } from "@/components/ui/tooltip";
import ProfilePopup from "./ProfilePopup";


/**
 * SearchBar Component
 * 
 * @returns {JSX.Element}
 */
export default function SearchBar() : JSX.Element{
  // Search Icon
  const searchIcon = (
    <Tooltip content="Search" openDelay={100}>
      <LuSearch size="24px" cursor="pointer"/>
    </Tooltip>
  );

  // Screen sized-based icons
  const endElement = useBreakpointValue({ 
    base: 
      <Flex
        gap="5px"
        alignItems="center"
      >
        {searchIcon}
        <ProfilePopup iconSize={22}/>
      </Flex>, 
    md: 
      <Flex
        gap="8px"
        alignItems="center"
      >
        {searchIcon}
        <ProfilePopup iconSize={22}/>
      </Flex>, 
    lg: searchIcon
  });

  // Placeholder String
  const placeholderString = useBreakpointValue({
    base: "Search for Courts...",
    md: "Search Basketball Courts..."
  })

  // Show LuCircleRound in the top right only in large screens
  const userElement = useBreakpointValue({
    lg: <ProfilePopup iconSize={30}/>
  });
  
  return (
    <>
      <Container
        position="absolute"
        zIndex="10"
        minWidth="100vw"
        display="flex"
        alignItems="center"
        justifyContent="space-between"
        mt="5"
      >
        <InputGroup
          startElement={
            <Image
              src="/assets/basket.png"
              alt="Basket Icon"
              boxSize="24px"
              objectFit="cover"
            />
          }
          width={{base: "100%", md: "100%", lg: "400px"}}
          endElement={endElement}
        >
          <Input 
            placeholder={placeholderString} 
            size="xl" 
            borderRadius="full"
            _focus={{
              borderWidth: "0px",
            }}
            backgroundColor={"white"}
          />
        </InputGroup>
        <Box position="relative" zIndex={12}> {/* Wrap userElement with proper z-index */}
          {userElement}
        </Box>
      </Container>
    </>
  )
}
