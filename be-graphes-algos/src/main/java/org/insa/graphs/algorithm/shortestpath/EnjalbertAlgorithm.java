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
import org.insa.graphs.model.Point;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class EnjalbertAlgorithm extends ShortestPathAlgorithm {

    static final double MAX_AUTONOMY = 75000.0;
    static final double STATION_PERCENTAGE = 0.03;
    static final double FACTOR_HEUR_LENGTH = 1.5;
    static final double FACTOR_HEUR_TIME = 2.0;
    HashMap<Node, Label> labelsMap;
    HashMap<Label, LabelStation> stationsMap;
    Node origine;
    Node destination;
    Graph graph;
    ShortestPathData data;
    static Mode mode;
    double maxSpeed;

    class Label implements Comparable<Label> {

        Node sommetCourant;
        Node pere;

        double cout;
        double heuristique;
        double usedAutonomy;

        boolean isStation;
        boolean marque;

        Mode mode;
        double maxSpeed;

        public Label(Node sommetCourant, Node destination, Mode mode, double maxSpeed) {
            this.sommetCourant = sommetCourant;
            this.pere = null;

            this.cout = Double.MAX_VALUE;
            this.usedAutonomy = 0;

            this.isStation = false;
            this.marque = false;

            this.mode = mode;
            this.maxSpeed = maxSpeed;

            switch(mode){
                case TIME:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint())/maxSpeed;
                break;
                case LENGTH:
                    heuristique = sommetCourant.getPoint().distanceTo(destination.getPoint());
                break;
            }
        }

        //GETTERS
        public double getRealCost(){
            return this.cout;
        }
        public double getCost(){
            return this.cout;
        }
        public Node getSommet() {
            return this.sommetCourant;
        }
        public double getHeuristique() {
            return this.heuristique;
        }
        public Node getFather() {
            return this.pere;
        }
        public double getUsedAutonomy(){
            return usedAutonomy;
        }

        public boolean isMarked() {
            return this.marque;
        }
        public boolean isStation(){
            return isStation;
        }

        //SETTERS
        public void setCost(double cost){
            this.cout = cost;
        }
        public void setMarked(boolean marked){
            marque = marked;
        }
        public void setStation(){
            this.isStation = true;
        }
        public void setFather(Node father){
            this.pere = father;
        }
        public void setUsedAutonomy(double usedAutonomy){
            this.usedAutonomy = usedAutonomy;
        }
        
        public int compareTo(Label other){
            return Double.compare(cout, other.getCost());
            /*
            //ASTAR
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
        boolean isMarked;

        LabelStation(Label label){
            this.label = label;
        }
        public boolean isMarked(){
            return isMarked;
        }
        public Node getNode(){
            return label.getSommet();
        }
        public void setMarked(boolean marked){
            this.isMarked = marked;
        }
        public Label getLabel(){
            return label;
        }

        @Override
        public int compareTo(LabelStation other){
            double totalCostSelf;
            double totalCostOther;
            if(EnjalbertAlgorithm.mode == Mode.LENGTH){
                totalCostSelf = label.cout + FACTOR_HEUR_LENGTH*label.heuristique;
                totalCostOther = other.label.cout + FACTOR_HEUR_LENGTH*other.label.heuristique;
            }else{
                totalCostSelf = label.cout + FACTOR_HEUR_TIME*label.heuristique;
                totalCostOther = other.label.cout + FACTOR_HEUR_TIME*other.label.heuristique;
            }
            
            if(totalCostSelf==totalCostOther) {
                return Double.compare(label.heuristique,other.label.heuristique);
            }
            return Double.compare(totalCostSelf, totalCostOther);
        }
    }

    //FONCTION D'UTILISATION DES BATTERIES EN FONCTION DE LA VITESSE
    private double autonomyUsed(Arc arc){
        double distance = arc.getLength();
        double speed = arc.getMaximumSpeed();
        return distance; //Implémenter la fonction ici
    }

    protected Label createLabel(Node node){
        return new Label(node, destination, mode, maxSpeed); 
    }

    public EnjalbertAlgorithm(ShortestPathData data) {
        super(data);
        mode = data.getMode();
        maxSpeed = (double)data.getMaximumSpeed()/3.6;
        if(maxSpeed < 0 ){
            maxSpeed = (double)data.getGraph().getGraphInformation().getMaximumSpeed()/3.6;
        }
    }

    public Node findClosestNode(Point point) {
        Node minNode = null;
        double minDis = Double.POSITIVE_INFINITY;
        for (Node node: graph.getNodes()) {
            double dlon = point.getLongitude() - node.getPoint().getLongitude();
            double dlat = point.getLatitude() - node.getPoint().getLatitude();
            double dis = dlon * dlon + dlat * dlat; // No need to square
            if (dis < minDis) {
                minNode = node;
                minDis = dis;
            }
        }
        //Si la distance minimale est superieure à 463 m on renvoit null
        if(minDis > 0.004166666666666667) return null;
        return minNode;
    }
    
    //Permet de récuperer les stations de rechargement réelles du midi pyrénées (fonctionne uniquement dessus) à partir des données sur toute la France
    //Exporte également la liste des nodes (par ID) étant des stations. Cette liste peut être urilisée par importer stations pour gagner du temps.
    protected void placeRealsStations(){
        stationsMap = new HashMap<Label,LabelStation>();
        FileWriter myWriter;        

        BufferedReader csvReader;
        String row;

        try {
            myWriter = new FileWriter("D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\nodesStationsMidPyr.txt",true);
            csvReader = new BufferedReader(new FileReader("D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\bornes-irve-20210520.csv"));
            int max = csvReader.readLine().split(";").length;
            float lati;
            float longi;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(";");
                //float lati = Float.parseFloat(data[7].substring(1, data[7].length()-2)); //Autre BDD
                //float longi = Float.parseFloat(data[8].substring(1, data[8].length()-2));

                //Si il y a une erreur (par exemple un ; dans un des champs) on passe à la station suivante
                if(data.length != max) continue;
                try {
                    lati = Float.parseFloat(data[7]);
                    longi = Float.parseFloat(data[8]);
                }catch (NumberFormatException e){
                    continue;
                }
                
                //On exclu les stations qui ne sont pas dans un carré de 444 x 444 km environ autour de toulouse (soit 4 x 4 degrés)
                if(lati < 1.4447083 -2 ||lati > 1.4447083 +2) continue;
                if(longi < 43.5972471005413 -2 ||longi > 43.5972471005413 +2) continue;

                //On récupère la node la plus proche de la station
                Node plusProche = findClosestNode(new Point(lati, longi));

                //Si elle est à plus de 463m on la passe
                if(plusProche==null)continue;

                //On l'ajoute à la liste des nodes et on la mets en station :
                myWriter.write(Integer.toString(plusProche.getId())+"\n");
                Label labelPlusProche = labelsMap.get(plusProche);
                labelPlusProche.setStation();
                notifyStation(labelPlusProche.getSommet());
                stationsMap.put(labelPlusProche,new LabelStation(labelPlusProche));
            }
            myWriter.close();
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Fonction permettant de placer des stations au hasard en fonction d'une probabilitée
    protected void placeStations(){
        stationsMap = new HashMap<Label,LabelStation>();
        double prob = STATION_PERCENTAGE / 100;
        for (Label label : labelsMap.values()){
            if(Math.random()<prob){
                label.setStation();
                notifyStation(label.getSommet());
                stationsMap.put(label,new LabelStation(label));
            }
        }
    }

    //Permet d'importer une liste des node étant des stations et de definir ces nodes comme des stations
    protected void importStations(){
        stationsMap = new HashMap<Label,LabelStation>();

        BufferedReader reader;
        String station;

        try {
            reader = new BufferedReader(new FileReader("D:\\home\\Documents\\Travail\\INSA\\3A\\BE-Graphes\\StationsMydPyr.txt"));
            while ((station = reader.readLine()) != null) {
                Integer id = Integer.parseInt(station);
                if(id == null) continue;
                Node node = graph.get(id);
                Label label = labelsMap.get(node);
                label.setStation();
                //notifyStation(node); //TROP COUTEUX EN RESSOURCES
                stationsMap.put(label,new LabelStation(label));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Fin de l'import des nodes");
    }

    protected void init(){
        //On prépare un map des nodes 
        data = getInputData();
        origine = data.getOrigin();
        destination = data.getDestination();
        graph = data.getGraph();
        Label label;
        
        labelsMap = new HashMap<Node, Label>();

        for (Node node : graph.getNodes()){
            label = createLabel(node);
            labelsMap.put(node,label);
        }
        importStations();
    }

    @Override
    protected ShortestPathSolution doRun() {
        init();
        notifyOriginProcessed(origine);
        if(origine==destination) return new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, origine));
        
        Path cheminComplet = new Path(graph,origine);
        Node dernier = origine;
        do{
            //Parmis ces nodes on calcule les couts de chacunes 
            Path chemin = findNextStep(dernier);
            if(chemin==null)  return new ShortestPathSolution(data, Status.INFEASIBLE); //si il n'y a pas de solutions
            cheminComplet = Path.concatenate(cheminComplet,chemin);          
            dernier = chemin.getDestination(); 
        }while(dernier != destination);
        

        ShortestPathSolution solution = new ShortestPathSolution(data, Status.OPTIMAL, cheminComplet);
        return solution;
    }

    protected void resetLabels(){
        for ( Label label : labelsMap.values()){
            label.setCost(Double.MAX_VALUE);
            label.setFather(null);
            label.setMarked(false);
            label.setUsedAutonomy(0);
        }
    }

    protected Path findNextStep(Node lastOrigine) {
        BinaryHeap<Label> labels = new BinaryHeap<Label>();
        BinaryHeap<LabelStation> stationsReached = new BinaryHeap<LabelStation>();
        ArrayList<Node> path = new ArrayList<Node>();

        resetLabels();

        Label min = labelsMap.get(lastOrigine);
        min.setCost(0);
        min.setUsedAutonomy(0);
        labels.insert(min);
        Node nodeMin;
        while(true){
            if(labels.isEmpty()) break;
            min = labels.deleteMin();
            nodeMin = min.getSommet();
            min.setMarked(true);
            
            notifyNodeMarked(nodeMin);

            if(nodeMin == destination) {
                notifyDestinationReached(destination);
                path.add(destination);
                break;
            }

            if(min.isStation()&& !stationsMap.get(min).isMarked()) {
                stationsReached.insert(stationsMap.get(min));
                notifyStation(min.getSommet());//Notifiaction ici comme cela on montre que les atteignables
                System.out.print("+");
            }
            
            for(Arc arc : nodeMin.getSuccessors()){
                if(!data.isAllowed(arc)) continue;
                Node node = arc.getDestination();
                Label label = labelsMap.get(node);

                if(!label.isMarked()){
                    double usedAutonomy = min.getUsedAutonomy() + autonomyUsed(arc);
                    if(usedAutonomy>MAX_AUTONOMY) {
                        continue;
                    }
                    
                    double cost = min.getCost()+data.getCost(arc);

                    if(label.getCost() > cost){
                        if(label.getCost()==Double.MAX_VALUE) {
                            notifyNodeReached(node);
                        }else{
                            labels.remove(label);
                        }
                        label.setCost(cost);
                        label.setUsedAutonomy(usedAutonomy);
                        labels.insert(label);
                        label.setFather(min.getSommet());
                    }
                }
            }
        }

        //Si on est pas déjà arrivé à l'arrivée on récupère la meilleure station
        if(path.size()==0){
            if (stationsReached.isEmpty()){
                System.out.println("Autonomie max atteinte : "+min.getCost());
                return null;
            }
            LabelStation meilleureStationLabel=stationsReached.deleteMin();
            meilleureStationLabel.setMarked(true);
            Node meilleureStation = meilleureStationLabel.getNode();
            notifyStationFill(meilleureStation);
            path.add(meilleureStation);
        }
        while(true){
            Node pere = labelsMap.get(path.get(path.size() - 1)).getFather();
            path.add(pere);
            if(pere==lastOrigine) break;
        }
        Collections.reverse(path);
        Path chemin = Path.createShortestPathFromNodes(graph, path);

        System.out.println("\nReconstitution du chemin : \n Longueur : "+chemin.getLength()+"\n Taille : "+chemin.size()+"\n Depart : "+chemin.getOrigin().getId()+"\n Arrivée : "+chemin.getDestination().getId());

        return chemin;

    }

}
