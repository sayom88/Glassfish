/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.security;

import java.security.PrivilegedExceptionAction;

public class PrivilegedGetDeclaredField implements PrivilegedExceptionAction {

    private Class javaClass;
    private String fieldName;
    private boolean shouldSetAccessible;
    
    public PrivilegedGetDeclaredField(Class javaClass, String fieldName, boolean shouldSetAccessible) {
        this.javaClass = javaClass;
        this.fieldName = fieldName;
        this.shouldSetAccessible = shouldSetAccessible;
    }

    public Object run() throws NoSuchFieldException {
        return PrivilegedAccessHelper.getDeclaredField(javaClass, fieldName, shouldSetAccessible);
    }

}
