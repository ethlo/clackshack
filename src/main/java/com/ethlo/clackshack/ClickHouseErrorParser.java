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

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.http.HttpFields;

import com.ethlo.clackshack.model.QueryProgress;
import com.ethlo.clackshack.util.JsonUtil;

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

    public static ClickHouseException handle(final int errorNum, final String message, final QueryOptions queryOptions, final Response response)
    {
        final HttpFields headers = response.getHeaders();
        final String queryId = headers.get(ClackShackImpl.CLICKHOUSE_QUERY_ID_HEADER_NAME);
        final QueryProgress progress = Optional.ofNullable(headers.get(ClackShackImpl.CLICKHOUSE_SUMMARY_HEADER_NAME))
                .map(summary -> JsonUtil.readJson(summary, QueryProgress.class)).orElse(QueryProgress.ZERO);

        return switch (errorNum)
        {
            case 216 -> new DuplicateQueryIdException(message);
            case 394 -> new QueryAbortedException(queryId);
            case 159 ->
                    new QueryTimeoutException(queryId, queryOptions.maxExecutionTime().orElse(Duration.ZERO), progress, message);
            default -> new ClickHouseException(errorNum, message);
        };
    }
}
