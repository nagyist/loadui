package com.eviware.loadui.launcher.server;

import java.nio.file.Path;
import java.util.Map;

interface LoadUiProjectRunner
{

	void runProject( Path projectPath, Map<String, Object> attributes );

}
