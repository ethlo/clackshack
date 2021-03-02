package com.ethlo.clackshack;

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

import java.util.AbstractMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickHouseErrorParser
{
    private static final String regexp = "^Code: ([0-9]+).*DB::Exception: (.*)\\(version";
    private static final Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    public static Optional<AbstractMap.SimpleImmutableEntry<Integer, String>> parseError(final String strContent)
    {
        final Matcher matcher = pattern.matcher(strContent);
        if (matcher.find())
        {
            final int id = Integer.parseInt(matcher.group(1));
            final String message = matcher.group(2);
            return Optional.of(new AbstractMap.SimpleImmutableEntry<>(id, message));
        }
        return Optional.empty();
    }

    public static ClickHouseException handle(final AbstractMap.SimpleImmutableEntry<Integer, String> error)
    {
        final int id = error.getKey();
        final String message = error.getValue();
        switch (id)
        {
            case 216:
                return new DuplicateQueryIdException(message);

            case 394:
                return new QueryAbortedException();
        }
        return new ClickHouseException(id, message);
    }
}
