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

import com.ethlo.clackshack.model.QueryParam;

public class QueryParams
{
    public static QueryParam of(String name, Object value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Cannot handle null as it is impossible to infer the data type for parameter '" + name + "'");
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
