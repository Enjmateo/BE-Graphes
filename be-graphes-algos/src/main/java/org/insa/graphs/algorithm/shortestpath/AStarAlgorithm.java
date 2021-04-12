package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.algorithm.AbstractInputData.Mode;

import org.insa.graphs.model.Node;

public class AStarAlgorithm extends DijkstraAlgorithm {
    Node destination;
    Mode mode;
    int maxSpeed;
    class LabelAstar extends Label {
        Node destination;
        Mode mode;
        int maxSpeed;
        public LabelAstar(Node sommetCourant, double cout, Node pere, Node destination, Mode mode, int maxSpeed) {
            super(sommetCourant, cout, pere);
            this.destination = destination;
            this.mode = mode;
            this.maxSpeed = maxSpeed;
        }

        @Override
        public int compareTo(Label other){
            double heuristicSelf = 0;
            double heuristicOther = 0;
            switch(this.mode){
                case TIME:
                    heuristicSelf = cout + super.sommetCourant.getPoint().distanceTo(destination.getPoint())/((double)maxSpeed/3.6);
                    heuristicOther = other.getCost()+other.sommetCourant.getPoint().distanceTo(destination.getPoint())/((double)maxSpeed/3.6);
                break;
                case LENGTH:
                    heuristicSelf = cout + super.sommetCourant.getPoint().distanceTo(destination.getPoint());
                    heuristicOther = other.getCost()+other.sommetCourant.getPoint().distanceTo(destination.getPoint());
                break;
            }
           
            return Double.compare(heuristicSelf, heuristicOther);
        }
    }

    @Override
    protected Label createLabel(Node node, double cost){
        return new LabelAstar(node, cost , null, destination, mode, maxSpeed); 
    }

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
        destination = data.getDestination();
        mode = data.getMode();
        maxSpeed = data.getGraph().getGraphInformation().getMaximumSpeed();
    }

}
