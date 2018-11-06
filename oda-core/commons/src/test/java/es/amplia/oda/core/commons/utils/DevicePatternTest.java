package es.amplia.oda.core.commons.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DevicePatternTest {
    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    @Test
    public void matchReturnsTrueWhenMatched() {
        DevicePattern dp = new DevicePattern("he*lo");
        assertThat(dp.match("hello"), is(true));
    }

    @Test
    public void matchReturnsTrueForAsterisk() {
        DevicePattern dp = new DevicePattern("*");
        assertThat(dp.match("bye"), is(true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void thePatternCanHaveAtMostOneAsterisk() {
        new DevicePattern("**");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void thePatternCannotBeNull() {
        new DevicePattern(null);
    }
    
    @Test
    public void withoutAsterisk_matchMatchesAsStringsDo() {
        DevicePattern dp = new DevicePattern("hello");
        assertThat(dp.match("bye"), is(false));
        assertThat(dp.match("hello"), is(true));
    }
    
    @Test
    public void withAsteriskThePrefixMustmatch() {
        DevicePattern dp = new DevicePattern("he*");
        assertThat(dp.match("je"), is(false));
    }

    @Test
    public void withAsteriskThePostfixMustmatch() {
        DevicePattern dp = new DevicePattern("*lo");
        assertThat(dp.match("je"), is(false));
    }
    
    @Test
    public void matchWithAsteriskReturnsTrueForNull() {
        DevicePattern dp = new DevicePattern("*");
        assertThat(dp.match(null), is(true));
    }

    @Test
    public void matchWithPatternReturnsFalseForNull() {
        DevicePattern dp = new DevicePattern("he*");
        assertThat(dp.match(null), is(false));
    }

    @Test
    public void matchOfEmpty_matchesWithNull() {
        DevicePattern dp = new DevicePattern("");
        assertThat(dp.match(null), is(true));
    }

    @Test
    public void matchOfEmpty_matchesWithEmpty() {
        DevicePattern dp = new DevicePattern("");
        assertThat(dp.match(""), is(true));
    }

    @Test
    public void matchAnyOf_alwaysReturnFalseForEmptySets() {
        DevicePattern dp = new DevicePattern("*");
        
        boolean actual = dp.matchAnyOf(asSet());
        
        assertThat(actual, is(false));
    }

    @Test
    public void matchAnyOf_returnsTrueForAsterisksAndNonEmptySets() {
        DevicePattern dp = new DevicePattern("*");
        Set<String> aSet = new HashSet<>();
        aSet.add(null);
        
        assertThat(dp.matchAnyOf(aSet), is(true));
    }

    @Test
    public void matchAnyOf_returnsTrueIfAnyElementMatches() {
        DevicePattern dp = new DevicePattern("he*lo");
        
        boolean actual = dp.matchAnyOf(asSet("bye","hello"));
        
        assertThat(actual, is(true));
    }

    @Test
    public void matchAnyOf_returnsFalseIfNoElementMatches() {
        DevicePattern dp = new DevicePattern("he*lo");
        
        boolean actual = dp.matchAnyOf(asSet("bye"));
        
        assertThat(actual, is(false));
    }
    
    @Test
    public void selectMatching_withAsteriskReturnsTheInput() {
        DevicePattern dp = new DevicePattern("*");
        
        Set<String> actual = dp.selectMatching(asSet());
        
        assertThat(actual, is(asSet()));
    }

    @Test
    public void selectMatching_withAsteriskReturnsAll() {
        DevicePattern dp = new DevicePattern("*");
        Set<String> input = asSet("hi", "ho");
        
        Set<String> actual = dp.selectMatching(input);
        
        assertThat(actual, is(input));
    }
    
    @Test
    public void selectMatching_filters() {
        DevicePattern dp = new DevicePattern("h*");
        Set<String> input = asSet("hi", "ho", "all");
        
        Set<String> actual = dp.selectMatching(input);
        
        assertThat(actual, is(asSet("hi","ho")));
    }
}

