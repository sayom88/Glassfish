/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.engines.xslt;

import java.util.*;
import java.io.*;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.*;

import org.apache.xpath.objects.XObject;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.apache.bsf.debug.util.DebugLog;

/**
 * Xerces XSLT interface to BSF. Requires Xalan and Xerces from Apache.
 * 
 * This integration uses the BSF registry to pass in any src document
 * and stylesheet base URI that the user may wish to set. 
 *
 * @author   Sanjiva Weerawarana
 * @author   Sam Ruby
 *
 * Re-implemented for the Xalan 2 codebase
 * 
 * @author   Victor J. Orlikowski
 */
public class XSLTEngine extends BSFEngineImpl {
    TransformerFactory tFactory;
    Transformer transformer;

    /**
     * call the named method of the given object.
     */
    public Object call (Object object, String method, Object[] args) 
        throws BSFException {
	throw new BSFException (BSFException.REASON_UNSUPPORTED_FEATURE,
                                "BSF:XSLTEngine can't call methods");
    }

    /**
     * Declare a bean by setting it as a parameter
     */
    public void declareBean (BSFDeclaredBean bean) throws BSFException {
        transformer.setParameter (bean.name, new XObject (bean.bean));
    }

    /**
     * Evaluate an expression. In this case, an expression is assumed
     * to be a stylesheet of the template style (see the XSLT spec).
     */
    public Object eval (String source, int lineNo, int columnNo, 
                        Object oscript) throws BSFException {
	// get the style base URI (the place from where Xerces XSLT will
	// look for imported/included files and referenced docs): if a
	// bean named "xslt:styleBaseURI" is registered, then cvt it
	// to a string and use that. Otherwise use ".", which means the
	// base is the directory where the process is running from
	Object sbObj = mgr.lookupBean ("xslt:styleBaseURI");
	String styleBaseURI = (sbObj == null) ? "." : sbObj.toString ();

	// Locate the stylesheet.
	StreamSource styleSource;

        styleSource = 
            new StreamSource(new StringReader(oscript.toString ()));
        styleSource.setSystemId(styleBaseURI);

        try {
            transformer = tFactory.newTransformer(styleSource);
        } catch (Exception e) {
            e.printStackTrace (DebugLog.getDebugStream());
            throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                    "Exception from Xerces XSLT: " + e, e);
        }

	// get the src to work on: if a bean named "xslt:src" is registered
	// and its a Node, then use it as the source. If its not a Node, then
	// if its a URL parse it, if not treat it as a file and make a URL and
	// parse it and go. If no xslt:src is found, use an empty document
	// (stylesheet is treated as a literal result element stylesheet)
	Object srcObj = mgr.lookupBean ("xslt:src");
	Object xis = null;
	if (srcObj != null) {
            if (srcObj instanceof Node) {
		xis = new DOMSource((Node)srcObj);
            } else {
		try {
                    String mesg = "as anything";
                    if (srcObj instanceof Reader) {
			xis = new StreamSource ((Reader) srcObj);
			mesg = "as a Reader";
                    } else if (srcObj instanceof File) {
                        xis = new StreamSource ((File) srcObj);
                        mesg = "as a file";
                    } else {
                        String srcObjstr=srcObj.toString();
                        xis = new StreamSource (new StringReader(srcObjstr));
                        if (srcObj instanceof URL) {
                            mesg = "as a URL";
                        } else {
                            ((StreamSource) xis).setPublicId (srcObjstr);
                            mesg = "as an XML string";
                        }
                    }

                    if (xis == null) {
			throw new Exception ("Unable to get input from '" +
                                             srcObj + "' " + mesg);
                    }
		} catch (Exception e) {
                    throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                            "BSF:XSLTEngine: unable to get " +
                                            "input from '" + srcObj + "' as XML", e);
		}
            }
	} else {
            // create an empty document - real src must come into the 
            // stylesheet using "doc(...)" [see XSLT spec] or the stylesheet
            // must be of literal result element type
            xis = new StreamSource();
	}
	
	// set all declared beans as parameters.
	for (int i = 0; i < declaredBeans.size (); i++) {
            BSFDeclaredBean b = (BSFDeclaredBean) declaredBeans.elementAt (i);
            transformer.setParameter (b.name, new XObject (b.bean));
	}

	// declare a "bsf" parameter which is the BSF handle so that 
	// the script can do BSF stuff if it wants to
	transformer.setParameter ("bsf", 
                                  new XObject (new BSFFunctions (mgr, this)));

	// do it
	try {
            DOMResult result = new DOMResult();
            transformer.transform ((StreamSource) xis, result);
            return new XSLTResultNode (result.getNode());
	} catch (Exception e) {
            throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                    "exception while eval'ing XSLT script" + e, e);
	}
    }

    /**
     * Initialize the engine.
     */
    public void initialize (BSFManager mgr, String lang,
                            Vector declaredBeans) throws BSFException {
	super.initialize (mgr, lang, declaredBeans);

        tFactory = TransformerFactory.newInstance();
    }

    /**
     * Undeclare a bean by setting he parameter represeting it to null
     */
    public void undeclareBean (BSFDeclaredBean bean) throws BSFException {
        // Cannot clear only one parameter in Xalan 2, so we set it to null
        if ((transformer.getParameter (bean.name)) != null) {
            transformer.setParameter (bean.name, null);
        }
    }
}
