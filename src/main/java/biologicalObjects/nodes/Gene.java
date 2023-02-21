package biologicalObjects.nodes;

import java.util.Vector;

import biologicalElements.Elementdeclerations;

public class Gene extends BiologicalNodeAbstract {
	private String ntSequence = "";
	private final Vector<String[]> proteins = new Vector<>();
	private final Vector<String[]> enzymes = new Vector<>();
	private boolean hasProteins = false;
	private boolean hasEnzymes = false;

	public Gene(String label, String name) {
		super(label, name);
		setBiologicalElement(Elementdeclerations.gene);
		attributeSetter(this.getClass().getSimpleName(), this);
	}

	public String getNtSequence() {
		return ntSequence;
	}

	public void setNtSequence(String ntSequence) {
		this.ntSequence = ntSequence;
	}

	public Vector<String[]> getProteins() {
		return proteins;
	}

	public Vector<String[]> getEnzymes() {
		return enzymes;
	}

	public boolean isHasProteins() {
		return hasProteins;
	}

	public boolean isHasEnzymes() {
		return hasEnzymes;
	}

	public void addProtein(String[] proteinID) {
		if (!proteins.contains(proteinID)) {
			proteins.add(proteinID);
		}
		hasProteins = true;
	}

	public void addEnzyme(String[] enzymeID) {
		if (!enzymes.contains(enzymeID)) {
			enzymes.add(enzymeID);
		}
		hasEnzymes = true;
	}
}
