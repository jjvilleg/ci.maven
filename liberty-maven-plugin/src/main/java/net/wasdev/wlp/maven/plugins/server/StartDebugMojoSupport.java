/**
 * (C) Copyright IBM Corporation 2014, 2017.
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
package net.wasdev.wlp.maven.plugins.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import net.wasdev.wlp.ant.ServerTask;
import net.wasdev.wlp.maven.plugins.BasicSupport;

import org.apache.tools.ant.taskdefs.Copy;

/**
 * Start/Debug server support.
 */
public class StartDebugMojoSupport extends BasicSupport {
    
    private static final String HEADER = "# Generated by liberty-maven-plugin";

    /**
     * Location of customized configuration file server.xml
     * 
     * @parameter expression="${configFile}"
     *            default-value="${basedir}/src/test/resources/server.xml"
     */
    protected File configFile;

    /**
     * 
     * Location of bootstrap.properties file.
     * 
     * @parameter expression="${bootstrapPropertiesFile}"
     *            default-value="${basedir}/src/test/resources/bootstrap.properties"
     */
    protected File bootstrapPropertiesFile;

    /**
     * @parameter
     */
    protected Map<String, String> bootstrapProperties;
    
    /**
     * 
     * Location of jvm.options file.
     * 
     * @parameter expression="${jvmOptionsFile}"
     *            default-value="${basedir}/src/test/resources/jvm.options"
     */
    protected File jvmOptionsFile;
    
    /**
     * @parameter
     */
    protected List<String> jvmOptions;

    /**
     * 
     * Location of customized server environment file server.env
     * 
     * @parameter expression="${serverEnv}"
     *            default-value="${basedir}/src/test/resources/server.env"
     */
    protected File serverEnv;

    protected ServerTask initializeJava() throws Exception {
        ServerTask serverTask = (ServerTask) ant.createTask("antlib:net/wasdev/wlp/ant:server");
        if (serverTask == null) {
            throw new NullPointerException("server task not found");
        }
        serverTask.setInstallDir(installDirectory);
        serverTask.setServerName(serverName);
        serverTask.setUserDir(userDirectory);
        serverTask.setOutputDir(outputDirectory);
        return serverTask;
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected void copyConfigFiles() throws IOException {
        // copy configuration file to server directory if end-user set it.
        if (configFile != null && configFile.exists()) {
            Copy copy = (Copy) ant.createTask("copy");
            copy.setFile(configFile);
            copy.setTofile(new File(serverDirectory, "server.xml"));
            copy.setOverwrite(true);
            copy.execute();

            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "server.xml", configFile.getCanonicalPath()));
        }
        
        // handle jvm.options
        File optionsFile = new File(serverDirectory, "jvm.options");
        if (jvmOptions != null) {
            writeJvmOptions(optionsFile, jvmOptions);
            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "jvm.options", "inlined configuration"));
        } else if (jvmOptionsFile != null && jvmOptionsFile.exists()) {
            Copy copy = (Copy) ant.createTask("copy");
            copy.setFile(jvmOptionsFile);
            copy.setTofile(optionsFile);
            copy.setOverwrite(true);
            copy.execute();

            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "jvm.options", jvmOptionsFile.getCanonicalPath()));
        }

        // handle bootstrap.properties
        File bootstrapFile = new File(serverDirectory, "bootstrap.properties");
        if (bootstrapProperties != null) {
            writeBootstrapProperties(bootstrapFile, bootstrapProperties);
            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "bootstrap.properties", "inlined configuration"));
        } else if (bootstrapPropertiesFile != null && bootstrapPropertiesFile.exists()) {
            Copy copy = (Copy) ant.createTask("copy");
            copy.setFile(bootstrapPropertiesFile);
            copy.setTofile(bootstrapFile);
            copy.setOverwrite(true);
            copy.execute();

            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "bootstrap.properties", bootstrapPropertiesFile.getCanonicalPath()));
        }
        
        // copy configuration file to server directory if end-user set it.
        if (serverEnv != null && serverEnv.exists()) {
            Copy copy = (Copy) ant.createTask("copy");
            copy.setFile(serverEnv);
            copy.setTofile(new File(serverDirectory, "server.env"));
            copy.setOverwrite(true);
            copy.execute();

            log.info(MessageFormat.format(messages.getString("info.server.start.update.config"), "server.env", serverEnv.getCanonicalPath()));
        }
    }
    
    private void writeBootstrapProperties(File file, Map<String, String> properties) throws IOException {
        makeParentDirectory(file);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8"); 
            writer.println(HEADER);
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.print(entry.getKey());
                writer.print("=");
                writer.println(entry.getValue().replace("\\", "/"));
            }
        } finally {
            if (writer != null) {
                writer.close(); 
            }
        }
    }
    
    private void writeJvmOptions(File file, List<String> options) throws IOException {
        makeParentDirectory(file);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8"); 
            writer.println(HEADER);
            for (String option : options) {
                writer.println(option);
            }
        } finally {
            if (writer != null) {
                writer.close(); 
            }
        }
    }
    
    private void makeParentDirectory(File file) {        
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
    }

}
