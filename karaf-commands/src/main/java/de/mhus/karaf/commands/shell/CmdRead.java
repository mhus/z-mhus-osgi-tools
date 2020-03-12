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
package de.mhus.karaf.commands.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MFile;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "read", description = "Read a line from stdin or a file")
@Service
public class CmdRead extends AbstractCmd {

    @Argument(
            index = 0,
            name = "fileName",
            required = false,
            description = "FileName to read from",
            multiValued = false)
    String fileName;

    @Option(
            name = "-o",
            aliases = {"--out"},
            description = "Store content into variable name",
            required = false,
            multiValued = false)
    String out;

    @Option(
            name = "-p",
            aliases = {"--prompt"},
            description = "Prompt",
            required = false,
            multiValued = false)
    String prompt;

    @Option(
            name = "-s",
            aliases = {"--secure"},
            description = "Echo stars instead of the characters",
            required = false,
            multiValued = false)
    boolean secure;

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        String content = null;

        if (fileName == null) {
            try {

                if (prompt != null) {
                    System.out.print(prompt);
                    System.out.flush();
                }
                Reader isr = new InputStreamReader(System.in);
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int c = isr.read();
                    if (c < 0 || c == '\n' || c == '\r') {
                        break;
                    } else if (c == '\u007F') {
                        if (sb.length() > 0) {
                            sb.setLength(sb.length() - 1);
                            System.out.print('\b');
                            System.out.print(' ');
                            System.out.print('\b');
                            System.out.flush();
                        } else {
                            System.out.print('\7');
                            System.out.flush();
                        }
                    } else {
                        sb.append((char) c);
                        if (secure) System.out.print('*');
                        else System.out.print((char) c);
                        System.out.flush();
                    }
                }
                System.out.println();
                content = sb.toString();
            } catch (IOException t) {

            }
        } else {
            InputStream is = null;
            if (fileName.equals("*")) {
                is = System.in;
            } else {
                File f = new File(fileName);
                is = new FileInputStream(f);
            }
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            MFile.copyFile(is, ba);
            is.close();

            content = new String(ba.toByteArray());
        }

        if (out != null) session.put(out, content);
        else return content;

        return null;
    }
}
