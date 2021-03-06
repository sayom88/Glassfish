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

package javax.enterprise.inject.spi;

/**
 * The metadata for an annotated parameter which can be parsed by the
 * {@link BeanManager}
 * 
 * @author Pete Muir
 * 
 * @param <X> the type of the parameter
 */
public interface AnnotatedParameter<X> extends Annotated
{

   /**
    * The position of the parameter in the callable's argument list
    * 
    * @return the position of the parameter
    */
   public int getPosition();

   /**
    * The declaring callable
    * 
    * @return the declaring callable
    */
   public AnnotatedCallable<X> getDeclaringCallable();

}
