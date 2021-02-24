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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryProgress;

public interface QueryProgressListener
{
    Logger logger = LoggerFactory.getLogger("query-progress");
    QueryProgressListener NOP = queryProgress -> true;
    /**
     * Simple logger that logs the progress to the 'query-progress' logger
     */
    QueryProgressListener LOGGER = p ->
    {
        logger.info("{}", p);
        return true;
    };

    /**
     * Progress callback for the query
     *
     * @param queryProgress The current progress
     * @return true to continue processing the query, false to cancel.
     */
    boolean progress(QueryProgress queryProgress);
}
