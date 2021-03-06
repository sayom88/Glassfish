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

package com.sun.enterprise.resource;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.common.constant.AdminConstants;

import javax.management.Attribute;
import javax.management.AttributeList;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

import static com.sun.enterprise.resource.ResourceConstants.*;
import org.xml.sax.ext.LexicalHandler;

/**
 * This Class reads the Properties (resources) from the xml file supplied 
 * to constructor
 */
public class ResourcesXMLParser implements EntityResolver
{

    private File resourceFile = null;
    private Document document;
    private List<Resource> vResources;
    private boolean isDoctypePresent = false;
    /* list of resources that needs to be created prior to module deployment. This 
     * includes all non-Connector resources and resource-adapter-config
     */
//    private List<Resource> connectorResources;
    
    /* Includes all connector resources in the order in which the resources needs
      to be created */
//    private List<Resource> nonConnectorResources;
    
    // i18n StringManager
    private static StringManager localStrings =
        StringManager.getManager( ResourcesXMLParser.class );
    
    private static final int NONCONNECTOR = 2;
    private static final int CONNECTOR = 1;

    /** Creates new ResourcesXMLParser */
    public ResourcesXMLParser(String resourceFileName) throws Exception {
        resourceFile = new File(resourceFileName);
        initProperties();
        vResources = new ArrayList<Resource>();
        generateResourceObjects();
    }

