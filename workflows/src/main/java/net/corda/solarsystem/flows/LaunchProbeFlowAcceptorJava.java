package net.corda.solarsystem.flows;

import net.corda.systemflows.ReceiveFinalityFlow;
import net.corda.systemflows.SignTransactionFlow;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.FlowSession;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

@InitiatedBy(LaunchProbeFlowJava.class)
public class LaunchProbeFlowAcceptorJava implements Flow<SignedTransaction> {

    private final FlowSession counterPartySession;

    @CordaInject
    private FlowEngine flowEngine;

    public LaunchProbeFlowAcceptorJava(FlowSession counterPartySession) {
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
