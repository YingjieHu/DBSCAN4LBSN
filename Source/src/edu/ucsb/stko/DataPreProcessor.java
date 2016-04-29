package edu.ucsb.stko;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javafx.geometry.Point2D;

import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;

class DataPreProcessor
{
		public JSONObject preprocessData(JSONObject parameterObject)
		{
				System.out.println("Preprocessing the input data...");
				try
				{
						double duplicateDistance = parameterObject.getDouble("eps");
						
						String inputFilePath = parameterObject.getString("dataPath");
						File inputFile = new File(inputFilePath);
						FileReader inputFileReader = new FileReader(inputFile);
						CSVReader inputCSVReader = new CSVReader(inputFileReader);
						
						String newLineSymbol = System.getProperty("line.separator");
						
						String tempFileName = parameterObject.getString("tempPath")+"/"+inputFile.getName().replace(".csv", "")+"_processed.csv";
						File outputFile = new File(tempFileName);
						if(outputFile.exists())
						{
							outputFile.delete();
							outputFile.createNewFile();
						}
						FileWriter outputFileWriter = new FileWriter(outputFile,true);
						
						
						// get the indexes
						int  recordIDIndex = parameterObject.getInt("recordIDIndex");   
						int userIDIndex =  parameterObject.getInt("userIDIndex");        
						int lngIndex = parameterObject.getInt("lngIndex");               
						int latIndex =  parameterObject.getInt("latIndex");   
						
						/*boolean removeDuplicates = parameterObject.getBoolean("removeDuplicates");
						if(removeDuplicates && (userIDIndex == -1))
						{
								System.out.println("You have indicated to remove duplicated records, but didn't provide the column number of user id; please check the configuration file.");
								inputCSVReader.close();
								outputFileWriter.close();
								return null;
						}*/
						
						Hashtable<String, Vector<Point2D>> existingDataHashtable = new Hashtable<>(1000);
						long totalProcessedDataRecord = 0;
						String[] thisInputLine = inputCSVReader.readNext();
						while((thisInputLine = inputCSVReader.readNext()) != null)
						{
    							String recordId = null;
    							String ownerString = null;
    							double latString = 0;
    							double lngString = 0;

    							try 
    							{
        							recordId = thisInputLine[recordIDIndex];
        							if(userIDIndex != -1)
        									ownerString = thisInputLine[userIDIndex];
        							
        							latString = Double.parseDouble(thisInputLine[latIndex]);
        							lngString = Double.parseDouble(thisInputLine[lngIndex]);
    							} 
    							catch (Exception e) 
    							{
    								continue;
    							}
							
    							if(userIDIndex != -1)
    							{
    									boolean isDuplicated = false;
    	    							if(existingDataHashtable.containsKey(ownerString))
    	    							{
    	    								Vector<Point2D> peopleCoordsVector = existingDataHashtable.get(ownerString);
    	    								Iterator<Point2D> coordIterator = peopleCoordsVector.iterator();
    	    								while(coordIterator.hasNext())
    	    								{
    	    									Point2D coordPoint = coordIterator.next();
    	    									double thisDistance = Math.sqrt((coordPoint.getX() - lngString)*(coordPoint.getX() - lngString) + (coordPoint.getY() - latString) * (coordPoint.getY() - latString));
    	    									if(thisDistance<= duplicateDistance)
    	    									{
    	    										isDuplicated = true;
    	    										break;
    	    									}
    	    								}
    	    							}
    	    							else
    	    							{
    	    								Vector<Point2D> peopleCoordsVector = new Vector<>(10);
    	    								existingDataHashtable.put(ownerString, peopleCoordsVector);
    	    							}
    								
        								if(!isDuplicated)
        								{
        									// add this point to existing table       									
        									Vector<Point2D> peopleCoordsVector = existingDataHashtable.get(ownerString);
        									peopleCoordsVector.add(new Point2D(lngString, latString));
        									existingDataHashtable.put(ownerString, peopleCoordsVector);
        									
        									outputFileWriter.append(recordId+","+ownerString+","+latString+","+lngString+newLineSymbol);
        									totalProcessedDataRecord++;
        								}
    									
    							}
    							else
    							{
										/*if(userIDIndex != -1)
										{
												existingDataHashtable.put(ownerString, new Vector<Point2D>(1));
												outputFileWriter.append(recordId+","+ownerString+","+latString+","+lngString+newLineSymbol);
										}
										else
										{*/
										outputFileWriter.append(recordId+","+latString+","+lngString+newLineSymbol);
										//}
										totalProcessedDataRecord++;
								}
    							
						}
						
						inputCSVReader.close();
						outputFileWriter.close();
						
						JSONObject resultObject = new JSONObject();
						resultObject.put("file", tempFileName);
						if(userIDIndex != -1)
								resultObject.put("userCount", existingDataHashtable.size());
						resultObject.put("recordCount", totalProcessedDataRecord);
						
						if(userIDIndex != -1)
								System.out.println("After pre-processing, there are "+ totalProcessedDataRecord+" records and "+ existingDataHashtable.size()+" users in the data.");
						else 
								System.out.println("After pre-processing, there are "+ totalProcessedDataRecord+" records in the data.");
						System.out.println("---------------------------------------------------");
						
						return resultObject;
						
				} 
				catch (Exception e)
				{
						System.out.println("An error happened in the data preprocessing; The program has been canceled");
						return null;
				}
		}

}
