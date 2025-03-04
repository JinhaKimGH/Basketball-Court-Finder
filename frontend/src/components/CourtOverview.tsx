import { BasketballCourt } from "@/interfaces";
import { Button, Collapsible, Flex, Heading, Link, List, Text } from "@chakra-ui/react";
import React from "react";
import { LuChevronDown, LuClock, LuExternalLink, LuLink, LuMapPin, LuPhone } from "react-icons/lu";
import { GiBasketballBasket, GiRopeCoil } from "react-icons/gi";
import { PiCourtBasketballFill } from "react-icons/pi";
import { RiMapPin4Line } from "react-icons/ri";
import { AiOutlineColumnHeight } from "react-icons/ai";
import { FaPersonArrowDownToLine } from "react-icons/fa6";
import { DialogContent, DialogRoot, DialogTrigger } from "./ui/dialog";
import CourtEditForm from "./CourtEditForm";

export default function CourtOverview(
  props: {
    index: number,
    court: BasketballCourt,
    setCourts: React.Dispatch<React.SetStateAction<BasketballCourt[]>>
  }
) {
  const { court, setCourts, index } = props;

  const hasFetched = React.useRef(false); // Prevent duplicate fetches

  function formatAddress(address: Partial<BasketballCourt["address"]>): string {
    return [
      address.house_number ? `${address.house_number} ${address.street}` : address.street,
      address.city,
      address.state,
      address.postal_code,
    ]
      .filter(Boolean) // Remove empty values
      .join(", ");
  }

  // Reverse geocodes the address with the nominatim API
  async function fetchLocation(){
    if (hasFetched.current) return; // Prevents duplicate API calls
    hasFetched.current = true;

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${court.lat}&lon=${court.lon}`,
        {
          headers: {
            Referer: "https://jinhakimgh.github.io/Basketball-Court-Finder",
            "User-Agent": "BasketballCourtFinder/1.0",
          },
        }
      );

      if (!response.ok) throw new Error("Failed to fetch address");
      const data = await response.json();

      const addr = formatAddress(data.address);

      // Only update state if address is different
      setCourts((prevItems) =>
        prevItems.map((item, i) =>
          i === index && item.address.complete_addr !== addr
            ? { ...item, address: { ...item.address, incomplete: false, complete_addr: addr } }
            : item
        )
      );
    } catch (error) {
      console.error("Error fetching location:", error);
    }
  }

  function hasIncompleteInfo(court: BasketballCourt): boolean {
    return (
      !court.website ||
      !court.phone ||
      !court.opening_hours ||
      !court.hoops ||
      !court.surface ||
      court.indoor === null ||
      !court.netting ||
      !court.rim_type ||
      !court.rim_height
    );
  }

  React.useEffect(() => {
    if (court.address.incomplete) {
      fetchLocation()
    } else if (!("complete_addr" in court.address)) {
      const formattedAddr = formatAddress(court.address);

      setCourts((prevItems) =>
        prevItems.map((item, i) =>
          i === index && item.address.complete_addr !== formattedAddr
            ? { ...item, address: { ...item.address, complete_addr: formattedAddr } }
            : item
        )
      );
    }
  }, [court.address, setCourts])

  const transformHours = React.useMemo(() => {
    if (!court.opening_hours) return [];
    
    const daysMap: { [key: string]: string } = {
        "Mo": "Monday",
        "Tu": "Tuesday",
        "We": "Wednesday",
        "Th": "Thursday",
        "Fr": "Friday",
        "Sa": "Saturday",
        "Su": "Sunday"
    };
  
    const ranges = court.opening_hours.split(/,|;/).map((range) => range.trim());
    
    return ranges.flatMap((range) => {
      const [days, time] = range.split(' ');
      if (!days || !time) return []; // If either days or time is missing, skip
      
      const formattedTime = time.replace('-', ' - ');
      const dayList: string[] = [];
      
      // Handling day ranges like "Mo-Fr"
      if (days.includes('-')) {
        const [start, end] = days.split('-');
        const startIdx = Object.keys(daysMap).indexOf(start);
        const endIdx = Object.keys(daysMap).indexOf(end);
        if (startIdx >= 0 && endIdx >= 0) {
          dayList.push(...Object.values(daysMap).slice(startIdx, endIdx + 1));
        }
      } else {
        // Handling individual days like "Mo,We,Fr"
        days.split(',').forEach((day) => {
          if (daysMap[day]) {
            dayList.push(daysMap[day]);
          }
        });
      }
  
      return dayList.map((day) => `${day}: ${formattedTime}`);
    });
  }, [court.opening_hours]);

  return (
    <>
      <List.Root 
        gap="2" 
        variant="plain" 
        fontSize={"15px"} 
        width={{sm: "70%", md: "100%"}}
        marginLeft={"auto"}
        marginRight={"auto"}
      >
        <Heading fontWeight={"400"} textAlign={"center"} padding={4}>Facility Info</Heading>
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <LuMapPin size={20}/>
          </List.Indicator>
          <Text>  
            {court.address?.complete_addr || "Loading..."}
          </Text>
        </List.Item>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <LuLink size={20}/>
          </List.Indicator>
          {
            court.website ? 
            <Link href={"https://" + court.website}>
              <Text marginRight={2}>
                {court.website} 
              </Text>
              <LuExternalLink/>
            </Link>

            : "Unknown website"
          }
        </List.Item>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <LuPhone size={20}/>
          </List.Indicator>
          {court.phone || "Unknown phone number"}
        </List.Item>

          <List.Item padding={3} alignItems={"center"}>
            { transformHours.length > 0 ? 
            <Collapsible.Root unmountOnExit>
              <Collapsible.Trigger cursor={"pointer"}>
                <Flex justifyContent={"space-between"} alignItems={"center"} gap={5}>
                  <div>
                    <List.Indicator color="orange.500" boxSize={5}>
                      <LuClock size={20}/>
                    </List.Indicator>
                    Opening Hours
                  </div>
                  <LuChevronDown />
                </Flex>
              </Collapsible.Trigger>
              <Collapsible.Content>
                <List.Root gap="2" variant="plain" padding={6} paddingBottom={0}>
                  {
                    transformHours.map((hour, index) => (
                      <List.Item key={index} padding={1}>
                        {hour}
                      </List.Item>
                    ))
                  }
                </List.Root>
              </Collapsible.Content>
            </Collapsible.Root>
            : 
              <>
                <List.Indicator color="orange.500" boxSize={5}>
                  <GiBasketballBasket size={21}/>
                </List.Indicator>
                Unknown opening hours
              </>
            }
          </List.Item>

        <Heading fontWeight={"400"} textAlign="center" padding={4}>Court Specifications</Heading>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <GiBasketballBasket size={21}/>
          </List.Indicator>
          { court.hoops ?
            `${court.hoops} hoops`
            : "Unknown number of hoops"
          }
        </List.Item>
      
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <FaPersonArrowDownToLine size={22}/>
          </List.Indicator>
          { court.surface ?
            `${court.surface.charAt(0).toUpperCase() + court.surface.slice(1)} surface`
            : "Unknown court surface type"
          }
        </List.Item>
        
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <PiCourtBasketballFill size={22}/>
          </List.Indicator>
          { court.indoor !== null ?
            (court.indoor ? "Indoor" : "Outdoor") + " court"

            : "Unknown if indoor or outdoor"
          }
        </List.Item>

        
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <GiRopeCoil size={22}/>
          </List.Indicator>
          {
            (() => {
              switch(court.netting) {
                case 1:
                  return "No net";
                case 2:
                  return "Chain net";
                case 3:
                  return "Nylon net";
                default:
                  return "Unknown net type";
              }
            })()
          }
        </List.Item>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <RiMapPin4Line size={22}/>
          </List.Indicator>
          {
            (() => {
              switch(court.rim_type) {
                case 1:
                  return "Single rim";
                case 2:
                  return "1.5 rims";
                case 3:
                  return "Double rim";
                default:
                  return "Unknown rim type";
              }
            })()
          }
        </List.Item>
        
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <AiOutlineColumnHeight size={22}/>
          </List.Indicator>
          { court.rim_height ?
            `${court.rim_height} foot height`
            : `Unknown height`
          }
        </List.Item>

        <DialogRoot>
          <DialogTrigger asChild>
            <Flex justifyContent={"center"} mt={7} mb={7} asChild>
              <Button
                colorPalette="orange"
                variant="outline"
                size="lg"
                rounded="full"
                onClick={() => {/* TODO: Add edit functionality */}}
              >
                {hasIncompleteInfo(court) ?
                  "Suggest Missing Information"
                  : "Update Court Information"
                }
              </Button>
            </Flex>
          </DialogTrigger>
          <DialogContent>
            <CourtEditForm/>
          </DialogContent>
        </DialogRoot>
      </List.Root>
    </>
  )
}