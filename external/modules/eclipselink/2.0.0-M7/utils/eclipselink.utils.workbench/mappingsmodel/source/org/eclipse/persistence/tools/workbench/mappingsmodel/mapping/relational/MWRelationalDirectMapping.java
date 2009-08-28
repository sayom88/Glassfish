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
package org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.relational;

import java.util.Collection;
import java.util.List;

import org.eclipse.persistence.tools.workbench.mappingsmodel.MWModel;
import org.eclipse.persistence.tools.workbench.mappingsmodel.MWQueryKey;
import org.eclipse.persistence.tools.workbench.mappingsmodel.ProblemConstants;
import org.eclipse.persistence.tools.workbench.mappingsmodel.db.MWColumn;
import org.eclipse.persistence.tools.workbench.mappingsmodel.descriptor.MWDescriptor;
import org.eclipse.persistence.tools.workbench.mappingsmodel.descriptor.MWMappingDescriptor;
import org.eclipse.persistence.tools.workbench.mappingsmodel.descriptor.relational.MWRelationalClassDescriptor;
import org.eclipse.persistence.tools.workbench.mappingsmodel.descriptor.relational.MWRelationalDescriptor;
import org.eclipse.persistence.tools.workbench.mappingsmodel.descriptor.relational.MWTableDescriptor;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWColumnHandle;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWHandle;
import org.eclipse.persistence.tools.workbench.mappingsmodel.handles.MWHandle.NodeReferenceScrubber;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWConverterMapping;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWDirectMapping;
import org.eclipse.persistence.tools.workbench.mappingsmodel.mapping.MWTypeConversionConverter;
import org.eclipse.persistence.tools.workbench.mappingsmodel.meta.MWClassAttribute;
import org.eclipse.persistence.tools.workbench.utility.CollectionTools;
import org.eclipse.persistence.tools.workbench.utility.node.Node;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;

