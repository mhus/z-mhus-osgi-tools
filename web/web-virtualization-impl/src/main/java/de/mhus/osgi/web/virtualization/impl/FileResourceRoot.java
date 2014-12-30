package de.mhus.osgi.web.virtualization.impl;

import java.io.File;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.directory.ResourceNode;

public class FileResourceRoot extends FileResource {

	public FileResourceRoot(File documentRoot) {
		super(null, null, documentRoot);
	}

	public ResourceNode getResource(String target) {
		
		return getResource(this,target);
	}

	private ResourceNode getResource(FileResource parent,
			String target) {
		if (parent == null || target == null) return null;
		if (target.length() == 0) return parent;
		
		String next = null;
		if (MString.isIndex(target, '/')) {
			next = MString.beforeIndex(target, '/');
			target = MString.afterIndex(target, '/');
		} else {
			next = target;
			target = "";
		}
		if (next.length() == 0) return getResource(parent,target);
		
		ResourceNode n = parent.getNode(next);
		if (n == null) return null;
		
		return getResource((FileResource) n, target);
	}

}
