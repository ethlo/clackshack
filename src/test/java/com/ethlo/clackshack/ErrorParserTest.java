package com.ethlo.clackshack;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ErrorParserTest
{
    @Test
    public void testParseDuplicateQueryId()
    {
        final String content = "Code: 216, e.displayText() = DB::Exception: Query with id = some-query-id is already running. (version 20.4.4.18 (official build))";
        final RuntimeException exc = ClickHouseErrorParser.handleError(content);
        assertThat(exc).isInstanceOf(DuplicateQueryIdException.class);
        assertThat(((DuplicateQueryIdException) exc).getErrorCode()).isEqualTo(216);
    }
}
