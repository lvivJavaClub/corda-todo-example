package com.template.states;

import com.template.contracts.TodoContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(TodoContract.class)
public class TodoState implements ContractState, LinearState {

    private Party assignedBy;
    private Party assignedTo;
    private String description;
    private UniqueIdentifier linearId = new UniqueIdentifier();

    public TodoState(Party assignedBy, Party assignedTo, String description) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.description = description;
    }

    @ConstructorForDeserialization
    public TodoState(Party assignedBy, Party assignedTo, String description, UniqueIdentifier linearId) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.description = description;
        this.linearId = linearId;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(assignedBy, assignedTo);
    }

    public TodoState assignTo(Party assignedTo) {
        return new TodoState(assignedBy, assignedTo, description);
    }
}
