package edu.ucsb.stko;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensphere.geometry.algorithm.ConcaveHull;

import au.com.bytecode.opencsv.CSVReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

class ClusterAndShapeGenerator
{
		public JSONObject generateClusterAndShapes(JSONObject parameterObject, JSONObject dataSummaryObject, JSONObject processedDataSummaryObject)
		{
				System.out.println("Clustering and generating shapes...");
				try
				{
						// get the input parameters
						int userIDIndex = parameterObject.getInt("userIDIndex");
						
						// get info about the data
						String tempFileName = processedDataSummaryObject.getString("file");
						long userCount = 0;
						if(userIDIndex != -1) userCount = processedDataSummaryObject.getInt("userCount");
						long recordCount = processedDataSummaryObject.getInt("recordCount");
										
						File inputFile = new File(tempFileName);
						FileReader inputFileReader = new FileReader(inputFile);
						CSVReader inputCsvReader = new CSVReader(inputFileReader);
						
						String[] thisInputLine = null;
						List<DoublePoint> dataPointList = new ArrayList<DoublePoint>(1500);
						while((thisInputLine = inputCsvReader.readNext())!= null)
						{
    							double[] thisCoordDouble = new double[2];
    							if(userIDIndex == -1)
    							{
    									thisCoordDouble[0] = Double.parseDouble(thisInputLine[2]);
    									thisCoordDouble[1] = Double.parseDouble(thisInputLine[1]);
    							}
    							else 
    							{
    									thisCoordDouble[0] = Double.parseDouble(thisInputLine[3]);
    									thisCoordDouble[1] = Double.parseDouble(thisInputLine[2]);
    						    }
    							DoublePoint thisCoordPoint = new DoublePoint(thisCoordDouble);
    							dataPointList.add(thisCoordPoint);
						}
						inputCsvReader.close();
						
						
						// derive the input parameters for DBSCAN
						boolean isMinPtsPercent = parameterObject.getBoolean("minPtsPercentage");
						int minPts = 0;
						if(!isMinPtsPercent)
						{
								minPts = (int)parameterObject.getDouble("minPts");
						}
						else
						{
							if(userIDIndex == -1)
							{
									minPts = (int)(parameterObject.getDouble("minPts") * recordCount);
							}
							else
							{
									minPts = (int)(parameterObject.getDouble("minPts") * userCount);
							}
						}
						
						if(minPts<3) minPts = 3;
						double clusterSizeLimit = minPts;
						
						double distanceThreshold = parameterObject.getDouble("eps");
						DBSCANClusterer<DoublePoint> dbscanClusterer = new DBSCANClusterer<DoublePoint>(distanceThreshold,minPts);
						List<Cluster<DoublePoint>> clusterResult = dbscanClusterer.cluster(dataPointList);
						
						// write the clustered result into a file
						File clusteredFileResult = new File(tempFileName.replaceAll("processed.csv", "")+"clustered.csv");
						if(clusteredFileResult.exists())
						{
							clusteredFileResult.delete();
							clusteredFileResult.createNewFile();
						}
						FileWriter clusterFileWriter = new FileWriter(clusteredFileResult, true);
						String newLineSymbol = System.getProperty("line.separator");

						// go through the clusters
						int clusterIndex = 1;
						for(int i=0;i<clusterResult.size();i++)
						{
    							List<DoublePoint> pointsCluster = clusterResult.get(i).getPoints();
    							int thisClusterSize = pointsCluster.size();			
    							
    							if(thisClusterSize >= clusterSizeLimit)
    							{
    								for(int j=0;j<pointsCluster.size();j++)
    								{
    									double[] pointCoords = pointsCluster.get(j).getPoint();			
    									clusterFileWriter.append(pointCoords[0]+","+pointCoords[1]+","+"cluster_"+clusterIndex+","+newLineSymbol);
    								}
    								clusterIndex++;
    							}
						}
						clusterFileWriter.close();
						
						// begin to construct shapes from clusters
						JSONObject clusterResultObject = new JSONObject();
						JSONArray featuresArray = new JSONArray();				
						
						clusterIndex = 1;
						for(int i=0;i<clusterResult.size();i++)
						{
							List<DoublePoint> pointsCluster = clusterResult.get(i).getPoints();
							int thisClusterSize = pointsCluster.size();
							
							if(thisClusterSize >= clusterSizeLimit)
							{
								JSONObject thisFeatureObject = createConcaveHull(pointsCluster, clusterIndex, parameterObject, dataSummaryObject);			
								if(thisFeatureObject == null)
								{
									continue;
								}
								featuresArray.put(thisFeatureObject);
								
								clusterIndex++;
							}
						}
						clusterResultObject.put("features", featuresArray);
						
						System.out.println(clusterResultObject.toString());
						System.out.println("Clustering and shape construction have finished...");
						System.out.println("---------------------------------------------------");
						return clusterResultObject;
				} 
				catch (Exception e)
				{
						System.out.println("An error happened in the clustering and shape generating process; The program has been canceled");
						return null;
				}
		}
		
		
		JSONObject createConcaveHull(List<DoublePoint> pointsInCluster, int clusterId, JSONObject parameterObject, JSONObject dataSummaryObject)
		{
    			try 
    			{		
    				GeometryFactory gf = new GeometryFactory();
    				int numberOfPointsInCluster = pointsInCluster.size();
    				
    				 Coordinate[] vertices = new Coordinate[numberOfPointsInCluster];
    				 Point[] pointArray = new Point[numberOfPointsInCluster];
    				 
    				 for(int i=0;i<numberOfPointsInCluster;i++)
    				 {
    					 double[] thisCoords = pointsInCluster.get(i).getPoint();
    					 vertices[i] = new Coordinate(thisCoords[0],thisCoords[1]);
    					 pointArray[i] = gf.createPoint(vertices[i]);
    				 }
    				 
    				GeometryCollection allPointCollection = gf.createGeometryCollection(pointArray); 
    				Geometry pointConvexHull = allPointCollection.convexHull();
    				
    				// calculate the longest edge of the convex hull
    		        Coordinate[] convexCoordinatesArray = pointConvexHull.getCoordinates();
    		        double longestEdgeOfConvexhull = -1.0; 
    		        for(int i=0;i<(convexCoordinatesArray.length-1);i++)
    		        {
    		        		Coordinate coord1 = convexCoordinatesArray[i];
    		        		Coordinate coord2 = convexCoordinatesArray[i+1];
    		        		
    		        		double distance = Math.sqrt((coord1.x - coord2.x)*(coord1.x - coord2.x) + (coord1.y - coord2.y)*(coord1.y - coord2.y));
    		        		if(distance > longestEdgeOfConvexhull)
    		        				longestEdgeOfConvexhull = distance;
    		        }
    		        // finish the longest edge
    				
    		         double lambda = parameterObject.getDouble("lambda");
    				 double edgeThreshold =  longestEdgeOfConvexhull * 0.01 * lambda;
    				 
    				 ConcaveHull concaveHull = new ConcaveHull(allPointCollection,edgeThreshold);
    				 Geometry concaveHullResultGeometry = concaveHull.getConcaveHull();
    				 
    				 Hashtable<String, Long> recordAndUserTable = countRecordAndUserInAOI(concaveHullResultGeometry, parameterObject);
    				 
    				 
    				 long recordCountInAOI = recordAndUserTable.get("recordCount");
    				 long totalRecordCount = dataSummaryObject.getLong("recordCount");
    				 double recordPercentageValue = (recordCountInAOI * 1.0)/ (totalRecordCount * 1.0);
    				 
    				 double minPts = parameterObject.getDouble("minPts");	 
    				 int userIDIndex = parameterObject.getInt("userIDIndex");
    				 
    				 long userCountInAOI = 0;    				 
    				 long totalUserCount = 0;
    				 double userPercentageValue = 0;
    				 if(userIDIndex != -1)
    				 {
    						 userCountInAOI = recordAndUserTable.get("userCount");
    						 totalUserCount = dataSummaryObject.getLong("userCount");
    						 userPercentageValue = (userCountInAOI *1.0)/(totalUserCount * 1.0);
    				 }

    				 
    				 if(parameterObject.getBoolean("minPtsPercentage"))
    				 {
    						 if(userIDIndex != -1)
    						 {
    								 if(userPercentageValue < minPts) return null;
    						 }
    						 else 
    						 {
								     if(recordPercentageValue < minPts) return null;
						     }
    				 }
    				 else
    				 {
    						 if(userIDIndex != -1)
    						 {
    								 if(userCountInAOI < minPts) return null;
    						 }
    						 else 
    						 {
								     if(recordCountInAOI < minPts) return null;
						     }
				     }			
    				 
    				 JSONObject thisFeatureObject = new JSONObject();
    				 if(userIDIndex != -1)
    				 {
    						 thisFeatureObject.put("attributes", new JSONObject("{\"Cluster\" : \"cluster_"+clusterId+"\", \"UserCount\": \""+userCountInAOI+"\",\"UserPercent\": \""+userPercentageValue+"\","
												+ "\"PointCount\": \""+recordCountInAOI+"\",\"PointPercent\": \""+recordPercentageValue+"\"}")); 
    				 }
    				 else 
    				 {
    						 thisFeatureObject.put("attributes", new JSONObject("{\"Cluster\" : \"cluster_"+clusterId+"\", \"PointCount\": \""+recordCountInAOI+"\",\"PointPercent\": \""+recordPercentageValue+"\"}")); 
    				 }
    				
    				 JSONObject geometryObject = new JSONObject();
    				 JSONArray ringsJsonArray = new JSONArray();
    				 JSONArray coordsArray = new JSONArray();
    
    				 Coordinate[] concaveCoords = concaveHullResultGeometry.getCoordinates();
    				 
    				 for(int j=0;j<concaveCoords.length;j++)
    				 {
    					 JSONArray thisCoordArray = new JSONArray();
    					 thisCoordArray.put(concaveCoords[j].x);
    					 thisCoordArray.put(concaveCoords[j].y);
    					 coordsArray.put(thisCoordArray);
    				 }   				
    				 ringsJsonArray.put(coordsArray);
    				 geometryObject.put("rings", ringsJsonArray);
    				 thisFeatureObject.put("geometry", geometryObject);
    				 
    				 return thisFeatureObject;
    
    			} 
    			catch (Exception e) 
    			{
    				e.printStackTrace();
    			}
    			return null;
		}
		
		
		
