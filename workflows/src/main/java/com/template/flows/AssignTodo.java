package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.DummyTodoCommand;
import com.template.states.TodoState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@StartableByRPC
@InitiatingFlow
public class AssignTodo extends FlowLogic<Void> {

    private final String linearId;
    private final String assignedTo;

    public AssignTodo(String linearId, String assignedTo) {
        this.linearId = linearId;
        this.assignedTo = assignedTo;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        final QueryCriteria linearStateQueryCriteria = new QueryCriteria
                .LinearStateQueryCriteria(null, Collections.singletonList(UUID.fromString(linearId)));

        final Vault.Page<TodoState> todoStatePage = getServiceHub().getVaultService().queryBy(TodoState.class, linearStateQueryCriteria);
        final StateAndRef<TodoState> todoStateStateAndRef = todoStatePage.getStates().get(0);
        final TodoState todo = todoStateStateAndRef.getState().getData();

        final Set<Party> parties = getServiceHub().getIdentityService().partiesFromName(assignedTo, true);
        final Party assignToParty = parties.iterator().next();

        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        final TodoState reassignedTodo = todo.assignTo(assignToParty);
        final PublicKey owningKey = getOurIdentity().getOwningKey();

        final TransactionBuilder tx = new TransactionBuilder(notary)
                .addInputState(todoStateStateAndRef)
                .addOutputState(reassignedTodo)
                .addCommand(new DummyTodoCommand(), owningKey, assignToParty.getOwningKey());

        final FlowSession flowSession = initiateFlow(assignToParty);
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx);
        final SignedTransaction subFlow = subFlow(new CollectSignaturesFlow(signedTransaction, Collections.singleton(flowSession)));

        subFlow(new FinalityFlow(subFlow, Collections.singletonList(flowSession)));

        return null;
    }

}
