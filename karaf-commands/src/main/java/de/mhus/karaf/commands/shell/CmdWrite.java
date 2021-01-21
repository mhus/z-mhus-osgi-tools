/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MThread;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "write", description = "Write to file")
@Service
public class CmdWrite extends AbstractCmd {

    @Argument(
            index = 0,
            name = "fileName",
            required = false,
            description = "FileName or * for std return",
            multiValued = false)
    String fileName;

    @Option(
            name = "-a",
            aliases = {"--append"},
            description = "Append to existing file",
            required = false,
            multiValued = false)
    boolean append;

    @Override
    public Object execute2() throws Exception {

        if (fileName.equals("*")) {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            copyFile(System.in, ba);
            return new String(ba.toByteArray());
        } else {
            File f = new File(fileName);
            FileOutputStream fos = new FileOutputStream(f, append);

            copyFile(System.in, fos);
            fos.close();

            return null;
        }
    }

    // copied from MFile
    public long copyFile(InputStream _is, OutputStream _os) {
        if (_is == null || _os == null) return -1;

        long size = 0;
        long free = Runtime.getRuntime().freeMemory();
        if (free < 1024) free = 1024;
        if (free > 32768) free = 32768;

        byte[] buffer = new byte[(int) free];
        int i = 0;

        try {
            while ((i = _is.read(buffer)) != -1) {
                if (i == 0) MThread.sleepInLoop(100);
                else {
                    _os.write(buffer, 0, i);
                    size += i;
                }
            }
        } catch (Exception e) {
            log().d(e);
        }
        return size;
    }
}
