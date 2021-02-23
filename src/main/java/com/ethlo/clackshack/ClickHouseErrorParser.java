package com.ethlo.clackshack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickHouseErrorParser
{
    public static RuntimeException handleError(final String strContent)
    {
        final String regexp = "^Code: ([0-9]+).*DB::Exception: (.*)\\(version";
        final Pattern pattern = Pattern.compile(regexp);
        final Matcher matcher = pattern.matcher(strContent);
        if (matcher.find())
        {
            final int id = Integer.parseInt(matcher.group(1));
            final String message = matcher.group(2);

            if (id == 216)
            {
                return new DuplicateQueryIdException(message);
            }
            return new ClickHouseException(id, message);
        }
        return new UncheckedIOException(new IOException(strContent));
    }
}
