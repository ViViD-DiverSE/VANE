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

package es.us.isa.FAMA.parser;

import fr.familiar.attributedfm.AttributedFeatureModel;

import java.util.Collection;

public class TreeParserResult {

	private AttributedFeatureModel afm;
	
	private Collection<String> errors;
	
	public TreeParserResult(AttributedFeatureModel arg0, Collection<String> arg1){
		afm = arg0;
		errors = arg1;
	}

	public AttributedFeatureModel getAfm() {
		return afm;
	}

	public Collection<String> getErrors() {
		return errors;
	}
	
}
