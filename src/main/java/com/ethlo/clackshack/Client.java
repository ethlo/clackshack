package com.ethlo.clackshack;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.ethlo.clackshack.model.QueryParam;
import com.ethlo.clackshack.model.QueryResult;

public interface Client extends AutoCloseable
{
    default CompletableFuture<QueryResult> query(final String query)
    {
        return query(query, Collections.emptyList(), QueryProgressListener.NOP);
    }

    default CompletableFuture<QueryResult> query(final String query, final QueryProgressListener queryProgressListener)
    {
        return query(query, Collections.emptyList(), queryProgressListener);
    }

    CompletableFuture<QueryResult> query(final String query, final List<QueryParam> params, final QueryProgressListener queryProgressListener);

    void close();
}
