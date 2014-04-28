package fr.familiar.readers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import fr.inria.lang.LuaConfigFileStandaloneSetup;
import fr.inria.lang.conf.Assignment;
import fr.inria.lang.conf.ConfFactory;
import fr.inria.lang.conf.ConfPackage;
import fr.inria.lang.conf.LuaConfigFile;

public class ConfigurationWriter {
	public String directory = ".";

	public void write(String confname, Collection<Trituple> tpc) {
		int maximumConfigFiles = 10; // this is just an example
		System.out.println("Creating" + maximumConfigFiles
				+ " different configurations");
		// Initialize the model
		ConfPackage.eINSTANCE.eClass();
		// Retrieve the default factory singleton
		ConfFactory luaCFFoctory = ConfFactory.eINSTANCE;
		// create the a Lua configuration
		LuaConfigFile myLuaCF = luaCFFoctory.createLuaConfigFile();
		// create 2 rows, one comment and two assignments as the content of the
		// configuration
		//GeneratedComment myComment = luaCFFoctory.createGeneratedComment();
		//myLuaCF.getRows().add(myComment);

		for (Trituple t:tpc) {
			Assignment myLuaCFAssingment = luaCFFoctory.createAssignment();
			myLuaCFAssingment.setFeature((String) t.a);
			myLuaCFAssingment.setAttribute((String)t.b);
			myLuaCFAssingment.setValue((String)t.c);
			myLuaCF.getRows().add(myLuaCFAssingment);

		}

		Injector luaInjector = new LuaConfigFileStandaloneSetup()
				.createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = luaInjector
				.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL,	Boolean.TRUE);
		Resource luaConfFilesResource = resourceSet.createResource(URI
				.createURI(directory+"/" + confname ));
		luaConfFilesResource.getContents().add(myLuaCF);
		// now save the content.
		try {
			luaConfFilesResource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void writeBF(String confname, Collection<Trituple> tpc) {
		try {
			FileWriter w = new FileWriter(new File(directory+"/" + confname ));
			for (Trituple t:tpc) {

					w.write((String) t.a);
					w.write(".");
					w.write((String)t.b);
					w.write("=");
					w.write((String)t.c);
					w.write("\r\n");
					w.flush();
			}
			w.close();

		} catch (IOException e1) {
			System.err.println("Problem writting the configuration");

			e1.printStackTrace();
		}
		}
}
