/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util.el;

import javax.el.ELContext;
import javax.el.ValueExpression;

/**
 * @author pmuir
 *
 */
public abstract class ForwardingValueExpression extends ValueExpression
{

   private static final long serialVersionUID = -2318681808639242038L;
   
   protected abstract ValueExpression delegate();

   @SuppressWarnings("unchecked")
   @Override
   public Class getExpectedType()
   {
      return delegate().getExpectedType();
   }

   @SuppressWarnings("unchecked")
   @Override
   public Class getType(ELContext context)
   {
      return delegate().getType(context);
   }

   @Override
   public Object getValue(ELContext context)
   {
      return delegate().getValue(context);
   }

   @Override
   public boolean isReadOnly(ELContext context)
   {
      return delegate().isReadOnly(context);
   }

   @Override
   public void setValue(ELContext context, Object value)
   {
      delegate().setValue(context, value);
   }

   @Override
   public String getExpressionString()
   {
      return delegate().getExpressionString();
   }

   @Override
   public boolean isLiteralText()
   {
      return delegate().isLiteralText();
   }   
   
   @Override
   public boolean equals(Object obj)
   {
      return this == obj || delegate().equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }

}
