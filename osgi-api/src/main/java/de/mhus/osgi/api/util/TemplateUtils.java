/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.api.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Scanner;

import de.mhus.lib.core.parser.StringCompiler;
import de.mhus.lib.errors.MException;

public class TemplateUtils {

    /**
     * The method will close output and input stream.
     * @param outFile
     * @param templateIs
     * @param properties
     * @throws MException
     */
    public static void createFromTemplate(
            File outFile, InputStream templateIs, HashMap<String, Object> properties) throws MException {
        if (outFile.exists()) {
            throw new IllegalArgumentException(
                    "File "
                            + outFile.getPath()
                            + " already exists. Remove it if you wish to recreate it.");
        }
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(outFile));
            createFromTemplate(out, templateIs, properties, true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Can not create " + outFile, e);
        }
    }
    
    /**
     * 
     * @param out
     * @param templateIs
     * @param properties
     * @param close 
     * @throws MException
     */
    public static void createFromTemplate(
            PrintStream out, InputStream templateIs, HashMap<String, Object> properties, boolean close) throws MException {
        Scanner scanner = null;
        try {
            // read it line at a time so that we can use the platform line ending when we write it
            // out
            scanner = new Scanner(templateIs);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = filter(line, properties);
                out.println(line);
            }
            scanner.close();
        } finally {
            if (close) {
                safeClose(out);
                safeClose(templateIs);
            }
        }
    }

    private static String filter(String line, HashMap<String, Object> props) throws MException {
        if (!line.contains("$")) return line;
        return StringCompiler.compile(line).execute(props);
    }

    private static void safeClose(Closeable cl) {
        if (cl == null) {
            return;
        }
        try {
            cl.close();
        } catch (Throwable ignore) {
            // nothing to do
        }
    }
}
