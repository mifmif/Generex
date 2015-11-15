/*
 * Copyright 2015 y.mifrah
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mifmif.common.regex;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * Unit test for {@code GenerexIterator}.
 */
public class GenerexIteratorUnitTest {

	@Test(expected = NullPointerException.class)
	public void shouldFailToCreateIteratorWithUndefinedInitialState() {
		// Given
		State initialState = null;
		// When
		GenerexIterator iterator = new GenerexIterator(initialState);
		// Then = NullPointerException
	}

	@Test
	public void shouldNotHaveNextIfInitialStateIsNotAcceptedAndHasNoTransitions() {
		// Given
		State initialStateWithoutTranstions = new State();
		GenerexIterator iterator = new GenerexIterator(initialStateWithoutTranstions);
		// When
		boolean hasNext = iterator.hasNext();
		// Then
		assertThat(hasNext, is(equalTo(false)));
	}

	@Test
	public void shouldHaveNextIfInitialStateIsAcceptedEvenIfHasNoTransitions() {
		// Given
		State acceptedState = new State();
		acceptedState.setAccept(true);
		GenerexIterator iterator = new GenerexIterator(acceptedState);
		// When / Then
		boolean hasNext = iterator.hasNext();
		assertThat(hasNext, is(equalTo(true)));
		String next = iterator.next();
		assertThat(next, is(equalTo("")));
	}

	@Test
	public void shouldNotHaveNextIfInitialStateHasJustTransitionToRejectedState() {
		// Given
		State initialState = new State();
		State rejectedState = new State();
		initialState.addTransition(new Transition('a', rejectedState));
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When
		boolean hasNext = iterator.hasNext();
		// Then
		assertThat(hasNext, is(equalTo(false)));
	}

	@Test
	public void shouldHaveNextIfInitialStateHasTransitionToAcceptedState() {
		// Given
		State initialState = new State();
		State acceptedState = new State();
		acceptedState.setAccept(true);
		initialState.addTransition(new Transition('a', acceptedState));
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When / Then
		boolean hasNext = iterator.hasNext();
		assertThat(hasNext, is(equalTo(true)));
		String next = iterator.next();
		assertThat(next, is(equalTo("a")));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToObtainNextIfDoesNotHaveNext() {
		// Given
		State rejectedInitialState = new State();
		GenerexIterator iterator = new GenerexIterator(rejectedInitialState);
		// When
		String string = iterator.next();
		// Then = IllegalStateException
	}

	@Test
	public void shouldReturnSameStringEvenIfHasNextIsCalledMultipleTimes() {
		// Given
		State initialState = Automaton.makeChar('a').union(Automaton.makeChar('b')).getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When / Then
		boolean hasNext = iterator.hasNext() & iterator.hasNext() & iterator.hasNext();
		assertThat(hasNext, is(equalTo(true)));
		String next = iterator.next();
		assertThat(next, is(equalTo("a")));
	}

	@Test
	public void shouldReturnFollowingStringsIfNextIsCalledMultipleTimes() {
		// Given
		State initialState = Automaton.makeChar('a').union(Automaton.makeChar('b')).getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When
		String next = iterator.next();
		String followingNext = iterator.next();
		// Then
		assertThat(next, is(equalTo("a")));
		assertThat(followingNext, is(equalTo("b")));
	}

	@Test
	public void shouldReturnFalseIfNoNextEvenIfHasNextIsCalledMultipleTimes() {
		// Given
		State initialState = Automaton.makeEmpty().getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When
		boolean hasNext = iterator.hasNext() | iterator.hasNext() | iterator.hasNext();
		// Then
		assertThat(hasNext, is(equalTo(false)));
	}

	@Test
	public void shouldIteratorOverRangeOfChars() {
		// Given
		char min = Character.MIN_VALUE;
		char max = Character.MAX_VALUE;
		State initialState = Automaton.makeCharRange(min, max).getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When / Then
		for (int character = min; character <= max; character++) {
			boolean hasNext = iterator.hasNext();
			assertThat(hasNext, is(equalTo(true)));
			String next = iterator.next();
			assertThat(next, is(equalTo(createString((char) character, 1))));
		}
		boolean hasNext = iterator.hasNext();
		assertThat(hasNext, is(equalTo(false)));
	}

	@Test
	public void shouldIteratorOverCharWithVariableLength() {
		// Given
		char character = 'a';
		int minLength = 1;
		int maxLength = 15;
		State initialState = Automaton.makeChar(character).repeat(minLength, maxLength).getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When / Then
		for (int count = minLength; count <= maxLength; count++) {
			boolean hasNext = iterator.hasNext();
			assertThat(hasNext, is(equalTo(true)));
			String next = iterator.next();
			assertThat(next, is(equalTo(createString(character, count))));
		}
		boolean hasNext = iterator.hasNext();
		assertThat(hasNext, is(equalTo(false)));
	}

	@Test
	public void shouldIteratorOverInfiniteState() {
		// Given
		char character = 'a';
		int max = 1000;
		State initialState = Automaton.makeChar(character).repeat(1).getInitialState();
		GenerexIterator iterator = new GenerexIterator(initialState);
		// When / Then
		for (int count = 1; count <= max; count++) {
			boolean hasNext = iterator.hasNext();
			assertThat(hasNext, is(equalTo(true)));
			String next = iterator.next();
			assertThat(next, is(equalTo(createString(character, count))));
		}
		boolean hasNext = iterator.hasNext();
		assertThat(hasNext, is(equalTo(true)));
	}

	private static String createString(char character, int length) {
		StringBuilder strBuilder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			strBuilder.append(character);
		}
		return strBuilder.toString();
	}
}
