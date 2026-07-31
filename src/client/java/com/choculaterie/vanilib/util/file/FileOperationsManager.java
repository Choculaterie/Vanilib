package com.choculaterie.vanilib.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileOperationsManager {
	private static final int MAX_UNDO_HISTORY = 50;

	private final File trashFolder;
	private final List<FileAction> undoStack = new ArrayList<>();
	private final List<FileAction> redoStack = new ArrayList<>();

	public FileOperationsManager(File trashFolder) {
		this.trashFolder = trashFolder;
	}

	public void moveFiles(List<File> files, File targetFolder) {
		List<FileOperation> operations = new ArrayList<>();
		for (File file : files) {
			File destination = new File(targetFolder, file.getName());
			operations.add(new FileOperation(file, destination));
			file.renameTo(destination);
		}
		recordAction(new FileAction(FileAction.Type.MOVE, operations));
	}

	public void deleteFiles(List<File> files) {
		List<FileOperation> operations = new ArrayList<>();
		for (File file : files) {
			operations.add(new FileOperation(file, new File(trashFolder, file.getName())));
			file.renameTo(new File(trashFolder, file.getName()));
		}
		recordAction(new FileAction(FileAction.Type.DELETE, operations));
	}

	public void renameFile(File file, String newName) {
		File newFile = new File(file.getParent(), newName);
		file.renameTo(newFile);
		recordAction(new FileAction(FileAction.Type.RENAME, new FileOperation(file, newFile)));
	}

	public void createFolder(String folderName, File parentDir) {
		File newFolder = new File(parentDir, folderName);
		newFolder.mkdirs();
		recordAction(new FileAction(FileAction.Type.CREATE_FOLDER, new FileOperation(newFolder, newFolder)));
	}

	public void performUndo() {
		if (undoStack.isEmpty()) return;
		FileAction action = undoStack.remove(undoStack.size() - 1);
		for (FileOperation op : action.operations) {
			if (action.type == FileAction.Type.DELETE || action.type == FileAction.Type.MOVE) {
				op.destination.renameTo(op.source);
			}
		}
		redoStack.add(action);
	}

	public void performRedo() {
		if (redoStack.isEmpty()) return;
		FileAction action = redoStack.remove(redoStack.size() - 1);
		for (FileOperation op : action.operations) {
			op.source.renameTo(op.destination);
		}
		undoStack.add(action);
	}

	private void recordAction(FileAction action) {
		undoStack.add(action);
		redoStack.clear();
		if (undoStack.size() > MAX_UNDO_HISTORY) {
			undoStack.remove(0);
		}
	}

	public boolean deleteDirectoryRecursively(File directory) {
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirectoryRecursively(file);
				}
			}
		}
		return directory.delete();
	}

	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	public boolean canRedo() {
		return !redoStack.isEmpty();
	}

	private static final class FileAction {
		enum Type { MOVE, DELETE, RENAME, CREATE_FOLDER }

		final Type type;
		final List<FileOperation> operations;

		FileAction(Type type, List<FileOperation> operations) {
			this.type = type;
			this.operations = operations;
		}

		FileAction(Type type, FileOperation operation) {
			this.type = type;
			this.operations = List.of(operation);
		}
	}

	private record FileOperation(File source, File destination) {}
}
