/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
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
package org.eclipse.persistence.testing.oxm.mappings.directcollection.union;

import java.math.BigInteger;
import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

public class SimpleUnionTestCases extends XMLMappingTestCases {
    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/directcollection/union/SimpleUnion.xml";
    private final static String CONTROL_ITEM = "10";
    private final static String CONTROL_FIRST_NAME = "Jane";
    private final static String CONTROL_LAST_NAME = "Doe";

    public SimpleUnionTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);
        setProject(new SimpleUnionProject());
    }

    protected Object getControlObject() {
        Person person = new Person();
        BigInteger item = new BigInteger(CONTROL_ITEM);
        person.addItem(item);
        person.setFirstName(CONTROL_FIRST_NAME);
        person.setLastName(CONTROL_LAST_NAME);
        return person;
    }
}
