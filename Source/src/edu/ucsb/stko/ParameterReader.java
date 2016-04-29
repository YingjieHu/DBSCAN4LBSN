package edu.ucsb.stko;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;

class ParameterReader
{
	  	public JSONObject readConfigParameters()
	  	{
	  			System.out.println("Loading the configuration parameters...");
	  			
	  			try
				{
						File configFile = new File("config.json");
						FileReader configFileReader = new FileReader(configFile);
						BufferedReader configBufferedReader = new BufferedReader(configFileReader);
						
						StringBuffer configContent = new StringBuffer();
						String thisInputLine = null;
						while((thisInputLine = configBufferedReader.readLine()) != null)
						{
								int hashIndex = thisInputLine.indexOf("#");
								if(hashIndex != -1)
								{
										thisInputLine = thisInputLine.substring(0, hashIndex);
								}
								configContent.append(thisInputLine);
						}
						configBufferedReader.close();
						
						JSONObject parameterObject = new JSONObject(configContent.toString());
						
						// examine if the config file is valid
						if(parameterObject.isNull("dataPath") || (parameterObject.getString("dataPath").length() == 0))
						{
								System.out.println("Empty path for the input location data; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("tempPath") || (parameterObject.getString("tempPath").length() == 0))
						{
								System.out.println("Empty path for the temporary workspace; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("lngIndex") || (parameterObject.getInt("lngIndex")==-1))
						{
								System.out.println("The index for longitude is not available; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("latIndex") || (parameterObject.getInt("latIndex")==-1))
						{
								System.out.println("The index for latitude is not available; please check the configuration file.");
								return null;
						}
						
					/*	if(parameterObject.getBoolean("removeDuplicates") && (parameterObject.getInt("userIDIndex") == -1))
						{
								System.out.println("You have indicated that you want to remove the duplicates. However, you didn't provide the userID index; please check the configuration file.");
								return null;
						}*/
						
						if(parameterObject.getBoolean("minPtsPercentage") && (parameterObject.getDouble("minPts")>1))
						{
								System.out.println("You have indicated that you want to use percentage for minPts. However, the minPts you provided is larger than 1; please check the configuration file.");
								return null;
						}
						
						if((!parameterObject.getBoolean("minPtsPercentage")) && (parameterObject.getDouble("minPts")<1))
						{
								System.out.println("You have indicated that you want to use absolute value for minPts. However, the minPts you provided is smaller than 1; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("lambda") || (parameterObject.getDouble("lambda")<1) || (parameterObject.getDouble("lambda")>100))
						{
								System.out.println("The lambda value for the concave hull should be between 1 and 100; please check the configuration file.");
								return null;
						}
						
						if(parameterObject.isNull("spatialReference"))
						{
								System.out.println("The spatial reference of the output is missing; please check the configuration file.");
								return null;
						}
						
						System.out.println(parameterObject.toString());
						System.out.println("Configuration parameters have been successfully loaded...");
						System.out.println("---------------------------------------------------");
						
						return parameterObject;
						
						
				} 
	  			catch (Exception e)
				{
						System.out.println("An issue has happened with the configuration file config.json; Please double check this file to ensure it is correct.");
						return null;
				}

	  	}
		

}
