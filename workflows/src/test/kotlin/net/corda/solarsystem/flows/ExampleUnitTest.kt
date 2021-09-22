package net.corda.solarsystem.flows

import com.nhaarman.mockito_kotlin.*
import net.corda.solarsystem.contracts.ProbeContract
import net.corda.solarsystem.states.ProbeState
import net.corda.systemflows.CollectSignaturesFlow
import net.corda.systemflows.FinalityFlow
import net.corda.testing.flow.utils.flowTest
import net.corda.v5.application.flows.RpcStartFlowRequestParameters
import net.corda.v5.application.identity.CordaX500Name
import net.corda.v5.application.services.json.parseJson
import net.corda.v5.ledger.contracts.Command
import net.corda.v5.ledger.contracts.CommandData
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test

class ExampleUnitTest {

    @Test
    fun `flow signs state`() {
        flowTest<LaunchProbeFlow> {

            // NOTE: this probably should be set up in flowTest
            val marsX500 = CordaX500Name.parse("O=Mars, L=FIFTH, C=GB, OU=planet")

            val inputParams = "{\"message\":\"Hey Mars\", \"planetaryOnly\":\"true\", \"target\":\"${marsX500}\"}"
            createFlow { LaunchProbeFlow(RpcStartFlowRequestParameters(inputParams)) }

            doReturn(marsX500)
                .whenever(otherSide)
                .name
            doReturn(otherSide)
                .whenever(flow.identityService)
                .partyFromName(marsX500)

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<CollectSignaturesFlow>())

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<FinalityFlow>())

            doReturn(
                mapOf(
                    "message" to "Hey Mars",
                    "planetaryOnly" to "true",
                    "target" to otherSide.name.toString()
                )
            )
                .whenever(flow.jsonMarshallingService)
                .parseJson<Map<String, String>>(inputParams)

            flow.call()

            // verify notary is set
            verify(transactionBuilderMock).setNotary(notary)

            // verify the correct output state is created
            argumentCaptor<ProbeState>().apply {
                verify(transactionBuilderMock).addOutputState(capture(), eq(ProbeContract.ID))
                assertSoftly {
                    it.assertThat(firstValue.launcher).isEqualTo(ourIdentity)
                    it.assertThat(firstValue.target).isEqualTo(otherSide)
                    it.assertThat(firstValue.message).isEqualTo("Hey Mars")
                    it.assertThat(firstValue.planetaryOnly).isEqualTo(true)
                }
            }

            // verify command is added
            argumentCaptor<Command<CommandData>>().apply {
                verify(transactionBuilderMock).addCommand(capture())
                assertThat(firstValue.value).isInstanceOf(ProbeContract.Commands.Launch::class.java)
                assertThat(firstValue.signers).contains(ourIdentity.owningKey)
                assertThat(firstValue.signers).contains(otherSide.owningKey)
            }
        }
    }
}
