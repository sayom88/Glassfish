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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
	Generated: Fri Jun 25 11:57:19 PDT 2004
	Generated from:
	com.sun.appserv:type=http-listener,id=http-listener-2,config=sa_server1-config,category=config
	com.sun.appserv:type=http-listener,id=admin-listenerUnSecure,config=server-config,category=config
	com.sun.appserv:type=http-listener,id=http-listener-1,config=sa_server1-config,category=config
	com.sun.appserv:type=http-listener,id=admin-listener,config=server-config,category=config
	com.sun.appserv:type=http-listener,id=http-listener-1,config=default-config,category=config
	com.sun.appserv:type=http-listener,id=http-listener-2,config=server-config,category=config
	com.sun.appserv:type=http-listener,id=http-listener-2,config=default-config,category=config
	com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import javax.management.ObjectName;
import javax.management.AttributeList;

/**
	Implementing class was: com.sun.enterprise.admin.mbeans.HttpListenerMBean
*/

public interface OldHTTPListenerMBean extends OldProperties
{
	public String	getAcceptorThreads();
	public void	setAcceptorThreads( final String value );

	public String	getAddress();
	public void	setAddress( final String value );

	public boolean	getBlockingEnabled();
	public void	setBlockingEnabled( final boolean value );

	public String	getDefaultVirtualServer();
	public void	setDefaultVirtualServer( final String value );

	public boolean	getEnabled();
	public void	setEnabled( final boolean value );

	public String	getExternalPort();
	public void	setExternalPort( final String value );

	public String	getFamily();
	public void	setFamily( final String value );

	public String	getId();
	public void	setId( final String value );

	public String	getPort();
	public void	setPort( final String value );

	public String	getRedirectPort();
	public void	setRedirectPort( final String value );

	public boolean	getSecurityEnabled();
	public void	setSecurityEnabled( final boolean value );

	public String	getServerName();
	public void	setServerName( final String value );

	public boolean	getXpoweredBy();
	public void	setXpoweredBy( final boolean value );


// -------------------- Operations --------------------
	public ObjectName	createSsl( final AttributeList attribute_list );
	public boolean	destroyConfigElement();
	public String	getDefaultAttributeValue( final String attributeName );
	public ObjectName	getSsl();
	public void	removeSsl();
}