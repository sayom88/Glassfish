/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Denise Smith- 2.4 
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.schemagen.imports.inheritance.child;

import org.eclipse.persistence.testing.jaxb.schemagen.imports.inheritance.parent.Parent;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace="childnamespace", name="childRoot")
@XmlType(namespace="childnamespace")
public class Child extends Parent {
   public String childThing;
   
   public boolean equals(Object obj){
	   if(obj instanceof Child){
		   boolean equals = super.equals(obj);
		   return equals && childThing.equals(((Child)obj).childThing);
	   }
	   return false;
   }
}
