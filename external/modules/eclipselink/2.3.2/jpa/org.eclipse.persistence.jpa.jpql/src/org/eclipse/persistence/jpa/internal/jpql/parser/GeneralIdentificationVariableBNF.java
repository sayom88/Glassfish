/*******************************************************************************
 * Copyright (c) 2006, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation
 *
 ******************************************************************************/
package org.eclipse.persistence.jpa.internal.jpql.parser;

/**
 * The query BNF for a general identification variable expression.
 *
 * <div nowrap><b>BNF:</b> <code>general_identification_variable ::= identification_variable |
 * KEY(identification_variable) | VALUE(identification_variable)</code><p>
 *
 * @version 2.3
 * @since 2.3
 * @author Pascal Filion
 */
@SuppressWarnings("nls")
public final class GeneralIdentificationVariableBNF extends JPQLQueryBNF {

	/**
	 * The unique identifier of this BNF rule.
	 */
	public static final String ID = "general_identification_variable";

	/**
	 * Creates a new <code>GeneralIdentificationVariableBNF</code>.
	 */
	GeneralIdentificationVariableBNF() {
		super(ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getFallbackBNFId() {
		return PreLiteralExpressionBNF.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void initialize() {
		super.initialize();
		registerExpressionFactory(IdentificationVariableFactory.ID);
		registerExpressionFactory(KeyExpressionFactory.ID);
		registerExpressionFactory(ValueExpressionFactory.ID);
	}
}