/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *      dclarke - Oracle Spatial Example (Bug 211007) Initial Contribution
 ******************************************************************************/
package testing.spatial.simple;

import static org.junit.Assert.assertNull;

import java.util.List;

import javax.persistence.EntityManager;

import model.spatial.simple.SimpleSpatial;
import oracle.spatial.geometry.JGeometry;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.expressions.spatial.SpatialExpressionFactory;
import org.eclipse.persistence.expressions.spatial.SpatialParameters;
import org.eclipse.persistence.expressions.spatial.SpatialParameters.Mask;
import org.eclipse.persistence.expressions.spatial.SpatialParameters.QueryType;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.junit.Test;

import testing.spatial.simple.util.SQLReader;

/**
 * Tests verifying the functionality of using a report-query as a sub-select to
 * access the second geometry. This will insert the test data point at a known
 * GID and use it in the sub-selects.
 * 
 * SQL samples from C:\oracle\db\10.2\md\demo\examples\eginsert.sql Note: Table
 * re-named from TEST81 to SIMPLE_SPATIAL
 */
public class Query_SpatialOp_ExpReport_Tests extends SpatialTestCase {

    /**
     * SDO_RELATE using a dynamic rectangular window with lower left and upper
     * right coordinates of {(1,1), (20,20)}
     */
    @Test
    public void testSDORelateRectangle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_relate(geometry, " + "mdsys.sdo_geometry(3,null,null, mdsys.sdo_elem_info_array(1,3,3),  "
                + "mdsys.sdo_ordinate_array(1,1, 20, 20)), " + "'MASK=ANYINTERACT QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 1, 1, 1, 20, 10, 20, 20, 1, 1, 1 };
        populateTestGeometry(JGeometry.createLinearPolygon(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setMask(Mask.ANYINTERACT).setQueryType(QueryType.WINDOW);
        Expression relateExp = SpatialExpressionFactory.relate(geom1, rq, params);

        raq.setSelectionCriteria(relateExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_RELATE using a with a circle of radius 10 around (0,0)
     */
    @Test
    public void testSDORelateCircle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_relate(geometry, " + "mdsys.sdo_geometry(3,null,null, " + "mdsys.sdo_elem_info_array(1,3,4), "
                + "mdsys.sdo_ordinate_array(-10,0, 0, 10, 10, 0)), " + "'MASK=ANYINTERACT QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);
        populateTestGeometry(JGeometry.createCircle(-10, 0, 0, 10, 10, 0, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setMask(Mask.ANYINTERACT).setQueryType(QueryType.WINDOW);
        Expression relateExp = SpatialExpressionFactory.relate(geom1, rq, params);

        raq.setSelectionCriteria(relateExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_RELATE using an arbitrary line string {(10,10), (20, 20), (30, 30),
     * (45,45)}
     */
    @Test
    public void testSDORelateArbitraryLine() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_relate(" + "geometry, mdsys.sdo_geometry(2,null,null, " + "mdsys.sdo_elem_info_array(1,2,1), "
                + "mdsys.sdo_ordinate_array(10,10, 20,20, 30,30, 45,45)), " + "'MASK=ANYINTERACT QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 10, 10, 20, 20, 30, 30, 45, 45 };
        populateTestGeometry(JGeometry.createLinearLineString(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setMask(Mask.ANYINTERACT).setQueryType(QueryType.WINDOW);
        Expression relateExp = SpatialExpressionFactory.relate(geom1, rq, params);

        raq.setSelectionCriteria(relateExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_Filter using a dynamic rectangular window with lower left and upper
     * right coordinates of {(1,1), (20,20)}
     */
    @Test
    public void testSDOFilterRectangle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_filter(" + "geometry, mdsys.sdo_geometry(3,null,null, " + "mdsys.sdo_elem_info_array(1,3,3), "
                + "mdsys.sdo_ordinate_array(1,1, 20, 20)), " + "'QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 1, 1, 1, 20, 10, 20, 20, 1, 1, 1 };
        populateTestGeometry(JGeometry.createLinearPolygon(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setQueryType(QueryType.WINDOW);
        Expression filterExp = SpatialExpressionFactory.filter(geom1, rq, params);

        raq.setSelectionCriteria(filterExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    @Test
    public void testSDOFilterRectangleNullParams() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_filter(" + "geometry, mdsys.sdo_geometry(3,null,null, " + "mdsys.sdo_elem_info_array(1,3,3), "
                + "mdsys.sdo_ordinate_array(1,1, 20, 20)), " + "NULL) = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 1, 1, 1, 20, 10, 20, 20, 1, 1, 1 };
        populateTestGeometry(JGeometry.createLinearPolygon(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        Expression filterExp = SpatialExpressionFactory.filter(geom1, rq, null);

        raq.setSelectionCriteria(filterExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_FILTER using a with a circle of radius 10 around (0,0)
     */
    @Test
    public void testSDOFilterCircle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_filter(" + "geometry, mdsys.sdo_geometry(3,null,null, " + "mdsys.sdo_elem_info_array(1,3,4), "
                + "mdsys.sdo_ordinate_array(-10,0, 0, 10, 10, 0)), " + "'QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        populateTestGeometry(JGeometry.createCircle(-10, 0, 0, 10, 10, 0, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setQueryType(QueryType.WINDOW);
        Expression filterExp = SpatialExpressionFactory.filter(geom1, rq, params);

        raq.setSelectionCriteria(filterExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_FILTER using an arbitrary line string {(10,10), (20, 20), (30, 30),
     * (45,45)}
     */
    @Test
    public void testSDOFilterArbitraryLine() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_filter(" + "geometry, mdsys.sdo_geometry(2,null,null, " + "mdsys.sdo_elem_info_array(1,2,1), "
                + "mdsys.sdo_ordinate_array(10,10, 20,20, 30,30, 45,45)), " + "'QUERYTYPE=WINDOW') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 10, 10, 20, 20, 30, 30, 45, 45 };
        populateTestGeometry(JGeometry.createLinearLineString(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setQueryType(QueryType.WINDOW);
        Expression filterExp = SpatialExpressionFactory.filter(geom1, rq, params);

        raq.setSelectionCriteria(filterExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_WITHIN_DISTANCE using a dynamic rectangular window with lower left
     * and upper right coordinates of {(1,1), (20,20)}
     */
    @Test
    public void testSDOWithinDistanceRectangle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_within_distance(" + "geometry, mdsys.sdo_geometry(3,null,null, mdsys.sdo_elem_info_array(1,3,3), "
                + "mdsys.sdo_ordinate_array(1,1, 20, 20)), " + "'DISTANCE=10') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 1, 1, 1, 20, 10, 20, 20, 1, 1, 1 };
        populateTestGeometry(JGeometry.createLinearPolygon(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setDistance(10d);
        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, params);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    @Test
    public void testSDOWithinDistanceRectangleUsingMaxResolution() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where mdsys.sdo_within_distance(" + "geometry, mdsys.sdo_geometry(3,null,null, mdsys.sdo_elem_info_array(1,3,3), "
                + "mdsys.sdo_ordinate_array(1,1, 20, 20)), " + "'DISTANCE=10 MAX_RESOLUTION=5') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 1, 1, 1, 20, 10, 20, 20, 1, 1, 1 };
        populateTestGeometry(JGeometry.createLinearPolygon(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setDistance(10d).setMaxResolution(5);
        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, params);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_WITHIN_DISTANCE using a with a circle of radius 10 around (0,0)
     */
    @Test
    public void testSDOWithinDistanceCircle() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where " + "mdsys.sdo_within_distance(geometry, " + "mdsys.sdo_geometry(3,null,null, " + "mdsys.sdo_elem_info_array(1,3,4), "
                + "mdsys.sdo_ordinate_array(-10,0, 0, 10, 10, 0)), " + "'DISTANCE=10') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        populateTestGeometry(JGeometry.createCircle(-10, 0, 0, 10, 10, 0, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setDistance(10d);
        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, params);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_WITHIN_DISTANCE using an arbitrary line string {(10,10), (20, 20),
     * (30, 30), (45,45)}
     */
    @Test
    public void testSDOWithinDistanceArbitraryLine() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where " + "mdsys.sdo_within_distance(geometry, " + "mdsys.sdo_geometry(2,null,null, " + "mdsys.sdo_elem_info_array(1,2,1), "
                + "mdsys.sdo_ordinate_array(10,10, 20,20, 30,30, 45,45)), " + "'DISTANCE=10') = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        double[] points = new double[] { 10, 10, 20, 20, 30, 30, 45, 45 };
        populateTestGeometry(JGeometry.createLinearLineString(points, 2, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        SpatialParameters params = new SpatialParameters().setDistance(10d).setMaxResolution(5);
        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, params);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_WITHIN_DISTANCE with NULL params matching a known circle geometry
     * (1004)
     */
    @Test
    public void testSDOWithinDistanceNullParamsMatchingCircle1004() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where " + "mdsys.sdo_within_distance(geometry, mdsys.sdo_geometry(3, " + "NULL, null, mdsys.sdo_elem_info_array(1,3,4), "
                + "mdsys.sdo_ordinate_array(1, 0, 0, 1, 0, -1)), " + "NULL) = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        populateTestGeometry(JGeometry.createCircle(1, 0, 0, 1, 0, -1, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, null);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }

    /**
     * SDO_WITHIN_DISTANCE with NULL params not matching existing
     */
    @Test
    public void testSDOWithinDistanceNullParamsNotMatching() throws Exception {
        EntityManager em = getEntityManager();

        String sql = "select GID, GEOMETRY from SIMPLE_SPATIAL where " + "mdsys.sdo_within_distance(geometry, mdsys.sdo_geometry(3, " + "NULL, null, mdsys.sdo_elem_info_array(1,3,4), "
                + "mdsys.sdo_ordinate_array(10, 0, 0, 10, 0, -10)), " + "NULL) = 'TRUE' ORDER BY GID";

        SQLReader reader = new SQLReader(em, sql);

        populateTestGeometry(JGeometry.createCircle(10, 0, 0, 10, 0, -10, 0), em);

        ReadAllQuery raq = new ReadAllQuery(SimpleSpatial.class);
        ExpressionBuilder eb = raq.getExpressionBuilder();
        Expression geom1 = eb.get("geometry");

        ExpressionBuilder eb2 = new ExpressionBuilder();
        ReportQuery rq = new ReportQuery(SimpleSpatial.class, eb2);
        rq.addAttribute("geometry");
        rq.setSelectionCriteria(eb2.get("id").equal(6666));

        Expression wdExp = SpatialExpressionFactory.withinDistance(geom1, rq, null);

        raq.setSelectionCriteria(wdExp.and(eb.get("id").notEqual(6666)));
        raq.addAscendingOrdering("id");

        List<SimpleSpatial> results = JpaHelper.createQuery(raq, em).getResultList();

        String compareResult = reader.compare(results);

        assertNull(compareResult, compareResult);
    }
}