		Hashtable<String, Long> countRecordAndUserInAOI(Geometry aoiGeometry, JSONObject parameterObject)
		{
    			try 
    			{
    					// read parameters 
    					String inputDataFilePath = parameterObject.getString("dataPath");
    					int userIDIndex = parameterObject.getInt("userIDIndex");
    				    int recordIDIndex = parameterObject.getInt("recordIDIndex"); 				    
    				    int lngIndex =  parameterObject.getInt("lngIndex"); 
    				    int latIndex = parameterObject.getInt("latIndex");
    					
    				    // read the input file
    					File inputFile = new File(inputDataFilePath);
    					FileReader inputFileReader = new FileReader(inputFile);
    				    CSVReader inputFileCsvReader = new CSVReader(inputFileReader);
    				
    				    Hashtable<String, Integer> userTable = new Hashtable<>();
    				    Hashtable<String, Integer> recordTable = new Hashtable<>();
    				
    				    GeometryFactory gf = new GeometryFactory();
    				
    				    String[] thisInputLine = inputFileCsvReader.readNext();
    				    while((thisInputLine = inputFileCsvReader.readNext()) != null)
    				    {
            					String recordId = null;
            					String ownerString = null;
            					double latString = 0;
            					double lngString = 0;
    					
            					try 
            					{
            						recordId = thisInputLine[recordIDIndex];
            						if(userIDIndex != -1) ownerString = thisInputLine[userIDIndex];
            						
            						latString = Double.parseDouble(thisInputLine[latIndex]);
            						lngString = Double.parseDouble(thisInputLine[lngIndex]);
            					} 
            					catch (Exception e) 
            					{
            						continue;
            					}
    					
            					Coordinate vertice = new Coordinate(lngString,latString);
            					Point point = gf.createPoint(vertice);
    					
            					if(aoiGeometry.covers(point))
            					{
            						if(userIDIndex != -1) userTable.put(ownerString, 1);
            						recordTable.put(recordId,1);
            					}
    					
    				    }
    				    inputFileCsvReader.close();
    				
    				    Hashtable<String, Long> resultHashtable = new Hashtable<>();
    				    resultHashtable.put("recordCount", new Long(recordTable.size()));
    				    resultHashtable.put("userCount", new Long(userTable.size()));
    				
    				    return resultHashtable;
    			} 
    			catch (Exception e) 
    			{
    				e.printStackTrace();
    			}
    			return null;
		}

}
