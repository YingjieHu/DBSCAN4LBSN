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
package com.vividsolutions.jts.geom.impl;

import com.vividsolutions.jts.geom.*;

import java.lang.ref.SoftReference;

/**
 * A {@link CoordinateSequence} implementation based on a packed arrays.
 * In this implementation, {@link Coordinate}s returned by #toArray and #get are copies
 * of the internal values.
 * To change the actual values, use the provided setters.
 * <p>
 * For efficiency, created Coordinate arrays
 * are cached using a soft reference.
 * The cache is cleared each time the coordinate sequence contents are
 * modified through a setter method.
 *
 * @version 1.7
 */
public abstract class PackedCoordinateSequence
    implements CoordinateSequence
{
  /**
   * The dimensions of the coordinates hold in the packed array
   */
  protected int dimension;

  /**
   * A soft reference to the Coordinate[] representation of this sequence.
   * Makes repeated coordinate array accesses more efficient.
   */
  protected SoftReference coordRef;

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension() {
    return this.dimension;
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinate(int i) {
    Coordinate[] coords = getCachedCoords();
    if(coords != null)
      return coords[i];
    else
      return getCoordinateInternal(i);
  }
  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinateCopy(int i) {
    return getCoordinateInternal(i);
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public void getCoordinate(int i, Coordinate coord) {
    coord.x = getOrdinate(i, 0);
    coord.y = getOrdinate(i, 1);
    if (dimension > 2) coord.z = getOrdinate(i, 2);
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#toCoordinateArray()
   */
  public Coordinate[] toCoordinateArray() {
    Coordinate[] coords = getCachedCoords();
// testing - never cache
    if (coords != null)
      return coords;

    coords = new Coordinate[size()];
    for (int i = 0; i < coords.length; i++) {
      coords[i] = getCoordinateInternal(i);
    }
    coordRef = new SoftReference(coords);

    return coords;
  }

  /**
   * @return
   */
  private Coordinate[] getCachedCoords() {
    if (coordRef != null) {
      Coordinate[] coords = (Coordinate[]) coordRef.get();
      if (coords != null) {
        return coords;
      } else {
        // System.out.print("-");
        coordRef = null;
        return null;
      }
    } else {
      // System.out.print("-");
      return null;
    }

  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
   */
  public double getX(int index) {
    return getOrdinate(index, 0);
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
   */
  public double getY(int index) {
    return getOrdinate(index, 1);
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
   */
  public abstract double getOrdinate(int index, int ordinateIndex);

  /**
   * Sets the first ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index
   * @param value  the new ordinate value
   */
  public void setX(int index, double value) {
    coordRef = null;
    setOrdinate(index, 0, value);
  }

  /**
   * Sets the second ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index
   * @param value  the new ordinate value
   */
  public void setY(int index, double value) {
    coordRef = null;
    setOrdinate(index, 1, value);
  }

  /**
   * Returns a Coordinate representation of the specified coordinate, by always
   * building a new Coordinate object
   *
   * @param index
   * @return
   */
  protected abstract Coordinate getCoordinateInternal(int index);

  /**
   * @see java.lang.Object#clone()
   */
  public abstract Object clone();

  /**
   * Sets the ordinate of a coordinate in this sequence.
   * <br>
   * Warning: for performance reasons the ordinate index is not checked
   * - if it is over dimensions you may not get an exception but a meaningless value.
   *
   * @param index
   *          the coordinate index
   * @param ordinate
   *          the ordinate index in the coordinate, 0 based, smaller than the
   *          number of dimensions
   * @param value
   *          the new ordinate value
   */
  public abstract void setOrdinate(int index, int ordinate, double value);

  /**
   * Packed coordinate sequence implementation based on doubles
   */
  public static class Double extends PackedCoordinateSequence {

    /**
     * The packed coordinate array
     */
    double[] coords;

    /**
     * Builds a new packed coordinate sequence
     *
     * @param coords
     * @param dimensions
     */
    public Double(double[] coords, int dimensions) {
      if (dimensions < 2) {
        throw new IllegalArgumentException("Must have at least 2 dimensions");
      }
      if (coords.length % dimensions != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
            + "an integral number of coordinates");
      }
      this.dimension = dimensions;
      this.coords = coords;
    }

    /**
     * Builds a new packed coordinate sequence out of a float coordinate array
     *
     * @param coordinates
     */
    public Double(float[] coordinates, int dimensions) {
      this.coords = new double[coordinates.length];
      this.dimension = dimensions;
      for (int i = 0; i < coordinates.length; i++) {
        this.coords[i] = coordinates[i];
      }
    }

    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public Double(Coordinate[] coordinates, int dimension) {
      if (coordinates == null)
        coordinates = new Coordinate[0];
      this.dimension = dimension;

      coords = new double[coordinates.length * this.dimension];
      for (int i = 0; i < coordinates.length; i++) {
        coords[i * this.dimension] = coordinates[i].x;
        if (this.dimension >= 2)
          coords[i * this.dimension + 1] = coordinates[i].y;
        if (this.dimension >= 3)
          coords[i * this.dimension + 2] = coordinates[i].z;
      }
    }
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public Double(Coordinate[] coordinates) {
      this(coordinates, 3);
    }

    /**
     * Builds a new empty packed coordinate sequence of a given size and dimension
     *
     * @param coordinates
     */
    public Double(int size, int dimension) {
      this.dimension = dimension;
      coords = new double[size * this.dimension];
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      double z = dimension == 2 ? Coordinate.NULL_ORDINATE : coords[i * dimension + 2];
      return new Coordinate(x, y, z);
    }

    /**
     * Gets the underlying array containing the coordinate values.
     * 
     * @return the array of coordinate values
     */
    public double[] getRawCoordinates()
    {
      return coords;
    }
    
    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
      double[] clone = new double[coords.length];
      System.arraycopy(coords, 0, clone, 0, coords.length);
      return new Double(clone, dimension);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
     *      Beware, for performace reasons the ordinate index is not checked, if
     *      it's over dimensions you may not get an exception but a meaningless
     *      value.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see com.vividsolutions.jts.geom.PackedCoordinateSequence#setOrdinate(int,
     *      int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = value;
    }

    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }
  }

  /**
   * Packed coordinate sequence implementation based on floats
   */
  public static class Float extends PackedCoordinateSequence {

    /**
     * The packed coordinate array
     */
    float[] coords;

    /**
     * Constructs a packed coordinate sequence from an array of <code>float<code>s
     *
     * @param coords
     * @param dimensions
     */
    public Float(float[] coords, int dimensions) {
      if (dimensions < 2) {
        throw new IllegalArgumentException("Must have at least 2 dimensions");
      }
      if (coords.length % dimensions != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
            + "an integral number of coordinates");
      }
      this.dimension = dimensions;
      this.coords = coords;
    }

    /**
     * Constructs a packed coordinate sequence from an array of <code>double<code>s
     *
     * @param coordinates
     * @param dimension
     */
    public Float(double[] coordinates, int dimensions) {
      this.coords = new float[coordinates.length];
      this.dimension = dimensions;
      for (int i = 0; i < coordinates.length; i++) {
        this.coords[i] = (float) coordinates[i];
      }
    }

    /**
     * Constructs a packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public Float(Coordinate[] coordinates, int dimension) {
      if (coordinates == null)
        coordinates = new Coordinate[0];
      this.dimension = dimension;

      coords = new float[coordinates.length * this.dimension];
      for (int i = 0; i < coordinates.length; i++) {
        coords[i * this.dimension] = (float) coordinates[i].x;
        if (this.dimension >= 2)
          coords[i * this.dimension + 1] = (float) coordinates[i].y;
        if (this.dimension >= 3)
          coords[i * this.dimension + 2] = (float) coordinates[i].z;
      }
    }

    /**
     * Constructs an empty packed coordinate sequence of a given size and dimension
     *
     * @param coordinates
     */
    public Float(int size, int dimension) {
      this.dimension = dimension;
      coords = new float[size * this.dimension];
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      double z = dimension == 2 ? Coordinate.NULL_ORDINATE : coords[i * dimension + 2];
      return new Coordinate(x, y, z);
    }

    /**
     * Gets the underlying array containing the coordinate values.
     * 
     * @return the array of coordinate values
     */
    public float[] getRawCoordinates()
    {
      return coords;
    }
    
    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
      float[] clone = new float[coords.length];
      System.arraycopy(coords, 0, clone, 0, coords.length);
      return new Float(clone, dimension);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
     *      Beware, for performace reasons the ordinate index is not checked, if
     *      it's over dimensions you may not get an exception but a meaningless
     *      value.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see com.vividsolutions.jts.geom.PackedCoordinateSequence#setOrdinate(int,
     *      int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = (float) value;
    }

    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }

  }

}