import axios from "./AxiosInstance";

/**
 * @remarks
 * Used for getting flow outcome details from a started flows flowId.
 *
 * @param flowId the flow id to check flow status.
 * 
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const getFlowOutcome = async (flowId: string) => {
    try {
        const response = await axios.get(`flowstarter/flowoutcome/${flowId}`);
        return response;
    }
    catch (error) {
        throw error;
    }
};

/**
 * @remarks
 * Used for sending a lauch probe start flow request for sending messages to other planets.
 *
 * @param probeMessage the message being sent to another planet.
 * @param targetPlanet the planet the message is being sent to.
 * @param planetaryOnly the smart contract functionality whether to send to planets only or all.
 * 
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const LaunchProbeFlow = async (probeMessage: string, targetPlanet: string, planetaryOnly: boolean) => {
  let launchPadNumber = `launchpad-${Math.random()}`;
    try {
        const response = await axios.post(`flowstarter/startflow`, {
          rpcStartFlowRequest: {
          clientId: launchPadNumber,
          flowName: "net.corda.solarsystem.flows.LaunchProbeFlowJava",
          parameters: {
            parametersInJson: `{"message": "${probeMessage}", "target": "${targetPlanet}", "planetaryOnly":"${planetaryOnly}"}`,
          },
        },
        });
        return response;
    }
    catch (error) {
        throw error;
    }
};

/**
 * @remarks
 * Used for sending a List Visited Probe Messages Flow that displays the messages that have been sent to the current planet.
 *
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const ListVisitedProbeMessagesFlow = async () => {
  let launchPadNumber = `launchpad-${Math.random()}`;
    try {
        const response = await axios.post(`flowstarter/startflow`, {
          rpcStartFlowRequest: {
          clientId: launchPadNumber,
          flowName: "net.corda.solarsystem.flows.ListVisitedProbeMessageFlowJava",
          parameters: {
            parametersInJson: ``,
          },
        },
        });
        return response;
    }
    catch (error) {
        throw error;
    }
};

// eslint-disable-next-line import/no-anonymous-default-export
export default { LaunchProbeFlow, ListVisitedProbeMessagesFlow, getFlowOutcome };