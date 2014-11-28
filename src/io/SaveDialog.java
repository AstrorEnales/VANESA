package io;

import fr.lip6.move.pnml.framework.utils.exception.InvalidIDException;
import fr.lip6.move.pnml.framework.utils.exception.VoidRepositoryException;
import gonOutput.GONoutput;
import graph.ContainerSingelton;
import graph.GraphContainer;
import graph.GraphInstance;
import gui.MainWindow;
import gui.MainWindowSingleton;
import io.graphML.SaveGraphML;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

//import ch.qos.logback.classic.LoggerContext;
import moOutput.MOoutput;
import petriNet.PNEdge;
import petriNet.Place;
import petriNet.Transition;
import xmlOutput.sbml.JSBMLoutput;
import xmlOutput.sbml.PNMLOutput;
import xmlOutput.sbml.VAMLoutput;
import biologicalObjects.edges.BiologicalEdgeAbstract;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import configurations.ConnectionSettings;
//import org.slf4j.LoggerFactory;

public class SaveDialog {

	private boolean sbmlBool = true;
	private boolean graphMLBool = true;
	private boolean moBool = true;
	private boolean gonBool = true;
	private boolean vaBool = true;
	private boolean txtBool = true;
	private boolean itxtBool = true;
	private boolean pnmlBool = true;

	private String fileFormat;
	private File file;

	private String sbmlDescription = "System Biology Markup Language (*.sbml)";
	private String sbml = "sbml";
	
	private String pnmlDescription = "Petri Net Markup Language (*.pnml)";
	private String pnml = "pnml";

	private String vamlDescription = "VANESA Markup Language (*.vaml)";
	private String vaml = "vaml";

	private String graphMlDescription = "Graph Markup Language (*.graphml)";
	private String graphMl = "graphml";

	private String moDescription = "Modelica Model (*.mo)";
	private String mo = "mo";

	private String csmlDescription = "Cell Illustrator (*.csml)";
	private String csml = "csml";

	private String txtDescription = "Graph Text File (*.txt)";
	private String txt = "txt";

	private String irinaDescription = "Irina Export File (*.itxt)";
	private String irinaTxt = "itxt";

	private JFileChooser chooser;

	/*
	 * public SaveDialog(boolean sbml) { this(sbml?1:1); }//
	 */
	// use power of 2
	public static int FORMAT_SBML = 1;
	public static int FORMAT_GRAPHML = 2;
	public static int FORMAT_MO = 4;
	public static int FORMAT_GON = 8;
	public static int FORMAT_VA = 16;
	public static int FORMAT_TXT = 32;
	public static int FORMAT_ITXT = 64;
	public static int FORMAT_PNML = 128;

	public SaveDialog(int format) {

		sbmlBool = (format & FORMAT_SBML) == FORMAT_SBML;
		graphMLBool = (format & FORMAT_GRAPHML) == FORMAT_GRAPHML;
		moBool = (format & FORMAT_MO) == FORMAT_MO;
		gonBool = (format & FORMAT_GON) == FORMAT_GON;
		vaBool = (format & FORMAT_VA) == FORMAT_VA;
		txtBool = (format & FORMAT_TXT) == FORMAT_TXT;
		itxtBool = (format & FORMAT_ITXT) == FORMAT_ITXT;
		pnmlBool = (format & FORMAT_PNML) == FORMAT_PNML;

		if (ConnectionSettings.getFileDirectory() != null) {
			chooser = new JFileChooser(ConnectionSettings.getFileDirectory());
		} else {
			chooser = new JFileChooser();
		}

		chooser.setAcceptAllFileFilterUsed(false);

		if (sbmlBool)
			chooser.addChoosableFileFilter(new MyFileFilter(sbml,
					sbmlDescription));

		/*if (itxtBool)
			chooser.addChoosableFileFilter(new MyFileFilter(irinaTxt,
					irinaDescription));*/

		if (graphMLBool)
			chooser.addChoosableFileFilter(new MyFileFilter(graphMl,
					graphMlDescription));

		if (moBool)
			chooser.addChoosableFileFilter(new MyFileFilter(mo, moDescription));

		if (gonBool)
			chooser
					.addChoosableFileFilter(new MyFileFilter(csml,
							csmlDescription));
		if (pnmlBool)
			chooser.addChoosableFileFilter(new MyFileFilter(pnml,
					pnmlDescription));
		if (vaBool)
			chooser.addChoosableFileFilter(new MyFileFilter(vaml,
					vamlDescription));
		if (txtBool)
			chooser
					.addChoosableFileFilter(new MyFileFilter(txt,
							txtDescription));

		int option = chooser.showSaveDialog(MainWindowSingleton.getInstance());
		if (option == JFileChooser.APPROVE_OPTION) {

			fileFormat = chooser.getFileFilter().getDescription();
			file = chooser.getSelectedFile();
			boolean overwrite = true;
			if (file.exists()) {
				int response = JOptionPane.showConfirmDialog(null,
						"Overwrite existing file?", "Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION) {
					overwrite = false;
				}
			}
			if (overwrite) {
				write();
			}

		}
	}

