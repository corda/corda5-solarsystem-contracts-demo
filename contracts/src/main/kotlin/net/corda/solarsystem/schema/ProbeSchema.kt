package net.corda.solarsystem.schema

import net.corda.v5.ledger.schemas.PersistentState
import net.corda.v5.persistence.MappedSchema
import net.corda.v5.persistence.UUIDConverter
import java.util.UUID
import javax.persistence.*

/**
 * The family of schemas for ProbeState.
 */
object ProbeSchema

/**
 * An probeState schema.
 */
object ProbeSchemaV1 : MappedSchema(
    schemaFamily = ProbeSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentProbe::class.java)
) {

    override val migrationResource: String
        get() = "probe.changelog-master"

    @Entity
    @NamedQuery(
        name = "ProbeSchemaV1.PersistentProbe.FindAll",
        query = "FROM net.corda.solarsystem.schema.ProbeSchemaV1\$PersistentProbe"
    )
    @Table(name = "probe_states")
    class PersistentProbe(
        @Column(name = "message")
        var message: String,

        @Column(name="planetary_only")
        var planetaryOnly: Boolean,

        @Column(name = "launcher")
        var launcherName: String,

        @Column(name = "target")
        var targetName: String,

        @Column(name = "linear_id")
        @Convert(converter = UUIDConverter::class)
        var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor() : this("", false, "", "", UUID.randomUUID())
    }
}
