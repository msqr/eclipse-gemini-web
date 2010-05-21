/*******************************************************************************
 * Copyright (c) 2009, 2010 VMware Inc.
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

package org.eclipse.gemini.web.tomcat.internal;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TomcatMBeanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatMBeanManager.class);

    private final String domain;

    public TomcatMBeanManager(String domain) {
        this.domain = domain;
    }

    public void start() {
        cleanMBeans();
    }

    public void stop() {
        cleanMBeans();
    }

    private void cleanMBeans() {
        Set<ObjectName> mbeans = findTomcatObjectNames();
        for (ObjectName objectName : mbeans) {
            tryUnregister(objectName);
        }

    }

    private void tryUnregister(ObjectName objectName) {
        try {
            getMBeanServer().unregisterMBean(objectName);
        } catch (InstanceNotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attempted to unregister MBean that was not registered '" + objectName + "'");
            }
        } catch (MBeanRegistrationException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error unregistering MBean '" + objectName + "'", e);
            }
        }
    }

    private Set<ObjectName> findTomcatObjectNames() {
        ObjectName tomcatPattern;
        try {
            tomcatPattern = new ObjectName(this.domain + ":*");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Unable to create query pattern.", e);
        }
        return getMBeanServer().queryNames(tomcatPattern, null);
    }

    private MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

}
