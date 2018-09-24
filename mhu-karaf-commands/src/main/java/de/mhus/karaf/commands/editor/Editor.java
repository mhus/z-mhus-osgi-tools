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
package de.mhus.karaf.commands.editor;

import de.mhus.lib.core.console.Console;

public class Editor {
	
	protected Content content = new Content();
	protected Screen screen = new Screen();
	protected Actions actions = new Actions();
	protected InputProcessor inputProcessor = new InputProcessor();
	private Console console = Console.get();

	private int cursorPos  = 0;
	private int cursorLine = 0;
	private boolean closed = false;
	
	public Content getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content = content;
		this.content.init(this);
		resetView();
	}
	
	public void resetView() {
		cursorLine = 0;
		cursorPos = 0;
		screen.repaintAll();
	}

	public Screen getScreen() {
		return screen;
	}

	public Actions getActions() {
		return actions;
	}

	public int getCursorPos() {
		return cursorPos;
	}

	public void setCursorPos(int cursorPos) {
		this.cursorPos = cursorPos;
	}

	public int getCursorLine() {
		return cursorLine;
	}

	public void setCursorLine(int cursorLine) {
		this.cursorLine = cursorLine;
	}

	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	public void edit() {
		inputProcessor.init(this);
		content.init(this);
		actions.init(this);
		screen.init(this);
		
		while(!isClosed()) {
			inputProcessor.process();
		}
		
		console.cleanup();
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public Console getConsole() {
		return console;
	}
	
}
