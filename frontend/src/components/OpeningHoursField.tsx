import { BasketballCourt } from "@/interfaces";
import { Box, Flex, Input, VStack, Text } from "@chakra-ui/react";
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
  court: BasketballCourt
}

// TODO: FIx to do one by one: Mo 9:00-17:00; Tu 9:00-17:00...
// TODO: Two inputs, buttons to pick the day of the week
// TODO: STYLING

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

const formatTime = (value: string): string | undefined => {
  // Remove any non-alphanumeric characters except ':'
  const cleaned = value.replace(/[^0-9:APMapm]/g, '');
  
  // Handle empty or invalid input
  if (!cleaned) return undefined;

  // Try to parse time in 12-hour format (e.g. "11:30 AM")
  const match = cleaned.match(/^(1[0-2]|0?[1-9]):([0-5][0-9])?\s*(AM|PM|am|pm)?$/);
  if (match) {
    const hours = parseInt(match[1]);
    const minutes = match[2] || '00';
    const period = (match[3] || '').toUpperCase();

    // Convert to 24-hour format
    let hour24 = hours;
    if (period === 'PM' && hours < 12) hour24 += 12;
    if (period === 'AM' && hours === 12) hour24 = 0;

    return `${hour24.toString().padStart(2, '0')}:${minutes}`;
  }

  return undefined;
};

export default function OpeningHoursField({ onChange, value, court }: OpeningHoursFieldProps) {
  // Initialize with court's opening hours if available
  const initializeSchedules = () => {

    const schedules: DaySchedules = {
      Mo: { },
      Tu: { },
      We: { },
      Th: { },
      Fr: { },
      Sa: { },
      Su: { }
    };

    if (court.opening_hours) {
      court.opening_hours.split(';').forEach(range => {
        const [days, times] = range.trim().split(' ');
        const [start, end] = times.split('-');
        
        days.split(',').forEach(day => {
          if (schedules[day]) {
            schedules[day] = {
              startTime: start,
              endTime: end
            };
          }
        });
      });
    }

    return schedules;
  };

  const [schedules, setSchedules] = React.useState<DaySchedules>(initializeSchedules);

  const daysMap = {
    Mo: "Monday",
    Tu: "Tuesday",
    We: "Wednesday",
    Th: "Thursday",
    Fr: "Friday",
    Sa: "Saturday",
    Su: "Sunday"
  };

  // Update schedules when court prop changes
  React.useEffect(() => {
    setSchedules(initializeSchedules());
  }, [court.opening_hours]);

  // Parse initial value if provided
  React.useEffect(() => {
    if (value) {
      const newSchedules = { ...schedules };
      value.split(';').forEach(range => {
        const [days, times] = range.trim().split(' ');
        const [start, end] = times.split('-');
        
        days.split(',').forEach(day => {
          if (newSchedules[day]) {
            newSchedules[day] = {
              startTime: start,
              endTime: end
            };
          }
        });
      });
      setSchedules(newSchedules);
    }
  }, [value]);

 

  // React.useEffect(() => {
  //   onChange(formatSchedules());
  // }, [schedules]);

  return (
    <VStack gap={2}>

      <style>{timeInputStyles}</style>
      {Object.entries(daysMap).map(([key, label]) => (
        <Box
          key={key}
          p={3}
        >
          <Flex justify="space-between" align="center">
            <Flex align="center" gap={4} flex={1} direction={{"base": "column"}}>
              <Text width="100px" fontWeight="medium">{label}</Text>
              <Flex gap={2} align="center" flex={1}>
                <input
                  type="time"
                  value={schedules[key].startTime || ''}
                  onChange={(e) => {
                    const formatted = formatTime(e.target.value);
                    setSchedules(prev => ({
                      ...prev,
                      [key]: { ...prev[key], startTime: formatted }
                    }));
                  }}
                />
                
                <Text>to</Text>
                <input
                  type="time"
                  value={schedules[key].endTime || ''} // Provide empty string as fallback
                  onChange={(e) => {
                    const formatted = formatTime(e.target.value);
                    setSchedules(prev => ({
                      ...prev,
                      [key]: { ...prev[key], endTime: formatted || '' } // Ensure value is never undefined
                    }));
                  }}
                />
              </Flex>
            </Flex>
          </Flex>
        </Box>
      ))}
    </VStack>
  );
}