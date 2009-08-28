// $Id: BeanDescriptor.java 16829 2009-06-18 00:29:12Z epbernard $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.validation.metadata;

import java.util.Set;

/**
 * Describe a constrained Java Bean and the constraints associated to it.
 *
 * @author Emmanuel Bernard
 */
public interface BeanDescriptor extends ElementDescriptor {
	/**
	 * Returns true if the bean involves validation:
	 * <ul>
	 * <li> a constraint is hosted on the bean itself </li>
	 * <li> a constraint is hosted on one of the bean properties </li>
	 * <li> or a bean property is marked for cascade (@Valid) </li>
	 * </ul>
	 *
	 * @return <code>true</code> if the bean involves validation, <code>false</code> otherwise.
	 */
	boolean isBeanConstrained();

	/**
	 * Return the property descriptor for a given property.
	 * Return <code>null</code> if the property does not exist or has no
	 * constraint nor is marked as cascaded (see {@link #getConstrainedProperties()} )
	 * <p/>
	 * The returned object (and associated objects including ConstraintDescriptors)
	 * are immutable.
	 *
	 * @param propertyName property evaluated
	 *
	 * @return the property descriptor for a given property.
	 *
	 * @throws IllegalArgumentException if propertyName is null
	 */
	PropertyDescriptor getConstraintsForProperty(String propertyName);

	/**
	 * @return the property descriptors having at least one constraint defined or which are marked
	 *         as cascaded (@Valid) or an empty set.
	 */
	Set<PropertyDescriptor> getConstrainedProperties();
}
