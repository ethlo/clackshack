package com.ethlo.clackshack.util;

/*-
 * #%L
 * ClackShack
 * %%
 * Copyright (C) 2021 - 2024 Morten Haraldsen (ethlo)
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ethlo.clackshack.model.QueryParam;

class QueryUtilTest
{
    @Test
    void testPlaceHolder()
    {
        final String query = QueryUtil.format("select * from foo where bar = :bar", List.of(QueryParam.of("bar", 123)));
        assertThat(query).isEqualTo("select * from foo where bar = {bar:Int32}");
    }

    @Test
    void testPlaceHolderLikeStringInQuotes()
    {
        final String query = QueryUtil.format("select * from foo where bar = ':bar'", List.of());
        assertThat(query).isEqualTo("select * from foo where bar = ':bar'");
    }

    @Test
    void testAdvancedFunction()
    {
        final List<QueryParam> params = List.of(QueryParam.of("organization_alias", "acme"),
                QueryParam.of("list", List.of("a", "b", "c"), "String")
        );

        final String query = QueryUtil.format("""
                SELECT sum(c) AS `count`, toStartOfDay(event_time) AS `event_time`
                FROM events te WHERE 1=1
                AND te.organization_alias = :organization_alias
                AND hasAny(:list, list)
                GROUP BY `event_time`
                ORDER BY `event_time`
                """, params
        );

        assertThat(query).isEqualToIgnoringWhitespace("""
                SELECT sum(c) AS `count`, toStartOfDay(event_time) AS `event_time`
                FROM events te WHERE 1=1
                AND te.organization_alias = {organization_alias:String}
                AND hasAny({list:Array(String)}, list)
                GROUP BY `event_time`
                ORDER BY `event_time`""");
    }
}
