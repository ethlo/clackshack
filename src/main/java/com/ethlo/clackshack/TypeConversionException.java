package com.ethlo.clackshack;

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
