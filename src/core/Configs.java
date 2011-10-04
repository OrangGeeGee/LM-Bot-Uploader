package core;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.Properties;

import org.ini4j.Wini;

public class Configs {
	public static final String SECTION_PATHS = "Paths";
	public static final String SECTION_LM = "LinkoManija";
	public static final String KEY_COOKIE = "userLoginCookie";
	public static final String KEY_OUTPUT_DIR = "torrentWatchDirectory";
	public static final String KEY_FILES_DIR = "extractedTorrentsDirectory";
	
	
	private String filename = "config.ini";
	private File file;
	public Wini ini;
	
	public Configs() throws FileSystemException, IOException {
		file = new File(filename);
		
		if(file.exists()) {
			if(!file.canWrite() || !file.canRead()) {
				throw new FileSystemException(filename);
			}
		} else {
			file.createNewFile();
		}
		
		ini = new Wini(file);
		initDefaultValues();
	}
	
	
	public void edit() throws IOException {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
        	desktop = Desktop.getDesktop();
        }

        desktop.edit(file);
	}
	
	private void initDefaultValues() throws IOException {
		// TODO add comments to config file
		if(ini!=null) {
			if(ini.get(SECTION_PATHS, KEY_FILES_DIR)==null) {
				ini.add(SECTION_PATHS, KEY_FILES_DIR, "");
				//ini.putComment(key, comment);
			}
			
			if(ini.get(SECTION_PATHS, KEY_OUTPUT_DIR)==null) {
				ini.add(SECTION_PATHS, KEY_OUTPUT_DIR, "");
			}
			
			if(ini.get(SECTION_LM, KEY_COOKIE)==null) {
				ini.add(SECTION_LM, KEY_COOKIE, "");
			}
			
			ini.store();
		}
	}
}
