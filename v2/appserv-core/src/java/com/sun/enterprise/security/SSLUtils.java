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
package com.sun.enterprise.security;

import java.io.IOException;
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;


import com.sun.enterprise.config.clientbeans.Ssl;
import com.sun.enterprise.iiop.IIOPSSLSocketFactory;
import com.sun.enterprise.security.auth.login.ClientCertificateLoginModule;
import com.sun.enterprise.security.ssl.J2EEKeyManager;
import com.sun.enterprise.security.ssl.UnifiedX509KeyManager;
import com.sun.enterprise.security.ssl.UnifiedX509TrustManager;
import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.web.security.SSLSocketFactory;

import java.util.logging.*;
import com.sun.logging.*;

/**
 *  Handy class containing static functions.
 * @author Harpreet Singh
 * @author Vivek Nagar
 * @author Shing Wai Chan
 */
public final class SSLUtils {
    private static final String DEFAULT_KEYSTORE_PASS = "changeit";
    private static final String DEFAULT_TRUSTSTORE_PASS = "changeit";

    private static final String KEYSTORE_PASS_PROP = "javax.net.ssl.keyStorePassword";
    private static final String TRUSTSTORE_PASS_PROP = "javax.net.ssl.trustStorePassword";
    private static final String HTTPS_OUTBOUND_KEY_ALIAS = "com.sun.enterprise.security.httpsOutboundKeyAlias";

    private static Logger _logger=null;
    private static SecuritySupport secSupp = null;
    private static boolean hasKey = false;
    private static KeyManager keyManager = null;
    private static TrustManager trustManager = null;
    private static KeyStore mergedTrustStore = null;
    private static final Date initDate;

