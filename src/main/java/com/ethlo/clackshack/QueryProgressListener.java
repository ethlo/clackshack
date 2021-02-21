package com.ethlo.clackshack;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryProgress;

public interface QueryProgressListener extends Function<QueryProgress, Boolean>
{
    Logger logger = LoggerFactory.getLogger(QueryProgressListener.class);

    QueryProgressListener NOP = queryProgress -> true;

    QueryProgressListener LOGGER = p ->
    {
        logger.info("Progress: {}", p);
        return true;
    };
}
