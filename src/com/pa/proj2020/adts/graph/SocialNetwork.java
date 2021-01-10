package com.pa.proj2020.adts.graph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

<<<<<<< Updated upstream
public class SocialNetwork implements Originator, Serializable {
=======
public class SocialNetwork implements Originator, Serializable{
>>>>>>> Stashed changes

    private final Logging log = Logging.getInstance();
    private final Statistics statistics;
    private DirectGraph<User, Relationship> graph;
    private final Map<Integer, User> users;
    private final Map<Integer, ArrayList<String>> relationships;
    private final Map<Integer, Interest> interests;

    /**
     * Cria um objeto SocialNetwork
     * Inicializa os atributos
     */
    public SocialNetwork() {
        graph = new DirectGraph<>();
        users = new HashMap<>();
        relationships = new HashMap<>();
        interests = new HashMap<>();
        statistics = new Statistics();
    }

    /**
     * Adiciona nas coleções a informação correspondente encontrada nos ficheiros user_names.csv e relationships.csv
     */
    public void initializeData() {
        ReadData read = new ReadData();
        HashMap<String, ArrayList<String>> temp = read.readData("user_names.csv");
        HashMap<String, ArrayList<String>> tempRelationships = read.readData("relationships.csv");
        HashMap<String, ArrayList<String>> tempInterests = read.readData("interest_names.csv");
        HashMap<String, ArrayList<String>> tempIdsOfUsersInterests = read.readData("interests.csv");

        for (String id : temp.keySet()) {
            users.put(Integer.parseInt(id), new User(temp.get(id).get(0), Integer.parseInt(id), Type.ADICIONADO));
        }

        for (String id : tempRelationships.keySet()) {
            relationships.put(Integer.parseInt(id), tempRelationships.get(id));
        }

        for (String id : tempInterests.keySet()) {
            interests.put(Integer.parseInt(id), new Interest(Integer.parseInt(id), tempInterests.get(id).get(0), tempIdsOfUsersInterests.get(id)));
        }

    }

    /**
     * Insere os vertices e arestas no grafo e retorna-o
     *
     * @return o grafo
     */
    public DirectGraph<User, Relationship> constructModelTotal() {
        for (User user : users.values()) {
            graph.insertVertex(user);
        }

        for (Integer id : relationships.keySet()) {
            for (String id2 : relationships.get(id)) {
                this.insertEdge(users.get(id), users.get(Integer.parseInt(id2)));
//                graph.insertEdge(users.get(id), users.get(Integer.parseInt(id2)), new RelationshipSimple());
            }
        }

        return this.graph;
    }


    public List<Interest> interestsOfUser(int idUser) {
        if (idUser < 0) {
            return null;
        }
        ArrayList<Interest> list = new ArrayList<>();

        for (Interest interest : this.interests.values()) {
            if (interest.getIdsOfUsers().contains(String.valueOf(idUser))) {
                list.add(interest);
                log.addInterest(idUser, interest.getId());
            }
        }

        return list;

    }
    public void insertEdge(User user1, User user2){
        this.insertEdge(user1, user2, false);
    }

