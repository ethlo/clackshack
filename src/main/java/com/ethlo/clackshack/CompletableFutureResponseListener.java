package com.ethlo.clackshack;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

public class CompletableFutureResponseListener extends BufferingResponseListener
{
    private final CompletableFuture<ContentResponse> completable;

    public CompletableFutureResponseListener(CompletableFuture<ContentResponse> completable)
    {
        this.completable = completable;
    }

    @Override
    public void onComplete(Result result)
    {
        if (result.isFailed())
        {
            completable.completeExceptionally(result.getFailure());
        }
        else
        {
            final HttpContentResponse response =
                    new HttpContentResponse(
                            result.getResponse(),
                            getContent(),
                            getMediaType(),
                            getEncoding()
                    );
            completable.complete(response);
        }
    }
}