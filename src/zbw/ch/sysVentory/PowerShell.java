/*
 * Copyright 2016 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zbw.ch.sysVentory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows to open a session into PowerShell console and launch different
 * commands.<br>
 * This class cannot be directly instantiated. Instead, use the method
 * PowerShell.openSession and call the commands using the returned instance.
 * <p>
 * Once the session is finished, call close() method in order to free the
 * resources.
 *
 * @author Javier Garcia Alonso
 */
public class PowerShell {

    //Process to store PowerShell session

    private Process p;
    //Writer to send commands
    private PrintWriter commandWriter;

    //Threaded session variables
    private boolean closed = false;
    private ExecutorService threadpool;
    private static final int MAX_THREADS = 3; //standard output + error output + session close thread
    static final int WAIT_PAUSE = 10;
    static final int MAX_WAIT = 10000000; //160 minuten

    //Private constructor.
    private PowerShell() {
    }

    //Initializes PowerShell console in which we will enter the commands
    private PowerShell initalize() throws PowerShellNotAvailableException {
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoExit", "-Command", "-");
        try {
            p = pb.start();
        } catch (IOException ex) {
            throw new PowerShellNotAvailableException(
                    "Cannot execute PowerShell.exe. Please make sure that it is installed in your system", ex);
        }

        commandWriter
                = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream())), true);

        //Init thread pool
        this.threadpool = Executors.newFixedThreadPool(MAX_THREADS);

        return this;
    }

    /**
     * Creates a session in PowerShell console an returns an instance which
     * allows to execute commands in PowerShell context
     *
     * @return an instance of the class
     * @throws PowerShellNotAvailableException if PowerShell is not installed in
     * the system
     */
    public static PowerShell openSession() throws PowerShellNotAvailableException {
        PowerShell powerShell = new PowerShell();

        return powerShell.initalize();
    }

    /**
     * Launch a PowerShell command.<p>
     * This method launch a thread which will be executed in the already created
     * PowerShell console context
     *
     * @param command the command to call. Ex: dir
     * @return PowerShellResponse the information returned by powerShell
     */
    public PowerShellResponse executeCommand(String command) {
        Callable commandProcessor = new PowerShellCommandProcessor("standard", p.getInputStream());
        Callable commandProcessorError = new PowerShellCommandProcessor("error", p.getErrorStream());

        String commandOutput = "";
        boolean isError = false;

        Future<String> result = threadpool.submit(commandProcessor);
        Future<String> resultError = threadpool.submit(commandProcessorError);

        //Launch command
        commandWriter.println(command);

        try {
            while (!result.isDone() && !resultError.isDone()) {
                Thread.sleep(WAIT_PAUSE);
            }
            if (result.isDone()) {
                commandOutput = result.get();
            } else {
                isError = true;
                commandOutput = resultError.get();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when processing PowerShell command", ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when processing PowerShell command", ex);
        } finally {
            //issue #2. Close and cancel processors/threads - Thanks to r4lly for helping me here
            ((PowerShellCommandProcessor) commandProcessor).close();
            ((PowerShellCommandProcessor) commandProcessorError).close();
        }

        return new PowerShellResponse(isError, commandOutput);
    }

    /**
     * Closes all the resources used to maintain the PowerShell context
     */
    public void close() {
        if (!this.closed) {
            try {
                Future<String> closeTask = threadpool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        commandWriter.println("exit");
                        p.waitFor();
                        return "OK";
                    }
                });
                waitUntilClose(closeTask);
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when when closing PowerShell", ex);
            } finally {
                try {
                    p.getInputStream().close();
                    p.getErrorStream().close();
                } catch (IOException ex) {
                    Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when when closing streams", ex);
                }                
                commandWriter.close();
                if (this.threadpool != null) {
                    try {
                        this.threadpool.shutdownNow();                        
                        this.threadpool.awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when when shutting thread pool", ex);
                    }
                    
                }
                this.closed = true;
            }
        }
    }

    private static void waitUntilClose(Future<String> task) throws InterruptedException {
        int closingTime = 0;
        while (!task.isDone()) {
            if (closingTime > MAX_WAIT) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when closing PowerShell: TIMEOUT!");
                break;
            }
            Thread.sleep(WAIT_PAUSE);
            closingTime += WAIT_PAUSE;
        }
    }

    /**
     * Execute a single command in PowerShell console and gets result
     * 
     * @param command the command to execute
     * @return response with the output of the command
     */
    public static PowerShellResponse executeSingleCommand(String command) {
        PowerShell session = null;
        PowerShellResponse response = null;
        try {
            session = PowerShell.openSession();

            response = session.executeCommand(command);
        } catch (PowerShellNotAvailableException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "PowerShell not available", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return response;
    }
}
