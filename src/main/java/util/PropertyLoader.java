package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

	private static Properties properties;
	
	static{
		properties = new Properties();
	     try {
			properties.load(new FileInputStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String parameter){
		assert parameter != null: "Precondition failed: parameter != null";
		
	     return properties.getProperty(parameter);
	}
}
