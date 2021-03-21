package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.DummyTodoCommand;
import com.template.states.TodoState;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Collections;

@StartableByRPC
public class CreateTodo extends FlowLogic<Void> {

    private String task;

    public CreateTodo(String message) {
        this.task = message;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        final Party ourIdentity = getOurIdentity();

        TodoState todo = new TodoState(ourIdentity, ourIdentity, task);
        final TransactionBuilder tx = new TransactionBuilder(notary)
                .addOutputState(todo)
                .addCommand(new DummyTodoCommand(), ourIdentity.getOwningKey());

        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx);
        subFlow(new FinalityFlow(signedTransaction, Collections.<FlowSession> emptySet()));

        return null;
    }
}
