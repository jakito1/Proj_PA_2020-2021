package com.pa.proj2020.adts.graph;

import observer.Subject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe responsável pela gestão da Social Network
 */
public class SocialNetwork extends Subject implements Originator, Serializable {
    private final HashMap<Integer, User> users;
    private final HashMap<Integer, ArrayList<String>> relationships;
    private final HashMap<Integer, Interest> interests;
    private final Statistics statistics;
    private final MemoryPersistence memoryPersistence;
    private DirectGraph<User, Relationship> graph;
    private FileObject fileObject;


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
        memoryPersistence = new MemoryPersistence(this);
    }

    /**
     * Cria um objeto SocialNetwork atraves do usernamefiles, relationshipfiles, interestnamefile e interestfiles
     *
     * @param userNamesFile     representa o ficheiro com os nomes dos utilizadores
     * @param relationshipsFile representa o ficheiro com relationships
     * @param interestNamesFile representa o ficheiro com o nome dos interesses
     * @param interestsFile     representa o ficheiro com os interesses
     */
    public SocialNetwork(String userNamesFile, String relationshipsFile, String interestNamesFile, String interestsFile) {
        this();
        this.fileObject = new FileObject(userNamesFile, relationshipsFile, interestNamesFile, interestsFile);
    }

    public HashMap<Integer, ArrayList<String>> getRelationships() {
        return relationships;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Método que inicializa os nomes dos ficheiros
     *
     * @param userNamesFile     representa o ficheiro com os nomes dos utilizadores
     * @param relationshipsFile representa o ficheiro com relationships
     * @param interestNamesFile representa o ficheiro com o nome dos interesses
     * @param interestsFile     representa o ficheiro com os interesses
     */
    public void setFileNames(String userNamesFile, String relationshipsFile,
                             String interestNamesFile, String interestsFile) {
        if (fileObject == null) {
            fileObject = new FileObject(userNamesFile, relationshipsFile, interestNamesFile, interestsFile);
        } else {
            this.fileObject.setFileNames(userNamesFile, relationshipsFile, interestNamesFile, interestsFile);
        }
        this.fileObject.checkFilenames();
    }

    /**
     * Adiciona nas coleções a informação correspondente encontrada nos ficheiros user_names.csv e relationships.csv
     */
    public void initializeData() {
        HashMap<String, ArrayList<String>> temp = ReadData.readData("user_names.csv");
        HashMap<String, ArrayList<String>> tempRelationships = ReadData.readData("relationships.csv");
        HashMap<String, ArrayList<String>> tempInterests = ReadData.readData("interest_names.csv");
        HashMap<String, ArrayList<String>> tempIdsOfUsersInterests = ReadData.readData("interests.csv");

        temp.keySet().forEach(id -> users
                .put(Integer.parseInt(id), new User(temp.get(id).get(0), Integer.parseInt(id), Type.ADICIONADO)));

        tempRelationships.keySet().forEach(id -> relationships
                .put(Integer.parseInt(id), tempRelationships.get(id)));

        tempInterests.keySet().forEach(id -> interests
                .put(Integer.parseInt(id), new Interest(Integer.parseInt(id),
                        tempInterests.get(id).get(0), tempIdsOfUsersInterests.get(id))));

    }

    /**
     * Método que constroi o algoritmo Total
     *
     * @return grafo com algoritmo total
     */
    public DirectGraph<User, Relationship> constructModelTotal() {
        return (new TotalModel(this).modelConstructor());
    }

    /**
     * Método que retorna o grafo
     *
     * @return o grafo construido
     */
    public DirectGraph<User, Relationship> getGraph() {
        return this.graph;
    }

    protected void setGraph(DirectGraph<User, Relationship> graph) {
        this.graph = graph;
    }

    /**
     * Metodo que retorna uma colecao com users
     *
     * @return um hashmap com users
     */
    public HashMap<Integer, User> getUsers() {
        return this.users;
    }

    /**
     * Metodo que retorna os interesses de um utilizador
     *
     * @param idUser representa o id do utilizador
     * @return a lista de interesses do utilizador fornecido
     */
    public List<Interest> interestsOfUser(int idUser) {
        return interestsOfUser(idUser, this.interests);
    }

    /**
     * Metodo que insere uma aresta entre dois users
     *
     * @param user1 representa um utilizador
     * @param user2 representa outro utilizador
     */
    public void insertEdge(User user1, User user2) {
        this.insertEdge(user1, user2, false);
    }

    /**
     * Metodo que insere uma aresta entre dois utilizadores
     *
     * @param user1       representa um utilizador
     * @param user2       representa outro utilizador
     * @param addIndirect true se quiser adicionar relacoes indiretas, false caso contrario
     */
    public void insertEdge(User user1, User user2, boolean addIndirect) {
        if (user1 == null || user2 == null) return;

        boolean relationshipDirect = false;

        List<Interest> tempInterests = this.interests.values().stream().filter(interest -> interest.getIdsOfUsers()
                .contains(String.valueOf(user1.getID())) &&
                interest.getIdsOfUsers().contains(String.valueOf(user2.getID()))).collect(Collectors.toList());

        if (this.relationships.get(user1.getID()).contains(String.valueOf(user2.getID()))) relationshipDirect = true;

        checkInterest(user1, user2, addIndirect, tempInterests, relationshipDirect);
        SocialNetworkLog.updateLog();

    }

    private void checkInterest(User user1, User user2, boolean addIndirect, List<Interest> tempInterests, boolean relationshipDirect) {
        Relationship relationship = new RelationshipSimple();
        if (!tempInterests.isEmpty() && ((!relationshipDirect && addIndirect) || (relationshipDirect && !addIndirect))) {
            relationship = new RelationshipIndirect(tempInterests);
        }
        this.graph.insertEdge(user1, user2, relationship);
        SocialNetworkLog.getLog().addRelationshipDirect(user1.getID(), user2.getID(), tempInterests.size());
    }

    /**
     * Metodo que adiciona relacoes indiretas
     *
     * @param idUser representa o user
     */
    public void addIndirectRelationships(int idUser) {
        User user = this.users.get(idUser);

        for (Vertex<User> userVertex : this.graph.vertices()) {
            if (userVertex.element().getID() != idUser) {
                this.insertEdge(user, userVertex.element(), true);
            }
        }
    }

    /**
     * Método que constroi o algoritmo Iterativo
     *
     * @param idUser representa o user
     */
    public void constructModelIterative(int idUser) {
        new IterativeModel(this, idUser).modelConstructor();
    }

    /**
     * Metodo que retorna o caminho de menor custo entre dois vertices
     *
     * @param origin representa um vertice
     * @param end    representa outro vertice
     * @param path   representa o caminho de menor custo
     * @return o custo do caminho
     */
    public int minPath(Vertex<User> origin, Vertex<User> end, ArrayList<User> path) {
        return this.graph.minCostPath(origin, end, path);
    }

    /**
     * Metodo que retorna uma string com a social network
     *
     * @return uma string com a social network
     */
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

    /**
     * Metodo que permite obter a lista de utilizadores nao inseridos
     *
     * @return a lista de utilizadores nao inseridos
     */
    public List<String> getUsersNotInserted() {
        ArrayList<Integer> tempList = this.graph.vertices().stream()
                .map(user -> user.element().getID()).collect(Collectors.toCollection(ArrayList::new));

        return users.keySet().stream().filter(id -> !tempList.contains(id))
                .map(id -> this.users.get(id).toString()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Metodo que retorna os interesses de um utilizador
     *
     * @param idUser representa o id do utilizador
     * @return a lista de interesses do utilizador fornecido
     */
    public List<Interest> interestsOfUser(int idUser, HashMap<Integer, Interest> thisInterests) {
        if (idUser < 0) return null;
        ArrayList<Interest> list = new ArrayList<>();

        thisInterests.values().stream().filter(interest -> interest.getIdsOfUsers()
                .contains(String.valueOf(idUser))).forEach(interest -> {
            list.add(interest);
            SocialNetworkLog.getLog().addInterest(idUser, interest.getId());
        });
        SocialNetworkLog.updateLog();
        return list;
    }

    /**
     * Metodo que retorna a estatistica de utilizadores adicionados
     *
     * @return estatistica de utilizadores adicionados
     */
    public String addedUsersStats() {
        return this.statistics.addedUsersStats(this.graph);
    }

    /**
     * Metodo que retorna a estatistica de utilizadores incluidos
     *
     * @return estatistica de utilizadores incluidos
     */
    public String includedUsersStats() {
        return this.statistics.includedUsersStats();
    }

    /**
     * Metodo que retorna a estatistica de utilizadores com mais relacionamentos
     *
     * @return estatistica de utilizadores com mais relacionamentos
     */
    public String userWithMoreDirectRelationshipsStats() {
        return this.statistics.userWithMoreDirectRelationshipsStats(this.graph);
    }

    /**
     * Metodo que retorna a estatistica de interesse mais partilhado
     *
     * @return estatistica de interesse mais partilhado
     */
    public String interestMostSharedStats() {
        return this.statistics.interestMostSharedStats(this.graph);
    }

    /**
     * Metodo que retorna a estatistica de utilizadores com mais relacionamentos
     *
     * @return estatistica de utilizadores com mais relacionamentos
     */
    public Map<User, Integer> topFiveUsersWithMostRelationshipsStats() {
        return this.statistics.topFiveUsersWithMostRelationshipsStats(this.graph);
    }

    /**
     * Metodo que permite obter uma colecao de interesses
     *
     * @return um hashmap de interesses
     */
    public HashMap<Integer, Interest> getInterests() {
        return interests;
    }

    public HashMap<User, Vertex<User>> getVertices() {
        return graph.getVertices();
    }

    /**
     * Metodo que permite obter a estatistica do top 5 de interesses
     *
     * @return a estatistica do top 5 de interesses
     */
    public Map<Interest, Integer> topFiveInterestsStats() {
        return this.statistics.topFiveInterestsStats(this.graph);
    }

    /**
     * Metodo que permite criar o memento
     *
     * @return o novo memento
     */
    @Override
    public Memento createMemento() {
        return new MyMemento(this.graph);
    }

    /**
     * Metodo que permite usar o memento
     *
     * @param savedState representa o novo memento
     */
    @Override
    public void setMemento(Memento savedState) {
        ByteArrayInputStream temp = new ByteArrayInputStream(savedState.getState());
        try {
            HashMap<User, Vertex<User>> hashMap;
            hashMap = (HashMap<User, Vertex<User>>) new ObjectInputStream(temp).readObject();
            graph = new DirectGraph<>(hashMap);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public MemoryPersistence getMemoryPersistence() {
        return memoryPersistence;
    }

    private static class MyMemento implements Memento {
        private byte[] state;

        public MyMemento(DirectGraph<User, Relationship> stateToSave) {
            load(stateToSave);
        }

        private void load(DirectGraph<User, Relationship> stateToSave) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(bos);
                oos.writeObject(stateToSave.getVertices());
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
}
