export interface SurfaceResponse {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
}

export interface SurfaceRequest {
  name: string;
  description: string;
  iconUrl: string;
}
