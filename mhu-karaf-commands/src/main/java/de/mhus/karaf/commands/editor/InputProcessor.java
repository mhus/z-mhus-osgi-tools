package de.mhus.karaf.commands.editor;

public class InputProcessor {

	private Editor editor;

	public void process() {
		int key = editor.getConsole().read();
		boolean alt = false;
		if (key >= 1000) {
			alt = true;
			key = key - 1000;
		}
		System.out.println(alt + " " + key);
		if (key == 113) editor.setClosed(true);
	}

	public void init(Editor editor) {
		this.editor = editor;
	}

}
