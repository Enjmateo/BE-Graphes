package org.insa.graphs.algorithm.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

import org.insa.graphs.algorithm.shortestpath.ShortestPathAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;

import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;


import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.BinaryPathReader;
import org.insa.graphs.model.io.GraphReader;
import org.insa.graphs.model.io.PathReader;


public abstract class ShortestPathTest {
    
    //J'ai choisit la carte de la haute-garonne car elle est assez complète pour tous les test mais pas trop pour que les tests ne prennent pas trop de temps.
    String mapFile = "D:\\home\\Downloads\\Maps\\Maps\\haute-garonne.mapgr";

    //On récupère les chemins pré-faits avec bellman ford pour gagner du temps lors des tests :
    final static String shortestAllRoadPathFile = "D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\paths\\shortestAllRoadPath.path";
    final static String shortestCarPathFile = "D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\paths\\shortestCarPath.path";
    final static String fastestAllRoadPathFile = "D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\paths\\fastestAllRoadPath.path";
    final static String fastestCarPathFile = "D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\paths\\fastestCarPath.path";
    final static String fastestPedestrianPathFile = "D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\paths\\fastestPedestrianPath.path";

    //Ici on a les numéros de 2 nodes qui ne sont pas reliables par un chemin :
    int[] impossiblePathNodes = {149133, 124982};
    
    Graph graph;

    Path shortestAllRoadPath, shortestCarPath, fastestAllRoadPath, fastestCarPath, fastestPedestrianPath;

    class Scenario {
        Graph graph;
        ArcInspector inspector;
        Node origine;
        Node destination;

        public Scenario(Graph graph, ArcInspector inspector, Node origine, Node destination) {
            this.graph = graph;
            this.origine = origine;
            this.destination = destination;
            this.inspector = inspector;
        }

    }

    protected abstract ShortestPathAlgorithm createAlgorithm(ShortestPathData data);

    @Before
    public void init() throws Exception {

        //Récupération de la carte :
        GraphReader reader = new BinaryGraphReader(new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile))));
        graph = reader.read();

        //Récupération des différents chemins :
        PathReader pathReader = new BinaryPathReader(new DataInputStream(new BufferedInputStream(new FileInputStream(shortestAllRoadPathFile))));
        shortestAllRoadPath = pathReader.readPath(graph);

        pathReader = new BinaryPathReader(new DataInputStream(new BufferedInputStream(new FileInputStream(shortestCarPathFile))));
        shortestCarPath = pathReader.readPath(graph);

        pathReader = new BinaryPathReader(new DataInputStream(new BufferedInputStream(new FileInputStream(fastestAllRoadPathFile))));
        fastestAllRoadPath = pathReader.readPath(graph);

        pathReader = new BinaryPathReader(new DataInputStream(new BufferedInputStream(new FileInputStream(fastestCarPathFile))));
        fastestCarPath = pathReader.readPath(graph);

        pathReader = new BinaryPathReader(new DataInputStream(new BufferedInputStream(new FileInputStream(fastestPedestrianPathFile))));
        fastestPedestrianPath = pathReader.readPath(graph);
    }


    public ShortestPathSolution testScenario(Scenario scenario) {
        ShortestPathAlgorithm algo = createAlgorithm(new ShortestPathData(scenario.graph, scenario.origine, scenario.destination, scenario.inspector));
        return algo.run();
    }

    @Test
    public void testShortestAllRoadPath(){
        Path correctPath = shortestAllRoadPath;

        //Sélection distance et tous les arcs : 
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);

        Scenario scenario = new Scenario(this.graph, inspector, correctPath.getOrigin(), correctPath.getDestination());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==correctPath.size());
        assertTrue(path.getLength()==correctPath.getLength());
    }

    @Test
    public void testShortestCarPath(){
        Path correctPath = shortestCarPath;

        //Sélection distance et uniquement arcs pour voitures :
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(1);

        Scenario scenario = new Scenario(this.graph, inspector, correctPath.getOrigin(), correctPath.getDestination());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==correctPath.size());
        assertTrue(path.getLength()==correctPath.getLength());
    }

    @Test
    public void testFastestAllRoadPath(){
        Path correctPath = fastestAllRoadPath;

        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(2);

        Scenario scenario = new Scenario(this.graph, inspector, correctPath.getOrigin(), correctPath.getDestination());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==correctPath.size());
        assertTrue(path.getMinimumTravelTime()==correctPath.getMinimumTravelTime());
    }

    @Test
    public void testFastestCarPath(){
        Path correctPath = fastestCarPath;

        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(3);

        Scenario scenario = new Scenario(this.graph, inspector, correctPath.getOrigin(), correctPath.getDestination());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==correctPath.size());
        assertTrue(path.getMinimumTravelTime()==correctPath.getMinimumTravelTime());
    }

    @Test
    public void testFastestPedestrianPath(){
        Path correctPath = fastestPedestrianPath;

        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(4);

        Scenario scenario = new Scenario(this.graph, inspector, correctPath.getOrigin(), correctPath.getDestination());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==correctPath.size());
        assertTrue(path.getMinimumTravelTime()==correctPath.getMinimumTravelTime());
    }

    @Test
    public void testNullLength(){
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);

        Scenario scenario = new Scenario(this.graph, inspector, shortestAllRoadPath.getOrigin(), shortestAllRoadPath.getOrigin());
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(solution.isFeasible());

        Path path = solution.getPath();

        assertTrue(path.isValid());
        assertTrue(path.size()==1);
        assertTrue(path.getLength()==0.0);
    }

    @Test
    public void testInfeasablePath(){
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);

        Scenario scenario = new Scenario(this.graph, inspector, graph.get(impossiblePathNodes[0]), graph.get(impossiblePathNodes[1]));
        ShortestPathSolution solution = testScenario(scenario);

        assertTrue(!solution.isFeasible());
    }

}