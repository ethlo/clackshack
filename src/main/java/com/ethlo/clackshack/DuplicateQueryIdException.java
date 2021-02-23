package com.ethlo.clackshack;

public class DuplicateQueryIdException extends ClickHouseException
{
    public DuplicateQueryIdException(final String message)
    {
        super(216, message);
    }
}
