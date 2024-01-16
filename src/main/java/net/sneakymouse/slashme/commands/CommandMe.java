package net.sneakymouse.slashme.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.sneakymouse.slashme.SlashMe;
import net.sneakymouse.slashme.types.MeEntity;

public class CommandMe extends Command {


    public CommandMe() {
        super("me");
        this.usageMessage = "/" + this.getName() + " [Message]";
        this.description = "Describe your actions in a holographic message on your body.";
        this.setPermission(SlashMe.IDENTIFIER + ".command.me");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length == 0) {
            player.sendMessage(Component.text(ChatColor.RED + "Invalid Usage: " + this.usageMessage));
            return false;
        }

        String message = String.join(" ", args);
        message = message.substring(0, Math.min(message.length(), 50));

        if(!SlashMe.getInstance().playerChatBubbles.containsKey(player)){
            MeEntity chatBubble = new MeEntity(player, message);
            SlashMe.getInstance().playerChatBubbles.put(player, chatBubble);

            chatBubble.spawn();

            Bukkit.getServer().getScheduler().runTaskLater(SlashMe.getInstance(), () -> {
                if (chatBubble.removeMessage(0)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, Math.max(message.length()*2, 120));
        } else{
            MeEntity chatBubble = SlashMe.getInstance().playerChatBubbles.get(player);

            int messageID = chatBubble.addMessage(message);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), ()->{
                if (chatBubble.removeMessage(messageID)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, Math.max(message.length()*2, 120));
        }

        logSpy(player.getName(), message);

        return true;
    }

    private void logSpy(String user, String message){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.hasPermission("slashme.mespy"))
                player.sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', "&7[&e/Me&7]: &e" + user + "&7: " + message)));
        }
    }
}
