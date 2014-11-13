package graph.layouts.hebLayout;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.HashSet;
import java.util.Set;

import biologicalObjects.edges.BiologicalEdgeAbstract;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import graph.GraphInstance;
import graph.jung.classes.MyGraph;


/**
 * All edge shapes must be defined so that their endpoints are at
 * (0,0) and (1,0). They will be scaled, rotated and translated into
 * position by the PluggableRenderer.
 * 
 * @author tloka
 * @param <Edge>
 */

public class HEBEdgeShape<V,E> extends EdgeShape<V,E>{
	 /**
     * An edge shape that renders as a CubicCurve between vertex
     * endpoints.  The two control points are at
     * (1/3*length, 2*controlY) and (2/3*length, controlY)
     * giving a 'spiral' effect.
     */
    public static class HEBCurve<V,E>
         extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E> {
         
        /**
         * singleton instance of the CubicCurve edge shape
         */
        private static CubicCurve2D instance = new CubicCurve2D.Float();
                 
        protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;
        
        private static Point2D centerPoint;
        
        public Point2D getCenterPoint(){
        	return centerPoint;
        }
        
        public HEBCurve(Point2D cP){
        	centerPoint = cP;
        }

		@SuppressWarnings("unchecked")
        public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction) {
            this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
            loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
       }
 
