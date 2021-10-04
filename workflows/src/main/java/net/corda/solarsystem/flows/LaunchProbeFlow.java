package net.corda.solarsystem.flows;

import net.corda.solarsystem.contracts.ProbeContract;
import net.corda.solarsystem.states.ProbeState;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.systemflows.ReceiveFinalityFlow;
import net.corda.systemflows.SignTransactionFlow;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
class LaunchProbeFlowJava implements Flow<SignedTransactionDigest> {

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
            planetaryOnly = Boolean.getBoolean(parametersMap.get("planetaryOnly"));

        if (!parametersMap.containsKey("target"))
            throw new BadRpcStartFlowRequestException("Parameter \"target\" missing.");
        else
            target = CordaX500Name.parse(parametersMap.get("target"));

        Party sender = flowIdentity.getOurIdentity();
        Party recipientParty = identityService.partyFromName(target);
        if (recipientParty == null) throw new NoSuchElementException("No party found for X500 name $target");
        Party notary = notaryLookup.getNotaryIdentities().get(0);

        // Stage 1.
        // Generate an unsigned transaction.
        ProbeState probeState = new ProbeState(message, planetaryOnly, flowIdentity.getOurIdentity(), recipientParty, new UniqueIdentifier());
        Command txCommand = new Command(new ProbeContract.Commands.Launch(), probeState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList()));

        TransactionBuilder txBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(probeState, ProbeContract.getID())
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
                        Arrays.asList(otherPartySession)
                )
        );

        // Stage 5.
        // Notarise and record the transaction in both parties' vaults.
        SignedTransaction notarisedTx = flowEngine.subFlow(
                new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession))
        );

        //Step 6.
        // Return Json output
        return new SignedTransactionDigest(
                notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                notarisedTx.getSigs()
        );

    }
}

@InitiatedBy(LaunchProbeFlowJava.class)
class LaunchProbeFlowAcceptorJava implements Flow<SignedTransaction> {

    private final FlowSession counterPartySession;

    @CordaInject
    private FlowEngine flowEngine;

    LaunchProbeFlowAcceptorJava(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
    }

    @Override
    @Suspendable
    public SignedTransaction call() {
        SignedTransaction signedTransaction = flowEngine.subFlow(new MySignTransactionFlow(counterPartySession));

        return flowEngine.subFlow(new ReceiveFinalityFlow(counterPartySession, signedTransaction.getId()));
    }

    public static class MySignTransactionFlow extends SignTransactionFlow {
        MySignTransactionFlow(FlowSession counterpartySession) {
            super(counterpartySession);
        }

        @Override
        protected void checkTransaction(@NotNull SignedTransaction stx) {
        }
    }
}
