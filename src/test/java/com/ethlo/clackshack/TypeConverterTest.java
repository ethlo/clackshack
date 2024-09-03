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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.ethlo.clackshack.model.ResultSet;
import com.ethlo.clackshack.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class TypeConverterTest
{
    @Test
    void testDateParsing()
    {
        verify(new ExpectedFormat("Date", new TextNode("2010-01-31"), LocalDate.parse("2010-01-31")));
    }

    @Test
    void testDateParsingInvalid()
    {
        verify(new ExpectedFormat("Date", new TextNode("2010-01-32"), null));
    }

    @Test
    void testDateTimeParsing()
    {
        verify(new ExpectedFormat("DateTime", new TextNode("2010-01-31 02:03:04"), LocalDateTime.parse("2010-01-31T02:03:04")));
    }

    @Test
    void testLowCardinalityNullableStringParsing()
    {
        verify(new ExpectedFormat("LowCardinality(Nullable(String))", new TextNode("urn:foo:bar"), "urn:foo:bar"));
    }

    @Test
    void testNullableStringParsing()
    {
        verify(new ExpectedFormat("Nullable(String))", new TextNode("urn:foo:bar"), "urn:foo:bar"));
    }

    @Test
    void testEnum8Parsing()
    {
        verify(new ExpectedFormat("Enum8('OBSERVE' = 1, 'ADD' = 2, 'DELETE' = 3)", new TextNode("OBSERVE"), "OBSERVE"));
    }

    @Test
    void testArrayLowCardinalityNullableStringParsing()
    {
        final JsonNode input = JsonUtil.readTree("""
                ["Hello", "World!"]""");
        verify(new ExpectedFormat("Array(LowCardinality(Nullable(String)))", input, input));
    }

    @Test
    void testMapLowCardinalityStringParsing()
    {
        final JsonNode input = JsonUtil.readTree("""
                {"Hello": "Verb", "World!": "Subject"}""");
        verify(new ExpectedFormat("Map(LowCardinality(String), String)", input, input));
    }

    private void verify(final ExpectedFormat format)
    {
        try
        {
            assertThat(ResultSet.convertType(format.type, format.input)).isEqualTo(format.expected);
        }
        catch (TypeConversionException exc)
        {
            if (format.expected != null)
            {
                throw exc;
            }
        }
    }

    private record ExpectedFormat(String type, JsonNode input, Object expected)
    {
    }
}
