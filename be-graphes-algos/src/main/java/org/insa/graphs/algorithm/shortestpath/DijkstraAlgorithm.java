package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Arrays;
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

        public boolean isMarked() {
            return this.marque;
        }

        public void setFather(Node father){
            this.pere = father;
        }

        public int compareTo(Label other){
            return Float.compare(cout, other.getCost())
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
        HashMap<Node, Label> map= new HashMap<Node, Label>();

        for (Node node : graph.getNodes()){
            map.put(node,null);
        }
        Label label = new Label(origine, 0, null);
        map.put(origine,label);
        labels.insert(label);
        while(true){
            Label min = labels.deleteMin();
            min.setMarked();
            
            for(Arc arc : min.getSommet().getSuccessors()){
                Node node = arc.getDestination();
                label = map.get(node);
                if(!label.isMarked()){
                    float cost = min.getCost()+arc.getLength();
                    if(label.getCost()>cost){
                        label.setCost(cost);
                        labels.insert(label);
                        label.setFather(min.getSommet());
                    }
                }

            }
        }
        


        ShortestPathSolution solution = null;
        // TODO:
        return solution;
    }

}
