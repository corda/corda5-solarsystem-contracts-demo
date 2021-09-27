import React, { useContext } from "react";
import "./OptionsSideDrawer.scss";
import { useHistory } from "react-router-dom";
import { PlanetContext } from "../Context/PlanetContext";

interface MyMemberInfoProps {
  myMemberInfo: {
    x500Name: String;
    status: String;
    platformVersion: String;
    serial: String;
    key: String;
  };
  getCurrentPlanetCoords: () => void;
  flowId: String;
  sendProbeToggle: (value: boolean) => void;
  planetSelectedToggle: (value: boolean) => void;
  setDialogMessage: any;
  setIsFlowChecking: (value: boolean) => void;
  setIsVaultViewing: (value: boolean) => void;
  setIsCompleted: (value: boolean) => void;
  isSending: any;
  isFlowChecking: any;
  isVaultViewing: any;
  isCompleted: any;
}

const OptionsSideDrawer: React.FC<MyMemberInfoProps> = ({
  isSending,
  myMemberInfo,
  sendProbeToggle,
  setIsFlowChecking,
  setIsVaultViewing,
  isFlowChecking,
  isVaultViewing,
  setIsCompleted,
  planetSelectedToggle,
  getCurrentPlanetCoords,
}) => {
  const history = useHistory();
  const { setCurrentPlanet } = useContext(PlanetContext);

  return (
    <article className="options-side-drawer">
      <div className="options">
        <h1>CORDA 5 SOLAR SYSTEM</h1>
        <article className="member-information">
          <h2>Member Information</h2>
          <hr />
          <p>
            X500 Name: <br /> {myMemberInfo?.x500Name}
          </p>
          <p>Status: {myMemberInfo?.status}</p>
          <p>Platform Version: {myMemberInfo?.platformVersion}</p>
          <p>Serial: {myMemberInfo?.serial}</p>
        </article>

        <button
          className={`options-button ${isSending ? `button-selected` : ``}`}
          onClick={(): void => {
            sendProbeToggle(true);
            setIsFlowChecking(false);
            setIsCompleted(false);
            setIsVaultViewing(false);
            getCurrentPlanetCoords();
            planetSelectedToggle(false);
          }}
        >
          SEND PROBE
        </button>
        <button
          className={`options-button ${
            isFlowChecking ? `button-selected` : ``
          }`}
          onClick={() => {
            setIsCompleted(false);
            setIsFlowChecking(true);
            sendProbeToggle(false);
            planetSelectedToggle(false);
            setIsVaultViewing(false);
            getCurrentPlanetCoords();
          }}
        >
          Check Flow Outcome
        </button>
        <button
          className={`options-button ${
            isVaultViewing ? `button-selected` : ``
          }`}
          onClick={() => {
            setIsCompleted(false);
            setIsFlowChecking(false);
            setIsVaultViewing(true);
            sendProbeToggle(false);
            planetSelectedToggle(false);
            getCurrentPlanetCoords();
          }}
        >
          View Messages
        </button>
        <button
          className={`options-button`}
          onClick={() => {
            sessionStorage.removeItem("currentPlanet");
            history.push("/");
            setCurrentPlanet("");
            setIsCompleted(false);
            setIsFlowChecking(false);
            sendProbeToggle(false);
            planetSelectedToggle(false);
            getCurrentPlanetCoords();
          }}
        >
          LOGOUT
        </button>
      </div>
    </article>
  );
};

export default OptionsSideDrawer;
