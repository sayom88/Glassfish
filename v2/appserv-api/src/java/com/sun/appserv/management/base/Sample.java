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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/base/Sample.java,v 1.1 2006/12/02 06:02:50 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:02:50 $
 */
package com.sun.appserv.management.base;

import javax.management.Attribute;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;

/**
	Interface for a sample MBean , used as target for sample and test code.
	Various Attributes of varying types are made available for testing.
 */
public interface Sample extends Utility, AMX, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE			= XTypes.SAMPLE;
	
	/**
		The type of Notification emitted by emitNotification().
	 */
	public static final String	SAMPLE_NOTIFICATION_TYPE	= "Sample";
	
	/**
		The key to access user data within the Map obtained from Notification.getUserData().
	 */
	public static final String	USER_DATA_KEY				= "UserData";
	
	/**
		Emit 'numNotifs' notifications of type
		SAMPLE_NOTIFICATION_TYPE at the specified interval.
		
		@param data arbitrary data which will be placed into the Notification's UserData field.
		@param numNotifs number of Notifications to issue >= 1
		@param intervalMillis interval at which Notifications should be issued >= 0
	 */
	public void	emitNotifications( final Object data, final int numNotifs, final long intervalMillis );
	
	/** 
		Add a new Attribute. After this, the MBeanInfo will contain an MBeanAttributeInfo
		for this Attribute.
		
		@param name
		@param value
	 */
	public void 	addAttribute( final String name, final Object value );
	
	/** 
		Remove an Attribute. After this, the MBeanInfo will no longer
		contain an MBeanAttributeInfo for this Attribute.
	 */
	public void 	removeAttribute( final String name );
	
	/**
		For testing bandwidth...
	 */
	public void		uploadBytes( final byte[] bytes );
	public byte[]	downloadBytes( final int numBytes );
}
