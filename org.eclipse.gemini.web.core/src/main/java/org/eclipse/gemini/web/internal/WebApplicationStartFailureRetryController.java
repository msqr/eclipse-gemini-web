/*******************************************************************************
 * Copyright (c) 2009, 2012 VMware Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *   http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.  
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.internal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebApplicationStartFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class WebApplicationStartFailureRetryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationStartFailureRetryController.class);

    private final Object monitor = new Object();

    private final ConcurrentMap<String, Set<StandardWebApplication>> failures = new ConcurrentHashMap<String, Set<StandardWebApplication>>();

    void recordFailure(StandardWebApplication failedWebApplication) {
        String contextPath = failedWebApplication.getContextPath();
        if (contextPath != null) {
            addFailureForWebContextPath(contextPath, failedWebApplication);
        }
    }

    private void addFailureForWebContextPath(String contextPath, StandardWebApplication failedWebApplication) {
        Set<StandardWebApplication> contextFailures = this.failures.get(contextPath);
        if (contextFailures == null) {
            contextFailures = new HashSet<StandardWebApplication>();
            Set<StandardWebApplication> previousContextFailures = this.failures.putIfAbsent(contextPath, contextFailures);
            if (previousContextFailures != null) {
                contextFailures = previousContextFailures;
            }
        }
        synchronized (this.monitor) {
            contextFailures.add(failedWebApplication);
        }
    }

    void retryFailures(StandardWebApplication stoppedWebApplication) {
        String contextPath = stoppedWebApplication.getContextPath();
        if (contextPath != null) {
            Set<StandardWebApplication> contextFailures = removeFailuresForWebContextPath(contextPath);
            contextFailures.remove(stoppedWebApplication);
            for (WebApplication failedWebApplication : contextFailures) {
                try {
                    failedWebApplication.start();
                } catch (WebApplicationStartFailedException _) {
                    // ignore as the web application will have been added to the new contextFailures set
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("", _);
                    }
                }
            }
        }
    }

    private Set<StandardWebApplication> removeFailuresForWebContextPath(String contextPath) {
        Set<StandardWebApplication> sortedContextFailures = createSetSortedByBundleId();
        Set<StandardWebApplication> contextFailures = this.failures.remove(contextPath);
        if (contextFailures != null) {
            sortedContextFailures.addAll(contextFailures);
        }
        return sortedContextFailures;

    }

    private Set<StandardWebApplication> createSetSortedByBundleId() {
        return new TreeSet<StandardWebApplication>(new Comparator<StandardWebApplication>() {

            @Override
            public int compare(StandardWebApplication wa1, StandardWebApplication wa2) {
                long id1 = wa1.getBundle().getBundleId();
                long id2 = wa2.getBundle().getBundleId();
                if (id1 < id2) {
                    return -1;
                } else if (id1 > id2) {
                    return 1;
                } else {
                    return compareHashcode(wa1, wa2);
                }
            }

            private int compareHashcode(StandardWebApplication wa1, StandardWebApplication wa2) {
                long hashcode1 = wa1.hashCode();
                long hashcode2 = wa2.hashCode();
                if (hashcode1 < hashcode2) {
                    return -1;
                } else if (hashcode1 > hashcode2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    void clear() {
        this.failures.clear();
    }

}