    public void insertEdge(User user1, User user2, boolean addIndirect) {
        if (user1 == null || user2 == null) {
            return;
        }

        List<Interest> tempInterests = new ArrayList<>();
        boolean relationshipDirect = false;
        Relationship relationship;

        for (Interest interest : this.interests.values()) {
            if (interest.getIdsOfUsers().contains(String.valueOf(user1.getID())) &&
                    interest.getIdsOfUsers().contains(String.valueOf(user2.getID()))) {
                tempInterests.add(interest);
            }
        }

        if (this.relationships.get(user1.getID()).contains(String.valueOf(user2.getID()))) {
            relationshipDirect = true;
        }

        //HashSet<Edge<Relationship, User>> tempEdges = new HashSet<>(this.graph.edges());
        //tempEdges

<<<<<<< Updated upstream
        if (!tempInterests.isEmpty() && !relationshipDirect) {
            relationship = new RelationshipIndirect(tempInterests);
            this.graph.insertEdge(user1, user2, relationship);
            log.addRelationshipIndirect(user1.getID(), user2.getID(), tempInterests.size());
        } else if (tempInterests.isEmpty() && relationshipDirect) {
            relationship = new RelationshipSimple();
            this.graph.insertEdge(user1, user2, relationship);
            log.addRelationshipDirect(user1.getID(), user2.getID(), 0);
        } else if (!tempInterests.isEmpty()) {
=======
        if (!tempInterests.isEmpty() && !relatioshipDirect && addIndirect) {
            relationship = new RelationshipIndirect(tempInterests);
            this.graph.insertEdge(user1, user2, relationship);
            log.addRelationshipIndirect(user1.getID(), user2.getID(), tempInterests.size());
        } else if (tempInterests.isEmpty() && relatioshipDirect && !addIndirect) {
            relationship = new RelationshipSimple();
            this.graph.insertEdge(user1, user2, relationship);
            log.addRelationshipDirect(user1.getID(), user2.getID(), 0);
        } else if (!tempInterests.isEmpty() && relatioshipDirect && !addIndirect) {
>>>>>>> Stashed changes
            relationship = new RelationshipShared(tempInterests);
            this.graph.insertEdge(user1, user2, relationship);
            log.addRelationshipDirect(user1.getID(), user2.getID(), tempInterests.size());
        }

    }


<<<<<<< Updated upstream
    public void constructModelIterative(int idUser) {
=======
    public void addIndirectRelationships(int idUser){
        User user = this.users.get(idUser);

        for (Vertex<User> userVertex : this.graph.vertices()) {
            if (userVertex.element().getID() != idUser) {
                this.insertEdge(user, userVertex.element(), true);
            }
        }
    }

    public void constructModelIteractive(int idUser) {
>>>>>>> Stashed changes

        if (relationships.isEmpty() || users.isEmpty()) {
            this.initializeData();
        } else if (idUser < 0) {
            return;
        }
        User user = this.users.get(idUser);

        if (graph.containVertice(user)) {

            for (Vertex<User> userVertex : this.graph.vertices()) {
                if (userVertex.element().getID() == idUser && userVertex.element().getType().equals(Type.INCLUIDO)) {
                    userVertex.element().setType(Type.ADICIONADO);
                    break;
                }
            }

        } else {
            user.addListInterest(this.interestsOfUser(user.getID()));
            graph.insertVertex(user);
        }

        for (String idRelationship : this.relationships.get(user.getID())) {
            User userRelationship = this.users.get(Integer.parseInt(idRelationship));
            if (!graph.containVertice(userRelationship)) {
                userRelationship.setType(Type.INCLUIDO);
                this.graph.insertVertex(userRelationship);
                this.statistics.addUsersIncluded(user, userRelationship);
                userRelationship.addListInterest(this.interestsOfUser(userRelationship.getID()));
            }
        }

        for (Vertex<User> userVertex : this.graph.vertices()) {
            if (userVertex.element().getID() != idUser) {
                this.insertEdge(user, userVertex.element());
            }
        }
    }


    public int minPath(Vertex<User> origin, Vertex<User> end, ArrayList<User> path) {
        return this.graph.minCostPath(origin, end, path);
    }

    @Override
    public String toString() {
        ArrayList<User> path = new ArrayList<>();
        Vertex<User> user1 = null;
        Vertex<User> user2 = null;
        for (Vertex<User> user : this.graph.vertices()) {
            if (user.element().getID() == 1) {
                user1 = user;
            } else if (user.element().getID() == 11) {
                user2 = user;
            }
        }

        minPath(user1, user2, path);
        return "SocialNetwork{" +
                "minPath=" + path.toString() +
                '}';
    }


<<<<<<< Updated upstream
    public String addedUsersStats() {
=======
    public List<String> getUsersNotInserted(){
        ArrayList<String> list = new ArrayList<>();

        ArrayList<Integer> tempList = new ArrayList<>();

        for(Vertex<User> user : this.graph.vertices()){
            tempList.add(user.element().getID());
        }

        for(Integer id : this.users.keySet()){
            if(!tempList.contains(id)){
                list.add(this.users.get(id).toString());
            }
        }
        return list;
    }


    public String addedUsersStats(){
>>>>>>> Stashed changes
        return this.statistics.addedUsersStats(this.graph);
    }

    public String includedUsersStats() {
        return this.statistics.includedUsersStats(this.graph);
    }

    public String userWithMoreDirectRelationshipsStats() {
        return this.statistics.userWithMoreDirectRelationshipsStats(this.graph);
    }

    public String interestMostSharedStats() {
        return this.statistics.interestMostSharedStats(this.graph);
    }

    public DirectGraph<User, Relationship> getGraph() {
        return this.graph;
    }

    public Map<Integer, User> getUsers() {
        return this.users;

    }

    public Logging getLog() {
        return log;
    }

    public Map<Integer, ArrayList<String>> getRelationships() {
        return relationships;
    }

    public Map<Integer, Interest> getInterests() {
        return interests;
    }
<<<<<<< Updated upstream

    @Override
=======
	
	@Override
>>>>>>> Stashed changes
    public Memento createMemento() {
        return new MyMemento(this);
    }

    @Override
    public void setMemento(Memento savedState) {
        ByteArrayInputStream temp = new ByteArrayInputStream(savedState.getState());
        try {
            graph = (DirectGraph<User, Relationship>) new ObjectInputStream(temp).readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
<<<<<<< Updated upstream

    static class MyMemento implements Memento {
=======
	
	static class MyMemento implements Memento {
>>>>>>> Stashed changes
        private byte[] state;

        public MyMemento(SocialNetwork stateToSave) {
            load(stateToSave);
        }

        private void load(SocialNetwork stateToSave) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(bos);
                oos.writeObject(stateToSave.getGraph());
                oos.flush();
                oos.close();
                bos.close();
                state = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] getState() {
            return state;
        }
    }
<<<<<<< Updated upstream
=======
	
	
>>>>>>> Stashed changes
}
