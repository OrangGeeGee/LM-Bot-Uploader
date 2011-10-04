package core;

import java.io.IOException;
import java.nio.file.FileSystemException;

public class Launcher {
	public static void main(String[] args) {
		echo("LM-Bot-Uploader v0.1.");
		echo("");
		
		int n = args.length;
		String path = "";
		boolean action = false;
		
		if (n == 0) {
			// be argumentu rodyti help'à
			echo("Paleiskite su parametru /?, kad pamatyti galimybes");
		} else {
			for(int i = 0; i < n; i++) {
				// jeigu tai komanda atpaþink ir vykdyk
				if( (args[i].charAt(0)=='/' || args[i].charAt(0)=='-') && args[i].length() == 2) {
					char c = args[i].charAt(1);
					switch (c) {
					case 'c':
						// init config
						echo("Atidaromas konfiguracijos failas");
						Configs cfgs;
						try {
							cfgs = new Configs();
							cfgs.edit();
						} catch (IOException e) {
							echo(e.getMessage());
							e.printStackTrace();
						}
						break;
					case 'b':
						// bot it
						action = true;
						break;
					case '?':
						echo("Galimi parametrai:");
						echo("-h: parodomas sis pranesimas");
						echo("-c: failo konfiguravimas");
						echo("-b %path%: programa atliks savo funkcija nurodytu keliu");
						// displaying help
						break;
					default:
						// unknown flag
						echo("Error reading "+args[i]+"; aborting");
						break;
					}
				} else {
					path = args[i]; 
				}
			}
		}
		if(action == true) {
			echo("Doing my job on "+path);
			(new Job(path)).start();
		}
	}
	
	public static void echo(String arg) {
		System.out.println(arg);
	}

}
