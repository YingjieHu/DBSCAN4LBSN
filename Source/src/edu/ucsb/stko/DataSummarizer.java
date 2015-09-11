package edu.ucsb.stko;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;

class DataSummarizer
{
		public JSONObject summarizeData(JSONObject parameterObject)
		{
				System.out.println("Summarizing the input data...");
				try
				{
						File inputFile = new File(parameterObject.getString("dataPath"));
						FileReader inputFileReader = new FileReader(inputFile);
						CSVReader csvReader = new CSVReader(inputFileReader);
											
						int userIDIndex = parameterObject.getInt("userIDIndex");
	
						Hashtable<String, Integer> userHashtable = new Hashtable<>();
						long totalRecordCount = 0;
						String[] thisInputLine = csvReader.readNext();
						while((thisInputLine = csvReader.readNext())!=null)
						{
								if(userIDIndex != -1)
								{
										String userIDString = thisInputLine[userIDIndex];
										userHashtable.put(userIDString, 0);
								}
								totalRecordCount++;
						}
						csvReader.close();
						
						JSONObject summaryObject = new JSONObject();
						summaryObject.put("recordCount", totalRecordCount);
						
						if(userIDIndex != -1)
						{
								summaryObject.put("userCount", userHashtable.size());
								System.out.println("There are "+totalRecordCount+" records and "+userHashtable.size()+" users in the data.");
						}
						else
						{
								System.out.println("There are "+totalRecordCount+" records in the data.");
						}
						
						System.out.println("Data summary finished...");
						System.out.println("---------------------------------------------------");
						return summaryObject;
				} 
				catch (Exception e)
				{
						System.out.println("An error happened in the data summarizing process; The program has been canceled");
						return null;
				}
		}

}
