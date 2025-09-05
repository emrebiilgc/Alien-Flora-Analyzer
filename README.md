# Alien Flora Analyzer

This project is a Java application that models and analyzes alien flora genomes.  
It constructs genome clusters, evaluates possible evolutions, and finds adaptation paths between genomes.  
The project combines **graph algorithms** and **XML parsing** to simulate scientific exploration on a newly discovered planet.  

---

## 📌 Project Overview
1. **Genome Clustering**  
   - Parses genomes and their links from XML input.  
   - Groups genomes into clusters using a graph structure.  

2. **Evolution Analysis**  
   - For possible evolution pairs, finds the minimum evolution factor in each cluster.  
   - Calculates the required energy for evolution using an average formula.  

3. **Adaptation Analysis**  
   - For genomes in the same cluster, finds the minimum adaptation path using **Dijkstra’s algorithm**.  
   - Returns `-1` if genomes are in different clusters.  

---

## 📂 File Structure
- `Main.java` – Entry point of the program  
- `Genome.java` – Represents a genome with evolution factor and links  
- `GenomeCluster.java` – Graph structure for grouping genomes  
- `AlienFlora.java` – Core logic for clustering, evolution, and adaptation analysis  
