package net.sneakymouse.slashme;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.sneakymouse.slashme.commands.CommandMe;
import net.sneakymouse.slashme.types.MeEntity;

public class SlashMe extends JavaPlugin {

	public static final String IDENTIFIER = "slashme";

	private static SlashMe instance;

	public Map<Player, MeEntity> playerChatBubbles = new HashMap<>();

    @Override
    public void onEnable() {
		instance = this;

		getServer().getCommandMap().register(IDENTIFIER, new CommandMe());
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