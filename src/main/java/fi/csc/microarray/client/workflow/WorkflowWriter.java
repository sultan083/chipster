package fi.csc.microarray.client.workflow;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import fi.csc.microarray.client.operation.Operation;
import fi.csc.microarray.client.operation.Operation.DataBinding;
import fi.csc.microarray.client.operation.parameter.Parameter;
import fi.csc.microarray.constants.ApplicationConstants;
import fi.csc.microarray.databeans.DataBean;
import fi.csc.microarray.databeans.DataBean.Link;
import fi.csc.microarray.module.chipster.ChipsterInputTypes;

public class WorkflowWriter {

	public static String generateVersionHeaderLine() {
		return "// VERSION " + WorkflowManager.WORKFLOW_VERSION + " (do not remove this)\n";
	}

	private LinkedList<String> writeWarnings = new LinkedList<String>();
	private HashMap<DataBean, String> resultIdMap;
	private boolean used = false;

	/**
	 * Saves workflow from currently selected databean. Method is synchronised
	 * because it uses state of the manager during the generation process.
	 */
	public synchronized StringBuffer writeWorkflow(DataBean root) throws IOException {

		if (this.used) {
			throw new IllegalStateException("writer cannot be reused");
		}

		// initialise recursion
		this.resultIdMap = new HashMap<DataBean, String>();

		// generate header
		StringBuffer script = new StringBuffer("");
		generateHeader(script, root);

		// do recursion
		generateRecursively(script, root);

		// mark this writer dirty
		this.used = true;

		return script;
	}

	private void generateRecursively(StringBuffer script, DataBean bean) {

		// dig all operations that were used to produce children of this bean
		HashSet<Operation> operations = new HashSet<Operation>();
		for (DataBean derived : bean.getLinkSources(Link.DERIVATION)) {
			operations.add(derived.getOperation());
		}

		// generate operations leading to derived beans
		for (Operation operation : operations) {
			LinkedList<DataBean> results = new LinkedList<DataBean>();
			// collect datas that were produced by this operation
			for (DataBean derived : bean.getLinkSources(Link.DERIVATION)) {
				if (derived.getOperation() == operation) {
					results.add(derived);
				}
			}

			generateStep(script, operation, results);
		}

		// continue recursion from derived beans
		for (DataBean derived : bean.getLinkSources(Link.DERIVATION)) {
			generateRecursively(script, derived);
		}
	}

	/**
	 * 
	 * @param results must be in the same order as they were produced by the operation!
	 */
	private void generateStep(StringBuffer script, Operation operation, LinkedList<DataBean> results) {
		if (operation.getBindings() == null) {
			// not enough data (i.e. bindings) available (this was import data, source data was deleted etc.)
			writeWarnings.add("Operation " + operation.getDefinition().getFullName() + " was skipped.");
			return;
		}

		StringBuffer dataString = new StringBuffer("\ndatas = new WfDataBean[] {\n");
		boolean first = true;
		for (DataBinding binding : operation.getBindings()) {
			if (binding.getInputType() == ChipsterInputTypes.PHENODATA) {
				continue; // phenodata is bound automatically
			}
			if (!first) {
				dataString.append(",\n");
			} else {
				first = false;
			}
			String name = resultIdMap.get(binding.getData());
			if (name == null) {
				// we cannot handle this, too complicated structure
				writeWarnings.add("Operation " + operation.getDefinition().getFullName() + " was skipped because it combines multiple workflow branches.");
				return; // skip this branch, nothing was written to script yet
			}
			dataString.append("  " + name);
		}
		dataString.append("\n};\n");
		script.append(dataString);

		script.append("op = new WfOperation(app.locateOperationDefinition(\"" + operation.getCategoryName() + "\", \"" + operation.getID() + "\"), datas);\n");

		for (Parameter parameter : operation.getParameters()) {
			script.append("op.setParameter(\"" + parameter.getName() + "\", " + parameter.getValueAsJava() + ");\n");
		}

		int resultCount = operation.getDefinition().getOutputCount();
		script.append("opBlocker = new WfResultBlocker(" + resultCount + ");\n");
		script.append("op.setResultListener(opBlocker);\n");
		script.append("app.executeOperation(op);\n");
		script.append("opBlocker.blockUntilDone();\n");

		int i = -1;
		for (DataBean result : results) {
			i++;
			// here we demand that the order of results is correct!
			String name = "data" + this.resultIdMap.size();
			this.resultIdMap.put(result, name);
			script.append(name + " = opBlocker.getWorkflowResults().get(" + i + ");\n");
		}
	}

	private void generateHeader(StringBuffer script, DataBean root) {
		// write info and imports
		script.append(generateVersionHeaderLine());
		script.append("/* \n" + "  BeanShell workflow script for " + ApplicationConstants.APPLICATION_TITLE + "\n" + "  Generated by " + System.getProperty("user.name") + " at " + new Date().toString() + "\n" + "*/\n");
		script.append("\n");
		script.append("import fi.csc.microarray.client.workflow.api.*;\n");
		script.append("\n");
		
		// write datas
		script.append("data0 = app.getSelectionManager().getSelectedDataBean();\n");
		this.resultIdMap.put(root, "data0");
	}

	/**
	 * List operations that we had to skip.
	 */
	public List<String> writeWarnings() {
		return writeWarnings ;
	}
}
