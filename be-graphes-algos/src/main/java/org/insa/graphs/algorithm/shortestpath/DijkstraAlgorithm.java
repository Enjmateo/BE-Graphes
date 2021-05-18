package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

import org.insa.graphs.algorithm.utils.BinaryHeap;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    class Label implements Comparable<Label> {
        Node sommetCourant;
        boolean marque;
        double cout;
        Node pere;

        public Label(Node sommetCourant, double cout, Node pere) {
            this.sommetCourant = sommetCourant;
            this.marque = false;
            this.cout = cout;
            this.pere = pere;
        }

        public double getCost(){
            return this.cout;
        }

        public Node getSommet() {
            return this.sommetCourant;
        }

        public void setCost(double cost){
            this.cout = cost;
        }

        public void setMarked(){
            marque = true;
        }

        public Node getFather() {
            return this.pere;
        }

        public boolean isMarked() {
            return this.marque;
        }

        public void setFather(Node father){
            this.pere = father;
        }

        public int compareTo(Label other){
            return Double.compare(cout, other.getCost());
        }
    }

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    protected Label createLabel(Node node, double cost){
        return new Label(node, cost, null); 
    }


    @Override
    protected ShortestPathSolution doRun() {

        final ShortestPathData data = getInputData();
        Node origine = data.getOrigin();
        Node destination = data.getDestination();
        Graph graph = data.getGraph();
        notifyOriginProcessed(origine);
        if(origine==destination) return new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, origine));
        BinaryHeap<Label> labels = new BinaryHeap<Label>();
        HashMap<Node, Label> map = new HashMap<Node, Label>();

        for (Node node : graph.getNodes()){
            map.put(node,createLabel(node,Float.MAX_VALUE));
        }
        Label min = map.get(origine);
        min.setCost(0);
        labels.insert(min);
        Node nodeMin;
        while(true){
            min = labels.deleteMin();
            nodeMin = min.getSommet();
            min.setMarked();
            notifyNodeMarked(nodeMin);
            if(nodeMin == destination) break;
            for(Arc arc : nodeMin.getSuccessors()){
                if(!data.isAllowed(arc)) continue;
                Node node = arc.getDestination();
                Label label = map.get(node);
                if(!label.isMarked()){
                    double cost = min.getCost()+data.getCost(arc);
                    if(label.getCost() > cost){
                        if(label.getCost()==Float.MAX_VALUE) {
                            notifyNodeReached(node);
                        }else{
                            labels.remove(label);
                        }
                        label.setCost(cost);
                        labels.insert(label);
                        label.setFather(min.getSommet());
                    }
                }
            }
            if(labels.isEmpty()) return new ShortestPathSolution(data, Status.INFEASIBLE); //si il n'y a pas de solutions
        }

        notifyDestinationReached(destination);

        ArrayList<Node> path = new ArrayList<Node>();
        path.add(destination);
        while(true){
            Node pere = map.get(path.get(path.size() - 1)).getFather();
            path.add(pere);
            if(pere==origine) break;
        }

        Collections.reverse(path);
        //for(Node node : path) System.out.println(Double.toString(map.get(node).getCost())); //test des couts croissants
        Path chemin = Path.createShortestPathFromNodes(graph, path);
        //System.out.println("Node min : "+Double.toString(map.get(nodeMin).getCost())+" Cout chemin : "+chemin.getLength());
        ShortestPathSolution solution = new ShortestPathSolution(data, Status.OPTIMAL, chemin);
        return solution;
    }

}
