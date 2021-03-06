// $Id: ConstraintHelper.java 17002 2009-07-06 11:28:23Z hardy.ferentschik $
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
package org.hibernate.validation.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Constraint;
import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validation.constraints.impl.AssertFalseValidator;
import org.hibernate.validation.constraints.impl.AssertTrueValidator;
import org.hibernate.validation.constraints.impl.DecimalMaxValidatorForNumber;
import org.hibernate.validation.constraints.impl.DecimalMaxValidatorForString;
import org.hibernate.validation.constraints.impl.DecimalMinValidatorForNumber;
import org.hibernate.validation.constraints.impl.DecimalMinValidatorForString;
import org.hibernate.validation.constraints.impl.DigitsValidatorForNumber;
import org.hibernate.validation.constraints.impl.DigitsValidatorForString;
import org.hibernate.validation.constraints.impl.FutureValidatorForCalendar;
import org.hibernate.validation.constraints.impl.FutureValidatorForDate;
import org.hibernate.validation.constraints.impl.MaxValidatorForNumber;
import org.hibernate.validation.constraints.impl.MaxValidatorForString;
import org.hibernate.validation.constraints.impl.MinValidatorForNumber;
import org.hibernate.validation.constraints.impl.MinValidatorForString;
import org.hibernate.validation.constraints.impl.NotNullValidator;
import org.hibernate.validation.constraints.impl.NullValidator;
import org.hibernate.validation.constraints.impl.PastValidatorForCalendar;
import org.hibernate.validation.constraints.impl.PastValidatorForDate;
import org.hibernate.validation.constraints.impl.PatternValidator;
import org.hibernate.validation.constraints.impl.SizeValidatorForArray;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfBoolean;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfByte;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfChar;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfDouble;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfFloat;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfInt;
import org.hibernate.validation.constraints.impl.SizeValidatorForArraysOfLong;
import org.hibernate.validation.constraints.impl.SizeValidatorForCollection;
import org.hibernate.validation.constraints.impl.SizeValidatorForMap;
import org.hibernate.validation.constraints.impl.SizeValidatorForString;
import org.hibernate.validation.util.ReflectionHelper;

/**
 * Keeps track of builtin constraints and their validator implementations, as well as already resolved validator definitions.
 *
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 */
public class ConstraintHelper {

	private final ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>> builtinConstraints =
			new ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>>();

	private final ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>> constraintValidatorDefinitons =
			new ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>>();

