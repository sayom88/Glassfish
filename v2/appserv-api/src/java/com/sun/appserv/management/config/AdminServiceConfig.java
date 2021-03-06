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
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/AdminServiceConfig.java,v 1.1 2006/12/02 06:02:59 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:02:59 $
 */


package com.sun.appserv.management.config;

import java.util.Map;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;

/**
	 Configuration for the &lt;admin-service&gt; element.
 */
public interface AdminServiceConfig
	extends AMXConfig, PropertiesAccess, Container
{  
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.ADMIN_SERVICE_CONFIG;
	

	/**
		Calls Container.getContaineeMap( XTypes.JMX_CONNECTOR_CONFIG ).
		@return Map of JMXConnectorConfig proxies, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,JMXConnectorConfig>		getJMXConnectorConfigMap();

	public String   getSystemJMXConnectorName();

	public void     setSystemJMXConnectorName( String value );

	public String   getType();

	/** Possible value for Type.  See {@link #setType} */
	public final static String	TYPE_DAS			= "das";
	/** Possible value for Type.  See {@link #setType} */
	public final static String	TYPE_SERVER			= "server";
	/** Possible value for Type.  See {@link #setType} */
	public final static String	TYPE_DAS_AND_SERVER	= "das-and-server";
	
	/**
		Valid values are:
		<ul>
		<li>{@link #TYPE_DAS}</li>
		<li>{@link #TYPE_SERVER}</li>
		<li>{@link #TYPE_DAS_AND_SERVER}</li>
		</ul>
		@param value
	*/
	public void     setType( String value );

    /** may return null if not present */
	public DASConfig	getDASConfig();
	
	/**
		Creates a new jmx-connector element.

		@param name		The name (id) of the jmx-connector.
		@param address	The IP address or host-name.
		@param port		The port of the jmx-connector-server.
		@param authRealmName	The name of the auth-realm that represents the 
		special administrative realm. All authentication (from administraive 
		GUI and CLI) will be handled by this realm.
		@param optional	Map of optional attributes
		@return A proxy to the JMXConnectorConfig MBean.
		@see JMXConnectorConfigKeys
        @since Appserver 9.0
	 */
	public JMXConnectorConfig	createJMXConnectorConfig( String name, String address, 
		String port, String authRealmName, Map<String,String> optional );
		
    /**
        @deprecated
     */
	public JMXConnectorConfig	createJMXConnectorConfig( String name, String address, 
		int port, String authRealmName, Map<String,String> optional );

	/**
		Removes a jmx-connector element.

		@param name	The name (id) of the jmx-connector.
	 */
	public void			removeJMXConnectorConfig( String name );
}




