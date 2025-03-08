import { Box, Flex, VStack, Text, Button, Input } from "@chakra-ui/react";
import React from "react";

interface DaySchedule {
  startTime?: string;
  endTime?: string;
}

interface DaySchedules {
  [key: string]: DaySchedule;
}

interface OpeningHoursFieldProps {
  onChange: (value: string) => void;
  value?: string;
}

const timeInputStyles = `
  input[type="time"] {
    border: 1px solid #E2E8F0;
    border-radius: 6px;
    padding: 6px 8px;
    width: 100px;
    text-align: center;
  }

  input[type="time"]::-webkit-calendar-picker-indicator {
    display: none;
  }

  input[type="time"]::-webkit-datetime-edit {
    text-align: center;
  }

  input[type="time"]::-webkit-datetime-edit-fields-wrapper {
    padding: 0;
  }
`;

export default function OpeningHoursField({ onChange, value }: OpeningHoursFieldProps) {
  const [currDay, setCurrDay] = React.useState<keyof typeof daysMap>("Mo");

  const daysMap = {
    Mo: "Monday",
    Tu: "Tuesday",
    We: "Wednesday",
    Th: "Thursday",
    Fr: "Friday",
    Sa: "Saturday",
    Su: "Sunday"
  };

  // Initialize with court's opening hours if available
  const initializeSchedules = React.useMemo(() : DaySchedules => {

    const newSchedules: DaySchedules = {
      Mo: { },
      Tu: { },
      We: { },
      Th: { },
      Fr: { },
      Sa: { },
      Su: { }
    };

    if (value) {
      const ranges = value.split(/,|;/).map((range) => range.trim());
  
      ranges.forEach(range => {
        // Split each range into days and time parts
        const [days, time] = range.split(' ');
        if (!days || !time) return;
  
        const [startTime, endTime] = time.split('-');
        
        // Handling day ranges like "Mo-Fr"
        if (days.includes('-')) {
          const [start, end] = days.split('-');
          const startIdx = Object.keys(daysMap).indexOf(start);
          const endIdx = Object.keys(daysMap).indexOf(end);
          
          if (startIdx >= 0 && endIdx >= 0) {
            Object.keys(daysMap)
              .slice(startIdx, endIdx + 1)
              .forEach((day) => {
                newSchedules[day] = {
                  startTime,
                  endTime
                };
              });
          }
        } else {
          // Handling individual days like "Mo,We,Fr"
          days.split(',').forEach((day) => {
            if (newSchedules[day]) {
              newSchedules[day] = {
                startTime,
                endTime
              };
            }
          });
        }
      });
    }
    return newSchedules;
  }, [value]);

  const [schedules, setSchedules] = React.useState<DaySchedules>(initializeSchedules);

  // Update schedules when court prop changes
  React.useEffect(() => {
    setSchedules(initializeSchedules);
  }, [value]);

  const formatSchedules = () => {
    let scheduleString = "";

    Object.entries(schedules).forEach(([day, { startTime, endTime }]) => {
      if (startTime && endTime) {
        scheduleString = scheduleString + `${day} ${startTime}-${endTime}; `
      }
    });

    return scheduleString.trimEnd();
  }

  React.useEffect(() => {
    onChange(formatSchedules());
  }, [schedules]);

  return (
    <VStack gap={2}>

      <style>{timeInputStyles}</style>

      <Flex
        justifyContent="space-around"
        width={{base: "80%", md: "90%"}}
        gap={2}
        flexWrap={{ base: "wrap", md: "nowrap" }}
      >
        {Object.entries(daysMap).map(([key]) => (
          <Button 
            key={key}
            rounded="full"
            variant={currDay == key ? "surface" : "outline"}
            colorPalette={currDay == key ? "blue": "white"}
            width="20px"
            onClick={() => setCurrDay(key as keyof typeof daysMap)}
          >
            {key}
          </Button>
        ))}
      </Flex>

      <Box
        p={3}
        width={{base: "80%"}}
      >
        <Flex justify="space-between" align="center">
          <Flex align="center" gap={4} flex={1} direction={{"base": "column"}}>
            <Text width="100px" fontWeight="medium" textAlign={"center"}>{daysMap[currDay]}</Text>
            <Flex gap={2} align="center" justifyContent="center" flex={1} direction={{"base": "column", md: "row"}} width="100%">
              <Input
                type="time"
                value={schedules[currDay].startTime || ''}
                onChange={(e) => {
                  setSchedules(prev => ({
                    ...prev,
                    [currDay]: { ...prev[currDay], startTime: e.target.value }
                  }));
                }}
                minWidth={{base: "100%", md: "50%"}}
              />
              <Text>to</Text>
              <Input
                type="time"
                value={schedules[currDay].endTime || ''} // Provide empty string as fallback
                onChange={(e) => {
                  setSchedules(prev => ({
                    ...prev,
                    [currDay]: { ...prev[currDay], endTime: e.target.value } // Ensure value is never undefined
                  }));
                }}
                minWidth={{base: "100%", md: "50%"}}
              />
            </Flex>
          </Flex>
        </Flex>
      </Box>
    </VStack>
  );
}