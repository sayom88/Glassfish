// $Id: SizeValidatorForCollection.java 16748 2009-06-10 18:41:22Z epbernard $
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
package org.hibernate.validation.constraints.impl;

import java.util.Collection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import javax.validation.constraints.Size;

/**
 * Check that a string's length is between min and max.
 *
 * @author Hardy Ferentschik
 */
public class SizeValidatorForCollection implements ConstraintValidator<Size, Collection> {
	private int min;
	private int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the number of entries in a map.
	 *
	 * @param collection The collection to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns <code>true</code> if the collection is <code>null</code> or the number of entries in
	 *         <code>collection</code> is between the specified <code>min</code> and <code>max</code> values (inclusive),
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
		if ( collection == null ) {
			return true;
		}
		int length = collection.size();
		return length >= min && length <= max;
	}

		private void validateParameters() {
		if ( min < 0 ) {
			throw new ValidationException( "The min parameter cannot be negative." );
		}
		if ( max < 0 ) {
			throw new ValidationException( "The max paramter cannot be negative." );
		}
		if ( max < min ) {
			throw new ValidationException( "The length cannot be negative." );
		}
	}
}