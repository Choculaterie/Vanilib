package com.choculaterie.vanilib.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectorySearchManager {
	private String query = "";
	private final List<FileEntry> results = new ArrayList<>();

	public void updateQuery(String query) {
		this.query = query.toLowerCase();
	}

	public boolean isActive() {
		return !query.isEmpty();
	}

	public List<FileEntry> search(File rootDirectory) {
		results.clear();
		if (!query.isEmpty()) {
			searchRecursively(rootDirectory, "");
		}
		return results;
	}

	private void searchRecursively(File directory, String pathPrefix) {
		File[] files = directory.listFiles();
		if (files == null) return;

		for (File file : files) {
			String displayPath = pathPrefix.isEmpty() ? file.getName() : pathPrefix + "/" + file.getName();
			if (file.getName().toLowerCase().contains(query)) {
				results.add(new FileEntry(file, displayPath));
			}
			if (file.isDirectory()) {
				searchRecursively(file, displayPath);
			}
		}
	}

	public record FileEntry(File file, String relativePath) {
		public boolean isDirectory() {
			return file.isDirectory();
		}
	}
}
