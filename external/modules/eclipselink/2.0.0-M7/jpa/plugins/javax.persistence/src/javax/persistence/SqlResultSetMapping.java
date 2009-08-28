/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * Contributors:
 *     dclarke - Java Persistence 2.0 - Proposed Final Draft (March 13, 2009)
 *     		     Specification available from http://jcp.org/en/jsr/detail?id=317
 *
 * Java(TM) Persistence API, Version 2.0 - EARLY ACCESS
 * This is an implementation of an early-draft specification developed under the 
 * Java Community Process (JCP).  The code is untested and presumed not to be a  
 * compatible implementation of JSR 317: Java(TM) Persistence API, Version 2.0.   
 * We encourage you to migrate to an implementation of the Java(TM) Persistence 
 * API, Version 2.0 Specification that has been tested and verified to be compatible 
 * as soon as such an implementation is available, and we encourage you to retain 
 * this notice in any implementation of Java(TM) Persistence API, Version 2.0 
 * Specification that you distribute.
 ******************************************************************************/
package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * This annotation is used to specify the mapping of the result 
 * of a native SQL query.
 *
 * <pre>
 *    Example:
 *
 *    Query q = em.createNativeQuery(
 *        "SELECT o.id AS order_id, " +
 *            "o.quantity AS order_quantity, " +
 *            "o.item AS order_item, " +
 *            "i.name AS item_name, " +
 *        "FROM Order o, Item i " +
 *        "WHERE (order_quantity > 25) AND (order_item = i.id)",
 *    "OrderResults");
 *    
 *    &#064;SqlResultSetMapping(name="OrderResults", 
 *        entities={ 
 *            &#064;EntityResult(entityClass=com.acme.Order.class, fields={
 *                &#064;FieldResult(name="id", column="order_id"),
 *                &#064;FieldResult(name="quantity", column="order_quantity"), 
 *                &#064;FieldResult(name="item", column="order_item")})},
 *        columns={
 *            &#064;ColumnResult(name="item_name")}
 *    )
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface SqlResultSetMapping { 

    /** 
     * The name given to the result set mapping, and used to refer 
     * to it in the methods of the {@link Query} API.
     */
    String name(); 

    /** Specifies the result set mapping to entities. */
    EntityResult[] entities() default {};

    /** Specifies the result set mapping to scalar values. */
    ColumnResult[] columns() default {};
}
