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
package de.mhus.osgi.api.karaf;

import java.io.IOException;

import org.apache.karaf.shell.api.console.Session;
import org.jline.reader.impl.LineReaderImpl;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.console.ANSIConsole;

public class KarafConsole extends ANSIConsole {

    private Session session;

    public KarafConsole(Session session) throws IOException {
        super((LineReaderImpl) session.get(".reader"));
        this.session = session;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adaptTo(Class<? extends T> clazz) {
        if (clazz == Session.class) return (T) session;
        return super.adaptTo(clazz);
    }

    @Override
    protected void loadSettings() {
        //		super.loadSettings();
        supportSize = true;
    }

    @Override
    public int getWidth() {
        if (width > 0) return width;
        return MCast.toint(session.get("COLUMNS"), DEFAULT_WIDTH);
    }

    @Override
    public int getHeight() {
        if (height > 0) return height;
        return MCast.toint(session.get("LINES"), DEFAULT_HEIGHT);
    }
}
