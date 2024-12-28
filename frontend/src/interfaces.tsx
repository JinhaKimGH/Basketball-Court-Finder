export interface BasketballCourt {
    id: number,
    lat: number,
    lon: number,
    name: string,
    hoops: number,
    surface: string,
    address: string
}

export interface BackendResponse {
    data: { 
        elements: BasketballCourt[];
    };
}