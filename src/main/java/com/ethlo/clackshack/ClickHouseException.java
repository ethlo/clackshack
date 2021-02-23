package com.ethlo.clackshack;

public class ClickHouseException extends RuntimeException
{
    private final int errorCode;

    public ClickHouseException(final int errorCode, final String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode()
    {
        return errorCode;
    }
}
