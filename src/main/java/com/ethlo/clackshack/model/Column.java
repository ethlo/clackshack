package com.ethlo.clackshack.model;

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


import java.io.Serializable;

public class Column
{
    private String name;
    private String type;
    private Serializable value;

    public Column(final String name, final String type, final Serializable value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public Serializable getValue()
    {
        return value;
    }
}
