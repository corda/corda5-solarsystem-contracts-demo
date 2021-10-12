package net.corda.solarsystem.contracts;

import net.corda.solarsystem.states.ProbeStateJava;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.corda.v5.ledger.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.v5.ledger.contracts.ContractsDSL.requireThat;

public class ProbeContractJava implements Contract {

    public static String ID = ProbeContractJava.class.getCanonicalName();

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        Command<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        requireThat(req -> {
            // Generic constraints around the transaction.
            req.using("No inputs should be consumed when launching a Probe.", tx.getInputs().isEmpty());
            req.using("Only one output state should be created.", tx.getOutputs().size() == 1);

            List<ProbeStateJava> output = tx.outputsOfType(ProbeStateJava.class);
            req.using("Output state should be of type 'ProbeStateJava'.", output.size() == 1);

            ProbeStateJava out = output.get(0);
            req.using("The launcher and the target cannot be the same entity.", out.getLauncher() != out.getTarget());

            List<PublicKey> participantKeys = out.getParticipants().stream()
                    .map(AbstractParty::getOwningKey)
                    .collect(Collectors.toList());
            req.using("All of the participants must be signers.", command.getSigners().containsAll(participantKeys));

            // Probe-specific constraints.
            req.using("The message's value must be non-empty.", !out.getMessage().isEmpty());

            // Planetary probes can only visit planets
            String organisationUnit = out.getTarget().getName().getOrganisationUnit();
            req.using("Solar System Objects Require an Org Unit in the x500 name", (organisationUnit != null && !organisationUnit.isEmpty()));
            if(out.isPlanetaryOnly()) {
                req.using("Planetary Probes Must only visit planets", Objects.requireNonNull(organisationUnit).equalsIgnoreCase("planet"));
            }
            return null;
        });
    }

    public interface Commands extends CommandData {
        class Launch implements Commands {
        }
    }
}
