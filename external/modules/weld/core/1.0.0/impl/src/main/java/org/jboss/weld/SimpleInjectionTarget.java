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
package org.jboss.weld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.util.Beans;

/**
 * @author pmuir
 *
 */
public class SimpleInjectionTarget<T> implements InjectionTarget<T>
{
 
   private final BeanManagerImpl beanManager;
   private final WeldClass<T> type;
   private final ConstructorInjectionPoint<T> constructor;
   private final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
   private final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;
   private final WeldMethod<?, ?> postConstruct;
   private final WeldMethod<?, ?> preDestroy;
   private final Set<InjectionPoint> injectionPoints;
   private final Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
   private final Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   private final Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   private final Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

   public SimpleInjectionTarget(WeldClass<T> type, BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
      this.type = type;
      this.injectionPoints = new HashSet<InjectionPoint>();
      ConstructorInjectionPoint<T> constructor = null;
      try
      {
         constructor = Beans.getBeanConstructor(null, type);
         this.injectionPoints.addAll(Beans.getParameterInjectionPoints(null, constructor));
      }
      catch (Exception e)
      {
         // this means the bean of a type that cannot be produce()d, but that is non-fatal
         // unless someone calls produce()
      }
      this.constructor = constructor;
      this.injectableFields = Beans.getFieldInjectionPoints(null, type);
      this.injectionPoints.addAll(Beans.getFieldInjectionPoints(null, this.injectableFields));
      this.initializerMethods = Beans.getInitializerMethods(null, type);
      this.injectionPoints.addAll(Beans.getParameterInjectionPoints(null, initializerMethods));
      this.postConstruct = Beans.getPostConstruct(type);
      this.preDestroy = Beans.getPreDestroy(type);
      this.ejbInjectionPoints = Beans.getEjbInjectionPoints(null, type, beanManager);
      this.persistenceContextInjectionPoints = Beans.getPersistenceContextInjectionPoints(null, type, beanManager);
      this.persistenceUnitInjectionPoints = Beans.getPersistenceUnitInjectionPoints(null, type, beanManager);
      this.resourceInjectionPoints = Beans.getResourceInjectionPoints(null, type, beanManager);
      for (InjectionPoint ip : this.injectionPoints)
      {
         if (ip.getType().equals(InjectionPoint.class))
         {
            throw new DefinitionException("Cannot inject an InjectionPoint on a non-contextual type. Type: " + type + "; InjectionPoint: " + ip);
         }
      }
   }

   public T produce(CreationalContext<T> ctx)
   {
      if (constructor == null)
      {
         // this means we couldn't find a constructor on instantiation, which
         // means there isn't one that's spec-compliant
         // try again so the correct DefinitionException is thrown
         Beans.getBeanConstructor(null, type);
         // should not be reached
         throw new IllegalStateException(
               "We were not previously able to find the bean constructor, but now are?");
      }
      return constructor.newInstance(beanManager, ctx);
   }
   
   public void inject(final T instance, final CreationalContext<T> ctx)
   {
      new InjectionContextImpl<T>(beanManager, this, instance)
      {
         
         public void proceed()
         {
            Beans.injectEEFields(instance, beanManager, ejbInjectionPoints, persistenceContextInjectionPoints, persistenceUnitInjectionPoints, resourceInjectionPoints);
            Beans.injectFieldsAndInitializers(instance, ctx, beanManager, injectableFields, initializerMethods);
         }
         
      }.run();

   }

   public void postConstruct(T instance)
   {
      if (postConstruct == null)
         return;
      
      try
      {
         postConstruct.invoke(instance);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error invoking postConstruct() " + postConstruct, e);
      }
   }

   public void preDestroy(T instance)
   {
      if (preDestroy == null)
         return;
      
      try
      {
         preDestroy.invoke(instance);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error invoking preDestroy() " + preDestroy, e);
      }
   }

   public void dispose(T instance)
   {
      // No-op
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      return injectionPoints;
   }

}
