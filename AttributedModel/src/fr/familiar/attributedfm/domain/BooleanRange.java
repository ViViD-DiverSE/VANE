package fr.familiar.attributedfm.domain;

import java.util.Arrays;
import java.util.Collection;


public class BooleanRange extends IntegerRange {
	
	static Integer values[]={0,1};
	public BooleanRange() {
		super((Collection<Integer>)Arrays.asList(values));
	}

}
