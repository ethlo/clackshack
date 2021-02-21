package com.ethlo.clackshack;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import com.ethlo.clackshack.model.QueryResult;

public interface Client
{
    default void query(final String query, final Consumer<QueryResult> resultListener)
    {
        query(query, Collections.emptyMap(), QueryProgressListener.NOP, resultListener);
    }

    default void query(final String query, final QueryProgressListener queryProgressListener, final Consumer<QueryResult> resultListener)
    {
        query(query, Collections.emptyMap(), queryProgressListener, resultListener);
    }

    void query(final String query, final Map<String, String> params, final QueryProgressListener queryProgressListener, final Consumer<QueryResult> callback);

    void close();
}
