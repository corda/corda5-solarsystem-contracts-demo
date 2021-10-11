package net.corda.solarsystem.flows;

import net.corda.solarsystem.contracts.ProbeContractJava;
import net.corda.solarsystem.states.ProbeStateJava;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.RpcStartFlowRequestParameters;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.contracts.CommandData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.corda.testing.flow.utils.FlowMockUtils.flowTest;

public class LaunchProbeFlowTestJava {


    @Test
    public void flowSignsStateTest() {
        flowTest(LaunchProbeFlowJava.class, flowMockHelper -> {
            // NOTE: this probably should be set up in flowTest
            CordaX500Name marsX500 = CordaX500Name.parse("O=Mars, L=FIFTH, C=GB, OU=planet");

            String inputParams = "{\"message\":\"Hey Mars\", \"planetaryOnly\":\"true\", \"target\":\"" +marsX500 + "\"}";
            flowMockHelper.createFlow( fmh -> new LaunchProbeFlowJava(new RpcStartFlowRequestParameters(inputParams)));


            Mockito.doReturn(marsX500)
                    .when(flowMockHelper.getOtherSide())
                    .getName();
            Mockito.doReturn(flowMockHelper.getOtherSide())
                    .when(flowMockHelper.getFlow().getIdentityService())
                    .partyFromName(marsX500);

            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(CollectSignaturesFlow.class));

            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(FinalityFlow.class));

            List<ProbeStateJava> outputs = List.of(new ProbeStateJava("Hey Mars", true, flowMockHelper.getOurIdentity(), flowMockHelper.getOtherSide()));
            Mockito.doReturn(flowMockHelper.getWireTransactionMock())
                    .when(flowMockHelper.getSignedTransactionMock())
                    .getTx();
            Mockito.doReturn(outputs)
                    .when(flowMockHelper.getWireTransactionMock())
                    .getOutputStates();

            HashMap<String, String> inputMap = new HashMap();
            inputMap.put("message", "Hey Mars");
            inputMap.put("planetaryOnly", "true");
            inputMap.put("target", flowMockHelper.getOtherSide().getName().toString());

            Mockito.doReturn(inputMap)
                    .when(flowMockHelper.getFlow().getJsonMarshallingService())
                    .parseJson(inputParams, Map.class);

            flowMockHelper.getFlow().call();

            // verify notary is set
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).setNotary(flowMockHelper.getNotary());

            // verify the correct output state is created
            ArgumentCaptor<ProbeStateJava> probeStateArgCaptor = ArgumentCaptor.forClass(ProbeStateJava.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addOutputState(probeStateArgCaptor.capture(), Mockito.eq(ProbeContractJava.ID));
            Assertions.assertThat(probeStateArgCaptor.getValue().getLauncher()).isEqualTo(flowMockHelper.getOurIdentity());
            Assertions.assertThat(probeStateArgCaptor.getValue().getTarget()).isEqualTo(flowMockHelper.getOtherSide());
            Assertions.assertThat(probeStateArgCaptor.getValue().getMessage()).isEqualTo("Hey Mars");
            Assertions.assertThat(probeStateArgCaptor.getValue().isPlanetaryOnly()).isEqualTo(true);

            // verify command is added
            ArgumentCaptor<Command<CommandData>> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addCommand(commandArgumentCaptor.capture());
            Assertions.assertThat(commandArgumentCaptor.getValue().getValue()).isInstanceOf(ProbeContractJava.Commands.Launch.class);
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOurIdentity().getOwningKey());
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOtherSide().getOwningKey());
        });
    }
}
