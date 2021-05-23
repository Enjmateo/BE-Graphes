package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

public class EnjalbertAlgorithm extends ShortestPathAlgorithm {

    static final double AUTONOMIE_MAX = 200000.0;
    static final double STATION_PERCENTAGE = 0.01;
    HashMap<Node, Label> map;
    Node origine;
    Node destination;
    Graph graph;
    ShortestPathData data;
    Mode mode;
    double maxSpeed;



    class Label implements Comparable<Label> {

        Node sommetCourant;
        boolean marque;
        double cout;
        double heuristique;
        Node pere;
        boolean isStation;
        boolean stationMarked;

        public Label(Node sommetCourant, double cout, Node pere, Node destination, Mode mode, double maxSpeed) {
            this.sommetCourant = sommetCourant;
            this.marque = false;
            this.cout = cout;
            this.pere = pere;
            this.isStation = false;
            this.stationMarked = false;
            switch(mode){
                case TIME:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint())/maxSpeed;
                break;
                case LENGTH:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint());
                break;
            }
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

        public void setUnmarked(){
            marque = false;
        }

        public Node getFather() {
            return this.pere;
        }

        public double getHeuristique() {
            return this.heuristique;
        }

        public boolean isMarked() {
            return this.marque;
        }

        public void setStation(){
            this.isStation = true;
        }

        public boolean isStation(){
            return isStation;
        }

        public void setFather(Node father){
            this.pere = father;
        }

        public int compareTo(Label other){
            return Double.compare(cout, other.getCost());
            /*
            ASTAR
            double totalCostSelf = cout + heuristique;
            double totalCostOther = other.getCost() + other.getHeuristique();
            if(totalCostSelf==totalCostOther) {
                return Double.compare(heuristique,other.getHeuristique());
            }
            return Double.compare(totalCostSelf, totalCostOther);*/
        }
    }

    class LabelStation implements Comparable<LabelStation>{
        Label label;

        LabelStation(Label label){
            this.label = label;
        }
        @Override
        public int compareTo(LabelStation other){
            double totalCostSelf = label.cout + 1.5*label.heuristique;
            double totalCostOther = other.label.cout + 1.5*other.label.heuristique;
            if(totalCostSelf==totalCostOther) {
                return Double.compare(label.heuristique,other.label.heuristique);
            }
            return Double.compare(totalCostSelf, totalCostOther);
        }
    }

    public EnjalbertAlgorithm(ShortestPathData data) {
        super(data);
        mode = data.getMode();
        maxSpeed = (double)data.getMaximumSpeed()/3.6;
        if(maxSpeed < 0 ){
            maxSpeed = (double)data.getGraph().getGraphInformation().getMaximumSpeed()/3.6;
        }
    }

    protected Label createLabel(Node node, double cost){
        return new Label(node, cost , null, destination, mode, maxSpeed); 
    }

    @Override
    protected ShortestPathSolution doRun() {
        init();
        notifyOriginProcessed(origine);
        if(origine==destination) return new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, origine));

        Path cheminComplet = new Path(graph,origine);
        Node dernier = origine;
        do{
            Path chemin = findNextStep(dernier);
            if(chemin==null)  return new ShortestPathSolution(data, Status.INFEASIBLE); //si il n'y a pas de solutions
            cheminComplet = Path.concatenate(cheminComplet,chemin);          
            dernier = chemin.getDestination(); 
        }while(dernier != destination);
        

        ShortestPathSolution solution = new ShortestPathSolution(data, Status.OPTIMAL, cheminComplet);
        return solution;
    }

    protected void placeStations(){
        double prob = STATION_PERCENTAGE / 100;
        for ( Label label : map.values()){
            if(Math.random()<prob){
              label.setStation();
              notifyStation(label.getSommet());
            }
        }
    }

    protected void init(){
        data = getInputData();
        origine = data.getOrigin();
        destination = data.getDestination();
        graph = data.getGraph();

        map = new HashMap<Node, Label>();

        for (Node node : graph.getNodes()){
            map.put(node,createLabel(node,Float.MAX_VALUE));
        }

        placeStations();

    }

    protected void resetLabels(){
        for ( Label label : map.values()){
            label.setCost(Float.MAX_VALUE);
            label.setFather(null);
            label.setUnmarked();
        }
    }

    protected Path findNextStep(Node lastOrigine) {
        BinaryHeap<Label> labels = new BinaryHeap<Label>();
        BinaryHeap<LabelStation> stations = new BinaryHeap<LabelStation>();
        ArrayList<Node> path = new ArrayList<Node>();

        resetLabels();

        Label min = map.get(lastOrigine);
        min.setCost(0);

        labels.insert(min);
        Node nodeMin;
        while(true){
            min = labels.deleteMin();
            nodeMin = min.getSommet();
            min.setMarked();
            if(min.getCost()>AUTONOMIE_MAX) {
                if (stations.isEmpty()){
                    System.out.println("Autonomie max atteinte : "+min.getCost());
                    return null;
                }
                LabelStation meilleureStationLabel=stations.deleteMin();
                Node meilleureStation = meilleureStationLabel.label.sommetCourant;
                notifyStationFill(meilleureStation);
                path.add(meilleureStation);
                break;
            }
            if(min.isStation()&& !min.stationMarked) {
                min.stationMarked = true;
                stations.insert(new LabelStation(min));
                System.out.print("+");
            }
            notifyNodeMarked(nodeMin);
            if(nodeMin == destination) {
                notifyDestinationReached(destination);
                path.add(destination);
                break;
            }
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
            if(labels.isEmpty()) return null;
        }
        
        while(true){
            Node pere = map.get(path.get(path.size() - 1)).getFather();
            path.add(pere);
            if(pere==lastOrigine) break;
        }

        Collections.reverse(path);
        Path chemin = Path.createShortestPathFromNodes(graph, path);

        System.out.println("\nReconstitution du chemin : \n Longueur : "+chemin.getLength()+"\n Taille : "+chemin.size()+"\n Depart : "+chemin.getOrigin().getId()+"\n Arrivée : "+chemin.getDestination().getId());

        return chemin;

    }

}
