

package org.glassfish.admin.amx.base;
import javax.management.ObjectName;

import java.util.Set;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.external.amx.AMXGlassfish;

import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
    MBean providing server-side support for AMX eg for efficiency or other
    reasons.
 */
@AMXMBeanMetadata(type="mbean-tracker",singleton=true, globalSingleton=true, leaf=true)
public interface MBeanTrackerMBean
{
    public static final ObjectName MBEAN_TRACKER_OBJECT_NAME = JMXUtil.newObjectName(AMXGlassfish.DEFAULT.amxSupportDomain(), "type=mbean-tracker");

    /**
        Get all children of the specified MBean.  An empty set is returned
        if no children are found.
    */
    @ManagedOperation
    public Set<ObjectName> getChildrenOf(final ObjectName parent);
    
    @ManagedOperation
    public ObjectName getParentOf(final ObjectName child);
    
    @ManagedAttribute
    public boolean getEmitMBeanStatus();
    
    @ManagedAttribute
    public void setEmitMBeanStatus(boolean emit);
}











