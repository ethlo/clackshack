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


import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ethlo.clackshack.model.QueryParam;

public class QueryParams
{
    private static final List<String> INTEGER_TYPES = List.of("Int8", "Int16", "Int32", "Int64");
    private static final List<String> FLOAT_TYPES = List.of("Float32", "Float64");
    private static final Set<Class<?>> NUMBER_TYPES = Set.of(Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);

    public static QueryParam of(String name, Object value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Cannot handle null as it is impossible to infer the data type for parameter '" + name + "'");
        }

        if (ParamValueFormatter.isArrayLike(value))
        {
            final List<Object> elements = ParamValueFormatter.asList(value);
            return new QueryParam(name, "Array(%s)".formatted(elementType(name, elements)), ParamValueFormatter.format(elements));
        }

        final Class<?> type = value.getClass();
        if (type == Boolean.class)
        {
            return QueryParam.of(name, (boolean) value);
        }
        else if (type == Character.class)
        {
            return new QueryParam(name, "String", value.toString());
        }
        else if (type == Byte.class)
        {
            return QueryParam.of(name, (byte) value);
        }
        else if (type == Short.class)
        {
            return QueryParam.of(name, (short) value);
        }
        else if (type == Integer.class)
        {
            return QueryParam.of(name, (int) value);
        }
        else if (type == Long.class)
        {
            return QueryParam.of(name, (long) value);
        }
        else if (type == Float.class)
        {
            return QueryParam.of(name, (float) value);
        }
        else if (type == Double.class)
        {
            return QueryParam.of(name, (double) value);
        }
        else if (type == LocalDateTime.class)
        {
            return QueryParam.of(name, (LocalDateTime) value);
        }

        return QueryParam.of(name, value.toString());
    }

    private static String elementType(final String name, final List<Object> elements)
    {
        String type = null;
        for (final Object element : elements)
        {
            if (element == null)
            {
                throw new IllegalArgumentException("Cannot handle null elements in array parameter '" + name + "'");
            }
            if ((element instanceof Number && !NUMBER_TYPES.contains(element.getClass())) || element instanceof Map<?, ?>)
            {
                throw new IllegalArgumentException("Cannot infer the data type of " + element.getClass().getSimpleName() + " elements in array parameter '" + name + "', use QueryParam.of(name, list, type)");
            }
            final String elementType = of(name, element).getType();
            type = type == null ? elementType : widen(name, type, elementType);
        }
        // An empty array has no element type to infer; Nothing is ClickHouse's type of an empty array literal
        return type != null ? type : "Nothing";
    }

    private static String widen(final String name, final String a, final String b)
    {
        if (a.equals(b))
        {
            return a;
        }

        final int intA = INTEGER_TYPES.indexOf(a);
        final int intB = INTEGER_TYPES.indexOf(b);
        if (intA >= 0 && intB >= 0)
        {
            return INTEGER_TYPES.get(Math.max(intA, intB));
        }

        // Int64 does not fit in Float64 without losing precision
        final boolean floatA = FLOAT_TYPES.contains(a) || (intA >= 0 && !a.equals("Int64"));
        final boolean floatB = FLOAT_TYPES.contains(b) || (intB >= 0 && !b.equals("Int64"));
        if (floatA && floatB)
        {
            return "Float64";
        }

        throw new IllegalArgumentException("Cannot mix element types " + a + " and " + b + " in array parameter '" + name + "'");
    }

    public static List<QueryParam> asList(final Map<String, Object> params)
    {
        if (params == null)
        {
            return null;
        }

        final List<QueryParam> paramList = new LinkedList<>();
        for (final Map.Entry<String, Object> p : params.entrySet())
        {
            paramList.add(of(p.getKey(), p.getValue()));
        }
        return paramList;
    }
}
