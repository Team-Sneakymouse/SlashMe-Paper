package net.sneakymouse.slashme;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import net.sneakymouse.slashme.commands.CommandMe;
import net.sneakymouse.slashme.commands.CommandMeSpy;
import net.sneakymouse.slashme.commands.CommandMee;
import net.sneakymouse.slashme.types.MeEntity;

public class SlashMe extends JavaPlugin implements Listener {

	public static final String IDENTIFIER = "slashme";

	private static SlashMe instance;
	
	public boolean papiActive = false;

	public Map<Player, MeEntity> playerChatBubbles = new HashMap<>();

    @Override
    public void onEnable() {
		instance = this;

		saveDefaultConfig();

		getServer().getCommandMap().register(IDENTIFIER, new CommandMe());
		getServer().getCommandMap().register(IDENTIFIER, new CommandMee());
		getServer().getCommandMap().register(IDENTIFIER, new CommandMeSpy());

		getServer().getPluginManager().registerEvents(this, this);

		getServer().getPluginManager().addPermission(new Permission(IDENTIFIER + ".admin"));

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

	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void removePlayer(Player player) {
		MeEntity meEntity = playerChatBubbles.remove(player);
		if (meEntity != null) meEntity.remove();
    }

	public static SlashMe getInstance(){
		return instance;
	}

}