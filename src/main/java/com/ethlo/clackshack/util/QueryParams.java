package com.ethlo.clackshack.util;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2017 - 2021 Morten Haraldsen (ethlo)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ethlo.clackshack.model.QueryParam;

public class QueryParams
{
    private static QueryParam of(String name, Object value)
    {
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
        final List<QueryParam> paramList = new LinkedList<>();
        for (final Map.Entry<String, Object> p : params.entrySet())
        {
            paramList.add(QueryParams.of(p.getKey(), p.getValue()));
        }
        return paramList;
    }
}
