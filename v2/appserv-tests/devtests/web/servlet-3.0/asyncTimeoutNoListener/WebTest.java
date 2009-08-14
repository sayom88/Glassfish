/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for the spec requirement that if an async timeout occurs, and
 * there are no AsyncListeners registered, the container MUST do an ERROR
 * dispatch to the original URI with a response code of 500.
 */
public class WebTest {

    private static final String TEST_NAME = "async-timeout-no-listener";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final int EXPECTED_RESPONSE_CODE = 200;
    private static final String EXPECTED_RESPONSE = "SUCCESS";
   
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for async timeout without " +
                            "any registered AsyncListener");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        InputStream is = null;
        BufferedReader input = null;

        try {
            URL url = new URL("http://" + host  + ":" + port +
                contextRoot + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20 * 1000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != EXPECTED_RESPONSE_CODE) {
                throw new Exception("Unexpected return code. Expected: " +
                    EXPECTED_RESPONSE_CODE + ", received: " + responseCode);
            }

            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String response = input.readLine();
            if (!EXPECTED_RESPONSE.equals(response)) {
                throw new Exception("Missing or wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + response);
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
