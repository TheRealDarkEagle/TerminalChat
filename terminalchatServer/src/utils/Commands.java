package utils;

import java.util.HashMap;
import java.util.Map;

public class Commands {

	private Map<String, String> usercommands;
	private Map<String, String> admincommands;

	public Commands() {
		loadDefaultCommands();
		loadAdminCommands();
	}

	/*
	 * Gibt die aktuell verfügbaren commandos zurück
	 */
	public Map<String, String> getCommands(boolean isAdmin) {
		Map<String, String> commandlist = new HashMap<>();
		commandlist.putAll(usercommands);
		if (isAdmin) {
			commandlist.putAll(admincommands);
		}
		return commandlist;
	}

	public void addCommand(String key, String description) {
		this.usercommands.put(key, description);
	}

	private void loadDefaultCommands() {
		this.usercommands = new HashMap<>();
		usercommands.put("/#time", "Gibt die aktuelle Serverzeit zurück");
		usercommands.put("/#com", "Siehe dir die Kommandos an");
		usercommands.put("/#name *newName*", "Wechselt deinen Usernamen");
		usercommands.put("/#userlist", "Gibt die Liste mit Usernamen zurück");
		usercommands.put("/#@ *userID* *Message*", "Schickt eine private nachricht an den Users");
//		commands.put("/#afk", "Setz deinen Status auf afk");
	}

	private void loadAdminCommands() {
		admincommands = new HashMap<>();
		admincommands.put("/#kick*userId*", "Kickt den User vom Server");
	}

}
