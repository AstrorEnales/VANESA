package launch;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;

import biologicalElements.ElementNames;
import biologicalElements.ElementNamesSingleton;
import configurations.ConnectionSettings;
import configurations.ProgramFileLock;
import configurations.ResourceLibrary;
import database.Connection.DBconnection;
import database.brenda.MostWantedMolecules;
import graph.GraphContainer;
import gui.IntroScreen;
import gui.MainWindow;
import gui.MyPopUp;

public class Launch {
	public static String dawis_sessionid = null;

	public static void main(String[] args) {
		// avoid strange awt/swing exceptions:
		// Exception in thread "AWT-EventQueue-0"
		// java.lang.IllegalArgumentException: Comparison method violates its
		// general contract!
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		// set app name for mac osx
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VANESA");

		final IntroScreen intro = new IntroScreen();
		intro.openWindow();

		if (!ProgramFileLock.writeLock()) {
			JOptionPane.showMessageDialog(null, "Another instance of the program is already running!\nExit program.",
					"Exit", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		try {

			Properties props = new Properties();
			props.put("logoString", "VANESA");

			// SubstanceBusinessBlueSteelLookAndFeel lf = new
			// SubstanceBusinessBlueSteelLookAndFeel();
			// lf.setSkin("");
			// workarround to get the menu into the main upper menu pane on mac
			// osx
			// @see http://www.pushing-pixels.org/?p=366
			if (System.getProperty("os.name").startsWith("Mac")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				Object MenuBarUI = UIManager.get("MenuBarUI");
				Object MenuUI = UIManager.get("MenuUI");
				Object MenuItemUI = UIManager.get("MenuItemUI");
				Object CheckBoxMenuItemUI = UIManager.get("CheckBoxMenuItemUI");
				Object RadioButtonMenuItemUI = UIManager.get("RadioButtonMenuItemUI");
				Object PopupMenuUI = UIManager.get("PopupMenuUI");

				UIManager.setLookAndFeel(new AluminiumLookAndFeel());
				UIManager.put("MenuBarUI", MenuBarUI);
				UIManager.put("MenuUI", MenuUI);
				UIManager.put("MenuItemUI", MenuItemUI);
				UIManager.put("CheckBoxMenuItemUI", CheckBoxMenuItemUI);
				UIManager.put("RadioButtonMenuItemUI", RadioButtonMenuItemUI);
				UIManager.put("PopupMenuUI", PopupMenuUI);
				props.put("macStyleWindowDecoration", "on");
				props.put("macStyleScrollBar", "on");

			} else {
				UIManager.setLookAndFeel(new AluminiumLookAndFeel());
			}
			AluminiumLookAndFeel.setCurrentTheme(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("fail");
		}

		// configure logging
		logConfig();

		Options options = new Options();
		Option dawis_sessionid_option = Option.builder("dawis_sessionid").argName("value").hasArg(true)
				.desc("use given sessionid to get data from the dawis remote control (used by webstart)").build();
		Option help = new Option("help", "print this message");
		options.addOption(dawis_sessionid_option);
		options.addOption(help);

		// create the parser
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// has the buildfile argument been passed?
			if (line.hasOption("dawis_sessionid")) {

				// initialise the member variable
				dawis_sessionid = line.getOptionValue("dawis_sessionid");

			}

			// automatically generate the help statement
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				// formatter.setSyntaxPrefix("kfsdkjfkjfds");
				formatter.printHelp("networkeditor", options);
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("CLI-Parsing failed.  Reason: " + exp.getMessage());
		}

		// go on with creating the editor window

		// boolean loaded = false;

		// create 3 Runnable instances to run them in separate threads simultaneously
		Runnable programStart = () -> SwingUtilities.invokeLater(() -> {
			intro.setLoadingText("Graphical User Interface");
			MainWindow w = MainWindow.getInstance();
			intro.closeWindow();
			w.getFrame().setEnabled(true);
			SwingUtilities.updateComponentTreeUI(w.getFrame());
		});

		Runnable containerStart = () -> {
			intro.setLoadingText("WebConnection");
			GraphContainer.getInstance();
			intro.setLoadingText("Database Information");
			new MostWantedMolecules();
			ElementNames names = ElementNamesSingleton.getInstance();
			names.fillEnzymeSet();
		};

		Runnable dbStart = new Runnable() {
			public void run() {
				// -- default database settings --
				String user = ResourceLibrary.getSettingsResource("settings.default.user");
				String password = ResourceLibrary.getSettingsResource("settings.default.password");
				String database = ResourceLibrary.getSettingsResource("settings.default.database.DAWIS");
				String database_ppi = ResourceLibrary.getSettingsResource("settings.default.database.PPI");
				String database_mirna = ResourceLibrary.getSettingsResource("settings.default.database.mirna");
				String database_mirnaNew = ResourceLibrary.getSettingsResource("settings.default.database.mirna_New");

				String server = ResourceLibrary.getSettingsResource("settings.default.server");
				String webservice = ResourceLibrary.getSettingsResource("settings.default.webservice.url");

				ConnectionSettings.getInstance().setDBConnection(new DBconnection(user, password, database, server));
				ConnectionSettings.getInstance().getDBConnection().setDawisDBName(database);
				ConnectionSettings.getInstance().getDBConnection().setPpiDBName(database_ppi);
				ConnectionSettings.getInstance().getDBConnection().setMirnaDBName(database_mirna);
				ConnectionSettings.getInstance().getDBConnection().setMirnaNewDBName(database_mirnaNew);

				ConnectionSettings.getInstance().setWebServiceUrl(webservice);

				// ConnectionSettings.setFileSaveDirectory(null);

				// if(ConnectionSettingsSingelton.getInstance().isInternetConnection()){
				try {
					ConnectionSettings.getInstance().getDBConnection().checkConnection();

					intro.setLoadingText("Data Warehouse Connection");
					;
					// fill data from the remote control (only if database is
					// reachable)
					// TODO uncomment if DAWIS DB integrated

					/*
					 * if (dawis_sessionid != null) { System.out.println("start");
					 * DAWISWebstartConnector dws = new DAWISWebstartConnector( dawis_sessionid);
					 * dws.execute();
					 *
					 * }
					 */

				}
				// else {
				catch (Exception e) {
					if (dawis_sessionid != null) {
						MyPopUp.getInstance().show("Error",
								"No internet connection available, can not load data from the remote control!"
										+ " Please check the connection settings and restart the program!");
					}
				}
			}
		};

		// run executor
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		executorService.execute(programStart);
		executorService.execute(dbStart);
		executorService.execute(containerStart);

		executorService.shutdown();
		//
		// new Thread(programStart).start();
		// new Thread(dbStart).start();
		// new Thread(containerStart).start();

	}

	/**
	 * Default logging for the network editor. Console and file logging is
	 * possible.<br>
	 * Configuration method uses programmable configuration class.
	 */
	private static void logConfig() {
		Logger logger = Logger.getRootLogger();
		logger.info("Network editor started by " + System.getProperty("user.name"));
	}
}