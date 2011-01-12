/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.geo;

import org.gbif.provider.model.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * TODO: Documentation.
 * 
 */
public class TransformationUtils {
  /**
   * TODO: Documentation.
   * 
   */
  public class Wgs84Transformer {
    private final MathTransform transform;

    public Wgs84Transformer(MathTransform transform) {
      this.transform = transform;
    }

    public Point transformPoint(Point point) throws TransformException {
      DirectPosition dpoint = new GeneralDirectPosition(point.getX(),
          point.getY());
      dpoint = transform.transform(dpoint, dpoint);
      point.setX(dpoint.getOrdinate(0));
      point.setY(dpoint.getOrdinate(1));
      return point;
    }
  }

  protected final Log log = LogFactory.getLog(TransformationUtils.class);
  private final CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
  private final CoordinateOperationFactory coFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(null);

  private final CoordinateReferenceSystem outCRS = DefaultGeographicCRS.WGS84;

  /**
   * Returns a specific Point Tansformer for a certain datum WKT that can
   * transform points
   * 
   * @param geodatum
   * @throws FactoryException
   */
  public Wgs84Transformer getWgs84Transformer(String geodatumWKT)
      throws FactoryException {
    if (geodatumWKT == null) {
      return null;
    }
    // create Wgs84Transformer
    CoordinateReferenceSystem inCRS = crsFactory.createFromWKT(geodatumWKT);
    CoordinateOperation co = coFactory.createOperation(inCRS, outCRS);
    return new Wgs84Transformer(co.getMathTransform());
  }

  /**
   * Transforms a point into WGS84 coordinates using geotools. If many points
   * with the same datum are transformed create a Wgs84Transformer.
   * 
   * @param latitude
   * @param longitude
   * @param geodatum
   * @throws FactoryException, TransformException
   */
  public void transformIntoWGS84(Point point, String geodatumWKT)
      throws FactoryException, TransformException {
    if (geodatumWKT == null) {
      return;
    }
    Wgs84Transformer t = getWgs84Transformer(geodatumWKT);
    t.transformPoint(point);
  }
}
