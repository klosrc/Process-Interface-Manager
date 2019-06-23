package com.acta.pim.scriptmanager.instances;

import java.util.List;

import com.acta.pim.scriptmanager.arguments.ScriptArgument;

public interface ScriptManager {

	void execute(String command, List<ScriptArgument> args);
	
}
