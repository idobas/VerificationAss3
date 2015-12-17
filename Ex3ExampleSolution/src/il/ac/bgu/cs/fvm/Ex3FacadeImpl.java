package il.ac.bgu.cs.fvm;

import il.ac.bgu.cs.fvm.channelsystem.ChannelSystem;
import il.ac.bgu.cs.fvm.circuits.Circuit;
import il.ac.bgu.cs.fvm.labels.Action;
import il.ac.bgu.cs.fvm.labels.LabeledElement;
import il.ac.bgu.cs.fvm.labels.Location;
import il.ac.bgu.cs.fvm.labels.State;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaFileReader;
import il.ac.bgu.cs.fvm.nanopromela.NanoPromelaParser;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

import java.util.*;
import java.util.stream.Collectors;

public class Ex3FacadeImpl implements Ex3Facade {
    @Override
    public ProgramGraph createProgramGraph() {
        return new ProgramGraphImpl();
    }

    @Override
    public TransitionSystem createTransitionSystem() {
        return new TransitionImpl();
    }

    @Override
    public ProgramGraph interleave(ProgramGraph pg1, ProgramGraph pg2) {
        ProgramGraph ans = new ProgramGraphImpl();
        getLocations(pg1, pg2, ans);
        getInitialLocations(pg1, pg2, ans);
        getInitializations(pg1, pg2, ans);
        getPGtransitions(pg1, pg2, ans);
        return ans;
    }

    private void getPGtransitions(ProgramGraph pg1, ProgramGraph pg2, ProgramGraph ans) {
        for (Location loc2 : pg2.getLocations()) {
            for (PGTransition t : pg1.getTransitions()) {
                Location from = new Location(t.getFrom().getLabel() + "," + loc2.getLabel());
                Location to = new Location(t.getTo().getLabel() + "," + loc2.getLabel());
                ans.addTransition(new PGTransition(from, t.getCondition(), t.getAction(), to));
            }
        }

        for (Location loc1 : pg1.getLocations()) {
            for (PGTransition t : pg2.getTransitions()) {
                Location from = new Location(loc1.getLabel() + "," + t.getFrom().getLabel());
                Location to = new Location(loc1.getLabel() + "," + t.getTo().getLabel());
                ans.addTransition(new PGTransition(from, t.getCondition(), t.getAction(), to));
            }
        }
    }

    private void getInitializations(ProgramGraph pg1, ProgramGraph pg2, ProgramGraph ans) {
        pg1.getInitalizations().forEach(ans::addInitalization);
        pg2.getInitalizations().forEach(ans::addInitalization);
    }

    private void getInitialLocations(ProgramGraph pg1, ProgramGraph pg2, ProgramGraph ans) {
        for (Location l1 : pg1.getInitialLocations()
                ) {
            for (Location l2 : pg2.getInitialLocations()
                    ) {
                ans.addInitialLocation(new Location(l1.getLabel() + "," + l2.getLabel()));
            }
        }
    }

    private void getLocations(ProgramGraph pg1, ProgramGraph pg2, ProgramGraph ans) {
        for (Location l1 : pg1.getLocations()
                ) {
            for (Location l2 : pg2.getLocations()
                    ) {
                ans.addLocation(new Location(l1.getLabel() + "," + l2.getLabel()));
            }
        }
    }

    @Override
    public TransitionSystem interleave(TransitionSystem ts1, TransitionSystem ts2) {
        TransitionSystem ans = new TransitionImpl();
        getAtomicProps(ts1, ts2, ans);
        getActions(ts1, ts2, ans);
        createStatesForInterleave(ts1, ts2, ans);
        createInitialStatesForInterleave(ts1, ts2, ans);
        createTransitions1(ts1, ts2, ans);
        createTransitions2(ts2, ts1, ans);
        return ans;
    }

    private void getAtomicProps(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        ts1.getAtomicPropositions().forEach(ans::addAtomicProposition);
        ts2.getAtomicPropositions().forEach(ans::addAtomicProposition);
    }

    private void createTransitions1(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        for (Transition t : ts1.getTransitions()
                ) {
            for (State s : ts2.getStates()
                    ) {
                State from = new State(t.getFrom().getLabel() + "," + s.getLabel());
                State to = new State(t.getTo().getLabel() + "," + s.getLabel());
                ans.addTransition(new Transition(from, t.getAction(), to));
            }
        }
    }

