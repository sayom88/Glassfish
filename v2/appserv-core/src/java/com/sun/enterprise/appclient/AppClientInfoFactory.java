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

package com.sun.enterprise.appclient;

import com.sun.enterprise.deployment.annotation.AnnotationProcessorException;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXParseException;

/**
 * Factory class for creating the appropriate subtype of AppClientInfo based
 * on various factors.
 *
 * @author tjquinn
 */
public class AppClientInfoFactory {

    /** access to the localizable strings */
    protected static final StringManager localStrings = 
                        StringManager.getManager(AppClientInfoFactory.class);
    
    /**
     *Factory merhod that creates an object of the correct concrete type
     *for the given location and content.
     *@param locationFile File where the client is
     *@param mainClassFromCommandLine the main class as specified on the command line
     */
    public static AppClientInfo buildAppClientInfo(
            boolean isJWS,
            Logger logger,
            File locationFile, 
            String mainClassFromCommandLine, 
            String displayNameFromCommandLine, 
            String classFileFromCommandLine,
            URL[] persistenceURLs) 
                throws IOException, SAXParseException, ClassNotFoundException, 
                       URISyntaxException, AnnotationProcessorException, 
                       Exception, UserError {
        AppClientInfo result = null;

        /*
         *Check if the user specified a .class file on the command line.
         */
        if (classFileFromCommandLine != null) {
            /*
             *Yes, it's a .class file.  Use an app client archivist and, from 
             *it, get the default app client descriptor.  Then create the
             *new app client info instance.
             */
            Archivist archivist = new AppClientArchivist();
            result = new ClassFileAppClientInfo(
                    isJWS, 
                    logger, 
                    locationFile, 
                    archivist,
                    mainClassFromCommandLine, 
                    classFileFromCommandLine);
        } else {
            /*
             *The user did not specify a .class file on the command line, so
             *the locationFile argument refers to a valid module.
             *Construct an Archivist for the location file.
             */
            Archivist archivist = prepareArchivist(locationFile);
            
            if (archivist != null) {
                /*
                 *Choose which type of concrete AppClientInfo class is 
                 *suitable for this app client execution.
                 */
                if (archivist instanceof AppClientArchivist) {
                    result = new StandAloneAppClientInfo(
                            isJWS,
                            logger,
                            locationFile,
                            archivist,
                            mainClassFromCommandLine);
                } else if (archivist instanceof ApplicationArchivist) {
                    /*
                     *The descriptor should be of an application if it is not an
                     *app client descriptor.
                     */
                    result = new NestedAppClientInfo(
                            isJWS,
                            logger,
                            locationFile,
                            archivist,
                            mainClassFromCommandLine, 
                            displayNameFromCommandLine);
                } else {
                    /*
                     *The archivist factory recognized the archive as a valid
                     *one but it is not an app client or an application.  
                     *Reject it.
                     */
                    throw new UserError(localStrings.getString("appclient.unexpectedArchive", locationFile.getAbsolutePath()));
                }
            } else {
                /*
                 *The archivist is null, which means the user-provided location is
                 *not recognized as a known type of module.
                 */
                throw new UserError(localStrings.getString("appclient.invalidArchive", locationFile.getAbsolutePath()));
            }
        }
        result.completeInit(persistenceURLs);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(result.toString());
        }
        return result;
    }

    /**
     *Returns an archivist of the correct concrete type (app client or application)
     *given the contents of the archive.
     *@param archive the archive that contains the module
     *@param className
     *@return concrete Archivist of the correct type given the contents of the archive
     *@exeception IOException in case of error getting an archivist for the archive
     */
    private static Archivist prepareArchivist(File file) throws IOException {
        Archivist result = null;
        result = ArchivistFactory.getArchivistForArchive(file);
        return result;
    }
}
