/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.annotation.processing.logging;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

@SupportedAnnotationTypes({"org.glassfish.logging.annotation.LogMessageInfo","org.glassfish.logging.annotation.LogMessagesResourceBundle"})
public class LogMessagesResourceBundleGenerator extends BaseLoggingProcessor {

    private static final String DETAILS_SUFFIX = "_details";

    private static final String RESOURCE_BUNDLE_KEY = "resourceBundle";

    private static final String VALIDATE_LEVELS[] = {
      "EMERGENCY",
      "ALERT",
      "SEVERE",
    };
    
    private static final String LOG_MESSAGES_METADATA = "META-INF/logmessages/LogMessagesMetadata";
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process (Set<? extends TypeElement> annotations, 
            RoundEnvironment env) {

        debug("LogMessagesResourceBundleGenerator invoked.");
        
        if (!env.processingOver()) {

            LoggingMetadata logMessagesMap = new LoggingMetadata();
            LoggingMetadata logMessagesDetails = new LoggingMetadata();
            LoggingMetadata logMessagesMetada = new LoggingMetadata();

            Set<? extends Element> logMessageElements = env.getElementsAnnotatedWith(LogMessageInfo.class);
            Set<? extends Element> logMessagesResourceBundleElements = env.getElementsAnnotatedWith(LogMessagesResourceBundle.class);

            Set<String> rbNames = new HashSet<String>();

            if (logMessagesResourceBundleElements.isEmpty() || logMessageElements.isEmpty()) {
                loadLogMessages(logMessagesMetada, LOG_MESSAGES_METADATA);
                if (logMessagesMetada.containsKey(RESOURCE_BUNDLE_KEY)) {
                    String rb = (String) logMessagesMetada.get(RESOURCE_BUNDLE_KEY);
                    if (rb != null && !rb.isEmpty()) {
                        rbNames.add(rb);
                    }
                } else {
                    warn("Skipping LogMessages resource bundle generation, either the LogMessageInfo or LogMessagesResourceBundle annotation is not specified in the current compilation round.");
                    return false;                    
                }
            }
                        
            for (Element rbElem : logMessagesResourceBundleElements) {
                if (!(rbElem instanceof VariableElement)) {
                    error("The LogMessagesResourceBundle annotation is applied on an invalid element.");
                    return false;
                }
                Object rbValue = ((VariableElement) rbElem).getConstantValue();
                if (rbValue == null) {
                    error("The resource bundle name value could not be computed. Specify the LogMessagesResourceBundle annotation only on a compile time constant String literal field in the class.");
                    return false;                    
                }
                rbNames.add(rbValue.toString());
            }
            if (rbNames.isEmpty()) {
                error("No resource bundle name found. Atleast one String literal constant needs to be decorated with the LogMessagesResourceBundle annotation.");
                return false;                
            }
            if (rbNames.size() > 1) {
                error("More than one resource bundle name specified. Found the following resource bundle names: " 
                        + rbNames + ". Please specify only one resource bundle name per module.");
                return false;
            }
            
            String rbName = rbNames.iterator().next();
            if (!rbName.endsWith("LogMessages")) {
                error("The resource bundle name '" + rbName + "' annotated by @LogMessagesResourceBundle does not end with 'LogMessages'");
                return false;
            }

            Iterator<? extends Element> it = logMessageElements.iterator();
            Set<String> messageIds = new HashSet<String>();
            
            loadLogMessages(logMessagesMap, rbName);
            loadLogMessages(logMessagesDetails, rbName + DETAILS_SUFFIX);
            debug("Initial messages found so far: " + logMessagesMap);

            while (it.hasNext()) {
                Element elem = it.next();
                if (!(elem instanceof VariableElement)) {
                    error("The LogMessageInfo annotation is applied on an invalid element.");
                    return false;
                }
                VariableElement varElem = (VariableElement)elem;
                String msgId = (String)varElem.getConstantValue();
                if (msgId == null) {
                    error("The LogMessageInfo annotation is not applied on a String constant field.");
                    return false;                    
                }
                debug("Processing: " + msgId);
                // Message ids must be unique
                if (!messageIds.contains(msgId)) {
                    LogMessageInfo lmi = varElem.getAnnotation(LogMessageInfo.class);
                    checkLogMessageInfo(msgId, lmi);

                    // Save the log message...
                    logMessagesMap.put(msgId, lmi.message());
                    // Save the message's comment if it has one...
                    String comment = lmi.comment();
                    if (comment != null && !comment.isEmpty()) {
                        logMessagesMap.putComment(msgId, comment);
                        logMessagesDetails.put(msgId+".comment", comment);
                    }
                    String cause = lmi.cause();
                    if (cause == null) {
                        cause = "";
                    }
                    String action = lmi.action();
                    if (action == null) {
                        action = "";
                    }
                    String level = lmi.level();
                    if (level == null || level.isEmpty()) {
                        level = "INFO";
                    }
                    logMessagesDetails.put(msgId+".cause", cause);
                    logMessagesDetails.put(msgId+".action", action);
                    logMessagesDetails.put(msgId+".level", level);
                    messageIds.add(msgId);
                } else {
                    error("Duplicate use of message-id " + msgId);
                }
            }
            debug("Total Messages including ones found from disk so far: " + logMessagesMap);
            storeLogMessages(logMessagesMap, rbName);
            storeLogMessages(logMessagesDetails, rbName + DETAILS_SUFFIX);
            // Store the package name of the LogMessages resource
            logMessagesMetada.put(RESOURCE_BUNDLE_KEY, rbName);
            storeLogMessages(logMessagesMetada, LOG_MESSAGES_METADATA);
            info("Annotation processing finished successfully.");
            return true; // Claim the annotations
        } else {
            return false;
        }
    }    

    private void checkLogMessageInfo(String msgId, LogMessageInfo lmi) {
      boolean needsCheck = false;
      for (String checkLevel : VALIDATE_LEVELS) {
        if (checkLevel.equals(lmi.level())) {
          needsCheck = true;
        }
      }
      debug("Message " + msgId + " needs checking for cause/action: " + needsCheck);
      if (needsCheck) {
        if (lmi.cause().trim().length() == 0) {
          error("Missing cause for message id '" + msgId + "' for levels SEVERE and above.");
        }
        if (lmi.action().trim().length() == 0) {
          error("Missing action for message id '" + msgId + "' for levels SEVERE and above.");
        }
      }
    }
    
}