    private void createTransitions2(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        for (Transition t : ts1.getTransitions()
                ) {
            for (State s : ts2.getStates()
                    ) {
                State from = new State(s.getLabel() + "," + t.getFrom().getLabel());
                State to = new State(s.getLabel() + "," + t.getTo().getLabel());
                ans.addTransition(new Transition(from, t.getAction(), to));
            }
        }
    }


    private void createInitialStatesForInterleave(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        for (State state1 : ts1.getInitialStates()
                ) {
            for (State state2 : ts2.getInitialStates()
                    ) {
                State state = new State(state1.getLabel() + "," + state2.getLabel());
                ans.addInitialState(state);
            }
        }
    }

    private void createStatesForInterleave(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        for (State state1 : ts1.getStates()
                ) {
            for (State state2 : ts2.getStates()
                    ) {
                State state = new State(state1.getLabel() + "," + state2.getLabel());
                ans.addState(state);
                for (String label : ts1.getLabelingFunction().get(state1)
                        ) {
                    ans.addLabel(state, label);
                }
                for (String label : ts2.getLabelingFunction().get(state2)
                        ) {
                    ans.addLabel(state, label);
                }

            }
        }
    }

    @Override
    public TransitionSystem interleave(TransitionSystem ts1,
                                       TransitionSystem ts2, Set<Action> hs) {
        TransitionSystem ans = new TransitionImpl();
        createStatesForInterleave(ts1, ts2, ans);
        createInitialStatesForInterleave(ts1, ts2, ans);
        getActions(ts1, ts2, ans);
        getAtomicProps(ts1, ts2, ans);
        getTransitionsNotHleft(ts1, ts2, hs, ans);
        getTransitionsNotHright(ts1, ts2, hs, ans);
        getTransitionsInH(ts1, ts2, hs, ans);
        Set<State> reachable = reach(ans);
        Set<State> unreachable = new HashSet<>();
        Set<Transition> trans = ans.getTransitions().stream().filter(tran -> !reachable.contains(tran.getFrom())
                || !reachable.contains(tran.getTo())).collect(Collectors.toSet());
        trans.forEach(ans::removeTransition);
        unreachable.addAll(ans.getStates().stream().filter(st -> !reachable.contains(st)).collect(Collectors.toList()));
        unreachable.forEach(ans.getLabelingFunction()::remove);
        unreachable.forEach(ans::removeState);

        return ans;
    }

    private void getTransitionsInH(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs, TransitionSystem ans) {
        for (Transition tran1 : ts1.getTransitions())
            ts2.getTransitions().stream().filter(tran2 -> tran1.getAction().equals(tran2.getAction())
                    && hs.contains(tran1.getAction())).forEach(tran2 -> ans.addTransition(new Transition(new State(tran1.getFrom()
                    .getLabel() + "," + tran2.getFrom().getLabel()),
                    tran1.getAction(), new State(tran1.getTo()
                    .getLabel()
                    + ","
                    + tran2.getTo().getLabel()))));
    }

    private void getTransitionsNotHright(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs, TransitionSystem ans) {
        for (Transition tran : ts2.getTransitions())
            ts1.getStates().stream().filter(st1 -> !hs.contains(tran.getAction())).forEach(st1 -> ans.addTransition(new Transition(new State(st1.getLabel()
                    + "," + tran.getFrom().getLabel()), tran
                    .getAction(), new State(st1.getLabel() + ","
                    + tran.getTo().getLabel()))));
    }

    private void getTransitionsNotHleft(TransitionSystem ts1, TransitionSystem ts2, Set<Action> hs, TransitionSystem ans) {
        for (Transition tran : ts1.getTransitions())
            ts2.getStates().stream().filter(st2 -> !hs.contains(tran.getAction())).forEach(st2 -> ans.addTransition(new Transition(new State(tran.getFrom()
                    .getLabel() + "," + st2.getLabel()), tran
                    .getAction(), new State(tran.getTo().getLabel()
                    + "," + st2.getLabel()))));
    }

    private void getActions(TransitionSystem ts1, TransitionSystem ts2, TransitionSystem ans) {
        ts1.getActions().forEach(ans::addAction);
        ts2.getActions().forEach(ans::addAction);
    }

    public boolean isActionDeterministic(TransitionSystem ts) {
        return ts.getInitialStates().size() <= 1 && isActionDeterministicHelper(ts);
    }

    private boolean isActionDeterministicHelper(TransitionSystem ts) {
        for (Transition t : ts.getTransitions()
                ) {
            if (post(ts, t.getFrom(), t.getAction()).size() > 1)
                return false;
        }
        return true;
    }


