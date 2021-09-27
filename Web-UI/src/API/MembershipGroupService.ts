import axios from "./AxiosInstance";

/**
 * @remarks
 * Used for sending a request for getting own member information. x500, status, platformVersion
 *
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const getMyMemberInfo = async () => {
    try {
        const response = await axios.get(`membershipgroup/getmymemberinfo`);
        return response;
    }
    catch (error) {
        throw error;
    }
};

/**
 * @remarks
 * Used for sending a request for getting all members & members information in membership group. x500, status, platformVersion
 *
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const getAllMembers = async () => {
    try {
        const response = await axios.get(`membershipgroup/getallmembers`);
        return response;
    }
    catch (error) {
        throw error;
    }
};

/**
 * @remarks
 * Used for sending a request for getting a members information based on name query match. MARS, PLUTO, EARTH
 *
 * @param query the planet name for request to perform match upon
 * @param exactmatch whether to do exact match or allow fuzzy matches
 * 
 * @returns
 * AxiosResponse which resolves either in an error or response data
 */
const getMembersFromName = async (query: string, exactmatch: boolean) => {
    try {
        const response = await axios.get(`membershipgroup/getallmembers`, {
            params: {
                query,
                exactmatch,
            }
        });
        return response;
    }
    catch (error) {
        throw error;
    }
};

// eslint-disable-next-line import/no-anonymous-default-export
export default { getMyMemberInfo };