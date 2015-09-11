# DBSCAN4LBSN

* Author: Yingjie Hu
* Email: yjhu.geo@gmail.com


### Overall description 
This program takes location-based social network (LBSN) data (point data), and performs two operations. One is DBSCAN which detects clusters based on point density; the other is concave hull which constructs polygons from point clusters. This program can be used to extract hotspots from location-based social media data, and can also reduce the dominating effects from active users. This program can achieve the following effect:
![DBSCAN and Concave Hull](http://www.geog.ucsb.edu/~hu/clustering.png)

A research project urban Areas of Interest (AOI) developed based on this program can be accessed at: http://stko-exp.geog.ucsb.edu/urbanAOIs/


### Repository organization
The "Source" folder contains the source java files, and the "Release" folder contains the compiled program as a zip file. You can unzip the file and directly use it in your project. An example data file has also been provided.

### How to run the compiled program?
Open a cmd line in the current folder, and execute: "java -jar DBSCAN4LBSN.jar". To## increase the allocated memory size, use "java -jar -Xmx2G DBSCAN4LBSN.jar". You will need Java 1.8 to run this program.

Input of the program: Location-based social network data in CSV format. The input data must contain three fields: "recordID", "x", and "y". If the input data also contain the id of the user, then "userID" can also be included. A sample of the input data can be found in the folder "Input".

The output of the program: the output is a EsriJSON file (called "result.json" in the same folder) which can be converted into Shapefile using the tool "json to features" in ArcGIS Toolbox. 

Configuration file (config.json): This file is very important for running the program as it specifies the key parameters. Detailed explaination for each parameter can be found in the config.json file. The DBSCAN algorithm requires two parameters: eps and minPts, whose values need to be specified through some statistical analysis. Note minPts can be either an absolute value or a percentage. The concave hull algorithm requires a parameter lambda: a larger lambda results in smoother polygons while a smaller lambda results in more complex polygons. If you have questions with the parameters, please send me messages.

The "Temp" folder: this folder stores some temporary files generated in the middle of the process, such as the preprocessed file as well as the clustered result. If needed, such data can be imported into ArcMap using "add x y" to see the middle results.

### Citation
If you use this program in your research, we would really appreciate if you could cite our following paper:

Y. Hu, S. Gao, K. Janowicz, B. Yu, W. Li, S. Prasad (2015): Extracting and understanding urban areas of interest using geotagged photos, Computers, Environment and Urban Systems, doi:10.1016/j.compenvurbsys.2015.09.001 

Thanks!


