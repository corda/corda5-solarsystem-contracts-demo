import { createContext } from "react";

export type UserContextType = {
  currentPlanet: string;
  setCurrentPlanet: (value: string) => void;
};

export const PlanetContext = createContext<UserContextType>({
  currentPlanet: "",
  setCurrentPlanet: (value) => console.log("No PLaneto selected"),
});
