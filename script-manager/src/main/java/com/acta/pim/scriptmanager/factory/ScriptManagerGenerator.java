package com.acta.pim.scriptmanager.factory;

import java.util.HashMap;
import java.util.List;

import com.acta.pim.scriptmanager.exceptions.ScriptExecutionException;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;

public class ScriptManagerGenerator implements CommandConstants {
	
	private enum BreakLevel{
		SKIP,
		EXIT
	}
	
	static Logger logger = Logger.getLogger(ScriptManagerGenerator.class);
	
	/**
	 * This method receives a JSON structure and transforms it into
	 * an instance of console commands and arguments for it
	 * Sample JSON Received:
	 * [
	 *  {
	 *   "sequence":"1",
	 * 	 "command":"ls",  
	 *   "arguments":["-f","-a"],
	 *   "evalExpression":"...",
	 *   "failureBreak":"exit",
	 *   "chain":[
	 *   	"command":"grep",
	 *   	"arguments":["-a"]
	 *   ]
	 *  }
	 * ]
	 * Sample JSON in Result:
	 * [
	 * 	{
	 * 	 "sequence":"1",
	 * 	 "result":"/mydir\t\t100Kb\ntextfile.txt\t\t1Kb",
	 * 	 "errorCode":"",
	 * 	 "errorMessage":"",
	 *   "exception":""
	 * 	}
	 * ]
	 * @param json
	 */
	public String generateScript(String jsonString) {
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		logger.info("[generateScript()] received JSON: " + jsonString);
		
		Gson gson = builder.create();
		List<HashMap<String,String>> baselist = gson.fromJson(jsonString, List.class);
		int counter = 1;
		
		try {
			for(HashMap<String, String> commandElementMap : baselist) {
				boolean isValid = validateRequestMapElement(commandElementMap, counter);
				if(!isValid) continue;
				counter++;
			}
		}catch(ScriptExecutionException e) {
			logger.error("Cannot process command execution request", e);
		}
		
		return null;
	}
	
	/**
	 * This method will determine if all expected parameters were received for the common
	 * Command JSON Element in the request
	 * @param commandElementMap The Map containing the values of the Main command node
	 * @return TRUE if no missing or unexpected values were found, FALSE if we must skip the element 
	 * @throws ScriptExecutionException
	 */
	private boolean validateRequestMapElement(HashMap<String, String> commandElementMap, 
			int counter) throws ScriptExecutionException {
		
		BreakLevel failureBreak = BreakLevel.EXIT; // Defaulting break level
		String sequence = String.valueOf(counter);
		
		// First we validate the failure flag has been received
		if(!commandElementMap.containsKey(this.REQUEST_LABEL_FAILURE_BREAK)) {
			logger.info("No failure flag value was found for cmd element at position ["+counter+"]");
			
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE,this.ERROR_CODE_NO_FAILURE_BREAK_FOUND_IN_REQUEST);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE,"No failure flag value was found for cmd element at position ["+counter+"]");
			
			throw new ScriptExecutionException(this.ERROR_CODE_NO_FAILURE_BREAK_FOUND_IN_REQUEST,"No failure flag value was found for cmd element at position ["+counter+"]");
		}
		else {
			failureBreak = BreakLevel.valueOf(commandElementMap.get(this.REQUEST_LABEL_FAILURE_BREAK).toUpperCase());
			if(failureBreak==null) failureBreak = BreakLevel.EXIT;
		}
		
		// We validate that we have received a sequence number
		if(!commandElementMap.containsKey(this.REQUEST_LABEL_SEQUENCE)) {
			logger.info("No sequence value was found for cmd element at position ["+counter+"]");
			
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE,this.ERROR_CODE_NO_SEQUENCE_FOUND_IN_REQUEST);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE,"No sequence value was found for cmd element at position ["+counter+"]");
			
			if(failureBreak.equals(BreakLevel.EXIT)) {
				throw new ScriptExecutionException(this.ERROR_CODE_NO_SEQUENCE_FOUND_IN_REQUEST,"No sequence value was found for cmd element at position ["+counter+"]");
			}
			if(failureBreak.equals(BreakLevel.SKIP)) {
				return false;
			}
		}
		else {
			sequence = commandElementMap.get(this.REQUEST_LABEL_SEQUENCE);
		}
		
		// We validate we have received a command
		if(!commandElementMap.containsKey("command")) {
			logger.info("No command was found for cmd element at position ["+sequence+"]");
		}
		
		// Arguments are optional
		
		return true;
	}
}
