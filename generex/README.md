Generex
=======
A Java library for generating String that match  a given regular expression. it help you generate all Strings that matches a given Regex, random one , or one String from the matched String based on it's index.
Generex is based on the library http://www.brics.dk/~amoeller/automaton/.


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
 

```java


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

import java.util.List;

/**
 * @author y.mifrah
 * 
 */
public class Main {
    public static void main(String[] args) {
        String raw = "user@server[0-2][0-2]env.sub.domain.sufix";
        String parse = parseRawInput(raw);
        Generex generex = new Generex(parse);

        // Generate all String that matches the given Regex.
        List<String> matchedStrs = generex.getAllMatchedStrings();

        for (String str : matchedStrs) {
            System.out.println(str);
        }

    }

    /**
     * Parses the raw input.
     * 
     * @param raw
     *            the raw
     * @return the string
     */
    private static String parseRawInput(String raw) {
        System.out.println("RAW : " + raw);
        StringBuilder build = new StringBuilder();
        for (char c : raw.toCharArray()) {
            switch (c) {
            /*
             * These are special cases that needs to be escaped. If these chars
             * are allowed this would hang the regexp generator.
             */
                case '@':
                case '.':
                    build.append("\\").append(c);
                    break;
                default:
                    build.append(c);

            }
        }
        System.out.println("PARSED: " + build);
        return build.toString();
    }
}

		
```
**Output:**
```
RAW : user@server[0-2][0-2]env.sub.domain.sufix
PARSED: user\@server[0-2][0-2]env\.sub\.domain\.sufix
user@server00env.sub.domain.sufix
user@server01env.sub.domain.sufix
user@server02env.sub.domain.sufix
user@server10env.sub.domain.sufix
user@server11env.sub.domain.sufix
user@server12env.sub.domain.sufix
user@server20env.sub.domain.sufix
user@server21env.sub.domain.sufix
user@server22env.sub.domain.sufix



```

**License**
-

Generex is licensed under the Apache License, Version 2.0.  
http://www.apache.org/licenses/LICENSE-2.0
 
