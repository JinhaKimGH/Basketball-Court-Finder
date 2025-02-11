import { Container, Flex, Image, Input, useBreakpointValue } from "@chakra-ui/react";
import { LuSearch, LuCircleUserRound } from "react-icons/lu";
import { InputGroup } from "@/components/ui/input-group";
import { Tooltip } from "@/components/ui/tooltip";


/**
 * Stars Component
 * 
 * @param {Object} props  – Component props.
 * @returns {JSX.Element}
 */
export default function SearchBar() : JSX.Element{
  // Search Icon
  const searchIcon = (
    <Tooltip content="Search" openDelay={100}>
      <LuSearch size="24px" cursor="pointer"/>
    </Tooltip>
  );

  // Create user icon with custom size
  const UserIcon = ({ size }: { size: number }) => {
    return (
      <Tooltip content="Sign In" openDelay={100}>
        <LuCircleUserRound size={size} color="#45a2ff" cursor="pointer" />
      </Tooltip>
    );
  };

  // Screen sized-based icons
  const endElement = useBreakpointValue({ 
    base: 
      <Flex
        gap="5px"
        alignItems="center"
      >
        {searchIcon}
        <UserIcon size={22} />
      </Flex>, 
    md: 
      <Flex
        gap="8px"
        alignItems="center"
      >
        {searchIcon}
        <UserIcon size={22} />
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
    lg: <UserIcon size={30} />
  });

  return (
    <>
      <Container
        position="absolute"
        zIndex="1"
        minWidth="98vw"
        display="flex"
        alignItems="center"
        justifyContent="space-between"
        mt="5"
        ml={{base: "0",lg: "10"}}
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
          width={{base: "100%", md: "100%", lg: "18%"}}
          endElement={endElement}
        >
          <Input 
            placeholder={placeholderString} 
            size="xl" 
            borderRadius="full"
            _focus={{
              borderWidth: "0px",
            }}
          />
        </InputGroup>
        {userElement}
      </Container>
    </>
  )
}
