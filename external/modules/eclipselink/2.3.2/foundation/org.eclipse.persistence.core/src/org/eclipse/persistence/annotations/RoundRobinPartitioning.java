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
 *     James Sutherland - initial API and implementation
 ******************************************************************************/  
package org.eclipse.persistence.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
/** 
 * RoundRobinPartitioning sends requests in a round robin fashion to the set of connection pools.
 * It is for load-balancing read queries across a cluster of database machines.
 * It requires that the full database be replicated on each machine, so does not support partitioning.
 * The data should either be read-only, or writes should be replicated on the database.
 * <p>
 * Partitioning can be enabled on an Entity, relationship, query, or session/persistence unit.
 * Partition policies are globally named to allow reuse,
 * the partitioning policy must also be set using the @Partitioned annotation to be used.
 * 
 * @see Partitioned
 * @see org.eclipse.persistence.descriptors.partitioning.RoundRobinPartitioningPolicy
 * @author James Sutherland
 * @since EclipseLink 2.2
 */ 
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface RoundRobinPartitioning {
    /**
     * The name of the partition policy, names must be unique for the persistence unit.
     */
    String name();
    
    /**
     * List of connection pool names to load balance across.
     * Defaults to all defined pools in the ServerSession.
     */
    String[] connectionPools() default {};
    
    /**
     * This allows for a set of database to be written to and kept in synch,
     * and have reads load-balanced across the databases.
     */
    boolean replicateWrites() default false;
}
