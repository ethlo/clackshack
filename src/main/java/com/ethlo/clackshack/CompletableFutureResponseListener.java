package com.ethlo.clackshack;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2017 - 2021 Morten Haraldsen (ethlo)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
