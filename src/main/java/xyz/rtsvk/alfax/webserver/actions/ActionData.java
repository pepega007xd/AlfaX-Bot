package xyz.rtsvk.alfax.webserver.actions;

import java.util.List;

public class ActionData {
	private final Action action;
	private final byte requiredPermissions;
	private final List<String> requiredArgs;
	private final String endpointName;

	public ActionData(String endpointName, Action action, byte requiredPermissions, List<String> requiredArgs) {
		this.endpointName = endpointName;
		this.action = action;
		this.requiredPermissions = requiredPermissions;
		this.requiredArgs = requiredArgs;
	}

	public Action getAction() {
		return action;
	}

	public byte getRequiredPermissions() {
		return requiredPermissions;
	}

	public List<String> getRequiredArgs() {
		return requiredArgs;
	}

	public String getEndpointName() {
		return endpointName;
	}
}
