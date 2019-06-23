package com.acta.pim.scriptmanager.factory;

public interface CommandConstants {

	/* JSON Request expected labels */
	static String REQUEST_LABEL_COMMAND = "command";
	static String REQUEST_LABEL_FAILURE_BREAK = "failureBreak";
	static String REQUEST_LABEL_ARGUMENTS = "arguments";
	static String REQUEST_LABEL_SEQUENCE = "sequence";
	static String REQUEST_LABEL_EVAL_EXPRESSION = "evalExpression";
	static String REQUEST_LABEL_CHAIN = "chain";
	
	/* JSON Response expected labels */
	static String RESPONSE_LABEL_SEQUENCE = "sequence";
	static String RESPONSE_LABEL_RESULT = "chain";
	static String RESPONSE_LABEL_ERROR_CODE = "errorCode";
	static String RESPONSE_LABEL_ERROR_MESSAGE = "errorMessage";
	static String RESPONSE_LABEL_EXCEPTION = "exception";
	
	/* Possible Error Codes  */
	static String ERROR_CODE_UNEXPECTED_ERROR = "01";
	static String ERROR_CODE_NO_SEQUENCE_FOUND_IN_REQUEST = "02";
	static String ERROR_CODE_NO_COMMAND_FOUND_IN_REQUEST = "03";
	static String ERROR_CODE_NO_FAILURE_BREAK_FOUND_IN_REQUEST = "04";
}
