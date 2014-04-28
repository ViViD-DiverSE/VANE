/*
	This file is part of FaMaTS.

    FaMaTS is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FaMaTS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.

 */
package es.us.isa.ChocoReasoner.pairwise.questions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.pairwise.ChocoQuestion;
import es.us.isa.ChocoReasoner.pairwise.ChocoReasoner;
import fr.familiar.attributedfm.Product;
import fr.familiar.attributedfm.reasoning.FeatureModelReasoner;
import fr.familiar.attributedfm.reasoning.PerformanceResult;
import fr.familiar.readers.Trituple;


public class PairWiseNonAttribtuedQuestion extends ChocoQuestion {

	private Product prod;
	public Collection<Collection<Trituple>> configurations;
	public long time=0;
	public boolean nocompute;
	public PairWiseNonAttribtuedQuestion() {
	}


	public PerformanceResult answer(FeatureModelReasoner r)  {
		ChocoReasoner reasoner = (ChocoReasoner) r;

		Model p = reasoner.getProblem();
		//
		
		// primero cramos la coleccion con los atributos que nos interesan
		// dependiendo de la cadena de entrada

		Solver sol = new CPSolver();
		sol.read(p);
		//sol.setValIntIterator(new DecreasingDomain());

		try {
			sol.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		long start=System.currentTimeMillis();
		sol.solve();
		time=System.currentTimeMillis()-start;
		if(nocompute){
		Map<String, Collection<String>> pairWiseCoverage = new HashMap<String, Collection<String>>();
		// I will only retrieve 1 assignation.
		for (int i = 0; i < p.getNbIntVars(); i++) {
			IntDomainVar aux = sol.getVar(p.getIntVar(i));

			if (aux.getVal() >= 0) {
				String name = aux.getName();
				
				if (!name.endsWith("_card")) {
					// System.out.println(name);
					int index = name.indexOf('_');
					String featureName = name.substring(0, index);
					String pairName = name.substring(index + 1);
					if (pairWiseCoverage.containsKey(pairName)) {
						pairWiseCoverage.get(pairName).add(featureName+"="+aux.getVal());
					} else {
						Collection<String> col = new LinkedList<String>();
						col.add(featureName+"="+aux.getVal());
						pairWiseCoverage.put(pairName, col);
					}
				}
			}
		}

		configurations = new ArrayList<Collection<Trituple>>();
		for (Entry<String, Collection<String>> entry : pairWiseCoverage
				.entrySet()) {
			//String str = "The pair " + entry.getKey()+ " is covered by the product composed by: ";
			Collection<Trituple> tpc =new ArrayList<Trituple>();
			for (String s : entry.getValue()) {
				if(s.contains(".")){
					String feature= s.substring(0, s.indexOf('.'));
					String attribute= s.substring(s.indexOf('.')+1, s.indexOf('='));
					String value= s.substring(s.indexOf('=')+1);
					Trituple tp= new Trituple(feature, attribute, value);
					tpc.add(tp);
				}

				
			}
			configurations.add(tpc);
		}
		}
		return null;

	}

	public Product getProduct() {
		return prod;
	}

}
