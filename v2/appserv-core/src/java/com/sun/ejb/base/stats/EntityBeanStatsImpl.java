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

package com.sun.ejb.base.stats;

import javax.management.j2ee.statistics.EntityBeanStats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;

import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;

import com.sun.ejb.spi.stats.EntityBeanStatsProvider;

import java.util.logging.*;
import com.sun.enterprise.log.Log;
import com.sun.logging.*;

/**
 * A Class for providing stats for Entity Container.
 *
 * @author Mahesh Kannan
 */

public class EntityBeanStatsImpl
    extends EJBStatsImpl
    implements javax.management.j2ee.statistics.EntityBeanStats
{
    protected EntityBeanStatsProvider		entityDelegate;

    private MutableBoundedRangeStatisticImpl	pooledCountStat;
    private MutableBoundedRangeStatisticImpl	readyCountStat;

    public EntityBeanStatsImpl(EntityBeanStatsProvider delegate) {
	super(delegate, "javax.management.j2ee.statistics.EntityBeanStats");
	this.entityDelegate = delegate;

	initStats();
    }

    private void initStats() {
	pooledCountStat = new MutableBoundedRangeStatisticImpl(
	    new BoundedRangeStatisticImpl("PooledCount",
		"Count", 0, entityDelegate.getMaxPoolSize(),
		entityDelegate.getSteadyPoolSize()));
	readyCountStat = new MutableBoundedRangeStatisticImpl(
	    new BoundedRangeStatisticImpl("ReadyCount",
		"Count", 0, entityDelegate.getMaxCacheSize(), 0));
    }

    public RangeStatistic getPooledCount() {
	pooledCountStat.setCount(entityDelegate.getPooledCount());
	return (RangeStatistic) pooledCountStat.modifiableView();
    }

    public RangeStatistic getReadyCount() {
	readyCountStat.setCount(entityDelegate.getReadyCount());
	return (RangeStatistic) readyCountStat.modifiableView();
    }

}
