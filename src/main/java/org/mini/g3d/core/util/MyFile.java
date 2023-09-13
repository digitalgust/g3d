package org.mini.g3d.core.util;

import java.io.*;

/**
 * Represents a "file" inside a Jar File. Used for accessing resources (models, textures), as they
 * are all inside a jar file when exported.
 *
 * @author Karl
 */
public class MyFile {

    private static final String FILE_SEPARATOR = File.separator;

    private String path;
    private String name;

    public MyFile(String path) {
        this.path = FILE_SEPARATOR + path;
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public MyFile(String... paths) {
        this.path = "";
        for (String part : paths) {
            this.path += (FILE_SEPARATOR + part);
        }
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

//	public MyFile(MyFile file, String subFile) {
//		this.path = file.path + FILE_SEPARATOR + subFile;
//		this.name = subFile;
//	}
//
//	public MyFile(MyFile file, String... subFiles) {
//		this.path = file.path;
//		for (String part : subFiles) {
//			this.path += (FILE_SEPARATOR + part);
//		}
//		String[] dirs = path.split(FILE_SEPARATOR);
//		this.name = dirs[dirs.length - 1];
//	}

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath();
    }

    public InputStream getInputStream() {
        return MyFile.class.getResourceAsStream(path);
    }

    public BufferedReader getReader() throws Exception {
        try {
            InputStreamReader isr = new InputStreamReader(getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            return reader;
        } catch (Exception e) {
            System.err.println("[G3D][WARN]Couldn't get reader for " + path);
            throw e;
        }
    }

    public String getName() {
        return name;
    }

}
