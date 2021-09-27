import { useEffect, useMemo, useState } from "react";
import { BrowserRouter as Router, Link, Route } from "react-router-dom";
import { PlanetContext } from "./Context/PlanetContext";
import ProtectedRoute from "./Routes/ProtectedRoute";
import SolarSystem from "./SolarSystem/SolarSystem";
import "./App.scss";

function App() {
  const [currentPlanet, setCurrentPlanet] = useState<any>(null);
  const [dateYear] = useState(new Date().getFullYear());
  let currentSession = sessionStorage.getItem("currentPlanet");

  const providerValue = useMemo(
    () => ({ currentPlanet, setCurrentPlanet }),
    [currentPlanet, setCurrentPlanet]
  );

  useEffect(() => {
    setCurrentPlanet(currentSession);
  }, [currentSession]);

  return (
    <Router>
      <Route path="/" exact>
        <main>
          <div className="stars"></div>
          <section className="login-screen">
            <section className="login-planet-selection">
              <h1>CORDA 5 SOLAR SYSTEM</h1>
              <Link
                className="planet-container"
                to="/solarSystem"
                onClick={async () => {
                  sessionStorage.setItem("currentPlanet", "EARTH");
                  setCurrentPlanet("EARTH");
                }}
              >
                EARTH
              </Link>
              <Link
                className="planet-container"
                to="/solarSystem"
                onClick={async () => {
                  sessionStorage.setItem("currentPlanet", "MARS");
                  setCurrentPlanet("MARS");
                }}
              >
                MARS
              </Link>
              <Link
                className="planet-container"
                to="/solarSystem"
                onClick={async () => {
                  sessionStorage.setItem("currentPlanet", "PLUTO");
                  setCurrentPlanet("PLUTO");
                }}
              >
                PLUTO
              </Link>
              <p>
                <small>&#169; {dateYear} R3. All rights reserved.</small>
              </p>
            </section>
          </section>
        </main>
      </Route>

      <PlanetContext.Provider value={providerValue}>
        <ProtectedRoute path="/solarsystem" component={SolarSystem} />
      </PlanetContext.Provider>
    </Router>
  );
}

export default App;
