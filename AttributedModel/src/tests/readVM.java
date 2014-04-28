package tests;

import fr.familiar.attributedfm.AttributedFeatureModel;
import fr.familiar.readers.VMReader;

public class readVM {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		VMReader reader = new VMReader();
		AttributedFeatureModel parseFile = reader.parseFile("./input/ISSTA2014.vm");
		System.out.println(parseFile);
		
	}

}
