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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ethlo.clackshack.model.QueryParam;

public class QueryUtil
{
    public static String format(String format, List<QueryParam> values)
    {
        final StringBuilder formatter = new StringBuilder(format);
        final List<Object> valueList = new ArrayList<>();
        final Matcher matcher = Pattern.compile(":(\\w+)").matcher(format);

        while (matcher.find())
        {
            String key = matcher.group(1);
            String formatKey = String.format(":%s", key);
            final int index = formatter.indexOf(formatKey);

            if (index != -1)
            {
                formatter.replace(index, index + formatKey.length(), "%s");
                final QueryParam param = getValue(key, values);
                valueList.add(String.format("{%s:%s}", param.getName(), param.getType()));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }

    private static QueryParam getValue(final String key, final List<QueryParam> values)
    {
        return values.stream().filter(v -> v.getName().equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("No such param name: " + key));
    }
}
