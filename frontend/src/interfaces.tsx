export interface Address {
  house_number: string,
  street: string,
  city: string,
  state: string,
  country: string,
  postal_code: string,
  incomplete: boolean,
}

export interface BasketballCourt {
    id: number,
    lat: number,
    lon: number,
    name: string,
    hoops: number,
    surface: string,
    address: Address,
    amenity: string,
    website: string,
    leisure: string,
    opening_hours: string,
    phone: string,
}

export interface BackendResponse {
    data: { 
        elements: BasketballCourt[];
    };
}