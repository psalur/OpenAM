/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2011-2015 ForgeRock AS.
 */
/*
 * Portions Copyrighted 2014 Nomura Research Institute, Ltd.
 */

package org.forgerock.openam.utils;

import org.forgerock.util.Reject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for handling Strings.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Swaps content with tags with the provided tag values.
     *
     * @param content The String content to be replaced
     * @param tagSwapMap A map containing the replacable tokens with their new values
     * @return the tagswapped String content
     */
    public static String tagSwap(String content, Map<String, String> tagSwapMap) {
        for (Map.Entry<String, String> entry : tagSwapMap.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        return content;
    }

    /**
     * Inserted content into a string.
     *
     * @param original The original string.
     * @param position The insertion position.
     * @param content The content to insert.
     * @return A new string with the inserted content.
     */
    public static String insertContent(String original, int position, String content) {
        return original.substring(0, position) + content + original.substring(position);
    }

    /**
     * Takes a list as a String (where each element is of the form name=value) and returns the value
     * specified by parameterName (if it exists).
     * @param list The list e.g. a=1,b=2,c=5
     * @param delimiter The delimiter used to separate the elements (e.g. ",")
     * @param parameterName The name of the parameter to return
     * @return The value(s) specified by parameterName
     */
    public static List<String> getParameter(String list, String delimiter, String parameterName) {
        String[] parameters = null;
        if (list != null) {
            parameters = list.split(delimiter);
        }

        List<String> result = new ArrayList<String>();
        if (parameters != null) {
            for (String parameter : parameters) {
                String[] valueParameterPair = parameter.split("=");
                final String currentParameterName = valueParameterPair[0];
                if (currentParameterName != null) {
                    if (parameterName.equals(currentParameterName.trim())) {
                        if (valueParameterPair.length == 2) {
                            result.add(valueParameterPair[1]);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks that the original string is not null nor empty, if so it returns the non-null default string instead.
     *
     * @param original
     *         the original string
     * @param defaultString
     *         the non-null default string
     *
     * @return the original string if not null and not empty, else the default string
     */
    public static String ifNullOrEmpty(final String original, final String defaultString) {
        Reject.ifNull(defaultString, "Default string must not be null");
        return (original == null || original.isEmpty()) ? defaultString : original;
    }

    /**
     * Encodes the passed String using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param component String to be encoded.
     * @param encoding The name of character encoding.
     * @return the same value as JavaScript encodeURIComponent function
     * @exception UnsupportedEncodingException
     *            If the named encoding is not supported
     */
    public static String encodeURIComponent(String component, String encoding) throws UnsupportedEncodingException {
        if (component == null) {
            return null;
        }
        String result = URLEncoder.encode(component, encoding)
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%7E", "~");
        return result;
    }

    /**
     * Determines if the string is empty.
     *
     * @param s string to test
     * @return true if the specified string is null or zero length.
     */
    public static boolean isEmpty(final String s) {
        return (s == null || s.length() == 0);
    }

    /**
     * Determines if the string is blank.
     *
     * @param s string to test
     * @return true if the specified string is null or when trimmed is empty (i.e. when trimmed it has zero length)
     */
    public static boolean isBlank(final String s) {
        return (s == null || s.trim().length() == 0);
    }

    /**
     * Determines if the string is not empty.
     *
     * @param s string to test
     * @return test if the specified string is not null and not empty (i.e. is greater than zero length).
     */
    public static boolean isNotEmpty(final String s) {
        return (s != null && s.length() > 0);
    }

    /**
     * Determines if the string is not blank.
     *
     * @param s string to test
     * @return true if the specified string is not null and when trimmed has greater than zero length.
     */
    public static boolean isNotBlank(final String s) {
        return (s != null && s.trim().length() > 0);
    }

    /**
     * Tests whether any string in the given set is blank.
     *
     * @param xs the set of strings.
     * @return {@code true} if the set is null or empty or if any member of the set is blank.
     * @see #isBlank(String)
     */
    public static boolean isAnyBlank(final Set<String> xs) {
        if (xs == null || xs.isEmpty()) {
            return true;
        }
        for (String x : xs) {
            if (isBlank(x)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compare two strings for equality.
     * @param s1 first string to compare.
     * @param s2 second string to compare.
     * @return true if strings are equal or they are both null.
     */
    public static boolean isEqualTo(String s1, String s2) {
        return CollectionUtils.genericCompare(s1, s2);
    }

    /**
     * Compares two strings in a case insensitive manner, that also allows for
     * either of the strings to be null, without issue.
     *
     * @param s1 the first string to be compared.
     * @param s2 the second string to tbe compared.
     * @return true if the parameter values are the same, false if different.
     */
    public static boolean compareCaseInsensitiveString(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
    }

    /**
     * Check if one string contains another string.
     * @param s1 The string to check in.
     * @param s2 The string to check for.
     * @return False if either string is null or if the first does not contain the second, otherwise true.
     */
    public static boolean contains(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.contains(s2);
    }

    /**
     * Check if one string contains another string in a case insensitive manner.
     * @param s1 The string to check in.
     * @param s2 The string to check for.
     * @return False if either string is null or if the first does not contain the second, otherwise true.
     */
    public static boolean containsCaseInsensitive(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    /**
     * Check if one string starts with another string.
     * @param s1 The string to check in.
     * @param s2 The string to check for.
     * @return False if either string is null or if the first does not start with the second, otherwise true.
     */
    public static boolean startsWith(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.startsWith(s2);
    }

    /**
     * Match the value to the regular expression pattern. The pattern given can contain a file name like search,
     * e.g. '*service*', which will be converted to a valid regular expression, e.g. '.*?service.*'.
     * @param value The value to match on.
     * @param strPattern The pattern to match.
     * @return True if the value matches the pattern, false otherwise.
     */
    public static boolean match(String value, String strPattern) {
        if (isNotEmpty(strPattern)) {
            if (isBlank(value)) {
                return strPattern.equals("*");
            }
            value = value.toLowerCase();
            strPattern = strPattern.toLowerCase();
            StringBuilder buff = new StringBuilder();

            for (int i = 0; i < strPattern.length() - 1; i++) {
                char c = strPattern.charAt(i);
                if (c == '*') {
                    buff.append(".*?");
                } else {
                    buff.append(c);
                }
            }

            char lastChar = strPattern.charAt(strPattern.length() - 1);
            if (lastChar == '*') {
                buff.append(".*");
            } else {
                buff.append(lastChar);
            }
            return Pattern.matches(buff.toString(), value);
        }

        return true;
    }
}
