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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/j2ee/JVMImpl.java,v 1.6 2006/03/17 03:34:18 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2006/03/17 03:34:18 $
 */
 
package com.sun.enterprise.management.j2ee;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.JVM;
import com.sun.appserv.management.j2ee.J2EEServer;

import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.config.ServerConfig;

import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.management.support.Delegate;

/**
	Identifies a Java VM being utilized by a server.
 */
public final class JVMImpl
	extends J2EEManagedObjectImplBase 
{
		public
	JVMImpl( final Delegate delegate )
	{
		super( delegate );
	}
	
		public String
	getjavaVendor()
	{
		return( (String)delegateGetAttributeNoThrow( "javaVendor" ) );
	}
	
		protected String
	getConfigPeerName()
	{
		return( AMX.NO_NAME );
	}
	
		protected String
	getConfigPeerJ2EEType()
	{
		return( XTypes.JAVA_CONFIG );
	}
	
		protected Map<String,String>
	getConfigPeerProps()
	{
		final J2EEServer	server			= (J2EEServer)getContainer();
		final ServerConfig 	serverConfig	= (ServerConfig)server.getConfigPeer();
		final String		configName		= serverConfig.getReferencedConfigName();
		
		final Map<String,String>	props	= new HashMap<String,String>();
		
		props.put( XTypes.CONFIG_CONFIG, configName );
		props.put( AMX.J2EE_TYPE_KEY, XTypes.JAVA_CONFIG );
		
		return props;
	}
	
		public String
	getjavaVersion()
	{
		return( (String)delegateGetAttributeNoThrow( "javaVersion" ) );
	}
	
		public String
	getnode()
	{
		String	fullyQualifiedHostName	= (String)delegateGetAttributeNoThrow( "node" );
		
		/*
			Underlying MBean does not comply with JSR77.3.4.1.3, which states:
			"Identifies the node (machine) this JVM is running on. The value of the node
			attribute must be the fully quailified hostname of the node the JVM is running on."
			
			value seems to be of the form: BLACK/129.150.29.138
			
			Roll our own instead.
		 */
		 try
		 {
		 	fullyQualifiedHostName	= java.net.InetAddress.getLocalHost().getCanonicalHostName();
		 }
		 catch( java.net.UnknownHostException e)
		 {
		 }
		
		return( fullyQualifiedHostName );
	}
	
	
	private final static Set<String>	DONT_MAP_SET = Collections.unmodifiableSet(
		GSetUtil.newSet( new String[] { "node", "javaVersion", "javaVendor" }) );
	
		protected Set<String>
	getDontMapAttributeNames()
	{
	    final Set<String>   all = GSetUtil.newSet( DONT_MAP_SET );
	    all.addAll( super.getDontMapAttributeNames() );
	    return( all );
	}
	

}
