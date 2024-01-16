package net.sneakymouse.slashme;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.sneakymouse.slashme.commands.CommandCleanTextDisplays;
import net.sneakymouse.slashme.commands.CommandSendMessage;
import net.sneakymouse.slashme.types.ChatBubble;

public class SlashMe extends JavaPlugin {

	public Map<Player, ChatBubble> playerChatBubbles = new HashMap<>();

	private static SlashMe instance;

    @Override
    public void onEnable() {
		instance = this;

		registerEvents();
		registerCommands();
    }

    @Override
    public void onDisable() {
    	for (ChatBubble chatBubble: playerChatBubbles.values()) {
    		chatBubble.remove();
    	}
    	playerChatBubbles.clear();
    }

	private void registerEvents(){
		//Commented out. Just un-comment if wanting all chat messages -> bubble chat
		//Bukkit.getServer().getPluginManager().registerEvents(new ChatEvents(), this);
	}

	private void registerCommands(){
		getServer().getCommandMap().register("slashme", new CommandCleanTextDisplays("cleantextdisplays"));
		getServer().getCommandMap().register("slashme", new CommandSendMessage("sendmessage"));
	}

    public void removePlayer(Player player, ChatBubble chatBubble) {
    	chatBubble.remove();
    	playerChatBubbles.remove(player);
    }

    public Collection<ChatBubble> getChatBubbles() {
		return playerChatBubbles.values();
	}


	public static SlashMe getInstance(){
		return instance;
	}
}