package org.jboss.webbeans.test.unit.implementation.producer.method;

import javax.enterprise.inject.Named;
import javax.enterprise.inject.Produces;

public class NamedProducer
{
   @Named("itoen")
   @Produces
   public String[] createName()
   {
      return new String[] { "oh", "otya" };
   }

   @Named("iemon")
   @Produces
   public String[] createName2()
   {
      return new String[] { "fukujyuen", "iemon", "otya" };
   }
}
