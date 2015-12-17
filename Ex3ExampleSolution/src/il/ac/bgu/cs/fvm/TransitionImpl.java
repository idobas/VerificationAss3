package il.ac.bgu.cs.fvm;

import il.ac.bgu.cs.fvm.exceptions.*;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

import java.util.*;

/**
 * Created by idob on 11/6/2015.
 */
public class TransitionImpl implements TransitionSystem {


    private Set<State> states;
    private Set<State> initialStates;
    private Set<Action> actions;
    private Map<State, Set<String>> labels;
    private Set<String> ap;
    private Set<Transition> transitions;
    private String name;

    public TransitionImpl() {
        this.states = new HashSet<>();
        this.transitions = new HashSet<>();
        this.actions = new HashSet<>();
        this.labels = new HashMap<>();
        this.ap = new HashSet<>();
        this.initialStates = new HashSet<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addAction(Action action) {
        this.actions.add(action);
    }

    @Override
    public void addAtomicProposition(String p) {
        this.ap.add(p);
    }

    @Override
    public void addInitialState(State state) throws FVMException {
        if (this.states.contains(state))
            this.initialStates.add(state);
        else
            throw new InvalidInitialStateException(state);
    }

    @Override
    public void addLabel(State s, String l) throws FVMException {
        if (this.ap.contains(l) && this.states.contains(s)) {
            Set<String> stateLabels = this.labels.get(s);
            stateLabels.add(l);
            this.labels.put(s, stateLabels);
        }
        else
            throw new InvalidLablingPairException(s,l);
    }

    @Override
    public void addState(State state) {
        this.states.add(state);
        Set<String> temp = this.labels.get(state);
        if (temp == null) {
            temp = new HashSet<>();
            this.labels.put(state, temp);
        }
    }

    @Override
    public void addTransition(Transition t) throws FVMException {
        if (!this.states.contains(t.getFrom()) || !this.states.contains(t.getTo()) || !this.actions.contains(t.getAction()))
            throw new InvalidTransitionException(t);
        else
            this.transitions.add(t);
    }

    @Override
    public Set<Action> getActions() {
        return this.actions;
    }

    @Override
    public Set<String> getAtomicPropositions() {
        return this.ap;
    }

    @Override
    public Set<State> getInitialStates() {
        return this.initialStates;
    }

    @Override
    public Map<State, Set<String>> getLabelingFunction() {
        return this.labels;
    }

    @Override
    public Set<State> getStates() {
        return this.states;
    }

    @Override
    public Set<Transition> getTransitions() {
        return this.transitions;
    }

    @Override
    public void removeAction(Action action) throws FVMException {
        for (Transition t : this.transitions
                ) {
            if (t.getAction().equals(action))
                throw new DeletionOfAttachedActionException(action, TransitionSystemPart.ACTIONS);
        }
        this.transitions.remove(action);
    }

    @Override
    public void removeAtomicProposition(String p) throws FVMException {
        for (Map.Entry<State, Set<String>> e : this.labels.entrySet()
                ) {
            if (e.getValue().contains(p))
                throw new DeletionOfAttachedAtomicPropositionException(p, TransitionSystemPart.ATOMIC_PROPOSITIONS);
        }
        this.ap.remove(p);
    }

    @Override
    public void removeInitialState(State state) {
        this.initialStates.remove(state);
    }

    @Override
    public void removeLabel(State s, String l) {
        Set<String> e = this.labels.get(s);
        e.remove(l);
        this.labels.replace(s, e);
    }

    @Override
    public void removeState(State state) throws FVMException {
        if (this.labels.containsKey(state) && !(this.labels.get(state).size() == 0))
            throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
        else if (this.initialStates.contains(state))
            throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
        else if (!isStateInTransitions(state))
            throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
        else
            this.states.remove(state);
    }

    private boolean isStateInTransitions(State s) {
        for (Transition t : this.transitions
                ) {
            if (t.getFrom().equals(s) || t.getTo().equals(s))
                return false;
        }
        return true;
    }

    @Override
    public void removeTransition(Transition t) {
        this.transitions.remove(t);
    }
}