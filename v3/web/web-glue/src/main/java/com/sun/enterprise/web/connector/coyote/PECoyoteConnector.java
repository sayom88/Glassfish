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

package com.sun.enterprise.web.connector.coyote;

import com.sun.appserv.ProxyHandler;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.connector.extension.GrizzlyConfig;
import com.sun.enterprise.web.pwc.connector.coyote.PwcCoyoteRequest;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.admin.config.Property;
import com.sun.grizzly.config.dom.Ssl;
import com.sun.grizzly.config.dom.ThreadPool;
import com.sun.grizzly.config.dom.Transport;
import com.sun.grizzly.config.dom.FileCache;
import com.sun.grizzly.util.IntrospectionUtils;
import com.sun.logging.LogDomains;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.MapperListener;
import org.glassfish.security.common.CipherInfo;
import org.glassfish.web.admin.monitor.RequestProbeProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PECoyoteConnector extends Connector {

    private static final String DEFAULT_KEYSTORE_TYPE = "JKS";
    private static final String DEFAULT_TRUSTSTORE_TYPE = "JKS";

    private static final String DUMMY_CONNECTOR_LAUNCHER = 
                com.sun.enterprise.web.
                    connector.grizzly.DummyConnectorLauncher.class.getName();

    protected static final Logger _logger = LogDomains.getLogger(
            PECoyoteConnector.class, LogDomains.WEB_LOGGER);

    protected static final ResourceBundle _rb = _logger.getResourceBundle();

    /**
     * Are we recycling objects
     */
    protected boolean recycleObjects;
    
    
     /**
     * The number of acceptor threads.
     */   
    protected int maxAcceptWorkerThreads;
    
    
    /**
     * The number of reader threads.
     */
    protected int maxReadWorkerThreads;
    
    
    /**
     * The request timeout value used by the processor threads.
     */
    protected int processorWorkerThreadsTimeout;
    
    
    /**
     * The increment number used by the processor threads.
     */
    protected int minProcessorWorkerThreadsIncrement;
    
    
    /**
     * The size of the accept queue.
     */
    protected int minAcceptQueueLength;
    
    
    /**
     * The size of the read queue
     */
    protected int minReadQueueLength;
    
    
    /**
     * The size of the processor queue.
     */ 
    protected int minProcessorQueueLength;   
    
    
    /**
     * Use direct or non direct byte buffer.
     */
    protected boolean useDirectByteBuffer;
    
    
    // Are we using the NIO Connector or the CoyoteConnector
    private boolean coyoteOn = false;
    
    /*
     * Number of seconds before idle keep-alive connections expire
     */
    private int keepAliveTimeoutInSeconds;

    /*
     * Number of keep-alive threads
     */
    private int keepAliveThreadCount;

    /*
     * Specifies whether response chunking is enabled/disabled
     */
    private boolean chunkingDisabled;

    /**
     * Maximum pending connection before refusing requests.
     */
    private int queueSizeInBytes = 4096;
  
    /**
     * Server socket backlog.
     */
    protected int ssBackLog = 4096;    
    
    /**
     * Set the number of <code>Selector</code> used by Grizzly.
     */
    public int selectorReadThreadsCount = 0;
    
    /**
     * The default response-type
     */
    protected String defaultResponseType = "text/plain; charset=iso-8859-1";

    /**
     * The forced request-type
     */
    protected String forcedRequestType = "text/plain; charset=iso-8859-1"; 
    
    /**
     * The monitoring classes used to gather stats.
     */
    protected GrizzlyConfig grizzlyMonitor;

    /**
     * The root folder where application are deployed
     */
    private String rootFolder = "";    
    

    // --------------------------------------------- FileCache support --//
    
    /**
     * Timeout before remove the static resource from the cache.
     */
    private int secondsMaxAge = -1;
    
    /**
     * The maximum entries in the <code>fileCache</code>
     */
    private int maxCacheEntries = 1024;
 
    /**
     * The maximum size of a cached resources.
     */
    private long minEntrySize = 2048;
            
    /**
     * The maximum size of a cached resources.
     */
    private long maxEntrySize = 537600;
    
    /**
     * The maximum cached bytes
     */
    private long maxLargeFileCacheSize = 10485760;
    
    /**
     * The maximum cached bytes
     */
    private long maxSmallFileCacheSize = 1048576;
    
    /**
     * Is the FileCache enabled.
     */
    private boolean fileCacheEnabled = true;
    
    /**
     * Is the large FileCache enabled.
     */
    private boolean isLargeFileCacheEnabled = true;    
    
    /**
     * Location of the CRL file
     */
    private String crlFile;    

    /**
     * The trust management algorithm
     */
    private String trustAlgorithm;    

    /**
     * The maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     */
    private String trustMaxCertLength;


    private WebContainer webContainer;


    private RequestProbeProvider requestProbeProvider;


    /**
     * Constructor
     */   
    public PECoyoteConnector(WebContainer webContainer) {
        this.webContainer = webContainer;
        requestProbeProvider = webContainer.getRequestProbeProvider();
        setProtocolHandlerClassName(DUMMY_CONNECTOR_LAUNCHER);
    }
    

    /**
     * Enables or disables chunked encoding for any responses returned by
     * this connector.
     *
     * @param chunkingDisabled true if chunking is to be disabled, false
     * otherwise
     */
    public void setChunkingDisabled(boolean chunkingDisabled) {
        this.chunkingDisabled = chunkingDisabled;
    }


    /**
     * @return true if chunking is disabled on this Connector, and false
     * otherwise
     */
    public boolean isChunkingDisabled() {
        return chunkingDisabled;
    }


    /** 
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible ContractProvider.
     */
    @Override
    public Request createRequest() {
        
        PwcCoyoteRequest request = new PwcCoyoteRequest();
        request.setConnector(this);
        return request;
    }


    /**
     * Creates and returns Response object.
     *
     * @return Response object
     */ 
    @Override
    public Response createResponse() {

        PECoyoteResponse response = new PECoyoteResponse(isChunkingDisabled());
        response.setConnector(this);
        return response;

    }


    /**
     * Gets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @return Keep-alive timeout in number of seconds
     */
    public int getKeepAliveTimeoutInSeconds() {
        return keepAliveTimeoutInSeconds;
    }


    /**
     * Sets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @param timeout Keep-alive timeout in number of seconds
     */
    public void setKeepAliveTimeoutInSeconds(int timeout) {
        keepAliveTimeoutInSeconds = timeout;
        setProperty("keepAliveTimeoutInSeconds", String.valueOf(timeout));
    }


    /**
     * Gets the number of keep-alive threads.
     *
     * @return Number of keep-alive threads
     */
    public int getKeepAliveThreadCount() {
        return keepAliveThreadCount;
    }

    /**
     * Set the maximum pending connection this <code>Connector</code>
     * can handle.
     */
    public void setQueueSizeInBytes(int queueSizeInBytes){
        this.queueSizeInBytes = queueSizeInBytes;
        setProperty("queueSizeInBytes", queueSizeInBytes);
    }


    /**
     * Return the maximum pending connection.
     */
    public int getQueueSizeInBytes(){
        return queueSizeInBytes;
    }

 
    /**
     * Set the <code>SocketServer</code> backlog.
     */
    public void setSocketServerBacklog(int ssBackLog){
        this.ssBackLog = ssBackLog;
        setProperty("socketServerBacklog", ssBackLog);
    }


    /**
     * Return the maximum pending connection.
     */
    public int getSocketServerBacklog(){
        return ssBackLog;
    }

    
    /**
     * Set the <code>recycle-tasks</code> used by this <code>Selector</code>
     */   
    public void setRecycleObjects(boolean recycleObjects){
        this.recycleObjects= recycleObjects;
        setProperty("recycleObjects", 
                    String.valueOf(recycleObjects));
    }
    
    
    /**
     * Return the <code>recycle-tasks</code> used by this 
     * <code>Selector</code>
     */      
    public boolean getRecycleObjects(){
        return recycleObjects;
    }
   
   
    /**
     * Set the <code>reader-thread</code> from domian.xml.
     */    
    public void setMaxReadWorkerThreads(int maxReadWorkerThreads){
        this.maxReadWorkerThreads = maxReadWorkerThreads;
        setProperty("maxReadWorkerThreads", 
                    String.valueOf(maxReadWorkerThreads));
    }
    
    
    /**
     * Return the <code>read-thread</code> used by this <code>Selector</code>
     */  
    public int getMaxReadWorkerThreads(){
        return maxReadWorkerThreads;
    }

    
    /**
     * Set the <code>reader-thread</code> from domian.xml.
     */    
    public void setMaxAcceptWorkerThreads(int maxAcceptWorkerThreads){
        this.maxAcceptWorkerThreads = maxAcceptWorkerThreads;
        setProperty("maxAcceptWorkerThreads", 
                    String.valueOf(maxAcceptWorkerThreads));
    }
    
    
    /**
     * Return the <code>read-thread</code> used by this <code>Selector</code>
     */  
    public int getMaxAcceptWorkerThreads(){
        return maxAcceptWorkerThreads;
    }
    
    
    /**
     * Set the <code>acceptor-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinAcceptQueueLength(int minAcceptQueueLength){
        this.minAcceptQueueLength = minAcceptQueueLength;
        setProperty("minAcceptQueueLength", 
                    String.valueOf(minAcceptQueueLength));
    }
 
    
    /**
     * Return the <code>acceptor-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinAcceptQueueLength(){
        return minAcceptQueueLength;
    }
    
    
    /**
     * Set the <code>reader-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinReadQueueLength(int minReadQueueLength){
        this.minReadQueueLength = minReadQueueLength;
        setProperty("minReadQueueLength", 
                    String.valueOf(minReadQueueLength));
    }
    
    
    /**
     * Return the <code>reader-queue-length</code> value
     * on this <code>Selector</code>
     */
    public int getMinReadQueueLength(){
        return minReadQueueLength;
    }
    
    
    /**
     * Set the <code>processor-queue-length</code> value 
     * on this <code>Selector</code>
     */
    public void setMinProcessorQueueLength(int minProcessorQueueLength){
        this.minProcessorQueueLength = minProcessorQueueLength;
        setProperty("minProcessorQueueLength", 
                    String.valueOf(minProcessorQueueLength));
    }
    
    
    /**
     * Return the <code>processor-queue-length</code> value
     * on this <code>Selector</code>
     */  
    public int getMinProcessorQueueLength(){
        return minProcessorQueueLength;
    }
    
    
    /**
     * Set the <code>use-nio-non-blocking</code> by this <code>Selector</code>
     */  
    public void setUseDirectByteBuffer(boolean useDirectByteBuffer){
        this.useDirectByteBuffer = useDirectByteBuffer;
        setProperty("useDirectByteBuffer", 
                    String.valueOf(useDirectByteBuffer));
    }
    
    
    /**
     * Return the <code>use-nio-non-blocking</code> used by this 
     * <code>Selector</code>
     */    
    public boolean getUseDirectByteBuffer(){
        return useDirectByteBuffer;
    }

    
    public void setProcessorWorkerThreadsTimeout(int timeout){
        processorWorkerThreadsTimeout = timeout;
        setProperty("processorWorkerThreadsTimeout", 
                    String.valueOf(timeout));        
    }
    
    
    public int getProcessorWorkerThreadsTimeout(){
        return processorWorkerThreadsTimeout;
    }

    public int getMinProcessorWorkerThreadsIncrement(){
        return minProcessorWorkerThreadsIncrement;
    }
 
    public void setSelectorReadThreadsCount(int selectorReadThreadsCount){
        setProperty("selectorReadThreadsCount", 
                     String.valueOf(selectorReadThreadsCount)); 
    }
    
    
    /**
     * Set the default response type used. Specified as a semi-colon
     * delimited string consisting of content-type, encoding,
     * language, charset
     */
    public void setDefaultResponseType(String defaultResponseType){
        this.defaultResponseType = defaultResponseType;
        setProperty("defaultResponseType", defaultResponseType);             
    }


    /**
     * Return the default response type used
     */
    public String getDefaultResponseType(){
         return defaultResponseType;
    }
    
    
    /**
     * Sets the forced request type, which is forced onto requests that
     * do not already specify any MIME type.
     */
    public void setForcedRequestType(String forcedResponseType){
        forcedRequestType = forcedResponseType;
        setProperty("forcedRequestType", forcedResponseType);                     
    }  
    
        
    /**
     * Return the default request type used
     */
    public String getForcedRequestType(){
        return forcedRequestType;
    } 

    
    public void start() throws LifecycleException {
        super.start();  
        if ( grizzlyMonitor != null ) {
            grizzlyMonitor.initConfig();
            grizzlyMonitor.registerMonitoringLevelEvents();
        }
    }
    
    
    public void stop() throws LifecycleException {
        super.stop(); 
        if ( grizzlyMonitor != null ) {
            grizzlyMonitor.destroy();
            grizzlyMonitor=null;
        }
    }
   //------------------------------------------------- FileCache config -----/

   
    /**
     * The timeout in seconds before remove a <code>FileCacheEntry</code>
     * from the <code>fileCache</code>
     */
    public void setSecondsMaxAge(int sMaxAges){
        secondsMaxAge = sMaxAges;
        setProperty("secondsMaxAge", String.valueOf(secondsMaxAge));        
    }
    
    
    /**
     * Set the maximum entries this cache can contains.
     */
    public void setMaxCacheEntries(int mEntries){
        maxCacheEntries = mEntries;
        setProperty("maxCacheEntries", String.valueOf(maxCacheEntries));         
    }

    
    /**
     * Return the maximum entries this cache can contains.
     */    
    public int getMaxCacheEntries(){
        return maxCacheEntries;
    }
    
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMinEntrySize(long mSize){
        minEntrySize = mSize;
        setProperty("minEntrySize", String.valueOf(minEntrySize));         
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMinEntrySize(){
        return minEntrySize;
    }
     
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMaxEntrySize(long mEntrySize){
        maxEntrySize = mEntrySize;
        setProperty("maxEntrySize", String.valueOf(maxEntrySize));        
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMaxEntrySize(){
        return maxEntrySize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxLargeCacheSize(long mCacheSize){
        maxLargeFileCacheSize = mCacheSize;
        setProperty("maxLargeFileCacheSize", 
                String.valueOf(maxLargeFileCacheSize));          
    }

    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxLargeCacheSize(){
        return maxLargeFileCacheSize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxSmallCacheSize(long mCacheSize){
        maxSmallFileCacheSize = mCacheSize;
        setProperty("maxSmallFileCacheSize", 
                String.valueOf(maxSmallFileCacheSize));         
    }
    
    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxSmallCacheSize(){
        return maxSmallFileCacheSize;
    }    

    
    /**
     * Is the fileCache enabled.
     */
    public boolean isFileCacheEnabled(){
        return fileCacheEnabled;
    }

    
    /**
     * Is the file caching mechanism enabled.
     */
    public void setFileCacheEnabled(boolean fileCacheEnabled){
        this.fileCacheEnabled = fileCacheEnabled;
        setProperty("fileCacheEnabled",String.valueOf(fileCacheEnabled));
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public void setLargeFileCacheEnabled(boolean isLargeEnabled){
        isLargeFileCacheEnabled = isLargeEnabled;
        setProperty("largeFileCacheEnabled",
                String.valueOf(isLargeFileCacheEnabled));                 
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public boolean getLargeFileCacheEnabled(){
        return isLargeFileCacheEnabled;
    } 

    // --------------------------------------------------------------------//
         
    
    /**
     * Set the documenr root folder
     */
    public void setWebAppRootPath(String rootFolder){
        this.rootFolder = rootFolder;
        setProperty("webAppRootPath",rootFolder);     
    }
    
    
    /**
     * Return the folder's root where application are deployed.
     */
    public String getWebAppRootPath(){
        return rootFolder;
    }
    
    
    /**
     * Initialize this connector.
     */
    @Override
    public void initialize() throws LifecycleException {
        super.initialize();
        // Set the monitoring.
        grizzlyMonitor = new GrizzlyConfig(webContainer, domain, getPort());
    }


    /**
     * Sets the truststore location of this connector.
     *
     * @param truststore The truststore location
     */
    public void setTruststore(String truststore) {
        setProperty("truststore", truststore);
    }


    /**
     * Gets the truststore location of this connector.
     *
     * @return The truststore location
     */
    public String getTruststore() {
        return (String) getProperty("truststore");
    }


    /**
     * Sets the truststore type of this connector.
     *
     * @param type The truststore type
     */
    public void setTruststoreType(String type) {
        setProperty("truststoreType", type);
    }


    /**
     * Gets the truststore type of this connector.
     *
     * @return The truststore type
     */
    public String getTruststoreType() {
        return (String) getProperty("truststoreType");
    }


    /**
     * Sets the keystore type of this connector.
     *
     * @param type The keystore type
     */
    public void setKeystoreType(String type) {
        setProperty("keystoreType", type);
    }


    /**
     * Gets the keystore type of this connector.
     *
     * @return The keystore type
     */
    public String getKeystoreType() {
        return (String) getProperty("keystoreType");
    }


    /**
     * Gets the location of the CRL file
     *
     * @return The location of the CRL file 
     */
    public String getCrlFile() {
         return crlFile;
    }
    
    
    /**
     * Sets the location of the CRL file.
     *
     * @param crlFile The location of the CRL file
     */
    public void setCrlFile(String crlFile) {
        this.crlFile = crlFile;
        setProperty("crlFile", crlFile);                     
    }  


    /**
     * Gets the trust management algorithm
     *
     * @return The trust management algorithm
     */
    public String getTrustAlgorithm() {
         return trustAlgorithm;
    }
    
    
    /**
     * Sets the trust management algorithm
     *
     * @param trustAlgorithm The trust management algorithm
     */
    public void setTrustAlgorithm(String trustAlgorithm) {
        this.trustAlgorithm = trustAlgorithm;
        setProperty("truststoreAlgorithm", trustAlgorithm);
    }  


    /**
     * Gets the maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     *
     * @return The maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     */
    public String getTrustMaxCertLength() {
         return trustMaxCertLength;
    }
    
    
    /**
     * Sets the maximum number of non-self-issued intermediate
     * certificates that may exist in a certification path
     *
     * @param trustMaxCertLength The maximum number of non-self-issued
     * intermediate certificates that may exist in a certification path
     */
    public void setTrustMaxCertLength(String trustMaxCertLength) {
        this.trustMaxCertLength = trustMaxCertLength;
        setProperty("trustMaxCertLength", trustMaxCertLength);
    }  


    /**
     * Gets the MapperListener of this connector.
     *
     * @return The MapperListener of this connector
     */
    public MapperListener getMapperListener() {
        return mapperListener;
    }


    /*
     * Configures this connector.
     *
     * @param listener The http-listener that corresponds to the given
     * connector
     * @param isSecure true if the connector is security-enabled, false
     * otherwise
     * @param httpServiceProps The http-service properties
     */
    public void configure(NetworkListener listener, boolean isSecure,
        HttpService httpService) {

        final Transport transport = listener.findTransport();
        try {
            setSocketServerBacklog(
                Integer.parseInt(transport.getMaxConnectionsCount()));
        } catch (NumberFormatException ex) {
            String msg = _rb.getString("pewebcontainer.invalidMaxPendingCount");
            msg = MessageFormat.format(
                msg, transport.getMaxConnectionsCount(),
                Integer.toString(getSocketServerBacklog()));
            _logger.log(Level.WARNING, msg, ex);
        }
        /* TODO
        WebContainerFeatureFactory wcFeatureFactory = _serverContext.getDefaultHabitat().getComponent(WebContainerFeatureFactory.class);
        String sslImplementationName = 
            webFeatureFactory.getSSLImplementationName();
        
        if (sslImplementationName != null) {
            connector.setProperty("sSLImplementation",sslImplementationName);
        }*/
        
        setDomain(webContainer.getServerContext().getDefaultDomainName());
        
        configureSSL(listener);
        configureThreadPool(listener.findThreadPool());
        configureFileCache(listener.findProtocol().getHttp().getFileCache());
        
        final Http http = listener.findProtocol().getHttp();
        setMaxHttpHeaderSize(Integer.parseInt(http.getSendBufferSizeBytes()));
        setDefaultHost(http.getDefaultVirtualServer());
        setDefaultResponseType(http.getDefaultResponseType());
        setForcedRequestType(http.getForcedResponseType());
        setEnableLookups(ConfigBeansUtilities.toBoolean(http.getEnableDnsLookup()));
        
        setXpoweredBy(Boolean.valueOf(http.getXpoweredBy()));
        
        // Application root
        setWebAppRootPath(webContainer.getModulesRoot().getAbsolutePath());
        
        // server-name (may contain scheme and colon-separated port number)
        String serverName = http.getServerName();
        if (serverName != null && serverName.length() > 0) {
            // Ignore scheme, which was required for webcore issued redirects
            // in 8.x EE
            if (serverName.startsWith("http://")) {
                serverName = serverName.substring("http://".length());
            } else if (serverName.startsWith("https://")) {
                serverName = serverName.substring("https://".length());
            }
            int index = serverName.indexOf(':');
            if (index != -1) {
                setProxyName(serverName.substring(0, index).trim());
                String serverPort = serverName.substring(index+1).trim();
                if (serverPort.length() > 0) {
                    try {
                        setProxyPort(Integer.parseInt(serverPort));
                    } catch (NumberFormatException nfe) {
                        _logger.log(Level.SEVERE,
                            "pewebcontainer.invalid_proxy_port",
                            new Object[] { serverPort, listener.getName() });
		    }
                }
            } else {
                setProxyName(serverName);
            }
        }

        // redirect-port
        String redirectPort = http.getRedirectPort();
        if (redirectPort != null && redirectPort.length() != 0) {
            try {
                setRedirectPort(Integer.parseInt(redirectPort));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_redirect_port",
                    new Object[] {
                        redirectPort,
                        listener.getName(),
                        Integer.toString(getRedirectPort()) });
            }  
        } else {
            setRedirectPort(-1);
        }

        // acceptor-threads
        String acceptorThreads = transport.getAcceptorThreads();
        if (acceptorThreads != null) {
            try {
                setSelectorReadThreadsCount(Integer.parseInt(
                    acceptorThreads));
            } catch (NumberFormatException nfe) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_acceptor_threads",
                    new Object[] {
                        acceptorThreads,
                        listener.getName(),
                        Integer.toString(getMaxProcessors()) });
            }  
        }
        
        // Configure Connector with keystore password and location
        if (isSecure) {
            configureKeysAndCerts();
        }
        
        webContainer.configureHttpServiceProperties(httpService, this);      

        // Override http-service property if defined.
        configureHttpListenerProperties(listener);
    }


    /*
     * Configures this connector for modjk.
     */
    public void configureJKProperties() {

        String propertiesURL = System.getProperty(
            "com.sun.enterprise.web.connector.enableJK.propertyFile");

        if (propertiesURL == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("com.sun.enterprise.web.connector.enableJK.propertyFile not defined");
            }
            return;
        } 

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Loading glassfish-jk.properties from " +
                           propertiesURL);
        }

        File propertiesFile   = new File(propertiesURL);
        if ( !propertiesFile.exists() ) {
            String msg = _rb.getString("pewebcontainer.missingJKProperties");
            msg = MessageFormat.format(msg, propertiesURL);
            _logger.log(Level.WARNING, msg);
            return;
        }

        Properties properties = null;
        InputStream is = null;
 
        try {
            FileInputStream fis = new FileInputStream(propertiesFile);
            is = new BufferedInputStream(fis);
            properties = new Properties();
            properties.load(is);

        } catch (Exception ex) {
            String msg = _rb.getString("pewebcontainer.configureJK");
            msg = MessageFormat.format(msg, getPort());
            _logger.log(Level.SEVERE, msg, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {}
            }
        }

        Enumeration enumeration = properties.keys();
        while (enumeration.hasMoreElements()) {
            String propName = (String) enumeration.nextElement();
            String value = properties.getProperty(propName);
            if (value != null) {
                IntrospectionUtils.setProperty(this, propName, value);
            }
        }
    }

    /**
     * Configure the Grizzly FileCache mechanism
     * @param fileCache
     */
    public void configureFileCache(FileCache fileCache) {
        if (fileCache != null) {
            setLargeFileCacheEnabled(ConfigBeansUtilities.toBoolean(
                fileCache.getEnabled()));
            if (fileCache.getMaxAgeSeconds() != null) {
                setSecondsMaxAge(Integer.parseInt(fileCache.getMaxAgeSeconds()));
            }
            if (fileCache.getMaxFilesCount() != null) {
                setMaxCacheEntries(Integer.parseInt(
                    fileCache.getMaxFilesCount()));
            }
            if (fileCache.getMaxCacheSizeBytes() != null) {
                setMaxLargeCacheSize(Integer.parseInt(fileCache.getMaxCacheSizeBytes()));
            }
        }
    }


    /**
     * Configures this connector with the given request-processing
     * config.
     *
     * @param pool http-service config to use
     */
    public void configureThreadPool(ThreadPool pool){
        if (pool != null) {
            try {
                setMaxProcessors(Integer.parseInt(pool.getMaxThreadPoolSize()));
                setMinProcessors(Integer.parseInt(pool.getMinThreadPoolSize()));
                setQueueSizeInBytes(Integer.parseInt(pool.getMaxQueueSize()));
//                setProcessorWorkerThreadsTimeout(Integer.parseInt(
//                    pool.getRequestTimeoutInSeconds()));
            } catch (NumberFormatException ex) {
                _logger
                    .log(Level.WARNING, " Invalid request-processing attribute",
                        ex);
            }
        }
    }

    /**
     * Configure http-listener property.
     * return true if the property exists and has been set.
     */
    public boolean configureHttpListenerProperty(String propName, String propValue)
        throws NumberFormatException {
        if ("proxyHandler".equals(propName)) {
            setProxyHandler(propValue);
            return true;
        }
        return false;
    }

    public void configHttpProperties(Http http, Transport transport, Ssl ssl) {
        if (http.getMaxConnections() != null) {
            setMaxKeepAliveRequests(Integer.parseInt(http.getMaxConnections()));
        }
        if (http.getTimeoutSeconds() != null) {
            setKeepAliveTimeoutInSeconds(Integer.parseInt(http.getTimeoutSeconds()));
        }
        if (http.getEnableAuthPassThrough() != null) {
            setAuthPassthroughEnabled(ConfigBeansUtilities.toBoolean(
                http.getEnableAuthPassThrough()));
        }
        if (http.getMaxPostSizeBytes() != null) {
            setMaxPostSize(Integer.parseInt(http.getMaxPostSizeBytes()));
        }
        if (http.getCompression() != null) {
            setProperty("compression", http.getCompression());
        }
        if (http.getCompressableMimeType() != null) {
            setProperty("compressableMimeType", http.getCompressableMimeType());
        }
        if (http.getNoCompressionUserAgents() != null) {
            setProperty("noCompressionUserAgents", http.getNoCompressionUserAgents());
        }
        if (http.getCompressionMinSizeBytes() != null) {
            setProperty("compressionMinSize", http.getCompressionMinSizeBytes());
        }
        if (http.getRestrictedUserAgents() != null) {
            setProperty("restrictedUserAgents", http.getRestrictedUserAgents());
        }
        if (http.getEnableCometSupport() != null) {
            setProperty("cometSupport",ConfigBeansUtilities.toBoolean(
                http.getEnableCometSupport()));
        }
        if (http.getEnableRcmSupport() != null) {
            setProperty("rcmSupport",ConfigBeansUtilities.toBoolean(
                http.getEnableRcmSupport()));
        }
        if (http.getConnectionUploadTimeoutMillis() != null) {
            setConnectionUploadTimeout(Integer.parseInt(
                http.getConnectionUploadTimeoutMillis()));
        }
        if (http.getDisableUploadTimeout() != null) {
            setDisableUploadTimeout(ConfigBeansUtilities.toBoolean(
                http.getDisableUploadTimeout()));
        }
        if (http.getUriEncoding() != null) {
            setURIEncoding(http.getUriEncoding());
        }
        if (http.getChunkingDisabled() != null) {
            setChunkingDisabled(ConfigBeansUtilities.toBoolean(
                http.getChunkingDisabled()));
        }
        configSslOptions(ssl);
    }

    private void configSslOptions(final Ssl ssl) {
        if (ssl != null) {
            if (ssl.getCrlFile() != null) {
                setCrlFile(ssl.getCrlFile());
            }
            if (ssl.getTrustAlgorithm() != null) {
                setTrustAlgorithm(ssl.getTrustAlgorithm());
            }
            if (ssl.getTrustMaxCertLengthBytes() != null) {
                setTrustMaxCertLength(ssl.getTrustMaxCertLengthBytes());
            }
        }
    }

    /*
     * Loads and instantiates the ProxyHandler implementation
     * class with the specified name, and sets the instantiated 
     * ProxyHandler on this connector.
     *
     * @param className The ProxyHandler implementation class name
     */
    public void setProxyHandler(String className) {

        Object handler = null;
        try {
            Class handlerClass = webContainer.loadCommonClass(className);
            handler = handlerClass.newInstance();
        } catch (Exception e) {
            String msg = _rb.getString(
                "pewebcontainer.proxyHandlerClassLoadError");
            msg = MessageFormat.format(msg, className);
            _logger.log(Level.SEVERE, msg, e);
        }
        if (handler != null) {
            if (!(handler instanceof ProxyHandler)) {
                _logger.log(
                    Level.SEVERE,
                    "pewebcontainer.proxyHandlerClassInvalid",
                    className);
            } else {
                setProxyHandler((ProxyHandler) handler);
            }                
        } 
    }


    /*
     * Request/response related probe events
     */

    /**
     * Fires probe event related to the fact that the given request has
     * been entered the web container.
     *
     * @param request the request object
     * @param response the response object
     * @param hostName the name of the virtual server to which the request
     * was mapped
     */
    public void requestStartEvent(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String hostName) {
        if (requestProbeProvider != null) {
            requestProbeProvider.requestStartEvent(request, response,
                                                   hostName);
        }
    };

    /**
     * Fires probe event related to the fact that the given request is about
     * to exit from the web container.
     *
     * @param request the request object
     * @param response the response object
     * @param hostName the name of the virtual server to which the request
     * was mapped
     * @param statusCode the response status code
     */
    public void requestEndEvent(HttpServletRequest request,
                                HttpServletResponse response,
                                String hostName,
                                int statusCode) {
        if (requestProbeProvider != null) {
            requestProbeProvider.requestEndEvent(request, response,
                                                 hostName, statusCode);
        }
    };


    /*
     * Configures the SSL properties on this PECoyoteConnector from the
     * SSL config of the given HTTP listener.
     *
     * @param listener HTTP listener whose SSL config to use
     */
    private void configureSSL(NetworkListener listener) {

        Ssl sslConfig = listener.findProtocol().getSsl();
        if (sslConfig == null) {
            return;
        }

        // client-auth
        if (Boolean.valueOf(sslConfig.getClientAuthEnabled())) {
            setClientAuth(true);
        }

        // ssl protocol variants
        StringBuilder sslProtocolsBuf = new StringBuilder();
        boolean needComma = false;
        if (Boolean.valueOf(sslConfig.getSsl2Enabled())) {
            sslProtocolsBuf.append("SSLv2");
            needComma = true;
        }
        if (Boolean.valueOf(sslConfig.getSsl3Enabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            } else {
                needComma = true;
            }
            sslProtocolsBuf.append("SSLv3");
        }
        if (Boolean.valueOf(sslConfig.getTlsEnabled())) {
            if (needComma) {
                sslProtocolsBuf.append(", ");
            }
            sslProtocolsBuf.append("TLSv1");
        }
        if (Boolean.valueOf(sslConfig.getSsl3Enabled()) ||
                Boolean.valueOf(sslConfig.getTlsEnabled())) {
            sslProtocolsBuf.append(", SSLv2Hello");
        }

        if (sslProtocolsBuf.length() == 0) {
            _logger.log(Level.WARNING,
                "pewebcontainer.all_ssl_protocols_disabled", listener.getName());
        } else {
            setSslProtocols(sslProtocolsBuf.toString());
        }

        // cert-nickname
        String certNickname = sslConfig.getCertNickname();
        if (certNickname != null && certNickname.length() > 0) {
            setKeyAlias(sslConfig.getCertNickname());
        }

        // ssl3-tls-ciphers
        String ciphers = sslConfig.getSsl3TlsCiphers();
        if (ciphers != null) {
            String jsseCiphers = getJSSECiphers(ciphers);
            if (jsseCiphers == null) {
                _logger.log(Level.WARNING,
                            "pewebcontainer.all_ciphers_disabled",
                            listener.getName());
            } else {
                setCiphers(jsseCiphers);
            }
        }            
    }


    /*
     * Configures this connector with its keystore and truststore.
     */
    private void configureKeysAndCerts() {

        /*
         * Keystore
         */
        String prop = System.getProperty("javax.net.ssl.keyStore");
        String keyStoreType = System.getProperty("javax.net.ssl.keyStoreType",DEFAULT_KEYSTORE_TYPE);
        if (prop != null) {
            // PE
            setKeystoreFile(prop);
            setKeystoreType(keyStoreType);
        }

        /*
         * Get keystore password from password.conf file.
         * Notice that JSSE, the underlying SSL implementation in PE,
         * currently does not support individual key entry passwords
         * that are different from the keystore password.
         *
        String ksPasswd = null;
        try {
            ksPasswd = PasswordConfReader.getKeyStorePassword();
        } catch (IOException ioe) {
            // Ignore
        }
        if (ksPasswd == null) {
            ksPasswd = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        if (ksPasswd != null) {
            try {
                connector.setKeystorePass(ksPasswd);
            } catch (Exception e) {
                _logger.log(Level.SEVERE,
                    "pewebcontainer.http_listener_keystore_password_exception",
                    e);
            }
        }*/

        /*
	 * Truststore
         */
        prop = System.getProperty("javax.net.ssl.trustStore");
        if (prop != null) {
            // PE
            setTruststore(prop);
            setTruststoreType(DEFAULT_TRUSTSTORE_TYPE);
        }
    }


    /**
     * Configure http-listener properties
     */
    private void configureHttpListenerProperties(NetworkListener listener) {
        // Configure Connector with <http-service> properties
        configHttpProperties(listener.findProtocol().getHttp(),
            listener.findTransport(), listener.findProtocol().getSsl());
/*
        for (Property httpListenerProp  : listener.getProperty()) {
            String propName = httpListenerProp.getName();
            String propValue = httpListenerProp.getValue();
            if (!configureHttpListenerProperty(propName, propValue)) {
                _logger.log(Level.WARNING,
                    "pewebcontainer.invalid_http_listener_property",
                    propName);                    
            }
        }
*/ 
    }          


    /*
     * Parses the given comma-separated string of cipher suite names,
     * converts each cipher suite that is enabled (i.e., not preceded by a
     * '-') to the corresponding JSSE cipher suite name, and returns a string
     * of comma-separated JSSE cipher suite names.
     *
     * @param sslCiphers String of SSL ciphers to parse
     *
     * @return String of comma-separated JSSE cipher suite names, or null if
     * none of the cipher suites in the given string are enabled or can be
     * mapped to corresponding JSSE cipher suite names
     */
    private String getJSSECiphers(String ciphers) {
        String cipher = null;
        StringBuffer enabledCiphers = null;
        boolean first = true;
        int index = ciphers.indexOf(',');
        if (index != -1) {
            int fromIndex = 0;
            while (index != -1) {
                cipher = ciphers.substring(fromIndex, index).trim();
                if (cipher.length() > 0 && !cipher.startsWith("-")) {
                    if (cipher.startsWith("+")) {
                        cipher = cipher.substring(1);
                    }
                    String jsseCipher = getJSSECipher(cipher);
                    if (jsseCipher == null) {
                        _logger.log(Level.WARNING,
                            "pewebcontainer.unrecognized_cipher", cipher);
                    } else {
                        if (enabledCiphers == null) {
                            enabledCiphers = new StringBuffer();
                        }
                        if (!first) {
                            enabledCiphers.append(", ");
                        } else {
                            first = false;
                        }
                        enabledCiphers.append(jsseCipher);
                    }
                }
                fromIndex = index + 1;
                index = ciphers.indexOf(',', fromIndex);
            }
            cipher = ciphers.substring(fromIndex);
        } else {
            cipher = ciphers;
        }
        if (cipher != null) {
            cipher = cipher.trim();
            if (cipher.length() > 0 && !cipher.startsWith("-")) {
                if (cipher.startsWith("+")) {
                    cipher = cipher.substring(1);
                }
                String jsseCipher = getJSSECipher(cipher);
                if (jsseCipher == null) {
                    _logger.log(Level.WARNING,
                        "pewebcontainer.unrecognized_cipher", cipher);
                } else {
                    if (enabledCiphers == null) {
                        enabledCiphers = new StringBuffer();
                    }
                    if (!first) {
                        enabledCiphers.append(", ");
                    } else {
                        first = false;
                    }
                    enabledCiphers.append(jsseCipher);
                }
            }
        }
        return enabledCiphers == null ? null : enabledCiphers.toString();
    }


    /*
     * Converts the given cipher suite name to the corresponding JSSE cipher.
     *
     * @param cipher The cipher suite name to convert
     *
     * @return The corresponding JSSE cipher suite name, or null if the given
     * cipher suite name can not be mapped
     */
    private String getJSSECipher(String cipher) {

        String jsseCipher = null;

        CipherInfo ci = CipherInfo.getCipherInfo(cipher);
        if( ci != null ) {
            jsseCipher = ci.getCipherName();
        }

        return jsseCipher;
    }
}

