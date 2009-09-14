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

package com.sun.enterprise.admin.cli;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.util.net.NetUtils;
import static com.sun.enterprise.admin.cli.CLIConstants.*;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.LocalDomainCommand;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.DASUtils;
import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;

import java.util.*;

/**
 * The start-domain command.
 *
 * @author bnevins
 * @author Bill Shannon
 */
@Service(name = "start-domain")
@Scoped(PerLookup.class)
public class StartDomainCommand extends LocalDomainCommand {

    private GFLauncherInfo info;
    private GFLauncher launcher;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(StartDomainCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, "debug", '\0', "BOOLEAN", false, "false");
        addOption(opts, "domaindir", '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        addOption(opts, "upgrade", '\0', "BOOLEAN", false, "false");
        addOption(opts, "verbose", 'v', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        processProgramOptions();
    }

    @Override
    protected int executeCommand() throws CommandException {
        String gfejar = System.getenv("GFE_JAR");

        if (gfejar != null && gfejar.length() > 0)
            return runCommandEmbedded();
        else
            return runCommandNotEmbedded();
    }

    private int runCommandNotEmbedded() throws CommandException {
        try {
            launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName(operands.get(0));
            }

            String parent = options.get("domaindir");
            if (parent != null)
                info.setDomainParentDir(parent);

            boolean verbose = getBooleanOption("verbose");
            boolean upgrade = getBooleanOption("upgrade");
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
            info.setUpgrade(upgrade);

            info.setRespawnInfo(programOpts.getClassName(),
                            programOpts.getClassPath(),
                            programOpts.getProgramArguments());

            launcher.setup();

            // only continue if all (normally 1) admin port(s) are free
            Set<Integer> adminPorts = info.getAdminPorts();
            for (Integer port : adminPorts) {
                if (!NetUtils.isPortFree(port)) {
                    String msg = strings.get("ServerRunning", port.toString());
                    logger.printWarning(msg);
                    return ERROR;
                }
            }

            // this can be slow, 500 msec,
            // with --passwordfile option it is ~~ 18 msec
            setMasterPassword(info);

            // launch returns very quickly if verbose is not set
            // if verbose is set then it returns after the domain dies
            launcher.launch();

            if (verbose || upgrade) { // we can potentially loop forever here...
                while (launcher.getExitValue() == RESTART_EXIT_VALUE) {
                    logger.printMessage(strings.get("restart"));

                    if (CLIConstants.debugMode)
                        System.setProperty(CLIConstants.WALL_CLOCK_START_PROP,
                                            "" + System.currentTimeMillis());

                    launcher.relaunch();
                }
                return launcher.getExitValue();
            } else {
                waitForDAS(info.getAdminPorts());
                report(info);
                return SUCCESS;
            }
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    private void setMasterPassword(GFLauncherInfo info)
                                throws CommandException {
        // Sets the password into the launcher info.
        // Yes, setting master password into a string is not right ...
        String mpn  = "AS_ADMIN_MASTERPASSWORD";
        String mpv  = passwords.get(mpn);
        if (mpv == null)
            mpv = checkMasterPasswordFile();
        if (mpv == null)
            mpv = "changeit"; //the default
        boolean ok = verifyMasterPassword(mpv);
        if (!ok) {
            mpv = retry(3);
        }
        info.addSecurityToken(mpn, mpv);
    }

    private String retry(int times) throws CommandException {
        String mpv;
        logger.printMessage("No valid master password found");
        // prompt times times
        for (int i = 0 ; i < times; i++) {
            // XXX - I18N
            String prompt =
                "Enter master password (" + (times-i) + " attempt(s) remain)> ";
            mpv = super.readPassword(prompt);
            if (mpv == null)
                throw new CommandException("No console, no prompting possible");
                // ignore retries :)
            if (verifyMasterPassword(mpv))
                return mpv;
            logger.printMessage("Sorry, incorrect master password, retry");
            // make them pay for typos?
            //Thread.currentThread().sleep((i+1)*10000);
        }
        throw new CommandException(
                    "Number of attempts (" + times + ") exhausted, giving up");
    }



    private int runCommandEmbedded() throws CommandException {
        try {
            // bnevins nov 23 2008
            // Embedded is a new type of server
            // For now -- we ONLY start embedded

            launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.embedded);

            info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName(operands.get(0));
            } else {
                info.setDomainName("domain1");
            }

            String parent = options.get("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            } else
                info.setDomainParentDir(
                            System.getenv("S1AS_HOME") + "/domains"); // TODO

            boolean verbose = getBooleanOption("verbose");
            info.setVerbose(verbose);
            info.setDebug(getBooleanOption("debug"));
            launcher.setup();

            // now admin ports are set.
            Set<Integer> ports = info.getAdminPorts();

            if (isServerAlive(ports)) {
                // todo add the port number to the message
                throw new CommandException("The Admin port is already taken: ");
            }

            launcher.launch();

            // if we are in verbose mode, we definitely do NOT want to wait for
            // DAS, since it already ran and is now dead!!
            //if(!verbose) {
                waitForDAS(ports);
                report(info);
            //}
            return SUCCESS;
        } catch (GFLauncherException gfle) {
            throw new CommandException(gfle.getMessage());
        } catch (MiniXmlParserException me) {
            throw new CommandException(me);
        }
    }

    private void waitForDAS(Set<Integer> ports) throws CommandException {
        if (ports == null || ports.size() <= 0) {
            String msg = strings.get("noPorts");
            throw new CommandException(
                    strings.get("CommandUnSuccessfulWithArg", name, msg));
        }
        long startWait = System.currentTimeMillis();
        logger.printMessage(strings.get("WaitDAS"));

        boolean alive = false;

        pinged:
        while (!timedOut(startWait)) {
            // first, see if the admin port is responding
            // if it is, the DAS is up
            for (int port : ports) {
                if (isServerAlive(port)) {
                    alive = true;
                    break pinged;
                }
            }

            // check to make sure the DAS process is still running
            // if it isn't, startup failed
            try {
                Process p = launcher.getProcess();
                int exitCode = p.exitValue();
                // uh oh, DAS died
                ProcessStreamDrainer psd = launcher.getProcessStreamDrainer();
                String output = psd.getOutErrString();
                if (ok(output))
                    throw new CommandException(strings.get("dasDiedOutput",
                                    info.getDomainName(), exitCode, output));
                else
                    throw new CommandException(strings.get("dasDied",
                                    info.getDomainName(), exitCode));
            } catch (GFLauncherException ex) {
                // should never happen
            } catch (IllegalThreadStateException ex) {
                // process is still alive
            }

            // wait before checking again
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // don't care
            }
        }

        if (!alive) {
            String msg = strings.get("dasNoStart", 
                info.getDomainName(), (WAIT_FOR_DAS_TIME_MS / 1000));
            throw new CommandException(msg);
        }
    }
 
    private boolean isServerAlive(int port) {
        logger.printDebugMessage("Check if server is alive on port " + port);
        programOpts.setPort(port);
        programOpts.setInteractive(false);      // don't prompt
        return DASUtils.pingDASQuietly(programOpts, env);
    }
 
    private boolean isServerAlive(Set<Integer> ports) {
        if (ports == null || ports.size() == 0)
            return false;
        return isServerAlive(ports.iterator().next());
    }

    private boolean timedOut(long startTime) {
        return (System.currentTimeMillis() - startTime) > WAIT_FOR_DAS_TIME_MS;
    }
 
    private void report(GFLauncherInfo info) {
        String msg = strings.get("DomainLocation", info.getDomainName(),
                            info.getDomainRootDir().getAbsolutePath());
        logger.printMessage(msg);
        Integer ap = -1;
        try {
            ap = info.getAdminPorts().iterator().next();
        } catch (Exception e) {
            //ignore
        }
        msg = strings.get("DomainAdminPort", ""+ap);
        logger.printMessage(msg);
    }
}
