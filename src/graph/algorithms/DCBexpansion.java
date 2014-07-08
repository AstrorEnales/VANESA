package graph.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.concurrent.Callable;

public class DCBexpansion implements Callable<LinkedHashSet<HashSet<Integer>>>{
	
	//TEST
	//extended Clusters:
	public int extendedSize;
	//doppelte Cluster:
	public int doppelExtended;
	//#Seeds zu beginn
	public int numSeeds;
	
	private HashMap<Integer, HashSet<Integer>> adjacencies;
	private DCBTests test;
	private HashMap<HashSet<Integer>, HashSet<Integer>> seeds;

	//Summe der Nachbarn aller bisher enthaltener seeds
	//(N�chster Seed wird immer dem Callable zugeordnet der bisher die wenigsten 
	//Nachbarn (insgesamt) hat)
	private int numOfNeighbours;

	
	
	public DCBexpansion(HashMap<Integer, HashSet<Integer>> adjacencies,
			double density, ArrayList<Double> ranges, int attrdim,
			HashMap<Integer, ArrayList<Double>> attributes) {
		this.adjacencies = adjacencies;
		this.test = new DCBTests(adjacencies, density, ranges, attrdim, attributes);	
		seeds = new HashMap<>();
		numOfNeighbours = 0;
		
		
	
	}



	/*
	 * Expansion: Seeds werden gem�� der Kriteiren (homogenity/density erweitert)
	 * @see java.util.concurrent.Callable#call()
	 */
	public LinkedHashSet<HashSet<Integer>> call(){
		
		numSeeds = seeds.size();
		
		LinkedHashSet<HashSet<Integer>> extended = new LinkedHashSet<HashSet<Integer>>();
		while(!seeds.isEmpty()){
			Hashtable<HashSet<Integer>, HashSet<Integer>>  seedsHelp = new Hashtable<HashSet<Integer>, HashSet<Integer>>();
			seedsHelp.putAll(seeds);
			seeds.clear();
			for(HashSet<Integer> nodeSet : seedsHelp.keySet()){
				boolean finish = true;
				for(int connectedNode : seedsHelp.get(nodeSet)){
					HashSet<Integer> testSet = new HashSet<Integer>();
					testSet.addAll(nodeSet);
					testSet.add(connectedNode);				
					if(test.testDensity(testSet) && test.testHomogenity(testSet)){
						HashSet<Integer> tempNodeSet = new HashSet<Integer>();
						tempNodeSet.addAll(seedsHelp.get(nodeSet));
						for(int tempConnected : adjacencies.get(connectedNode)){
							tempNodeSet.add(tempConnected);
						}
						tempNodeSet.removeAll(testSet);
						seeds.put(testSet, tempNodeSet);
						finish = false;

					}

				}
				if(finish){
					extended.add(nodeSet);
				}
				
			}
		}
		
		
		/*
		 * Entfernung doppelter Cluster: (gepr�ft wird auch ob ein Cluster ein anderes enth�lt)
		 */
		LinkedHashSet<HashSet<Integer>> removeSubsets = new LinkedHashSet<HashSet<Integer>>();
		
		for(HashSet<Integer> cluster : extended){
			boolean subset = false;
			for(HashSet<Integer> clusterHelp : extended){
				if(clusterHelp.size() > cluster.size() && clusterHelp.containsAll(cluster)){
					subset = true;
				}
			}
			if(subset){
				removeSubsets.add(cluster);
			}
		}
		
		
		
		//TODO syso
		
		extendedSize = extended.size();
		doppelExtended = removeSubsets.size();
		
		extended.removeAll(removeSubsets);
		return extended;
	}
	
	//hinzuf�gen eines seeds
	public void putSeed(HashSet<Integer> seed, HashSet<Integer> neighbours){
		numOfNeighbours += neighbours.size();
		seeds.put(seed, neighbours);
	}
	
	
	public HashMap<HashSet<Integer>, HashSet<Integer>> getSeeds(){
		return seeds;
	}
	
	public int getNumOfNeighbours(){
		return numOfNeighbours;
	}

	

	


}