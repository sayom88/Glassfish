/*******************************************************************************
 * Copyright (c) 2010 Oracle. All rights reserved.
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
package org.eclipse.persistence.testing.jaxb.xmlmarshaller;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import javax.xml.bind.Validator;

import junit.framework.TestCase;

import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

public class NoSchemaRefTestCases extends TestCase {

    public void testValidateRootNoSchemaReference() {
        try {
            Class[] classes = {Address.class};
            JAXBContext jc = JAXBContextFactory.createContext(classes, null);
            Validator validator = jc.createValidator();
            validator.validateRoot(new Address());
        } catch(ValidationException e) {
            XMLMarshalException xme = (XMLMarshalException) e.getLinkedException();
            assertEquals(XMLMarshalException.ERROR_RESOLVING_XML_SCHEMA, xme.getErrorCode());
            return;
        } catch(JAXBException e) {
            fail("A JAXBException was thrown instead of the expected ValidationException.");
        }
        fail("No exception was thrown instead of the expected ValidationException.");
    }

    public void testValidateNoSchemaReference() throws JAXBException {
        Class[] classes = {Address.class};
        JAXBContext jc = JAXBContextFactory.createContext(classes, null);
        Validator validator = jc.createValidator();
        // We currently always return true for validate
        assertTrue(validator.validate(new Address()));
    }

}