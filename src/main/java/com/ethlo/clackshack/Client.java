package com.ethlo.clackshack;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import com.ethlo.clackshack.model.ProgressListener;
import com.ethlo.clackshack.model.QueryResult;

public interface Client
{
    default void query(final String query, final Consumer<QueryResult> resultListener)
    {
        query(query, Collections.emptyMap(), ProgressListener.NOP, resultListener);
    }

    default void query(final String query, final ProgressListener progressListener, final Consumer<QueryResult> resultListener)
    {
        query(query, Collections.emptyMap(), progressListener, resultListener);
    }

    void query(final String query, final Map<String, String> params, final ProgressListener progressListener, final Consumer<QueryResult> callback);
}
