import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class AlienFlora {
    private File xmlFile;
    private Map<String, Genome> genomeMap = new HashMap<>();
    private List<GenomeCluster> clusters = new ArrayList<>();

    public AlienFlora(File xmlFile) {
        this.xmlFile = xmlFile;
    }
    public void readGenomes() {
        try {
            System.out.println("##Start Reading Flora Genomes##");

            Map<String, List<String>> connectionMap = new HashMap<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            Document xmlDoc = docBuilder.parse(xmlFile);
            xmlDoc.getDocumentElement().normalize();

            NodeList genomes = xmlDoc.getElementsByTagName("genome");

            for (int i = genomes.getLength() - 1; i >= 0; i--) {
                Element genomeElement = (Element) genomes.item(i);
                String genomeId = genomeElement.getElementsByTagName("id").item(0).getTextContent();
                int evoFactor = Integer.parseInt(genomeElement.getElementsByTagName("evolutionFactor").item(0).getTextContent());
                Genome genome = new Genome(genomeId, evoFactor);
                genomeMap.put(genomeId, genome);
                connectionMap.putIfAbsent(genomeId, new ArrayList<>());

                NodeList links = genomeElement.getElementsByTagName("link");
                int j = 0;
                while (j < links.getLength()) {
                    Element linkElem = (Element) links.item(j);
                    String neighborId = linkElem.getElementsByTagName("target").item(0).getTextContent();
                    int adaptFactor = Integer.parseInt(linkElem.getElementsByTagName("adaptationFactor").item(0).getTextContent());
                    genome.addLink(neighborId, adaptFactor);
                    connectionMap.get(genomeId).add(neighborId);
                    connectionMap.putIfAbsent(neighborId, new ArrayList<>());
                    connectionMap.get(neighborId).add(genomeId);
                    j++;
                }
            }

            Set<String> marked = new HashSet<>();
            for (String genomeId : genomeMap.keySet()) {
                if (marked.contains(genomeId)) continue;

                List<String> clusterMembers = new ArrayList<>();
                Queue<String> queue = new LinkedList<>();
                queue.add(genomeId);

                do {
                    String current = queue.poll();
                    if (marked.contains(current)) continue;

                    marked.add(current);
                    clusterMembers.add(current);

                    List<String> neighbors = connectionMap.getOrDefault(current, Collections.emptyList());
                    for (int idx = neighbors.size() - 1; idx >= 0; idx--) {
                        String neighbor = neighbors.get(idx);
                        if (!marked.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                } while (!queue.isEmpty());

                GenomeCluster cluster = new GenomeCluster();
                for (int m = 0; m < clusterMembers.size(); m++) {
                    cluster.addGenome(genomeMap.get(clusterMembers.get(m)));
                }
                clusters.add(cluster);
            }

            System.out.println("Number of Genome Clusters: " + clusters.size());
            List<List<String>> clusterOutput = new ArrayList<>();
            for (GenomeCluster cluster : clusters) {
                List<String> ids = new ArrayList<>(cluster.genomeMap.keySet());
                clusterOutput.add(ids);
            }
            System.out.println("For the Genomes: " + clusterOutput);
            System.out.println("##Reading Flora Genomes Completed##");

        } catch (Exception ex) {
            System.out.println("Error during genome reading: " + ex.getMessage());
        }
    }


    public void evaluateEvolutions() {
        try {
            System.out.println("##Start Evaluating Possible Evolutions##");

            List<Double> outcomeList = new ArrayList<>();
            int validPairs = 0;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList pairList = ((Element) doc.getElementsByTagName("possibleEvolutionPairs").item(0))
                    .getElementsByTagName("pair");

            int index = 0;
            while (index < pairList.getLength()) {
                Element pair = (Element) pairList.item(index);
                String first = pair.getElementsByTagName("firstId").item(0).getTextContent();
                String second = pair.getElementsByTagName("secondId").item(0).getTextContent();

                GenomeCluster clusterA = null;
                GenomeCluster clusterB = null;

                for (GenomeCluster cl : clusters) {
                    if (clusterA == null && cl.contains(first)) clusterA = cl;
                    if (clusterB == null && cl.contains(second)) clusterB = cl;
                    if (clusterA != null && clusterB != null) break;
                }

                if (clusterA == null || clusterB == null) {
                    outcomeList.add(-1.0);
                    index++;
                    continue;
                }

                if (clusterA == clusterB) {
                    outcomeList.add(-1.0);
                    index++;
                    continue;
                }

                int minEvoA = clusterA.getMinEvolutionGenome().evolutionFactor;
                int minEvoB = clusterB.getMinEvolutionGenome().evolutionFactor;

                double computedValue = (minEvoA + minEvoB) / 2.0;

                if (computedValue >= 0) {
                    validPairs++;
                }

                outcomeList.add(computedValue);
                index++;
            }

            int totalPairs = outcomeList.size();
            System.out.println("Number of Possible Evolutions: " + totalPairs);
            System.out.println("Number of Certified Evolution: " + validPairs);
            System.out.println("Evolution Factor for Each Evolution Pair: " + outcomeList);
            System.out.println("##Evaluated Possible Evolutions##");

        } catch (Exception err) {
            System.out.println("Error during evolution evaluation: " + err.getMessage());
        }
    }



    public void evaluateAdaptations() {
        System.out.println("##Start Evaluating Possible Adaptations##");

        List<Integer> adaptationResults = new ArrayList<>();
        int confirmedCount = 0;

        try {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = xmlBuilder.parse(xmlFile);
            xmlDoc.getDocumentElement().normalize();

            NodeList pairList = ((Element) xmlDoc.getElementsByTagName("possibleAdaptationPairs").item(0))
                    .getElementsByTagName("pair");

            for (int idx = 0; idx < pairList.getLength(); idx++) {
                Element pairNode = (Element) pairList.item(idx);
                String genomeIdA = pairNode.getElementsByTagName("firstId").item(0).getTextContent();
                String genomeIdB = pairNode.getElementsByTagName("secondId").item(0).getTextContent();

                GenomeCluster foundClusterA = null;
                GenomeCluster foundClusterB = null;

                for (GenomeCluster currentCluster : clusters) {
                    if (foundClusterA == null && currentCluster.contains(genomeIdA)) {
                        foundClusterA = currentCluster;
                    }
                    if (foundClusterB == null && currentCluster.contains(genomeIdB)) {
                        foundClusterB = currentCluster;
                    }
                    if (foundClusterA != null && foundClusterB != null) {
                        break;
                    }
                }

                boolean clustersExist = (foundClusterA != null) && (foundClusterB != null);
                if (!clustersExist) {
                    adaptationResults.add(-1);
                    continue;
                }

                int calculatedCost = -1;
                if (foundClusterA.equals(foundClusterB)) {
                    calculatedCost = foundClusterA.dijkstra(genomeIdA, genomeIdB);
                    if (calculatedCost != -1) {
                        confirmedCount++;
                    }
                }

                adaptationResults.add(calculatedCost);
            }

            System.out.println("Number of Possible Adaptations: " + adaptationResults.size());
            System.out.println("Number of Certified Adaptations: " + confirmedCount);
            System.out.println("Adaptation Factor for Each Adaptation Pair: " + adaptationResults);
            System.out.println("##Evaluated Possible Adaptations##");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


}
