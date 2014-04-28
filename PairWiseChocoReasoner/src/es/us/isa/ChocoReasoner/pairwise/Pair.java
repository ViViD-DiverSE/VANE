package es.us.isa.ChocoReasoner.pairwise;

import fr.familiar.attributedfm.Feature;


public class Pair {

	public Feature featurea;
	public Feature featureb;
	
	public Pair(Feature featurea, Feature featureb){
		this.featurea=featurea;
		this.featureb=featureb;
	}
	
	public Pair(String featurea, String featureb){
		this.featurea=new Feature(featurea);
		this.featureb=new Feature(featureb);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Pair){
			Pair newPair=(Pair)o;
			if((featurea.getName().equals(newPair.featureb.getName())&&featureb.getName().equals(newPair.featurea.getName()))||(featurea.getName().equals(newPair.featurea.getName())&&featureb.getName().equals(newPair.featureb.getName()))){
				return true;
			}else{
				return false;
			}
		}else{
			return false;

		}
		
	}
	
}
