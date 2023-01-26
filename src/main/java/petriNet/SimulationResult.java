package petriNet;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import biologicalElements.GraphElementAbstract;
import biologicalObjects.edges.petriNet.PNArc;
import biologicalObjects.nodes.petriNet.DiscreteTransition;
import util.DoubleHashMap;

public class SimulationResult {
	private final DoubleHashMap<GraphElementAbstract, Integer, Series> result = new DoubleHashMap<>();
	private DoubleHashMap<GraphElementAbstract, Integer, TimeSeries> resultFiltered;
	private final Series time;
	private boolean active = true;
	private String name;
	private final String id;
	private final StringBuilder logMessage = new StringBuilder();

	public SimulationResult(String id, String name, boolean filtered) {
		this.id = id;
		this.name = name;
		time = new Series();
		if (filtered) {
			resultFiltered = new DoubleHashMap<>();
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getId() {
		return id;
	}

	public StringBuilder getLogMessage() {
		return logMessage;
	}

	public Series getTime() {
		return time;
	}

	public void addTime(double time) {
		this.time.add(time);
	}

	public void addValue(GraphElementAbstract gea, int type, double value) {

		if (!result.contains(gea, type)) {
			result.put(gea, type, new Series());
		}
		result.get(gea, type).add(value);
	}

	public Series get(GraphElementAbstract gea, int type) {
		return this.result.get(gea, type);
	}

	public List<Double> getValues(GraphElementAbstract gea, int type) {
		return this.result.get(gea, type).getAll();
	}

	public Double getValue(GraphElementAbstract gea, int type, int pos) {
		return this.result.get(gea, type).get(pos);
	}

	public List<Double> getFiltered(GraphElementAbstract gea, int type) {
		return resultFiltered.get(gea, type).getValues().getAll();
	}

	public Double getFiltered(GraphElementAbstract gea, int type, int pos) {
		return resultFiltered.get(gea, type).getValues().get(pos);
	}

	public List<Double> getTimeValues() {
		return this.time.getAll();
	}

	public List<Double> getTime(GraphElementAbstract gea, int type, boolean filtered) {
		if (filtered) {
			return resultFiltered.get(gea, type).getTime().getAll();
		} else {
			return this.time.getAll();
		}
	}

	public void refreshFilter() {
		resultFiltered = new DoubleHashMap<GraphElementAbstract, Integer, TimeSeries>();
	}

	public boolean contains(GraphElementAbstract gea) {
		return result.contains(gea);
	}

	public boolean contains(GraphElementAbstract gea, int type) {
		return result.contains(gea, type);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public int size() {
		return this.time.size();
	}

	public void refineEdgeFlow(Set<PNArc> edges) {
		// so far only for edges connecting discrete transition (not yet stochastic)
		// TODO proper calculation of delay (based on returned values of delay from simulation, esp. if delay is not a constant value, use .putDelay for stoch. Trans.
		Iterator<PNArc> it = edges.iterator();

		PNArc e;
		double delay;
		double midTime;
		double lastToken = 0;
		double currentToken = 0;
		int revisedTime = 0;
		Series sum;
		//System.out.println("refining edge flow");
		while (it.hasNext()) {
			e = it.next();
			if (e.getFrom() instanceof DiscreteTransition) {
				delay = ((DiscreteTransition) e.getFrom()).getDelay();
			} else if (e.getTo() instanceof DiscreteTransition) {
				delay = ((DiscreteTransition) e.getTo()).getDelay();
			} else {
				continue;
			}
			// System.out.println("refine: "+e.getFrom().getName()+" -> "+e.getTo().getName() );
			if (result.contains(e, SimulationResultController.SIM_SUM_OF_TOKEN)
					&& result.get(e, SimulationResultController.SIM_SUM_OF_TOKEN).size() > 0
					&& !result.contains(e, SimulationResultController.SIM_ACTUAL_TOKEN_FLOW)) {
				// System.out.println("refine");
				sum = result.get(e, SimulationResultController.SIM_SUM_OF_TOKEN);
				midTime = delay * 0.5;
				lastToken = 0;
				currentToken = 0;
				revisedTime = 0;
				for (int i = 0; i < time.size(); i++) {
					if (time.get(i) >= midTime) {
						currentToken = sum.get(i);
						if (midTime > delay) {
							for (int j = revisedTime; j <= i; j++) {
								if (time.get(j) <= time.get(revisedTime) + delay) {
									// System.out.println(time.get(j) +" "+ (currentToken-lastToken));
									addValue(e, SimulationResultController.SIM_ACTUAL_TOKEN_FLOW,
											currentToken - lastToken);
								} else {
									revisedTime = j;
									lastToken = currentToken;
									break;
								}
							}
						}
						midTime += delay;
					}
				}
				currentToken = sum.get(sum.size() - 1);
				for (int i = revisedTime; i < time.size(); i++) {
					// System.out.println(time.get(i) +" "+ (currentToken-lastToken));
					addValue(e, SimulationResultController.SIM_ACTUAL_TOKEN_FLOW, currentToken - lastToken);
				}
				// System.out.println(time.size() + "
				// "+result.get(e,SimulationResultController.SIM_ACTUAL_TOKEN_FLOW).size());
			}
		}
	}
}
