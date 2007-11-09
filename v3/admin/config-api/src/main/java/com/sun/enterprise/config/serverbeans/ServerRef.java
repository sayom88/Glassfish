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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "healthChecker"
}) */
@Configured
public class ServerRef extends ConfigBean implements Ref, Serializable {

    final transient private VetoableChangeSupport support = new VetoableChangeSupport(this);
    
    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String ref;
    @Attribute

    protected String disableTimeoutInMinutes;
    @Attribute

    protected String lbEnabled;
    @Attribute

    protected String enabled;
    @Element
    protected HealthChecker healthChecker;



    /**
     * Gets the value of the ref property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRef(String value) throws PropertyVetoException {
        support.fireVetoableChange("ref", this.ref, value);

        this.ref = value;
    }

    /**
     * Gets the value of the disableTimeoutInMinutes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDisableTimeoutInMinutes() {
        if (disableTimeoutInMinutes == null) {
            return "30";
        } else {
            return disableTimeoutInMinutes;
        }
    }

    /**
     * Sets the value of the disableTimeoutInMinutes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDisableTimeoutInMinutes(String value) throws PropertyVetoException {
        support.fireVetoableChange("disableTimeoutInMinutes", this.disableTimeoutInMinutes, value);

        this.disableTimeoutInMinutes = value;
    }

    /**
     * Gets the value of the lbEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getLbEnabled() {
        if (lbEnabled == null) {
            return "false";
        } else {
            return lbEnabled;
        }
    }

    /**
     * Sets the value of the lbEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLbEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("lbEnabled", this.lbEnabled, value);

        this.lbEnabled = value;
    }

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getEnabled() {
        if (enabled == null) {
            return "true";
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("enabled", this.enabled, value);

        this.enabled = value;
    }

    /**
     * Gets the value of the healthChecker property.
     *
     * @return possible object is
     *         {@link HealthChecker }
     */
    public HealthChecker getHealthChecker() {
        return healthChecker;
    }

    /**
     * Sets the value of the healthChecker property.
     *
     * @param value allowed object is
     *              {@link HealthChecker }
     */
    public void setHealthChecker(HealthChecker value) throws PropertyVetoException {
        support.fireVetoableChange("healthChecker", this.healthChecker, value);

        this.healthChecker = value;
    }



}
