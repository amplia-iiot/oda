/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package es.amplia.oda.subsystem.sshserver.internal;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

class ShellCommand implements Command, Runnable, SessionAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellCommand.class);

    private final String command;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private final CommandProcessor processor;
    private Environment env;

    ShellCommand(CommandProcessor processor, String command) {
        this.processor = processor;
        this.command = command;
    }

    public void setInputStream(InputStream in) {
        this.in = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    public void setSession(ServerSession session) {
        // The session is created from the CommandProcessor

    }

    public void start(final Environment env) {
        this.env = env;
        new Thread(this).start();
    }

    public void run() {
        int exitStatus = 0;
        try (CommandSession session = processor.createSession(in, new PrintStream(out), new PrintStream(err))) {
            for (Map.Entry<String, String> e : env.getEnv().entrySet()) {
                session.put(e.getKey(), e.getValue());
            }
            session.execute(command);
        } catch (Exception e) {
            exitStatus = 1;
            LOGGER.error("Unable to start shell", e);
        } finally {
            ShellFactoryImpl.close(in, out, err);
            callback.onExit(exitStatus);
        }
    }

    public void destroy() {
        // Nothing to do
    }
}
