package com.ethlo.clackshack;

import java.time.Duration;
import java.util.Optional;

import com.ethlo.clackshack.model.QueryProgress;

public class QueryTimeoutException extends RuntimeException
{
    private final String queryId;
    private final Duration timeout;
    private final QueryProgress queryProgress;

    public QueryTimeoutException(String queryId, Duration timeout, QueryProgress queryProgress, String message)
    {
        super(message);
        this.queryId = queryId;
        this.timeout = timeout;
        this.queryProgress = queryProgress;
    }

    public String getQueryId()
    {
        return queryId;
    }

    public Duration getTimeout()
    {
        return timeout;
    }

    public Optional<QueryProgress> getQueryProgress()
    {
        return Optional.ofNullable(queryProgress);
    }
}
