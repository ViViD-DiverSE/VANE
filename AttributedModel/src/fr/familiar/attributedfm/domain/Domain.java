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
package fr.familiar.attributedfm.domain;

import java.util.Collection;
import java.util.LinkedList;

/**
 * This class represents a Domain  
 */
public class Domain {

	public Collection<Range> ranges;

	public Domain(){ranges= new LinkedList<Range>();};
	
	public Domain(Range r){
		//useful when only 1 range
		ranges= new LinkedList<Range>();
		this.ranges.add(r);
	}
	
	public void addRange(Range range) {
		this.ranges.add(range);
		
	}
	
}
