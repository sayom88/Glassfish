package org.jboss.weld.tests.beanDeployment.session.multiple;

import javax.ejb.Remove;
import javax.ejb.Stateful;

@Stateful
@Synchronous
public class Tiger implements TigerLocal
{
   
   @Remove
   public void remove()
   {
      
   }

}
