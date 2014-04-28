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
package es.us.isa.ChocoReasoner.pairwise;

import fr.familiar.attributedfm.reasoning.FeatureModelReasoner;
import fr.familiar.attributedfm.reasoning.PerformanceResult;




public abstract class ChocoQuestion  {
	
	public Class<? extends FeatureModelReasoner> getReasonerClass() {
		return es.us.isa.ChocoReasoner.pairwise.ChocoReasoner.class;
	}
	
	public ChocoQuestion(){
	}
	
	public void preAnswer(FeatureModelReasoner r){
		
	}
	
	public  PerformanceResult answer(FeatureModelReasoner r){
		
		return null;
		
	}
	
	public void postAnswer(FeatureModelReasoner r){
		
	}
	
}
