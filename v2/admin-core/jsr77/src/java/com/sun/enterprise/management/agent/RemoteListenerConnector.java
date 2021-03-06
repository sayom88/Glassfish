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

package com.sun.enterprise.management.agent;

import java.rmi.Naming;
import javax.management.*;

/** 
 * RemoteListenerConnectors are instantiated by the ListenerRegistry
 * with the address of the callback port of the RemoteEventListener
 * and then registered with the MBeanServer by the MEJB.
 *
 * @author Hans Hrasna
 */
public class RemoteListenerConnector implements NotificationListener, java.io.Serializable {

    private String proxyAddress;	// the RMI address of the remote event listener
    private RemoteEventListener listener = null;  //the remote event listener
    private MBeanServer server = null;    // the MBeanServer holds the object this is listening to
                                   		  // which is set when this is registered in the MEJB
    private String id = hashCode() + ":" + System.currentTimeMillis();
    private boolean debug = false;

    public RemoteListenerConnector(String address) {
        proxyAddress = address;
    }

    public void handleNotification(Notification evt, Object h) {
        try {
            if (debug) System.out.println("RemoteListenerConnector.handleNotification()");
            if (listener == null) {
            	listener = (RemoteEventListener)Naming.lookup(proxyAddress);
            }
            listener.handleNotification(evt, h);
        } catch (java.rmi.RemoteException ce) {
            if (server != null) {
                if (debug) System.out.println("RemoteListenerConnector.server.removeNotificationListener("+ (ObjectName)evt.getSource() + ", " + this + ")");
                try {
                    server.removeNotificationListener((ObjectName)evt.getSource(), this);
                } catch (javax.management.ListenerNotFoundException e) {
                    if(debug) System.out.println(toString() + ": " + e); //occurs normally if event was fowarded from J2EEDomain
                } catch (Exception e1) {
                    System.out.println(toString() + ": " + e1);
                }
            }
        } catch (Exception e) {
            System.out.println(toString() + ": " + e);
            if (debug) {
            	try {
                	System.out.println("Naming.list(\"//localhost:1100\")");
                	String [] names = Naming.list("//localhost:1100");
            		for(int x=0;x<names.length;x++) {
                		System.out.println("names["+x+"] = " + names[x]);
            		}
            	} catch(Exception e1) {
                	e1.printStackTrace();
            	}
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setMBeanServer(MBeanServer s) {
        server = s;
    }
}
