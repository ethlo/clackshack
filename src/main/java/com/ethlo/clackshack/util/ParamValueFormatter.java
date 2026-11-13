package com.ethlo.clackshack.util;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2021 Morten Haraldsen (ethlo)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Formats values of composite query parameters (Array, Map) in the quoted text format ClickHouse expects,
 * e.g. <code>['a','it\'s']</code> or <code>{'key':1}</code>. ClickHouse does not accept JSON (double-quoted strings) here.
 */
public class ParamValueFormatter
{
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ParamValueFormatter()
    {
    }

    public static String format(final Object value)
    {
        final StringBuilder sb = new StringBuilder();
        append(sb, value);
        return sb.toString();
    }

    static boolean isArrayLike(final Object value)
    {
        return value instanceof Collection<?> || (value != null && value.getClass().isArray());
    }

    static List<Object> asList(final Object value)
    {
        if (value instanceof Collection<?> c)
        {
            return new ArrayList<>(c);
        }
        final int length = Array.getLength(value);
        final List<Object> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
        {
            result.add(Array.get(value, i));
        }
        return result;
    }

    private static void append(final StringBuilder sb, final Object value)
    {
        if (value == null)
        {
            sb.append("NULL");
        }
        else if (value instanceof Boolean b)
        {
            sb.append(b ? '1' : '0');
        }
        else if (value instanceof Float f)
        {
            // Matches the value a Float has once widened to Float64, and still parses back to the same Float32
            sb.append(f.doubleValue());
        }
        else if (value instanceof Number)
        {
            sb.append(value);
        }
        else if (value instanceof LocalDateTime dateTime)
        {
            // Keeps fractional seconds when present, for DateTime64 elements
            appendQuoted(sb, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime));
        }
        else if (value instanceof InetAddress address)
        {
            appendQuoted(sb, address.getHostAddress());
        }
        else if (isArrayLike(value))
        {
            final StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (final Object element : asList(value))
            {
                joiner.add(format(element));
            }
            sb.append(joiner);
        }
        else if (value instanceof Map<?, ?> map)
        {
            final StringJoiner joiner = new StringJoiner(",", "{", "}");
            for (final Map.Entry<?, ?> entry : map.entrySet())
            {
                joiner.add(format(entry.getKey()) + ":" + format(entry.getValue()));
            }
            sb.append(joiner);
        }
        else
        {
            appendQuoted(sb, value.toString());
        }
    }

    private static void appendQuoted(final StringBuilder sb, final String value)
    {
        sb.append('\'');
        for (int i = 0; i < value.length(); i++)
        {
            final char c = value.charAt(i);
            if (c == '\\' || c == '\'')
            {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append('\'');
    }
}
