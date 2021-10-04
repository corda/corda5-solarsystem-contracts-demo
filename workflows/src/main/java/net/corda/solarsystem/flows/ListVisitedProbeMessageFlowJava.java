package net.corda.solarsystem.flows;

import net.corda.solarsystem.states.ProbeState;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.flows.StartableByRPC;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.application.services.persistence.PersistenceService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.stream.Cursor;
import net.corda.v5.ledger.services.vault.IdentityContractStatePostProcessor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class ListVisitedProbeMessageFlowJava implements Flow<List<String>> {

    @CordaInject
    private PersistenceService persistenceService;

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @CordaInject
    private FlowIdentity flowIdentity;

    @Override
    @Suspendable
    public List<String> call() {


        Party us = flowIdentity.getOurIdentity();
        Cursor<ProbeState> cursor = persistenceService.query(
                "ProbeSchemaV1.PersistentProbe.FindAll",
                Collections.emptyMap(),
                IdentityContractStatePostProcessor.POST_PROCESSOR_NAME
        );

        ArrayList<ProbeState> accumulator = new ArrayList<>();
        Cursor.PollResult<ProbeState> poll;
        do {
            poll = cursor.poll(100, Duration.of(10, ChronoUnit.SECONDS));
            accumulator.addAll(poll.getValues());
        } while (!poll.isLastResult());

        return accumulator.stream().filter(
                message -> message.getLauncher() != us
        ).map(
                message -> jsonMarshallingService.formatJson("From: " + message.getLauncher().getName().toString() + " - Message: " + message.getMessage().toString())
        ).collect(Collectors.toList());

    }
}
