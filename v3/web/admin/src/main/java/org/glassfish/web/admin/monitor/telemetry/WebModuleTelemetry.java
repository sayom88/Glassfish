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
package org.glassfish.web.admin.monitor.telemetry;

import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.*;
import org.glassfish.flashlight.client.ProbeListener;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
        
import org.glassfish.flashlight.provider.annotations.*;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
public class WebModuleTelemetry{
    private TreeNode webModuleNode;
    //Provides the longest response time for a request - not a cumulative value, 
    //but the largest response time from among the response times.
    private Counter maxTime = CounterFactory.createCount();
   
    
    public WebModuleTelemetry(TreeNode parent) {
        webModuleNode = TreeNodeFactory.createTreeNode("request", this, "web");
        parent.addChild(webModuleNode);
    }

    private Counter servletsTotal = CounterFactory.createCount();
    private Counter jspsTotal = CounterFactory.createCount();
    

    public void enableMonitoring(boolean isEnable) {
        //loop through the handles for this node and enable/disable the listeners
        //delegate the request to the child nodes
    }
    
    public void enableMonitoringForSubElements(boolean isEnable) {
        //loop through the children and enable/disable all
    }
    
    ThreadLocal<Long> entry = new ThreadLocal<Long>();

    @ProbeListener("web:webmodule::webModuleStartedEvent")
    public void webModuleStartedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName ) {
        System.out.println("[TM]webModuletStartedEvent received - appName = " + 
                            appName + ": hostName = " + hostName);
        
    }

    /**
     * Emits probe event that the web module with the given
     * <code>appName</code> has been unloaded from the virtual server with
     * the given <code>hostName</code>.
     */
    @ProbeListener("web:webmodule::webModuleStoppedEvent")
    public void webModuleStoppedEvent(
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName ) {
        
        System.out.println("[TM]webModuleStoppedEvent received - appName = " + 
                            appName + ": hostName = " + hostName);
    }
}
