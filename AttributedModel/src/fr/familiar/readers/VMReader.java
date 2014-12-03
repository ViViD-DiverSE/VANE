package fr.familiar.readers;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import es.us.isa.FAMA.models.variabilityModel.parsers.IReader;
import fr.familiar.attributedfm.AttributedFeatureModel;
import fr.familiar.attributedfm.ComplexConstraint;
import fr.familiar.attributedfm.GenericAttribute;
import fr.familiar.attributedfm.domain.BooleanRange;
import fr.familiar.attributedfm.domain.Cardinality;
import fr.familiar.attributedfm.domain.Domain;
import fr.familiar.attributedfm.domain.IntegerRange;
import fr.familiar.attributedfm.domain.KeyWords;
import fr.familiar.attributedfm.domain.Range;
import fr.familiar.attributedfm.domain.RealRange;
import fr.familiar.attributedfm.domain.StringRange;
import fr.familiar.attributedfm.util.Node;
import fr.familiar.attributedfm.util.Tree;
import fr.inria.lang.VMStandaloneSetup;
import fr.inria.lang.vM.And;
import fr.inria.lang.vM.AttrDef;
import fr.inria.lang.vM.Attributes;
import fr.inria.lang.vM.BasicAttrDef;
import fr.inria.lang.vM.BiImplication;
import fr.inria.lang.vM.BooleanAttrDef;
import fr.inria.lang.vM.BooleanExpression;
import fr.inria.lang.vM.BrackedExpression;
import fr.inria.lang.vM.ComplexExpression;
import fr.inria.lang.vM.Constraint;
import fr.inria.lang.vM.Constraints;
import fr.inria.lang.vM.Division;
import fr.inria.lang.vM.EnumIntegerDef;
import fr.inria.lang.vM.EnumRealDef;
import fr.inria.lang.vM.EnumStringDef;
import fr.inria.lang.vM.Equality;
import fr.inria.lang.vM.Excludes;
import fr.inria.lang.vM.Feature;
import fr.inria.lang.vM.FeatureDefinition;
import fr.inria.lang.vM.FeatureHierarchy;
import fr.inria.lang.vM.FeaturesGroup;
import fr.inria.lang.vM.Greater;
import fr.inria.lang.vM.Greaterequal;
import fr.inria.lang.vM.Inequality;
import fr.inria.lang.vM.IntegerAttrDefBounded;
import fr.inria.lang.vM.IntegerAttrDefComplement;
import fr.inria.lang.vM.IntegerAttrDefUnbounded;
import fr.inria.lang.vM.LeftImplication;
import fr.inria.lang.vM.Less;
import fr.inria.lang.vM.Lessequal;
import fr.inria.lang.vM.Minus;
import fr.inria.lang.vM.Model;
import fr.inria.lang.vM.Multiplication;
import fr.inria.lang.vM.NumericExpression;
import fr.inria.lang.vM.Or;
import fr.inria.lang.vM.Orgroup;
import fr.inria.lang.vM.PackageDeclaration;
import fr.inria.lang.vM.Plus;
import fr.inria.lang.vM.PrimitiveExpression;
import fr.inria.lang.vM.RealAttrDefBounded;
import fr.inria.lang.vM.RealAttrDefComplement;
import fr.inria.lang.vM.RealAttrDefUnbounded;
import fr.inria.lang.vM.Relationships;
import fr.inria.lang.vM.Requires;
import fr.inria.lang.vM.RightImplication;
import fr.inria.lang.vM.StringAttrDef;
import fr.inria.lang.vM.StringExpression;
import fr.inria.lang.vM.VmBlock;
import fr.inria.lang.vM.Xorgroup;



public class VMReader implements IReader {

	@Override
	public AttributedFeatureModel parseFile(String fileName) throws Exception {
		Injector injector = new VMStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL,	Boolean.TRUE);
		Resource resource = resourceSet.getResource(URI.createURI(fileName),
				true);
		Model model = (Model) resource.getContents().get(0);
		
		fr.familiar.attributedfm.AttributedFeatureModel fm = new AttributedFeatureModel();

