package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Job {
	private String path;
	private File[] files;
	private File outputDir;
	private File filesDir;
	private Configs cfgs;
	private File nfo;
	
	public Job(String path) {
		this.path = path;
	}
	
	public void start() {
		try {
			// check for config file integrity
			this.cfgs = new Configs();
			String torrentName;
			String filesPath = cfgs.ini.get(Configs.SECTION_PATHS, Configs.KEY_FILES_DIR);
			String outputPath = cfgs.ini.get(Configs.SECTION_PATHS, Configs.KEY_OUTPUT_DIR);
			this.filesDir = new File(filesPath);
			this.outputDir = new File(outputPath);
			if(!this.filesDir.isDirectory()) {
				throw new Exception("Blogai nurodyta failu direktorija");
			}
			if(!this.outputDir.isDirectory()) {
				throw new Exception("Blogai nurodyta .torrent issaugojimo direktorija");
			}
			
			// checking if path exists and tidying up files
			File f = new File(this.path);
			if(f.isDirectory()) {
				// if param is a dir
				this.files = f.listFiles();
				
				// create dir
				torrentName = files[0].getParentFile().getName();
				File newDir = new File(this.filesDir, torrentName+"/");
				if(newDir.mkdirs()) {
					echo("Dir created "+newDir.getPath());
				}

				// TODO see if there is .nfo and copy it
				String tmp;
				int fileCount = this.files.length;
				int i = 0;
				while (i < fileCount) {
					tmp = this.files[i].getName();
					if(tmp.substring( tmp.length()-4 ) == "nfo") {
						nfo = this.files[i];
						copy(nfo.getAbsolutePath(), newDir.getAbsolutePath());
						break;
					}
					i++;
				}
				
				// TODO recursive directories search for rars
				// TODO copy extracted .rar files
				
				// TODO copy all other non .rar/.nfo files
				
			} else if(f.isFile()) {
				// if param is a file
				this.files = new File[1];
				this.files[0] = f;
				
				// TODO copy file to the dir
				String filename = f.getName();
				String src = f.getAbsolutePath();
				String dest = filesDir.getAbsolutePath()+filename;
				copy(src, dest);
				torrentName = stripExtension(filename);
				echo("File copied to "+dest.toString());
			} else {
				throw new Exception("Failas/folderis nerastas.");
			}
			
			// TODO make a new torrent file
			
			// TODO make description for lm
			
			// TODO determine lm category 
			
			// TODO upload torrent to lm
			
			// TODO download new torrent file and save it to output dir
			
		} catch(Exception e) {
			echo("Exception. "+e.getMessage());
			e.printStackTrace(System.out);
		}		
	}
	
	public void unpackAll(File[] files) {
		// TODO implement unpacking of all content
	}

	public static void echo(String arg) {
		System.out.println(arg);
	}

    public static String stripExtension(String fn) {
        if (fn == null) return null;
        int pos = fn.lastIndexOf(".");
        if (pos == -1) return fn;
        return fn.substring(0, pos);
    }
    
    public static void copy(String from, String to) throws IOException {
		Path src = Paths.get(from);
		Path dest = Paths.get(to);
		Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);    	
    }

}
