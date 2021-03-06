// $ANTLR : "TreeParser.g" -> "FaMaTreeParser.java"$

package es.us.isa.FAMA.parser;    

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import fr.familiar.attributedfm.AttributedFeatureModel;
import fr.familiar.attributedfm.ComplexConstraint;
import fr.familiar.attributedfm.Constraint;
import fr.familiar.attributedfm.ExcludesDependency;
import fr.familiar.attributedfm.Feature;
import fr.familiar.attributedfm.GenericAttribute;
import fr.familiar.attributedfm.Relation;
import fr.familiar.attributedfm.RequiresDependency;
import fr.familiar.attributedfm.domain.Cardinality;
import fr.familiar.attributedfm.domain.Domain;
import fr.familiar.attributedfm.domain.IntegerRange;
import fr.familiar.attributedfm.domain.Range;
import fr.familiar.attributedfm.util.Tree;


public class FaMaTreeParser extends antlr.TreeParser       implements FaMaTreeParserTokenTypes
 {

	//Map<String,AST> mapASTFeatures = null;

	Map<String,Feature> features = new HashMap<String,Feature>();
	//zona de funciones	
	
	FMFParser fmp = new FMFParser();
	
	Collection<String> errors = new LinkedList<String>();
	
	//Creamos una feature con su nombre y dominio
	public Feature createFeature(AST name, Domain d){
		String n = name.getText();
		if (features.get(n) != null){
			//ya existe la feauture, asi que esta duplicada
			errors.add("Duplicated feature detected: "+n);	
		}
		Feature f = new Feature(name.getText());
		f.setDomain(d);
		features.put(name.getText(),f);
		return f;
	}
	
	public Relation createRelation(AST relName, AST card, Collection<Feature> children){
		//TODO en teoria debe funcionar
		Relation res = new Relation(relName.getText());
		Cardinality c = getCardinality(card);
		res.addCardinality(c);
		Iterator<Feature> it = children.iterator();
		while (it.hasNext()){
			Feature f = it.next();
			res.addDestination(f);	
		}
		return res;	
	}
	
	//aadimos a una feature todas sus relaciones
	public void addRelations(Feature f, Collection<Relation> rels){
		Iterator<Relation> it = rels.iterator();
		while (it.hasNext()){
			Relation r = it.next();
			f.addRelation(r);	
			//al aadir la relacion, a esta se le pone como feature padre la feature actual
		}	
	}
	
	public Cardinality getCardinality(AST t){
		String aux = t.getFirstChild().getText();
		int min = Integer.parseInt(aux);
		aux = t.getFirstChild().getNextSibling().getText();
		int max = Integer.parseInt(aux);
		Cardinality res = new Cardinality(min,max);
		return res;	
	}
	
	public TreeParserResult createFeatureModel(Feature root, Collection<Constraint> cons){
		TreeParserResult res;
		AttributedFeatureModel fm = new AttributedFeatureModel(root);
		Iterator<Constraint> it = cons.iterator();
		//TODO
		//aadir metodos a feature model para poder aadir todas las dependencias
		//del tiron, y lo mismo para las relaciones y las features
		while (it.hasNext()){
			Constraint d = it.next();
			fm.addConstraint(d);	
		}
		res = new TreeParserResult(fm,errors);
		return res;
	}
	
	public ExcludesDependency createExcludes(AST relName,AST f1, AST f2){
		//TODO
		Feature feat1 = features.get(f1.getText());
		Feature feat2 = features.get(f2.getText());
		ExcludesDependency res = new ExcludesDependency(relName.getText(),feat1,feat2);
		return res;	
	}
	
	public RequiresDependency createRequires(AST relName,AST f1, AST f2){
		//TODO
		Feature feat1 = features.get(f1.getText());
		Feature feat2 = features.get(f2.getText());
		RequiresDependency res = new RequiresDependency(relName.getText(),feat1,feat2);
		return res;	
	}
	
	public Constraint ASTtoConstraint(AST t, AST name){
		//TODO checkear que funciona bien
		String n = name.getText();
		Tree<String> tree = fmp.astToTree(t);
		Constraint res;
		if (t.getType() == EXCLUDES){
			AST f1 = t.getFirstChild();
			AST f2 = t.getFirstChild().getNextSibling();
			res = createExcludes(name,f1,f2);
		}
		else if (t.getType() == REQUIRES){
			AST f1 = t.getFirstChild();
			AST f2 = t.getFirstChild().getNextSibling();
			res = createRequires(name,f1,f2);
		}
		else{
			res = new ComplexConstraint(tree);
			res.setName(n);
		}
		return res;	
	}
	
	public Domain createEnumeratedDomain(Collection<Object> c){
		Domain d;
		Iterator<Object> it = c.iterator();
		if (it.hasNext()){
			Object aux = it.next();
			if (aux instanceof Integer){
				
				IntegerRange auxDomain = new IntegerRange();
				Integer i = (Integer)aux;
				auxDomain.getItems().add(i);
				while (it.hasNext()){
					aux = it.next();
					if (aux instanceof Integer){
						i = (Integer)aux;
						auxDomain.getItems().add(i);
					}
					else{
						throw new IllegalStateException("Different types on the attribute domain");	
					}
				}
				d = new Domain(auxDomain);
			}
			else{
				ObjectDomain auxDomain = new ObjectDomain();
				auxDomain.addValue(aux);
				while (it.hasNext()){
					aux = it.next();
					auxDomain.addValue(aux);
				}
				d = auxDomain;
			}
		}
		else{
			d = new SetIntegerDomain();
		}
		return d;
	}
	
	public Range createRange(AST min, AST max){
		Collection<Integer> vals= new LinkedList<Integer>();
		vals.add(Integer.parseInt(min.getText()));
		vals.add(Integer.parseInt(max.getText()));
		Range res = new IntegerRange (vals);
		return res;	
	}
	
	public Integer astToInteger(AST t){
		Integer res = new Integer(t.getText());
		return res;	
	}
	
	public Float astToFloat(AST t){
		Float res = new Float(t.getText());
		return res;	
	}
	
	public void addAttributes(Feature f, Collection<GenericAttribute> atts){
		f.addAttributes(atts);
	}
	
	public void addInvariants(Feature f, Collection<Constraint> cons){
		f.addUncheckedInvariants(cons);	
	}
public FaMaTreeParser() {
	tokenNames = _tokenNames;
}

	public final TreeParserResult  entrada(AST _t) throws RecognitionException {
		TreeParserResult res = null;;
		
		AST entrada_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Feature root; Collection<Constraint> cons = new LinkedList<Constraint>();
		
		try {      // for error handling
			AST __t2252 = _t;
			AST tmp1_AST_in = (AST)_t;
			match(_t,FEATURE_MODEL);
			_t = _t.getFirstChild();
			root=seccion_rels(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CONSTRAINTS:
			{
				cons=seccion_cons(_t);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t2252;
			_t = _t.getNextSibling();
			res = createFeatureModel(root,cons);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return res;
	}
	
	public final Feature  seccion_rels(AST _t) throws RecognitionException {
		Feature root = null;;
		
		AST seccion_rels_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2255 = _t;
			AST tmp2_AST_in = (AST)_t;
			match(_t,SECCION_RELACIONES);
			_t = _t.getFirstChild();
			root=feature(_t);
			_t = _retTree;
			_t = __t2255;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return root;
	}
	
	public final Collection<Constraint>  seccion_cons(AST _t) throws RecognitionException {
		Collection<Constraint> res = new LinkedList<Constraint>();;
		
		AST seccion_cons_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Constraint aux;
		
		try {      // for error handling
			AST __t2330 = _t;
			AST tmp3_AST_in = (AST)_t;
			match(_t,CONSTRAINTS);
			_t = _t.getFirstChild();
			{
			_loop2332:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==CONSTRAINT)) {
					aux=constraint(_t);
					_t = _retTree;
					res.add(aux);
				}
				else {
					break _loop2332;
				}
				
			} while (true);
			}
			_t = __t2330;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return res;
	}
	
	public final Feature  feature(AST _t) throws RecognitionException {
		Feature feat = null;;
		
		AST feature_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST f = null;
		Collection<Relation> rels;Collection<GenericAttribute> atts;
		Collection<Constraint> invs;Domain d;
		
		try {      // for error handling
			AST __t2257 = _t;
			AST tmp4_AST_in = (AST)_t;
			match(_t,FEATURE);
			_t = _t.getFirstChild();
			f = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			d=dom(_t);
			_t = _retTree;
			feat = createFeature(f,d);
			atts=atributos(_t);
			_t = _retTree;
			addAttributes(feat,atts);
			rels=relaciones(_t);
			_t = _retTree;
			addRelations(feat,rels);
			invs=invariantes(_t);
			_t = _retTree;
			addInvariants(feat,invs);
			_t = __t2257;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return feat;
	}
	
	public final Domain  dom(AST _t) throws RecognitionException {
		Domain d = new Domain();;
		
		AST dom_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST min = null;
		AST max = null;
		Range r;
		
		try {      // for error handling
			AST __t2259 = _t;
			AST tmp5_AST_in = (AST)_t;
			match(_t,DOMINIO);
			_t = _t.getFirstChild();
			min = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			max = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			r = createRange(min,max);
				d.addRange(r);
			_t = __t2259;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return d;
	}
	
	public final Collection<GenericAttribute>  atributos(AST _t) throws RecognitionException {
		Collection<GenericAttribute> atts = new LinkedList<GenericAttribute>();
		
		AST atributos_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		GenericAttribute aux;
		
		try {      // for error handling
			AST __t2261 = _t;
			AST tmp6_AST_in = (AST)_t;
			match(_t,ATRIBUTOS);
			_t = _t.getFirstChild();
			{
			_loop2263:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==ATRIBUTO)) {
					aux=atributo(_t);
					_t = _retTree;
					atts.add(aux);
				}
				else {
					break _loop2263;
				}
				
			} while (true);
			}
			_t = __t2261;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return atts;
	}
	
	public final Collection<Relation>  relaciones(AST _t) throws RecognitionException {
		Collection<Relation> rels = new LinkedList<Relation>();;
		
		AST relaciones_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Relation aux;
		
		try {      // for error handling
			AST __t2289 = _t;
			AST tmp7_AST_in = (AST)_t;
			match(_t,RELACIONES);
			_t = _t.getFirstChild();
			{
			_loop2291:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==RELACION)) {
					aux=relacion(_t);
					_t = _retTree;
					rels.add(aux);
				}
				else {
					break _loop2291;
				}
				
			} while (true);
			}
			_t = __t2289;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return rels;
	}
	
	public final Collection<Constraint>  invariantes(AST _t) throws RecognitionException {
		Collection<Constraint> invs = new LinkedList<Constraint>();;
		
		AST invariantes_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Constraint aux;
		
		try {      // for error handling
			AST __t2295 = _t;
			AST tmp8_AST_in = (AST)_t;
			match(_t,INVARIANTES);
			_t = _t.getFirstChild();
			{
			_loop2297:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==CONSTRAINT)) {
					aux=constraint(_t);
					_t = _retTree;
					invs.add(aux);
				}
				else {
					break _loop2297;
				}
				
			} while (true);
			}
			_t = __t2295;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return invs;
	}
	
	public final GenericAttribute  atributo(AST _t) throws RecognitionException {
		GenericAttribute att = null;;
		
		AST atributo_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST n = null;
		Domain d;Object defVal, nullVal;
		
		try {      // for error handling
			AST __t2265 = _t;
			AST tmp9_AST_in = (AST)_t;
			match(_t,ATRIBUTO);
			_t = _t.getFirstChild();
			n = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			d=dominio_att(_t);
			_t = _retTree;
			defVal=default_value(_t);
			_t = _retTree;
			nullVal=null_value(_t);
			_t = _retTree;
			_t = __t2265;
			_t = _t.getNextSibling();
			att = new GenericAttribute(n.getText(),d,nullVal,defVal);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return att;
	}
	
	public final Domain  dominio_att(AST _t) throws RecognitionException {
		Domain d = null;;
		
		AST dominio_att_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2267 = _t;
			AST tmp10_AST_in = (AST)_t;
			match(_t,DOMINIO);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case INTEGER:
			{
				d=dominio_rango(_t);
				_t = _retTree;
				break;
			}
			case ENUM:
			{
				d=dominio_enumerado(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t2267;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return d;
	}
	
	public final Object  default_value(AST _t) throws RecognitionException {
		Object o = null;;
		
		AST default_value_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2283 = _t;
			AST tmp11_AST_in = (AST)_t;
			match(_t,DEF_VALUE);
			_t = _t.getFirstChild();
			o=valor(_t);
			_t = _retTree;
			_t = __t2283;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return o;
	}
	
	public final Object  null_value(AST _t) throws RecognitionException {
		Object o = null;;
		
		AST null_value_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2285 = _t;
			AST tmp12_AST_in = (AST)_t;
			match(_t,NULL_VALUE);
			_t = _t.getFirstChild();
			o=valor(_t);
			_t = _retTree;
			_t = __t2285;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return o;
	}
	
	public final Domain  dominio_rango(AST _t) throws RecognitionException {
		Domain d = null;
		
		AST dominio_rango_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Collection<Range> ranges;
		
		try {      // for error handling
			AST __t2270 = _t;
			AST tmp13_AST_in = (AST)_t;
			match(_t,INTEGER);
			_t = _t.getFirstChild();
			ranges=rangos(_t);
			_t = _retTree;
			_t = __t2270;
			_t = _t.getNextSibling();
			
			d = new Domain();
			d.ranges=ranges;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return d;
	}
	
	public final Domain  dominio_enumerado(AST _t) throws RecognitionException {
		Domain d = null;;
		
		AST dominio_enumerado_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Collection<Object> c = new LinkedList<Object>();Object aux;
		
		try {      // for error handling
			AST __t2278 = _t;
			AST tmp14_AST_in = (AST)_t;
			match(_t,ENUM);
			_t = _t.getFirstChild();
			AST __t2279 = _t;
			AST tmp15_AST_in = (AST)_t;
			match(_t,VALORES);
			_t = _t.getFirstChild();
			{
			int _cnt2281=0;
			_loop2281:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LIT_ENTERO||_t.getType()==LIT_REAL||_t.getType()==LIT_STRING)) {
					aux=valor(_t);
					_t = _retTree;
					c.add(aux);
				}
				else {
					if ( _cnt2281>=1 ) { break _loop2281; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt2281++;
			} while (true);
			}
			_t = __t2279;
			_t = _t.getNextSibling();
			_t = __t2278;
			_t = _t.getNextSibling();
			d = createEnumeratedDomain(c);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return d;
	}
	
	public final Collection<Range>  rangos(AST _t) throws RecognitionException {
		Collection<Range> ranges = new HashSet<Range>();;
		
		AST rangos_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Range aux = null;
		
		try {      // for error handling
			AST __t2272 = _t;
			AST tmp16_AST_in = (AST)_t;
			match(_t,RANGOS);
			_t = _t.getFirstChild();
			{
			int _cnt2274=0;
			_loop2274:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==RANGO)) {
					aux=rango(_t);
					_t = _retTree;
					ranges.add(aux);
				}
				else {
					if ( _cnt2274>=1 ) { break _loop2274; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt2274++;
			} while (true);
			}
			_t = __t2272;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return ranges;
	}
	
	public final Range  rango(AST _t) throws RecognitionException {
		Range r = null;;
		
		AST rango_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST min = null;
		AST max = null;
		
		try {      // for error handling
			AST __t2276 = _t;
			AST tmp17_AST_in = (AST)_t;
			match(_t,RANGO);
			_t = _t.getFirstChild();
			min = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			max = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			_t = __t2276;
			_t = _t.getNextSibling();
			r = createRange(min,max);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final Object  valor(AST _t) throws RecognitionException {
		Object o = null;;
		
		AST valor_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST i = null;
		AST r = null;
		AST s = null;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LIT_ENTERO:
			{
				i = (AST)_t;
				match(_t,LIT_ENTERO);
				_t = _t.getNextSibling();
				o = astToInteger(i);
				break;
			}
			case LIT_REAL:
			{
				r = (AST)_t;
				match(_t,LIT_REAL);
				_t = _t.getNextSibling();
				o = astToFloat(r);
				break;
			}
			case LIT_STRING:
			{
				s = (AST)_t;
				match(_t,LIT_STRING);
				_t = _t.getNextSibling();
				o = s.getText();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return o;
	}
	
	public final Relation  relacion(AST _t) throws RecognitionException {
		Relation r = null;;
		
		AST relacion_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST n = null;
		AST c = null;
		Collection<Feature> children;
		
		try {      // for error handling
			AST __t2293 = _t;
			AST tmp18_AST_in = (AST)_t;
			match(_t,RELACION);
			_t = _t.getFirstChild();
			n = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			c = _t==ASTNULL ? null : (AST)_t;
			card(_t);
			_t = _retTree;
			children=features(_t);
			_t = _retTree;
			_t = __t2293;
			_t = _t.getNextSibling();
			r = createRelation(n,c,children);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final void card(AST _t) throws RecognitionException {
		
		AST card_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2328 = _t;
			AST tmp19_AST_in = (AST)_t;
			match(_t,CARDINALIDAD);
			_t = _t.getFirstChild();
			AST tmp20_AST_in = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			AST tmp21_AST_in = (AST)_t;
			match(_t,LIT_ENTERO);
			_t = _t.getNextSibling();
			_t = __t2328;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final Collection<Feature>  features(AST _t) throws RecognitionException {
		Collection<Feature> feats = new LinkedList<Feature>();;
		
		AST features_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		Feature aux;
		
		try {      // for error handling
			AST __t2324 = _t;
			AST tmp22_AST_in = (AST)_t;
			match(_t,FEATURES);
			_t = _t.getFirstChild();
			{
			_loop2326:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==FEATURE)) {
					aux=feature(_t);
					_t = _retTree;
					feats.add(aux);
				}
				else {
					break _loop2326;
				}
				
			} while (true);
			}
			_t = __t2324;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return feats;
	}
	
	public final Constraint  constraint(AST _t) throws RecognitionException {
		Constraint c = null;;
		
		AST constraint_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST n = null;
		AST e = null;
		
		try {      // for error handling
			AST __t2299 = _t;
			AST tmp23_AST_in = (AST)_t;
			match(_t,CONSTRAINT);
			_t = _t.getFirstChild();
			n = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			e = _t==ASTNULL ? null : (AST)_t;
			expresion(_t);
			_t = _retTree;
			c = ASTtoConstraint(e,n);
			_t = __t2299;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return c;
	}
	
	public final void expresion(AST _t) throws RecognitionException {
		
		AST expresion_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IFF:
			{
				AST __t2301 = _t;
				AST tmp24_AST_in = (AST)_t;
				match(_t,IFF);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2301;
				_t = _t.getNextSibling();
				break;
			}
			case IMPLIES:
			{
				AST __t2302 = _t;
				AST tmp25_AST_in = (AST)_t;
				match(_t,IMPLIES);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2302;
				_t = _t.getNextSibling();
				break;
			}
			case EXCLUDES:
			{
				AST __t2303 = _t;
				AST tmp26_AST_in = (AST)_t;
				match(_t,EXCLUDES);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2303;
				_t = _t.getNextSibling();
				break;
			}
			case REQUIRES:
			{
				AST __t2304 = _t;
				AST tmp27_AST_in = (AST)_t;
				match(_t,REQUIRES);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2304;
				_t = _t.getNextSibling();
				break;
			}
			case OR:
			{
				AST __t2305 = _t;
				AST tmp28_AST_in = (AST)_t;
				match(_t,OR);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2305;
				_t = _t.getNextSibling();
				break;
			}
			case AND:
			{
				AST __t2306 = _t;
				AST tmp29_AST_in = (AST)_t;
				match(_t,AND);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2306;
				_t = _t.getNextSibling();
				break;
			}
			case NOT:
			{
				AST __t2307 = _t;
				AST tmp30_AST_in = (AST)_t;
				match(_t,NOT);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				_t = __t2307;
				_t = _t.getNextSibling();
				break;
			}
			case MAYOR:
			{
				AST __t2308 = _t;
				AST tmp31_AST_in = (AST)_t;
				match(_t,MAYOR);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2308;
				_t = _t.getNextSibling();
				break;
			}
			case MENOR:
			{
				AST __t2309 = _t;
				AST tmp32_AST_in = (AST)_t;
				match(_t,MENOR);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2309;
				_t = _t.getNextSibling();
				break;
			}
			case MAYOR_IGUAL:
			{
				AST __t2310 = _t;
				AST tmp33_AST_in = (AST)_t;
				match(_t,MAYOR_IGUAL);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2310;
				_t = _t.getNextSibling();
				break;
			}
			case MENOR_IGUAL:
			{
				AST __t2311 = _t;
				AST tmp34_AST_in = (AST)_t;
				match(_t,MENOR_IGUAL);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2311;
				_t = _t.getNextSibling();
				break;
			}
			case IGUAL:
			{
				AST __t2312 = _t;
				AST tmp35_AST_in = (AST)_t;
				match(_t,IGUAL);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2312;
				_t = _t.getNextSibling();
				break;
			}
			case DISTINTO:
			{
				AST __t2313 = _t;
				AST tmp36_AST_in = (AST)_t;
				match(_t,DISTINTO);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2313;
				_t = _t.getNextSibling();
				break;
			}
			case MAS:
			{
				AST __t2314 = _t;
				AST tmp37_AST_in = (AST)_t;
				match(_t,MAS);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2314;
				_t = _t.getNextSibling();
				break;
			}
			case MENOS:
			{
				AST __t2315 = _t;
				AST tmp38_AST_in = (AST)_t;
				match(_t,MENOS);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2315;
				_t = _t.getNextSibling();
				break;
			}
			case MULT:
			{
				AST __t2316 = _t;
				AST tmp39_AST_in = (AST)_t;
				match(_t,MULT);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2316;
				_t = _t.getNextSibling();
				break;
			}
			case DIV:
			{
				AST __t2317 = _t;
				AST tmp40_AST_in = (AST)_t;
				match(_t,DIV);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2317;
				_t = _t.getNextSibling();
				break;
			}
			case MOD:
			{
				AST __t2318 = _t;
				AST tmp41_AST_in = (AST)_t;
				match(_t,MOD);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2318;
				_t = _t.getNextSibling();
				break;
			}
			case POW:
			{
				AST __t2319 = _t;
				AST tmp42_AST_in = (AST)_t;
				match(_t,POW);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				expresion(_t);
				_t = _retTree;
				_t = __t2319;
				_t = _t.getNextSibling();
				break;
			}
			case MENOS_UNARIO:
			{
				AST __t2320 = _t;
				AST tmp43_AST_in = (AST)_t;
				match(_t,MENOS_UNARIO);
				_t = _t.getFirstChild();
				expresion(_t);
				_t = _retTree;
				_t = __t2320;
				_t = _t.getNextSibling();
				break;
			}
			case LIT_ENTERO:
			case LIT_REAL:
			case LIT_STRING:
			{
				valor(_t);
				_t = _retTree;
				break;
			}
			case IDENT:
			{
				AST tmp44_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				break;
			}
			case ATRIBUTO:
			{
				id_att(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void id_att(AST _t) throws RecognitionException {
		
		AST id_att_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t2322 = _t;
			AST tmp45_AST_in = (AST)_t;
			match(_t,ATRIBUTO);
			_t = _t.getFirstChild();
			AST tmp46_AST_in = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			AST tmp47_AST_in = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			_t = __t2322;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"FEATURE_MODEL",
		"FEATURE",
		"FEATURES",
		"CONSTRAINTS",
		"CONSTRAINT",
		"DOMINIO",
		"DEF_VALUE",
		"NULL_VALUE",
		"ATRIBUTOS",
		"ATRIBUTO",
		"RELACION",
		"CARDINALIDAD",
		"RELACIONES",
		"INVARIANTES",
		"INVARIANTE",
		"RANGO",
		"LITERAL",
		"RANGOS",
		"VALORES",
		"ENUM",
		"MENOS_UNARIO",
		"SECCION_RELACIONES",
		"DOSPUNTOS",
		"PyC",
		"IDENT",
		"CORCHETE_ABRIR",
		"CORCHETE_CERRAR",
		"LIT_ENTERO",
		"COMA",
		"LLAVE_ABRIR",
		"LLAVE_CERRAR",
		"SECCION_ATRIBUTOS",
		"PUNTO",
		"INTEGER",
		"TO",
		"LIT_REAL",
		"LIT_STRING",
		"SECCION_CONSTRAINTS",
		"EXCLUDES",
		"REQUIRES",
		"IFF",
		"IMPLIES",
		"OR",
		"AND",
		"NOT",
		"MAYOR",
		"MENOR",
		"MAYOR_IGUAL",
		"MENOR_IGUAL",
		"IGUAL",
		"DISTINTO",
		"MAS",
		"MENOS",
		"MULT",
		"DIV",
		"MOD",
		"POW",
		"ABS",
		"SIN",
		"COS",
		"PARENTESIS_ABRIR",
		"PARENTESIS_CERRAR",
		"MAX",
		"MIN",
		"SUM"
	};
	
	}
	
