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
