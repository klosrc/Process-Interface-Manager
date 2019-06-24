package com.acta.pim.scriptmanager.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.acta.pim.scriptmanager.exceptions.ScriptExecutionException;
import com.acta.pim.scriptmanager.utils.UtilsHelper;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;

public class ScriptManagerGenerator implements CommandConstants {
	
	private enum BreakLevel{
		SKIP,
		EXIT
	}
	
	private enum CommandChain{
		/*
		 *	A ; B  – Run A and then B, regardless of the success or failure of A
		 *	A && B  – Run B only if A succeeded
		 *	A || B  – Run B only if A failed
		 *  A | B – The output of A acts as the input to B in the chain.
		 */
		CONDITIONED("&&"),
		SEPARATED(";"),
		CHAINED("|"),
		NEGATED("||");
		
		CommandChain(String separator) {
			this.separator = separator;
		}
		
		String separator;
		
		String getSeparator(){
			return separator;
		}
	}
	
	static Logger logger = Logger.getLogger(ScriptManagerGenerator.class);
	
	List<HashMap<String,Object>> baselist;
	
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
	 *   	{
	 *   	 "type":"conditioned",
	 *   	 "command":"grep",
	 *   	 "arguments":["-a"]
	 *   	}
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
	public List<String> generateScript(String jsonString) {
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		logger.info("[generateScript()] received JSON: " + jsonString);
		
		Gson gson = builder.create();
		baselist = gson.fromJson(jsonString, List.class);
		int counter = 1;
		List<String> statements = new ArrayList();
		
		try {
			for(HashMap<String, Object> commandElementMap : baselist) {
				boolean isValid = validateRequestMapElement(commandElementMap, counter);
				if(!isValid) continue;
				
				String statement = obtainStatement(commandElementMap);
				statements.add(statement);
				
				counter++;
			}
		}catch(ScriptExecutionException e) {
			logger.error("Cannot process command execution request (failed on command sequence #"+counter+")", e);
			HashMap<String,Object> commandElementMap = baselist.get(counter);
			commandElementMap.put(this.RESPONSE_LABEL_EXCEPTION, UtilsHelper.getExceptionTraceAsString(e));
		}
		catch(Exception e) {
			logger.error("Unexpected Error. Cannot process command execution request (failed on command sequence #"+counter+")", e);
			HashMap<String,Object> commandElementMap = baselist.get(counter);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE, this.ERROR_CODE_UNEXPECTED_ERROR);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE, e.getMessage());
			commandElementMap.put(this.RESPONSE_LABEL_EXCEPTION, UtilsHelper.getExceptionTraceAsString(e));
		}
		
		return statements;
	}
	
	/**
	 * This method will determine if all expected parameters were received for the common
	 * Command JSON Element in the request
	 * @param commandElementMap The Map containing the values of the Main command node
	 * @return TRUE if no missing or unexpected values were found, FALSE if we must skip the element 
	 * @throws ScriptExecutionException
	 */
	private boolean validateRequestMapElement(HashMap<String, Object> commandElementMap, 
			int counter) throws ScriptExecutionException {
		
		logger.debug("validateRequestMapElement() > JSON Element: " + commandElementMap);
		
		BreakLevel failureBreak = BreakLevel.EXIT; // Defaulting break level
		String sequence = String.valueOf(counter);
		
		// First we validate the failure flag has been received
		if(!commandElementMap.containsKey(this.REQUEST_LABEL_FAILURE_BREAK)) {
			logger.info("No failure flag value was found for cmd element at position ["+sequence+"]");
			
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE,this.ERROR_CODE_NO_FAILURE_BREAK_FOUND_IN_REQUEST);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE,"No failure flag value was found for cmd element at position ["+sequence+"]");
			
			throw new ScriptExecutionException(this.ERROR_CODE_NO_FAILURE_BREAK_FOUND_IN_REQUEST,"No failure flag value was found for cmd element at position ["+sequence+"]");
		}
		else {
			failureBreak = BreakLevel.valueOf(commandElementMap.get(this.REQUEST_LABEL_FAILURE_BREAK).toString().toUpperCase());
			if(failureBreak==null) failureBreak = BreakLevel.EXIT;
		}
		
		// We validate that we have received a sequence number
		if(!commandElementMap.containsKey(this.REQUEST_LABEL_SEQUENCE)) {
			logger.info("No sequence value was found for cmd element at position ["+sequence+"]");
			
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE,this.ERROR_CODE_NO_SEQUENCE_FOUND_IN_REQUEST);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE,"No sequence value was found for cmd element at position ["+sequence+"]");
			
			if(failureBreak.equals(BreakLevel.EXIT)) {
				throw new ScriptExecutionException(this.ERROR_CODE_NO_SEQUENCE_FOUND_IN_REQUEST,"No sequence value was found for cmd element at position ["+sequence+"]");
			}
			if(failureBreak.equals(BreakLevel.SKIP)) {
				return false;
			}
		}
		else {
			sequence = commandElementMap.get(this.REQUEST_LABEL_SEQUENCE).toString();
		}
		
		// We validate we have received a command
		if(!commandElementMap.containsKey("command")) {
			logger.info("No command was found for cmd element at position ["+sequence+"]");
			
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_CODE,this.ERROR_CODE_NO_COMMAND_FOUND_IN_REQUEST);
			commandElementMap.put(this.RESPONSE_LABEL_ERROR_MESSAGE,"No command was found for cmd element at position ["+sequence+"]");
			
			if(failureBreak.equals(BreakLevel.EXIT)) {
				throw new ScriptExecutionException(this.ERROR_CODE_NO_COMMAND_FOUND_IN_REQUEST,"No command was found for cmd element at position ["+sequence+"]");
			}
			if(failureBreak.equals(BreakLevel.SKIP)) {
				return false;
			}
		}
		
		// Arguments are optional
		
		// Evaluation expression is optional
		
		// Chain is optional 

		return true;
	}
	
	/**
	 * This method will obtain the command to be executed received from the original JSON Structure
	 * @param commandElementMap
	 * @return
	 */
	private String obtainStatement(HashMap<String, Object> commandElementMap) {
		
		logger.debug("obtainStatement() > JSON Element: " + commandElementMap);
		
		StringBuilder sb = new StringBuilder(""); 
		String command = commandElementMap.get(this.REQUEST_LABEL_COMMAND).toString();
		List arguments = (List) commandElementMap.get(this.REQUEST_LABEL_ARGUMENTS);
		// [Command]
		sb.append(command);
		for(Object arg : arguments) {
			// [Command] [arg1] [arg2] [arg3]
			if(arg!=null) {
				sb.append(" ");
				sb.append(arg.toString());
			}
		}
		
		List chain = (List) commandElementMap.get(this.REQUEST_LABEL_CHAIN);
		for(Object nextChainElement : chain) {
			// [Command] [arg1] [arg2] [arg3] [|| / ; / &&] [command2]
			HashMap<String,Object> nextMapElement = (HashMap) nextChainElement;
			Object type = nextMapElement.get(this.REQUEST_LABEL_TYPE);
			Object chainCommand = nextMapElement.get(this.REQUEST_LABEL_COMMAND);
			if(chainCommand!=null) {
				if(type!=null) {
					CommandChain chainType = CommandChain.valueOf(type.toString().toUpperCase());
					if(chainType!=null) {
						sb.append(" "+chainType.getSeparator()+" ");
						sb.append(chainCommand.toString());
						List chainArguments = (List) nextMapElement.get(this.REQUEST_LABEL_ARGUMENTS);
						for(Object arg : chainArguments) {
							// [Command] [arg1] [arg2] [arg3] [|| / ; / &&] [command2] [arg01] [arg02] [arg03]
							if(arg!=null) {
								sb.append(" ");
								sb.append(arg.toString());
							}
						}
					}
				}
			}
		}
		
		logger.debug("obtainStatement() > Statement: " + sb.toString());
		
		return sb.toString();
	}
}
