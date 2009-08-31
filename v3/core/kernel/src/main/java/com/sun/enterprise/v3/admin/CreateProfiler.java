/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin;

import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.Properties;

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Create Profiler Command
 *
 */
@Service(name="create-profiler")
@Scoped(PerLookup.class)
@I18n("create.profiler")
public class CreateProfiler implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateProfiler.class);

    @Param(optional=true)
    String classpath;

    @Param(optional=true, defaultValue="true")
    Boolean enabled;

    @Param(name="nativelibrarypath", optional=true)
    String nativeLibraryPath;

    @Param(name="profiler_name", primary=true)
    String name;

    @Param(name="property", optional=true, separator=':')
    Properties properties;

    @Param(optional=true)
    String target;

    @Inject
    JavaConfig javaConfig;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (javaConfig.getProfiler() != null) {
            System.out.println("profiler exists. Please delete it first");
            report.setMessage(
                localStrings.getLocalString("create.profiler.alreadyExists",
                "profiler exists. Please delete it first"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {

                public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                    Profiler newProfiler = param.createChild(Profiler.class);
                    newProfiler.setName(name);
                    newProfiler.setClasspath(classpath);
                    newProfiler.setEnabled(enabled.toString());
                    newProfiler.setNativeLibraryPath(nativeLibraryPath);
                    if (properties != null) {
                        for ( Map.Entry e : properties.entrySet()) {
                            Property prop = newProfiler.createChild(Property.class);
                            prop.setName((String)e.getKey());
                            prop.setValue((String)e.getValue());
                            newProfiler.getProperty().add(prop);
                        }
                    }
                    param.setProfiler(newProfiler);                    
                    return newProfiler;
                }
            }, javaConfig);

        } catch(TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.profiler.fail", "{0} create failed ", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
