/**
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mifmif.common.regex.util.Iterable;
import com.mifmif.common.regex.util.Iterator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * A Java utility class that help generating string values that match a given
 * regular expression.It generate all values that are matched by the Regex, a
 * random value, or you can generate only a specific string based on it's
 * lexicographical order .
 * 
 * @author y.mifrah
 *
 */
public class Generex implements Iterable {

    private Map<String, String> predefinedCharacterClasses = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("\\\\d","[0-9]");
            put("\\\\D","[^0-9]");
            put("\\\\s","[ \t\n\f\r]");
            put("\\\\S","[^ \t\n\f\r]");
            put("\\\\w","[a-zA-Z_0-9]");
            put("\\\\W","[^a-zA-Z_0-9]");
        }
    };
    
	public Generex(String regex) {
	    for (String key : predefinedCharacterClasses.keySet()) {
	        regex = regex.replaceAll(key, predefinedCharacterClasses.get(key));
	    }
		regExp = new RegExp(regex);
		automaton = regExp.toAutomaton();
	}

	public Generex(Automaton automaton) {
		this.automaton = automaton;
	}

	private RegExp regExp;
	private Automaton automaton;
	private List<String> matchedStrings = new ArrayList<String>();
	private Node rootNode;
	private boolean isTransactionNodeBuilt;

	/**
	 * @param indexOrder
	 *            ( 1<= indexOrder <=n)
	 * @return The matched string by the given pattern in the given it's order
	 *         in the sorted list of matched String.<br>
	 *         <code>indexOrder</code> between 1 and <code>n</code> where
	 *         <code>n</code> is the number of matched String.<br>
	 *         If indexOrder >= n , return an empty string. if there is an
	 *         infinite number of String that matches the given Regex, the
	 *         method throws {@code StackOverflowError}
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
	 * @return first string in lexicographical order that is matched by the
	 *         given pattern.
	 */
	public String getFirstMatch() {
		buildRootNode();
		Node node = rootNode;
		String result = "";
		while (node.getNextNodes().size() > 0) {
			result = result.concat("" + node.getMinChar());
			node = node.getNextNodes().get(0);
		}
		return result;
	}

	/**
	 * @return the number of strings that are matched by the given pattern.
	 */
	public long matchedStringsSize() {
		return rootNode.getNbrMatchedString();
	}

	/**
	 * Prepare the rootNode and it's child nodes so that we can get
	 * matchedString by index
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
	 * Build list of nodes that present possible transactions from the
	 * <code>state</code>.
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
	 * Generate subList with a size of <code>limit</code> of Strings that
	 * matches the given Regex. the Strings are ordered in lexicographical
	 * order.
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
	 * Generate and return a random String that match the pattern used in this
	 * Generex.
	 * 
	 * @return
	 */
	public String random() {
		return prepareRandom("", automaton.getInitialState(), 1, Integer.MAX_VALUE);
	}

	/**
	 * Generate and return a random String that match the pattern used in this
	 * Generex, and the string has a length >= <code>minLength</code>
	 * 
	 * @param minLength
	 * @return
	 */
	public String random(int minLength) {
		return prepareRandom("", automaton.getInitialState(), minLength, Integer.MAX_VALUE);
	}

	/**
	 * Generate and return a random String that match the pattern used in this
	 * Generex, and the string has a length >= <code>minLength</code> and <=
	 * <code>maxLength</code>
	 * 
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

		if (state.isAccept()) {
			if (strMatch.length() == maxLength) {
				return strMatch;
			}
			if (Math.random() > 0.7 && strMatch.length() >= minLength) {
				return strMatch;
			}
		}
		if (transitions.size() == 0) {
			return strMatch;
		}
		Random random = new Random();
		Transition randomTransition = transitions.get(random.nextInt(transitions.size()));
		int diff = randomTransition.getMax() - randomTransition.getMin();
		int randomOffset = diff;
		if( diff > 0 ) {
		    randomOffset = (int) (random.nextInt(diff));
		}
		char randomChar = (char) (randomOffset + randomTransition.getMin());
		return prepareRandom(strMatch + randomChar, randomTransition.getDest(), minLength, maxLength);

	}

	public Iterator iterator() {
		return new GenerexIterator(automaton.getInitialState());

	}

}
