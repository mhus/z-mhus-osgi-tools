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

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.XTermConsole;

@Command(scope = "mhus", name = "console", description = "Manipulate and control the console")
@Service
public class CmdConsole implements Action {

	@Argument(index=0, name="cmd", required=false, description="info, create [<type>], size <cols/width> <rows/height>, reset, cleanup, color <fg> <bg>", multiValued=false)
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
		case "stty": {
			System.out.println( Arrays.deepToString( new XTermConsole().getRawSettings() ));
		}
		}
		
		return null;
	}

}
