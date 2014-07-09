package com.mifmif.common.regex;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * This class present a level where we will choose the next path to follow to
 * build String that matches the Regex.
 * 
 * @author y.mifrah
 *
 */
class TransitionLevel {
	private State state;
	private int transitionIndex;
	private boolean isNextTransitionLevelPassed = false;
	private char currentChar;

	public TransitionLevel(State state) {
		this.state = state;
		if (getState().getSortedTransitions(true).size() > 0) {
			currentChar = getCurrentTransition().getMin();
		}
	}

	public boolean hasCharacterTransition() {

		return state.getTransitions().size() > 0;
	}

	private boolean hasMoreTransitions() {
		return transitionIndex < state.getTransitions().size() - 1;
	}

	/**
	 * return true if we succeed to jump to the next transition, false if there
	 * is no more transition .
	 * 
	 * @return
	 */
	public boolean jumpToNextTransition() {
		if (!hasMoreTransitions())
			return false;
		++transitionIndex;
		currentChar = getCurrentTransition().getMin();
		return true;
	}

	public boolean hasNextTransitionLevel() {
		return state.isAccept() && state.getTransitions().size() > 0 && !isNextTransitionLevelPassed;
	}

	public TransitionLevel nextTransitionLevel() {
		State nextState = getCurrentTransition().getDest();
		TransitionLevel nextTransitionLevel = new TransitionLevel(nextState);
		isNextTransitionLevelPassed = true;
		return nextTransitionLevel;
	}

	public Transition getTransition(int index) {
		return getState().getSortedTransitions(true).get(index);
	}

	public Transition getCurrentTransition() {
		return getTransition(transitionIndex);
	}

	public char getMaxCharInCurrentTransition() {
		return getCurrentTransition().getMax();
	}

	public char getCurrentChar() {
		return currentChar;
	}

	public void setCurrentChar(char currentChar) {
		this.currentChar = currentChar;
	}

	public int getTransitionIndex() {
		return transitionIndex;
	}

	public void setTransitionIndex(int transitionIndex) {
		this.transitionIndex = transitionIndex;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
