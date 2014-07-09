package com.mifmif.common.regex;

import java.util.List;

import com.mifmif.common.regex.util.Iterator;

/**
 * @author y.mifrah
 *
 */
public class Main {
	public static void main(String[] args) {
		Generex generex = new Generex("[0-3]([a-c]|[e-g]{1,2})");

		// generate the second String in lexicographical order that match the
		// given Regex.
		String secondString = generex.getMatchedString(2);
		System.out.println(secondString);// it print '0b'

		// Generate all String that matches the given Regex.
		List<String> matchedStrs = generex.getAllMatchedStrings();

		// Using Generex iterator
		Iterator iterator = generex.iterator();
		while (iterator.hasNext()) {
			System.out.print(iterator.next() + " ");
		}
		// it print 0a 0b 0c 0e 0ee 0e 0e 0f 0fe 0f 0f 0g 0ge 0g 0g 1a 1b 1c 1e
		// 1ee
		// 1e 1e 1f 1fe 1f 1f 1g 1ge 1g 1g 2a 2b 2c 2e 2ee 2e 2e 2f 2fe 2f 2f 2g
		// 2ge 2g 2g 3a 3b 3c 3e 3ee 3e 3e 3f 3fe 3f 3f 3g 3ge 3g 3g 1ee

		// Generate random String
		String randomStr = generex.random();
		System.out.println(randomStr);// a random value from the previous String list

	}
}
