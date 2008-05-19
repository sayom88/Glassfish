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
package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import java.io.*;
import java.util.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Defines various global configuration for the running GlassFish instance.
 *
 * <p>
 * This primarily replaces all the system variables in V2.
 *
 * @author Jerome Dochez
 */
@Service
public class ServerEnvironment implements org.glassfish.api.admin.ServerEnvironment, PostConstruct {
    @Inject
    StartupContext startupContext;

    /** folder where all generated code like compiled jsps, stubs is stored */
    public static final String kGeneratedDirName = "generated";
    /** default folder where deployed j2ee-apps are stored */
    public static final String kRepositoryDirName = "applications";
    public static final String kApplicationDirName = "j2ee-apps";
    /** folder where deployed modules are stored */
    public static final String kModuleDirName = "j2ee-modules";
    public static final String kConfigXMLFileName = "domain.xml";
    public static final String kLoggingPropertiesFileNAme = "logging.properties";
    /** folder where the configuration of this instance is stored */
    public static final String kConfigDirName = "config";
    /** init file name */
    public static final String kInitFileName = "init.conf";
    /** folder where the compiled JSP pages reside */
    public static final String kCompileJspDirName = "jsp";

    public static final String DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT = "/admin";
    public static final String DEFAULT_ADMIN_CONSOLE_APP_NAME     = "__admingui"; //same as folder
    
    private /*almost final*/ File root;
    private /*almost final*/ boolean verbose;
    private /*almost final*/ boolean debug;
    private static final ASenvPropertyReader asenv = new ASenvPropertyReader();
    private /*almost final*/ String domainName;
    private /*almost final*/ String instanceName;

    private final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";

    /**
     * Compute all the values per default.
     */
    public ServerEnvironment() {
    }

    public ServerEnvironment(File root) {
        // the getParentFile() that we do later fails to work correctly if
        // root is for example "new File(".")
        this.root = root.getAbsoluteFile();
    }

    /**
     * This is where the real initialization happens.
     */
    public void postConstruct() {
        // default
        if(this.root==null)
            this.root = new File(System.getProperty(INSTANCE_ROOT_PROP_NAME));

        asenv.getProps().put(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, root.getAbsolutePath());
        Map<String, String> args = startupContext.getArguments();

        verbose = Boolean.parseBoolean(args.get("-verbose"));
        debug = Boolean.parseBoolean(args.get("-debug"));

        // ugly code because domainName & instanceName are final...
        String s = startupContext.getArguments().get("-domainname");

        if (!ok(s)) {
            s = root.getName();
        }
        domainName = s;

        s = startupContext.getArguments().get("-instancename");

        if (!ok(s)) {
            instanceName = "server";
        }
        else {
            instanceName = s;
        }
    }

    public String getInstanceName() {
        return instanceName;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public File getDomainRoot() {
        return root;
    }


    public StartupContext getStartupContext() {
        return startupContext;
    }

    /**
     * Gets the directory to store configuration.
     * Normally {@code ROOT/config}
     */
    public File getConfigDirPath() {
        return new File(root,kConfigDirName);
    }

    /**
     * Gets the directory to store deployed applications
     * Normally {@code ROOT/applications}
     */
    public File getApplicationRepositoryPath() {
        return new File(root,kRepositoryDirName);
    }

    /**
     * Gets the directory to store generated stuff.
     * Normally {@code ROOT/generated}
     */
    public File getApplicationStubPath() {
        return new File(root,kGeneratedDirName);
    }

    /**
     * Gets the <tt>init.conf</tt> file.
     */
    public File getInitFilePath() {
        return new File(getConfigDirPath(),kInitFileName);
    }

    /**
     * Gets the directory for hosting user-provided jar files.
     * Normally {@code ROOT/lib}
     */
    public File getLibPath() {
        return new File(root,"lib");

    }

    public String getApplicationGeneratedXMLPath() {
        return null;
    }

    /**
     * Returns the path for compiled JSP Pages from an J2EE application
     * that is deployed on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getApplicationCompileJspPath() {
        return new File(getApplicationStubPath(),kCompileJspDirName);
    }

    /**
     * Returns the path for compiled JSP Pages from an Web application
     * that is deployed standalone on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getWebModuleCompileJspPath() {
        return getApplicationCompileJspPath();
    }

    /**
    Returns the absolute path for location where all the deployed
    standalone modules are stored for this Server Instance.
     */
    public String getModuleRepositoryPath() {
        return null;
    }

    public String getJavaWebStartPath() {
        return null;
    }

    public String getApplicationBackupRepositoryPath() {
        return null;
    }

    public String getInstanceClassPath() {
        return null;
    }

    public String getModuleStubPath() {
        return null;
    }


    public Map<String, String> getProps() {
        return Collections.unmodifiableMap(asenv.getProps());
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    
    /** Returns the folder where the admin console application's folder (in the
     *  name of admin console application) should be found. Thus by default,
     *  it should be: [install-dir]/lib/install/applications. No attempt is made
     *  to check if this location is readable or writable.
     *  @return java.io.File representing parent folder for admin console application
     *   Never returns a null
     */
    public File getDefaultAdminConsoleFolderOnDisk() {
        File install = new File(asenv.getProps().get(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        File agp = new File(new File(new File(install, "lib"), "install"), "applications");
        return (agp);
    }
}

