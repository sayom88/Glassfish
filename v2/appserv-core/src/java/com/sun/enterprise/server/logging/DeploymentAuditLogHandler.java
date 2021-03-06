/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.server.logging;

import com.sun.logging.LogDomains;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes deployment audit records.
 * <p>
 * This class extends FileandSyslogHandler because it does nearly the same
 * function, except to a separate deployment audit file.
 */
public class DeploymentAuditLogHandler extends FileandSyslogHandler {
    
    private static final String CONFIGURED_LEVEL_PROPERTY_NAME = "com.sun.aas.deployment.audit.level";
    private static final String CONFIGURED_LEVEL_PROPERTY_DEFAULT = "OFF";
    
    private static final DeploymentAuditLogHandler thisInstance = 
        new DeploymentAuditLogHandler( );

    private String logFileName = "deployment.log"; 

    public static synchronized DeploymentAuditLogHandler getInstance( ) {
        return thisInstance;
    }

    /**
     * Creates a new instance of DeploymentAuditLogHandler, bypassing
     * logic in the superclass constructor that creates the AMXLoggingHook.
     */
    protected DeploymentAuditLogHandler() {
    }
    
    protected AMXLoggingHook createAMXLoggingHook() {
        return null;
    }

    protected String getLogFileName() {
        return logFileName;
    }
    
    /**
     *Returns the logging Level setting for the deployment auditing logger.
     *<p>
     *This method uses a system property rather than a normal logging level 
     *setting from domain.xml to avoid changing the domain.xml layout (due to schedule issues).
     *Later on this could be converted, time permitting.
     *@return the logging Level to be used for deployment logging; default if OFF
     */
    public static Level getConfiguredLevel() {
        Level result = Level.OFF;
        try {
            result = Level.parse(System.getProperty(CONFIGURED_LEVEL_PROPERTY_NAME, CONFIGURED_LEVEL_PROPERTY_DEFAULT));
        } catch (Throwable thr) {
            // Use the default OFF value but log the problem.
            Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);
            logger.log(Level.WARNING, thr.getLocalizedMessage(), thr);
        }
        return result;
    }
}
