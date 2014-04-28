/**
 * 	This file is part of FaMaTS.
 *
 *     FaMaTS is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FaMaTS is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.us.isa.ChocoReasoner.pairwise.questions;
import static choco.Choco.and;
import static choco.Choco.eq;
import static choco.Choco.ifThenElse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.pairwise.ChocoQuestion;
import es.us.isa.ChocoReasoner.pairwise.ChocoReasoner;
import es.us.isa.ChocoReasoner.pairwise.Pair;
import fr.familiar.attributedfm.Product;
import fr.familiar.attributedfm.reasoning.FeatureModelReasoner;
import fr.familiar.attributedfm.reasoning.PerformanceResult;


public class ChocoOptimalPairwise extends ChocoQuestion {

	Collection<Product> products = new ArrayList<Product>();
	String attname = "";

	public PerformanceResult answer(FeatureModelReasoner r) {
		ChocoReasoner reasoner = (ChocoReasoner) r;

		Model p = reasoner.getProblem();

		Pair pairs[] = reasoner.pairs.toArray(new Pair[0]);

		int opi=0;
		Collection<IntegerVariable> reifieds = new ArrayList<IntegerVariable>();
		for(int p1index=0;p1index<pairs.length;p1index++){
			for(int p2index=0;p2index<pairs.length;p2index++){
				//play with the indexes to bypass double constratins
				Pair p1 = pairs[p1index];
				Pair p2 = pairs[p2index];
			
					if(p2index>p1index){

				Constraint c=null;
				IntegerVariable reif=Choco.makeIntVar("optimal-pw"+opi, 0, 1);
					p.addVariable(reif);
					//HACER UNA VAIABLE 1 SI LAS DOS PAREJAS SON IGUALES.
					Map<String, IntegerVariable> map = reasoner.variables.get(p1);
					Map<String, IntegerVariable> map2 = reasoner.variables.get(p2);
				
					for(String s:map.keySet()){
						IntegerVariable var1 = map.get(s);
						IntegerVariable var2 = map2.get(s);
						if(c==null){
							c=eq(var1,var2);
						}else{
							c=and(c,eq(var1,var2));
						}
					}
					//Si la variable reif esta a 1 la configuration p1 es igual a p2
					//Constraint reifiedIntConstraint = Choco.reifiedIntConstraint(reif,c);
	
					Constraint reifiedIntConstraint = ifThenElse(c, eq(reif,1), eq(reif,0));
					p.addConstraint(reifiedIntConstraint);
					reifieds.add(reif);
					opi++;
				}}
			}
		IntegerVariable[] array = reifieds.toArray(new IntegerVariable[0]);
		IntegerVariable suma = Choco.makeIntVar("_suma", 0, array.length);
		IntegerExpressionVariable sumatorio = Choco.sum(array);
		Constraint sumReifieds = Choco.eq(suma, sumatorio);
		p.addConstraint(sumReifieds);

		Solver sol = new CPSolver();
		sol.read(p);
		try {
			sol.propagate();
		} catch (ContradictionException e) {
			System.out.println("This model is not valid");
			e.printStackTrace();
		}
		//sol.setTimeLimit(30000);//?? will return best solution in some time?
		sol.maximize(sol.getVar(suma), false);
		//if(sol.solve()){System.out.println("The problem had solution");}
		Map<String, Collection<String>> pairWiseCoverage = new HashMap<String, Collection<String>>();

		// I will only retrieve 1 assignation.
		for (int i = 0; i < p.getNbIntVars(); i++) {
			IntDomainVar aux = sol.getVar(p.getIntVar(i));

			if (aux.getVal() > 0) {
				String name = aux.getName();
				if (!name.contains(".") && !name.contains("suma") &&!name.endsWith("_card") && !name.startsWith("optimal-pw")) {
					// System.out.println(name);
					int index = name.indexOf('_');
					String featureName = name.substring(0, index);
					String pairName = name.substring(index + 1);
					if (pairWiseCoverage.containsKey(pairName)) {
						pairWiseCoverage.get(pairName).add(featureName);
					} else {
						Collection<String> col = new LinkedList<String>();
						col.add(featureName);
						pairWiseCoverage.put(pairName, col);
					}
				}
			}
		}

		for (Entry<String, Collection<String>> entry : pairWiseCoverage
				.entrySet()) {
			String str = "The pair " + entry.getKey()
					+ " is covered by the product composed by: ";
			for (String s : entry.getValue()) {
				str += s + ";";
			}
			System.out.println(str);
		}
		return null;

	}



	public void setAttributeName(String name) {
		this.attname = name;

	}
}
