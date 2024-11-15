package com.ethlo.clackshack.util;

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