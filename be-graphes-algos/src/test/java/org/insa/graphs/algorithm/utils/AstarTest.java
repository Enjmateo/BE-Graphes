package org.insa.graphs.algorithm.utils;
import org.insa.graphs.algorithm.shortestpath.ShortestPathAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;


public class AstarTest extends ShortestPathTest{
    protected ShortestPathAlgorithm createAlgorithm(ShortestPathData data){
        return new AStarAlgorithm(data);
    }
}
