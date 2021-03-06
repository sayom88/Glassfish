<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
-->
<!-- resourceNode/advancePool.inc -->

<sun:propertySheetSection id="connectionPropertySheet" label="$resource{i18n.jdbcPool.connectionSettings}">

    <sun:property id="p1"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.validateAtmostOnceInSeconds}" helpText="$resource{i18n.jdbcPool.validateAtmostOnceInSecondsHelp}">
        <sun:textField id="va" styleClass="integer" columns="$int{30}" text="#{advance.validateAtMostOncePeriodInSeconds}" /> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="p2"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.connectionLeakTimeoutInSeconds}" helpText="$resource{i18n.jdbcPool.connectionLeakTimeoutInSecondsHelp}">
        <sun:textField id="p2" styleClass="integer" columns="$int{30}" text="#{advance.connectionLeakTimeoutInSeconds}"/> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
   </sun:property>   

    <sun:property id="p3"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.connectionLeakReclaim}" helpText="$resource{i18n.jdbcPool.connectionLeakReclaimHelp}">
        <sun:checkbox  selected="#{advance.connectionLeakReclaim}" label=" "/>
    </sun:property> 

    <sun:property id="p4"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.connectionCreationRetryAttempts}" helpText="$resource{i18n.jdbcPool.connectionCreationRetryAttemptsHelp}">
        <sun:textField id="p4" styleClass="integer" columns="$int{30}" text="#{advance.connectionCreationRetryAttempts}"/> 
   </sun:property>   

    <sun:property id="p5"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.connectionCreationRetryIntervalInSeconds}" helpText="$resource{i18n.jdbcPool.connectionCreationRetryIntervalInSecondsHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" text="#{advance.connectionCreationRetryIntervalInSeconds}"/> 
        <sun:staticText id="p51" text="$resource{i18n.common.Seconds}"/>
   </sun:property>

    <sun:property id="p7"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.lazyConnectionAssociation}" helpText="$resource{i18n.jdbcPool.lazyConnectionAssociationHelp}">
        <sun:checkbox id="associate"  selected="#{advance.lazyConnectionAssociation}" label="$resource{i18n.common.Enabled}" onClick="enableDisableLazyConnection('#{associateId}');" >  
           <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{associateId});
            />
        </sun:checkbox>
   </sun:property>

    <sun:property id="p6"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.lazyConnectionEnlistment}" helpText="$resource{i18n.jdbcPool.lazyConnectionEnlistmentHelp}">
        <sun:checkbox id="enlist"  selected="#{advance.lazyConnectionEnlistment}" label="$resource{i18n.common.Enabled}">  
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{enlistId});
            />
        </sun:checkbox>
    </sun:property>

    <sun:property id="p8"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.associationWithThread}" helpText="$resource{i18n.jdbcPool.associationWithThreadHelp}">
        <sun:checkbox  selected="#{advance.associateWithThread}" label="$resource{i18n.common.Enabled}"/>  
   </sun:property>

    <sun:property id="p9"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.matchConnections}" helpText="$resource{i18n.jdbcPool.matchConnectionsHelp}">
        <sun:checkbox  selected="#{advance.matchConnections}" label="$resource{i18n.common.Enabled}"/>  
   </sun:property>

    <sun:property id="p10"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.maxConnectionUsageCount}" helpText="$resource{i18n.jdbcPool.maxConnectionUsageCountHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" text="#{advance.maxConnectionUsageCount}"/> 
   </sun:property>

</sun:propertySheetSection>

