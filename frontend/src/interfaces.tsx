export interface Address {
  house_number: string,
  street: string,
  city: string,
  state: string,
  road?: string,
  country: string,
  postcode: string,
  incomplete: boolean,
  complete_addr?: string
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
    opening_hours: string,
    netting?: number,
    rim_type?: number,
    rim_height?: number,
    phone: string,
    indoor: boolean
}

export interface BackendResponse {
    data: { 
        elements: BasketballCourt[];
    };
}