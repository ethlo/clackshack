package com.ethlo.clackshack;

public class QueryAbortedException extends ClickHouseException
{
    private final String query;
    private final String queryId;

    public QueryAbortedException(final String queryId, final String query)
    {
        super(0, "Query " + queryId + " was aborted");
        this.queryId = queryId;
        this.query = query;
    }

    public String getQuery()
    {
        return query;
    }

    public String getQueryId()
    {
        return queryId;
    }
}
