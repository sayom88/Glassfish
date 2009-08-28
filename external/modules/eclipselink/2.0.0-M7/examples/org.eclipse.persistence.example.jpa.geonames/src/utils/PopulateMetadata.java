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
 *     dclarke - initial GeoNames EclipseLink JPA example
 ******************************************************************************/
package utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import model.geonames.Continent;
import utils.load.*;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.0
 */
public class PopulateMetadata {

	public static void main(String[] args) {
		EntityManagerFactory emf = PersistenceHelper.createEMF(null, true);
		EntityManager em = emf.createEntityManager();
		CreateSchema.deleteAll(em);

		try {
			em.getTransaction().begin();

			for (Continent continent : Continent.getAllContinents()) {
				em.persist(continent);
			}
			new FeatureLoader().load(em);
			new TimeZoneLoader().load(em);

			LanguageLoader langLoader = new LanguageLoader();
			langLoader.load(em);

			new CountryLoader().load(em, langLoader);
			new AdminCodesLoader().load(em);

			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			emf.close();
		}
	}

}
