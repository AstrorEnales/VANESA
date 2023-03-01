package graph.gui;

import biologicalElements.GraphElementAbstract;
import biologicalElements.Pathway;
import biologicalObjects.edges.BiologicalEdgeAbstract;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import biologicalObjects.nodes.DynamicNode;
import biologicalObjects.nodes.petriNet.ContinuousTransition;
import graph.ChangedFlags;
import graph.GraphInstance;
import gui.MainWindow;
import gui.PopUpDialog;
import net.miginfocom.swing.MigLayout;
import util.KineticBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ParameterWindow implements DocumentListener {
    private final JFrame frame;
    private final JPanel panel;
    private final JTextField name = new JTextField("");
    private final GraphInstance graphInstance = new GraphInstance();
    private final Pathway pw = graphInstance.getPathway();
    private final JTextField value = new JTextField("");
    private final JTextField unit = new JTextField("");
    private final JButton add;
    private final GraphElementAbstract gea;
    private FormulaPanel fp;
    private final JTextPane formula;
    private boolean editMode = false;

    public ParameterWindow(GraphElementAbstract gea) {
        frame = new JFrame("Parameters");
        this.gea = gea;
        MigLayout layout = new MigLayout("", "[left]");
        panel = new JPanel(layout);
        formula = new JTextPane();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AutoSuggester(formula, frame, null, Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f) {
                    @Override
                    boolean wordTyped(String typedWord) {
                        // create list for dictionary this in your case might be done via calling a method which queries db and returns results as arraylist
                        ArrayList<String> words = new ArrayList<>();
                        for (BiologicalNodeAbstract bna : pw.getAllGraphNodes()) {
                            if (!bna.isLogical()) {
                                if (!words.contains(bna.getName())) {
                                    words.add(bna.getName());
                                }
                            }
                        }
                        for (Parameter p : gea.getParameters()) {
                            if (!words.contains(p.getName())) {
                                words.add(p.getName());
                            }
                        }
                        setDictionary(words);
                        return super.wordTyped(typedWord);
                    }
                };
            }
        });

        if (gea instanceof DynamicNode || gea instanceof BiologicalEdgeAbstract ||
            gea instanceof ContinuousTransition) {
            PropertyChangeListener pcListener = evt -> frame.pack();
            String function = "";
            if (gea instanceof DynamicNode) {
                function = ((DynamicNode) gea).getMaximalSpeed();
            } else if (gea instanceof BiologicalEdgeAbstract) {
                function = ((BiologicalEdgeAbstract) gea).getFunction();
            } else if (gea instanceof ContinuousTransition) {
                function = ((ContinuousTransition) gea).getMaximalSpeed();
            }
            fp = new FormulaPanel(formula, function, pcListener);
            fp.setVisible(true);
            panel.add(fp);
        }
        name.getDocument().addDocumentListener(this);
        add = new JButton("add");
        add.addActionListener(e -> onAddClicked());
        JButton cancel = new JButton("cancel");
        cancel.addActionListener(e -> onCancelClicked());
        JButton okButton = new JButton("ok");
        okButton.addActionListener(e -> onOkClicked());
        repaintPanel();
        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE);
        optionPane.setOptions(new JButton[]{okButton, cancel});
        frame.setAlwaysOnTop(false);
        frame.setContentPane(optionPane);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setIconImages(MainWindow.getInstance().getFrame().getIconImages());
        frame.revalidate();
        frame.pack();
        frame.setLocationRelativeTo(MainWindow.getInstance().getFrame());
        frame.requestFocus();
        frame.setVisible(true);
        frame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent e) {
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                frame.pack();
            }
        });
    }

    private void onOkClicked() {
        if (gea instanceof DynamicNode || gea instanceof BiologicalEdgeAbstract ||
            gea instanceof ContinuousTransition) {
            String formula = fp.getFormula();
            String formulaClean = formula.replaceAll("\\s", "");
            boolean changed = false;
            if (gea instanceof DynamicNode) {
                DynamicNode dn = (DynamicNode) gea;
                String orgClean = dn.getMaximalSpeed().replaceAll("\\s", "");
                if (!orgClean.equals(formulaClean)) {
                    dn.setMaximalSpeed(formula);
                    changed = true;
                }
            } else if (gea instanceof BiologicalEdgeAbstract) {
                BiologicalEdgeAbstract bea = (BiologicalEdgeAbstract) gea;
                String orgClean = bea.getFunction().replaceAll("\\s", "");
                if (!orgClean.equals(formulaClean)) {
                    bea.setFunction(formula);
                    changed = true;
                }
            } else if (gea instanceof ContinuousTransition) {
                ContinuousTransition t = (ContinuousTransition) gea;
                String orgClean = t.getMaximalSpeed().replaceAll("\\s", "");
                if (!orgClean.equals(formulaClean)) {
                    t.setMaximalSpeed(formula);
                    changed = true;
                }
            }
            if (changed) {
                pw.handleChangeFlags(ChangedFlags.PNPROPERTIES_CHANGED);
                MainWindow.getInstance().updateElementProperties();
            }
        }
        frame.setVisible(false);
    }

    private void onCancelClicked() {
        frame.setVisible(false);
    }

    private void onAddClicked() {
        for (int i = 0; i < gea.getParameters().size(); i++) {
            Parameter p = gea.getParameters().get(i);
            if (p.getName().equals(name.getText())) {
                if (editMode && name.getText().trim().length() > 0) {
                    try {
                        p.setValue(Double.parseDouble(value.getText()));
                        p.setUnit(unit.getText());
                        // TODO handling of rebuild
                        pw.handleChangeFlags(ChangedFlags.NODE_CHANGED);
                        editMode = false;
                        add.setText("add");
                        repaintPanel();
                    } catch (NumberFormatException nfe) {
                        PopUpDialog.getInstance().show("Parameter",
                                                       "Parameter not correct. Value not a number or empty?");
                    }
                } else {
                    PopUpDialog.getInstance().show("Parameter",
                                                   "Parameter with same name already exists! Use edit button to edit parameter");
                }
                return;
            }
        }
        if (name.getText().trim().length() > 0) {
            try {
                Parameter p = new Parameter(name.getText(), Double.parseDouble(value.getText()), unit.getText());
                gea.getParameters().add(p);
                pw.getChangedParameters().put(p, gea);
                pw.handleChangeFlags(ChangedFlags.PARAMETER_CHANGED);
                panel.add(new JLabel(name.getText()), "span 1, gaptop 2 ");
                panel.add(new JLabel(value.getText()), "span 1, gapright 4");
                panel.add(new JLabel(unit.getText()), "span 1, gapright 4, wrap");
                repaintPanel();
            } catch (NumberFormatException nfx) {
                PopUpDialog.getInstance().show("Parameter", "Parameter not correct. Value not a number or empty?");
            }
        } else {
            PopUpDialog.getInstance().show("Parameter", "Name is empty!");
        }
    }

    private void onGuessKineticClicked() {
        guessKinetic();
        repaintPanel();
    }

    private void onSetValuesClicked() {
        if (gea instanceof DynamicNode && gea instanceof BiologicalNodeAbstract ||
            gea instanceof ContinuousTransition) {
            new ParameterSearcher((BiologicalNodeAbstract) gea, true);
        }
    }

    private void listParameters() {
        panel.add(new JLabel("Name"), "span 1, gaptop 2");
        panel.add(new JLabel("Value"), "span 1, gapright 4");
        panel.add(new JLabel("Unit"), "span 1, gapright 4, wrap");
        for (int i = 0; i < gea.getParameters().size(); i++) {
            final int parameterIdx = i;
            Parameter p = gea.getParameters().get(i);
            panel.add(new JLabel(p.getName()), "span 1, gaptop 2");

            panel.add(new JLabel(p.getValue() + ""), "span 1, gapright 4");

            panel.add(new JLabel(p.getUnit()), "span 1, gapright 4");

            JButton edit = new JButton("✎");
            edit.addActionListener(e -> onParameterEditClicked(parameterIdx));
            edit.setToolTipText("edit entry");
            edit.setMaximumSize(edit.getMinimumSize());

            JButton del = new JButton("✖");
            del.setBackground(Color.RED);
            del.addActionListener(e -> onParameterDeleteClicked(parameterIdx));
            del.setToolTipText("delete entry");

            JButton up = new JButton("↑");
            up.addActionListener(e -> onParameterUpClicked(parameterIdx));
            up.setToolTipText("move up");

            JButton down = new JButton("↓");
            down.addActionListener(e -> onParameterDownClicked(parameterIdx));
            down.setToolTipText("move down");
            if (gea.getParameters().size() > 1) {
                if (i == 0) {
                    panel.add(down, "skip, span 1");
                } else if (i == gea.getParameters().size() - 1) {
                    panel.add(up, "span 1, gapright 4");
                } else {
                    panel.add(up, "span 1, gapright 4");
                    panel.add(down, "span 1");
                }
            }
            if (i == gea.getParameters().size() - 1) {
                panel.add(edit, "skip, span 1");
            } else {
                panel.add(edit, "span 1");
            }
            if (gea.getParameters().size() == 1) {
                panel.add(del, "wrap");
            } else {
                panel.add(del, "span 1, wrap");
            }
        }
    }

    private void onParameterEditClicked(int idx) {
        Parameter p = gea.getParameters().get(idx);
        name.setText(p.getName());
        value.setText(p.getValue() + "");
        unit.setText(p.getUnit());
        add.setText("Override");
        editMode = true;
        repaintPanel();
    }

    private void onParameterDeleteClicked(int idx) {
        pw.handleChangeFlags(ChangedFlags.PARAMETER_CHANGED);
        pw.getChangedParameters().remove(gea.getParameters().get(idx));
        gea.getParameters().remove(idx);
        repaintPanel();
    }

    private void onParameterUpClicked(int idx) {
        Parameter p = gea.getParameters().get(idx);
        gea.getParameters().set(idx, gea.getParameters().get(idx - 1));
        gea.getParameters().set(idx - 1, p);
        repaintPanel();
    }

    private void onParameterDownClicked(int idx) {
        Parameter p = gea.getParameters().get(idx);
        gea.getParameters().set(idx, gea.getParameters().get(idx + 1));
        gea.getParameters().set(idx + 1, p);
        repaintPanel();
    }

    private void repaintPanel() {
        panel.removeAll();
        if (gea instanceof DynamicNode || gea instanceof BiologicalEdgeAbstract ||
            gea instanceof ContinuousTransition) {
            panel.add(fp, "span 20, wrap");
        }
        if (gea instanceof DynamicNode || gea instanceof ContinuousTransition) {
            JButton guessKinetic = new JButton("apply kinetic");
            guessKinetic.setToolTipText("Applies convenience kinetic");
            guessKinetic.addActionListener(e -> onGuessKineticClicked());
            panel.add(guessKinetic, "");
            JButton setValues = new JButton("BRENDA kinetic parameters");
            setValues.setToolTipText("Browse BRENDA for kinetic parameters (km and kcat)");
            setValues.addActionListener(e -> onSetValuesClicked());
            panel.add(setValues, "span 2, wrap");
        }
        panel.add(new JSeparator(), "span, growx, gaptop 7 ");

        panel.add(new JLabel("Name"), "span 1, gaptop 2 ");
        panel.add(name, "span,wrap,growx ,gap 10, gaptop 2");

        panel.add(new JLabel("Value"), "span 1, gapright 4");
        panel.add(value, "span,wrap,growx ,gap 10, gaptop 2");

        panel.add(new JLabel("Unit"), "span 1, gapright 4");
        panel.add(unit, "span,wrap,growx ,gap 10, gaptop 2");

        panel.add(add, "wrap");
        panel.add(new JSeparator(), "span, growx, gaptop 7 ");
        this.listParameters();
        panel.repaint();
        frame.pack();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleChangedName();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleChangedName();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void handleChangedName() {
        if (editMode) {
            this.editMode = false;
            this.add.setText("add");
        }
    }

    private void guessKinetic() {
        if (gea instanceof DynamicNode || gea instanceof ContinuousTransition) {
            BiologicalNodeAbstract bna = (BiologicalNodeAbstract) gea;
            String kinetic = KineticBuilder.createConvenienceKinetic(bna);
            formula.setText(kinetic);
            formula.requestFocus();
        }
    }
}
