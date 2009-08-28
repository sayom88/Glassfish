/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Hashtable;


// String categoryName
// String ndc
// boolan ndcLookupRequired
// String renderedMessage
// String threadName
// long timeStamp

// LocationInfo
// ThrowableInformation ti.
public class T113 {

  public
  byte[] serialize(Hashtable ht) {
    try {
      Category category = Category.getInstance((String) ht.get("categoryName"));
      

      LoggingEvent event = new LoggingEvent("org.apache.log4j.Category", 
					    category, 
					    Priority.toPriority((String)ht.get("priorityStr")),
					    ht.get("message"), 
					    (Throwable) ht.get("throwable"));
      event.getThreadName();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(event);
      oos.flush();
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public
  Hashtable deserialize(byte[] buf) {
    try {
      System.out.println("deserialize called.");
      ByteArrayInputStream bais = new ByteArrayInputStream(buf);
      ObjectInputStream si = new ObjectInputStream(bais);  
      LoggingEvent event = (LoggingEvent)  si.readObject();	    
      System.out.println("Desrialization looks successful.");

      return eventToHashtable(event);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  Hashtable eventToHashtable(LoggingEvent event) {
    Hashtable ht = new Hashtable();
    ht.put("categoryName", event.categoryName);
    ht.put("renderedMessage", event.getRenderedMessage());
    ht.put("priorityStr", event.priority.toString());
    ht.put("throwableInfo", event.getThrowableInformation());
    return ht;
  }
   
}

