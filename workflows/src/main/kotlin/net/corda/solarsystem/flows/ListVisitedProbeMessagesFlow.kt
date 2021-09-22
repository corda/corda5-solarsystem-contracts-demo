package net.corda.solarsystem.flows

import net.corda.solarsystem.states.ProbeState
import net.corda.v5.application.flows.*
import net.corda.v5.application.flows.flowservices.FlowIdentity
import net.corda.v5.application.injection.CordaInject
import net.corda.v5.application.services.json.JsonMarshallingService
import net.corda.v5.application.services.persistence.PersistenceService
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.stream.Cursor
import net.corda.v5.base.util.seconds
import net.corda.v5.ledger.services.vault.IdentityContractStatePostProcessor

/**
 * This flow allows planets to check what messages they have received from other planets and celestial bodies
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
@InitiatingFlow
@StartableByRPC
class ListVisitedProbeMessagesFlow @JsonConstructor constructor(private val params: RpcStartFlowRequestParameters) :
    Flow<List<String>> {

    @CordaInject
    lateinit var persistenceService: PersistenceService

    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    @CordaInject
    lateinit var flowIdentity: FlowIdentity

    @Suspendable
    override fun call(): List<String> {

        val us = flowIdentity.ourIdentity
        val cursor: Cursor<ProbeState> = persistenceService.query(
            "ProbeSchemaV1.PersistentProbe.FindAll",
            emptyMap<String, String>(),
            IdentityContractStatePostProcessor.POST_PROCESSOR_NAME
        )

        val accumulator = mutableListOf<ProbeState>()
        do {
            val poll = cursor.poll(100, 10.seconds)
            accumulator.addAll(poll.values)
        } while (!poll.isLastResult)

        return accumulator.filter { message -> message.launcher != us}
            .map { message -> jsonMarshallingService.formatJson("From: ${message.launcher.name} - Message: ${message.message}") }
    }
}