    public boolean isAPDeterministic(TransitionSystem ts) {
        return ts.getInitialStates().size() <= 1 && isAPDeterministicHelper(ts);
    }

    private boolean isAPDeterministicHelper(TransitionSystem ts) {
        for (State s : ts.getStates()
                ) {
            Set<State> posts = post(ts, s);
            for (State ps : posts
                    ) {
                if (ts.getLabelingFunction().get(s).equals(ts.getLabelingFunction().get(ps)))
                    return false;
            }
        }
        return true;
    }

    public boolean isExecution(TransitionSystem ts, List<LabeledElement> e) {
        return isInitialExecutionFragment(ts, e) && isMaximalExecutionFragment(ts, e);
    }

    public boolean isExecutionFragment(TransitionSystem ts, List<LabeledElement> e) {
        int i = 0;
        while (i < e.size() - 1) {
            if (i == e.size() - 1 || i + 1 == e.size() - 1)
                return false;
            else if (condEx(ts, e, i))
                i += 2;
            else
                return false;
        }
        return true;
    }

    private boolean condEx(TransitionSystem ts, List<LabeledElement> e, int i) {
        return (e.get(i) instanceof State && e.get(i + 1) instanceof Action && e.get(i + 2) instanceof State) && ts.getTransitions().contains(new Transition((State) e.get(i), (Action) e.get(i + 1), (State) e.get(i + 2)));
    }

    public boolean isInitialExecutionFragment(TransitionSystem ts, List<LabeledElement> e) {
        if (ts.getInitialStates().contains(e.get(0)))
            return true;
        else
            return false;
    }

    public boolean isMaximalExecutionFragment(TransitionSystem ts, List<LabeledElement> e) {
        return isExecutionFragment(ts, e) && isStateTerminal(ts, (State) e.get(e.size() - 1));
    }

    public boolean isStateTerminal(TransitionSystem ts, State s) {
        return post(ts, s).isEmpty();
    }

    public Set<State> post(TransitionSystem ts, Set<State> c) {

        Set<State> ans = new HashSet<>();
        for (State state : c
                ) {
            ans.addAll(ts.getTransitions().stream().filter(t -> t.getFrom().equals(state)).map(Transition::getTo).collect(Collectors.toList()));
        }
        return ans;
    }

    public Set<State> post(TransitionSystem ts, Set<State> c, Action a) {

        Set<State> ans = new HashSet<>();
        for (State state : c
                ) {
            ans.addAll(ts.getTransitions().stream().filter(t -> t.getFrom().equals(state) && t.getAction().equals(a)).map(Transition::getTo).collect(Collectors.toList()));
        }
        return ans;
    }

    public Set<State> post(TransitionSystem ts, State s) {

        Set<State> ans = new HashSet<>();
        ans.addAll(ts.getTransitions().stream().filter(t -> t.getFrom().equals(s)).map(Transition::getTo).collect(Collectors.toList()));
        return ans;
    }

    public Set<State> post(TransitionSystem ts, State s, Action a) {
        Set<State> ans = new HashSet<>();
        ans.addAll(ts.getTransitions().stream().filter(t -> t.getFrom().equals(s) && t.getAction().equals(a)).map(Transition::getTo).collect(Collectors.toList()));
        return ans;
    }

    public Set<State> pre(TransitionSystem ts, Set<State> c) {

        Set<State> ans = new HashSet<>();
        for (State state : c
                ) {
            ans.addAll(ts.getTransitions().stream().filter(t -> t.getTo().equals(state)).map(Transition::getFrom).collect(Collectors.toList()));
        }
        return ans;
    }

    public Set<State> pre(TransitionSystem ts, Set<State> c, Action a) {

        Set<State> ans = new HashSet<>();
        for (State state : c
                ) {
            ans.addAll(ts.getTransitions().stream().filter(t -> t.getTo().equals(state) && t.getAction().equals(a)).map(Transition::getFrom).collect(Collectors.toList()));
        }
        return ans;
    }

    public Set<State> pre(TransitionSystem ts, State s) {

        Set<State> ans = new HashSet<>();
        ans.addAll(ts.getTransitions().stream().filter(t -> t.getTo().equals(s)).map(Transition::getFrom).collect(Collectors.toList()));
        return ans;
    }

    public Set<State> pre(TransitionSystem ts, State s, Action a) {
        Set<State> ans = new HashSet<>();
        ans.addAll(ts.getTransitions().stream().filter(t -> t.getTo().equals(s) && t.getAction().equals(a)).map(Transition::getFrom).collect(Collectors.toList()));
        return ans;
    }

