/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
 
/**
 *	This generated bean class EjbModule matches the DTD element ejb-module
 *
 */

package com.sun.enterprise.config.serverbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.Serializable;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.util.i18n.StringManager;

// BEGIN_NOI18N

public class EjbModule extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String DESCRIPTION = "Description";
	static public final String WEB_SERVICE_ENDPOINT = "WebServiceEndpoint";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public EjbModule() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EjbModule(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("description", DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("web-service-endpoint", WEB_SERVICE_ENDPOINT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebServiceEndpoint.class);
		this.createAttribute(WEB_SERVICE_ENDPOINT, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(WEB_SERVICE_ENDPOINT, "monitoring", "Monitoring", 
						AttrProp.CDATA,
						null, "OFF");
		this.createAttribute(WEB_SERVICE_ENDPOINT, "max-history-size", "MaxHistorySize", 
						AttrProp.CDATA,
						null, "25");
		this.createAttribute(WEB_SERVICE_ENDPOINT, "jbi-enabled", "JbiEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("property", ELEMENT_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	/**
	* Return  the Description of the Element ejb-module
	*/
	public String getDescription() {
		return (String) getValue(ServerTags.DESCRIPTION);
	}
	/**
	* Modify  the Description of the Element ejb-module
	* @param v the new value
	*/
	public void setDescription(String v){
		setValue(ServerTags.DESCRIPTION, (null != v ? v.trim() : null));
		}
	// Get Method
	public WebServiceEndpoint getWebServiceEndpoint(int index) {
		return (WebServiceEndpoint)this.getValue(WEB_SERVICE_ENDPOINT, index);
	}

	// This attribute is an array, possibly empty
	public void setWebServiceEndpoint(WebServiceEndpoint[] value) {
		this.setValue(WEB_SERVICE_ENDPOINT, value);
	}

	// Getter Method
	public WebServiceEndpoint[] getWebServiceEndpoint() {
		return (WebServiceEndpoint[])this.getValues(WEB_SERVICE_ENDPOINT);
	}

	// Return the number of properties
	public int sizeWebServiceEndpoint() {
		return this.size(WEB_SERVICE_ENDPOINT);
	}

	// Add a new element returning its index in the list
	public int addWebServiceEndpoint(WebServiceEndpoint value)
			throws ConfigException{
		return addWebServiceEndpoint(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addWebServiceEndpoint(WebServiceEndpoint value, boolean overwrite)
			throws ConfigException{
		WebServiceEndpoint old = getWebServiceEndpointByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(EjbModule.class).getString("cannotAddDuplicate",  "WebServiceEndpoint"));
		}
		return this.addValue(WEB_SERVICE_ENDPOINT, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWebServiceEndpoint(WebServiceEndpoint value){
		return this.removeValue(WEB_SERVICE_ENDPOINT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeWebServiceEndpoint(WebServiceEndpoint value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(WEB_SERVICE_ENDPOINT, value, overwrite);
	}

	public WebServiceEndpoint getWebServiceEndpointByName(String id) {
	 if (null != id) { id = id.trim(); }
	WebServiceEndpoint[] o = getWebServiceEndpoint();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ElementProperty getElementProperty(int index) {
		return (ElementProperty)this.getValue(ELEMENT_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setElementProperty(ElementProperty[] value) {
		this.setValue(ELEMENT_PROPERTY, value);
	}

	// Getter Method
	public ElementProperty[] getElementProperty() {
		return (ElementProperty[])this.getValues(ELEMENT_PROPERTY);
	}

	// Return the number of properties
	public int sizeElementProperty() {
		return this.size(ELEMENT_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addElementProperty(ElementProperty value)
			throws ConfigException{
		return addElementProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addElementProperty(ElementProperty value, boolean overwrite)
			throws ConfigException{
		ElementProperty old = getElementPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(EjbModule.class).getString("cannotAddDuplicate",  "ElementProperty"));
		}
		return this.addValue(ELEMENT_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeElementProperty(ElementProperty value){
		return this.removeValue(ELEMENT_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeElementProperty(ElementProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ELEMENT_PROPERTY, value, overwrite);
	}

	public ElementProperty getElementPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	ElementProperty[] o = getElementProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for Name of the Element ejb-module
	* @return  the Name of the Element ejb-module
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element ejb-module
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for Location of the Element ejb-module
	* @return  the Location of the Element ejb-module
	*/
	public String getLocation() {
		return getAttributeValue(ServerTags.LOCATION);
	}
	/**
	* Modify  the Location of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLocation(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOCATION, v, overwrite);
	}
	/**
	* Modify  the Location of the Element ejb-module
	* @param v the new value
	*/
	public void setLocation(String v) {
		setAttributeValue(ServerTags.LOCATION, v);
	}
	/**
	* Getter for ObjectType of the Element ejb-module
	* @return  the ObjectType of the Element ejb-module
	*/
	public String getObjectType() {
		return getAttributeValue(ServerTags.OBJECT_TYPE);
	}
	/**
	* Modify  the ObjectType of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setObjectType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.OBJECT_TYPE, v, overwrite);
	}
	/**
	* Modify  the ObjectType of the Element ejb-module
	* @param v the new value
	*/
	public void setObjectType(String v) {
		setAttributeValue(ServerTags.OBJECT_TYPE, v);
	}
	/**
	* Get the default value of ObjectType from dtd
	*/
	public static String getDefaultObjectType() {
		return "user".trim();
	}
	/**
	* Getter for Enabled of the Element ejb-module
	* @return  the Enabled of the Element ejb-module
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element ejb-module
	* @param v the new value
	*/
	public void setEnabled(boolean v) {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Enabled from dtd
	*/
	public static String getDefaultEnabled() {
		return "true".trim();
	}
	/**
	* Getter for Libraries of the Element ejb-module
	* @return  the Libraries of the Element ejb-module
	*/
	public String getLibraries() {
			return getAttributeValue(ServerTags.LIBRARIES);
	}
	/**
	* Modify  the Libraries of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLibraries(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LIBRARIES, v, overwrite);
	}
	/**
	* Modify  the Libraries of the Element ejb-module
	* @param v the new value
	*/
	public void setLibraries(String v) {
		setAttributeValue(ServerTags.LIBRARIES, v);
	}
	/**
	* Getter for AvailabilityEnabled of the Element ejb-module
	* @return  the AvailabilityEnabled of the Element ejb-module
	*/
	public boolean isAvailabilityEnabled() {
		return toBoolean(getAttributeValue(ServerTags.AVAILABILITY_ENABLED));
	}
	/**
	* Modify  the AvailabilityEnabled of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAvailabilityEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AVAILABILITY_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AvailabilityEnabled of the Element ejb-module
	* @param v the new value
	*/
	public void setAvailabilityEnabled(boolean v) {
		setAttributeValue(ServerTags.AVAILABILITY_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of AvailabilityEnabled from dtd
	*/
	public static String getDefaultAvailabilityEnabled() {
		return "false".trim();
	}
	/**
	* Getter for DirectoryDeployed of the Element ejb-module
	* @return  the DirectoryDeployed of the Element ejb-module
	*/
	public boolean isDirectoryDeployed() {
		return toBoolean(getAttributeValue(ServerTags.DIRECTORY_DEPLOYED));
	}
	/**
	* Modify  the DirectoryDeployed of the Element ejb-module
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDirectoryDeployed(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DIRECTORY_DEPLOYED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the DirectoryDeployed of the Element ejb-module
	* @param v the new value
	*/
	public void setDirectoryDeployed(boolean v) {
		setAttributeValue(ServerTags.DIRECTORY_DEPLOYED, ""+(v==true));
	}
	/**
	* Get the default value of DirectoryDeployed from dtd
	*/
	public static String getDefaultDirectoryDeployed() {
		return "false".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WebServiceEndpoint newWebServiceEndpoint() {
		return new WebServiceEndpoint();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ElementProperty newElementProperty() {
		return new ElementProperty();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "ejb-module" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.OBJECT_TYPE)) return "user".trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.AVAILABILITY_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.DIRECTORY_DEPLOYED)) return "false".trim();
	return null;
	}
	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("WebServiceEndpoint["+this.sizeWebServiceEndpoint()+"]");	// NOI18N
		for(int i=0; i<this.sizeWebServiceEndpoint(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebServiceEndpoint(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEB_SERVICE_ENDPOINT, i, str, indent);
		}

		str.append(indent);
		str.append("ElementProperty["+this.sizeElementProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeElementProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getElementProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ELEMENT_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EjbModule\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

