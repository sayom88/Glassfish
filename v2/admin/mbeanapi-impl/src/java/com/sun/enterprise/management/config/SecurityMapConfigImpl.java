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
package com.sun.enterprise.management.config;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.Attribute;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.SecurityMapConfig;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;

import com.sun.enterprise.management.config.AMXConfigImplBase;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.AMXAttributeNameMapper;

import com.sun.enterprise.management.support.oldconfig.OldSecurityMap;

/**
	@since Appserver 9.0
*/
public final class SecurityMapConfigImpl extends AMXConfigImplBase
{
        public
    SecurityMapConfigImpl( final Delegate delegate )
    {
        super( delegate );
    }

		protected void
	addCustomMappings( final AMXAttributeNameMapper mapper )
	{
	    super.addCustomMappings( mapper );
	    
		mapper.matchName( "PrincipalNames", "Principal" );
		mapper.matchName( "UserGroupNames", "UserGroup" );
	}
	
	    private SecurityMapConfig
	self()
	{
	    return (SecurityMapConfig)getSelf();
	}
	
	    public void
	createPrincipal( final String principal )
	{
	    final String[]  existing    = self().getPrincipalNames();
	    
	    final Set<String>   newSet	= GSetUtil.newSet( existing );
	    newSet.add( principal );
	    
	    final String[]  newOnes = GSetUtil.toStringArray( newSet );
	    
	    delegateSetAttributeNoThrow( "PrincipalNames", newOnes );
	}
	
	    public void
	removePrincipal( final String principal )
	{
	    final String[]  existing    = self().getPrincipalNames();
	    
	    final Set<String>   newSet	= GSetUtil.newSet( existing );
	    newSet.remove( principal );
	    
	    final String[]  newOnes = GSetUtil.toStringArray( newSet );
	    
	    delegateSetAttributeNoThrow( "PrincipalNames", newOnes );
	}
	
	    public void
	createUserGroup( final String userGroup )
	{
	    final String[]  existing    = self().getUserGroupNames();
	    
	    final Set<String>   newSet	= GSetUtil.newSet( existing );
	    newSet.add( userGroup );
	    
	    final String[]  newOnes = GSetUtil.toStringArray( newSet );
	    
	    delegateSetAttributeNoThrow( "UserGroupNames", newOnes );
	}
	
	    public void
	removeUserGroup( final String userGroup )
	{
	    final String[]  existing    = self().getUserGroupNames();
	    
	    final Set<String>   newSet	= GSetUtil.newSet( existing );
	    newSet.remove( userGroup );
	    
	    final String[]  newOnes = GSetUtil.toStringArray( newSet );
	    
	    delegateSetAttributeNoThrow( "UserGroupNames", newOnes );
	}
	
	/*
	    public ObjectName
	getBackendPrincipalConfigObjectName()
	{
	    return getContaineeObjectName( XTypes.BACKEND_PRINCIPAL_CONFIG );
	} 
	*/
}



