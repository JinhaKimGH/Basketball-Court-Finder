import { Box, Button, Container, Flex, Image, Input, useBreakpointValue } from "@chakra-ui/react";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "@/components/ui/input-group";
import { Tooltip } from "@/components/ui/tooltip";
import ProfilePopup from "./ProfilePopup";
import React from "react";
import { LatLngTuple } from "leaflet";


/**
 * SearchBar Component
 * 
 * @returns {JSX.Element}
 */
export default function SearchBar(
  props: {
    setCoordinates: React.Dispatch<React.SetStateAction<LatLngTuple>>
  }
) : JSX.Element{
  
  const [isLoading, setIsLoading] = React.useState(false);
  const [location, setLocation] = React.useState('');

  // Handling Input events
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setLocation(e.target.value);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSubmit(e);
    }
  };

  // Calculates the latitude/longitude of the user's location on submit
  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!location) {
      return;
    }

    setIsLoading(true);
    await fetch(`https://nominatim.openstreetmap.org/search?q=${location}&format=json&limit=1`, {
      method: 'GET',
      headers: {
        Referer: 'https://jinhakimgh.github.io/Basketball-Court-Finder',
        'User-Agent': 'BasketballCourtFinder/1.0'
      }
    }).then(
      (res) => {
        if (res.ok) {
          return res.json();
        } else {
          throw new Error(`HTTP error! status: ${res.status}`);
        }
    })
    .then((data) => {
      if (data.length > 0) {
        const {lat, lon} = data[0];
        props.setCoordinates([lat, lon]);
      } else {
        throw new Error(`No data found for location: ${location}`);
      }
    })
    .catch((error) => {
      console.error('Error fetching data from the nominatim API: ', error);
      // TODO: Replace with logging later
    });
    setIsLoading(false);
  }


  // Search Icon
  const searchIcon = (
    <Tooltip content="Search" openDelay={100}>
      <Button variant="plain" onClick={handleSubmit} loading={isLoading} padding={0}>
        <LuSearch size="24px" cursor="pointer" />
      </Button>
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
            paddingRight={{base: "79px !important", md: "50px !important"}}
            value={location}
            onChange={handleChange}
            overflow={"hidden"}
            onKeyDown={handleKeyDown}
          />
        </InputGroup>
        <Box position="relative" zIndex={12}> {/* Wrap userElement with proper z-index */}
          {userElement}
        </Box>
      </Container>
    </>
  )
}
