/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
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
package org.eclipse.persistence.testing.oxm.mappings.choicecollection;

import org.eclipse.persistence.testing.oxm.mappings.choicecollection.reuse.ChoiceCollectionReuseTestCases;
import org.eclipse.persistence.testing.oxm.mappings.choicecollection.converter.ConverterTestCases;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XMLChoiceCollectionMappingTestSuite extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("XMLChoiceCollectionMapping Test Suite");
        suite.addTestSuite(XMLChoiceCollectionMappingEmptyTestCases.class);
        suite.addTestSuite(XMLChoiceCollectionMappingMixedTestCases.class);
        suite.addTestSuite(XMLChoiceCollectionWithGroupingElementTestCases.class);
        suite.addTestSuite(ConverterTestCases.class);
        suite.addTestSuite(ChoiceCollectionReuseTestCases.class);
        return suite;
    }

    public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.oxm.mappings.choicecollection.XMLChoiceCollectionMappingTestSuite" };
        junit.textui.TestRunner.main(arguments);
    }
}
