# Corda 5 Solar System

A simple Corda 5 Cordapp that allows you to launch probes between planets with little messages. So Come say "Hello" to Mars or Pluto.


## Flows

There are two flows `LaunchProbeFlow`/`LaunchProbeFlowJava` which take two parameters:

- `message` (String) - A message to send with the probe
- `target`  (x500 String) - The X500 name of the probes target

And `ListVisitedProbeMessagesFlow` which takes no parameters, but returns a list of recieved messages.


## Deploying and Testing
### Required Prerequisites

- Corda CLI
- Cordapp Builder
- Node CLI
- Docker

### Deployment via Corda CLI

1. Navigate to the app directory
2. Build the app with `gradlew build`
3. Build the Cordapp with the cordapp-builder CLI util `cordapp-builder create --cpk contracts\build\libs\<CPK FILE NAME> --cpk workflows\build\libs\<CPK FILE NAME> -o solar-system.cpb`
4. Configure the network with `corda-cli network config docker-compose solar-system`
5. Build the network deployment dockerfile using corda-cli `corda-cli network deploy -n solar-system -f solar-system.yaml > docker-compose.yaml`
6. Deploy the network using docker-compose `docker-compose -f docker-compose.yaml up -d`
7. When deployed check the status with corda-cli `corda-cli network status -n solar-system` note the mapped web ports for Http RPC
8. Install the application on the network using corda-cli `corda-cli package install -n solar-system solar-system.cpb`

### Testing VIA Swagger
- Using the port noted from the network status visit `https://localhost:<port>/api/v1/swagger`
- Login using the button on the top right usernames and passwords are as follows:

  | Planet | Username  | Password |
  |--------|-----------|----------|
  | Earth  | earthling | password |
  | Mars   | martian   | password |
  | Pluto  | plutonian | password |

- Launch the `LaunchProbeFlow` via the Start Flow api by passing something similar

```
{
  "rpcStartFlowRequest": {
    "clientId": "launchpad-2", 
    flowName": "net.corda.solarsystem.flows.LaunchProbeFlow", 
    "parameters": { 
      "parametersInJson": "{\"message\": \"Hello Mars\", \"target\": \"C=GB, L=FOURTH, O=MARS, OU=PLANET\", \"planetaryOnly\":\"true\"}" 
    } 
  } 
}
```

### Testing via Node CLI


1. Add a node `corda-node-cli endpoint add -n earth --basic-auth -u earthling -P password https://localhost:<port>/api/v1/`
2. set the node as default `corda-node-cli endpoint set -e earth`
3. launch a flow `corda-node-cli flow start -n LaunchProbeFlow -A message="hello" -A target="C=US, L=NINTH, O=PLUTO, OU=DWARF_PLANET" -A planetaryOnly=true -u earthling -P password`

### Web UI

Please see the Web-UI directory for details on installing and luanching a web interface
