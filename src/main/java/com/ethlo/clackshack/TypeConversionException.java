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

public class TypeConversionException extends RuntimeException
{
    private final String type;
    private final String input;

    public TypeConversionException(final String type, final String input, final Class<?> targetType, final Exception cause)
    {
        super("Unable to convert " + input + " of source type " + type + " to type " + targetType.getCanonicalName() + ": " + cause.getMessage(), cause);
        this.type = type;
        this.input = input;
    }

    public String getType()
    {
        return type;
    }

    public String getInput()
    {
        return input;
    }
}
