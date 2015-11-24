/*
 * Copyright 2015 y.mifrah
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.mifmif.common.regex.util.Iterator;

import dk.brics.automaton.Automaton;

/**
 * Unit test for {@code Generex}.
 */
public class GenerexUnitTest {

	@Test(expected = NullPointerException.class)
	public void shouldFailToCreateAnInstanceWithUndefinedPattern() {
		// Given
		String undefinedPattern = null;
		// When
		Generex generex = new Generex(undefinedPattern);
		// Then = NullPointerException
	}

	@Test
	public void shouldNotFailToCreateAnInstanceWithUndefinedAutomaton() {
		// Given
		Automaton undefinedAutomaton = null;
		// When
		Generex generex = new Generex(undefinedAutomaton);
		// Then
		assertThat(generex, is(notNullValue()));
	}

	@Test
	public void shouldReturnTrueWhenQueryingIfInfiniteWithInfinitePattern() {
		// Given
		String infinitePattern = "a+";
		Generex generex = new Generex(infinitePattern);
		// When
		boolean infinite = generex.isInfinite();
		// Then
		assertThat(infinite, is(equalTo(true)));
	}

	@Test
	public void shouldReturnFalseWhenQueryingIfInfiniteWithFinitePattern() {
		// Given
		String finitePattern = "a{5}";
		Generex generex = new Generex(finitePattern);
		// When
		boolean infinite = generex.isInfinite();
		// Then
		assertThat(infinite, is(equalTo(false)));
	}

	@Test(expected = Exception.class)
	public void shouldFailWhenQueryingIfInfiniteWithUndefinedAutomaton() {
		// Given
		Generex generex = new Generex((Automaton) null);
		// When
		generex.isInfinite();
		// Then = Exception
	}

	@Test
	public void shouldReturnTrueWhenQueryingIfInfiniteWithInfiniteAutomaton() {
		// Given
		Automaton infiniteAutomaton = Automaton.makeChar('a').repeat(1); // same as "a+"
		Generex generex = new Generex(infiniteAutomaton);
		// When
		boolean infinite = generex.isInfinite();
		// Then
		assertThat(infinite, is(equalTo(true)));
	}

	@Test
	public void shouldReturnFalseWhenQueryingIfInfiniteWithFiniteAutomaton() {
		// Given
		Automaton finiteAutomaton = Automaton.makeChar('a').repeat(5, 5); // same as "a{5}"
		Generex generex = new Generex(finiteAutomaton);
		// When
		boolean infinite = generex.isInfinite();
		// Then
		assertThat(infinite, is(equalTo(false)));
	}

	@Test
	public void shouldReturnIteratorOfAPattern() {
		// Given
		Generex generex = new Generex("a");
		// When
		Iterator iterator = generex.iterator();
		// Then
		assertThat(iterator, is(notNullValue()));
	}

	@Test
	public void shouldReturnIteratorOfAnAutomaton() {
		// Given
		Automaton finiteAutomaton = Automaton.makeChar('a');
		Generex generex = new Generex(finiteAutomaton);
		// When
		Iterator iterator = generex.iterator();
		// Then
		assertThat(iterator, is(notNullValue()));
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailToReturnIteratorOfUndefinedAutomaton() {
		// Given
		Generex generex = new Generex((Automaton) null);
		// When
		generex.iterator();
		// Then = NullPointerException
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailToValidateUndefinedPattern() {
		// Given
		String undefinedPattern = null;
		// When
		Generex.isValidPattern(undefinedPattern);
		// Then = NullPointerException
	}

	@Test
	public void shouldReturnTrueWhenValidatingValidPattern() {
		// Given
		String validPattern = "[a-z0-9]{1,3}";
		// When
		boolean valid = Generex.isValidPattern(validPattern);
		// Then
		assertThat(valid, is(equalTo(true)));
	}

	@Test
	public void shouldReturnTrueWhenValidatingValidPatternWithPredefinedClasses() {
		// Given
		String validPattern = "\\d{2,3}\\w{1}";
		// When
		boolean valid = Generex.isValidPattern(validPattern);
		// Then
		assertThat(valid, is(equalTo(true)));
	}

	@Test
	public void shouldReturnFalseWhenValidatingInvalidPattern() {
		// Given
		String invalidPattern = "a)";
		// When
		boolean valid = Generex.isValidPattern(invalidPattern);
		// Then
		assertThat(valid, is(equalTo(false)));
	}

	@Test
	public void shouldReturnFalseWhenValidatingPatternWithRepetitionsHigherThanMaxIntegerValue() {
		// Given
		String invalidPattern = "[a-z0-9]{" + ((long) Integer.MAX_VALUE + 1) + "}";
		// When
		boolean valid = Generex.isValidPattern(invalidPattern);
		// Then
		assertThat(valid, is(equalTo(false)));
	}

	@Test
	public void shouldReturnFalseWhenValidatingPatternWithHigherNumberOfTransitions() {
		// Given
		String invalidPattern = createPatternWithTransitions(1000000);
		// When
		boolean valid = Generex.isValidPattern(invalidPattern);
		// Then
		assertThat(valid, is(equalTo(false)));
	}

	private static String createPatternWithTransitions(int numberOfTransitions) {
		StringBuilder strBuilder = new StringBuilder(numberOfTransitions * 2);
		for (int i = 1; i < numberOfTransitions; i++) {
			strBuilder.append('a').append('|');
		}
		if (numberOfTransitions > 0) {
			strBuilder.append('a');
		}
		return strBuilder.toString();
	}
}
