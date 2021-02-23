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

import org.junit.Test;

import com.ethlo.clackshack.model.QueryResult;

public class TypeConverterTest
{
    @Test
    public void testInputTypes()
    {
        verify(new ExpectedFormat("Date", "2010-01-31", true, LocalDate.parse("2010-01-31")));
        verify(new ExpectedFormat("Date", "2010-01-32", false, null));

        verify(new ExpectedFormat("DateTime", "2010-01-31 02:03:04", true, LocalDateTime.parse("2010-01-31T02:03:04")));

        /*chTypes.add("Array", "[]");
        chTypes.add("Date", "2010-01-31");
        chTypes.add("DateTime", "2010-01-31 00:10:20");
        chTypes.add("DateTime64", "");
        "Decimal", "Decimal128", "Decimal32",
                "Decimal64", "Enum", "Enum16", "Enum8", "FixedString", "Float32", "Float64", "IPv4", "IPv6", "Int16", "Int32",
                "Int64", "Int8", "IntervalDay", "IntervalHour", "IntervalMinute", "IntervalMonth", "IntervalQuarter", "IntervalSecond",
                "IntervalWeek", "IntervalYear", "LowCardinality", "Nested", "Nothing", "Nullable", "SimpleAggregateFunction", "String",
                "Tuple", "UInt16", "UInt32", "UInt64", "UInt8", "UUID"
        );*/
    }

    private void verify(final ExpectedFormat format)
    {
        try
        {
            assertThat(QueryResult.convertType(format.type, format.input)).isEqualTo(format.expected);
        }
        catch (TypeConversionException exc)
        {
            if (format.expectedOk)
            {
                throw exc;
            }
        }
    }

    private static class ExpectedFormat
    {
        private final String type;
        private final String input;
        private final boolean expectedOk;
        private final Object expected;

        private ExpectedFormat(final String type, final String input, final boolean expectedOk, final Object expected)
        {
            this.type = type;
            this.input = input;
            this.expectedOk = expectedOk;
            this.expected = expected;
        }
    }
}