public abstract class MWRelationalDirectMapping 
	extends MWDirectMapping 
	implements AggregateRuntimeFieldNameGenerator
{
	// **************** Variables *********************************************
	
	private MWColumnHandle columnHandle;
		public final static String COLUMN_PROPERTY = "column";
		
	/**
	 * The auto-generated query key for this mapping.  This should
	 * not be made persistent, as it is initialized by instances of 
	 * this class and basically just encapsulates some of the behavior	
	 * of itself.
	 */
	private AutoGeneratedQueryKey autoGeneratedQueryKey;
	
	
	// **************** Constructors ***************

	/** Default constructor - for TopLink use only */
	protected MWRelationalDirectMapping() {
		super();
	}
	
	protected MWRelationalDirectMapping(MWMappingDescriptor parent, MWClassAttribute attribute, String name) {
		super(parent, attribute, name);
	}
	
	
	// **************** Initialization ****************************************

	protected void initialize() {
		super.initialize();
		this.autoGeneratedQueryKey = new AutoGeneratedQueryKey(this);		
	}
	
	/** initialize persistent state */
	protected void initialize(Node parent) {
		super.initialize(parent);	
		this.columnHandle = new MWColumnHandle(this, this.buildColumnScrubber());
	}
	
	
	// **************** MWMapping override ****************
	
	public void setName(String name) {
		String oldName = this.getName();
		super.setName(name);
		String newName = this.getName();
		if (this.attributeValueHasChanged(oldName, newName)) {
			this.autoGeneratedQueryKey.nameChanged(oldName, newName);
		}
	}


	// **************** MWDirectMapping implementation ****************
	
	protected MWTypeConversionConverter buildTypeConversionConverter() {
		return new MWRelationalTypeConversionConverter(this);
	}


	// **************** MWRelationalMapping implementation ****************
	
	public boolean parentDescriptorIsAggregate() {
		return ((MWRelationalDescriptor) getParentDescriptor()).isAggregateDescriptor();
	}

	public MWRelationalDescriptor getParentRelationalDescriptor() {
		return (MWRelationalDescriptor) getParentDescriptor();
	}


	// **************** Column *************************************************
	
	public MWColumn getColumn() {
		 return this.columnHandle.getColumn();
	}
	
	public void setColumn(MWColumn column) {
		 Object old = this.columnHandle.getColumn();
		 this.columnHandle.setColumn(column);
		 this.firePropertyChanged(COLUMN_PROPERTY, old, column);
	}
	
	
	// **************** Auto generated query key ******************************
	
	/**
	 * Returns an auto-generated query key for this mapping.
	 * Because the behavior of the query key mimics some of that of
	 * this mapping, it is basically just a "window" into the
	 * direct mapping.
	 */
	public MWQueryKey getAutoGeneratedQueryKey() {
		return this.autoGeneratedQueryKey;
	}
	
	
	// **************** icon key **********************************************
	
	public abstract String iconKey();
	
	
	// **************** MWRelationalMapping implementation *****************
	
	public void addWrittenFieldsTo(Collection writtenFields) {
		if (isReadOnly()) {
			return;
		}
		if (getColumn() != null) {
			writtenFields.add(getColumn());
		}
	}
	
	
	// ************* aggregate support *************
	
	public String fieldNameForRuntime() {
		return "DIRECT";
	}
	
	public AggregateFieldDescription fullFieldDescription() {
		return new AggregateFieldDescription() {
			public String getMessageKey() {
				return "AGGREGATE_FIELD_DESCRIPTION_FOR_DIRECT_MAPPING";
			}
			
			public Object[] getMessageArguments() {
				return new Object[] {};
			}
		};
	}

	protected Collection buildAggregateFieldNameGenerators() {
		Collection aggregateFieldNameGenerators = super.buildAggregateFieldNameGenerators();
		aggregateFieldNameGenerators.add(this);
		return aggregateFieldNameGenerators;
	}

	public void parentDescriptorMorphedToAggregate() {
		super.parentDescriptorMorphedToAggregate();
		setColumn(null);
	}

	public boolean fieldIsWritten() {
		return true;
	}
	
	public MWDescriptor owningDescriptor() {
		return (MWDescriptor) this.getParent();
	}
	
	
	// **************** Containment hierarchy ******************************
	
	protected void addChildrenTo(List children) {
		super.addChildrenTo(children);
		children.add(this.autoGeneratedQueryKey);	// ???  ~bjv
		children.add(this.columnHandle);
	}
	
	private NodeReferenceScrubber buildColumnScrubber() {
		return new NodeReferenceScrubber() {
			public void nodeReferenceRemoved(Node node, MWHandle handle) {
				MWRelationalDirectMapping.this.setColumn(null);
			}
			public String toString() {
				return "MWRelationalDirectMapping.buildColumnScrubber()";
			}
		};
	}

	
	// **************** Mapping Morphing support ******************************

	public void initializeFromMWRelationalDirectMapping(MWRelationalDirectMapping mapping) {
		super.initializeFromMWRelationalDirectMapping(mapping);
		this.setColumn(mapping.getColumn());
	}
	
	protected void initializeFromMWConverterMapping(MWConverterMapping converterMapping) {
		// do nothing - this mapping doesn't like to change converters
	}
	
	
	// **************** Problem Handling **************************************
	
	protected void addProblemsTo(List newProblems) {
		super.addProblemsTo(newProblems);
		this.checkColumn(newProblems);
	}
	
	
	private void checkColumn(List newProblems) {
		if (this.parentDescriptorIsAggregate()) {
			return;
		}
		if (this.getColumn() == null) {
			newProblems.add(this.buildProblem(ProblemConstants.MAPPING_FIELD_NOT_SPECIFIED));
		} else {
			if ( ! CollectionTools.contains(((MWTableDescriptor) this.getParentDescriptor()).allAssociatedColumns(), this.getColumn())) {
				newProblems.add(this.buildProblem(ProblemConstants.MAPPING_FIELD_NOT_VALID));
			}
		}
	}
	
	
	// **************** Automap Support ***************************************

	public void addColumnlessDirectMappingTo(Collection columnlessDirectMappings) {
		if (this.getColumn() == null) {
			columnlessDirectMappings.add(this);
		}
	}


	// **************** Runtime Conversion ****************
	
	public DatabaseMapping runtimeMapping() {
		DirectToFieldMapping mapping = (DirectToFieldMapping) super.runtimeMapping();
		
		if (parentDescriptorIsAggregate()) {
			mapping.setFieldName(getName() + "->" + fieldNameForRuntime());
		}
		else if (getColumn() != null) {
			mapping.setFieldName(getColumn().qualifiedName());
		}
		else {
			mapping.setFieldName("");
		}
				
		return mapping;
	}
	
	
	// **************** TopLink methods ***************************************
	
	public static XMLDescriptor buildDescriptor() {	
		XMLDescriptor descriptor = new XMLDescriptor();
		
		descriptor.setJavaClass(MWRelationalDirectMapping.class);
		descriptor.getInheritancePolicy().setParentClass(MWDirectMapping.class);
		
		XMLCompositeObjectMapping columnHandleMapping = new XMLCompositeObjectMapping();
		columnHandleMapping.setAttributeName("columnHandle");
		columnHandleMapping.setGetMethodName("getColumnHandleForTopLink");
		columnHandleMapping.setSetMethodName("setColumnHandleForTopLink");
		columnHandleMapping.setReferenceClass(MWColumnHandle.class);
		columnHandleMapping.setXPath("column-handle");
		descriptor.addMapping(columnHandleMapping);
		
		return descriptor;	
	}
	
	/** check for null */
	private MWColumnHandle getColumnHandleForTopLink() {
		return (this.columnHandle.getColumn() == null) ? null : this.columnHandle;
	}
	private void setColumnHandleForTopLink(MWColumnHandle handle) {
		NodeReferenceScrubber scrubber = this.buildColumnScrubber();
		this.columnHandle = ((handle == null) ? new MWColumnHandle(this, scrubber) : handle.setScrubber(scrubber));
	}
	
	
	// **************** Member classes ****************************************
	
	private static final class AutoGeneratedQueryKey
		extends MWModel 
		implements MWQueryKey
	{
		AutoGeneratedQueryKey(MWRelationalDirectMapping parent) {
			super(parent);
		}
		
		private MWRelationalDirectMapping getMapping() {
			return (MWRelationalDirectMapping) this.getParent();
		}
		
		public MWColumn getColumn() {
			return this.getMapping().getColumn();
		}
		 
		public boolean isAutoGenerated() {
			return true;
		}
		 
		public MWRelationalClassDescriptor getDescriptor() {
			return (MWRelationalClassDescriptor) this.getMapping().getParentDescriptor();
		}
	    
		public String getName() {
			return this.getMapping().getName();
		}

		void nameChanged(String oldName, String newName) {
	        this.firePropertyChanged(NAME_PROPERTY, oldName, newName);
		}
		
        public String displayString() {
            return this.getName();
        }

        public void toString(StringBuffer sb) {
            sb.append("field=");
            if (getColumn() == null) {
                sb.append("null");
            }
            else {
                getColumn().toString(sb);
            }
        }
	 }	
}
