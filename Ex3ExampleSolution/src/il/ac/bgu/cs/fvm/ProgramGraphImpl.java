package il.ac.bgu.cs.fvm;

import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgramGraphImpl implements ProgramGraph {

    private Set<Location> locations;
    private Set<Location> initialLocations;
    private Set<Action> actions;
    private Set<String> conditions;
    private Set<PGTransition> transitions;
    private String name;
    private Set<List<String>> initializations;


    public ProgramGraphImpl() {
        this.locations = new HashSet<>();
        this.initialLocations = new HashSet<>();
        this.actions = new HashSet<>();
        this.conditions = new HashSet<>();
        this.transitions = new HashSet<>();
        this.initializations = new HashSet<>();
    }

    public Set<Action> getActions(){
        return this.actions;
    }

    @Override
    public void addInitalization(List<String> init) {
        this.initializations.add(init);
    }

    @Override
    public void addInitialLocation(Location location) {
        this.initialLocations.add(location);
    }

    @Override
    public void addLocation(Location l) {
        this.locations.add(l);
    }

    @Override
    public void addTransition(PGTransition t) {
        this.transitions.add(t);
    }

    @Override
    public Set<List<String>> getInitalizations() {
        return this.initializations;
    }

    @Override
    public Set<Location> getInitialLocations() {
        return this.initialLocations;
    }

    @Override
    public Set<Location> getLocations() {
        return this.locations;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public HashSet<PGTransition> getTransitions() {
        return (HashSet<PGTransition>) this.transitions;
    }

    @Override
    public void removeLocation(Location l) {
        this.locations.remove(l);
    }

    @Override
    public void removeTransition(PGTransition t) {
        this.transitions.remove(t);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}