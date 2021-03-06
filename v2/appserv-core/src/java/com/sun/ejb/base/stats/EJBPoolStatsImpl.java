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

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;

import com.sun.enterprise.admin.monitor.stats.EJBPoolStats;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;

import com.sun.enterprise.admin.monitor.stats.EJBPoolStats;
import com.sun.ejb.spi.stats.EJBPoolStatsProvider;

import java.util.logging.*;
import com.sun.enterprise.log.Log;
import com.sun.logging.*;

/**
 * A Class for providing pool stats for EJB Container
 *  Used by both Entity and Stateless Container
 *
 * @author Mahesh Kannan
 */

public class EJBPoolStatsImpl
    extends StatsImpl
    implements com.sun.enterprise.admin.monitor.stats.EJBPoolStats
{
    private EJBPoolStatsProvider delegate;

    private MutableCountStatisticImpl		jmsStat;
    private MutableBoundedRangeStatisticImpl	beansInPoolStat;
    private MutableBoundedRangeStatisticImpl	threadStat;
    private MutableCountStatisticImpl		createdStat;
    private MutableCountStatisticImpl		destroyedStat;

    public EJBPoolStatsImpl(EJBPoolStatsProvider delegate) {
	this.delegate = delegate;

	initialize();
    }

    protected void initialize() {
	super.initialize("com.sun.enterprise.admin.monitor.stats.EJBPoolStats");

	jmsStat = new MutableCountStatisticImpl(
		new CountStatisticImpl("JmsMaxMessagesLoad"));
	beansInPoolStat = new MutableBoundedRangeStatisticImpl(
		new BoundedRangeStatisticImpl("NumBeansInPool",
		    "Count", 0, delegate.getMaxPoolSize(),
		    delegate.getSteadyPoolSize()));
	threadStat = new MutableBoundedRangeStatisticImpl(
		new BoundedRangeStatisticImpl("NumThreadsWaiting"));
	createdStat = new MutableCountStatisticImpl(
		new CountStatisticImpl("TotalBeansCreated"));
	destroyedStat = new MutableCountStatisticImpl(
		new CountStatisticImpl("TotalBeansDestroyed"));
    }

    public CountStatistic getJmsMaxMessagesLoad() {
	jmsStat.setCount(delegate.getJmsMaxMessagesLoad());
	return (CountStatistic) jmsStat.modifiableView();
    }

    public BoundedRangeStatistic getNumBeansInPool() {
	beansInPoolStat.setCount(delegate.getNumBeansInPool());
	return (BoundedRangeStatistic) beansInPoolStat.modifiableView();
    }

    public BoundedRangeStatistic getNumThreadsWaiting() {
	threadStat.setCount(delegate.getNumThreadsWaiting());
	return (BoundedRangeStatistic) threadStat.modifiableView();
    }

    public CountStatistic getTotalBeansCreated() {
	createdStat.setCount(delegate.getTotalBeansCreated());
	return (CountStatistic) createdStat.modifiableView();
    }

    public CountStatistic getTotalBeansDestroyed() {
	destroyedStat.setCount(delegate.getTotalBeansDestroyed());
	return (CountStatistic) destroyedStat.modifiableView();
    }

}
