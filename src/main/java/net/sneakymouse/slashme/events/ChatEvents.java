package net.sneakymouse.slashme.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.sneakymouse.slashme.SlashMe;
import net.sneakymouse.slashme.types.ChatBubble;

public class ChatEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Cancel the default chat message
        event.setCancelled(true);

        Player player = event.getPlayer();
        final String message = event.getMessage();

        if (!SlashMe.getInstance().playerChatBubbles.containsKey(player)) {
            // Create the display entity and attach it to a player
            ChatBubble chatBubble = new ChatBubble(player, message);
            SlashMe.getInstance().playerChatBubbles.put(player, chatBubble);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), chatBubble::spawn);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), () -> {
                if (chatBubble.removeMessage(0)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, Math.max(message.length(), 60));


        } else {
            // Get the display entity and add new lines to it
            ChatBubble chatBubble = SlashMe.getInstance().playerChatBubbles.get(player);

            int messageID = chatBubble.addMessage(message);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), ()->{
                if (chatBubble.removeMessage(messageID)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, Math.max(message.length(), 60));


        }
    }

}
