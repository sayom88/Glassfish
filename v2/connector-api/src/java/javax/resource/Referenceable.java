/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.resource;

import javax.naming.Reference;

/** The Referenceable interface extends the javax.naming.Referenceable
 *  interface. It enables support for JNDI Reference mechanism for 
 *  the registration of the connection factory in the JNDI name space. 
 *  Note that the implementation and structure of Reference is specific
 *  to an application server.
 *
 *  <p>The implementation class for a connection factory interface is 
 *  required to implement both java.io.Serializable and 
 *  javax.resource.Referenceable interfaces to support JNDI registration.
 *
 *  @version     0.9
 *  @author      Rahul Sharma
 *
**/

public interface Referenceable extends javax.naming.Referenceable {

  /** Sets the Reference instance. This method is called by the
   *  deployment code to set the Reference that can be later
   *  returned by the getReference method (as defined in the
   *  javax.naming.Referenceable interface).
   *
   *  @param   reference  A Reference instance
   *  @see     javax.naming.Referenceable#getReference
   *
  **/
  public 
  void setReference(Reference reference);

}
