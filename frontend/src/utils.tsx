import { Tooltip } from "@/components/ui/tooltip";
import { LuShieldCheck, LuShieldQuestion, LuShieldX } from "react-icons/lu";

// Helper function ensures that inputted email is a valid email
export function validateEmail(inputEmail: string){
  const emailRegex = new RegExp(/^[A-Za-z0-9_!#$%&'*+/=?`{|}~^.-]+@[A-Za-z0-9.-]+$/, 'gm');

  return emailRegex.test(inputEmail);
}

const colorPalette = ["red", "blue", "green", "yellow", "purple", "orange"]
export function pickPalette(name: string) {
  const index = name.charCodeAt(0) % colorPalette.length;
  return colorPalette[index];
}

// Haversine Formula Distance Calculation
export function calcDistance(lat1: number, lon1: number, lat2: number, lon2: number){
  const earthRadius = 6371
  const dLat = (lat2 - lat1) * (Math.PI / 180)
  const dLon = (lon2 - lon1) * (Math.PI / 180)
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = earthRadius * c;

  return distance;
}

export function trustSymbol(trust: number) {
  let icon;
  if(trust == 0) {
    icon = <LuShieldQuestion color="#F0AD4E"/>;
  }
  else if(trust > 0) {
    icon = <LuShieldCheck color="#5CB85C"/>;
  }

  else {
    icon = <LuShieldX color="#D9534F"/>;
  }

  return (
    <Tooltip 
      content={`Overall trust score ${trust.toString()}`}
      openDelay={200}
      showArrow
    >
      {icon}
    </Tooltip>
  )
}