import React, { useContext, useEffect, useRef, useState } from "react";
import probe from "../Assets/r3_probe.svg";
import "./SolarSystem.scss";
import OptionsSideDrawer from "../Components/OptionsSideDrawer";
import DialogBox from "../Components/DialogBox";
import MembershipGroupService from "../API/MembershipGroupService";
import { PlanetContext } from "../Context/PlanetContext";

const SolarSystem: React.FC = () => {
  let MARS = "MARS";
  let EARTH = "EARTH";
  let PLUTO = "PLUTO";

  const probeRef = useRef<HTMLDivElement>(null);
  const sunRef = useRef<HTMLDivElement>(null);
  const earthRef = useRef<HTMLDivElement>(null);
  const marsRef = useRef<HTMLDivElement>(null);
  const plutoRef = useRef<HTMLDivElement>(null);

  const [myMemberInfo, setMyMemberInfo] = useState<any>(null);
  const [isSending, setIsSending] = useState<boolean>(false);
  const [dialogMessage, setDialogMessage] = useState<string>("");
  const [isPlanetSelected, setIsPlanetSelected] = useState<boolean>(false);
  const [isFlowChecking, setIsFlowChecking] = useState<boolean>(false);
  const [isVaultViewing, setIsVaultViewing] = useState<boolean>(false);
  const [selectedPlanet, setSelectedPlanet] = useState<string>("");
  const [flowId, setFlowId] = useState("");
  const [isCompleted, setIsCompleted] = useState<boolean>(false);
  const { currentPlanet } = useContext(PlanetContext);

  const getMyMemberInfo = async () => {
    try {
      const response = await MembershipGroupService.getMyMemberInfo();
      setMyMemberInfo(response.data);
    } catch (error) {
      console.log("ERROR: ", error);
    }
  };

  function getSelectedPlanetInfo(planet: string) {
    setIsPlanetSelected(true);
    setSelectedPlanet(planet);
  }

  function goToSelectedPlanetPosition(planet: String) {
    let planetReference = null;

    if (planet === MARS && planet !== currentPlanet) {
      planetReference = marsRef;
    } else if (planet === PLUTO && planet !== currentPlanet) {
      planetReference = plutoRef;
    } else if (planet === EARTH && planet !== currentPlanet) {
      planetReference = earthRef;
    } else {
      return null;
    }

    if (planetReference?.current && probeRef.current) {
      let xPosition =
        planetReference.current?.getBoundingClientRect().x -
        probeRef.current.offsetWidth / 2 / 2;
      let yPosition =
        planetReference.current?.getBoundingClientRect().y -
        probeRef.current.offsetHeight / 2 / 2;

      let translate3dValue = `translate3d(${xPosition}px, ${yPosition}px, 0) ${
        xPosition > 500 ? `rotate(90deg)` : `rotate(270deg)`
      }`;

      if (isSending) {
        probeRef.current.style.transform = translate3dValue;
      }
    }
  }

  function getCurrentPlanetCoords() {
    let planetReference = null;

    if (currentPlanet === MARS) {
      planetReference = marsRef;
    } else if (currentPlanet === PLUTO) {
      planetReference = plutoRef;
    } else if (currentPlanet === EARTH) {
      planetReference = earthRef;
    } else {
      return null;
    }

    if (planetReference?.current && probeRef.current) {
      let xPosition =
        planetReference.current?.getBoundingClientRect().x -
        probeRef.current.offsetWidth / 2 / 2;
      let yPosition =
        planetReference.current?.getBoundingClientRect().y -
        probeRef.current.offsetHeight / 2 / 2;

      let translate3dValue = `translate3d(${xPosition}px, ${yPosition}px, 0) ${
        xPosition > 300 ? `rotate(90deg)` : `rotate(270deg)`
      }`;
      probeRef.current.style.transform = translate3dValue;
    }
  }

  useEffect(() => {
    getMyMemberInfo();
    getCurrentPlanetCoords();
  }, [currentPlanet]);

  return (
    <main>
      <section className="solar-system">
        <div className="stars"></div>
        <div className="probe" ref={probeRef}>
          <img
            src={probe}
            className={`r3-probe ${isSending ? `is-active` : `is-not-active`}`}
            alt="probe"
          />
        </div>
        <div className="sun" ref={sunRef}></div>

        <div className="solar-system-grid">
          <section className="planets">
            <div className="mercury"></div>
            <div className="venus"></div>
            <div
              className={`earth ${isSending ? `planet-hover` : ``}`}
              ref={earthRef}
              onClick={
                isSending ? () => getSelectedPlanetInfo(EARTH) : undefined
              }
            ></div>
            <div
              className={`mars ${isSending ? `planet-hover` : ``}`}
              ref={marsRef}
              onClick={
                isSending ? () => getSelectedPlanetInfo(MARS) : undefined
              }
            ></div>
            <div className="jupiter"></div>
            <div className="saturn"></div>
            <div className="uranus"></div>
            <div className="neptune"></div>
            <div
              className={`pluto ${isSending ? `planet-hover` : ``}`}
              ref={plutoRef}
              onClick={
                isSending ? () => getSelectedPlanetInfo(PLUTO) : undefined
              }
            ></div>
          </section>

          <OptionsSideDrawer
            getCurrentPlanetCoords={getCurrentPlanetCoords}
            setDialogMessage={setDialogMessage}
            flowId={flowId}
            planetSelectedToggle={setIsPlanetSelected}
            setIsFlowChecking={setIsFlowChecking}
            setIsVaultViewing={setIsVaultViewing}
            sendProbeToggle={setIsSending}
            setIsCompleted={setIsCompleted}
            isSending={isSending}
            isFlowChecking={isFlowChecking}
            isVaultViewing={isVaultViewing}
            isCompleted={isCompleted}
            myMemberInfo={myMemberInfo}
          />

          <DialogBox
            isFlowChecking={isFlowChecking}
            isVaultViewing={isVaultViewing}
            setFlowUIId={setFlowId}
            sendProbeToggle={setIsSending}
            setIsCompleted={setIsCompleted}
            selectedPlanet={selectedPlanet}
            isSending={isSending}
            isCompleted={isCompleted}
            isPlanetSelected={isPlanetSelected}
            dialogMessage={dialogMessage}
            goToSelectedPlanetPosition={goToSelectedPlanetPosition}
          />
        </div>
      </section>
    </main>
  );
};

export default SolarSystem;
