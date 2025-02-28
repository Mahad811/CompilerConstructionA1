import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;

class DFA {
    private final Map<Integer, Map<Character, Integer>> transitionTable;
    private final Set<Integer> acceptingStates;
    private int currentState;
    private final int defaultState = -1; // Default transition state


    public DFA() {
        transitionTable = new HashMap<>();
        acceptingStates = new HashSet<>(Arrays.asList(2, 3, 4, 5, 6, 7, 10, 13, 15, 17, 18, 24)); // Only save tokens ending in these states
        initializeDFA();
    }
    // Add a transition for a specific character
    public void addTransition(int fromState, char input, int toState) {
        transitionTable.computeIfAbsent(fromState, k -> new HashMap<>()).put(input, toState);
    }

    // Get the next state, with a default fallback
    public int getNextState(int currentState, char input) {
        return transitionTable.getOrDefault(currentState, new HashMap<>())
                .getOrDefault(input, defaultState); // Use defaultState if character is not explicitly mapped
    }

    private void initializeDFA() {
        // State 0: Start state
        for (char c = 'i'; c <= 'z'; c++) addTransition(0,c,1);; // Identifiers
        for (char c = '0'; c <= '9'; c++) addTransition(0,c,3);; // Numbers
        addTransition(0,'+',5);
        addTransition(0,'-',5);
        addTransition(0,'*',5);
        addTransition(0,'/',7);
        addTransition(0,'%',5);
        addTransition(0,'^',5);
        addTransition(0,'{',6);
        addTransition(0,'}',6);
        addTransition(0,' ',0);
        addTransition(0,'\n',0);
        addTransition(0,';',0);

        // State 1: Identifier
        for (char c = 'a'; c <= 'z'; c++) addTransition(1,c,1);;
        addTransition(1,' ',2);
        addTransition(1,'\n',2);
        addTransition(1,';',2);
        addTransition(2,';',2);

        // State 3: Integer
        for (char c = '0'; c <= '9'; c++) addTransition(3,c,3);;
        addTransition(3,'.',4);

        // State 4: Decimal
        for (char c = '0'; c <= '9'; c++) addTransition(4,c,19);
        addTransition(19,' ',24);
        addTransition(19,';',24);
        addTransition(19,'+',24);
        addTransition(19,'-',24);
        addTransition(19,'*',24);
        addTransition(19,'/',24);
        addTransition(19,'%',24);


        for (char c = '0'; c <= '9'; c++) addTransition(19,c,20);
        addTransition(20,' ',24);
        addTransition(20,';',24);
        addTransition(20,'+',24);
        addTransition(20,'-',24);
        addTransition(20,'*',24);
        addTransition(21,'/',24);
        addTransition(22,'%',24);
        for (char c = '0'; c <= '9'; c++) addTransition(20,c,21);
        addTransition(21,' ',24);
        addTransition(21,';',24);
        addTransition(21,'+',24);
        addTransition(21,'-',24);
        addTransition(21,'*',24);
        addTransition(21,'/',24);
        addTransition(21,'%',24);
        for (char c = '0'; c <= '9'; c++) addTransition(21,c,22);
        addTransition(22,' ',24);
        addTransition(22,';',24);
        addTransition(22,'+',24);
        addTransition(22,'-',24);
        addTransition(22,'*',24);
        addTransition(22,'/',24);
        addTransition(22,'%',24);
        for (char c = '0'; c <= '9'; c++) addTransition(22,c,23);
        addTransition(23,' ',24);
        addTransition(23,';',24);
        addTransition(23,'+',24);
        addTransition(23,'-',24);
        addTransition(23,'*',24);
        addTransition(23,'/',24);
        addTransition(23,'%',24);

        for (char c = '0'; c <= '9'; c++) addTransition(24,c,24);

        // State 5: Arithmetic Operators

        // State 7: Possible Comment Start
        addTransition(7,'*',8);

        // State 8: Inside Multi-line Comment
        for (char c = 'a'; c <= 'z'; c++) addTransition(8,c,8);;
        addTransition(8,'*',9);
        // State 9: Potential End of Multi-line Comment
        addTransition(9,'/',10);

        addTransition(0,'\'',11);
        for (char c = 'a'; c <= 'z'; c++) addTransition(11,c,12);
        addTransition(12,'\'',13);

        addTransition(0,'<',14);
        addTransition(14,'<',15);

        addTransition(0,'>',16);
        addTransition(16,'>',17);

        addTransition(0,'=',18);
    }

