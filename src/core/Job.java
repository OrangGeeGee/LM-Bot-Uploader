package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import de.innosystec.unrar.exception.RarException;

public class Job {
	private String path;
	private ArrayList<File> files;
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
				this.files = new ArrayList<File>( Arrays.asList(f.listFiles()) );
				
				// create dir
				torrentName = files.get(0).getParentFile().getName();
				File newDir = new File(this.filesDir, torrentName+"/");
				if(newDir.mkdirs()) {
					echo("Dir created "+newDir.getPath());
				}

				// see if there is .nfo and copy it
				String tmpName, ending;
				Iterator<File> iterator = this.files.iterator();
				File tmpFile;
				while(iterator.hasNext()) {
					tmpFile = iterator.next();
					if(tmpFile.isFile()) {
						tmpName = tmpFile.getName();
						ending = tmpName.substring( tmpName.length()-4 );
						if(ending.equals(".nfo")) {
							echo("Found nfo file "+tmpName);
							nfo = tmpFile;
							copy(nfo.getAbsolutePath(), newDir.getAbsolutePath()+"/"+nfo.getName());
							this.files.remove(tmpFile);
							break;
						}
					}
				}
				
				// extract rars or copy everything recursively to destination dir
				extractOrCopy(this.files, newDir);
				
				// list of resulting files for further manipulation
				
			} else if(f.isFile()) {
				// if param is a single file
				// copy file to the dir
				String filename = f.getName();
				String src = f.getAbsolutePath();
				String dest = filesDir.getAbsolutePath()+"/"+filename;
				copy(src, dest);
				torrentName = stripExtension(filename);
				echo("File copied to "+dest);
				
				// list of one file for further manipulation
				this.files = new ArrayList<File>(1);
				this.files.add(new File(dest));
			} else {
				throw new Exception("Failas/folderis nerastas.");
			}
			
			// TODO make a new torrent file
			
			// TODO make description for lm
			
			// TODO determine lm category 
			
			// TODO upload torrent to lm
			
			// TODO download new torrent file and save it to output dir
			echo("Finish up the torrent");
		} catch(Exception e) {
			echo("Exception. "+e.getMessage());
			e.printStackTrace(System.out);
		}		
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
    
    public static void copy(File from, File to) throws IOException {
    	copy(from.getAbsolutePath(), to.getAbsolutePath());
    }
    public static void copy(String from, String to) throws IOException {
		Path src = Paths.get(from);
		Path dest = Paths.get(to);
		Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);    	
    }
    
    public void extractOrCopy(ArrayList<File> fromFiles, File destinationDir) throws RarException, IOException {
    	// recursive directories search for rars and other files
    	// copies all other non .rar/.nfo files
    	
    	// extracts .rar files with parts e.g. .r01, .r02 etc
    	Iterator<File> iterator = fromFiles.iterator();
    	File file;
    	String ending, name;
    	while(iterator.hasNext()) {
    		file = iterator.next();
    		if(file.isDirectory()) {
    			ArrayList<File> inside = new ArrayList<File>(Arrays.asList(file.listFiles()));
    			extractOrCopy(inside, destinationDir);
    		} else {
    			name = file.getName();
    			ending = name.substring( name.length()-4 );
    			if(name.matches(".*((.part([2-9]|[0-9]{2,}).rar)|.r[0-9]{2})")) {
    				echo("Found rar file "+name+" but it's a part; ignoring");
    			} else if(name.matches(".*((.part1.rar)|(.rar))")) {
    				echo("Found rar file "+name);
    				extract(file, destinationDir);
    			} else {
    				echo("Copying a file "+name);
    				copy(file, new File(destinationDir, name));
    			}
    		}
    	}
    	
    }
    
    public boolean extract(File rarFile, File destination) throws RarException, IOException {
        try {
            Runtime rt = Runtime.getRuntime();
            //Process pr = rt.exec("cmd /c dir");
            
            String unrarPath = cfgs.ini.get(Configs.SECTION_PATHS, Configs.KEY_UNRAR_PATH);
            String unrarPathCompiled = String.format( unrarPath, rarFile.getAbsolutePath(), destination.getAbsolutePath()+"\\" );
            Process pr = rt.exec(unrarPathCompiled);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line=null;

            while((line=input.readLine()) != null) {
            	if(!line.matches("\\s*"))
            		System.out.println(line);
            }

            int exitVal = pr.waitFor();
            System.out.println("Exited with code "+exitVal);
            if(exitVal == 0) return true;

        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
        return false;
    	
    	/*
    	Archive rar = new Archive(rarFile, new UnrarCallback() {
			public void volumeProgressChanged(long current, long total) {
				echo( String.format("%d/%d", current, total) );
			}
			public boolean isNextVolumeReady(File nextVolume) {
				echo ("next volume "+nextVolume.getName());
				return true;
			}
		});
    	
    	//Volume.nextVolumeName(arcName, oldNumbering);
    	//Volume.mergeArchive(archive, dataIO);
    	//Volume.
		List<FileHeader> packedFiles = rar.getFileHeaders();
		Iterator<FileHeader> iterator = packedFiles.iterator();
		FileHeader fhead;
		int dataSize;
		File output;
		// iterate over all packed files and copy non directories
		while(iterator.hasNext()) {
			fhead = iterator.next();
			String fn = fhead.getFileNameString();
			dataSize = fhead.getDataSize();
			if(dataSize>0) {
				output = new File(destination, fn);
				output.getParentFile().mkdirs();
				output.createNewFile();
				OutputStream os = new FileOutputStream(output);
				rar.extractFile(fhead, os);
				
			}
		}*/
    	
    }

}
