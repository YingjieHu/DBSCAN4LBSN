package edu.ucsb.stko;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONObject;

public class DBSCANMainOperator
{
		private static JSONObject parameterObject = null;
		
		public static void main(String[] args)
		{
				// Read parameters from the configuration file into memory
				ParameterReader parameterReader = new ParameterReader();
				parameterObject = parameterReader.readConfigParameters();
				if(parameterObject ==  null) return;   
			
				
				// summarize data
				DataSummarizer dataSummarizer = new DataSummarizer();
				JSONObject dataSummaryObject = dataSummarizer.summarizeData(parameterObject);
				if(dataSummaryObject ==  null) return;
				
							
				// initialize Esri JSON object
				EsriJSONInitialiser esriJSONInitialiser = new EsriJSONInitialiser();
				JSONObject esriJsonObject = esriJSONInitialiser.initializeEsriJsonObject(parameterObject);
				if(esriJsonObject == null) return;
				
				
				// pre-process data
				DataPreProcessor dataPreProcessor = new DataPreProcessor();
				JSONObject processedDataSummaryObject = dataPreProcessor.preprocessData(parameterObject);
				if(processedDataSummaryObject == null) return;
				
				
				// clustering and construct shapes
				ClusterAndShapeGenerator clusterAndShapeGenerator = new ClusterAndShapeGenerator();
				JSONObject clusteringResultObject = clusterAndShapeGenerator.generateClusterAndShapes(parameterObject, dataSummaryObject, processedDataSummaryObject);
				if(clusteringResultObject ==  null) return;
				
				
				// write the clustering result into output file
				try
				{
						esriJsonObject.put("features", clusteringResultObject.getJSONArray("features"));
						
						File outputFile = new File("result.json");
						if(outputFile.exists())
						{
							outputFile.delete();
							outputFile.createNewFile();
						}
						FileWriter outputFileWriter = new FileWriter(outputFile);
						outputFileWriter.write(esriJsonObject.toString());
						outputFileWriter.close();
						
						System.out.println("The program has successfully completed.");
				} 
				catch (Exception e)
				{
						System.out.println("An error happened when writing the result into a file.");
				}
				
				
				
		}

}
