/*
 * Created on Feb 24, 2005
 *
 */
package fi.csc.microarray.analyser;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fi.csc.microarray.messaging.message.JobMessage;


/**
 * Compute service specific versions of analysis tools descriptions.
 * Content is overlapping with generic SADLDescription objects, but 
 * some features are not here and some are extra.
 * 
 * @author Taavi Hupponen, Aleksi Kallio 
 */
public class AnalysisDescription {

	/**
	 * Describes one parameter, such as "number of iterations".
	 * 
	 */
	public static class ParameterDescription {

		private String name;
		private String comment;
		private boolean numeric;
		
		public ParameterDescription(String name, String comment, boolean numeric) {
			this.name = name;
			this.comment = comment;
			this.numeric = numeric;
		}

		public boolean isNumeric() {
			return numeric;
		}

		public String getComment() {
			return comment;
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * Describes an output (parameter name and file name). 
	 */
	public static class OutputDescription {
        private String fileName;

        public String getFileName() {
            return fileName;
        }
	    
	    public OutputDescription(String fileName) {
	        this.fileName = fileName;
	    }
	}
	
	/**
	 * Actual executable that handles the analysis.
	 */
	private String command;
	
	/**
	 * The actual content of the operation implementation.
	 */
	private Object implementation;
	
	/**
	 * Analysis name (used in GUI etc.)
	 */
	private String name;
	
	/**
	 * Description.
	 */
	private String comment;

	
	
	private List<OutputDescription> outputFiles = new LinkedList<OutputDescription>();
	private List<ParameterDescription> parameters = new LinkedList<ParameterDescription>();
	private String sourceCode;
	private String category;
	private String sadl;
	private String helpURL = null;
	private AnalysisHandler handler;
	private Map<String, String> configParameters = null;

    /**
	 * Name of the original source script or java class etc.
	 * Needed for update checks.
	 */
	private String sourceResourceName;

	/**
	 * Needed for update checks.
	 */
	private String sourceResourceFullPath;
	
	private String initialiser;
	
	private Date creationTime = new Date();
	private boolean updatedSinceStartup = false;


	/**
	 * Initializes empty (non-usable) description.
	 *
	 */
	public AnalysisDescription(AnalysisHandler handler) {
		this.handler = handler;
	}

	public String getCommand() {
		return command;
	}
	
	/**
	 * Factory method, infers correct job type.
	 * @param message
	 * @param resultHandler
	 * @return
	 * @throws AnalysisException 
	 */
	public AnalysisJob createAnalysisJob(JobMessage message, ResultCallback resultHandler) throws AnalysisException {
		return handler.createAnalysisJob(message, this, resultHandler);		
	}
	
	public Object getImplementation() {
		return implementation;
	}
	
	public List<OutputDescription> getOutputFiles() {
		return outputFiles;
	}
	
	public Iterable<ParameterDescription> getParameters() {
		return parameters;
	}
	
	public void addParameter(ParameterDescription pd) {
		parameters.add(pd);
	}
	
	public String getInitialiser() {
		return this.initialiser;
	}

	public void setInitialiser(String initialiser) {
		this.initialiser = initialiser;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setImplementation(Object implementation) {
		this.implementation = implementation;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getFullName() {
		return "\"" + getCategory() + "\"/\"" + getName() + "\""; 
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addOutputFile(String fileName) {
		outputFiles.add(new OutputDescription(fileName));
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;		
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setCategory(String category) {
		this.category = category;		
	}

	public String getCategory() {
		return category;
	}

	public void setSADL(String sadl) {
		this.sadl = sadl;
	}
	
	public String getSADL() {
		
		if (sadl != null) {
			return sadl;
		} else {
			throw new RuntimeException("sadl is null");
		}
	}

	public String getSourceResourceName() {
		return sourceResourceName;
	}

	public void setSourceResourceName(String sourceResourceName) {
		this.sourceResourceName = sourceResourceName;
	}

	public String getSourceResourceFullPath() {
		return sourceResourceFullPath;
	}

	public void setSourceResourceFullPath(String sourceResourceFullPath) {
		this.sourceResourceFullPath = sourceResourceFullPath;
	}
	
	public void setHelpURL(String helpURL) {
	    this.helpURL = helpURL;
	}

    public String getHelpURL() {
	    return helpURL;
	}
    
	
	public long getCreationTime() {
		return this.creationTime.getTime();
	}
	
	public boolean isUptodate() {
		return this.handler.isUptodate(this);
	}
	
	public AnalysisHandler getHandler() {
		return this.handler;
	}

	public boolean isUpdatedSinceStartup() {
		return updatedSinceStartup;
	}

	public void setUpdatedSinceStartup() {
		this.updatedSinceStartup = true;
	}
  
    public Map<String, String> getConfigParameters() {
        return configParameters;
    }

    public void setConfigParameters(Map<String, String> configParameters) {
        this.configParameters = configParameters;
    }
}
 