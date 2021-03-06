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
 *     Blaise Doughan - 2.2 - initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.xmlelementref.inheritance1;

import javax.xml.bind.annotation.XmlRootElement;

// TEST WITH @XmlRootElement
@XmlRootElement
public class ContactInfo {

    @Override
    public boolean equals(Object object) {
        if(null == object || object.getClass() != ContactInfo.class) {
            return false;
        }
        return true;
    }

}