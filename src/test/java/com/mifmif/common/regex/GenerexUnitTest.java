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
import static org.junit.Assert.assertThat;

import org.junit.Test;

import dk.brics.automaton.Automaton;

/**
 * Unit test for {@code Generex}.
 */
public class GenerexUnitTest {

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
}
