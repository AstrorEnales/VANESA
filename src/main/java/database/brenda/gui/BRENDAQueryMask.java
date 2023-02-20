package database.brenda.gui;

import database.gui.QueryMask;
import gui.MainWindow;
import gui.eventhandlers.TextfeldColorChanger;

import javax.swing.*;

public class BRENDAQueryMask extends QueryMask {
    private final JTextField ecNumber;
    private final JTextField name;
    private final JTextField substrate;
    private final JTextField product;
    private final JTextField organism;

    public BRENDAQueryMask() {
        super();
        ecNumber = new JTextField(20);
        ecNumber.addFocusListener(new TextfeldColorChanger());

        name = new JTextField(20);
        name.setText("mutase");
        name.addFocusListener(new TextfeldColorChanger());

        substrate = new JTextField(20);
        substrate.addFocusListener(new TextfeldColorChanger());

        product = new JTextField(20);
        product.setText("glycerat");
        product.addFocusListener(new TextfeldColorChanger());

        organism = new JTextField(20);
        organism.setText("bacillus");
        organism.addFocusListener(new TextfeldColorChanger());

        panel.add(new JLabel("BRENDA Search Window"), "span 4");
        panel.add(new JSeparator(), "span, growx, wrap 15, gaptop 10, gap 5");

        panel.add(new JLabel(imagePath.getImageIcon("dataServer.png")), "span 2 5");

        panel.add(new JLabel("EC-Number"), "span 2, gap 5 ");
        panel.add(ecNumber, "span,wrap,growx ,gap 10");
        panel.add(new JLabel("Name"), "span 2, gap 5 ");
        panel.add(name, "span, wrap, growx, gap 10");
        panel.add(new JLabel("Substrat"), "span 2, gap 5 ");
        panel.add(substrate, "span, wrap, growx, gap 10");
        panel.add(new JLabel("Product"), "span 2, gap 5 ");
        panel.add(product, "span, wrap, growx, gap 10");
        panel.add(new JLabel("Organism"), "span 2, gap 5 ");
        panel.add(organism, "span, wrap 15, growx, gap 10");

        addControlButtons();
    }

    @Override
    public String getMaskName() {
        return "BRENDA";
    }

    @Override
    protected void reset() {
        ecNumber.setText("");
        name.setText("");
        substrate.setText("");
        product.setText("");
        organism.setText("");
    }

    @Override
    protected String search() {
        // TODO
        return null;
    }

    public String[] getKeyword() {
        String[] input = new String[5];
        input[0] = ecNumber.getText();
        input[1] = name.getText();
        input[2] = substrate.getText();
        input[3] = product.getText();
        input[4] = organism.getText();
        return input;
    }

    @Override
    protected boolean doSearchCriteriaExist() {
        return ecNumber.getText().length() > 0 || name.getText().length() > 0 || substrate.getText().length() > 0 ||
                product.getText().length() > 0 || organism.getText().length() > 0;
    }

    @Override
    protected void showInfoWindow() {
        String instructions =
                "<html>" +
                        "<h3>The BRENDA search window</h3>" +
                        "<ul>" +
                        "<li>BRENDA is the comprehensive enzyme information database.<p>" +
                        "It is maintained and developed at the Institute of Biochemistry <p>" +
                        "at the University of Cologne. Data on enzyme functions are extracted <p>" +
                        "directly from the primary literature and stored in this database.<p>" +
                        "<li>The search window is a query mask that gives the user the<p>" +
                        "possibility to consult the BRENDA database for information of interest. <p>" +
                        "<li>By searching the database for one of the following attributes EC-Number,<p>" +
                        "name, substrate, product or organism the database will be checked <p>" +
                        "for all enzymes that meet the given demands. As a result a list of possible enzymes <p>" +
                        "will be displayed to the user. In the following step the user can choose either one or more <p>" +
                        "enzymes of interest he would like to examine in detail.<p>" +
                        "<li>Additionally the software will try to calculate a possible pathway with a given search depth.<p>" +
                        "The calculation is an iterative procedure in which possible connections to other enzymes<p>" +
                        "will be checked<p>" +
                        "</ul>" +
                        "<h3>How to use the search form</h3>" +
                        "<ul>" +
                        "<li>To search for one attribute simply type " +
                        "in the attribute of interest.  <p><font color=\"#000099\">Example: Homosapiens </font><p>" +
                        "<li>To search for two attributes in one field use the ' & ' char " +
                        "to connect them.  <p><font color=\"#000099\">Example: 5.4.2.1 & 5.3.2.1 </font><p>" +
                        "<li>To search for either one or another attribute in one field " +
                        "use the ' | ' char to connect them. <p><font color=\"#000099\">Example: 5.4.2.1 | 5.3.2.1 </font><p>" +
                        "<li>To search for data where given attributes should not appear " +
                        "put an exclamation mark before that attribute. <p><font color=\"#000099\">Example: !homo </font><p>" +
                        "<ul>" +
                        "</html>";
        JOptionPane.showMessageDialog(MainWindow.getInstance().getFrame(), instructions, "BRENDA Information",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
