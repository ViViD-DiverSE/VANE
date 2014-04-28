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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.pairwise.ChocoQuestion;
import es.us.isa.ChocoReasoner.pairwise.ChocoReasoner;
import es.us.isa.ChocoReasoner.pairwise.Pair;
import fr.familiar.attributedfm.Product;
import fr.familiar.attributedfm.reasoning.FeatureModelReasoner;
import fr.familiar.attributedfm.reasoning.PerformanceResult;


public class ChocoOptimalProducts extends ChocoQuestion  {

	Collection<Product> products = new ArrayList<Product>();
	public String attname = "";
	public float time=0;
	public Map<String, Collection<String>> pairWiseCoverage = new HashMap<String, Collection<String>>();
	public PerformanceResult answer(FeatureModelReasoner r) {
		ChocoReasoner reasoner = (ChocoReasoner) r;
		

		Model p = reasoner.getProblem();

		// primero cramos la coleccion con los atributos que nos interesan
		// dependiendo de la cadena de entrada

		Collection<IntegerVariable> selectedAtts = new ArrayList<IntegerVariable>();

		Iterator<Entry<Pair, Map<String, IntegerVariable>>> atributesIt = reasoner.attVars
				.entrySet().iterator();
		while (atributesIt.hasNext()) {
			Entry<Pair, Map<String, IntegerVariable>> e = atributesIt.next();
			for (Entry<String, IntegerVariable> entry : e.getValue().entrySet()) {
				if (entry.getKey().contains(attname)) {
					selectedAtts.add(entry.getValue());
				}
			}
		}
		// Ahora necesitamos crear una variable suma de todos los atributos
		// anteriores"
		IntegerVariable[] reifieds = new IntegerVariable[selectedAtts.size()];

		IntegerVariable suma = Choco.makeIntVar("_suma", 0, 100000);
		IntegerExpressionVariable sumatorio = Choco.sum(selectedAtts.toArray(reifieds));
		Constraint sumReifieds = Choco.eq(suma, sumatorio);
		p.addConstraint(sumReifieds);

		Solver sol = new CPSolver();
		sol.read(p);

		IntDomainVar maxVar = sol.getVar(suma);
		//Optimizing heuristic for the sum of attributes
		sol.setVarIntSelector(new MinDomain(sol, sol.getVar(new IntegerVariable[]{suma})));
		sol.setValIntIterator(new DecreasingDomain());
		long starttime=System.currentTimeMillis();
		sol.maximize(maxVar, false);
		time=System.currentTimeMillis()-starttime;
		
		// I will only retrieve 1 assignation.
		for (int i = 0; i < p.getNbIntVars(); i++) {
			IntDomainVar aux = sol.getVar(p.getIntVar(i));
			if (aux.getName().equals("_suma")) {
				//System.out.println("The maximum value is: " + aux.getVal());
			}
			if (aux.getVal() > 0) {
				String name = aux.getName();
				if (!name.contains(".") && !name.endsWith("_card")) {
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
			//System.out.println(str);
			
		}
//		res.fillFields(sol);
//		return res;
		return null;
	}




	public void setAttributeName(String name) {
		this.attname = name;

	}

}
