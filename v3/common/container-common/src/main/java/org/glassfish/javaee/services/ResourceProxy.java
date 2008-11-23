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

package org.glassfish.javaee.services;

import org.glassfish.api.naming.NamingObjectProxy;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.Resource;

import java.util.Collection;

@Service
@Scoped(PerLookup.class)
public class ResourceProxy implements NamingObjectProxy {
    @Inject
    protected Habitat connectorRuntimeHabitat;

    @Inject
    private Habitat deployerHabitat;

    private Resource resource = null;
    private Object result = null;
    private String jndiName = null;

    public void setResource(Resource resource){
        this.resource = resource;
    }

    public Object create(Context ic) throws NamingException {
        try{
            if(result == null){
                getResourceDeployer(resource).deployResource(resource);
            }
            result = ic.lookup(jndiName);
        }catch(Exception e){
            throwResourceNotFoundException(e, jndiName);            
        }
        return result;
    }

    protected ConnectorRuntime getConnectorRuntime() {
        return connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    protected Object throwResourceNotFoundException(Exception e, String resourceName) throws NamingException {
        NamingException ne = new NamingException("Unable to lookup resource : " + resourceName);
        ne.initCause(e);
        throw ne;
    }

    protected ResourceDeployer getResourceDeployer(Object resource){
        Collection<ResourceDeployer> deployers = deployerHabitat.getAllByContract(ResourceDeployer.class);

        for(ResourceDeployer deployer : deployers){
            if(deployer.handles(resource)){
                return deployer;
            }
        }
        return null;
    }

}