	public ConstraintHelper() {
		List<Class<? extends ConstraintValidator<?, ?>>> constraintList =
				new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertFalseValidator.class );
		builtinConstraints.put( AssertFalse.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertTrueValidator.class );
		builtinConstraints.put( AssertTrue.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( DecimalMaxValidatorForNumber.class );
		constraintList.add( DecimalMaxValidatorForString.class );
		builtinConstraints.put( DecimalMax.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( DecimalMinValidatorForNumber.class );
		constraintList.add( DecimalMinValidatorForString.class );
		builtinConstraints.put( DecimalMin.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( DigitsValidatorForString.class );
		constraintList.add( DigitsValidatorForNumber.class );
		builtinConstraints.put( Digits.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( FutureValidatorForCalendar.class );
		constraintList.add( FutureValidatorForDate.class );
		builtinConstraints.put( Future.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MaxValidatorForNumber.class );
		constraintList.add( MaxValidatorForString.class );
		builtinConstraints.put( Max.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MinValidatorForNumber.class );
		constraintList.add( MinValidatorForString.class );
		builtinConstraints.put( Min.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NotNullValidator.class );
		builtinConstraints.put( NotNull.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NullValidator.class );
		builtinConstraints.put( Null.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( PastValidatorForCalendar.class );
		constraintList.add( PastValidatorForDate.class );
		builtinConstraints.put( Past.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( PatternValidator.class );
		builtinConstraints.put( Pattern.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( SizeValidatorForString.class );
		constraintList.add( SizeValidatorForCollection.class );
		constraintList.add( SizeValidatorForArray.class );
		constraintList.add( SizeValidatorForMap.class );
		constraintList.add( SizeValidatorForArraysOfBoolean.class );
		constraintList.add( SizeValidatorForArraysOfByte.class );
		constraintList.add( SizeValidatorForArraysOfChar.class );
		constraintList.add( SizeValidatorForArraysOfDouble.class );
		constraintList.add( SizeValidatorForArraysOfFloat.class );
		constraintList.add( SizeValidatorForArraysOfInt.class );
		constraintList.add( SizeValidatorForArraysOfLong.class );
		builtinConstraints.put( Size.class, constraintList );
	}

	public List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> getBuiltInConstraints(Class<? extends Annotation> annotationClass) {
		final List<Class<? extends ConstraintValidator<?, ?>>> builtInList = builtinConstraints.get( annotationClass );

		if ( builtInList == null || builtInList.size() == 0 ) {
			throw new ValidationException( "Unable to find constraints for  " + annotationClass );
		}

		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraints =
				new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>( builtInList.size() );
		for ( Class<? extends ConstraintValidator<?, ?>> validatorClass : builtInList ) {
			//safe cause all CV for a given annotation A are CV<A, ?>
			@SuppressWarnings("unchecked")
			Class<ConstraintValidator<? extends Annotation, ?>> safeValdiatorClass = ( Class<ConstraintValidator<? extends Annotation, ?>> ) validatorClass;
			constraints.add( safeValdiatorClass );
		}

		return constraints;
	}

	public boolean isBuiltinConstraint(Class<? extends Annotation> annotationType) {
		return builtinConstraints.containsKey( annotationType );
	}

	/**
	 * Checks whether a given annotation is a multi value constraint or not.
	 *
	 * @param annotation the annotation to check.
	 *
	 * @return <code>true</code> if the specified annotation is a multi value constraints, <code>false</code>
	 *         otherwise.
	 */
	public boolean isMultiValueConstraint(Annotation annotation) {
		boolean isMultiValueConstraint = false;
		try {
			Method m = annotation.getClass().getMethod( "value" );
			Class returnType = m.getReturnType();
			if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
				Annotation[] annotations = ( Annotation[] ) m.invoke( annotation );
				for ( Annotation a : annotations ) {
					if ( isConstraintAnnotation( a ) || isBuiltinConstraint( a.annotationType() ) ) {
						isMultiValueConstraint = true;
					}
					else {
						isMultiValueConstraint = false;
						break;
					}
				}
			}
		}
		catch ( NoSuchMethodException nsme ) {
			// ignore
		}
		catch ( IllegalAccessException iae ) {
			// ignore
		}
		catch ( InvocationTargetException ite ) {
			// ignore
		}
		return isMultiValueConstraint;
	}


	/**
	 * Checks whether a given annotation is a multi value constraint and returns the contained constraints if so.
	 *
	 * @param annotation the annotation to check.
	 *
	 * @return A list of constraint annotations or the empty list if <code>annotation</code> is not a multi constraint
	 *         annotation.
	 */
	public <A extends Annotation> List<Annotation> getMultiValueConstraints(A annotation) {
		List<Annotation> annotationList = new ArrayList<Annotation>();
		try {
			Method m = annotation.getClass().getMethod( "value" );
			Class returnType = m.getReturnType();
			if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
				Annotation[] annotations = ( Annotation[] ) m.invoke( annotation );
				for ( Annotation a : annotations ) {
					if ( isConstraintAnnotation( a ) || isBuiltinConstraint( a.annotationType() ) ) {
						annotationList.add( a );
					}
				}
			}
		}
		catch ( NoSuchMethodException nsme ) {
			// ignore
		}
		catch ( IllegalAccessException iae ) {
			// ignore
		}
		catch ( InvocationTargetException ite ) {
			// ignore
		}
		return annotationList;
	}

	/**
	 * Checks whether the specified annotation is a valid constraint annotation. A constraint annotations has to
	 * fulfill the following conditions:
	 * <ul>
	 * <li>Has to contain a <code>ConstraintValidator</code> implementation.</li>
	 * <li>Defines a message parameter.</li>
	 * <li>Defines a group parameter.</li>
	 * <li>Defines a payload parameter.</li>
	 * </ul>
	 *
	 * @param annotation The annotation to test.
	 *
	 * @return <code>true</code> if the annotation fulfills the above condtions, <code>false</code> otherwise.
	 */
	public boolean isConstraintAnnotation(Annotation annotation) {

		Constraint constraint = annotation.annotationType()
				.getAnnotation( Constraint.class );
		if ( constraint == null ) {
			return false;
		}

		assertMessageParameterExists( annotation );
		assertGroupsParameterExists( annotation );
		assertPayloadParameterExists( annotation );

		assertNoParameterStartsWithValid( annotation );

		return true;
	}

	private void assertNoParameterStartsWithValid(Annotation annotation) {
		Method[] methods = annotation.getClass().getMethods();
		for ( Method m : methods ) {
			if ( m.getName().startsWith( "valid" ) ) {
				String msg = "Parameters starting with 'valid' are not allowed in a constraint.";
				throw new ConstraintDefinitionException( msg );
			}
		}
	}

	private void assertPayloadParameterExists(Annotation annotation) {
		try {
			Class<?>[] defaultPayload = ( Class<?>[] ) annotation.annotationType()
					.getMethod( "payload" )
					.getDefaultValue();
			if ( defaultPayload.length != 0 ) {
				String msg = annotation.annotationType()
						.getName() + " contains Constraint annotation, but the payload " +
						"paramter default value is not the empty array.";
				throw new ConstraintDefinitionException( msg );
			}
		}
		catch ( ClassCastException e ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but the " +
					"payload parameter is of wrong type.";
			throw new ConstraintDefinitionException( msg );
		}
		catch ( NoSuchMethodException nsme ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but does " +
					"not contain a payload parameter.";
			throw new ConstraintDefinitionException( msg );
		}
	}

	private void assertGroupsParameterExists(Annotation annotation) {
		try {
			Class<?>[] defaultGroups = ( Class<?>[] ) annotation.annotationType()
					.getMethod( "groups" )
					.getDefaultValue();
			if ( defaultGroups.length != 0 ) {
				String msg = annotation.annotationType()
						.getName() + " contains Constraint annotation, but the groups " +
						"paramter default value is not the empty array.";
				throw new ConstraintDefinitionException( msg );
			}
		}
		catch ( ClassCastException e ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but the " +
					"groups parameter is of wrong type.";
			throw new ConstraintDefinitionException( msg );
		}
		catch ( NoSuchMethodException nsme ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but does " +
					"not contain a groups parameter.";
			throw new ConstraintDefinitionException( msg );
		}
	}

	private void assertMessageParameterExists(Annotation annotation) {
		try {
			ReflectionHelper.getAnnotationParameter( annotation, "message", String.class );
		}
		catch ( Exception e ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but does " +
					"not contain a message parameter.";
			throw new ConstraintDefinitionException( msg );
		}
	}

	public <T extends Annotation> List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorDefinition(Class<T> annotationClass) {
		if ( annotationClass == null ) {
			throw new IllegalArgumentException( "Class cannot be null" );
		}

		final List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> list = constraintValidatorDefinitons.get(
				annotationClass
		);

		List<Class<? extends ConstraintValidator<T, ?>>> constraintsValidators =
				new ArrayList<Class<? extends ConstraintValidator<T, ?>>>( list.size() );
		for ( Class<? extends ConstraintValidator<?, ?>> validatorClass : list ) {
			//safe cause all CV for a given annotation A are CV<A, ?>
			@SuppressWarnings("unchecked")
			Class<ConstraintValidator<T, ?>> safeValdiatorClass = ( Class<ConstraintValidator<T, ?>> ) validatorClass;
			constraintsValidators.add( safeValdiatorClass );
		}

		return constraintsValidators;
	}

	public <A extends Annotation> void addConstraintValidatorDefinition(Class<A> annotationClass, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> definitionClasses) {
		constraintValidatorDefinitons.putIfAbsent( annotationClass, definitionClasses );
	}

	public boolean containsConstraintValidatorDefinition(Class<? extends Annotation> annotationClass) {
		return constraintValidatorDefinitons.containsKey( annotationClass );
	}
}
