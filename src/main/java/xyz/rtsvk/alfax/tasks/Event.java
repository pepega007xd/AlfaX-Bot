package xyz.rtsvk.alfax.tasks;

public class Event {

	private final int id;
	private long time;
	private String name;
	private String description;
	private boolean reminded;
	private String guildID;

	public Event(int id, String name, String description, String time, String guild) {
		this.id = id;
		this.time = Long.parseLong(time);
		this.name = name;
		this.description = description;
		this.reminded = false;
		this.guildID = guild;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReminded() {
		return reminded;
	}

	public void setReminded(boolean reminded) {
		this.reminded = reminded;
	}

	public String getGuildID() {
		return this.guildID;
	}

	public void setGuildID(String guild) {
		this.guildID = guild;
	}

	public int getId() {
		return id;
	}
}
