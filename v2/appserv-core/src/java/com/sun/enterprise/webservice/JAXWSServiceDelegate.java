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
package com.sun.enterprise.webservice;

import java.lang.UnsupportedOperationException;

import java.net.URL;
import java.net.URI;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;

import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.HandlerResolver;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;

/**
 * Used as a delegate to concrete JAXWS 
 * Service implementation.  
 */
public class JAXWSServiceDelegate extends Service {

    private ServiceReferenceDescriptor serviceRef;

    // real service instance
    private Service serviceDelegate;

    private ClassLoader classLoader;

    // location of full wsdl associated with service-ref
    private URL wsdlLocation;

    private boolean fullWsdl = false;
    private boolean noWsdl = false;

    // Service method types
    private static final int ADD_PORT = 1;
    private static final int CREATE_DISPATCH_CLASS = 2;
    private static final int CREATE_DISPATCH_CONTEXT = 3;
    private static final int GET_EXECUTOR = 4;
    private static final int SET_EXECUTOR = 5;
    private static final int GET_HANDLER_RESOLVER = 6;
    private static final int GET_PORT_CONTAINER_MANAGED = 7;
    private static final int GET_PORT_CLIENT_MANAGED = 8;
    private static final int GET_PORTS = 9;
    private static final int GET_SERVICE_NAME = 10;
    private static final int SET_HANDLER_RESOLVER = 11;
    private static final int GET_WSDL_LOCATION = 12;
    private static final int GENERATED_SERVICE_METHOD = 13;

    private static Map serviceMethodTypes;
    private static Set fullWsdlIllegalMethods;
    private static Set noWsdlIllegalMethods;

    static {
        Init();
    }

    public JAXWSServiceDelegate(ServiceReferenceDescriptor descriptor,
                    Service delegate, ClassLoader loader) throws Exception {
        super((new WsUtil()).privilegedGetServiceRefWsdl(descriptor),
                descriptor.getServiceName());
        serviceRef = descriptor;
        serviceDelegate = delegate;
        classLoader = loader;
        if( serviceRef.hasWsdlFile() ) {
            wsdlLocation = (new WsUtil()).privilegedGetServiceRefWsdl(serviceRef);
            fullWsdl = true;
        } else {
            noWsdl = true;
        }
    }

    public void addPort(QName q, String id, String addr) {
        checkUnsupportedMethods(ADD_PORT);
        serviceDelegate.addPort(q, id, addr);
        return;
    }
    
    // TODO : To be implemented
    public <T> Dispatch<T> createDispatch(QName qName, Class<T> aClass, Service.Mode mode) {
        checkUnsupportedMethods(CREATE_DISPATCH_CLASS);
        return null;
    }
                                                                                
    // TODO : To be implemented
    public Dispatch<Object> createDispatch(QName qName, JAXBContext jaxbContext, Service.Mode mode) {
        checkUnsupportedMethods(CREATE_DISPATCH_CONTEXT);
        return null;
    }

    public Executor getExecutor() {
        checkUnsupportedMethods(GET_EXECUTOR);
        return serviceDelegate.getExecutor();
    }
    
    public void setExecutor(Executor obj) {
        checkUnsupportedMethods(SET_EXECUTOR);
        serviceDelegate.setExecutor(obj);
        return;
    }
    
    public HandlerResolver getHandlerResolver() {
        checkUnsupportedMethods(GET_HANDLER_RESOLVER);
        return serviceDelegate.getHandlerResolver();
    }
       
    public Object getPort(QName q, Class sei) {
        checkUnsupportedMethods(GET_PORT_CLIENT_MANAGED);
        return serviceDelegate.getPort(q, sei);
    }
    
    public Object getPort(Class sei) {
        checkUnsupportedMethods(GET_PORT_CONTAINER_MANAGED);
        String serviceEndpointInterface = sei.getName();
        ServiceRefPortInfo portInfo = 
                serviceRef.getPortInfo(serviceEndpointInterface);
        Object retVal;
        if( (portInfo != null) && portInfo.hasWsdlPort() ) {
            retVal = getPort(portInfo.getWsdlPort(), sei);
        } else {
            retVal = serviceDelegate.getPort(sei);
        }
        return retVal;
    }
    
    public Iterator getPorts() {
        checkUnsupportedMethods(GET_PORTS);
        return serviceDelegate.getPorts();
    }
    
    public QName getServiceName() {
        checkUnsupportedMethods(GET_SERVICE_NAME);
        return serviceRef.getServiceName();
    }
    
    public void setHandlerResolver(HandlerResolver resolver) {
        checkUnsupportedMethods(SET_HANDLER_RESOLVER);
        serviceDelegate.setHandlerResolver(resolver);
        return;
    }
    
