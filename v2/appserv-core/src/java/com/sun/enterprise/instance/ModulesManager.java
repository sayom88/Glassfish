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

package com.sun.enterprise.instance;

import java.util.Map;
import java.util.HashMap;

import com.sun.enterprise.config.ConfigContext;
import  com.sun.enterprise.deployment.backend.DeployableObjectInfo;
import  com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.PropertyResolver;

/** Base-class for all the different types of ModulesManager's.
 *  Put common code that applies to all types of modules in here...
 **/

public abstract class ModulesManager extends BaseManager {
    protected ModulesManager(InstanceEnvironment env, boolean useBackupServerXml) throws ConfigException {
        super(env, useBackupServerXml);
    }
    
    /**
     * @return the registered descriptors map
     */
    public Map getRegisteredDescriptors() {
                
        if (modules==null) {
            synchronized (ModulesManager.class) {
                if (modules==null) {
                    modules = new HashMap();
                }
            }
        } 
        return modules;
    }
    
    protected String resolvePath(String path) {
        String resolved = path;
        try {
            resolved =  new PropertyResolver(configContext, 
                getInstanceEnvironment().getName()).resolve(path);
        } catch (ConfigException ce) {
            //log this.
        }
        return resolved;
    }

    private static Map modules = null;
    
}
