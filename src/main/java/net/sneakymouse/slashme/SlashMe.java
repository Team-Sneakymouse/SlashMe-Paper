package net.sneakymouse.slashme;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.sneakymouse.slashme.commands.CommandMe;
import net.sneakymouse.slashme.types.MeEntity;

public class SlashMe extends JavaPlugin {

	public static final String IDENTIFIER = "slashme";

	private static SlashMe instance;
	
	public boolean papiActive = false;

	public Map<Player, MeEntity> playerChatBubbles = new HashMap<>();

    @Override
    public void onEnable() {
		instance = this;

		saveDefaultConfig();

		getServer().getCommandMap().register(IDENTIFIER, new CommandMe());

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			papiActive = true;
        }
    }

	@EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == this) {
			for (MeEntity chatBubble: playerChatBubbles.values()) {
				chatBubble.remove();
			}
			playerChatBubbles.clear();
		}
    }

    public void removePlayer(Player player, MeEntity chatBubble) {
    	chatBubble.remove();
    	playerChatBubbles.remove(player);
    }

	public static SlashMe getInstance(){
		return instance;
	}
}