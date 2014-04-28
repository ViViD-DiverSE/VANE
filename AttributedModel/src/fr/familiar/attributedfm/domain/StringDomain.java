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

package fr.familiar.attributedfm.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.familiar.attributedfm.AbstractDomainIntConverter;
import fr.familiar.attributedfm.StringDomainIntConverter;


public class StringDomain extends ObjectDomain {

	private List<String> stringVal;
	
	private AbstractDomainIntConverter domConverter;
	 
	public StringDomain(List<String> values){
		stringVal=values;
		domConverter= new StringDomainIntConverter(stringVal);
	}
	
	
	@Override
	public Object getValue(int i){
		if (i < stringVal.size()){
			return stringVal.get(i);
		}
		else{
			return null;
		}
	}
	
	@Override
	public Integer getIntegerValue(Object obj){
		return domConverter.convertToInteger(obj);
		
	}

	@Override
	public Set<Integer> getAllIntegerValues(){
		HashSet<Integer> res = new HashSet<Integer>();
		for(String str : stringVal){
			res.add(domConverter.convertToInteger(str));
		}
		return res;
	}
	
}
