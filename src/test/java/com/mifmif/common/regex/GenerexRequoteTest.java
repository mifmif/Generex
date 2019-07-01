package com.mifmif.common.regex;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Vadim Kopichenko
 */
@RunWith(Parameterized.class)
public class GenerexRequoteTest {

    private static final String Q = "\\Q";
    private static final String E = "\\E";

    private String patternValue;
    private Generex generex;

    @Parameters(name = "{0}")
    public static Collection<String[]> data() {
        List<String[]> data = new ArrayList<String[]>(300);
        for (char c = (char) 0; c < 256; ++c) {
            data.add(new String[]{"char " + c + " (" + (int) c + ")", Q + Character.valueOf(c).toString() + E});
        }
        data.addAll(Arrays.asList(new String[][]{
                {"common regex special chars", Q + ".^$*+?(){|[\\" + E}, // http://stackoverflow.com/questions/399078/what-special-characters-must-be-escaped-in-regular-expressions
                {"email", Q + "minion_X@gru.evil" + E}, // https://github.com/mifmif/Generex/issues/21
                {"url", Q + "https://github.com/mifmif/Generex/issues/21?a=b&c=true&c=false" + E},
                {"'json'", Q + "{a:13, \"b\": 'test'}" + E},
                {"xml", Q + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                        "\t&entity;\r\n" +
                        "\t<modelVersion>4.0.0</modelVersion>\n" +
                        "\t<groupId>com.github.mifmif</groupId>\n" +
                        "</project>" + E},
                {"multiple neighbour quote blocks", "Both regex and automaton special chars should be preserved: \\Q.^$*+?(){|[\\\\E\\Q@&#~\"\\E"},
                }));
        return data;
    }

    public GenerexRequoteTest(String description, String patternValue) {
        this.patternValue = patternValue;
    }

    @Before
    public void setUp() {
        generex = new Generex(patternValue);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRequote() {
        Assert.assertEquals("Should produce single string", 1, generex.matchedStringsSize());
        Assert.assertEquals("Should produce exact input string",
                patternValue.replace(Q, "").replace(E, ""),
                generex.random());
    }

}
