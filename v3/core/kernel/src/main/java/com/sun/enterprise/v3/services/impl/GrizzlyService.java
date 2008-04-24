/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.Startup;
import org.glassfish.api.deployment.ApplicationContainer;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.StringUtils;
import com.sun.grizzly.Controller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Network Service is responsible for starting grizzly and register the
 * top level proxy. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints proxy to
 * particular context root.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class GrizzlyService implements Startup, PostConstruct, PreDestroy {

    @Inject(name="server-config") // for now
    Config config;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;

    List<NetworkProxy> proxies = new ArrayList<NetworkProxy>();
    
    private final Controller controller  = new Controller();

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;                
    }


    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        HttpService httpService = config.getHttpService();
        try {
            for (HttpListener listener : httpService.getHttpListener()) {
                // create the proxy for the port.
                NetworkProxy proxy = new GrizzlyProxy(logger, habitat,
                        listener, controller, httpService);
                proxy.setVsMapper(new VirtualHostMapper(logger, listener));

                // attach all virtual servers to this port
                for (VirtualServer vs : httpService.getVirtualServer()) {
                    List<String> vsListeners = StringUtils.parseStringList(vs.getHttpListeners(), " ,");
                    if (vsListeners == null || vsListeners.size() == 0 || 
                            vsListeners.contains(listener.getId())) {
                        proxy.getVsMapper().addVirtualServer(vs);
                    }
                }
                proxy.start();

                // add the new proxy to our list of proxies.
                proxies.add(proxy);
            }
            
            // todo : this neeed some rework...
            // now register all proxies you can find out there !
            // TODO : so far these qets registered everywhere, maybe not the right thing ;-)
            for (org.glassfish.api.container.Adapter subAdapter : habitat.getAllByContract(org.glassfish.api.container.Adapter.class)) {
                //@TODO change EndportRegistrationException processing if required
                try {
                    registerEndpoint(subAdapter.getContextRoot(), null, 
                            subAdapter, null);
                } catch(EndpointRegistrationException e) {
                    logger.log(Level.WARNING, 
                            "GrizzlyService endpoint registration problem", e);
                }
            }
        } catch(RuntimeException e) { // So far postConstruct can not throw any other exception type
            logger.log(Level.WARNING, "Closing initialized network proxies");
            for(NetworkProxy proxy : proxies) {
                try {
                    proxy.stop();
                } catch(Exception proxyStopException) {
                    logger.log(Level.SEVERE, "Stop network proxy error ", proxyStopException);
                }
            }
            
            throw e;
        }
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        for (NetworkProxy proxy : proxies) {
            proxy.stop();
        }
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {

        registerEndpoint(contextRoot, null, endpointAdapter, container);
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
            com.sun.grizzly.tcp.Adapter endpointAdapter,
            ApplicationContainer container) throws EndpointRegistrationException {

        for (NetworkProxy proxy : proxies) {
            proxy.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
        }
    }


    /**
     * Registers a new endpoint for the given context root at the given port
     * number.
     */
    public void registerEndpoint(String contextRoot,
                                 int port,
                                 Collection<String> vsServers,
                                 com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            if (proxy.getPort() == port) {
                proxy.registerEndpoint(contextRoot, vsServers,
                                       endpointAdapter, container);
            }
        }
    }


    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot) throws EndpointRegistrationException {
        unregisterEndpoint(contextRoot, null);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, 
            ApplicationContainer app) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            proxy.unregisterEndpoint(contextRoot, app);
        }
    }    
}
