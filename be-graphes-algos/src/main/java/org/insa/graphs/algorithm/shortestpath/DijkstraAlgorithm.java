package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {
    class Label{
        Node sommetCourant;
        boolean marque;
        float cout;
        Node pere;

        public Label(Node sommetCourant, boolean marque, float cout, Node pere) {
            this.sommetCourant = sommetCourant;
            this.marque = marque;
            this.cout = cout;
            this.pere = pere;
        }

        public float getCost(){
            return this.cout;
        }
    }

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected ShortestPathSolution doRun() {
        final ShortestPathData data = getInputData();
        ShortestPathSolution solution = null;
        // TODO:
        return solution;
    }

}
