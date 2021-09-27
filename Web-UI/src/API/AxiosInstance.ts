import axios, { AxiosRequestConfig } from "axios";

axios.interceptors.request.use(async (config: AxiosRequestConfig) => {

let user = sessionStorage.getItem('currentPlanet');
let username = '';
let APIURL = '';

    switch (user) {
        case 'EARTH':
            APIURL = `earth/api/v1/`;
            username = 'earthling';
            break;
        case 'MARS':
            APIURL = `mars/api/v1/`;
            username = 'martian';
            break;
        case 'PLUTO':
            APIURL = `pluto/api/v1/`;
            username = 'plutonian';
            break;
        default:
            console.log("No Planet Selected");
    }

    config.baseURL = APIURL;

    config.headers = {
        'accept': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET,PUT,POST,DELETE,PATCH,OPTIONS',
    };
    config.auth = {
        username: username,
        password: 'password'
      }
    return config;
});

export default axios;