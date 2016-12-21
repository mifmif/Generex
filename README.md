Generex
=======
A Java library for generating String that match  a given regular expression. it help you generate all Strings that matches a given Regex, random one , or one String from the matched String based on it's index.
Generex is based on the library http://www.brics.dk/~amoeller/automaton/.

[![Build Status](https://travis-ci.org/mifmif/Generex.png)](https://travis-ci.org/mifmif/Generex)


**Features :**
-

-Generate Random String that match the Regex.

-Specify the min/max length  of the random generated String.

-Generate a list of all Strings that matches the Regex, if the number of String that matches the Regex is greater then Integer.MAX_VALUE , the returned list will contains Strings up to the size limit of java.util.List which is Integer.MAX_VALUE (see iterator's feature in this case). 

-generate a sublist of the Strings that matches the Regex based on the lexicographical order.

-Given an index 'n' , generate the n'th element in lexicographical order of the list of Strings that matches the Regex.

-Prepare an iterator that loop over all the Strings that matches the Regex. even if the set of String that matches the given Regex is infinite.
 

**How to use it :**
-

If you use [Maven](http://maven.apache.org) you can include this library to your project by adding the following dependency: 
```xml
<dependency>
  <groupId>com.github.mifmif</groupId>
  <artifactId>generex</artifactId>
  <version>1.0.2</version>
</dependency>
```

The list of all available versions can be found at [Maven Central](http://search.maven.org/#browse|588844112).

Here is the sample Java code demonstrating library usage:
```java


		Generex generex = new Generex("[0-3]([a-c]|[e-g]{1,2})");

		// Generate random String
		String randomStr = generex.random();
		System.out.println(randomStr);// a random value from the previous String list

		// generate the second String in lexicographical order that match the given Regex.
		String secondString = generex.getMatchedString(2);
		System.out.println(secondString);// it print '0b'

		// Generate all String that matches the given Regex.
		List<String> matchedStrs = generex.getAllMatchedStrings();

		// Using Generex iterator
		Iterator iterator = generex.iterator();
		while (iterator.hasNext()) {
			System.out.print(iterator.next() + " ");
		}
		// it prints:
		// 0a 0b 0c 0e 0ee 0ef 0eg 0f 0fe 0ff 0fg 0g 0ge 0gf 0gg
		// 1a 1b 1c 1e 1ee 1ef 1eg 1f 1fe 1ff 1fg 1g 1ge 1gf 1gg
		// 2a 2b 2c 2e 2ee 2ef 2eg 2f 2fe 2ff 2fg 2g 2ge 2gf 2gg
		// 3a 3b 3c 3e 3ee 3ef 3eg 3f 3fe 3ff 3fg 3g 3ge 3gf 3gg
		
```

**License**
-

Generex is licensed under the Apache License, Version 2.0.  
http://www.apache.org/licenses/LICENSE-2.0


 
