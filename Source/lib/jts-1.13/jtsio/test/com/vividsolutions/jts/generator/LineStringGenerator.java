/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.generator;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * 
 * This class is used to create a line string within the specified bounding box.
 * 
 * Sucessive calls to create may or may not return the same geometry topology.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class LineStringGenerator extends GeometryGenerator {
	protected int numberPoints = 2;
	protected int generationAlgorithm = 0;
	
	/**
	 * Create the points in a vertical line
	 */
	public static final int VERT = 1;
	
	/**
	 * Create the points in a horizontal line
	 */
	public static final int HORZ = 2;
	
	/**
	 * Create the points in an approximation of an open circle (one edge will not be included).
	 * 
	 * Note: this requires the number of points to be greater than 2.
	 * 
	 * @see #getNumberPoints()
	 * @see #setNumberPoints(int)
	 */
	public static final int ARC = 0;
	
	/**
	 * Number of interations attempting to create a valid line string
	 */
	private static final int RUNS = 5;

	/**
	 * As the user increases the number of points, the probability of creating a random valid linestring decreases. 
	 * Please take not of this when selecting the generation style, and the number of points. 
	 * 
	 * May return null if a geometry could not be created.
	 * 
	 * @see #getNumberPoints()
	 * @see #setNumberPoints(int)
	 * @see #getGenerationAlgorithm()
	 * @see #setGenerationAlgorithm(int)
	 * 
	 * @see #VERT
	 * @see #HORZ
	 * @see #ARC
	 * 
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#create()
	 * 
	 * @throws IllegalStateException When the alg is not valid or the number of points is invalid
	 * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
	 */
	public Geometry create() {

		if(geometryFactory == null){
			throw new NullPointerException("GeometryFactory is not declared");
		}
		if(boundingBox == null || boundingBox.isNull()){
			throw new NullPointerException("Bounding Box is not declared");
		}
		if(numberPoints<2){
			throw new IllegalStateException("Too few points");
		}
		
		Coordinate[] coords = new Coordinate[numberPoints];

		double x = boundingBox.getMinX(); // base x
		double dx = boundingBox.getMaxX()-x;
		
		double y = boundingBox.getMinY(); // base y
		double dy = boundingBox.getMaxY()-y;
		
		
		for(int i=0;i<RUNS;i++){
			switch(getGenerationAlgorithm()){
			case VERT:
				fillVert(x,dx,y,dy,coords,geometryFactory);
				break;
			case HORZ:
				fillHorz(x,dx,y,dy,coords,geometryFactory);
				break;
			case ARC:
				fillArc(x,dx,y,dy,coords,geometryFactory);
				break;
			default:
				throw new IllegalStateException("Invalid Alg. Specified");
			}
			
			LineString ls = geometryFactory.createLineString(coords);
			IsValidOp valid = new IsValidOp(ls);
			if(valid.isValid()){
				return ls;
			}
		}
		return null;
	}
	
	private static void fillVert(double x, double dx, double y, double dy, Coordinate[] coords, GeometryFactory gf){
		double fx = x+Math.random()*dx;
		double ry = dy; // remainder of y distance
		coords[0] = new Coordinate(fx,y);
		gf.getPrecisionModel().makePrecise(coords[0]);
		for(int i=1;i<coords.length-1;i++){
			ry -= Math.random()*ry;
			coords[i] = new Coordinate(fx,y+dy-ry);
			gf.getPrecisionModel().makePrecise(coords[i]);
		}
		coords[coords.length-1] = new Coordinate(fx,y+dy);
		gf.getPrecisionModel().makePrecise(coords[coords.length-1]);
	}
	
	private static void fillHorz(double x, double dx, double y, double dy, Coordinate[] coords, GeometryFactory gf){
		double fy = y+Math.random()*dy;
		double rx = dx; // remainder of x distance
		coords[0] = new Coordinate(x,fy);
		gf.getPrecisionModel().makePrecise(coords[0]);
		for(int i=1;i<coords.length-1;i++){
			rx -= Math.random()*rx;
			coords[i] = new Coordinate(x+dx-rx,fy);
			gf.getPrecisionModel().makePrecise(coords[i]);
		}
		coords[coords.length-1] = new Coordinate(x+dx,fy);
		gf.getPrecisionModel().makePrecise(coords[coords.length-1]);
	}
	
	private static void fillArc(double x, double dx, double y, double dy, Coordinate[] coords, GeometryFactory gf){
		if(coords.length == 2)
			throw new IllegalStateException("Too few points for Arc");
		
		double theta = 360/coords.length;
		double start = theta/2;
		
		double radius = dx<dy?dx/3:dy/3;
		
		double cx = x+(dx/2); // center
		double cy = y+(dy/2); // center
		
		for(int i=0;i<coords.length;i++){
			double angle = Math.toRadians(start+theta*i);
			
			double fx = Math.sin(angle)*radius; // may be neg.
			double fy = Math.cos(angle)*radius; // may be neg.
			
			coords[i] = new Coordinate(cx+fx,cy+fy);
			gf.getPrecisionModel().makePrecise(coords[i]);
		}
	}

	/**
	 * @return Returns the numberPoints.
	 */
	public int getNumberPoints() {
		return numberPoints;
	}

	/**
	 * @param numberPoints The numberPoints to set.
	 */
	public void setNumberPoints(int numberPoints) {
		this.numberPoints = numberPoints;
	}

	/**
	 * @return Returns the generationAlgorithm.
	 */
	public int getGenerationAlgorithm() {
		return generationAlgorithm;
	}

	/**
	 * @param generationAlgorithm The generationAlgorithm to set.
	 */
	public void setGenerationAlgorithm(int generationAlgorithm) {
		this.generationAlgorithm = generationAlgorithm;
	}
	
}
