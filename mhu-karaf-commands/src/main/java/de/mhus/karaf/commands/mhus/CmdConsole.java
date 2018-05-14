/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import java.io.IOException;
import java.util.Arrays;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.XTermConsole;

@Command(scope = "mhus", name = "console", description = "Manipulate and control the console")
@Service
public class CmdConsole implements Action {

	@Argument(index=0, name="cmd", required=false, description="Command: \n"
			+ " info            - Print current console instance information\n"
			+ " create [<type>] - create new console of type: SIMPLE,ANSI,ANSI_COLOR,XTERM,XTERM_COLOR,CMD\n"
			+ " size <cols/width> <rows/height> - set static cols and rows\n"
			+ " reset           - recreate console instance\n"
			+ " cleanup         - cleanup console\n"
			+ " color <fg> <bg> - set colors\n"
			+ " debug           - print console debug information", multiValued=false)
    String cmd;
	
	@Argument(index=1, name="arguments", required=false, description="arguments", multiValued=true)
    String arguments[];

	@Override
	public Object execute() throws Exception {

		
		switch (cmd) {
		case "info": {
			System.out.println("Default Width :" + Console.DEFAULT_WIDTH);
			System.out.println("Defualt Height: " + Console.DEFAULT_HEIGHT);
			System.out.println("Current Type  : " + Console.getConsoleType() );
			Console console = Console.get();
			if (console != null) {
				System.out.println();
				System.out.println("Current:");
				System.out.println("Type      : " + console.getClass().getCanonicalName());
				System.out.println("CursorX   : " + console.getCursorX());
				System.out.println("CursorY   : " + console.getCursorY());
				System.out.println("Width     : " + console.getWidth());
				System.out.println("Heigth    : " + console.getHeight());
				System.out.println("Foreground: " + console.getForegroundColor());
				System.out.println("Background: "+console.getBackgroundColor());
				System.out.println("Support   : " + (console.isSupportBlink() ? "blink " : "") + (console.isSupportBold() ? "bold " : "") + (console.isSupportColor() ? "color " : "") + (console.isSupportCursor() ? "cursor " : "") + (console.isSupportSize() ? "size ": "") );
				System.out.println("Attributes: " + (console.isBlink() ? "blink " : "") + (console.isBold() ? "bold " : "") );
			}
			System.out.println();
			System.out.println("Supported types : " + Arrays.toString(Console.CONSOLE_TYPE.values()));
			System.out.println("Supported colors: " + Arrays.toString(Console.COLOR.values()));
		} break;
		case "create": {
			Console.resetConsole();
			Console.setConsoleType(null);
			if (arguments != null && arguments.length > 0) {
				Console.setConsoleType(Console.CONSOLE_TYPE.valueOf(arguments[0].toUpperCase()));
			}
			System.out.println( Console.create() );
		} break;
		case "size": {
			Console.DEFAULT_WIDTH = MCast.toint(arguments[0], Console.DEFAULT_WIDTH);
			Console.DEFAULT_HEIGHT = MCast.toint(arguments[1], Console.DEFAULT_HEIGHT);
			System.out.println(Console.DEFAULT_WIDTH + "x" + Console.DEFAULT_HEIGHT );
		} break;
		case "reset": {
			Console console = Console.create();
			console.resetTerminal();
		} break;
		case "cleanup": {
			Console console = Console.create();
			console.cleanup();
		} break;
		case "color": {
			Console console = Console.create();
			console.setColor(Console.COLOR.valueOf(arguments[0].toUpperCase()), Console.COLOR.valueOf(arguments[1].toUpperCase()));
			System.out.println(console.getForegroundColor() + " " + console.getBackgroundColor());
		} break;
		case "debug": {
			System.out.println("Java        : " + MSystem.getJavaVersion());
			System.out.println("Term        : " + System.getenv("TERM"));
			System.out.println("COLUMNS: " + System.getenv("COLUMNS"));
			System.out.println("LINES  : " + System.getenv("LINES"));
			if (MSystem.isWindows())
				System.out.println("CmdConsole  : " + Arrays.deepToString( new de.mhus.lib.core.console.CmdConsole().getRawSettings() ));
			else {
				System.out.println("XTermConsole: " + Arrays.deepToString( new XTermConsole().getRawSettings() ));
			}
			System.out.println("stty        : " + Arrays.deepToString( getRawSettings() ));
		} break;
		default:
			System.out.println("Command not found");
		}
		
		return null;
	}

	public String[] getRawSettings() {
		try {
			String[] ret;
			ret = MSystem.execute("/bin/sh","-c","stty -a < /dev/tty");
			return ret;
		} catch (IOException e) {
			return new String[] {e.toString()};
		}
	}

}
