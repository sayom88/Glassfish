/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.errorpage;

import java.util.HashSet;
import java.util.Set;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.testharness.impl.packaging.war.WarArtifactDescriptor;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * <p>This test was mostly developed to test the scenario related to WELD-29.  Essentially
 * a JSF action throws an exception, and the error page is then rendered during which
 * all relevant scopes for CDI are tested.</p>
 * 
 * @author David Allen
 *
 */
@Artifact(addCurrentPackage=false)
@Classes({Storm.class, Rain.class})
@IntegrationTest(runLocally=true)
@Resources({
  @Resource(destination=WarArtifactDescriptor.WEB_XML_DESTINATION, source="web.xml"),
  @Resource(destination="storm.jspx", source="storm.jsf"),
  @Resource(destination="error.jspx", source="error.jsf"),
  @Resource(destination="/WEB-INF/faces-config.xml", source="faces-config.xml")
})
public class ErrorPageTest extends AbstractWeldTest
{
   @Test(description = "WELD-29", groups = { "broken" })
   public void testActionMethodExceptionDoesNotDestroyContext() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);
      
      HtmlPage page = client.getPage(getPath("/storm.jsf"));
      HtmlSubmitInput disasterButton = getFirstMatchingElement(page, HtmlSubmitInput.class, "disasterButton");
      HtmlTextInput strength = getFirstMatchingElement(page, HtmlTextInput.class, "stormStrength");
      strength.setValueAttribute("10");
      page = disasterButton.click();
      assert "Application Error".equals(page.getTitleText());
      HtmlDivision conversationValue = getFirstMatchingElement(page, HtmlDivision.class, "conversation");
      assert conversationValue.asText().equals("10");
      HtmlDivision requestValue = getFirstMatchingElement(page, HtmlDivision.class, "request");
      assert requestValue.asText().equals("medium");
   }

   protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass)
   {
     Set<T> result = new HashSet<T>();
     
     for (HtmlElement element : rootElement.getAllHtmlChildElements())
     {
        result.addAll(getElements(element, elementClass));
     }
     
     if (elementClass.isInstance(rootElement))
     {
        result.add(elementClass.cast(rootElement));
     }
     return result;
     
   }
 
   protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id)
   {
     
     Set<T> inputs = getElements(page.getBody(), elementClass);
     for (T input : inputs)
     {
         if (input.getId().contains(id))
         {
            return input;
         }
     }
     return null;
   }
   
}
