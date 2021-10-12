package net.corda.solarsystem.flows;

import net.corda.solarsystem.contracts.ProbeContractJava;
import net.corda.solarsystem.states.ProbeStateJava;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.*;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.flows.flowservices.FlowMessaging;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.IdentityService;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.UniqueIdentifier;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.services.NotaryLookupService;
import net.corda.v5.ledger.services.TransactionService;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;

import java.util.*;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class LaunchProbeFlowJava implements Flow<SignedTransactionDigest> {

    private final RpcStartFlowRequestParameters params;

    @CordaInject
    private FlowEngine flowEngine;

    @CordaInject
    private FlowIdentity flowIdentity;

    @CordaInject
    private FlowMessaging flowMessaging;

    @CordaInject
    private TransactionService transactionService;

    @CordaInject
    private TransactionBuilderFactory transactionBuilderFactory;

    @CordaInject
    private IdentityService identityService;

    @CordaInject
    private NotaryLookupService notaryLookup;

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @JsonConstructor
    public LaunchProbeFlowJava(RpcStartFlowRequestParameters params) {
        this.params = params;
    }

    @Override
    @Suspendable
    public SignedTransactionDigest call() {

        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

        String message;
        boolean planetaryOnly;
        CordaX500Name target;

        if (!parametersMap.containsKey("message"))
            throw new BadRpcStartFlowRequestException("Parameter \"message\" missing.");
        else
            message = parametersMap.get("message");

        if (!parametersMap.containsKey("planetaryOnly"))
            throw new BadRpcStartFlowRequestException("Parameter \"planetaryOnly\" missing.");
        else
            planetaryOnly = Boolean.parseBoolean(parametersMap.get("planetaryOnly"));

        if (!parametersMap.containsKey("target"))
            throw new BadRpcStartFlowRequestException("Parameter \"target\" missing.");
        else
            target = CordaX500Name.parse(parametersMap.get("target"));

        Party recipientParty = identityService.partyFromName(target);
        if (recipientParty == null) throw new NoSuchElementException("No party found for X500 name " +target);
        Party notary = notaryLookup.getNotaryIdentities().get(0);

        // Stage 1.
        // Generate an unsigned transaction.
        ProbeStateJava probeState = new ProbeStateJava(message, planetaryOnly, flowIdentity.getOurIdentity(), recipientParty, new UniqueIdentifier());
        Command txCommand = new Command(new ProbeContractJava.Commands.Launch(), probeState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList()));

        TransactionBuilder txBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(probeState, ProbeContractJava.ID)
                .addCommand(txCommand);

        // Stage 2.
        // Verify that the transaction is valid.
        txBuilder.verify();

        // Stage 3.
        // Sign the transaction.
        SignedTransaction partSignedTx = txBuilder.sign();

        // Stage 4.
        // Send the state to the counterparty, and receive it back with their signature.
        FlowSession otherPartySession = flowMessaging.initiateFlow(recipientParty);

        SignedTransaction fullySignedTx = flowEngine.subFlow(
                new CollectSignaturesFlow(
                        partSignedTx,
                        List.of(otherPartySession)
                )
        );

        // Stage 5.
        // Notarise and record the transaction in both parties' vaults.
        SignedTransaction notarisedTx = flowEngine.subFlow(
                new FinalityFlow(fullySignedTx, List.of(otherPartySession))
        );

        //Step 6.
        // Return Json output
        return new SignedTransactionDigest(
                notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                notarisedTx.getSigs()
        );

    }

    public FlowEngine getFlowEngine() {
        return flowEngine;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public JsonMarshallingService getJsonMarshallingService() {
        return jsonMarshallingService;
    }
}

