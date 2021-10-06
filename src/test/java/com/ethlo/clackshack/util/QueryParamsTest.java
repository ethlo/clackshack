package com.ethlo.clackshack.util;

/*-
 * #%L
 * ClackShack
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ethlo.clackshack.model.QueryParam;

public class QueryParamsTest
{
    @Test
    public void asList()
    {
        // Given
        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("foo", "bar");
        input.put("fee", 1L);

        // When
        final List<QueryParam> params = QueryParams.asList(input);

        // Then
        assertThat(params).hasSize(2);
        assertThat(params.get(0).getType()).isEqualTo("String");
        assertThat(params.get(1).getType()).isEqualTo("Int64");
    }
}
