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

package com.mifmif.common.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mifmif.common.regex.util.Iterable;
import com.mifmif.common.regex.util.Iterator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * A Java utility class that help generating string values that match a given regular expression.It generate all values
 * that are matched by the Regex, a random value, or you can generate only a specific string based on it's
 * lexicographical order .
 *
 * @author y.mifrah
 */
public class Generex implements Iterable {

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
     * @throws NullPointerException if the given regular expression is {@code null}
     * @throws IllegalArgumentException if an error occurred while parsing the given regular expression
     * @throws StackOverflowError if the regular expression has to many transitions
     * @see #PREDEFINED_CHARACTER_CLASSES
     * @see #isValidPattern(String)
     */
    private static RegExp createRegExp(String regex) {
        String finalRegex = regex;
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
     * @param indexOrder ( 1<= indexOrder <=n)
     * @return The matched string by the given pattern in the given it's order in the sorted list of matched String.<br>
     * <code>indexOrder</code> between 1 and <code>n</code> where <code>n</code> is the number of matched String.<br> If
     * indexOrder >= n , return an empty string. if there is an infinite number of String that matches the given Regex,
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
        for (char usedChar = node.getMinChar(); usedChar <= node.getMaxChar(); ++ usedChar) {
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
        return ! automaton.isFinite();
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
        ++ matchedStringCounter;
        List<Transition> transitions = state.getSortedTransitions(true);
        if (transitions.size() == 0) {
            matchedStrings.add(strMatch);
            return;
        }
        if (state.isAccept()) {
            matchedStrings.add(strMatch);
        }
        for (Transition transition : transitions) {
            for (char c = transition.getMin(); c <= transition.getMax(); ++ c) {
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
        ++ preparedTransactionNode;

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
     * Generate and return a random String that match the pattern used in this Generex.
     *
     * @return
     */
    public String random() {
        return prepareRandom("", automaton.getInitialState(), 1, Integer.MAX_VALUE);
    }

    /**
     * Generate and return a random String that match the pattern used in this Generex, and the string has a length >=
     * <code>minLength</code>
     *
     * @param minLength
     * @return
     */
    public String random(int minLength) {
        return prepareRandom("", automaton.getInitialState(), minLength, Integer.MAX_VALUE);
    }

    /**
     * Generate and return a random String that match the pattern used in this Generex, and the string has a length >=
     * <code>minLength</code> and <= <code>maxLength</code>
     *
     * @param minLength
     * @param maxLength
     * @return
     */
    public String random(int minLength, int maxLength) {
        return prepareRandom("", automaton.getInitialState(), minLength, maxLength);
    }

    private String prepareRandom(String strMatch, State state, int minLength, int maxLength) {
        List<Transition> transitions = state.getSortedTransitions(false);
        Set<Integer> selectedTransitions = new HashSet<Integer>();
        String result = strMatch;

        for (int resultLength = -1;
            transitions.size() > selectedTransitions.size()
                && (resultLength < minLength || resultLength > maxLength);
            resultLength = result.length()) {

            if (randomPrepared(strMatch, state, minLength, maxLength, transitions)) {
                return strMatch;
            }

            int nextInt = random.nextInt(transitions.size());
            if (!selectedTransitions.contains(nextInt)) {
                selectedTransitions.add(nextInt);

                Transition randomTransition = transitions.get(nextInt);
                int diff = randomTransition.getMax() - randomTransition.getMin() + 1;
                int randomOffset = diff > 0 ? random.nextInt(diff) : diff;
                char randomChar = (char) (randomOffset + randomTransition.getMin());
                result = prepareRandom(strMatch + randomChar, randomTransition.getDest(), minLength, maxLength);
            }
        }

        return result;
    }

    private boolean randomPrepared(
        String strMatch,
        State state,
        int minLength,
        int maxLength,
        List<Transition> transitions) {

        if (state.isAccept()) {
            if (strMatch.length() == maxLength) {
                return true;
            }
            if (random.nextInt() > 0.3 * Integer.MAX_VALUE && strMatch.length() >= minLength) {
                return true;
            }
        }

        return transitions.size() == 0;
    }

    public Iterator iterator() {
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
        final Pattern patternRequoted = Pattern.compile("\\\\Q(.*?)\\\\E");
        // http://stackoverflow.com/questions/399078/what-special-characters-must-be-escaped-in-regular-expressions
        // adding "@" prevents StackOverflowError inside generex: https://github.com/mifmif/Generex/issues/21
        final Pattern patternSpecial = Pattern.compile("[.^$*+?(){|\\[\\\\@]");
        StringBuilder sb = new StringBuilder(regex);
        Matcher matcher = patternRequoted.matcher(sb);
        while (matcher.find()) {
            sb.replace(matcher.start(), matcher.end(), patternSpecial.matcher(matcher.group(1)).replaceAll("\\\\$0"));
            //matcher.reset();
        }
        return sb.toString();
    }

}
