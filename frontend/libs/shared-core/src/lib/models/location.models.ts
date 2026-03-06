export interface ProvinceResponse {
  code: string;
  label: string;
}

export interface CityResponse {
  idCity: number;
  label: string;
  provinceCode: string;
}