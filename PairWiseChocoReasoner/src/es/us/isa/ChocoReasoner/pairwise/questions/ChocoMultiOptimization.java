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
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.pairwise.ChocoQuestion;
import es.us.isa.ChocoReasoner.pairwise.ChocoReasoner;
import es.us.isa.ChocoReasoner.pairwise.Pair;
import fr.familiar.attributedfm.Product;
import fr.familiar.attributedfm.reasoning.FeatureModelReasoner;
import fr.familiar.attributedfm.reasoning.PerformanceResult;

public class ChocoMultiOptimization extends ChocoQuestion{

	public String attname = "";
	public float time = 0;
	Model model;
	Solver solver;
	int val1,val2;
	
	int arraysize;
	IntegerVariable optimalAttValue;
	IntegerVariable minimalCoverage;
	public Map<String, Collection<String>> pairWiseCoverage = new HashMap<String, Collection<String>>();

	//private Solution solution;
	private double resval;

	public IntegerVariable calculateOptimalAttValue(ChocoReasoner reasoner) {
		Collection<IntegerVariable> selectedAtts = new ArrayList<IntegerVariable>();

		int maxDom=0;
		int numatt=0;
		Iterator<Entry<Pair, Map<String, IntegerVariable>>> atributesIt = reasoner.attVars
				.entrySet().iterator();
		while (atributesIt.hasNext()) {
			Entry<Pair, Map<String, IntegerVariable>> e = atributesIt.next();
			for (Entry<String, IntegerVariable> entry : e.getValue().entrySet()) {
				if (entry.getKey().contains(attname)&& entry.getValue()!=null) {
					selectedAtts.add(entry.getValue());
					if(maxDom<entry.getValue().getDomainSize()){
						maxDom=entry.getValue().getDomainSize();
					}
					numatt++;
				}
			}
		}
	//	IntegerVariable[] reifieds = new IntegerVariable[selectedAtts.size()];
		IntegerVariable suma = Choco.makeIntVar("_suma", 0,numatt*maxDom );
		
		//normalizing
		//IntegerVariable maxval= Choco.makeIntVar("_sumamax", arraysize,arraysize );
		//IntegerExpressionVariable sumatorio = Choco.div(Choco.sum(selectedAtts.toArray(reifieds)),maxval);
		
		IntegerExpressionVariable sumatorio = Choco.sum(selectedAtts.toArray(new IntegerVariable[0]));

		Constraint sumReifieds = Choco.eq(suma, sumatorio);
		model.addConstraint(sumReifieds);
		
		return suma;
	}

	public IntegerVariable calculateMinimalCoverage(ChocoReasoner reasoner) {
		Pair pairs[] = reasoner.pairs.toArray(new Pair[0]);
		Collection<IntegerVariable> reifieds = new ArrayList<IntegerVariable>();
		for (int p1index = 0; p1index < pairs.length; p1index++) {
			for (int p2index = 0; p2index < pairs.length; p2index++) {

				Pair p1 = pairs[p1index];
				Pair p2 = pairs[p2index];

				if (p2index > p1index) {

					IntegerVariable reif = Choco.makeIntVar("optimal-pw" + p1index+"_"+p2index,	0, 1);
					model.addVariable(reif);
					
					// HACER UNA VAIABLE 1 SI LAS DOS PAREJAS SON IGUALES.
					Map<String, IntegerVariable> map = reasoner.variables
							.get(p1);
					Map<String, IntegerVariable> map2 = reasoner.variables
							.get(p2);
					Collection<Constraint> c=null;
					for (String s : map.keySet()) {
						IntegerVariable var1 = map.get(s);
						IntegerVariable var2 = map2.get(s);
						if (c == null) {
							c= new LinkedList<Constraint>();
							c.add(eq(var1, var2));
						} else {
							c.add(eq(var1, var2));
						}
					}
					Constraint andctc= and(c.toArray(new Constraint[0]));
					Constraint reifiedIntConstraint = ifThenElse(andctc,
							eq(reif, 1), eq(reif, 0));
					model.addConstraint(reifiedIntConstraint);
					reifieds.add(reif);
					
				}
			}
		}
		IntegerVariable[] array = reifieds.toArray(new IntegerVariable[0]);
		IntegerVariable suma = Choco.makeIntVar("_opt2", 0, array.length);
		
		//IntegerVariable maxval = Choco.makeIntVar("_optmax", array.length,array.length);
		//IntegerExpressionVariable sumatorio = Choco.div(Choco.sum(array),maxval );
		
		IntegerExpressionVariable sumatorio = Choco.sum(array) ;
		
		Constraint sumReifieds = Choco.eq(suma, sumatorio);
		model.addConstraint(sumReifieds);
		return suma;
	}

	public PerformanceResult answer(FeatureModelReasoner r) {
		ChocoReasoner reasoner = (ChocoReasoner) r;
		//ChocoResult res = new ChocoResult();

		//if (model == null) {// so we can reuse this cpu power.
			model = reasoner.getProblem();
			optimalAttValue = calculateOptimalAttValue(reasoner);
			minimalCoverage = calculateMinimalCoverage(reasoner);
			solver = new CPSolver();
			solver.read(model);
			try {
				solver.propagate();
				//solver.solve();
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
			//System.out.println("propagation done");
		//For future optimizations	
	//		solution=solver.recordSolution();
			
			
	//	}

		IntegerVariable total = Choco.makeIntVar("_total", 0, val1*arraysize+val2*minimalCoverage.getDomainSize());

		Constraint totalctc = Choco.eq(total,Choco.sum(Choco.mult(val1,optimalAttValue),Choco.mult(val2, minimalCoverage)));
		
		
		model.addConstraint(totalctc);
		
		
		solver.read(model);
		//For future optimizations	

		//solver.restoreSolution(solution);
		solver.setTimeLimit(120000);
		
		IntDomainVar maxVar = solver.getVar(total);

		// Optimizing heuristic for the sum of attributes
		solver.setVarIntSelector(new MinDomain(solver, solver.getVar(new IntegerVariable[] { optimalAttValue })));
		solver.setValIntIterator(new DecreasingDomain());
		
	//	System.out.println("It just started");
		long starttime = System.currentTimeMillis();
		solver.maximize(maxVar, false);
		time = System.currentTimeMillis() - starttime;
	//	System.out.println("It already finished, toking "+time+" seconds.");
			
		pairWiseCoverage = new HashMap<String, Collection<String>>();
		
			for (int i = 0; i < model.getNbIntVars(); i++) {
			IntDomainVar aux = solver.getVar(model.getIntVar(i));
			if(aux.getName().contains("_total")){
				this.resval=aux.getVal();
				//System.out.println(aux.getVal());
			}
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
		
		//res.fillFields(solver);
		return null;

	}


	public void setAttributeName(String name) {
		this.attname = name;

	}

	public Class<? extends FeatureModelReasoner> getReasonerClass() {
		return ChocoReasoner.class;
	}

	public void set2stW(int parseInt) {
		this.val2=parseInt;
		
	}

	public void set1stW(int parseInt) {

		this.val1=parseInt;
	}

	public double getValue() {
		return this.resval;
	}
}