    /**
     *Parse the XML Properties file and populate it into document object
     */
    private void initProperties() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            AddResourcesErrorHandler  errorHandler = new AddResourcesErrorHandler();
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(this);
            builder.setErrorHandler(errorHandler);
            if (resourceFile == null) {
		String msg = localStrings.getString( "admin.server.core.mbean.config.no_resource_file" );
                throw new Exception( msg );
            }
            InputSource is = new InputSource(resourceFile.toURI().toString());
            document = builder.parse(is);
        }/*catch(SAXParseException saxpe){
            throw new Exception(saxpe.getLocalizedMessage());
        }*/catch (SAXException sxe) {
            //This check is introduced to check if DOCTYPE is present in sun-resources.xml
            //And throw proper error message if DOCTYPE is missing
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                sp.setProperty("http://xml.org/sax/properties/lexical-handler", new MyLexicalHandler());
                sp.getXMLReader().parse(new InputSource(resourceFile.toString()));
            } catch (ParserConfigurationException ex) {
            } catch (SAXException ex) {
            } catch (IOException ex) {
            }
            if(!isDoctypePresent){
                throw new Exception(localStrings.getString("doctype.not.present.xml", resourceFile.toString()));
            }
            Exception  x = sxe;
            if (sxe.getException() != null)
               x = sxe.getException();
            throw new Exception(x.getLocalizedMessage());
        }
        catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            throw new Exception(pce.getLocalizedMessage());
        }
        catch (IOException ioe) {
            throw new Exception(ioe.getLocalizedMessage());
        }
    }
    
    /**
     * Get All the resources from the document object.
     *
     */
    private void generateResourceObjects() throws Exception
    {
        if (document != null) {
            for (Node nextKid = document.getDocumentElement().getFirstChild();
                    nextKid != null; nextKid = nextKid.getNextSibling()) 
            {
                String nodeName = nextKid.getNodeName();
                if (nodeName.equalsIgnoreCase(Resource.CUSTOM_RESOURCE)) {
                    generateCustomResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.EXTERNAL_JNDI_RESOURCE)) 
                {
                    generateJNDIResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.JDBC_RESOURCE)) 
                {
                    generateJDBCResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.JDBC_CONNECTION_POOL)) 
                {
                    generateJDBCConnectionPoolResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.MAIL_RESOURCE)) 
                {
                    generateMailResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE)) 
                {
                    generatePersistenceResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.ADMIN_OBJECT_RESOURCE)) 
                {
                    generateAdminObjectResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.CONNECTOR_RESOURCE)) 
                {
                    generateConnectorResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.CONNECTOR_CONNECTION_POOL)) 
                {
                    generateConnectorConnectionPoolResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(Resource.RESOURCE_ADAPTER_CONFIG)) 
                {
                    generateResourceAdapterConfig(nextKid);
                }
            }
        }
    }
    
    /**
     * Sorts the resources defined in the resources configuration xml file into
     * two buckets
     *  a) list of resources that needs to be created prior to
     *  module deployment. This includes all non-Connector resources
     *  and resource-adapter-config
     *  b) includes all connector resources in the order in which the resources needs
     *  to be created
     *  and returns the requested resources bucker to the caller.
     *   
     *  @param resources Resources list as defined in sun-resources.xml
     *  @param type Specified either ResourcesXMLParser.CONNECTOR or 
     *  ResourcesXMLParser.NONCONNECTOR to indicate the type of 
     *  resources are needed by the client of the ResourcesXMLParser
     *  @param isResourceCreation During the resource Creation Phase, RA configs are
     *  added to the nonConnector resources list so that they can be created in the
     *  <code>PreResCreationPhase</code>. When the isResourceCreation is false, the 
     *  RA config resuorces are added to the Connector Resources list, so that they 
     *  can be deleted as all other connector resources in the 
     *  <code>PreResDeletionPhase</code>
     */
    private static List<Resource> getResourcesOfType(List<Resource> resources, 
                                            int type, boolean isResourceCreation) {
        List<Resource> nonConnectorResources = new ArrayList<Resource>();
        List<Resource> connectorResources = new ArrayList<Resource>();
        
        for (Resource res : resources) {
            if (isConnectorResource(res)) {
                if (res.getType().equals(Resource.RESOURCE_ADAPTER_CONFIG)) {
                    if(isResourceCreation) {
                        //RA config is considered as a nonConnector Resource, 
                        //during sun-resources.xml resource-creation phase, so that 
                        //it could be created before the RAR is deployed.
                        nonConnectorResources.add(res);
                    } else {
                        connectorResources.add(res);
                    }
                } else {
                    connectorResources.add(res);
                }
            } else {
                nonConnectorResources.add(res);
            }
        }
        
        List<Resource> finalSortedConnectorList = sortConnectorResources(connectorResources);
        List<Resource> finalSortedNonConnectorList = sortNonConnectorResources(nonConnectorResources);
        if (type == CONNECTOR) {
            return finalSortedConnectorList;
        } else {
            return finalSortedNonConnectorList;
        }
    }

    /**
     * Sort connector resources
     * Resource Adater configs are added first.
     * Pools are then added to the list, so that the conncetion
     * pools can be created prior to any other connector resource defined
     * in the resources configuration xml file.
     * @param connectorResources List of Resources to be sorted.
     * @return The sorted list.
     */
    private static List<Resource> sortConnectorResources(List<Resource> connectorResources) {
        List<Resource> raconfigs = new ArrayList<Resource>();
        List<Resource> ccps = new ArrayList<Resource>();
        List<Resource> others = new ArrayList<Resource>();
        
        List<Resource> finalSortedConnectorList = new ArrayList<Resource>();
        
        for (Resource resource : connectorResources) {
            if (resource.getType().equals(Resource.RESOURCE_ADAPTER_CONFIG)){
                raconfigs.add(resource);
            } else if (resource.getType().equals(Resource.CONNECTOR_CONNECTION_POOL)) {
                ccps.add(resource);
            } else {
                others.add(resource);
            }
        }
        
        finalSortedConnectorList.addAll(raconfigs);
        finalSortedConnectorList.addAll(ccps);
        finalSortedConnectorList.addAll(others);
        return finalSortedConnectorList;
    }
    
    /**
     * Sort non connector resources
     * JDBC Pools are added first to the list, so that the conncetion
     * pools can be created prior to any other jdbc resource defined
     * in the resources configuration xml file.
     * @param nonConnectorResources List of Resources to be sorted.
     * @return The sorted list.
     */
    private static List<Resource> sortNonConnectorResources(List<Resource> nonConnectorResources) {
        List<Resource> jdbccps = new ArrayList<Resource>();
        List<Resource> pmfs = new ArrayList<Resource>();
        List<Resource> others = new ArrayList<Resource>();
        
        List<Resource> finalSortedNonConnectorList = new ArrayList<Resource>();
        
        for (Resource resource : nonConnectorResources) {
            if(resource.getType().equals(Resource.JDBC_CONNECTION_POOL)) {
                jdbccps.add(resource);
            } else if(resource.getType().equals(Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE)) {
                pmfs.add(resource);
            } else {
                others.add(resource);
            }
        }
        
        finalSortedNonConnectorList.addAll(jdbccps);
        finalSortedNonConnectorList.addAll(others);
        finalSortedNonConnectorList.addAll(pmfs);
        return finalSortedNonConnectorList;
    }
    
    /**
     * Determines if the passed in <code>Resource</code> is a connector
     * resource. A connector resource is either a connector connection pool or a
     * connector resource, security map, ra config or an admin object
     * 
     * @param res Resource that needs to be tested
     * @return
     */
    private static boolean isConnectorResource(Resource res) {
        String type = res.getType();
        return (    
                    (type.equals(Resource.ADMIN_OBJECT_RESOURCE)) ||
                    (type.equals(Resource.CONNECTOR_CONNECTION_POOL)) ||
                    (type.equals(Resource.CONNECTOR_RESOURCE)) ||
                    (type.equals(Resource.CONNECTOR_SECURITY_MAP)) ||
                    (type.equals(Resource.RESOURCE_ADAPTER_CONFIG)) 
            );
    }

    /*
     * Generate the Custom resource
     */
    private void generateCustomResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();
        
        Node enabledNode = attributes.getNamedItem(ENABLED);
        
        Resource customResource = new Resource(Resource.CUSTOM_RESOURCE);
        customResource.setAttribute(JNDI_NAME, jndiName);
        customResource.setAttribute(RES_TYPE, resType);
        customResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           customResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(customResource, children);
        vResources.add(customResource);
        
        //debug strings
        printResourceElements(customResource);
    }
    
    /*
     * Generate the JNDI resource
     */
    private void generateJNDIResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node jndiLookupNode = attributes.getNamedItem(JNDI_LOOKUP);
        String jndiLookup = jndiLookupNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);
        
        Resource jndiResource = new Resource(Resource.EXTERNAL_JNDI_RESOURCE);
        jndiResource.setAttribute(JNDI_NAME, jndiName);
        jndiResource.setAttribute(JNDI_LOOKUP, jndiLookup);
        jndiResource.setAttribute(RES_TYPE, resType);
        jndiResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           jndiResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jndiResource, children);
        vResources.add(jndiResource);
        
        //debug strings
        printResourceElements(jndiResource);
    }
    
    /*
     * Generate the JDBC resource
     */
    private void generateJDBCResource(Node nextKid) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node poolNameNode = attributes.getNamedItem(POOL_NAME);
        String poolName = poolNameNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource jdbcResource = new Resource(Resource.JDBC_RESOURCE);
        jdbcResource.setAttribute(JNDI_NAME, jndiName);
        jdbcResource.setAttribute(POOL_NAME, poolName);
        if (enabledNode != null) {
           String enabledName = enabledNode.getNodeValue();
           jdbcResource.setAttribute(ENABLED, enabledName);
        }
        
        NodeList children = nextKid.getChildNodes();
        //get description
        if (children != null) 
        {
            for (int ii=0; ii<children.getLength(); ii++) 
            {
                if (children.item(ii).getNodeName().equals("description")) {
                    if (children.item(ii).getFirstChild() != null) {
                        jdbcResource.setDescription(
                            children.item(ii).getFirstChild().getNodeValue());
                    } 
                }
            }
        }

        vResources.add(jdbcResource);
        
        //debug strings
        printResourceElements(jdbcResource);
    }
    
    /*
     * Generate the JDBC Connection pool Resource
     */
    private void generateJDBCConnectionPoolResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node nameNode = attributes.getNamedItem(CONNECTION_POOL_NAME);
        String name = nameNode.getNodeValue();
        Node nSteadyPoolSizeNode = attributes.getNamedItem(STEADY_POOL_SIZE);
        Node nMaxPoolSizeNode = attributes.getNamedItem(MAX_POOL_SIZE);
        Node nMaxWaitTimeInMillisNode  = 
             attributes.getNamedItem(MAX_WAIT_TIME_IN_MILLIS);
        Node nPoolSizeQuantityNode  = 
             attributes.getNamedItem(POOL_SIZE_QUANTITY);
        Node nIdleTimeoutInSecNode  = 
             attributes.getNamedItem(IDLE_TIME_OUT_IN_SECONDS);
        Node nIsConnectionValidationRequiredNode  = 
             attributes.getNamedItem(IS_CONNECTION_VALIDATION_REQUIRED);
        Node nConnectionValidationMethodNode  = 
             attributes.getNamedItem(CONNECTION_VALIDATION_METHOD);
        Node nFailAllConnectionsNode  = 
             attributes.getNamedItem(FAIL_ALL_CONNECTIONS);
        Node nValidationTableNameNode  = 
             attributes.getNamedItem(VALIDATION_TABLE_NAME);
        Node nResType  = attributes.getNamedItem(RES_TYPE);
        Node nTransIsolationLevel  = 
             attributes.getNamedItem(TRANS_ISOLATION_LEVEL);
        Node nIsIsolationLevelQuaranteed  = 
             attributes.getNamedItem(IS_ISOLATION_LEVEL_GUARANTEED);
        Node datasourceNode = attributes.getNamedItem(DATASOURCE_CLASS);
        String datasource = datasourceNode.getNodeValue();
        
        Resource jdbcResource = new Resource(Resource.JDBC_CONNECTION_POOL);
        jdbcResource.setAttribute(CONNECTION_POOL_NAME, name);
        jdbcResource.setAttribute(DATASOURCE_CLASS, datasource);
        if (nSteadyPoolSizeNode != null) {
           String sSteadyPoolSize = nSteadyPoolSizeNode.getNodeValue();
           jdbcResource.setAttribute(STEADY_POOL_SIZE, sSteadyPoolSize);
        }
        if (nMaxPoolSizeNode != null) {
           String sMaxPoolSize = nMaxPoolSizeNode.getNodeValue();
           jdbcResource.setAttribute(MAX_POOL_SIZE, sMaxPoolSize);
        }
        if (nMaxWaitTimeInMillisNode != null) {
           String sMaxWaitTimeInMillis = nMaxWaitTimeInMillisNode.getNodeValue();
           jdbcResource.setAttribute(MAX_WAIT_TIME_IN_MILLIS, sMaxWaitTimeInMillis);
        }
        if (nPoolSizeQuantityNode != null) {
           String sPoolSizeQuantity = nPoolSizeQuantityNode.getNodeValue();
           jdbcResource.setAttribute(POOL_SIZE_QUANTITY, sPoolSizeQuantity);
        }
        if (nIdleTimeoutInSecNode != null) {
           String sIdleTimeoutInSec = nIdleTimeoutInSecNode.getNodeValue();
           jdbcResource.setAttribute(IDLE_TIME_OUT_IN_SECONDS, sIdleTimeoutInSec);
        }
        if (nIsConnectionValidationRequiredNode != null) {
           String sIsConnectionValidationRequired = nIsConnectionValidationRequiredNode.getNodeValue();
           jdbcResource.setAttribute(IS_CONNECTION_VALIDATION_REQUIRED, sIsConnectionValidationRequired);
        }
        if (nConnectionValidationMethodNode != null) {
           String sConnectionValidationMethod = nConnectionValidationMethodNode.getNodeValue();
           jdbcResource.setAttribute(CONNECTION_VALIDATION_METHOD, sConnectionValidationMethod);
        }
        if (nFailAllConnectionsNode != null) {
           String sFailAllConnection = nFailAllConnectionsNode.getNodeValue();
           jdbcResource.setAttribute(FAIL_ALL_CONNECTIONS, sFailAllConnection);
        }
        if (nValidationTableNameNode != null) {
           String sValidationTableName = nValidationTableNameNode.getNodeValue();
           jdbcResource.setAttribute(VALIDATION_TABLE_NAME, sValidationTableName);
        }
        if (nResType != null) {
           String sResType = nResType.getNodeValue();
           jdbcResource.setAttribute(RES_TYPE, sResType);
        }
        if (nTransIsolationLevel != null) {
           String sTransIsolationLevel = nTransIsolationLevel.getNodeValue();
           jdbcResource.setAttribute(TRANS_ISOLATION_LEVEL, sTransIsolationLevel);
        }
        if (nIsIsolationLevelQuaranteed != null) {
           String sIsIsolationLevelQuaranteed = 
                  nIsIsolationLevelQuaranteed.getNodeValue();
           jdbcResource.setAttribute(IS_ISOLATION_LEVEL_GUARANTEED, 
                                     sIsIsolationLevelQuaranteed);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jdbcResource, children);
        vResources.add(jdbcResource);
        
        //debug strings
        printResourceElements(jdbcResource);
    }
    
    /*
     * Generate the Mail resource
     */
    private void generateMailResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        Node hostNode   = attributes.getNamedItem(MAIL_HOST);
        Node userNode   = attributes.getNamedItem(MAIL_USER);
        Node fromAddressNode   = attributes.getNamedItem(MAIL_FROM_ADDRESS);
        Node storeProtoNode   = attributes.getNamedItem(MAIL_STORE_PROTO);
        Node storeProtoClassNode   = attributes.getNamedItem(MAIL_STORE_PROTO_CLASS);
        Node transProtoNode   = attributes.getNamedItem(MAIL_TRANS_PROTO);
        Node transProtoClassNode   = attributes.getNamedItem(MAIL_TRANS_PROTO_CLASS);
        Node debugNode   = attributes.getNamedItem(MAIL_DEBUG);
        Node enabledNode   = attributes.getNamedItem(ENABLED);

        String jndiName = jndiNameNode.getNodeValue();
        String host     = hostNode.getNodeValue();
        String user     = userNode.getNodeValue();
        String fromAddress = fromAddressNode.getNodeValue();
        
        Resource mailResource = new Resource(Resource.MAIL_RESOURCE);

        mailResource.setAttribute(JNDI_NAME, jndiName);
        mailResource.setAttribute(MAIL_HOST, host);
        mailResource.setAttribute(MAIL_USER, user);
        mailResource.setAttribute(MAIL_FROM_ADDRESS, fromAddress);
        if (storeProtoNode != null) {
           String sStoreProto = storeProtoNode.getNodeValue();
           mailResource.setAttribute(MAIL_STORE_PROTO, sStoreProto);
        }
        if (storeProtoClassNode != null) {
           String sStoreProtoClass = storeProtoClassNode.getNodeValue();
           mailResource.setAttribute(MAIL_STORE_PROTO_CLASS, sStoreProtoClass);
        }
        if (transProtoNode != null) {
           String sTransProto = transProtoNode.getNodeValue();
           mailResource.setAttribute(MAIL_TRANS_PROTO, sTransProto);
        }
        if (transProtoClassNode != null) {
           String sTransProtoClass = transProtoClassNode.getNodeValue();
           mailResource.setAttribute(MAIL_TRANS_PROTO_CLASS, sTransProtoClass);
        }
        if (debugNode != null) {
           String sDebug = debugNode.getNodeValue();
           mailResource.setAttribute(MAIL_DEBUG, sDebug);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           mailResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(mailResource, children);
        vResources.add(mailResource);
        
        //debug strings
        printResourceElements(mailResource);
    }
    
    /*
     * Generate the Persistence Factory Manager resource
     */
    private void generatePersistenceResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        Node poolNameNode = attributes.getNamedItem(JDBC_RESOURCE_JNDI_NAME);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource persistenceResource = 
                    new Resource(Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE);
        persistenceResource.setAttribute(JNDI_NAME, jndiName);
        if (factoryClassNode != null) {
           String factoryClass = factoryClassNode.getNodeValue();
           persistenceResource.setAttribute(FACTORY_CLASS, factoryClass);
        }
        if (poolNameNode != null) {
           String poolName = poolNameNode.getNodeValue();
           persistenceResource.setAttribute(JDBC_RESOURCE_JNDI_NAME, poolName);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           persistenceResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(persistenceResource, children);
        vResources.add(persistenceResource);
        
        //debug strings
        printResourceElements(persistenceResource);
    }

    /*
     * Generate the Admin Object resource
     */
    private void generateAdminObjectResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node resAdapterNode = attributes.getNamedItem(RES_ADAPTER);
        String resAdapter = resAdapterNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource adminObjectResource = 
                    new Resource(Resource.ADMIN_OBJECT_RESOURCE);
        adminObjectResource.setAttribute(JNDI_NAME, jndiName);
        adminObjectResource.setAttribute(RES_TYPE, resType);
        adminObjectResource.setAttribute(RES_ADAPTER, resAdapter);

        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           adminObjectResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(adminObjectResource, children);
        vResources.add(adminObjectResource);
        
        //debug strings
        printResourceElements(adminObjectResource);
    }

    /*
     * Generate the Connector resource
     */
    private void generateConnectorResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node poolNameNode = attributes.getNamedItem(POOL_NAME);
        String poolName = poolNameNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RESOURCE_TYPE);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource connectorResource = 
                    new Resource(Resource.CONNECTOR_RESOURCE);
        connectorResource.setAttribute(JNDI_NAME, jndiName);
        connectorResource.setAttribute(POOL_NAME, poolName);
        if (resTypeNode != null) {
           String resType = resTypeNode.getNodeValue();
           connectorResource.setAttribute(RESOURCE_TYPE, resType);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           connectorResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(connectorResource, children);
        vResources.add(connectorResource);
        
        //debug strings
        printResourceElements(connectorResource);
    }

    private void generatePropertyElement(Resource rs, NodeList children) throws Exception 
    {
        if (children != null) {
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeName().equals("property")) {
                    NamedNodeMap attNodeMap = children.item(i).getAttributes();
                    Node nameNode = attNodeMap.getNamedItem("name");
                    Node valueNode = attNodeMap.getNamedItem("value");
                    if (nameNode != null && valueNode != null) {
                        boolean bDescFound = false;
                        String sName = nameNode.getNodeValue();
                        String sValue = valueNode.getNodeValue();
                        //get property description
                        // FIX ME: Ignoring the value for description for the 
                        // time-being as there is no method available in 
                        // configMBean to set description for a property
                        Node descNode = children.item(i).getFirstChild();
                        while (descNode != null && !bDescFound) {
                            if (descNode.getNodeName().equalsIgnoreCase("description")) {
                                try {
                                    //rs.setElementProperty(sName, sValue, descNode.getFirstChild().getNodeValue());
                                    rs.setProperty(sName, sValue);
                                    bDescFound = true;
                                }
                                catch (DOMException dome) {
                                    // DOM Error
                                    throw new Exception(dome.getLocalizedMessage());
                                }
                            }
                            descNode = descNode.getNextSibling();
                        }
                        if (!bDescFound) {
                            rs.setProperty(sName, sValue);
                        }
                    }
                }
                if (children.item(i).getNodeName().equals("description")) {
                    Node descNode = children.item(i).getFirstChild();
                    if(descNode != null){
                        rs.setDescription(descNode.getNodeValue());
                    }
                }
            }
        }
    }
    
    /*
     * Generate the Connector Connection pool Resource
     */
    private void generateConnectorConnectionPoolResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return ;
        
        Node nameNode 
            = attributes.getNamedItem(CONNECTOR_CONNECTION_POOL_NAME);
        Node raConfigNode
            = attributes.getNamedItem(RESOURCE_ADAPTER_CONFIG_NAME);
        Node conDefNode
            = attributes.getNamedItem(CONN_DEF_NAME);
        Node steadyPoolSizeNode
            = attributes.getNamedItem(CONN_STEADY_POOL_SIZE);
        Node maxPoolSizeNode 
            =  attributes.getNamedItem(CONN_MAX_POOL_SIZE);
        Node poolResizeNode
            = attributes.getNamedItem(CONN_POOL_RESIZE_QUANTITY);
        Node idleTimeOutNode
            = attributes.getNamedItem(CONN_IDLE_TIME_OUT);
        Node failAllConnNode
            = attributes.getNamedItem(CONN_FAIL_ALL_CONNECTIONS);
        
        String poolName = null;
        
        Resource connectorConnPoolResource = new Resource(Resource.CONNECTOR_CONNECTION_POOL);
        if(nameNode != null){
            poolName = nameNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONNECTION_POOL_NAME, poolName);
        }    
       if(raConfigNode != null){
            String raConfig = raConfigNode.getNodeValue();
            connectorConnPoolResource.setAttribute(RESOURCE_ADAPTER_CONFIG_NAME,raConfig);
        }
        if(conDefNode != null){
            String conDef = conDefNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_DEF_NAME,conDef);
        }
        if(steadyPoolSizeNode != null){
            String steadyPoolSize = steadyPoolSizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_STEADY_POOL_SIZE,steadyPoolSize);
        }
        if(maxPoolSizeNode != null){
            String maxPoolSize = maxPoolSizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_MAX_POOL_SIZE,maxPoolSize);
        }
        if(poolResizeNode != null){
            String poolResize = poolResizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_POOL_RESIZE_QUANTITY,poolResize);
        }
        if(idleTimeOutNode != null){
            String idleTimeOut = idleTimeOutNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_IDLE_TIME_OUT,idleTimeOut);
        }
        if(failAllConnNode != null){
            String failAllConn = failAllConnNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_FAIL_ALL_CONNECTIONS,failAllConn);
        }
                     
        NodeList children = nextKid.getChildNodes();
        //get description
        generatePropertyElement(connectorConnPoolResource, children);
        
        vResources.add(connectorConnPoolResource);
        // with the given poolname ..create security-map
        if (children != null){
            for (int i=0; i<children.getLength(); i++) {
                if((children.item(i).getNodeName().equals(SECURITY_MAP)))
                    generateSecurityMap(poolName,children.item(i));
                    
            }
        }
        
        
        
        //debug strings
        printResourceElements(connectorConnPoolResource);
    }
    
    private void generateSecurityMap(String poolName,Node mapNode) throws Exception{
        
        NamedNodeMap attributes = mapNode.getAttributes();
        if (attributes == null)
            return ;
        Node nameNode 
            = attributes.getNamedItem(SECURITY_MAP_NAME);
               
              
        Resource map = new Resource(Resource.CONNECTOR_SECURITY_MAP);
        if(nameNode != null){
            String name = nameNode.getNodeValue();
            map.setAttribute(SECURITY_MAP_NAME, name);
        }
        if(poolName != null)
           map.setAttribute(POOL_NAME,poolName);
        
        StringBuffer principal = new StringBuffer();
        StringBuffer usergroup = new StringBuffer();
        
        NodeList children = mapNode.getChildNodes();
        
        if(children != null){
            for (int i=0; i<children.getLength(); i++){
                Node gChild = children.item(i);
                String strNodeName = gChild.getNodeName();
                if(strNodeName.equals(PRINCIPAL)){
                    String p = (gChild.getFirstChild()).getNodeValue();
                    principal.append(p).append(",");
                }
                if(strNodeName.equals(USERGROUP)){
                    String u = (gChild.getFirstChild()).getNodeValue();
                    usergroup.append(u).append(",");
                }
                if((strNodeName.equals(BACKEND_PRINCIPAL))){
                    NamedNodeMap attributes1 = (children.item(i)).getAttributes();    
                    if(attributes1 != null){
                        Node userNode = attributes1.getNamedItem(USER_NAME);
                        if(userNode != null){
                            String userName =userNode.getNodeValue();
                            map.setAttribute(USER_NAME,userName);
                        }
                        Node passwordNode = attributes1.getNamedItem(PASSWORD);
                        if(passwordNode != null){
                            String pwd = passwordNode.getNodeValue();
                            map.setAttribute(PASSWORD,pwd);
                        }
                    }
                }
            }
        }
            map.setAttribute(PRINCIPAL,convertToStringArray(principal.toString()));
            map.setAttribute("user_group",convertToStringArray(usergroup.toString()));
       vResources.add(map);
    }//end of generateSecurityMap....     
   
    
    /*
     * Generate the Resource Adapter Config
     *
     */
       
     
    private void generateResourceAdapterConfig(Node nextKid) throws Exception
    { NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Resource resAdapterConfigResource = new Resource(Resource.RESOURCE_ADAPTER_CONFIG);
        Node resAdapConfigNode = attributes.getNamedItem(RES_ADAPTER_CONFIG);
        if(resAdapConfigNode != null){
            String resAdapConfig = resAdapConfigNode.getNodeValue();
            resAdapterConfigResource.setAttribute(RES_ADAPTER_CONFIG,resAdapConfig);
        }
        Node poolIdNode = attributes.getNamedItem(THREAD_POOL_IDS);
        if(poolIdNode != null){
            String threadPoolId = poolIdNode.getNodeValue();
            resAdapterConfigResource.setAttribute(THREAD_POOL_IDS,threadPoolId);
        }
        Node resAdapNameNode = attributes.getNamedItem(RES_ADAPTER_NAME);
        if(resAdapNameNode != null){
            String resAdapName = resAdapNameNode.getNodeValue();
            resAdapterConfigResource.setAttribute(RES_ADAPTER_NAME,resAdapName);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(resAdapterConfigResource, children);
        vResources.add(resAdapterConfigResource);
        
        //debug strings
        printResourceElements(resAdapterConfigResource);
    }
    
    /**
     * Returns an Iterator of <code>Resource</code>objects in the order as defined
     * in the resources XML configuration file. Maintained for backward compat 
     * purposes only. 
     */
    public Iterator<Resource> getResources() {
        return vResources.iterator();
    }
    
    public List getResourcesList() {
        return vResources;
    }
    
    /**
     * Returns an List of <code>Resource</code>objects that needs to be 
     * created prior to module deployment. This includes all non-Connector 
     * resources and resource-adapter-config
     * @param resources List of resources, from which the non connector
     * resources need to be obtained.
     * @param isResourceCreation indicates if this determination needs to be
     * done during the <code>PreResCreationPhase</code>. In the
     * <code>PreResCreationPhase</code>, RA config is added to the 
     * non connector list, so that the RA config is created prior to the
     * RA deployment. For all other purpose, this flag needs to be set to false. 
     */
    public static List getNonConnectorResourcesList(List<Resource> resources, 
                                                 boolean isResourceCreation) {
        return getResourcesOfType(resources, NONCONNECTOR, isResourceCreation);
    }

    /**
     *  Returns an Iterator of <code>Resource</code> objects that correspond to 
     *  connector resources that needs to be created post module deployment. They 
     *  are arranged in the order in which the resources needs to be created
     * @param resources List of resources, from which the non connector
     * resources need to be obtained.
     * @param isResourceCreation indicates if this determination needs to be
     * done during the <code>PreResCreationPhase</code>. In the
     * <code>PreResCreationPhase</code>, RA config is added to the 
     * non connector list, so that the RA config is created prior to the
     * RA deployment. For all other purpose, this flag needs to be set to false. 
     */
    
    public static List getConnectorResourcesList(List<Resource> resources, boolean isResourceCreation) {
        return getResourcesOfType(resources, CONNECTOR, isResourceCreation);
    }
    
    /*
     * Print(Debug) the resource
     */
    private void printResourceElements(Resource resource)
    {
        AttributeList attrList = resource.getAttributes();
        
        for (int i=0; i<attrList.size(); i++)
        {
            Attribute attr = (Attribute)attrList.get(i);
            Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
            logger.log(Level.FINE, "general.print_attr_name", attr.getName());
        }
    }
    
    // Helper Method to convert a String type to a String[]
     private String[] convertToStringArray(Object sOptions){
        StringTokenizer optionTokenizer   = new StringTokenizer((String)sOptions,",");
        int             size            = optionTokenizer.countTokens();
        String []       sOptionsList = new String[size];
        for (int ii=0; ii<size; ii++){
            sOptionsList[ii] = optionTokenizer.nextToken();
        } 
        return sOptionsList;
     }
    
    
      class AddResourcesErrorHandler implements ErrorHandler {
          public void error(SAXParseException e) throws org.xml.sax.SAXException{
           throw e ;
        }
          public void fatalError(SAXParseException e) throws org.xml.sax.SAXException{
          throw e ;
        }
          public void warning(SAXParseException e) throws org.xml.sax.SAXException{
          throw e ;
        }
    }
      
      
    public InputSource resolveEntity(String publicId,String systemId) 
        throws SAXException {
        InputSource is = null;
        try {
            String dtd = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
                         File.separator + "lib" + File.separator + "dtds" + File.separator +
                         "sun-resources_1_3.dtd";
            is = new InputSource(new java.io.FileInputStream(dtd));
        } catch(Exception e) {
            throw new SAXException("cannot resolve dtd", e);
        }
        return is;
    }
    
    class MyLexicalHandler implements LexicalHandler{
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            isDoctypePresent = true;
        }
        
        public void endDTD() throws SAXException {
        }
        
        public void startEntity(String name) throws SAXException {
        }
        
        public void endEntity(String name) throws SAXException {
        }
        
        public void startCDATA() throws SAXException {
        }
        
        public void endCDATA() throws SAXException {
        }
        
        public void comment(char[] ch, int start, int length) throws SAXException {
        }
        
    }
}

