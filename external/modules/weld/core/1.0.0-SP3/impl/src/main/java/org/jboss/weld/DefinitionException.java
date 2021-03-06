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

import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import ch.qos.cal10n.IMessageConveyor;

/**
 * Thrown if the definition of a bean is incorrect
 * 
 * @author Pete Muir
 */
public class DefinitionException extends RuntimeException
{
   private static final long serialVersionUID = 8014646336322875707L;

   // Exception messages
   private static final IMessageConveyor messageConveyer  = loggerFactory().getMessageConveyor();

   public DefinitionException()
   {
      super();
   }

   public <E extends Enum<?>> DefinitionException(E key, Object... args)
   {
      super(messageConveyer.getMessage(key, args));
   }

   public <E extends Enum<?>> DefinitionException(E key, Throwable throwable, Object... args)
   {
      super(messageConveyer.getMessage(key, args), throwable);
   }

   public DefinitionException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public DefinitionException(String message)
   {
      super(message);
   }

   public DefinitionException(Throwable throwable)
   {
      super(throwable);
   }
   
}
