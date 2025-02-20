import java.util.*;

class State {
    int id;
    boolean isFinal;
    Map<Character, List<State>> transitions = new HashMap<>();

    public State(int id) {
        this.id = id;
        this.isFinal = false;
    }

    public void addTransition(char symbol, State next) {
        transitions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(next);
    }
}

class NFA {
    State startState;
    Set<State> states = new HashSet<>();
    State finalState;

    public NFA(State start, State end) {
        this.startState = start;
        this.finalState = end;
        this.finalState.isFinal = true;
    }

    public void addState(State state) {
        states.add(state);
    }

    public void displayTransitions() {
        System.out.println("\nNFA Transition Table:");
        for (State s : states) {
            for (Map.Entry<Character, List<State>> entry : s.transitions.entrySet()) {
                for (State next : entry.getValue()) {
                    System.out.println("State " + s.id + " -- " + entry.getKey() + " --> State " + next.id);
                }
            }
        }
    }

    public boolean validateString(String input) {
        return dfsValidate(startState, input, 0, new HashSet<>());
    }

    private boolean dfsValidate(State currentState, String input, int index, Set<State> visited) {
        if (index == input.length()) {
            return currentState.isFinal || hasEpsilonPathToFinal(currentState, visited);
        }

        char symbol = input.charAt(index);
        visited.add(currentState);

        if (currentState.transitions.containsKey(symbol)) {
            for (State nextState : currentState.transitions.get(symbol)) {
                if (dfsValidate(nextState, input, index + 1, new HashSet<>(visited))) {
                    return true;
                }
            }
        }

        if (currentState.transitions.containsKey('ε')) {
            for (State nextState : currentState.transitions.get('ε')) {
                if (!visited.contains(nextState) && dfsValidate(nextState, input, index, new HashSet<>(visited))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasEpsilonPathToFinal(State state, Set<State> visited) {
        if (state.isFinal) return true;
        visited.add(state);

        if (state.transitions.containsKey('ε')) {
            for (State nextState : state.transitions.get('ε')) {
                if (!visited.contains(nextState) && hasEpsilonPathToFinal(nextState, visited)) {
                    return true;
                }
            }
        }

        return false;
    }
}

class REtoNFA {
    private int stateCount = 0;

    public NFA convert(String regex) {
        Stack<NFA> stack = new Stack<>();

        for (char ch : regex.toCharArray()) {
            if (ch == '|') {
                NFA second = stack.pop();
                NFA first = stack.pop();
                State start = new State(stateCount++);
                State end = new State(stateCount++);

                start.addTransition('ε', first.startState);
                start.addTransition('ε', second.startState);
                first.finalState.addTransition('ε', end);
                second.finalState.addTransition('ε', end);

                NFA newNFA = new NFA(start, end);
                newNFA.addState(start);
                newNFA.addState(end);
                newNFA.states.addAll(first.states);
                newNFA.states.addAll(second.states);
                stack.push(newNFA);
            } else if (ch == '*') {
                NFA last = stack.pop();
                State start = new State(stateCount++);
                State end = new State(stateCount++);

                start.addTransition('ε', last.startState);
                start.addTransition('ε', end);
                last.finalState.addTransition('ε', end);
                last.finalState.addTransition('ε', last.startState);

                NFA newNFA = new NFA(start, end);
                newNFA.addState(start);
                newNFA.addState(end);
                newNFA.states.addAll(last.states);
                stack.push(newNFA);
            } else {
                State start = new State(stateCount++);
                State end = new State(stateCount++);
                start.addTransition(ch, end);

                NFA nfa = new NFA(start, end);
                nfa.addState(start);
                nfa.addState(end);
                stack.push(nfa);
            }
        }

        return stack.pop();
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Regular Expression: ");
        String regex = scanner.nextLine();

        REtoNFA converter = new REtoNFA();
        NFA nfa = converter.convert(regex);

        nfa.displayTransitions();

        System.out.print("\nEnter String to Validate: ");
        String input = scanner.nextLine();

        boolean isValid = nfa.validateString(input);
        System.out.println("String " + input + " is " + (isValid ? "VALID" : "INVALID") + " for the given RE.");
    }
}