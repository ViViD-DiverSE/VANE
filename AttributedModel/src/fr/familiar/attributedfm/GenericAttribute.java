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
package fr.familiar.attributedfm;

import fr.familiar.attributedfm.domain.Domain;

/**
 * 
 */
public class GenericAttribute  {

	protected Feature feature;

	protected Domain domain;
	protected String name;
	protected Object defaultValue;
	protected Object nullValue;

	public boolean nonTranstalable=false;
	public boolean runTime=false;

	public boolean nonDesicion=false;
	
	public static final int OBJECT_NULL_VALUE = -1;

	public GenericAttribute(String n, Domain d, Object nv, Object dv) {
		domain = d;
		name = n;
		defaultValue = dv;
		nullValue = nv;
		
	}

	public GenericAttribute(String n) {
		name = n;
		defaultValue = 0;
		nullValue = 0;
	}
	public Object getNullValue() {
		return nullValue;
	}

	public void setNullValue(Object nullValue) {
		this.nullValue = nullValue;
	}



	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object o) {
		defaultValue = o;
	}

	public Feature getFeature() {
		return feature;
	}

	public String toString() {
		return getFullName();
	}

	public String getFullName() {
		return feature.getName() + "." + name;
	}


	public boolean equals(Object o) {
		boolean b = false;
		if (o instanceof GenericAttribute) {
			GenericAttribute aux = (GenericAttribute) o;
			if (aux.getFeature().equals(feature) && aux.getName().equals(name)) {
				b = true;
			}
		}
		return b;
	}

	public String getName() {
		return this.name;
	}

	public Domain getDomain() {
		return this.domain;
	}

	public void setDomain(Domain domain) {
		this.domain=domain;
	}

	public void setName(String name) {
		this.name=name;
		
	}

}
