package com.acta.pim.scriptmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;
import com.acta.pim.scriptmanager.exceptions.ScriptExecutionException;
import com.acta.pim.scriptmanager.factory.CommandConstants;

public class ScriptExecutor {
	
	static Logger logger = Logger.getLogger(ScriptExecutor.class);
	
	public static HashMap execute(String statement) throws ScriptExecutionException {
		
		StringBuilder result = new StringBuilder("");
		StringBuilder errorResult = new StringBuilder("");
		String s = "";
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		HashMap<String, String> results = new HashMap();
		
        try {
            Process p = Runtime.getRuntime().exec(statement);
            
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            logger.debug("Here is the standard output of the command:");
            while ((s = stdInput.readLine()) != null) {
            	result.append(s);
            	result.append("\n");
            }
            logger.debug("\n"+result);
            results.put(CommandConstants.RESPONSE_LABEL_RESULT, result.toString());
            
            // read any errors from the attempted command
            logger.debug("Here is the standard error of the command (if any):");
            while ((s = stdError.readLine()) != null) {
            	errorResult.append(s);
            	errorResult.append("\n");
            }
            logger.debug("\n"+errorResult);
            results.put(CommandConstants.RESPONSE_LABEL_ERROR_RESULT, errorResult.toString());
        }
        catch(Exception e) {
        	logger.error("Cannot execute script, Cause: " + e.getMessage(), e);
        	throw new ScriptExecutionException(CommandConstants.ERROR_CODE_UNEXPECTED_ERROR,
        			"Cannot execute script, Cause: " + e.getMessage(),e); 
        }
        finally {
        	try {
	        	if(stdInput!=null)
	        		stdInput.close();
	        	if(stdError!=null)
	        		stdError.close();
        	}
        	catch(IOException e) {
        		logger.error("Cannot close InputStreams, Cause: " + e.getMessage(), e);
        	}
        }
        
        return results;
	}
	
}
