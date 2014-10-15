package com.mifmif.common.regex;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GenerexTest {

    private String pattern;
    private Generex generex;
    
    @Parameters(name = "Test get match: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Sample multicharacter expression","[A-B]{5,9}"},
                {"Sample expression","[0-3]([a-c]|[e-g]{1,2})"},
                {"Number format","\\d{3,4}"},
                //{"Any non-number","\\D{3,4}"},
                {"Any word","\\w{1,2}"},
                {"Empty string",""},
                //{"Any non-word","\\W{1,2}"}
        });
    }

    public GenerexTest(String description,
            String patternValue) {
        this.pattern = patternValue;
    }

    @Before
    public void setUp() throws Exception {
        generex = new Generex(pattern);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetMatchedFirstMatchShouldBeTheSameAsMatchWithZeroIndex() {
        String firstMatch = generex.getFirstMatch();
        String matchStringZeroIndex = generex.getMatchedString(0);
        Assert.assertTrue(
                String.format("The generated string '%s' doesn't match the pattern '%s'", firstMatch, pattern),
                firstMatch.matches(pattern)
        );
        Assert.assertTrue(
                String.format("The generated string '%s' doesn't match the pattern '%s'", matchStringZeroIndex, pattern),
                matchStringZeroIndex.matches(pattern)
        );
        Assert.assertEquals(
                firstMatch,
                matchStringZeroIndex);
    }

    @Test
    public void testIterateThroughAllMatchesShouldReturnConsistentResults () {
        generex.getFirstMatch();
        long total = generex.matchedStringsSize();
        for (int count = 1; count < total; count++) {
            String matchStringZeroIndex = generex.getMatchedString(count);
            Assert.assertTrue(
                    String.format("The generated string '%s' doesn't match the pattern '%s' at iteration #%d",
                            matchStringZeroIndex, pattern, count),
                    matchStringZeroIndex.matches(pattern)
            );
        }
    }
}
