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
package de.mhus.karaf.commands.mhus;

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Attributes.ControlFlag;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Attributes.OutputFlag;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.console.ANSIConsole;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.XTermConsole;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;
import de.mhus.osgi.api.karaf.ConsoleInterceptor;
import de.mhus.osgi.api.karaf.KarafConsole;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

@Command(scope = "mhus", name = "console", description = "Manipulate and control the console")
@Service
public class CmdConsole extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = false,
            description =
                    "Command: \n"
                            + " info            - Print current console instance information\n"
                            + " create [<type>] - create new console of type: SIMPLE,ANSI,ANSI_COLOR,XTERM,XTERM_COLOR,CMD\n"
                            + " size <cols/width> <rows/height> - set static cols and rows\n"
                            + " reset           - recreate console instance\n"
                            + " cleanup         - cleanup console\n"
                            + " color <fg> <bg> - set colors\n"
                            + " debug           - print console debug information\n"
                            + " keys            - debug keyboard\n"
                            + " ask <question> <answers>"
                            + "",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "arguments",
            required = false,
            description = "arguments",
            multiValued = true)
    String arguments[];

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        switch (cmd) {
            case "reader.status":
                {
                    LineReader reader = (LineReader) session.get(".jline.reader");
                    for (Option option : Option.values())
                        System.out.println(option + ": " + reader.isSet(option));
                }
                break;
            case "reader.set":
                {
                    LineReader reader = (LineReader) session.get(".jline.reader");
                    reader.option(
                            Option.valueOf(arguments[0].toUpperCase()), M.to(arguments[1], false));
                }
                break;
            case "terminal.status":
                {
                    Attributes attr =
                            ((org.jline.terminal.Terminal) session.getTerminal()).getAttributes();
                    System.out.println("ControlChars: " + attr.getControlChars());
                    System.out.println("ControlFlags: " + attr.getControlFlags());
                    System.out.println("InputFlags  : " + attr.getInputFlags());
                    System.out.println("OutputFlags : " + attr.getOutputFlags());
                    System.out.println("LocalFlags  : " + attr.getLocalFlags());
                }
                break;
            case "terminal.set":
                {
                    Attributes attr =
                            ((org.jline.terminal.Terminal) session.getTerminal()).getAttributes();
                    if (arguments[0].equals("ControlChar"))
                        attr.setControlChar(
                                ControlChar.valueOf(arguments[1].toUpperCase()),
                                M.to(arguments[2], 0));
                    else if (arguments[0].equals("ControlFlag"))
                        attr.setControlFlag(
                                ControlFlag.valueOf(arguments[1].toUpperCase()),
                                M.to(arguments[2], false));
                    else if (arguments[0].equals("InputFlag"))
                        attr.setInputFlag(
                                InputFlag.valueOf(arguments[1].toUpperCase()),
                                M.to(arguments[2], false));
                    else if (arguments[0].equals("OutputFlag"))
                        attr.setOutputFlag(
                                OutputFlag.valueOf(arguments[1].toUpperCase()),
                                M.to(arguments[2], false));
                    else if (arguments[0].equals("LocalFlag"))
                        attr.setLocalFlag(
                                LocalFlag.valueOf(arguments[1].toUpperCase()),
                                M.to(arguments[2], false));
                    else System.out.println("Flags unknown: " + arguments[0]);
                }
                break;
            case "info":
                {
                    System.out.println("Default Width :" + Console.DEFAULT_WIDTH);
                    System.out.println("Default Height: " + Console.DEFAULT_HEIGHT);
                    System.out.println("Current Type  : " + get().getClass().getCanonicalName());
                    Console console = get();
                    if (console != null) {
                        System.out.println();
                        System.out.println("Current:");
                        System.out.println("Type      : " + console.getClass().getCanonicalName());
                        System.out.println("CursorX   : " + console.getCursorX());
                        System.out.println("CursorY   : " + console.getCursorY());
                        System.out.println("Width     : " + console.getWidth());
                        System.out.println("Heigth    : " + console.getHeight());
                        System.out.println("Foreground: " + console.getForegroundColor());
                        System.out.println("Background: " + console.getBackgroundColor());
                        System.out.println(
                                "Support   : "
                                        + (console.isSupportBlink() ? "blink " : "")
                                        + (console.isSupportBold() ? "bold " : "")
                                        + (console.isSupportColor() ? "color " : "")
                                        + (console.isSupportCursor() ? "cursor " : "")
                                        + (console.isSupportSize() ? "size " : ""));
                        System.out.println(
                                "Attributes: "
                                        + (console.isBlink() ? "blink " : "")
                                        + (console.isBold() ? "bold " : ""));
                        System.out.println();
                        System.out.println("Ansi : " + console.isAnsi());
                        System.out.println("Blink: " + console.isBlink());
                        System.out.println("Bold : " + console.isBold());

                        try {
                            LineReaderImpl reader = console.adaptTo(LineReaderImpl.class);
                            System.out.println();
                            System.out.println("jline3 Console:");
                            System.out.println("  Type: " + reader.getTerminal().getType());
                        } catch (NotSupportedException e) {
                            System.out.println("Not a jline3 console");
                        }
                    }
                    System.out.println();
                    System.out.println(
                            "Supported colors: " + Arrays.toString(Console.COLOR.values()));
                }
                break;
            case "set":
                {
                    CmdInterceptorUtil.setInterceptor(
                            session, new ConsoleInterceptor(new KarafConsole(session)));
                    System.out.println(get().getWidth() + "x" + get().getHeight());
                }
                break;
            case "create":
                {
                    Console.resetConsole();
                    if (arguments != null && arguments.length > 0) {
                        Object console =
                                new OsgiBundleClassLoader()
                                        .loadClass(arguments[0])
                                        .getDeclaredConstructor()
                                        .newInstance();
                        CmdInterceptorUtil.setInterceptor(
                                session, new ConsoleInterceptor((Console) console));
                    } else {
                        Console console = Console.create();
                        CmdInterceptorUtil.setInterceptor(session, new ConsoleInterceptor(console));
                    }
                    System.out.println(get());
                }
                break;
            case "defaultsize":
                {
                    Console.DEFAULT_WIDTH = MCast.toint(arguments[0], Console.DEFAULT_WIDTH);
                    Console.DEFAULT_HEIGHT = MCast.toint(arguments[1], Console.DEFAULT_HEIGHT);
                    System.out.println(Console.DEFAULT_WIDTH + "x" + Console.DEFAULT_HEIGHT);
                }
                break;
            case "size":
                {
                    get().setWidth(MCast.toint(arguments[0], Console.DEFAULT_WIDTH));
                    get().setHeight(MCast.toint(arguments[1], Console.DEFAULT_HEIGHT));
                    System.out.println(get().getWidth() + "x" + get().getHeight());
                }
                break;
            case "reset":
                {
                    Console console = get();
                    console.resetTerminal();
                }
                break;
            case "cleanup":
                {
                    Console console = get();
                    console.cleanup();
                }
                break;
            case "color":
                {
                    Console console = get();
                    console.setColor(
                            Console.COLOR.valueOf(arguments[0].toUpperCase()),
                            Console.COLOR.valueOf(arguments[1].toUpperCase()));
                    System.out.println(
                            console.getForegroundColor() + " " + console.getBackgroundColor());
                }
                break;
            case "debug":
                {
                    System.out.println("Java        : " + MSystem.getJavaVersion());
                    System.out.println("Term        : " + System.getenv("TERM"));
                    System.out.println(
                            "COLUMNS     : " + MSystem.execute("tput", "cols").getOutput());
                    System.out.println(
                            "LINES       : " + MSystem.execute("tput", "lines").getOutput());
                    System.out.println(
                            "COLUMNS     : " + MSystem.execute("/bin/sh", "-c", "echo $COLUMNS"));
                    System.out.println(
                            "LINES       : " + MSystem.execute("/bin/sh", "-c", "echo $LINES"));
                    if (MSystem.isWindows())
                        System.out.println(
                                "CmdConsole  : "
                                        + Arrays.deepToString(
                                                new de.mhus.lib.core.console.CmdConsole()
                                                        .getRawSettings()));
                    else {
                        System.out.println("XTermConsole: " + new XTermConsole().getRawSettings());
                        for (String i : ANSIConsole.getRawAnsiSettings()) {
                            System.out.println(i);
                        }
                    }
                    System.out.println("stty        : " + XTermConsole.getRawTTYSettings());
                }
                break;
            case "keys":
                {
                    System.out.println(">>> Key debug mode - press 'q' to leave");
                    int cnt = 1000;
                    while (true) {
                        int key = get().read();
                        System.out.println("KeyCode: " + key);
                        if (key == ANSIConsole.KEY_q) break;
                        if (key == ANSIConsole.KEY_ENTER) System.out.println();
                        if (key < 0) {
                            System.out.println("Cnt: " + cnt);
                            cnt--;
                            Thread.sleep(100);
                        }
                    }
                }
                break;
            case "ask":
                {
                    char res =
                            Console.askQuestion(
                                    arguments[0], arguments[1].toCharArray(), true, true);
                    System.out.println("Result: " + res + " " + MString.toHexString(res));
                }
                break;
            case "test":
                {
                    LineReaderImpl reader = get().adaptTo(LineReaderImpl.class);
                    boolean echo = reader.getTerminal().echo();
                    reader.getTerminal().echo(false);
                    int c = reader.readCharacter();
                    System.out.println(c);
                    reader.getTerminal().echo(echo);
                }
                break;
            case "testline":
                {
                    LineReaderImpl reader = get().adaptTo(LineReaderImpl.class);
                    String line = reader.readLine(">");
                    System.out.println(line);
                }
                break;
            default:
                System.out.println("Command not found");
        }

        return null;
    }

    private Console get() {
        ConsoleInterceptor inter =
                CmdInterceptorUtil.getInterceptor(session, ConsoleInterceptor.class);
        if (inter == null) return Console.get();
        return inter.get();
    }
}
