package net.corda.solarsystem.states;

import com.google.gson.Gson;
import net.corda.solarsystem.contracts.ProbeContractJava;
import net.corda.solarsystem.schema.ProbeSchemaV1Java;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.ledger.UniqueIdentifier;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.contracts.LinearState;
import net.corda.v5.ledger.schemas.PersistentState;
import net.corda.v5.ledger.schemas.QueryableState;
import net.corda.v5.persistence.MappedSchema;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@CordaSerializable
@BelongsToContract(ProbeContractJava.class)
public class ProbeStateJava implements LinearState, QueryableState, JsonRepresentable {
    // state data
    private String message;
    private Boolean planetaryOnly;

    // parties involved
    private Party launcher;
    private Party target;

    private UniqueIdentifier linearId;

    public ProbeStateJava() {
    }

    @ConstructorForDeserialization
    public ProbeStateJava(String message, Boolean planetaryOnly, Party launcher, Party target, UniqueIdentifier linearId) {
        this.message = message;
        this.planetaryOnly = planetaryOnly;
        this.launcher = launcher;
        this.target = target;
        this.linearId = linearId;
    }

    public ProbeStateJava(String message, Boolean planetaryOnly, Party launcher, Party target) {
        this.message = message;
        this.planetaryOnly = planetaryOnly;
        this.launcher = launcher;
        this.target = target;
        this.linearId = new UniqueIdentifier();
    }

    public String getMessage() {
        return message;
    }

    public Boolean isPlanetaryOnly() {
        return planetaryOnly;
    }

    public Party getLauncher() {
        return launcher;
    }

    public Party getTarget() {
        return target;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(launcher, target);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof ProbeSchemaV1Java) {
            return new ProbeSchemaV1Java.PersistentProbeJava(
                    this.message,
                    this.planetaryOnly,
                    this.launcher.getName().toString(),
                    this.target.getName().toString(),
                    this.linearId.getId()
            );
        } else {
            throw new IllegalArgumentException("Unrecognised schema " + schema);
        }
    }

    private ProbeStateDtoJava toDto() {
        return new ProbeStateDtoJava(
                message,
                planetaryOnly,
                launcher.getName().toString(),
                target.getName().toString(),
                linearId.toString()
        );
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return List.of(new ProbeSchemaV1Java());
    }

    @NotNull
    @Override
    public String toJsonString() {
        return new Gson().toJson(this.toDto());
    }

    static class ProbeStateDtoJava {
        private String message;
        private Boolean planetaryOnly;
        private String launcher;
        private String target;
        private String linearId;

        public ProbeStateDtoJava() {
        }

        public ProbeStateDtoJava(String message, Boolean planetaryOnly, String launcher, String target, String linearId) {
            this.message = message;
            this.planetaryOnly = planetaryOnly;
            this.launcher = launcher;
            this.target = target;
            this.linearId = linearId;
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

        public String getLauncher() {
            return launcher;
        }

        public void setLauncher(String launcher) {
            this.launcher = launcher;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getLinearId() {
            return linearId;
        }

        public void setLinearId(String linearId) {
            this.linearId = linearId;
        }
    }
}
