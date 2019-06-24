package com.acta.pim.scriptmanager.exceptions;

public class ScriptExecutionException extends Exception {

	public ScriptExecutionException(String errorCode, String errorMessage) {
		super(new StringBuilder("").append("[CODE: ").append(errorCode).append("]: ").append(errorMessage).toString());
	}
	
	public ScriptExecutionException(String errorCode, String errorMessage, Throwable e) {
		super(new StringBuilder("").append("[CODE: ").append(errorCode).append("]: ").append(errorMessage).toString(), e);
	}
	
}