    static{
        _logger=LogDomains.getLogger(LogDomains.SECURITY_LOGGER);

        secSupp = SecurityUtil.getSecuritySupport();
        try {
            initDate = new Date();
            KeyStore[] keyStores = getKeyStores();
            initKeyManagers(keyStores, secSupp.getKeyStorePasswords());
            initTrustManagers(getTrustStores());
            if (keyStores != null && keyStores.length > 0) {
                for (int i = 0; i < keyStores.length; i++) {
                    Enumeration aliases = keyStores[i].aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = (String)aliases.nextElement();
                        if (keyStores[i].isKeyEntry(alias)) {
                            hasKey = true;
                            break;
                        }
                    }
                    if (hasKey) {
                        break;
                    }
                }
            }
            mergedTrustStore = mergingTrustStores(secSupp.getTrustStores());
        } catch(Exception ex) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "SSLUtils static init fails.", ex);
            }
            throw new IllegalStateException(ex);
        }
    }

    //XXX initStoresAtStartup may call more than once, should clean up later
    private static boolean initialized = false;

    private static Ssl appclientSsl = null;

    public static synchronized void initStoresAtStartup()
	throws Exception
    {
        if (initialized) {
            return;
        }

	SSLSocketFactory.setManagers(getKeyManagers(), getTrustManagers());
        // Creating a default SSLContext and HttpsURLConnection for clients 
        // that use Https
        SSLContext ctx = SSLContext.getInstance("TLS");
        String keyAlias = System.getProperty(HTTPS_OUTBOUND_KEY_ALIAS);
        KeyManager[] kMgrs = getKeyManagers();
        if (keyAlias != null && keyAlias.length() > 0 && kMgrs != null) {
            for (int i = 0; i < kMgrs.length; i++) {
                kMgrs[i] = new J2EEKeyManager((X509KeyManager)kMgrs[i], keyAlias);
            }
        }
	ctx.init(kMgrs, getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

        initialized = true;
    }

    public static KeyStore[] getKeyStores() throws Exception{
        return secSupp.getKeyStores();
    }

    public static KeyStore getKeyStore() throws Exception{
        return getKeyStores()[0];
    }

    public static KeyStore[] getTrustStores() throws Exception{
        return secSupp.getTrustStores();
    }

    public static KeyStore getTrustStore() throws Exception{
        return getTrustStores()[0];
    }

    /**
     * This API is for temporary purpose.  It will be removed once JSR 196
     * is updated.
     */
    public static KeyStore getMergedTrustStore() {
        return mergedTrustStore;
    }

    public static KeyManager[] getKeyManagers() throws Exception{
        return new KeyManager[] { keyManager };
    } 

    public static TrustManager[] getTrustManagers() throws Exception{
        return new TrustManager[] { trustManager };
    }

    public static void setAppclientSsl(Ssl ssl){
        appclientSsl = ssl;
    }

    public static Ssl getAppclientSsl() {
        return appclientSsl;
    }

    public static String getKeyStorePass () {
        //XXX need to revisit if the value should be cached
        return System.getProperty(KEYSTORE_PASS_PROP, DEFAULT_KEYSTORE_PASS);
    }

    public static String getTrustStorePass () {
        //XXX need to revisit if the value should be cached
        return System.getProperty(TRUSTSTORE_PASS_PROP, DEFAULT_TRUSTSTORE_PASS);
    }

    /**
     * This method checks whether a private key is available or not.
     */
    public static boolean isKeyAvailable() {
        return hasKey;
    }

    /**
     * Check whether given String is of the form [&lt;TokenName&gt;:]alias
     * where alias is an key entry.
     * @param certNickname
     * @return boolean
     */ 
    public static boolean isTokenKeyAlias(String certNickname) throws Exception {
        boolean isTokenKeyAlias = false;
        if (certNickname != null) {
            int ind = certNickname.indexOf(':');
            KeyStore[] kstores = getKeyStores();
            int count = -1;
            String aliasName = null;
            if (ind != -1) {
                String[] tokens = secSupp.getTokenNames();
                String tokenName = certNickname.substring(0, ind);
                aliasName = certNickname.substring(ind + 1);
                for (int i = 0; i < tokens.length; i++) {
                    if (tokenName.equals(tokens[i])) {
                        count = i;
                    }
                }
            }

            if (count != -1) {
                isTokenKeyAlias = kstores[count].isKeyEntry(aliasName);
            } else {
                for (int i = 0; i < kstores.length; i++) {
                    if (kstores[i].isKeyEntry(certNickname)) {
                        isTokenKeyAlias = true;
                        break;
                    }
                }
            }
        }
        return isTokenKeyAlias;
    }

    /**
     * Get a PrivateKeyEntry with certNickName is of the form
     * [&lt;TokenName&gt;:]alias where alias is an key entry.
     * @param certNickname
     * @return PrivateKeyEntry
     */ 
    public static PrivateKeyEntry getPrivateKeyEntryFromTokenAlias(
            String certNickname) throws Exception {
        PrivateKeyEntry privKeyEntry = null;
        if (certNickname != null) {
            int ind = certNickname.indexOf(':');
            KeyStore[] kstores = getKeyStores();
            int count = -1;
            String aliasName = certNickname;
            if (ind != -1) {
                String[] tokens = secSupp.getTokenNames();
                String tokenName = certNickname.substring(0, ind);
                aliasName = certNickname.substring(ind + 1);
                for (int i = 0; i < tokens.length; i++) {
                    if (tokenName.equals(tokens[i])) {
                        count = i;
                    }
                }
            }

            String[] passwords = secSupp.getKeyStorePasswords();
            if (count != -1 && passwords.length >= count) {
                Key key = kstores[count].getKey(
                        aliasName, passwords[count].toCharArray());
                if (key instanceof PrivateKey) {
                    PrivateKey privKey = (PrivateKey)key;
                    Certificate[] certs = kstores[count].getCertificateChain(
                            aliasName);
                    privKeyEntry = new PrivateKeyEntry(privKey, certs);
                }
            } else {
                for (int i = 0; i < kstores.length; i++) {
                    Key key = kstores[i].getKey(
                            aliasName, passwords[i].toCharArray());
                    if (key != null && key instanceof PrivateKey) {
                        PrivateKey privKey = (PrivateKey)key;
                        Certificate[] certs =
                                kstores[i].getCertificateChain(
                                aliasName);
                        privKeyEntry = new PrivateKeyEntry(privKey, certs);
                        break;
                    }
                }
            }
            passwords = null;
        }

        return privKeyEntry;
    }

    private static void initKeyManagers(KeyStore[] kstores, String[] pwds) 
            throws Exception {

        ArrayList keyManagers = new ArrayList();
        for (int i = 0; i < kstores.length; i++) {
            checkCertificateDates(kstores[i]);
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
	    kmf.init(kstores[i], pwds[i].toCharArray());
            KeyManager[] kmgrs = kmf.getKeyManagers();
            if (kmgrs != null) {
                for (int j = 0; j < kmgrs.length; j++) {
                     keyManagers.add(kmgrs[j]);
                }
            }
        }

        keyManager = new UnifiedX509KeyManager(
            (X509KeyManager [])keyManagers.toArray(
                new X509KeyManager[keyManagers.size()]),
            secSupp.getTokenNames());
    }
    
    private static void initTrustManagers(KeyStore[] tstores) throws Exception {
        ArrayList trustManagers = new ArrayList();
        for (int i = 0; i < tstores.length; i++) {
            checkCertificateDates(tstores[i]);
	    TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(tstores[i]);
            TrustManager[] tmgrs = tmf.getTrustManagers();
            if (tmgrs != null) {
                for (int j = 0; j < tmgrs.length; j++) {
                     trustManagers.add(tmgrs[j]);
                }
            }
        }
        if (trustManagers.size() == 1) {
            trustManager = (TrustManager)trustManagers.get(0);
        } else {
            trustManager = new UnifiedX509TrustManager((X509TrustManager [])trustManagers.toArray(new X509TrustManager[trustManagers.size()]));
        }
    }

    private static KeyStore mergingTrustStores(KeyStore[] trustStores)
            throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException {
        KeyStore mergedStore = null;
        try {
            mergedStore = KeyStore.getInstance("CaseExactJKS");
        } catch(KeyStoreException ex) {
            mergedStore = KeyStore.getInstance("JKS");
        }
        String[] passwords = secSupp.getKeyStorePasswords();
        mergedStore.load(null,
                passwords[passwords.length - 1].toCharArray());

        String[] tokens = secSupp.getTokenNames();
        for (int i = 0; i < trustStores.length; i++) {
            Enumeration aliases = trustStores[i].aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String)aliases.nextElement();
                Certificate cert = trustStores[i].getCertificate(alias);

                //need to preserve the token:alias name format
                String alias2 = (i < tokens.length - 1)? tokens[i] + ":" + alias : alias;

                String alias3 = alias2;
                boolean alreadyInStore = false;
                Certificate aCert = null;
                int count = 1;
                while ((aCert = mergedStore.getCertificate(alias3)) != null) {
                    if (aCert.equals(cert)) {
                        alreadyInStore = true;
                        break;
                    }
                    alias3 = alias2 + "__" + count++;
                }
                if (!alreadyInStore) {
                    mergedStore.setCertificateEntry(alias3, cert);
                }
            }
        }
        return mergedStore;
     }
    
    /*
     * Check X509 certificates in a store for expiration.
     */
    private static void checkCertificateDates(KeyStore store)
        throws KeyStoreException {
        
        Enumeration<String> aliases = store.aliases();
        while (aliases.hasMoreElements()) {
            Certificate cert = store.getCertificate(aliases.nextElement());
            if (cert instanceof X509Certificate) {
                if (((X509Certificate) cert).getNotAfter().before(initDate)) {
                    _logger.log(Level.SEVERE,
                        "java_security.expired_certificate",
                        cert);
                }
            }
        }
    }
    
}
