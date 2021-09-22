package net.corda.solarsystem.states

import com.google.gson.*
import net.corda.solarsystem.contracts.ProbeContract
import net.corda.solarsystem.schema.ProbeSchemaV1
import net.corda.v5.application.identity.AbstractParty
import net.corda.v5.application.identity.Party
import net.corda.v5.application.utilities.JsonRepresentable
import net.corda.v5.ledger.UniqueIdentifier
import net.corda.v5.ledger.contracts.BelongsToContract
import net.corda.v5.ledger.contracts.LinearState
import net.corda.v5.ledger.schemas.PersistentState
import net.corda.v5.ledger.schemas.QueryableState
import net.corda.v5.persistence.MappedSchema

/**
 * The state object recordind the probe being sent between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param message the message to be delivered by the probe.
 * @param planetaryOnly whether the probe is only meant for planets.
 * @param launcher the party launching the Probe.
 * @param target the party being visited by the Probe and returning data.
 */
@BelongsToContract(ProbeContract::class)
data class ProbeState(

    // State Data
    val message: String,
    val planetaryOnly: Boolean,

    //  Parties Involved
    val launcher: Party,
    val target: Party,

    override val linearId: UniqueIdentifier = UniqueIdentifier()
) :
    LinearState, QueryableState, JsonRepresentable {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(launcher, target)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ProbeSchemaV1 -> ProbeSchemaV1.PersistentProbe(
                this.message,
                this.planetaryOnly,
                this.launcher.name.toString(),
                this.target.name.toString(),
                this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    fun toDto(): ProbeStateDto {
        return ProbeStateDto(
            message,
            planetaryOnly,
            launcher.name.toString(),
            target.name.toString(),
            linearId.toString()
        )
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ProbeSchemaV1)
    override fun toJsonString(): String {
        return Gson().toJson(this.toDto())
    }
}

data class ProbeStateDto(
    val message: String,
    val planetaryOnly: Boolean,
    val launcher: String,
    val target: String,
    val linearId: String
)
