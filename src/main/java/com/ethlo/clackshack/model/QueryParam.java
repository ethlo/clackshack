package com.ethlo.clackshack.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueryParam
{
    private final String name;
    private final String type;
    private final Object value;

    public QueryParam(final String name, final String type, final Object value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public static QueryParam of(final String name, final int value)
    {
        return new QueryParam(name, "Int32", value);
    }

    public static QueryParam of(final String name, final long value)
    {
        return new QueryParam(name, "Int64", value);
    }

    public static QueryParam of(final String name, final String value)
    {
        return new QueryParam(name, "String", value);
    }

    public static QueryParam of(final String name, final LocalDateTime dateTime)
    {
        return new QueryParam(name, "String", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(dateTime));
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }
}
