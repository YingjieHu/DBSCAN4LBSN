package edu.ucsb.stko;

import org.json.JSONArray;
import org.json.JSONObject;

class EsriJSONInitialiser
{
		
		public JSONObject initializeEsriJsonObject(JSONObject parameterObject)
		{
				System.out.println("Initialize Esri JSON object...");
				try
				{
						 JSONObject jsonObject = new JSONObject();
						 jsonObject.put("geometryType","esriGeometryPolygon");
						 
						 // add spatial reference
						 String spatialReferenceString = parameterObject.getString("spatialReference");
						 if(spatialReferenceString.equals(""))  // this is WGS 84
						 {
								 JSONObject spatialRefObject = new JSONObject();
								 spatialRefObject.put("wkid", 4326);
								 spatialRefObject.put("latestWkid", 4326);
								 jsonObject.put("spatialReference", spatialRefObject);  
						 }
						 else  // other spatial reference
						 {
								 JSONObject spatialRefObject = new JSONObject(spatialReferenceString);
								 jsonObject.put("spatialReference", spatialRefObject);  
						}
						
						 // add attribute fields
						 JSONArray fieldsArray = new JSONArray();
						 fieldsArray.put(new JSONObject("{\"name\":\"Cluster\",\"type\":\"esriFieldTypeString\",\"length\":50}"));
						 
						 if(parameterObject.getInt("userIDIndex")!= -1)
						 {
								 fieldsArray.put(new JSONObject("{\"name\" : \"UserCount\",\"type\" : \"esriFieldTypeInteger\"}"));
								 fieldsArray.put(new JSONObject("{\"name\" : \"UserPercent\",\"type\" : \"esriFieldTypeDouble\"}"));
						 }
						
						 fieldsArray.put(new JSONObject("{\"name\" : \"PointCount\",\"type\" : \"esriFieldTypeInteger\"}"));
						 fieldsArray.put(new JSONObject("{\"name\" : \"PointPercent\",\"type\" : \"esriFieldTypeDouble\"}"));
						 
						 jsonObject.put("fields", fieldsArray);
						 
						 System.out.println(jsonObject.toString());
						 System.out.println("EsriJSON object has been initialized...");
						 System.out.println("---------------------------------------------------");
						 return jsonObject;
				} 
				catch (Exception e)
				{
						System.out.println("An error happened in the Esri JSON initialization process; The program has been canceled");
						return null;
				}
		}

}