    public URL getWSDLDocumentLocation() {
        checkUnsupportedMethods(SET_HANDLER_RESOLVER);
        return wsdlLocation;
    }

    /**
     * Convert invocation method to a constant for easier processing.
     */
    private static void Init() {

        serviceMethodTypes     = new HashMap();
        fullWsdlIllegalMethods = new HashSet();
        noWsdlIllegalMethods   = new HashSet();

        try {

            Class noParams[]   = new Class[0];
            Class serviceClass = javax.xml.ws.Service.class;

            //
            // Map Service method to method type.
            //

            Method addPort = serviceClass.getDeclaredMethod
                ("addPort", new Class[] {QName.class, URI.class, String.class});
            serviceMethodTypes.put(addPort, 
                                   new Integer(ADD_PORT));

            Method createDispatchClass = serviceClass.getDeclaredMethod
                ("createDispatch", new Class[] {QName.class, Class.class, Service.Mode.class});
            serviceMethodTypes.put(createDispatchClass, 
                                   new Integer(CREATE_DISPATCH_CLASS));

            Method createDispatchContext = serviceClass.getDeclaredMethod
                ("createDispatch", new Class[] {QName.class, JAXBContext.class, Service.Mode.class});
            serviceMethodTypes.put(createDispatchContext, 
                                   new Integer(CREATE_DISPATCH_CONTEXT));

            Method getExecutor = serviceClass.getDeclaredMethod
                ("getExecutor", noParams);
            serviceMethodTypes.put(getExecutor, 
                                   new Integer(GET_EXECUTOR));

            Method setExecutor = serviceClass.getDeclaredMethod
                ("setExecutor", new Class[] {Executor.class});
            serviceMethodTypes.put(setExecutor, 
                                   new Integer(SET_EXECUTOR));

            Method getHandlerResolver = serviceClass.getDeclaredMethod
                ("getHandlerResolver", noParams);
            serviceMethodTypes.put(getHandlerResolver, 
                                   new Integer(GET_HANDLER_RESOLVER));

            Method getPortContainerManaged = serviceClass.getDeclaredMethod
                ("getPort", new Class[] { Class.class });
            serviceMethodTypes.put(getPortContainerManaged, 
                                   new Integer(GET_PORT_CONTAINER_MANAGED));

            Method getPortClientManaged = serviceClass.getDeclaredMethod
                ("getPort", new Class[] { QName.class, Class.class });
            serviceMethodTypes.put(getPortClientManaged, 
                                   new Integer(GET_PORT_CLIENT_MANAGED));
            
            Method getPorts = serviceClass.getDeclaredMethod
                ("getPorts", noParams);
            serviceMethodTypes.put(getPorts, new Integer(GET_PORTS));

            Method getServiceName = serviceClass.getDeclaredMethod
                ("getServiceName", noParams);
            serviceMethodTypes.put(getServiceName, 
                                   new Integer(GET_SERVICE_NAME));

            Method setHandlerResolver = serviceClass.getDeclaredMethod
                ("setHandlerResolver", new Class[] {HandlerResolver.class});
            serviceMethodTypes.put(setHandlerResolver, 
                                   new Integer(SET_HANDLER_RESOLVER));

            Method getWsdlLocation = serviceClass.getDeclaredMethod
                ("getWSDLDocumentLocation", noParams);
            serviceMethodTypes.put(getWsdlLocation,
                                   new Integer(GET_WSDL_LOCATION));
        } catch(NoSuchMethodException nsme) {}

        noWsdlIllegalMethods.add(new Integer(GET_PORT_CONTAINER_MANAGED));
        noWsdlIllegalMethods.add(new Integer(GET_PORT_CLIENT_MANAGED));
        noWsdlIllegalMethods.add(new Integer(GET_PORTS));
        noWsdlIllegalMethods.add(new Integer(GET_SERVICE_NAME));
        noWsdlIllegalMethods.add(new Integer(GET_WSDL_LOCATION));

        // This case shouldn't happen since if service-ref has generated
        // service and no WSDL it won't get past deployment, but it's here
        // for completeness.
        noWsdlIllegalMethods.add(new Integer(GENERATED_SERVICE_METHOD));
    }

    private void checkUnsupportedMethods(int methodType) 
        throws UnsupportedOperationException {

        Set illegalMethods = fullWsdl ?
            fullWsdlIllegalMethods : noWsdlIllegalMethods;

        if( illegalMethods.contains(new Integer(methodType)) ) {
            throw new UnsupportedOperationException();
        }

        return;
    }        
}
