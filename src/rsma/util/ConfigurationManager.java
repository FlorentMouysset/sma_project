package rsma.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigurationManager {
	private static ConfigurationManager instance = null;
	private Properties prop;
	
	private ConfigurationManager(){
		
	}
	
	private static ConfigurationManager getInstance(){
		if(instance == null){
			instance = new ConfigurationManager();
			instance.prop = new Properties();
		}
		return instance;
	}
	
	public synchronized static void loadPropertiesFile(String propFile){
		InputStream in;
		ConfigurationManager cm = getInstance();
		try {
			 in = cm.getClass().getClassLoader().getResourceAsStream(propFile);
			
			cm.prop.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String getProperty(String key){
		return getInstance().prop.getProperty(key);
	}
	
}
