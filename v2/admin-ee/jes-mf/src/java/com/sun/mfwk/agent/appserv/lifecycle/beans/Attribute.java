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

/**
 *	This generated bean class Attribute
 *	matches the schema element 'attribute'.
 *  The root bean class is Notifications
 *
 *	Generated on Sun Aug 28 23:45:36 PDT 2005
 * @Generated
 */

package com.sun.mfwk.agent.appserv.lifecycle.beans;

public class Attribute {
	public static final String NAME = "Name";	// NOI18N
	public static final String VALUE = "Value";	// NOI18N

	private String _Name;
	private String _Value;

	/**
	 * Normal starting point constructor.
	 */
	public Attribute() {
		_Name = "";
		_Value = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Attribute(String name, String value) {
		_Name = name;
		_Value = value;
	}

	/**
	 * Deep copy
	 */
	public Attribute(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Attribute(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute source, boolean justData) {
		_Name = source._Name;
		_Value = source._Value;
	}

	// This attribute is mandatory
	public void setName(String value) {
		_Name = value;
	}

	public String getName() {
		return _Name;
	}

	// This attribute is mandatory
	public void setValue(String value) {
		_Value = value;
	}

	public String getValue() {
		return _Value;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "attribute";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_Name != null) {
			out.write(nextIndent);
			out.write("<name");	// NOI18N
			out.write(">");	// NOI18N
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _Name, false);
			out.write("</name>\n");	// NOI18N
		}
		if (_Value != null) {
			out.write(nextIndent);
			out.write("<value");	// NOI18N
			out.write(">");	// NOI18N
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _Value, false);
			out.write("</value>\n");	// NOI18N
		}
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6, attrName.length());
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "name") {
				_Name = childNodeValue;
			}
			else if (childNodeName == "value") {
				_Value = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException {
		boolean restrictionFailure = false;
		// Validating property name
		if (getName() == null) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("getName() == null", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property value
		if (getValue() == null) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("getValue() == null", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "value", this);	// NOI18N
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "name")
			setName((String)value);
		else if (name == "value")
			setValue((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Attribute");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "name")
			return getName();
		if (name == "value")
			return getValue();
		throw new IllegalArgumentException(name+" is not a valid property name for Attribute");
	}

	public String nameSelf() {
		return "Attribute";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Name) {
				if (returnConstName) {
					return NAME;
				} else if (returnSchemaName) {
					return "name";
				} else if (returnXPathName) {
					return "name";
				} else {
					return "Name";
				}
			}
			if (child == _Value) {
				if (returnConstName) {
					return VALUE;
				} else if (returnSchemaName) {
					return "value";
				} else if (returnXPathName) {
					return "value";
				} else {
					return "Value";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public java.lang.Object[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		java.lang.Object[] result = new java.lang.Object[children.size()];
		return (java.lang.Object[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute && equals((com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute) o);
	}

	public boolean equals(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Name == null ? inst._Name == null : _Name.equals(inst._Name))) {
			return false;
		}
		if (!(_Value == null ? inst._Value == null : _Value.equals(inst._Value))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Name == null ? 0 : _Name.hashCode());
		result = 37*result + (_Value == null ? 0 : _Value.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : listener.dtd
    Created on : Aug 20, 2005, 4:52PM
    Description: DTD to defines the notifications to listen for.
-->

<!--
-->
<!ELEMENT notifications (object-name*)>
<!ATTLIST notifications
     domain CDATA  #REQUIRED>


<!--
attribute subelement provides notification filtering information.
Notifcatios from runtime mbeans are filtered by maching the name/vaue
pairsspecified by attribute subelement

mapping sub-element provide information to convert runtime mbean objectname to
corresponding monitoring mbean objectname using monitoring-mbean-name-template attribute
-->
<!ELEMENT object-name (attribute+, mapping*)>
<!ATTLIST object-name
     monitoring-mbean-name-template CDATA  #IMPLIED>


<!--
Provides attribute name and value of runtime mbean used to filter the notifications
-->
<!ELEMENT attribute (name, value)>


<!--
Provides mapping from runtime mbean attribute name to monitoring mbean attribute name
i.e value of runtime mbean attribute needs to be substituted for the vaule of monitoring
mbean attribute in monitoring-mbean-name-template to form the monitoring mbean object name.
-->
<!ELEMENT mapping (runtime-mbean-attribute-name, monitoring-mbean-attribute-value)>


<!--
-->
<!ELEMENT name (#PCDATA)>


<!--
-->
<!ELEMENT value (#PCDATA)>


<!--
-->
<!ELEMENT runtime-mbean-attribute-name (#PCDATA)>


<!--
-->
<!ELEMENT monitoring-mbean-attribute-value (#PCDATA)>

*/