	private void getCorrectFile(String ending) {

		String extension = file.getPath();
		int i = extension.lastIndexOf('.');
		if (i > 0 && i < extension.length() - 1) {
		} else {
			file = new File(file.getAbsolutePath() + "." + ending);
		}

	}

	private void write() {

		ConnectionSettings.setFileDirectory(file.getAbsolutePath());
		if (fileFormat.equals(sbmlDescription)) {
			getCorrectFile(sbml);
			// create a sbmlOutput object
//			SBMLoutputNoWS sbmlOutput = new SBMLoutputNoWS(file, new GraphInstance()
//					.getPathway());
//			//if (sbmlOutput.generateSBMLDocument())
//				JOptionPane.showMessageDialog(
//						MainWindowSingelton.getInstance(), sbmlDescription
//								+ sbmlOutput.generateSBMLDocument());
			JSBMLoutput jsbmlOutput = new JSBMLoutput(file, new GraphInstance().getPathway());
			JOptionPane.showMessageDialog(
					MainWindowSingleton.getInstance(), sbmlDescription
							+ jsbmlOutput.generateSBMLDocument());
				
			//else
			//	JOptionPane.showMessageDialog(
			//			MainWindowSingelton.getInstance(), sbmlDescription
			//					+ " File not saved");
		} else if (fileFormat.equals(graphMlDescription)) {
			getCorrectFile(graphMl);
			new SaveGraphML(file);
			JOptionPane.showMessageDialog(MainWindowSingleton.getInstance(),
					graphMlDescription + " File saved");
		} else if (fileFormat.equals(irinaDescription)) {
			getCorrectFile(irinaTxt);
			new IrinaGraphTextWriter(file, new GraphInstance().getPathway());
			JOptionPane.showMessageDialog(MainWindowSingleton.getInstance(),
					irinaDescription + " File saved");

		} else if (fileFormat.equals(moDescription)) {
			getCorrectFile(mo);
			new MOoutput(file, new GraphInstance().getPathway());
			JOptionPane.showMessageDialog(MainWindowSingleton.getInstance(),
					moDescription + " File saved");

		} else if (fileFormat.equals(txtDescription)) {
			getCorrectFile(txt);
			new GraphTextWriter(file, new GraphInstance().getPathway());
			JOptionPane.showMessageDialog(MainWindowSingleton.getInstance(),
					txtDescription + " File saved");

		} else if (fileFormat.equals(pnmlDescription)) {
			GraphContainer con = ContainerSingelton.getInstance();
			MainWindow w = MainWindowSingleton.getInstance();
			if (!con.isPetriView()){
				/*ConvertToPetriNet convertToPetriNet = new ConvertToPetriNet();
				getCorrectFile(pnml);
				PNMLOutput pnmlOutput = new PNMLOutput(file, convertToPetriNet.getBiologicalEdges(),convertToPetriNet.getBiologicalNodes(),convertToPetriNet.getBiologicalTransitions());
				
				try {
					JOptionPane.showMessageDialog(
							MainWindowSingelton.getInstance(),pnmlDescription
									+ pnmlOutput.generatePNMLDocument() + " File saved");
				} catch (HeadlessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidIDException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VoidRepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			} else {
				
				getCorrectFile(pnml);
				BiologicalEdgeAbstract bea;
				ArrayList <PNEdge> edgeList = new ArrayList <PNEdge>();
				Iterator<BiologicalEdgeAbstract> itEdge = con.getPathway(w.getCurrentPathway()).getAllEdges().iterator();
				while (itEdge.hasNext()) {
					bea = itEdge.next();
					if(bea instanceof PNEdge){
					edgeList.add((PNEdge)bea);
					}

				}
				BiologicalNodeAbstract bna;
				ArrayList <Place> nodeList = new ArrayList <Place>();
				ArrayList <Transition> transitionList = new ArrayList <Transition>();
				Iterator<BiologicalNodeAbstract> itNode = con.getPathway(w.getCurrentPathway()).getAllNodes().iterator();
			
				while (itNode.hasNext()) {
					bna = itNode.next();
					if(bna instanceof Place){
						nodeList.add((Place)bna);
					} else if(bna instanceof Transition) {
						 transitionList.add((Transition)bna);
					}
				}

				PNMLOutput pnmlOutput = new PNMLOutput(file, edgeList, nodeList,transitionList);
				
				try {
					JOptionPane.showMessageDialog(
							MainWindowSingleton.getInstance(),pnmlDescription
									+ pnmlOutput.generatePNMLDocument() + "File saved. File validated and saved.");
					
					if(pnmlOutput.getFinished()){
						//LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
						//loggerContext.stop();
					}
					
				} catch (HeadlessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidIDException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VoidRepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} 
		}else if (fileFormat.equals(csmlDescription)) {
			getCorrectFile(csml);
			new GONoutput(file, new GraphInstance().getPathway());
			JOptionPane.showMessageDialog(MainWindowSingleton.getInstance(),
					csmlDescription + " File saved");

		} else if (fileFormat.equals(vamlDescription)) {
			getCorrectFile(vaml);
			try {
				new VAMLoutput(file, new GraphInstance().getPathway());
				JOptionPane.showMessageDialog(
						MainWindowSingleton.getInstance(), vamlDescription
								+ " File saved");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