    public List<String> tokenize(String code) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        currentState = 0;
        int lastAcceptingState = -1;
        int lastAcceptingIndex = -1;
        int linecount = 0;
        Boolean error = false;


        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (transitionTable.containsKey(currentState) && transitionTable.get(currentState).containsKey(c))
                {
                currentState = transitionTable.get(currentState).get(c);
                token.append(c);
                if (acceptingStates.contains(currentState)) {
                    lastAcceptingState = currentState;
                    lastAcceptingIndex = i;
                }
            } else {
                if (lastAcceptingState != -1) {
                    tokens.add(token.substring(0, lastAcceptingIndex - (i - token.length()) + 1));
                    i = lastAcceptingIndex;
                }

                token.setLength(0);
                currentState = 0;
                lastAcceptingState = -1;
                lastAcceptingIndex = -1;

            }
        }
        if (lastAcceptingState != -1) {
            tokens.add(token.toString());
        }
        return tokens;
    }

    public void printTokens(List<String> tokens) {
        System.out.println("Tokens:");
        for (String token : tokens) {
            System.out.println(token);
        }
    }


}
class SymbolTable {
    static String[][] tokens = {
            {"DATATYPE", "int"},
            {"DATATYPE", "bool"},
            {"DATATYPE", "dec"},
            {"DATATYPE", "char"},
            {"KEYWORD", "return"},
            {"KEYWORD", "global"},
            {"KEYWORD", "TRUE"},
            {"KEYWORD", "FALSE"},
            {"OPERATOR", "+"},
            {"OPERATOR", "/"},
            {"OPERATOR", "*"},
            {"OPERATOR", "-"},
            {"OPERATOR", "%"},
            {"INPUT", "cin"},
            {"OUTPUT", "cout"},
            {"SEMICOLON", ";"},
            {"IN_OP", ">>"},
            {"OUT_OP", "<<"},
            {"ASS_OP", "="},
            {"CURLY_OPEN", "{"},
            {"CURLY_CLOSE", "}"}
    };
    public static boolean isValidString(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        char firstChar = input.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            for (char c : input.toCharArray()) {
                if (!Character.isLowerCase(c)) {
                    return false; // Invalid if it contains anything other than lowercase letters
                }
            }
            return true;
        }
        if (Character.isDigit(firstChar)) {
            boolean hasDot = false;
            int dotIndex = -1;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);

                if (Character.isDigit(c)) {
                    continue;
                } else if (c == '.') {
                    if (hasDot) {
                        return false; // More than one '.'
                    }
                    hasDot = true;
                    dotIndex = i;
                } else {
                    return false; // Invalid character found
                }
            }
            if (hasDot) {
                int digitsAfterDot = input.length() - dotIndex - 1;
                if (digitsAfterDot > 5) {
                    return false;
                }
            }
            return true;
        }
        return false; // If it starts with anything else, it's invalid
    }

    public static String[][] searchTokens(List<String> inputTokens) {
        List<String[]> result = new ArrayList<>();

        for (String token : inputTokens) {
            boolean found = false;

            for (String[] entry : tokens) {
                if (entry[1].equals(token)) {
                    result.add(new String[]{entry[0], entry[1]});
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (isValidString(token))
                    result.add(new String[]{"IDENTIFIER", token});
                else{
                    System.out.println("ERROR TOKEN INVALID" + token);
                    System.exit(0);}

            }
        }

        // Convert List to 2D array
        return result.toArray(new String[0][0]);
    }
}

class Main{
    public static List<String> removeDuplicates(List<String> tokens) {
        return new ArrayList<>(new LinkedHashSet<>(tokens));
    }

    public static void main(String[] args) {
        DFA dfa = new DFA();
        String code;
        try {
            code = new String(Files.readAllBytes(Paths.get("C:\\Users\\Hasan\\IdeaProjects\\CC_Ass1\\src\\code.txt")));
            List<String> tokens = dfa.tokenize(code);
            tokens.replaceAll(token -> token.replaceAll("\\s+", ""));
            tokens.replaceAll(token -> token.replaceAll(";+", ""));
            //dfa.printTokens(tokens);
            String[][] matchedTokens = SymbolTable.searchTokens(tokens);

            // Print the result
            for (String[] tokenPair : matchedTokens) {
                System.out.println(tokenPair[0] + " -> " + tokenPair[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
