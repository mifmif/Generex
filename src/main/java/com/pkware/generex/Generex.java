/*
 * Copyright 2014 y.mifrah
 *

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pkware.generex;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Java utility class that help generating string values that match a given regular expression.It generate all values
 * that are matched by the Regex, a random value, or you can generate only a specific string based on it's
 * lexicographical order .
 *
 * @author y.mifrah
 */
public class Generex implements Iterable<String> {

    /**
     * The predefined character classes supported by {@code Generex}.
     * <p>
     * An immutable map containing as keys the character classes and values the equivalent regular expression syntax.
     *
     * @see #createRegExp(String)
     */
    private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;
    private RegExp regExp;
    private Automaton automaton;
    private List<String> matchedStrings = new ArrayList<String>();
    private Node rootNode;
    private boolean isTransactionNodeBuilt;

    /**
     * Determined possible minimum and maximum length of a regex by traversing the
     * Automaton tree using depth first search.
     */
    private Integer cachedMinLength;
    private Integer cachedMaxLength;

    /**
     * The maximum length a produced string for an infinite regex if {@link #random(int, int)} hasn't been given a max
     * length other than {@link Integer#MAX_VALUE}.
     */
    public static final int DEFAULT_INFINITE_MAX_LENGTH = 50;

    static {
        Map<String, String> characterClasses = new HashMap<String, String>();
        characterClasses.put("\\\\d", "[0-9]");
        characterClasses.put("\\\\D", "[^0-9]");
        characterClasses.put("\\\\s", "[ \t\n\f\r]");
        characterClasses.put("\\\\S", "[^ \t\n\f\r]");
        characterClasses.put("\\\\w", "[a-zA-Z_0-9]");
        characterClasses.put("\\\\W", "[^a-zA-Z_0-9]");
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasses);
    }

    public Generex(String regex) {
        this(regex, new Random());
    }

    public Generex(Automaton automaton) {
        this(automaton, new Random());
    }

    public Generex(String regex, Random random) {
        regex = requote(regex);
        regExp = createRegExp(regex);
        automaton = regExp.toAutomaton();
        this.random = random;
    }

    public Generex(Automaton automaton, Random random) {
        this.automaton = automaton;
        this.random = random;
    }

    /**
     * Creates a {@code RegExp} instance from the given regular expression.
     * <p>
     * Predefined character classes are replaced with equivalent regular expression syntax prior creating the instance.
     *
     * @param regex the regular expression used to build the {@code RegExp} instance
     * @return a {@code RegExp} instance for the given regular expression
     * @throws NullPointerException     if the given regular expression is {@code null}
     * @throws IllegalArgumentException if an error occurred while parsing the given regular expression
     * @throws StackOverflowError       if the regular expression has to many transitions
     * @see #PREDEFINED_CHARACTER_CLASSES
     * @see #isValidPattern(String)
     */
    private static RegExp createRegExp(String regex) {
        String finalRegex = regex;

        // Strip anchors as brics library treats ^ and $ as literal characters.
        if (finalRegex.startsWith("^")) {
            finalRegex = finalRegex.substring(1);
        }
        if (finalRegex.endsWith("$") && !finalRegex.endsWith("\\$")) {
            finalRegex = finalRegex.substring(0, finalRegex.length() - 1);
        }

        // Convert non-capturing groups (?:...) to plain groups (...) as brics library doesn't support them.
        finalRegex = finalRegex.replace("(?:", "(");

        for (Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
            finalRegex = finalRegex.replaceAll(charClass.getKey(), charClass.getValue());
        }
        return new RegExp(finalRegex);
    }

    /**
     * initialize the random instance used with a seed value  to generate a
     * pseudo random suite of strings based on the passed seed and matches the used regular expression
     * instance
     *
     * @param seed
     */
    public void setSeed(long seed) {
        random = new Random(seed);
    }

    /**
     * @param indexOrder ( 1&lt;= indexOrder &lt;=n)
     * @return The matched string by the given pattern in the given it's order in the sorted list of matched String.<br>
     * <code>indexOrder</code> between 1 and <code>n</code> where <code>n</code> is the number of matched String.<br> If
     * indexOrder &gt;= n , return an empty string. if there is an infinite number of String that matches the given Regex,
     * the method throws {@code StackOverflowError}
     */
    public String getMatchedString(int indexOrder) {
        buildRootNode();
        if (indexOrder == 0)
            indexOrder = 1;
        String result = buildStringFromNode(rootNode, indexOrder);
        result = result.substring(1, result.length() - 1);
        return result;
    }

    private String buildStringFromNode(Node node, int indexOrder) {
        String result = "";
        long passedStringNbr = 0;
        long step = node.getNbrMatchedString() / node.getNbrChar();
        for (char usedChar = node.getMinChar(); usedChar <= node.getMaxChar(); ++usedChar) {
            passedStringNbr += step;
            if (passedStringNbr >= indexOrder) {
                passedStringNbr -= step;
                indexOrder -= passedStringNbr;
                result = result.concat("" + usedChar);
                break;
            }
        }
        long passedStringNbrInChildNode = 0;
        if (result.length() == 0)
            passedStringNbrInChildNode = passedStringNbr;
        for (Node childN : node.getNextNodes()) {
            passedStringNbrInChildNode += childN.getNbrMatchedString();
            if (passedStringNbrInChildNode >= indexOrder) {
                passedStringNbrInChildNode -= childN.getNbrMatchedString();
                indexOrder -= passedStringNbrInChildNode;
                result = result.concat(buildStringFromNode(childN, indexOrder));
                break;
            }
        }
        return result;
    }

    /**
     * Tells whether or not the given pattern (or {@code Automaton}) is infinite, that is, generates an infinite number
     * of strings.
     * <p>
     * For example, the pattern "a+" generates an infinite number of strings whether "a{5}" does not.
     *
     * @return {@code true} if the pattern (or {@code Automaton}) generates an infinite number of strings, {@code false}
     * otherwise
     */
    public boolean isInfinite() {
        return !automaton.isFinite();
    }

    /**
     * @return first string in lexicographical order that is matched by the given pattern.
     */
    public String getFirstMatch() {
        buildRootNode();
        Node node = rootNode;
        String result = "";
        while (node.getNextNodes().size() > 0) {
            result = result.concat("" + node.getMinChar());
            node = node.getNextNodes().get(0);
        }
        result = result.substring(1);
        return result;
    }

    /**
     * @return the number of strings that are matched by the given pattern.
     * @throws StackOverflowError if the given pattern generates a large, possibly infinite, number of strings.
     */
    public long matchedStringsSize() {
        buildRootNode();
        return rootNode.getNbrMatchedString();
    }

    /**
     * Prepare the rootNode and it's child nodes so that we can get matchedString by index
     */
    private void buildRootNode() {
        if (isTransactionNodeBuilt)
            return;
        isTransactionNodeBuilt = true;
        rootNode = new Node();
        rootNode.setNbrChar(1);
        List<Node> nextNodes = prepareTransactionNodes(automaton.getInitialState());
        rootNode.setNextNodes(nextNodes);
        rootNode.updateNbrMatchedString();
    }

    private int matchedStringCounter = 0;

    private void generate(String strMatch, State state, int limit) {
        if (matchedStringCounter == limit)
            return;
        ++matchedStringCounter;
        List<Transition> transitions = state.getSortedTransitions(true);
        if (transitions.size() == 0) {
            matchedStrings.add(strMatch);
            return;
        }
        if (state.isAccept()) {
            matchedStrings.add(strMatch);
        }
        for (Transition transition : transitions) {
            for (char c = transition.getMin(); c <= transition.getMax(); ++c) {
                generate(strMatch + c, transition.getDest(), limit);
            }
        }
    }

    /**
     * Build list of nodes that present possible transactions from the <code>state</code>.
     *
     * @param state
     * @return
     */
    private List<Node> prepareTransactionNodes(State state) {

        List<Node> transactionNodes = new ArrayList<Node>();
        if (preparedTransactionNode == Integer.MAX_VALUE / 2)
            return transactionNodes;
        ++preparedTransactionNode;

        if (state.isAccept()) {
            Node acceptedNode = new Node();
            acceptedNode.setNbrChar(1);
            transactionNodes.add(acceptedNode);
        }
        List<Transition> transitions = state.getSortedTransitions(true);
        for (Transition transition : transitions) {
            Node trsNode = new Node();
            int nbrChar = transition.getMax() - transition.getMin() + 1;
            trsNode.setNbrChar(nbrChar);
            trsNode.setMaxChar(transition.getMax());
            trsNode.setMinChar(transition.getMin());
            List<Node> nextNodes = prepareTransactionNodes(transition.getDest());
            trsNode.setNextNodes(nextNodes);
            transactionNodes.add(trsNode);
        }
        return transactionNodes;
    }

    private int preparedTransactionNode;
    private Random random;

    /**
     * Generate all Strings that matches the given Regex.
     *
     * @return
     */
    public List<String> getAllMatchedStrings() {
        matchedStrings = new ArrayList<String>();
        generate("", automaton.getInitialState(), Integer.MAX_VALUE);
        return matchedStrings;

    }

    /**
     * Generate subList with a size of <code>limit</code> of Strings that matches the given Regex. the Strings are
     * ordered in lexicographical order.
     *
     * @param limit
     * @return
     */
    public List<String> getMatchedStrings(int limit) {
        matchedStrings = new ArrayList<String>();
        generate("", automaton.getInitialState(), limit);
        return matchedStrings;

    }

    /**
     * See {@link #random(int, int)}
     */
    public String random() {
        return random(1);
    }

    /**
     * See {@link #random(int, int)}
     */
    public String random(int minLength) {
        calculateLengthBounds();
        int actualMaxLength = isInfinite() ? DEFAULT_INFINITE_MAX_LENGTH : cachedMaxLength;
        return random(minLength, actualMaxLength);
    }

    /**
     * Attempts to generate a string that would match the regex stored by this {@link Generex} instance that has a
     * length in the range {@code [minLength, maxLength]}.
     * <p></p>
     * If the provided regex can't generate a matching string less than or equal to {@code maxLength}, then a random
     * string that matches the start of the regex will be generated with a length of {@code maxLength}. If the provided
     * regex can't generate a matching string greater than or equal to {@code minLength}, then a string with the maximum
     * length possible is returned.
     * <p></p>
     * When a string is generated that may be an infinite length, it is attempted to be randomly generated in a way
     * where repeated generations will produce a uniform distribution of lengths between `minLength` and `maxLength`. In
     * practice, the accuracy of this depends on the complexity of the regex.
     *
     * @param minLength Minimum wanted length of the generated string. The generated string may be smaller if the given
     *                  regex can't produce a string of the wanted length. Assumed to be less than or equal to {@code maxLength}.
     * @param maxLength Maximum wanted length of the generated string. The generated string <b>will be trimmed</b> to
     *                  this length if the regex can't generate a string less than this value. Assumed to be greater than or equal to
     *                  {@code minLength}.
     * @return A string between {@code minLength} and {@code maxLength} if the regex provided can match a string in the
     * given range. Otherwise, see the {@code minLength} and {@code maxLength} docs.
     */
    public String random(int minLength, int maxLength) {
        calculateLengthBounds();

        // Calculate actual valid range by comparing the regex and the user defined bounds
        int actualMinLength = Math.max(minLength, cachedMinLength);
        int actualMaxLength = Math.min(maxLength, isInfinite() ? maxLength : cachedMaxLength);

        // Pre-select target length uniformly from valid range
        int targetLength;
        if (actualMinLength > actualMaxLength) {
            targetLength = actualMaxLength;
        } else {
            targetLength = actualMinLength + random.nextInt(actualMaxLength - actualMinLength + 1);
        }

        String result = prepareRandom("", automaton.getInitialState(), minLength, maxLength, targetLength);
        // Substring in case a length of 'maxLength + 1' is returned, which is possible if a smaller string can't be produced.
        return result.substring(0, Math.min(maxLength, result.length()));
    }

    /**
     * Recursive function used to generate a regex as defined by {@link Generex#random(int, int)}.
     *
     * @param currentMatch A string built from the accumulation of previous transitions.
     * @param state        Current state of the regex.
     * @param minLength    Minimum wanted length of the produced string.
     * @param maxLength    Maximum wanted length of produced string.
     * @return A string built from the accumulation of previous transitions.
     */
    /**
     * Maximum number of recursive calls allowed during random generation for infinite regexes.
     * Prevents exponential backtracking when the automaton has cycles with many transitions.
     */
    private static final int MAX_RANDOM_ATTEMPTS = 1000;

    private String prepareRandom(String currentMatch, State state, int minLength, int maxLength, int targetLength) {
        return prepareRandom(currentMatch, state, minLength, maxLength, targetLength, isInfinite() ? new int[]{0} : null);
    }

    private String prepareRandom(String currentMatch, State state, int minLength, int maxLength, int targetLength, int[] attempts) {

        // Return a string of length 'maxLength + 1' to indicate a dead branch.
        if (currentMatch.length() > maxLength || state.getTransitions().isEmpty()) return currentMatch;

        // For infinite regexes, the automaton has cycles that can cause exponential recursion.
        // This budget limit caps total recursive iterations to prevent hanging.
        if (attempts != null && attempts[0] >= MAX_RANDOM_ATTEMPTS) return currentMatch;

        String returnValue = null;

        if (state.isAccept()) {
            // Set the current match to the value to return, just in case this would happen to be the cloest match to
            // the target length.
            returnValue = currentMatch;

            if (currentMatch.length() == targetLength) return currentMatch;
        }

        // Make a copy so the original set is never modified.
        Set<Transition> possibleTransitions = new HashSet<>(state.getTransitions());
        int totalWeightedTransitions = calculateTotalWeightedTransitions(possibleTransitions);

        // Will never start as empty due to the initial if statement in the function.
        while (!possibleTransitions.isEmpty()) {
            if (attempts != null) {
                attempts[0]++;
                if (attempts[0] >= MAX_RANDOM_ATTEMPTS) break;
            }

            Transition randomTransition = pickRandomWeightedTransition(possibleTransitions, totalWeightedTransitions);
            int subTransitions = getWeightedTransitions(randomTransition);
            totalWeightedTransitions -= subTransitions;
            possibleTransitions.remove(randomTransition);

            char randomChar = (char) (random.nextInt(subTransitions) + randomTransition.getMin());
            String result = prepareRandom(currentMatch + randomChar, randomTransition.getDest(), minLength, maxLength, targetLength, attempts);

            // Greedily return the first valid result found that is of the wanted length..
            if (result.length() == targetLength) return result;

            returnValue = getBestMatch(result, returnValue, minLength, maxLength, targetLength);
        }

        // For infinite regexes, if budget was exhausted before reaching an accept state, return currentMatch
        // as a fallback instead of null.
        return returnValue != null ? returnValue : currentMatch;
    }

    /**
     * Determines if the new generation is better than the current generation.
     * <p></p>
     * The new generation is better if it is within the bounds and is closer to the target length than the current
     * generation. Otherwise, the current generation is better.
     *
     * @param newMatch the new generation to compare against the current generation.
     * @param currentMatch the current generation.
     * @param min minimum length of the generated string.
     * @param max maximum length of the generated string.
     * @param target the target length of the generated string.
     * @return the best match between the new generation and the current generation.
     */
    private String getBestMatch(String newMatch, String currentMatch, int min, int max, int target) {

        if (currentMatch == null) return newMatch;
        if (newMatch.length() > max && currentMatch.length() > min) return currentMatch;

        boolean newInRange = newMatch.length() >= min;
        boolean currentInRange = currentMatch.length() >= min && currentMatch.length() <= max;

        if (newInRange && !currentInRange) return newMatch;
        if (currentInRange && !newInRange) return currentMatch;

        int currentTargetDistance = Math.abs(currentMatch.length() - target);
        int newTargetDistance = Math.abs(newMatch.length() - target);

        if (newTargetDistance < currentTargetDistance) return newMatch;
        return currentMatch;
    }

    /**
     * Calculate the possible bounds of the generated string by traversing the regex
     */
    private void calculateLengthBounds() {
        if (cachedMinLength != null) return;

        int[] bounds = dfsLengthBounds(automaton.getInitialState(), new HashSet<>());
        cachedMinLength = bounds[0];
        cachedMaxLength = bounds[1];
    }

    /**
     * Uses a depth first search to calculate the minimum and maximum length of the regex by
     * traversing through the automaton tree.
     * <br>
     * We can use DFS because the automaton is finite (does not contain infinite loops) and
     * we need to visit every state regardless to determine the longest length.
     *
     * @param state the current state of the automaton.
     * @param visited the set of visited states.
     * @return an int array containing the minimum and maximum length of the regex.
     */
    private int[] dfsLengthBounds(State state, Set<State> visited) {
        if (visited.contains(state)) return new int[]{Integer.MAX_VALUE, 0};

        int minLength = state.isAccept() ? 0 : Integer.MAX_VALUE;
        int maxLength = 0;

        visited.add(state);

        for (Transition transition : state.getTransitions()) {
            int[] bounds = dfsLengthBounds(transition.getDest(), visited);
            if (bounds[0] != Integer.MAX_VALUE) {
                minLength = Math.min(minLength, bounds[0] + 1);
            }
            maxLength = Math.max(maxLength, bounds[1] + 1);
        }

        visited.remove(state);
        return new int[]{minLength, maxLength};
    }

    /**
     * Returns a {@link Transition} from the given collection randomly based on the total number of characters that
     * {@link Transition} can produce.
     *
     * @param transitions              Collection of transitions to choose from.
     * @param totalWeightedTransitions The sum of the total number of characters each transition could produce.
     * @throws IllegalArgumentException When {@code totalWeightedTransitions} is not equal to the sum of all weighted
     *                                  transitions in {@code transitions}.
     */
    private Transition pickRandomWeightedTransition(Collection<Transition> transitions, int totalWeightedTransitions) {

        int value = random.nextInt(totalWeightedTransitions) + 1;
        for (Transition transition : transitions) {

            value -= getWeightedTransitions(transition);
            if (value <= 0) return transition;
        }

        throw new IllegalArgumentException(
                "totalWeightedTransitions was greater than the total number of weighted transitions in the supplied collection."
        );
    }

    /**
     * Calculates the sum of all the {@link #getWeightedTransitions(Transition) weighted transitions} from the given collection.
     */
    private static int calculateTotalWeightedTransitions(Collection<Transition> transitions) {

        int totalWeight = 0;
        for (Transition transition : transitions) totalWeight += getWeightedTransitions(transition);

        return totalWeight;
    }

    /**
     * Calculates the number of different characters a {@link Transition} could produce.
     */
    private static int getWeightedTransitions(Transition transition) {
        return transition.getMax() - transition.getMin() + 1;
    }

    public Iterator<String> iterator() {
        return new GenerexIterator(automaton.getInitialState());
    }

    /**
     * Tells whether or not the given regular expression is a valid pattern (for {@code Generex}).
     *
     * @param regex the regular expression that will be validated
     * @return {@code true} if the regular expression is valid, {@code false} otherwise
     * @throws NullPointerException if the given regular expression is {@code null}
     */
    public static boolean isValidPattern(String regex) {
        try {
            createRegExp(regex);
            return true;
        } catch (IllegalArgumentException ignore) { // NOPMD - Not valid.
        } catch (StackOverflowError ignore) { // NOPMD - Possibly valid but stack not big enough to handle it.
        }
        return false;
    }

    /**
     * Requote a regular expression by escaping some parts of it from generation without need to escape each special
     * character one by one. <br> this is done by setting the part to be interpreted as normal characters (thus, quote
     * all meta-characters) between \Q and \E , ex : <br> <code> minion_\d{3}\Q@gru.evil\E </code> <br> will be
     * transformed to : <br> <code> minion_\d{3}\@gru\.evil </code>
     *
     * @param regex
     * @return
     */
    private static String requote(String regex) {
        final Pattern patternRequoted = Pattern.compile("(?s)(?<!\\\\)(?:\\\\{2})*+\\\\Q(.*?)\\\\E");
        // http://stackoverflow.com/questions/399078/what-special-characters-must-be-escaped-in-regular-expressions
        // adding "@" prevents StackOverflowError inside generex: https://github.com/mifmif/Generex/issues/21
        final Pattern patternSpecial = Pattern.compile("[.^$*+?(){|\\[\\\\@]");

        StringBuilder sb = new StringBuilder();
        Matcher matcher = patternRequoted.matcher(regex);
        int index = 0;

        while (matcher.find()) {
            sb.append(regex, index, matcher.start(1) - 2); // Don't include \Q
            sb.append(patternSpecial.matcher(matcher.group(1)).replaceAll("\\\\$0"));
            index = matcher.end(1) + 2; // Don't include \E
        }

        sb.append(regex.substring(index));
        return sb.toString();
    }
}
