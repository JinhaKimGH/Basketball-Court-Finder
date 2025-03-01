import { BasketballCourt } from "@/interfaces";
import { Collapsible, Flex, Link, List, Text } from "@chakra-ui/react";
import React from "react";
import { LuChevronDown, LuClock, LuExternalLink, LuLink, LuMapPin, LuPhone } from "react-icons/lu";
import { GiBasketballBasket } from "react-icons/gi";
import { PiCourtBasketballFill } from "react-icons/pi";
import { FaPersonArrowDownToLine } from "react-icons/fa6";

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
      <List.Root gap="2" variant="plain" fontSize={"15px"}>
        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <LuMapPin size={20}/>
          </List.Indicator>
          <Text>  
            {court.address?.complete_addr || "Loading..."}
          </Text>
        </List.Item>
        { court.website &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <LuLink size={20}/>
            </List.Indicator>
            <Link href={"https://" + court.website}>
              <Text marginRight={2}>
                {court.website} 
              </Text>
              <LuExternalLink/>
            </Link>
          </List.Item>
        }

        { court.phone &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <LuPhone size={20}/>
            </List.Indicator>
            {court.phone}
          </List.Item>
        }

        { transformHours.length > 0 && (
          <List.Item padding={3} alignItems={"center"}>
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
                <List.Root gap="2" variant="plain" padding={6}>
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
          </List.Item>
        )}

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <GiBasketballBasket size={21}/>
          </List.Indicator>
          {(!court?.hoops || court.hoops == 0) 
            ? "Add number of hoops"
            : `${court.hoops} hoops`
          }
        </List.Item>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <FaPersonArrowDownToLine size={22}/>
          </List.Indicator>
          {(!court?.surface) 
            ? "Add surface type"
            : `${court.surface.charAt(0).toUpperCase() + court.surface.slice(1)} surface`
          }
        </List.Item>

        <List.Item padding={3} alignItems={"center"}>
          <List.Indicator color="orange.500" boxSize={5}>
            <PiCourtBasketballFill size={22}/>
          </List.Indicator>
          {court?.indoor === undefined 
            ? "Add environment type"
            : (court.indoor ? "Indoor" : "Outdoor") + " court"
          }
        </List.Item>
      </List.Root>
    </>
  )
}