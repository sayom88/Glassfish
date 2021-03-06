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

/*
 * CertificateArgumentHandler.java
 *
 * Created on August 25, 2005, 3:24 PM
 */

package com.sun.enterprise.tools.upgrade.common.arguments;

import com.sun.enterprise.tools.upgrade.common.ArgsParser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;

/**
 *
 * @author Hans Hrasna
 */
public abstract class CertificateArgumentHandler extends ArgumentHandler {
    
    /** Creates a new instance of CertificateArgumentHandler */
    public CertificateArgumentHandler(ParsedArgument pa) {
        super(pa);
        commonInfo.setCertificateConversionFlag(true);
    }
    
    /** processCertificatePasswords
     *  Reads password triplets or doublets from passwordFile and sets them in CommonInfoModel
     *  format: domain_name instance_name password
     *      or: domain_name password
     */
    protected void processCertificatePasswords(String pwdfile) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pwdfile)));
            while(reader.ready()){
                String entry = reader.readLine();
                StringTokenizer tokens = new StringTokenizer(entry, " ", false);
                int tokenCount = tokens.countTokens();
                if (tokenCount < 1){
                    _logger.severe(sm.getString("enterprise.tools.upgrade.cli.password_missing",pwdfile));
                    System.exit(1);
                }
                if (tokenCount > 1) {
                    String domainName = tokens.nextToken();                    
                    commonInfo.setCurrentDomain(domainName);
                    commonInfo.addDomainOptionName(domainName);
                }
                if (tokenCount == 3) {
                    commonInfo.setCurrentSourceInstance(tokens.nextToken());
                }
                setCertificatePassword(tokens.nextToken());
                if(commonInfo.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
                    interactiveMap.put(ArgsParser.DOMAIN + "-" + commonInfo.getCurrentDomain() + ":" + commonInfo.getCurrentSourceInstance(), pwdfile);
                } else interactiveMap.put(ArgsParser.DOMAIN + "-" + commonInfo.getCurrentDomain(), pwdfile);
            }
        } catch(FileNotFoundException fe) {
            helpUsage();
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.cli.password_file_missing",pwdfile),fe);
            System.exit(1);
        } catch(Exception io) {
            _logger.log(Level.SEVERE,sm.getString("enterprise.tools.upgrade.cli.password_missing",pwdfile),io);
            System.exit(1);
        }
    }
    
    protected abstract void setCertificatePassword(String pwd);
}
