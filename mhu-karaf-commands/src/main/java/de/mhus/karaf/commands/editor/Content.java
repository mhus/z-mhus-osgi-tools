package de.mhus.karaf.commands.editor;

import java.util.LinkedList;

public class Content {

	private LinkedList<Line> lines = new LinkedList<>();
	private Editor editor;
	
	public void init(Editor editor) {
		this.editor = editor;
	}

	public int lines() {
		return lines.size();
	}
	
	public int lineSize(int line) {
		if (line < 0 || line >= lines.size()) return 0;
		return lines.get(line).size();
	}
	
	public Line getLine(int line) {
		if (line < 0 || line >= lines.size()) return null;
		return lines.get(line);
	}	
}
