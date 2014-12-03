package fr.familiar.attributedfm.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StringRange extends IntegerRange {

	Map<Integer,String> strings;
	public StringRange (){
		this.strings= new HashMap<Integer,String>();
	}
	public StringRange (Collection<String> strings){
		this.strings= new HashMap<Integer,String>();
		initializeIntegers(strings);
	}
	
	public void initializeIntegers(Collection<String> strings){
		int i=1;
		for(String str:strings){
			this.strings.put(i, str);
		}
		this.items=this.strings.values();
	}
	
	public void addString(String str){
		this.strings.put(strings.size()+1, str);
	}
}
