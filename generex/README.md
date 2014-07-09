Generex
=======
A Java library for generating String that match  a given regular expression. it help you generate all Strings that matches a given Regex, random one , or one String from the matched String based on it's index.


**Features :**
-

-Generate Random String that match the Regex.
-Specify the min/max length  of the random generated String.
-Generate a list of all Strings that matches the Regex, if the number of String that matches the Regex is greater then Integer.MAX_VALUE , the returned list will contains Strings up to the size limit of java.util.List which is Integer.MAX_VALUE (see iterator's feature in this case). 
-generate a sublist of the Strings that matches the Regex based on the lexicographical order.
-Given an index 'n' , generate the n'th element in lexicographical order of the list of Strings that matches the Regex.
-Prepare an iterator that loop over all the Strings that matches the Regex.
 

**How to use it :**
-
 

```java


		Generex generex = new Generex("[0-3]([a-c]|[e-g]{1,2})");

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
		// it print 0a 0b 0c 0e 0ee 0e 0e 0f 0fe 0f 0f 0g 0ge 0g 0g 1a 1b 1c 1e
		// 1ee 1e 1e 1f 1fe 1f 1f 1g 1ge 1g 1g 2a 2b 2c 2e 2ee 2e 2e 2f 2fe 2f 2f 2g
		// 2ge 2g 2g 3a 3b 3c 3e 3ee 3e 3e 3f 3fe 3f 3f 3g 3ge 3g 3g 1ee

		// Generate random String
		String randomStr = generex.random();
		System.out.println(randomStr);// a random value from the previous String list
		
```


 