        /**
         * @return the parallelEdgeIndexFunction
         */
        public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
            return parallelEdgeIndexFunction;
        }
 
        /**
         * Get the shape for this edge, returning either the
         * shared instance or, in the case of self-loop edges, the
         * Loop shared instance.
         */
        @SuppressWarnings("unchecked")
        public Shape transform(Context<Graph<V,E>,E> context) {
            Graph<V,E> graph = context.graph;
            E e = context.element;
            if(!(e instanceof BiologicalEdgeAbstract)){
            	return new EdgeShape.CubicCurve<V, E>().transform(context);
            }
            BiologicalEdgeAbstract edge = (BiologicalEdgeAbstract) e;
           Pair<V> endpoints = graph.getEndpoints(e);
           Pair<BiologicalNodeAbstract> endpointNodes = new Pair<BiologicalNodeAbstract>((BiologicalNodeAbstract) endpoints.getFirst(), (BiologicalNodeAbstract) endpoints.getSecond());
           if(endpoints != null) {
               boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
               if (isLoop) {
                   return loop.transform(context);
               }
           }
           
           // current MyGraph
           MyGraph myGraph = GraphInstance.getMyGraph();
           
           // the locations of all nodes containing to the same parentNode as the startNode.
           Set<Point2D> group1 = new HashSet<Point2D>();
           if(endpointNodes.getFirst().getParentNode()==null){
        	   group1.add(myGraph.getVertexLocation(endpointNodes.getFirst()));
           } else {
        	   for(BiologicalNodeAbstract bna : endpointNodes.getFirst().getParentNode().getInnerNodes()){
        		   group1.add(myGraph.getVertexLocation(bna));
        	   }
           }
           
           // the locations of all nodes containing to the same parentNode as the endNode.
           Set<Point2D> group2 = new HashSet<Point2D>();
           if(endpointNodes.getSecond().getParentNode()==null){
        	   group2.add(myGraph.getVertexLocation(endpointNodes.getSecond()));
           } else {
        	   for(BiologicalNodeAbstract bna : endpointNodes.getSecond().getParentNode().getInnerNodes()){
        		   group2.add(myGraph.getVertexLocation(bna));
        	   }
           }
           
           if(group1.equals(group2) && !HEBLayoutConfig.getInstance().getShowInternalEdges()){
        	   instance.setCurve(0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
        	   return instance;
           }
           
           
           // location of the startNode.
           Point2D startPoint = new Point2D.Double(myGraph.getVertexLocation(endpointNodes.getFirst()).getX(),
        		   myGraph.getVertexLocation(endpointNodes.getFirst()).getY());
                      
           // location of the endNode.
           Point2D endPoint = new Point2D.Double(myGraph.getVertexLocation(endpointNodes.getSecond()).getX(),
        		   myGraph.getVertexLocation(endpointNodes.getSecond()).getY());
                      
           // The circle's center.
           Point2D center = getCenterPoint();
           
           double moveQuotient = group1.equals(group2) ? 
        		   HEBLayoutConfig.GROUPINTERNAL_EDGE_BENDING_FACTOR : HEBLayoutConfig.EDGE_BENDING_FACTOR;
           
           double bundlingQuotient = HEBLayoutConfig.EDGE_BUNDLING_FACTOR;
           
           double circleRadius = Point2D.distance(centerPoint.getX(), centerPoint.getY(), startPoint.getX(), startPoint.getY());
    
           double group1Angle = Circle.getAngle(center,averagePoint(group1));
           double group2Angle = Circle.getAngle(center,averagePoint(group2));
                     
           // Computation of the first control point to bundle all edges connecting the same groups.
           Point2D cPoint1 = Circle.getPointOnCircle(center, circleRadius, group1Angle);
           cPoint1 = moveInCenterDirection(cPoint1, center, moveQuotient);  
           if(!group1.equals(group2)){
        	   	cPoint1 = moveInCenterDirection(cPoint1, startPoint, -bundlingQuotient);
           }
           cPoint1 = computeControlPoint(cPoint1, center, startPoint, endPoint, edge);
           
           // Computation of the second control point to bundle all edges connecting the same groups.
           Point2D cPoint2 = Circle.getPointOnCircle(center, circleRadius, group2Angle);
           cPoint2 = moveInCenterDirection(cPoint2, center, moveQuotient); 
           if(!group1.equals(group2)){
        	   cPoint2 = moveInCenterDirection(cPoint2, endPoint, -bundlingQuotient);
           }
           cPoint2 = computeControlPoint(cPoint2, center, startPoint, endPoint,edge);
           
//           instance.setCurve(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, cPoint1.getX(), cPoint1.getY());
//           instance.setCurve(cPoint2.getX(), cPoint2.getY(), 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        	 instance.setCurve(0.0f, 0.0f, cPoint1.getX(), cPoint1.getY(), cPoint2.getX(), cPoint2.getY(), 1.0f, 0.0f);
           
           return instance;
        }
    }
    
    public static double gradient(double x1, double y1, double x2, double y2){
    	return (y2-y1)/(x2-x1);
    }
    
    public static double gradientAngle(double gradient){
    	return Math.atan(gradient);
    }
    
    private static Point2D averagePoint(Set<Point2D> nodes){
    	Point2D avPoint = new Point2D.Double(0,0);
    	for(Point2D node : nodes){
    		avPoint.setLocation(avPoint.getX()+(node.getX()/nodes.size()), avPoint.getY()+(node.getY()/nodes.size()));
    	}
    	return avPoint;
    }
    
    public static Point2D moveInCenterDirection(Point2D point, Point2D center, double distancePart){
    	return new Point2D.Double(1/distancePart*(center.getX()-point.getX())+point.getX(),
    			1/distancePart*(center.getY()-point.getY())+point.getY());
    }
    
    /**
     * Method to compute the control Points in the coordinate system with Start=(0,0) and End=(1,0).
     * @param basis The controlPoint to be computed
     * @param center The center of the circle
     * @param startPoint The location of start node
     * @param endPoint The location of end node
    
     * @return The controlPoint in transformed coordinates.
     */
    private static Point2D computeControlPoint(Point2D basis, Point2D center, Point2D startPoint, Point2D endPoint, BiologicalEdgeAbstract edge){
    	
    	double startBasisDistance = Point2D.distance(basis.getX(), basis.getY(), startPoint.getX(), startPoint.getY());
    	double startEndDistance = Point2D.distance(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    	double startEndAngle = Circle.getAngle(startPoint, endPoint);
    	double startBasisAngle = Circle.getAngle(startPoint, basis);
    	    	
   		double alpha = startBasisAngle - startEndAngle;

   		double beta = Math.PI/2-alpha;
    	
   		double c = startBasisDistance;
   		double a = startBasisDistance*Math.cos(beta);
   		double b = Math.sqrt(Math.pow(c,2)-Math.pow(a, 2));
    	
//    	if(edge.getFrom().getLabel().equals("9")){
//    		System.out.println(edge.getTo().getLabel());
//    		System.out.println(alpha);
//    		System.out.println(beta);
//    		System.out.println(c);
//    		System.out.println(a);
//    		System.out.println(b);
//    		System.out.println();
//    		
//    	}
   		Point2D longerControlPoint = new Point2D.Double(b/startEndDistance,a);
   		return longerControlPoint;
//   		return new Point2D.Double(b/startEndDistance,a);
   	} 
        
}