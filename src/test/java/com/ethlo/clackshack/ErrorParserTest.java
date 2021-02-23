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
