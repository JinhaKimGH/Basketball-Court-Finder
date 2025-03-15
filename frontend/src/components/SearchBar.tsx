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
    setCoordinates: React.Dispatch<React.SetStateAction<LatLngTuple>>,
    currentMapCenter?: LatLngTuple
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

  // Search this Area handler
  const handleSearchArea = () => {
    if (props.currentMapCenter) {
      props.setCoordinates(props.currentMapCenter);
    }
  };

  // Calculates the latitude/longitude of the user's location on submit
  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!location) {
      return;
    }

    setIsLoading(true);
    fetch(`https://nominatim.openstreetmap.org/search?q=${location}&format=json&limit=1`, {
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
      <Button variant="plain" onClick={handleSubmit} loading={isLoading} padding={0} _hover={{color: "orange.500"}}>
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
    md: searchIcon,
    lg: searchIcon
  });

  // Placeholder String
  const placeholderString = useBreakpointValue({
    base: "Search for Courts...",
    md: "Search Basketball Courts..."
  })

  // Show LuCircleRound in the top right only in large screens
  const userElement = useBreakpointValue({
    md: <ProfilePopup iconSize={30}/>,
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
        <Flex gap={10} alignItems="center" flex={1}>
          <InputGroup
            startElement={
              <Image
                src="/assets/basket.png"
                alt="Basket Icon"
                boxSize="24px"
                objectFit="cover"
              />
            }
            width={{base: "100%", md: "350px", lg: "400px"}}
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

          <Button
            display={{base: "none", md: "flex"}}
            variant="outline"
            backgroundColor="white"
            onClick={handleSearchArea}
            _hover={{
              backgroundColor: "orange.50"
            }}
            padding={3}
            rounded="full"
            boxShadow="sm"
            gap={2}
            whiteSpace="nowrap"
          >
            <LuSearch size={16}/>
            Search this area
          </Button>
        </Flex>
        <Box position="relative" zIndex={12}> {/* Wrap userElement with proper z-index */}
          {userElement}
        </Box>
      </Container>
      <Box
      position="fixed"
      left="50%"
      transform="translateX(-50%)"
      top="80px"
      zIndex={1}
      >
        <Button
          display={{base: "flex", md: "none"}}
          variant="outline"
          backgroundColor="white"
          onClick={handleSearchArea}
          _hover={{
            backgroundColor: "orange.50"
          }}
          padding={3}
          rounded="full"
          boxShadow="md"
        >
          <LuSearch size={16}/>
          Search this area
        </Button>
      </Box>
    </>
  )
}
