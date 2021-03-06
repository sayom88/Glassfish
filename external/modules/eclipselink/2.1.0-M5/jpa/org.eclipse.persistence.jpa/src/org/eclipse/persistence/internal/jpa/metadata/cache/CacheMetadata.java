/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 *     05/16/2008-1.0M8 Guy Pelletier 
 *       - 218084: Implement metadata merging functionality between mapping files
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.cache;

import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.CacheCoordinationType;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.invalidation.DailyCacheInvalidationPolicy;
import org.eclipse.persistence.descriptors.invalidation.TimeToLiveCacheInvalidationPolicy;
import org.eclipse.persistence.exceptions.ValidationException;

import org.eclipse.persistence.internal.jpa.metadata.MetadataDescriptor;
import org.eclipse.persistence.internal.jpa.metadata.ORMetadata;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAccessibleObject;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAnnotation;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataClass;

/**
 * Object to hold onto cache metadata. This class should eventually be 
 * extended by an XMLCache.
 * 
 * @author Guy Pelletier
 * @since TopLink 11g
 */
public class CacheMetadata extends ORMetadata {
    protected Boolean m_alwaysRefresh;
    protected Boolean m_disableHits;
    protected Boolean m_shared;
    protected Boolean m_refreshOnlyIfNewer;
    
    protected String m_coordinationType;
    protected String m_type;
    
    protected Integer m_expiry;
    protected Integer m_size;
    
    protected TimeOfDayMetadata m_expiryTimeOfDay;

    /**
     * INTERNAL:
     */
    public CacheMetadata() {
        super("<cache>");
    }
    
