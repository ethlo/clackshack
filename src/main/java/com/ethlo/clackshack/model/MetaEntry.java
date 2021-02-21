package com.ethlo.clackshack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaEntry
{
    private final String name;
    private final String type;

    public MetaEntry(@JsonProperty("name") final String name, @JsonProperty("type") final String type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
}
