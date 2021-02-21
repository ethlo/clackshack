package com.ethlo.clackshack.model;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ProgressListener extends Function<QueryProgressData, Boolean>
{
    Logger logger = LoggerFactory.getLogger(ProgressListener.class);

    ProgressListener NOP = queryProgressData -> true;

    ProgressListener LOGGER = p ->
    {
        logger.info("Progress: {}", p);
        return true;
    };
}
