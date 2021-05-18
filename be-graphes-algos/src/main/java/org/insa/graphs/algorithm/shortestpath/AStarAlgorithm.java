package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.algorithm.AbstractInputData.Mode;

import org.insa.graphs.model.Node;

public class AStarAlgorithm extends DijkstraAlgorithm {
    Node destination;
    Mode mode;
    double maxSpeed;

    class LabelAstar extends Label {
        double heuristique;
        public LabelAstar(Node sommetCourant, double cout, Node pere, Node destination, Mode mode, double maxSpeed) {
            super(sommetCourant, cout, pere);
            switch(mode){
                case TIME:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint())/maxSpeed;
                break;
                case LENGTH:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint());
                break;
            }
        }

        public double getHeuristique() {
            return this.heuristique;
        }

        @Override
        public int compareTo(Label other){
            double totalCostSelf = cout + heuristique;
            double totalCostOther = other.getCost() + ((LabelAstar)other).getHeuristique();
            if(totalCostSelf==totalCostOther) {
                return Double.compare(heuristique,((LabelAstar)other).getHeuristique());
            }
            return Double.compare(totalCostSelf, totalCostOther);
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
        maxSpeed = (double)data.getMaximumSpeed()/3.6;
        if(maxSpeed < 0 ){
            maxSpeed = (double)data.getGraph().getGraphInformation().getMaximumSpeed()/3.6;
        }
    }

}
