package es.amplia.oda.core.commons.utils;

import java.util.Set;
import java.util.stream.Collectors;

public class DevicePattern {
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DevicePattern other = (DevicePattern) obj;
        if (pattern == null) {
            return other.pattern == null;
        } else return pattern.equals(other.pattern);
    }

    private final String pattern;
    private final String prefix;
    private final String postfix;
    private int asteriskPos;

    public static final DevicePattern NullDevicePattern = new DevicePattern("");
    public static final DevicePattern AllDevicePattern = new DevicePattern("*");
    
    public DevicePattern(String pattern) {
        if (pattern == null)
            throw new IllegalArgumentException("Devices Patterns cannot be null");
        this.pattern = pattern;
        asteriskPos = pattern.indexOf('*');
        if (asteriskPos == -1) {
            prefix = "";
            postfix = "";
            return;
        }
        if (pattern.indexOf('*', asteriskPos + 1) != -1)
            throw new IllegalArgumentException("Devices Patterns with two asterisk not currently supportted");
        this.prefix = pattern.substring(0, asteriskPos);
        this.postfix = pattern.substring(asteriskPos + 1);
    }

    public boolean match(String id) {
        if (id == null) {
            if (pattern.equals("*"))
                return true;
            return pattern.equals("");
        }
        if (asteriskPos == -1)
            return id.equals(pattern);
        if (!id.startsWith(prefix))
            return false;
        return id.endsWith(postfix);
    }

    public boolean matchAnyOf(Set<String> aSet) {
        return !selectMatching(aSet).isEmpty();
    }

    public Set<String> selectMatching(Set<String> aSet) {
        if (pattern.equals("*"))
            return aSet;
        return aSet.stream().filter(this::match).collect(Collectors.toSet());
    }

}

