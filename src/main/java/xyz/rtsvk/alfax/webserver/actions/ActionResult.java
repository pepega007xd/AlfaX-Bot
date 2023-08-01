package xyz.rtsvk.alfax.webserver.actions;

public class ActionResult {

	private String status;
	private String message;


	public ActionResult(String status, String message) {
		this.status = status;
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
