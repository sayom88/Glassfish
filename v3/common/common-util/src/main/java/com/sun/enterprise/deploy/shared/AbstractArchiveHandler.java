package com.sun.enterprise.deploy.shared;


import java.io.File;
import java.net.URI;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.deployment.archive.ArchiveHandler;

/**
 * Common methods for ArchiveHandler implementations
 *
 * @author Jerome Dochez
 */
public abstract class AbstractArchiveHandler implements ArchiveHandler {

    /**
     * Prepares the jar file to a format the ApplicationContainer is
     * expecting. This could be just a pure unzipping of the jar or
     * nothing at all.
     *
     * @param source of the expanding
     * @param target of the expanding
     */
    public void expand(ReadableArchive source, WritableArchive target) throws IOException {

        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            InputStream is = new BufferedInputStream(source.getEntry(entryName));
            OutputStream os = null;
            try {
                os = target.putNextEntry(entryName);
                FileUtils.copy(is, os, source.getEntrySize(entryName));
            } finally {
                if (os!=null) {
                    target.closeEntry();
                }
                if (is!=null) {
                    is.close();
                }
            }
        }
    }
    
    /**
     * Returns the default application name usable for identifying the archive.
     * <p>
     * This default implementation returns the name portion of 
     * the archive's URI.  The archive's name depends on the type of archive 
     * (FileArchive vs. JarArchive vs. MemoryMappedArchive, for example).  
     * <p>
     * A concrete subclass can override this method to provide an alternative 
     * way of deriving the default application name.
     * 
     * @param archive the archive for which the default name is needed
     * @return the default application name for the specified archive
     */
    public String getDefaultApplicationName(ReadableArchive archive) {
        String appName = archive.getName();
        int lastDot = appName.lastIndexOf('.');
        if (lastDot != -1) {
            if (appName.substring(lastDot).equalsIgnoreCase("." + getArchiveType())) {
                appName = appName.substring(0, lastDot);
            }
        }
        return appName;
    }
    
}