    /**
     * INTERNAL:
     */
    public CacheMetadata(MetadataAnnotation cache, MetadataAccessibleObject accessibleObject) {
        super(cache, accessibleObject);
        
        m_alwaysRefresh = (Boolean) cache.getAttribute("alwaysRefresh");
        m_disableHits = (Boolean) cache.getAttribute("disableHits");
        m_coordinationType = (String) cache.getAttribute("coordinationType");
        m_expiry = (Integer) cache.getAttribute("expiry");

        MetadataAnnotation expiryTimeOfDay = (MetadataAnnotation) cache.getAttribute("expiryTimeOfDay");
        
        if (expiryTimeOfDay != null) {
            m_expiryTimeOfDay = new TimeOfDayMetadata(expiryTimeOfDay, accessibleObject);
        }
        
        m_shared = (Boolean) cache.getAttribute("shared");
        m_size = (Integer) cache.getAttribute("size");
        m_type = (String) cache.getAttribute("type");
        m_refreshOnlyIfNewer = (Boolean) cache.getAttribute("refreshOnlyIfNewer");
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Boolean getAlwaysRefresh() {
        return m_alwaysRefresh; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public String getCoordinationType() {
        return m_coordinationType; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Boolean getDisableHits() {
        return m_disableHits; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Integer getExpiry() {
        return m_expiry; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public TimeOfDayMetadata getExpiryTimeOfDay() { 
        return m_expiryTimeOfDay;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Boolean getRefreshOnlyIfNewer() {
       return m_refreshOnlyIfNewer; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Boolean getShared() {
       return m_shared; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Integer getSize() {
        return m_size;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public String getType() {
       return m_type;
    }
    
    /**
     * INTERNAL:
     */
    public void process(MetadataDescriptor descriptor, MetadataClass javaClass) {
        // Set the cache flag on the metadata Descriptor.
        descriptor.setHasCache();
        
        // Process the cache metadata.
        ClassDescriptor classDescriptor = descriptor.getClassDescriptor();
        
        // Process type
        if (m_type == null) {
            // Leave as default.
        } else if (m_type.equals(CacheType.SOFT_WEAK.name())) {
            classDescriptor.useSoftCacheWeakIdentityMap();
        } else if (m_type.equals(CacheType.FULL.name())) {
            classDescriptor.useFullIdentityMap();
        } else if (m_type.equals(CacheType.WEAK.name())) {
            classDescriptor.useWeakIdentityMap();
        }  else if (m_type.equals(CacheType.SOFT.name())) {
            classDescriptor.useSoftIdentityMap();
        } else if (m_type.equals(CacheType.HARD_WEAK.name())) {
            classDescriptor.useHardCacheWeakIdentityMap();
        } else if (m_type.equals(CacheType.CACHE.name())) {
            classDescriptor.useCacheIdentityMap();
        } else if (m_type.equals(CacheType.NONE.name())) {
            classDescriptor.useNoIdentityMap();
        }
        
        // Process size.
        if (m_size != null) {
            classDescriptor.setIdentityMapSize(m_size);
        }
        
        // Process shared.
        if (m_shared != null) {
            classDescriptor.setIsIsolated(!m_shared);
        }
        
        // Process expiry or expiry time of day.
        if (m_expiryTimeOfDay == null) {
            // Expiry time of day is not specified, look for an expiry.
            if (m_expiry != null && m_expiry != -1) {
                classDescriptor.setCacheInvalidationPolicy(new TimeToLiveCacheInvalidationPolicy(m_expiry));
            }
        } else {
            // Expiry time of day is specified, if expiry is also specified, 
            // throw an exception.
            if (m_expiry == null || m_expiry == -1) {
                classDescriptor.setCacheInvalidationPolicy(new DailyCacheInvalidationPolicy(m_expiryTimeOfDay.processHour(), m_expiryTimeOfDay.processMinute(), m_expiryTimeOfDay.processSecond(), m_expiryTimeOfDay.processMillisecond()));
            } else {
                throw ValidationException.cacheExpiryAndExpiryTimeOfDayBothSpecified(javaClass);
            }
        }
        
        // Process always refresh.
        if (m_alwaysRefresh != null) {
            classDescriptor.setShouldAlwaysRefreshCache(m_alwaysRefresh);
        }
        
        // Process refresh only if newer.
        if (m_refreshOnlyIfNewer != null) {
            classDescriptor.setShouldOnlyRefreshCacheIfNewerVersion(m_refreshOnlyIfNewer);
        }
        
        // Process disable hits.
        if (m_disableHits != null) {
            classDescriptor.setShouldDisableCacheHits(m_disableHits);
        }
        
        // Process coordination type.
        if (m_coordinationType == null) {
            // Leave as default.
        } else if (m_coordinationType.equals(CacheCoordinationType.SEND_OBJECT_CHANGES.name())) {
            classDescriptor.setCacheSynchronizationType(ClassDescriptor.SEND_OBJECT_CHANGES);
        } else if (m_coordinationType.equals(CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS.name())) {
            classDescriptor.setCacheSynchronizationType(ClassDescriptor.INVALIDATE_CHANGED_OBJECTS);
        } else if (m_coordinationType.equals(CacheCoordinationType.SEND_NEW_OBJECTS_WITH_CHANGES.name())) {
            classDescriptor.setCacheSynchronizationType(ClassDescriptor.SEND_NEW_OBJECTS_WITH_CHANGES);
        } else if (m_coordinationType.equals(CacheCoordinationType.NONE.name())) {
            classDescriptor.setCacheSynchronizationType(ClassDescriptor.DO_NOT_SEND_CHANGES);
        }
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping 
     */
    public void setAlwaysRefresh(Boolean alwaysRefresh) {
        m_alwaysRefresh = alwaysRefresh;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setCoordinationType(String coordinationType) {
        m_coordinationType = coordinationType; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setDisableHits(Boolean disableHits) {
        m_disableHits = disableHits; 
    }

    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setExpiry(Integer expiry) {
       m_expiry = expiry; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setExpiryTimeOfDay(TimeOfDayMetadata expiryTimeOfDay) { 
        m_expiryTimeOfDay = expiryTimeOfDay;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setRefreshOnlyIfNewer(Boolean refreshOnlyIfNewer) {
        m_refreshOnlyIfNewer = refreshOnlyIfNewer;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setShared(Boolean shared) {
       m_shared = shared; 
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setSize(Integer size) {
        m_size = size;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setType(String type) {
       m_type = type;
    }
}
