package com.ethlo.clackshack.util;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

public class IOUtil
{
    public static String read(InputStream inputStream)
    {
        final StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                resultStringBuilder.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        return resultStringBuilder.toString();
    }

    public static byte[] readAll(InputStream inputStream)
    {
        final byte[] buffer = new byte[8192];
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream())
        {
            int read = 0;
            while ((read = inputStream.read(buffer)) != -1)
            {
                bout.write(buffer, 0, read);
            }
            return bout.toByteArray();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public static String readClasspath(final String path)
    {
        return read(IOUtil.class.getClassLoader().getResourceAsStream(path));
    }
}
