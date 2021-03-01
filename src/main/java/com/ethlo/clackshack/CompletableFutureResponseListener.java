package com.ethlo.clackshack;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2021 Morten Haraldsen (ethlo)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
        super(10 * 1024 * 1024);
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
            completable.complete(new HttpContentResponse(
                    result.getResponse(),
                    getContent(),
                    getMediaType(),
                    getEncoding()
            ));
        }
    }
}
