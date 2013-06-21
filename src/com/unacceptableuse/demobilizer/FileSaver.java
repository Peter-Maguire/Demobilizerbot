package com.unacceptableuse.demobilizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class FileSaver {

	
	public static void save(Object file, String path)
	{
		FileOutputStream fos;
		ObjectOutputStream oos;
	
		try{
			
			fos = new FileOutputStream(path);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(file);
		}
		catch(FileNotFoundException e)
		{
			new File(path).mkdir();
		}
		catch(Exception e){e.printStackTrace();
		}
	

	}
	
	public static Object load(String path)
	{
		try {

			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			return ois.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	
	}
	public static String getCleanPath() {
	    ClassLoader classLoader = FileSaver.class.getClassLoader();
	    File classpathRoot = new File(classLoader.getResource("").getPath());

	    return classpathRoot.getPath();
	}
}
