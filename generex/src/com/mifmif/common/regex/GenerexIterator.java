package com.mifmif.common.regex;

import java.util.Stack;

import com.mifmif.common.regex.util.Iterator;

import dk.brics.automaton.State;

/**
 * An implementation of {@code Iterator} class that iterate over the list of
 * Strings that matches a given Regex.
 * 
 * @author y.mifrah
 *
 */
public class GenerexIterator implements Iterator {
	Stack<TransitionLevel> transitionsPath = new Stack<TransitionLevel>();
	String currentValue = "";

	public GenerexIterator(State initialState) {
		TransitionLevel initialLevel = new TransitionLevel(initialState);
		transitionsPath.add(initialLevel);

	}

	@Override
	public boolean hasNext() {
		return !transitionsPath.isEmpty();
	}

	@Override
	public String next() {
		while (!transitionsPath.isEmpty()) {
			TransitionLevel currentLevel = transitionsPath.peek();
			State state = currentLevel.getState();
			if (!state.isAccept()) {
				addNextTransitionLevel(currentLevel);
				continue;
			}
			currentValue = "";
			for (int i = 0; i < transitionsPath.size() - 1; ++i) {
				TransitionLevel transitionLevel = transitionsPath.get(i);
				currentValue += transitionLevel.getCurrentChar();
			}
			jumpToNextPath();
			break;
		}
		return currentValue;
	}

	private void jumpToNextPath() {
		while (!transitionsPath.isEmpty()) {
			TransitionLevel currentLevel = transitionsPath.peek();
			if (currentLevel.hasNextTransitionLevel()) {
				TransitionLevel nextTransitionLevel = currentLevel.nextTransitionLevel();
				transitionsPath.push(nextTransitionLevel);
				break;
			}
			if (currentLevel.hasCharacterTransition()) {
				char currChar = currentLevel.getCurrentChar();
				if (currChar < currentLevel.getMaxCharInCurrentTransition()) {
					++currChar;
					currentLevel.setCurrentChar(currChar);
					break;
				}
			}
			if (currentLevel.jumpToNextTransition()) {
				break;
			} else {
				transitionsPath.pop();
			}

		}
	}

	private void addNextTransitionLevel(TransitionLevel currentLevel) {
		State nextState = currentLevel.getCurrentTransition().getDest();
		TransitionLevel nextLevel = new TransitionLevel(nextState);
		transitionsPath.add(nextLevel);

	}
}