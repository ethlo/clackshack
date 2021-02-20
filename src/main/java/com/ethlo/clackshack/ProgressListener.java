package com.ethlo.clackshack;

import java.util.function.Function;

public interface ProgressListener extends Function<QueryProgressData, Boolean>
{
    ProgressListener NOP = queryProgressData -> true;
    ProgressListener STDOUT = p ->
    {
        System.out.println(p);
        return true;
    };
}
