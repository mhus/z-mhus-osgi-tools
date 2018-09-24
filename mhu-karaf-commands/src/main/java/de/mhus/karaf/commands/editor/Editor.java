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
