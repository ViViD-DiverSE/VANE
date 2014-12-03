package fr.familiar.attributedfm.domain;

import java.util.Collection;
import java.util.LinkedList;

public class IntegerRange extends Range{

	public IntegerRange(Collection<Integer> integers) {
		this.items=integers;
	}

	public IntegerRange() {
		this.items=new LinkedList<Integer>();
	}
	


}
