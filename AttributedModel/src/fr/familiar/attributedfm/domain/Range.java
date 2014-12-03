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
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;


/**
 * Represents a Range
 */
public abstract class Range {

	Collection items;

	public Object getMax() {
		return Collections.max(items);
	}

	public Object getMin() {
		return Collections.min(items);
	}

	public boolean isBounded(){
		return items.size()>2;
	}

	public Collection getItems() {
		return items;
	}
	
	
}