    public Set<State> reach(TransitionSystem ts) {
        Set<State> ans = new HashSet<>();
        Set<State> initials = ts.getInitialStates();
        ans.addAll(initials);
        for (State state : initials
                ) {
            ans.addAll(reachFrom(ts, state, ans));
        }
        return ans;
    }

    private Set<State> reachFrom(TransitionSystem ts, State s, Set<State> ans) {
        Set<State> reachable = post(ts, s);
        reachable.stream().filter(state -> !ans.contains(state)).forEach(state -> {
            ans.add(state);
            ans.addAll(reachFrom(ts, state, ans));
        });
        return ans;
    }

    @Override
    public TransitionSystem transitionSystemFromChannelSystem(ChannelSystem cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransitionSystem transitionSystemFromCircuit(Circuit c) {
        TransitionSystem ans = new TransitionImpl();
        addPropsForTransitionSystemFromCircuit(c, ans);
        addStatesTransitionFromCircuit(c, ans);
        addActionsForTransitionFromCircuit(c, ans);
        addTransitionsToTransitionFromCircuit(c, ans);
        return ans;
    }

    private void addTransitionsToTransitionFromCircuit(Circuit c, TransitionSystem ans) {
        int numberOfInputStates = getPow(c.getNumberOfInputPorts());
        for (int from = 0; from < numberOfInputStates; from++) {
            for (int to = 0; to < numberOfInputStates; to++) {
                for (int j = 0; j < getPow(c.getNumberOfRegiters()); j++) {
                    String format = "%" + (c.getNumberOfInputPorts()) + "s";
                    String fromInputState = getString(from, format);
                    String ToInputStr = getString(to, format);
                    format = "%" + (c.getNumberOfRegiters()) + "s";
                    String regState = getString(j, format);
                    State fromState = new State("[registers=" + regState + ", inputs=" + fromInputState + "]");
                    Action action = new Action(ToInputStr);
                    List<Boolean> FromRegistersList = new ArrayList<>();
                    List<Boolean> FromInputsList = new ArrayList<>();
                    createInputList(fromInputState, FromInputsList);
                    createInputList(regState, FromRegistersList);
                    String toRegStr = "";
                    for (Boolean b : c.updateRegisters(FromRegistersList, FromInputsList)) {
                        if (b)
                            toRegStr += "true,";
                        else
                            toRegStr += "false,";
                    }
                    toRegStr = "[" + toRegStr.substring(0, toRegStr.length() - 1) + "]";
                    State toState = new State("[registers=" + toRegStr + ", inputs=" + ToInputStr + "]");
                    Transition transition = new Transition(fromState, action, toState);

                    ans.addTransition(transition);
                }
            }
        }
    }

    private void createInputList(String fromInputState, List<Boolean> fromInputsList) {
        String[] fromInputStrArray = fromInputState.substring(1, fromInputState.length() - 1).split(",");
        for (int z = 0; z < fromInputStrArray.length; z++) {
            if (fromInputStrArray[z].equals("true")) {
                fromInputsList.add(z, true);
            } else
                fromInputsList.add(z, false);
        }
    }

    private void addActionsForTransitionFromCircuit(Circuit c, TransitionSystem ans) {
        int numberOfInputStates = getPow(c.getNumberOfInputPorts());
        for (int i = 0; i < numberOfInputStates; i++) {
            String format = "%" + (c.getNumberOfInputPorts()) + "s";
            String action = getString(i, format);
            Action newAction = new Action(action);
            ans.addAction(newAction);
        }
    }

    private void addStatesTransitionFromCircuit(Circuit c, TransitionSystem ans) {
        for (int i = 0; i < getPow(c.getNumberOfInputPorts()); i++) {
            for (int j = 0; j < getPow(c.getNumberOfRegiters()); j++) {
                String format = "%" + (c.getNumberOfInputPorts()) + "s";
                String inputState = getString(i, format);
                format = "%" + (c.getNumberOfRegiters()) + "s";
                String regState = getString(j, format);
                State state = new State("[registers=" + regState + ", inputs=" + inputState + "]");
                ans.addState(state);
                if (j == 0)
                    ans.addInitialState(state);
                List<Boolean> registersList = new ArrayList<>();
                List<Boolean> inputsList = new ArrayList<>();
                String[] inputStateArray = inputState.substring(1, inputState.length() - 1).split(",");
                addLabels(ans, state, inputsList, inputStateArray, "x");
                String[] registerStateArray = regState.substring(1, regState.length() - 1).split(",");
                addLabels(ans, state, registersList, registerStateArray, "r");
                List<Boolean> outputList = c.computeOutputs(registersList, inputsList);
                addLabelsForOutputs(ans, state, outputList);
            }
        }
    }

    private void addLabelsForOutputs(TransitionSystem ans, State state, List<Boolean> outputList) {
        for (int z = 0; z < outputList.size(); z++) {
            if (outputList.get(z))
                ans.addLabel(state, "y" + (z + 1));
        }
    }

    private void addLabels(TransitionSystem ans, State state, List<Boolean> inputsList, String[] inputStateArray, String regOrInput) {
        for (int i = 0; i < inputStateArray.length; i++) {
            if (inputStateArray[i].equals("true")) {
                ans.addLabel(state, regOrInput + (i + 1));
                inputsList.add(i, true);
            } else
                inputsList.add(i, false);
        }
    }

    private String getString(int i, String format) {
        String ans = String.format(format, Integer.toBinaryString(i)).replace(' ', '0').replaceAll("0", "false,").replaceAll("1", "true,");
        return "[" + ans.substring(0, ans.length() - 1) + "]";
    }

    private void addPropsForTransitionSystemFromCircuit(Circuit c, TransitionSystem ans) {
        for (int i = 0; i < c.getNumberOfRegiters(); i++) {
            ans.addAtomicProposition("r" + (i + 1));
        }
        for (int i = 0; i < c.getNumberOfInputPorts(); i++) {
            ans.addAtomicProposition("x" + (i + 1));
        }
        for (int i = 0; i < c.getNumberOfOutputPorts(); i++) {
            ans.addAtomicProposition("y" + (i + 1));
        }
    }

    private int getPow(int c) {
        return (int) Math.pow(2, c);
    }

    @Override
    public TransitionSystem transitionSystemFromProgramGraph(ProgramGraph pg, Set<ActionDef> actionDefs,
                                                             Set<ConditionDef> conditionDefs) {
        TransitionSystem ans = new TransitionImpl();
        addInitialStatesForTransSystemFromProgGraph(pg, ans);
        addActionsTransSystemFromProg(pg, ans);
        addAtomicPropsTransFromProg(pg, ans);


        pg.getTransitions().stream().filter(trans -> pg.getInitialLocations().contains(trans.getFrom())).forEach(trans -> {
            Map<String, Object> eval = new HashMap<>();
            for (List<String> ls : pg.getInitalizations()
                    ) {
                for (String s : ls
                        ) {
                    String var = s.split(":")[0];
                    Object val = Integer.parseInt(s.split(":=")[1]);
                    eval.put(var, val);
                }
            }
            bfs(ans, trans.getFrom(), pg, eval, actionDefs, conditionDefs, new HashSet<>());
        });

        return ans;
    }


    private void bfs(TransitionSystem ans, Location source, ProgramGraph pg, Map<String, Object> eval, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs, Set<Location> saw) {
        saw.add(source);
        pg.getTransitions().stream().filter(trans -> trans.getFrom().equals(source) && ConditionDef.evaluate(conditionDefs, eval, trans.getCondition())).forEach(trans -> {
            Map<String, Object> newVals = ActionDef.effect(actionDefs, eval, trans.getAction());
            StringBuilder sb = new StringBuilder();
            for (Map.Entry e : newVals.entrySet()
                    ) {
                sb.append(e.getKey() + "=" + e.getValue() + ",");
            }
            State newState = new State("[location=" + trans.getTo().getLabel() + ", eval={" + sb.toString().substring(0, sb.toString().length() - 1) + "}]");
            ans.addState(newState);

            ans.addLabel(newState, sb.toString().substring(0, sb.toString().length() - 1));
            StringBuilder sb2 = new StringBuilder();
            for (Map.Entry e : eval.entrySet()
                    ) {
                sb2.append(e.getKey() + "=" + e.getValue() + ",");
            }
            State fromState = new State("[location=" + trans.getFrom().getLabel() + ", eval={" + sb2.toString().substring(0, sb2.toString().length() - 1) + "}]");
            if (ans.getStates().contains(fromState))
                ans.addTransition(new Transition(fromState, new Action(trans.getAction()), newState));
            if (!saw.contains(trans.getTo()))
                bfs(ans, trans.getTo(), pg, newVals, actionDefs, conditionDefs, saw);
        });
    }

    private void addActionsTransSystemFromProg(ProgramGraph pg, TransitionSystem ans) {
        for (PGTransition trans : pg.getTransitions()) {
            ans.addAction(new Action(trans.getAction()));
        }
    }

    private void addAtomicPropsTransFromProg(ProgramGraph pg, TransitionSystem ans) {
        for (Location loc : pg.getLocations()
                ) {
            ans.addAtomicProposition(loc.getLabel());
        }
        for (PGTransition trans : pg.getTransitions()) {
            ans.addAtomicProposition(trans.getCondition());
        }
    }

    private void addInitialStatesForTransSystemFromProgGraph(ProgramGraph pg, TransitionSystem ans) {
        for (Location loc : pg.getInitialLocations()
                ) {
            for (List<String> ls : pg.getInitalizations()
                    ) {
                StringBuilder sb = new StringBuilder();
                for (String s : ls
                        ) {
                    sb.append(s.split(":")[0] + s.split(":")[1] + ",");
                }
                State newState = new State("[location=" + loc.getLabel() + ", eval=" + "{" + sb.toString().substring(0, sb.toString().length() - 1) + "}]");
                ans.addState(newState);
                ans.addInitialState(newState);
            }
        }
    }


    @Override
    public ProgramGraph programGraphFromNanoPromela(String filename) throws Exception {
        ProgramGraph ans = new ProgramGraphImpl();
        NanoPromelaParser.StmtContext root = NanoPromelaFileReader.pareseNanoPromelaFile(filename);
        createLocationsPGfromNP(root, ans);
        createTransitionsPGfromNP(root, ans);
        return ans;
    }

    private void createTransitionsPGfromNP(NanoPromelaParser.StmtContext root, ProgramGraph ans) {
        //SKIP case
        if (root.skipstmt() != null) {
            ans.addTransition(new PGTransition(new Location(root.getText()), "", "", new Location("[]")));
        }
        //assignment case
        if (root.assstmt() != null) {
            ans.addTransition(new PGTransition(new Location(root.getText()), "", root.getText(), new Location("[]")));
        }
        //channels case
        if (root.chanwritestmt() != null || root.chanreadstmt() != null) {
            ans.addTransition(new PGTransition(new Location(root.getText()), "", root.getText(), new Location("[]")));
        }
    }

    private void createLocationsPGfromNP(NanoPromelaParser.StmtContext root, ProgramGraph ans) {
        Set<Location> locs = getSub(root);
        locs.forEach(ans::addLocation);
    }

    private Set<Location> getSub(NanoPromelaParser.StmtContext stmt) {
        return sub(stmt, new HashSet<>());
    }

    private Set<Location> sub(NanoPromelaParser.StmtContext stmt, Set<Location> ans) {
        if (isStmtBaseCondition(stmt)) {
            ans.add(new Location(stmt.getText()));
            ans.add(new Location("[]"));
        } else if (stmt.ifstmt() != null) {
            ans.add(new Location(stmt.getText()));
            for (NanoPromelaParser.OptionContext oc : stmt.ifstmt().option()
                    ) {
                ans.addAll(sub(oc.stmt(), ans));
            }
        } else if (stmt.dostmt() != null) {
            ans.add(new Location(stmt.getText()));
            ans.add(new Location("[]"));
            for (NanoPromelaParser.OptionContext oc : stmt.dostmt().option()
                    ) {
                ans.addAll(sub(oc.stmt(), ans));
            }
        } else {
            Set<Location> subOfStmt1 = sub(stmt.stmt().get(0),new HashSet<>());
            Set<Location> subOfStmt2 = sub(stmt.stmt().get(1),new HashSet<>());
            ans.addAll(subOfStmt1.stream().filter(l -> !l.getLabel().equals("[]")).map(l -> new Location(l.getLabel() + ";" + stmt.stmt().get(1).getText())).collect(Collectors.toList()));
            ans.addAll(subOfStmt2.stream().map(l -> new Location(l.getLabel())).collect(Collectors.toList()));
        }
        return ans;
    }

    private boolean isStmtBaseCondition(NanoPromelaParser.StmtContext stmt) {
        return stmt.chanreadstmt() != null || stmt.skipstmt() != null || stmt.chanwritestmt() != null || stmt.assstmt() != null || stmt.atomicstmt() != null;
    }

    @Override
    public ProgramGraph programGraphFromNanoPromelaString(String nanopromela) throws Exception {
        throw new UnsupportedOperationException();
    }
}
