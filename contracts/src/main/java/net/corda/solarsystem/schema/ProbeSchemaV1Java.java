package net.corda.solarsystem.schema;

import net.corda.v5.ledger.schemas.PersistentState;
import net.corda.v5.persistence.MappedSchema;
import net.corda.v5.persistence.UUIDConverter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

/**
 * A ProbeStateJava schema.
 */
public class ProbeSchemaV1Java extends MappedSchema {
    public ProbeSchemaV1Java() {
        super(ProbeSchemaJava.class, 1, List.of(PersistentProbeJava.class));
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "probe-java.changelog-master";
    }

    @Entity
    @NamedQuery(
            name = "ProbeSchemaV1Java.PersistentProbeJava.findByLauncherNameNot",
            query = "FROM net.corda.solarsystem.schema.ProbeSchemaV1Java$PersistentProbeJava it WHERE it.launcherName <> :launcherName"
    )
    @Table(name = "probe_state_java")
    public static class PersistentProbeJava extends PersistentState {
        @Column(name = "message")
        private String message;
        @Column(name = "planetary_only")
        private Boolean planetaryOnly;
        @Column(name = "launcher")
        private String launcherName;
        @Column(name = "target")
        private String targetName;
        @Column(name = "linear_id")
        @Convert(converter = UUIDConverter.class)
        private UUID linearId;

        public PersistentProbeJava(String message, Boolean planetaryOnly, String launcherName, String targetName, UUID linearId) {
            this.message = message;
            this.planetaryOnly = planetaryOnly;
            this.launcherName = launcherName;
            this.targetName = targetName;
            this.linearId = linearId;
        }

        public PersistentProbeJava() {
            this("", false, "", "", UUID.randomUUID());
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getPlanetaryOnly() {
            return planetaryOnly;
        }

        public void setPlanetaryOnly(Boolean planetaryOnly) {
            this.planetaryOnly = planetaryOnly;
        }

        public String getLauncherName() {
            return launcherName;
        }

        public void setLauncherName(String launcherName) {
            this.launcherName = launcherName;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public void setLinearId(UUID linearId) {
            this.linearId = linearId;
        }
    }
}
