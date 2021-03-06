/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.interfaces;

public class Asset {

    public CompanyAsset asset;

    public Asset() {
        super();
    }

    public Object clone() {
        Asset object = new Asset();
        object.asset = (CompanyAsset)this.asset.clone();
        return object;
    }

    public CompanyAsset getAsset() {
        return this.asset;
    }

    public java.math.BigDecimal getSerNum() {
        return null;
    }

    public void setAsset(CompanyAsset ca) {
        this.asset = ca;
    }
}