		VmBlock relationships = null;
		VmBlock attsblock =null;
		VmBlock constratins=null;
		
		EList<VmBlock> bl = model.getBlocks();
		for(VmBlock block:bl){
			if (block instanceof Relationships) {
				relationships=block;
			}else if(block instanceof Attributes){
				attsblock=block;
			}else if(block instanceof Constraints){
				constratins=block;
			}else if(block instanceof PackageDeclaration){
				for(VmBlock blockinpkg :((PackageDeclaration) block).getBlocks()){
					if (blockinpkg instanceof Relationships) {
						relationships=blockinpkg;
					}else if(blockinpkg instanceof Attributes){
						attsblock=blockinpkg;
						
					}else if(blockinpkg instanceof Constraints){
						constratins=blockinpkg;
						
						}
				}
			}
		}
		FeatureHierarchy fhs= ((Relationships) relationships).getRoot();
		fr.familiar.attributedfm.Feature ffeat = new fr.familiar.attributedfm.Feature(fhs.getParent().getName());
		visitFeatureHierarchy(ffeat, fhs);
		fm.setRoot(ffeat);
		visitAttributes(((Attributes) attsblock).getAttrDefs(), fm);
		visitConstraints(((Constraints) constratins).getConstraints(),fm);

		return fm;
	}
	private Node<String> traverseConstraint(ComplexExpression cex){
				Node<String> node= new Node<String>();
				if (cex instanceof RightImplication) {
					node.setData(KeyWords.IMPLIES);
					node.addChild(traverseConstraint(((RightImplication) cex).getLeft()));
					//System.out.println("->");
					node.addChild(traverseConstraint(((RightImplication) cex).getRight()));
				} else if (cex instanceof LeftImplication) {
					node.setData(KeyWords.IMPLIES);
					//Damos la vuelta... adding first right
					node.addChild(traverseConstraint(((LeftImplication) cex).getRight()));
					node.addChild(traverseConstraint(((LeftImplication) cex).getLeft()));
					System.out.println("<-");
					
				} else if (cex instanceof BiImplication) {
					node.setData(KeyWords.IFF);
					node.addChild(traverseConstraint(((BiImplication) cex).getLeft()));
					//System.out.println("<->");
					node.addChild(traverseConstraint(((BiImplication) cex).getRight()));
				} else if (cex instanceof Or) {
					node.setData(KeyWords.OR);
					node.addChild(traverseConstraint(((Or) cex).getLeft()));
					//System.out.println("||");
					node.addChild(traverseConstraint(((Or) cex).getRight()));
				} else if (cex instanceof And) {
					node.setData(KeyWords.AND);
					node.addChild(traverseConstraint(((And) cex).getLeft()));
					//System.out.println("&&");
					node.addChild(traverseConstraint(((And) cex).getRight()));
				} else if (cex instanceof Equality) {
					node.setData(KeyWords.EQUAL);
					node.addChild(traverseConstraint(((Equality) cex).getLeft()));
					//System.out.println("==");
					node.addChild(traverseConstraint(((Equality) cex).getRight()));
				} else if (cex instanceof Inequality) {
					node.setData(KeyWords.NON_EQUAL);
					node.addChild(traverseConstraint(((Inequality) cex).getLeft()));
					//System.out.println("!=");
					node.addChild(traverseConstraint(((Inequality) cex).getRight()));
				} else if (cex instanceof Less) {
					node.setData(KeyWords.LESS);
					node.addChild(traverseConstraint(((Less) cex).getLeft()));
					//System.out.println("<<");
					node.addChild(traverseConstraint(((Less) cex).getRight()));
				} else if (cex instanceof Lessequal) {
					node.setData(KeyWords.LESS_EQUAL);
					node.addChild(traverseConstraint(((Lessequal) cex).getLeft()));
					//System.out.println("<=");
					node.addChild(traverseConstraint(((Lessequal) cex).getRight()));
				} else if (cex instanceof Greater) {
					node.setData(KeyWords.GREATER);
					node.addChild(traverseConstraint(((Greater) cex).getLeft()));
					//System.out.println(">>");
					node.addChild(traverseConstraint(((Greater) cex).getRight()));
				} else if (cex instanceof Greaterequal) {
					node.setData(KeyWords.GREATER_EQUAL);
					node.addChild(traverseConstraint(((Greaterequal) cex).getLeft()));
					//System.out.println(">=");
					node.addChild(traverseConstraint(((Greaterequal) cex).getRight()));
				} else if (cex instanceof Plus) {
					node.setData(KeyWords.PLUS);
					node.addChild(traverseConstraint(((Plus) cex).getLeft()));
					//System.out.println("+");
					node.addChild(traverseConstraint(((Plus) cex).getRight()));
				} else if (cex instanceof Minus) {
					node.setData(KeyWords.MINUS);
					node.addChild(traverseConstraint(((Minus) cex).getLeft()));
					//System.out.println("-");
					node.addChild(traverseConstraint(((Minus) cex).getRight()));
				} else if (cex instanceof Multiplication) {
					node.setData(KeyWords.MULT);
					node.addChild(traverseConstraint(((Multiplication) cex).getLeft()));
					//System.out.println("*");
					node.addChild(traverseConstraint(((Multiplication) cex).getRight()));
				} else if (cex instanceof Division) {
					node.setData(KeyWords.DIV);
					node.addChild(traverseConstraint(((Division) cex).getLeft()));
					//System.out.println("/");
					node.addChild(traverseConstraint(((Division) cex).getRight()));
				} else if (cex instanceof Excludes) {
					node.setData(KeyWords.EXCLUDES);
					node.addChild(traverseConstraint(((Excludes) cex).getLeft()));
					//System.out.println("excludes");
					node.addChild(traverseConstraint(((Excludes) cex).getRight()));
				} else if (cex instanceof Requires) {
					node.setData(KeyWords.REQUIRES);
					node.addChild(traverseConstraint(((Requires) cex).getLeft()));
					//System.out.println("requires");
					node.addChild(traverseConstraint(((Requires) cex).getRight()));
				// These are terminals
				} else if(cex instanceof NumericExpression){
					node.setData(((NumericExpression) cex).getValue());

					//System.out.println(((NumericExpression) cex).getValue());
				} else if(cex instanceof BooleanExpression){
					//System.out.println(((BooleanExpression) cex).getValue());
					node.setData(((BooleanExpression) cex).getValue());
				} else if(cex instanceof BrackedExpression){
					//System.out.println("(");
					node=traverseConstraint(((BrackedExpression) cex).getExpression());
					//System.out.println(")");
				} else if(cex instanceof StringExpression){
					node.setData(((StringExpression) cex).getValue());

					//System.out.println(((StringExpression) cex).getValue());
				} else if(cex instanceof PrimitiveExpression){
					//this is a feature
					if(((PrimitiveExpression) cex).getFeatureID()!=null){
						node.setData(((PrimitiveExpression) cex).getFeatureID().getName());
						//System.out.println(((PrimitiveExpression) cex).getFeatureID().getName());

					}
					//This is an attribute
					if(((PrimitiveExpression) cex).getRefAtt()!=null){
						//error
						node.setData(KeyWords.ATTRIBUTE);
						node.addChild(new Node<String>(((PrimitiveExpression) cex).getRefAtt().getHead().getOwnedFeature().getName()));
						node.addChild(new Node<String>(((PrimitiveExpression) cex).getRefAtt().getName()));
						//System.out.println(((PrimitiveExpression) cex).getRefAtt().getHead().getOwnedFeature().getName());
						//System.out.println('.'+((PrimitiveExpression) cex).getRefAtt().getName());
					}
				} else{
				//	System.out.println(cex);
				}
				return node;
	}
	private void visitConstraints(EList<Constraint> con, AttributedFeatureModel fm) {
		for (Constraint co : con) {
			Tree<String> ast =new Tree<String>();
			Node<String> root=traverseConstraint((ComplexExpression) co.getExpression());
			ast.setRootElement(root);
			ComplexConstraint ccons = new ComplexConstraint(ast);
			fm.addConstraint(ccons);
		}
	}

	private void visitAttributes(EList<AttrDef> att, AttributedFeatureModel fm) {
		
		for (AttrDef atdef : att) {
			GenericAttribute attribute= new GenericAttribute("");
			
			attribute.nonTranstalable=atdef.isNotTranslatable();
			attribute.runTime=atdef.isRunTime();
			attribute.nonDesicion=atdef.isNotDecidable();
			BasicAttrDef at = atdef.getBasicAttrDef();
			if (at instanceof BooleanAttrDef) {
				
				String name= ((BooleanAttrDef) at).getName().getName();
				
				Boolean val = Boolean.parseBoolean(((BooleanAttrDef) at).getValue());
				if(((BooleanAttrDef) at).getDefault()!=null){
					Boolean defaultvalue=Boolean.parseBoolean(((BooleanAttrDef) at).getDefault().getValue());
					attribute.setDefaultValue(defaultvalue);
				}
				
				if(val!=null){
					//tiene 2 valores
					attribute.setDomain(new Domain(new BooleanRange()));
				}else{
					Collection<Integer> values= new LinkedList<Integer>();
					if(val==true){values.add(1);}else{values.add(0);}
					Domain domain = new Domain(new IntegerRange(values));
				
				}
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((BooleanAttrDef) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if (at instanceof StringAttrDef) {
				String name= ((StringAttrDef) at).getName().getName();
				String val = ((StringAttrDef) at).getValue();
				if(((StringAttrDef) at).getDefault()!=null){
					String defaultvalue=((StringAttrDef) at).getDefault().getValue();
					attribute.setDefaultValue(defaultvalue);
				}
				StringRange rang= new StringRange();
				rang.addString(val);
				Domain domain= new Domain(rang);
				attribute.setDomain(domain);
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((StringAttrDef) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if (at instanceof IntegerAttrDefBounded) {
				//bounded can have deltas and multiples ranges, thus, will always use a set integerDomain
				
				String name= ((IntegerAttrDefBounded) at).getName().getName();
				if(((IntegerAttrDefBounded) at).getDefault()!=null){
					Integer defaultvalue=((IntegerAttrDefBounded) at).getDefault().getValue();
					attribute.setDefaultValue(defaultvalue);
				}
				
				Set<Integer> vals= new HashSet<Integer>();
				EList<IntegerAttrDefComplement> complements = ((IntegerAttrDefBounded) at).getComplements();
				Range range = new IntegerRange();
				for(IntegerAttrDefComplement complement:complements){
					int delta=1;
					if(complement.getDelta()!=null){
						delta=complement.getDelta().getValue();
					}
					Integer max=Integer.parseInt(complement.getMax());
					Integer min= Integer.parseInt(complement.getMin());
					//Add the values to the set
					for(int i=min;i<=max;i+=delta){
						range.getItems().add(new Integer(i));
					}
				}
				
				//Domain domain= new SetIntegerDomain(vals);
				Domain domain = new Domain(range);
				attribute.setDomain(domain);
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((IntegerAttrDefBounded) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if (at instanceof IntegerAttrDefUnbounded) {
				
				String name= ((IntegerAttrDefUnbounded) at).getName().getName();
				//Integer val =Integer.parseInt(((IntegerAttrDefUnbounded) at).getValue());
				if(((IntegerAttrDefUnbounded) at).getDefault()!=null){
					Integer defaultvalue=((IntegerAttrDefUnbounded) at).getDefault().getValue();
					attribute.setDefaultValue(defaultvalue);
				}
				
				Range range = new IntegerRange();
				range.getItems().add(Integer.MIN_VALUE);
				range.getItems().add(Integer.MAX_VALUE);
				Domain domain= new Domain(range);
				attribute.setDomain(domain);
				//attribute.setValue(val);
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((IntegerAttrDefUnbounded) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if (at instanceof RealAttrDefBounded) {
				String name= ((RealAttrDefBounded) at).getName().getName();
				if(((RealAttrDefBounded) at).getDefault()!=null){
					Float defaultvalue=Float.parseFloat(((RealAttrDefBounded) at).getDefault().getValue());
					attribute.setDefaultValue(defaultvalue);
				}
				
				//Set<Float> vals= new HashSet<Float>();
				Collection<Range> ranges= new LinkedList<Range>();
				EList<RealAttrDefComplement> complements = ((RealAttrDefBounded) at).getComplement();
				for(RealAttrDefComplement complement:complements){
					Float max=Float.parseFloat(complement.getMax());
					Float min= Float.parseFloat(complement.getMin());
					Range r= null;
					Collection<Float> floats = new LinkedList<Float>();
					if(complement.getDelta()!=null){
						float delta=Float.parseFloat(complement.getDelta().getValue());
						//a set of values
						//Add the values to the set
						for(float i=min;i<=max;i+=delta){
							floats.add(i);
						}	
					}else{
						//max min
						floats.add(max);
						floats.add(min);
					}
					
					ranges.add(new RealRange(floats));
					
				}
				
				//Domain domain= new SetRealDomain(vals);
				
				Domain domain = new Domain();
				domain.ranges=ranges;
				attribute.setDomain(domain);
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((RealAttrDefBounded) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if (at instanceof RealAttrDefUnbounded) {
				
				String name= ((RealAttrDefUnbounded) at).getName().getName();
				Float val =Float.parseFloat(((RealAttrDefUnbounded) at).getValue());
				if(((RealAttrDefUnbounded) at).getDefault()!=null){
					Float defaultvalue=Float.parseFloat(((RealAttrDefUnbounded) at).getDefault().getValue());
					attribute.setDefaultValue(defaultvalue);
				}
				Collection<Float> floats= new LinkedList<Float>();
				floats.add(val);
				floats.add(Float.MAX_VALUE) ;floats.add(Float.MAX_VALUE);
				RealRange range = new RealRange(floats);
				Domain domain= new Domain();
				domain.addRange(range);
				attribute.setDomain(domain);
				//attribute.setValue(val);
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((RealAttrDefUnbounded) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if(at instanceof EnumStringDef){
				String name= ((EnumIntegerDef) at).getName().getName();
				if(((EnumStringDef) at).getDefault()!=null){
					String defaultvalue=((EnumStringDef) at).getDefault().getValue();
					attribute.setDefaultValue(defaultvalue);
				}
				
				Collection<String> vals= new ArrayList<String>();
				EList<String> values = ((EnumIntegerDef) at).getValue();
				for(String value:values){
					vals.add(value);
				}
				
				Range range= new StringRange(vals);
				attribute.setDomain(new Domain(range));
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((EnumIntegerDef) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if(at instanceof EnumIntegerDef){
				String name= ((EnumIntegerDef) at).getName().getName();
				if(((EnumIntegerDef) at).getDefault()!=null){
					Integer defaultvalue=((EnumIntegerDef) at).getDefault().getValue();
					attribute.setDefaultValue(defaultvalue);
				}
				
				Set<Integer> vals= new HashSet<Integer>();
				EList<String> values = ((EnumIntegerDef) at).getValue();
				for(String value:values){
					vals.add(Integer.parseInt(value));
				}
				
				Range range= new IntegerRange(vals);
				attribute.setDomain(new Domain(range));
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((EnumIntegerDef) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			} else if(at instanceof EnumRealDef){
				String name= ((EnumRealDef) at).getName().getName();
				if(((EnumRealDef) at).getDefault()!=null){
					Float defaultvalue=Float.parseFloat(((EnumRealDef) at).getDefault().getValue());
					attribute.setDefaultValue(defaultvalue);
				}
				
				Set<Float> vals= new HashSet<Float>();
				EList<String> values = ((EnumIntegerDef) at).getValue();
				for(String value:values){
					vals.add(Float.parseFloat(value));
				}
				
				Range range= new RealRange(vals);
				attribute.setDomain(new Domain(range));
				attribute.setName(name);
				fr.familiar.attributedfm.Feature searchFeatureByName = fm.searchFeatureByName(((EnumRealDef) at).getName().getHead().getOwnedFeature().getName());
				searchFeatureByName.addAttribute(attribute);
			}
		

		}
	}

	private void visitFeatureHierarchy(fr.familiar.attributedfm.Feature ffeatroot,	FeatureHierarchy fh) {
		EList<FeatureDefinition> fhchildren = fh.getChildren();
		for (FeatureDefinition fd : fhchildren) {

			fr.familiar.attributedfm.Relation frel = new fr.familiar.attributedfm.Relation();

			if (fd instanceof Feature) {
				fr.inria.lang.vM.Feature f = (fr.inria.lang.vM.Feature) fd;
				if (f.isOptional()) {
					frel.addCardinality(new Cardinality(0, 1));
				} else if (!f.isOptional()) {// isMandatory
					frel.addCardinality(new Cardinality(1, 1));
				}
				frel.addDestination(new fr.familiar.attributedfm.Feature(f.getName()));
				ffeatroot.addRelation(frel);

			} else if (fd instanceof FeaturesGroup) {
				frel.addCardinality(new Cardinality(1, 1));

				FeaturesGroup group = (FeaturesGroup) fd;
				EList<FeatureDefinition> groupedFeatures = group.getGroupedFeatures();
				int maxCard = 0;
				for (FeatureDefinition fdef : groupedFeatures) {
					maxCard++;

					if (fdef instanceof Feature) {
						Feature vmfeature=(Feature) fdef;
						fr.familiar.attributedfm.Feature feat = new fr.familiar.attributedfm.Feature(vmfeature.getName());
						//Adding translation info
						feat.nonTranstalable=vmfeature.isNotTranslatable();
						feat.runTime=vmfeature.isRunTime();
						feat.nonDecision=vmfeature.isNotDecidable();
						//addding range (if not specified =1,1)
						if(vmfeature.getMin()!=null &&vmfeature.getMax()!=null){
							IntegerRange r = new IntegerRange();
							r.getItems().add(vmfeature.getMin());
							r.getItems().add(vmfeature.getMax());
							
							feat.clone.add(r);
						}else{
							IntegerRange r = new IntegerRange();
							r.getItems().add(1);
							feat.clone.add(r);						}
						frel.addDestination(feat);
					} else if (fdef instanceof FeatureHierarchy) {
						Feature vmfeature=((FeatureHierarchy) fdef).getParent();
						fr.familiar.attributedfm.Feature feat = new fr.familiar.attributedfm.Feature(vmfeature.getName());
						//adding translation info
						feat.nonTranstalable=vmfeature.isNotTranslatable();
						feat.runTime=vmfeature.isRunTime();
						if(vmfeature.getMin()!=null &&vmfeature.getMax()!=null){
							IntegerRange r = new IntegerRange();
							r.getItems().add(vmfeature.getMin());
							r.getItems().add(vmfeature.getMax());
							
							feat.clone.add(r);
						}else{
							IntegerRange r = new IntegerRange();
							r.getItems().add(1);
							feat.clone.add(r);
						}
						frel.addDestination(feat);
						visitFeatureHierarchy(feat, (FeatureHierarchy) fdef);
					}
				}
				if (fd instanceof Xorgroup) {
					frel.addCardinality(new Cardinality(1, 1));
				} else if (fd instanceof Orgroup) {
					frel.addCardinality(new Cardinality(1, maxCard));
				}
				ffeatroot.addRelation(frel);

			} else if (fd instanceof FeatureHierarchy) {
				String dest = ((FeatureHierarchy) fd).getParent().getName();
				fr.familiar.attributedfm.Feature fdest = new fr.familiar.attributedfm.Feature(dest);

				frel.addDestination(fdest);
				ffeatroot.addRelation(frel);

				if (((FeatureHierarchy) fd).getParent().isOptional()) {
					frel.addCardinality(new Cardinality(0, 1));

				} else if (!((FeatureHierarchy) fd).getParent().isOptional()) {
					frel.addCardinality(new Cardinality(1, 1));
				}
				visitFeatureHierarchy(fdest, (FeatureHierarchy) fd);
			}
		}
	}

	@Override
	public AttributedFeatureModel parseString(String data) throws Exception {
		return null;
	}

	@Override
	public boolean canParse(String fileName) {
		return false;
	}

}
