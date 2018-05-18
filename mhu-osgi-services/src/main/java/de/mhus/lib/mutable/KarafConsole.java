package de.mhus.lib.mutable;

import java.io.IOException;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.console.ANSIConsole;

public class KarafConsole extends ANSIConsole {

	private Session session;

	public KarafConsole(Session session) throws IOException {
		super();
		this.session = session;
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
