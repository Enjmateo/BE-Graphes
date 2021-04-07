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
        float cout;
        Node pere;

        public Label(Node sommetCourant, float cout, Node pere) {
            this.sommetCourant = sommetCourant;
            this.marque = false;
            this.cout = cout;
            this.pere = pere;
        }

        public float getCost(){
            return this.cout;
        }

        public Node getSommet() {
            return this.sommetCourant;
        }

        public void setCost(float cost){
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
            return Float.compare(cout, other.getCost());
        }
    }

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }


    @Override
    protected ShortestPathSolution doRun() {

        final ShortestPathData data = getInputData();
        Node origine = data.getOrigin();
        Node destination = data.getDestination();
        Graph graph = data.getGraph();

        if(origine==destination){
            // TODO
        }
        BinaryHeap<Label> labels = new BinaryHeap<Label>();
        HashMap<Node, Label> map = new HashMap<Node, Label>();

        for (Node node : graph.getNodes()){
            map.put(node,new Label(node, Float.MAX_VALUE, null));
        }
        Label min = map.get(origine);
        min.setCost(0);
        labels.insert(min);
        while(true){
            min = labels.deleteMin();
            Node nodeMin = min.getSommet();
            min.setMarked();
            if(nodeMin == destination) break;
            for(Arc arc : nodeMin.getSuccessors()){
                Node node = arc.getDestination();
                Label label = map.get(node);
                if(!label.isMarked()){
                    float cost = min.getCost()+arc.getLength();
                    if(label.getCost()>cost){
                        label.setCost(cost);
                        labels.insert(label);
                        label.setFather(min.getSommet());
                    }
                }

            }
            if(labels.isEmpty()) return new ShortestPathSolution(data, Status.INFEASIBLE); //si il n'y a pas de solutions
        }

        ArrayList<Node> path = new ArrayList<Node>();
        path.add(destination);
        while(true){
            Node pere = map.get(path.get(path.size() - 1)).getFather();
            path.add(pere);
            if(pere==origine) break;
        }

        Collections.reverse(path);
        Path chemin = Path.createShortestPathFromNodes(graph, path);

        ShortestPathSolution solution = new ShortestPathSolution(data, Status.OPTIMAL, chemin);
        // TODO:
        return solution;
    }

}
