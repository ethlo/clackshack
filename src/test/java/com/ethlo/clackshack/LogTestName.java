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

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log the currently running test.
 *
 * <p>Typical usage:
 *
 * <p>{@code @Rule public LogTestName logTestName = new LogTestName();}
 *
 * <p>See also:
 * <br>{@link org.junit.Rule}
 * <br>{@link org.junit.rules.TestWatcher}
 */
public class LogTestName extends TestWatcher
{

    private final static Logger log = LoggerFactory.getLogger("junit.logTestName");

    @Override
    protected void starting(Description description)
    {
        log.debug("Test {}", description.getMethodName());
    }

}
