package net.corda.solarsystem.contracts

import net.corda.solarsystem.states.ProbeState
import net.corda.v5.ledger.contracts.CommandData
import net.corda.v5.ledger.contracts.Contract
import net.corda.v5.ledger.contracts.requireSingleCommand
import net.corda.v5.ledger.contracts.requireThat
import net.corda.v5.ledger.transactions.LedgerTransaction
import net.corda.v5.ledger.transactions.outputsOfType

/**
 * An implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [ProbeState], which in turn encapsulates an [ProbeState].
 *
 * For a new [ProbeState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [ProbeState].
 * - An Launch() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class ProbeContract : Contract {
    companion object {
        @JvmStatic
        val ID: String = ProbeContract::class.java.canonicalName
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands.Launch>()
        requireThat {
            // Generic constraints around the transaction.
            "No inputs should be consumed when launching a Probe." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<ProbeState>().single()
            "The launcher and the target cannot be the same entity." using (out.launcher != out.target)
            "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

            // Probe-specific constraints.
            "The message's value must be non-empty." using (out.message.isNotEmpty())

            // Planetary probes can only visit planets
            "Solar System Objects Require an Org Unit in the x500 name" using (!out.target.name.organisationUnit.isNullOrBlank())
            if(out.planetaryOnly) {
                "Planetary Probes Must only visit planets" using (out.target.name.organisationUnit?.toLowerCase() == "planet")
            }
        }
    }

    /**
     * This contract only implements one command, Launch.
     */
    interface Commands : CommandData {
        class Launch : Commands
    }
}