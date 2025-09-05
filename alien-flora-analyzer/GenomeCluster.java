import java.util.*;

public class GenomeCluster {
    public Map<String, Genome> genomeMap = new HashMap<>();

    public void addGenome(Genome genome) {
        if (genome != null && genome.id != null) {
            genomeMap.put(genome.id, genome);
        }
    }

    public boolean contains(String genomeId) {
        return genomeMap.containsKey(genomeId);
    }

    public Genome getMinEvolutionGenome() {
        Iterator<Genome> iterator = genomeMap.values().iterator();

        if (!iterator.hasNext()) return null;

        Genome min = iterator.next();

        while (iterator.hasNext()) {
            Genome current = iterator.next();
            if (current.evolutionFactor < min.evolutionFactor) {
                min = current;
            }
        }
        return min;
    }

    public int dijkstra(String startId, String endId) {
        if (!genomeMap.containsKey(startId) || !genomeMap.containsKey(endId)) {
            return -1;
        }

        Map<String, Integer> distance = new HashMap<>();
        List<String> keys = new ArrayList<>(genomeMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            distance.put(keys.get(i), Integer.MAX_VALUE);
        }

        distance.put(startId, 0);
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));
        queue.add(startId);

        Set<String> visited = new HashSet<>();

        do {
            String current = queue.poll();
            if (current == null) break;

            if (!visited.add(current)) continue;

            if (current.equals(endId)) {
                return distance.get(current);
            }

            Genome currentGenome = genomeMap.get(current);
            List<Genome.Link> links = currentGenome.links;

            for (int i = 0; i < links.size(); i++) {
                Genome.Link link = links.get(i);
                String neighbor = link.target;
                if (!genomeMap.containsKey(neighbor)) continue;

                int newDistance = distance.get(current) + link.adaptationFactor;
                if (newDistance < distance.get(neighbor)) {
                    distance.put(neighbor, newDistance);
                    queue.add(neighbor);
                }
            }

        } while (!queue.isEmpty());

        return -1;
    }

}
