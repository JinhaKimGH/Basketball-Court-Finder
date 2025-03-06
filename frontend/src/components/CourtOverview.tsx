import { BasketballCourt } from "@/interfaces";
import { Box, Collapsible, Flex, Heading, Link, List, Text } from "@chakra-ui/react";
import React from "react";
import { LuChevronDown, LuClock, LuExternalLink, LuLink, LuMapPin, LuPencil, LuPhone } from "react-icons/lu";
import { GiBasketballBasket, GiRopeCoil } from "react-icons/gi";
import { PiCourtBasketballFill, PiSparkleLight } from "react-icons/pi";
import { RiMapPin4Line } from "react-icons/ri";
import { AiOutlineColumnHeight } from "react-icons/ai";
import { FaPersonArrowDownToLine } from "react-icons/fa6";
import { DialogContent, DialogRoot, DialogTrigger } from "./ui/dialog";
import CourtEditForm, { FieldType } from "./CourtEditForm";

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
      console.error("Error fetching location:", error); // TODO: LOG IT
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

  const renderTrigger = (field : FieldType) => {
    return (
      <DialogTrigger asChild>
        <Box
          marginLeft="auto"
          cursor={"pointer"}
          padding="2"
          _hover={{ backgroundColor: "gray.100", color: "orange.500" }}
          borderRadius={"10px"}
          onClick={() => handleEditClick(field)}
          alignItems={"center"}
        >
          <LuPencil/>
        </Box>
      </DialogTrigger>
    )
  }

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

  // Add state for dialog management
  const [selectedField, setSelectedField] = React.useState<FieldType | null>(null);
  const [isDialogOpen, setIsDialogOpen] = React.useState(false);

  const handleEditClick = (field: FieldType) => {
    setSelectedField(field);
    setIsDialogOpen(true);
  };

  const handleDialogChange = (details: { open: boolean }) => {
    setIsDialogOpen(details.open);
    if (!details.open) {
      // Reset the selected field when dialog closes
      setSelectedField(null);
    }
  };

  const getMissingFields = () => {
    const missing: Array<{ field: FieldType; label: string, icon: React.ReactNode }> = [];
    
    if (!court.website) missing.push({ field: 'website', label: 'Add website', icon: <LuLink size={20}/>});
    if (!court.phone) missing.push({ field: 'phone', label: 'Add phone number', icon: <LuPhone size={20}/>});
    if (!court.opening_hours) missing.push({ field: 'opening_hours', label: 'Add opening hours', icon: <LuClock size={20}/>});
    if (!court.hoops) missing.push({ field: 'hoops', label: 'Add number of hoops', icon: <GiBasketballBasket size={21}/>});
    if (!court.surface) missing.push({ field: 'surface', label: 'Add surface type', icon: <FaPersonArrowDownToLine size={22}/>});
    if (court.indoor === null) missing.push({ field: 'indoor', label: 'Add indoor/outdoor', icon: <PiCourtBasketballFill size={22}/>});
    if (!court.netting) missing.push({ field: 'netting', label: 'Add net type', icon: <GiRopeCoil size={22}/>});
    if (!court.rim_type) missing.push({ field: 'rim_type', label: 'Add rim type', icon: <RiMapPin4Line size={22}/>});
    if (!court.rim_height) missing.push({ field: 'rim_height', label: 'Add rim height', icon: <AiOutlineColumnHeight size={22}/>});
    if (!court.amenity) missing.push({ field: 'amenity', label: 'Add amenity type', icon: <PiSparkleLight size={22}/>});

    return missing;
  };

  return (
    <DialogRoot open={isDialogOpen} onOpenChange={handleDialogChange}>
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
            {renderTrigger("website")}
          </List.Item>
        }

        {court.phone &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <LuPhone size={20}/>
            </List.Indicator>
            {court.phone}
            {renderTrigger("phone")}
          </List.Item>
        }

        { transformHours.length > 0 && 
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

            {renderTrigger("opening_hours")}
          </List.Item>
        }

        { (court.hoops || court.surface || court.indoor || court.rim_height || court.rim_type || court.netting) &&
          <Heading fontWeight={"400"} textAlign="center" padding={4}>Court Specifications</Heading>
        }
        
        { court.hoops &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <GiBasketballBasket size={21}/>
            </List.Indicator>
            {`${court.hoops} hoops`}

            {renderTrigger("hoops")}
          </List.Item>
        }

        { court.surface &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <FaPersonArrowDownToLine size={22}/>
            </List.Indicator>
            {`${court.surface.charAt(0).toUpperCase() + court.surface.slice(1)} surface`}

            {renderTrigger("surface")}
          </List.Item>
        }

        { court.indoor !== null &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <PiCourtBasketballFill size={22}/>
            </List.Indicator>
            {(court.indoor ? "Indoor" : "Outdoor") + " court"}

            {renderTrigger("indoor")}
          </List.Item>
        }
        

        { (court.netting && 0 < court.netting && court.netting >= 3) &&
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
                    return "";
                }
              })()
            }

          {renderTrigger("netting")}
          </List.Item>
        }


        
        { (court.rim_type && 0 < court.rim_type && court.rim_type >= 3) &&
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
                    return "";
                }
              })()
            }

            {renderTrigger("rim_type")}
          </List.Item>
        }
        
        { court.rim_height &&
          <List.Item padding={3} alignItems={"center"}>
            <List.Indicator color="orange.500" boxSize={5}>
              <AiOutlineColumnHeight size={22}/>
            </List.Indicator>
            { `${court.rim_height} foot height`}

            {renderTrigger("rim_height")}
          </List.Item>
        }

        {/* Missing Information Section */}
        {getMissingFields().length > 0 && (
          <>
            <Heading fontWeight={"400"} textAlign="center" padding={4}>Missing Information</Heading>
            {getMissingFields().map(({ field, label, icon }) => (
              <DialogTrigger key={field} asChild>
                <List.Item
                  padding={3}
                  alignItems={"center"}
                  cursor={"pointer"}
                  _hover={{ backgroundColor: "gray.100" }}
                  borderRadius={"10px"}
                  onClick={() => handleEditClick(field)}
                >
                <List.Indicator color="orange.500" boxSize={5}>
                  {icon}
                </List.Indicator>
                {label}
                </List.Item>
              </DialogTrigger>
            ))}
          </>
        )}
      </List.Root>

      {selectedField && (
        <DialogContent maxWidth={"80%"}>
          <CourtEditForm field={selectedField} id={court.id}/>
        </DialogContent>
      )}
    </DialogRoot>
  )
}