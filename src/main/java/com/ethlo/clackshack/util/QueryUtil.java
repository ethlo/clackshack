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
