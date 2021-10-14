import React, { useContext, useEffect, useState } from "react";
import FlowService from "../API/FlowService";
import { PlanetContext } from "../Context/PlanetContext";
import "./DialogBox.scss";

interface DialogProps {
  setFlowUIId: any;
  goToSelectedPlanetPosition: any;
  sendProbeToggle: (value: boolean) => void;
  setIsCompleted: (value: boolean) => void;
  isSending: any;
  isCompleted: any;
  isPlanetSelected: any;
  dialogMessage: string;
  selectedPlanet: String;
  isFlowChecking: any;
  isVaultViewing: any;
}

const DialogBox: React.FC<DialogProps> = ({
  goToSelectedPlanetPosition,
  setFlowUIId,
  isSending,
  isCompleted,
  sendProbeToggle,
  setIsCompleted,
  isPlanetSelected,
  dialogMessage,
  selectedPlanet,
  isFlowChecking,
  isVaultViewing,
}) => {
  let MARS = "MARS";
  let EARTH = "EARTH";
  let PLUTO = "PLUTO";

  const [probeMessage, setProbeMessage] = useState<string>("");
  const [planetaryOnly, setPlanetaryOnly] = useState<boolean>(false);
  const [flowId, setFlowId] = useState("");
  const [flowInputId, setFlowInputId] = useState("");
  const [flowStatusMessage, setFlowStatusMessage] = useState("");
  const [flowStatus, setFlowStatus] = useState<string>("");
  const [isCopied, setIsCopied] = useState<boolean>(false);
  const [probeMessages, setProbeMessages] = useState<[]>([]);

  const [dialogFlowMessage, setDialogFlowMessage] =
    useState<string>(dialogMessage);

  const { currentPlanet } = useContext(PlanetContext);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(flowId);
    setIsCopied(true);
  };

  const getFlowOutcome = async (flowId: string) => {
    try {
      const response = await FlowService.getFlowOutcome(flowId);

      if (response.data.status === "COMPLETED") {
        goToSelectedPlanetPosition(selectedPlanet);
        setFlowStatusMessage(`Flow status: ${response.data.status}`);
        setDialogFlowMessage(response.data.resultJson);
        setProbeMessages(JSON.parse(response.data.resultJson).reverse());
      } else if (response.data.status === "RUNNING") {
        getFlowOutcome(flowId);
        setFlowStatusMessage(`Flow status: ${response.data.status}...`);
      } else {
        setFlowStatusMessage(`Flow status is ${response.data.status}`);
        setDialogFlowMessage(response.data.exceptionDigest.message);
      }
    } catch (error) {
      console.log("ERROR: ", error);
    }
  };

  const LaunchProbeFlow = async () => {
    let targetPlanet = "";

    if (selectedPlanet === MARS) {
      targetPlanet = "C=GB, L=FOURTH, O=MARS, OU=PLANET";
    } else if (selectedPlanet === EARTH) {
      targetPlanet = "C=IE, L=THIRD, O=EARTH, OU=PLANET";
    } else if (selectedPlanet === PLUTO) {
      targetPlanet = "C=US, L=NINTH, O=PLUTO, OU=DWARF_PLANET";
    } else {
      targetPlanet = "";
    }
    try {
      const response = await FlowService.LaunchProbeFlow(
        probeMessage,
        targetPlanet,
        planetaryOnly
      );

      setIsCopied(false);
      setFlowId(response.data.flowId.uuid);
      setFlowStatus(response.data.status);
      getFlowOutcome(response.data.flowId.uuid);
      setFlowUIId(response.data.flowId.uuid);
    } catch (error) {
      console.log("ERROR: ", error);
    }
    setIsCompleted(true);
    setProbeMessage("");
    setPlanetaryOnly(false);
  };

  const viewMessages = async () => {
    try {
      const response = await FlowService.ListVisitedProbeMessagesFlow();
      setFlowId(response.data.flowId.uuid);
      setFlowStatus(response.data.status);
      getFlowOutcome(response.data.flowId.uuid);
      setFlowUIId(response.data.flowId.uuid);
    } catch (error) {
      console.log("ERROR: ", error);
    }
  };

  useEffect(() => {
    if (!isCompleted) {
      setFlowInputId("");
    }
  }, [isCompleted]);

  if (isSending) {
    return (
      <section className="dialog-box">
        <div className="dialog">
          {isPlanetSelected && !isFlowChecking && !isCompleted && (
            <>
              <h2>Planet selected: {selectedPlanet}</h2>
              <hr />
              <p>What message would you like to send to {selectedPlanet}?</p>
              <textarea
                autoFocus
                value={probeMessage}
                placeholder="Type message here"
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                  setProbeMessage(e.target.value)
                }
              ></textarea>
              <div className="checkbox">
                <input
                  type="checkbox"
                  id="scales"
                  name="scales"
                  onChange={() => {
                    setPlanetaryOnly(true);
                  }}
                />
                <label>Planetary Only?</label>
              </div>

              <button className="submit-button" onClick={LaunchProbeFlow}>
                SEND PROBE
              </button>
            </>
          )}

          {!isPlanetSelected && !isFlowChecking && !isCompleted && (
            <>
              <h2>Select a Planet to send a Probe from {currentPlanet}!</h2>
            </>
          )}

          {isPlanetSelected && !isFlowChecking && isCompleted && (
            <>
              <p>{flowStatusMessage}</p>
              <p>
                FLOW ID: {flowId}{" "}
                <span className="copy" onClick={copyToClipboard}>
                  <small>{isCopied ? `COPIED` : `COPY`}</small>
                </span>
              </p>
              <textarea value={dialogFlowMessage} readOnly></textarea>
            </>
          )}
        </div>
      </section>
    );
  } else if (isFlowChecking) {
    return (
      <section className="dialog-box">
        <div className="dialog">
          {!isSending && !isVaultViewing && (
            <>
              <h2>Check Flow Outcomes here:</h2>
              <hr />
              <section className="flow-check-input">
                <input
                  type="text"
                  value={flowInputId}
                  className="input-box"
                  placeholder={`"Flow ID"`}
                  onChange={(e: React.ChangeEvent<any>) =>
                    setFlowInputId(e.target.value)
                  }
                />
                <button
                  className="flow-check-button"
                  onClick={() => {
                    getFlowOutcome(flowInputId);
                    setIsCompleted(true);
                  }}
                >
                  CHECK FLOW
                </button>
              </section>
              {isCompleted && (
                <>
                  <textarea value={dialogFlowMessage} readOnly></textarea>
                </>
              )}
            </>
          )}
        </div>
      </section>
    );
  } else if (isVaultViewing) {
    return (
      <section className="dialog-box">
        <div className="dialog">
          {!isSending && !isFlowChecking && (
            <>
              <h2>View Messages that have been sent to {currentPlanet}:</h2>
              <hr />
              <section className="flow-check-input">
                <button
                  className="flow-check-button"
                  onClick={() => {
                    viewMessages();
                    setIsCompleted(true);
                  }}
                >
                  Check Messages
                </button>
              </section>
              <div className="vault-messages-output">
                {probeMessages.length > 0
                  ? probeMessages.map((message: any, index: any) => (
                      <article
                        className="vault-messages"
                        key={`itemName-${index}`}
                      >
                        <p>{message}</p>
                      </article>
                    ))
                  : ""}
              </div>
            </>
          )}
        </div>
      </section>
    );
  } else
    return (
      <section className="dialog-box">
        <div className="dialog">
          {!isCompleted && !isSending && !isPlanetSelected && !isFlowChecking && (
            <>
              <h2>
                Welcome to CORDA 5 Solar System, choose an option from the menu
                to begin.
              </h2>
            </>
          )}
        </div>
      </section>
    );
};

export default DialogBox;
