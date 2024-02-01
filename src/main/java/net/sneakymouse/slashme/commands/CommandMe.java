package net.sneakymouse.slashme.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.HSVLike;
import net.sneakymouse.slashme.SlashMe;
import net.sneakymouse.slashme.types.MeEntity;
import net.sneakymouse.slashme.utils.MessageUtil;

public class CommandMe extends CommandBase {

    public CommandMe() {
        this("me");
    }

    protected CommandMe(@NotNull String name) {
        super(name);
        this.usageMessage = "/" + this.getName() + " [Message]";
        this.description = "Describe your actions in a holographic message on your body.";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return handle(sender, commandLabel, args, 120);
    }

    protected boolean handle(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args, int duration) {
        if(!(sender instanceof Player player)) return false;

        if(args.length == 0) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid Usage: " + this.usageMessage));
            return false;
        }

        String message = String.join(" ", args);
        message = message.substring(0, Math.min(message.length(), 50));

        if (player.hasPermission(SlashMe.IDENTIFIER + ".formatmes")) {
            message = MessageUtil.replaceFormatCodes(message);
        } else {
            message = MiniMessage.miniMessage().escapeTags(message.replaceAll("\\x{00A7}", "&"));
        }

        if(!SlashMe.getInstance().playerChatBubbles.containsKey(player)){
            MeEntity chatBubble = new MeEntity(player, message);
            SlashMe.getInstance().playerChatBubbles.put(player, chatBubble);

            chatBubble.spawn();

            Bukkit.getServer().getScheduler().runTaskLater(SlashMe.getInstance(), () -> {
                if (chatBubble.removeMessage(0)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, Math.max(message.length()*2, duration));
        } else{
            MeEntity chatBubble = SlashMe.getInstance().playerChatBubbles.get(player);

            int messageID = chatBubble.addMessage(message);

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), ()->{
                if (chatBubble.removeMessage(messageID)) SlashMe.getInstance().removePlayer(player, chatBubble);
            }, duration);
        }

        meSpy(player, message);

        return true;
    }

    private static void meSpy(Player player, String message){
        String escapeTags = MiniMessage.miniMessage().escapeTags(message);

        double meSpyNearRadiusSq = Math.pow(SlashMe.getInstance().getConfig().getInt("meSpyNearRadius", 12), 2);
        
        Component defaultComponent = makeMeSpyComponent(player, escapeTags, false);
        Component globalComponent = makeMeSpyComponent(player, escapeTags, true);

        for(Player pl : Bukkit.getOnlinePlayers()){
            if (player.equals(pl) && !pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.none")) {
                pl.sendMessage(defaultComponent);
            } else if ((pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.near") || (pl.hasPermission(SlashMe.IDENTIFIER + ".admin") && pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global"))) && (player.getLocation().getWorld().equals(pl.getLocation().getWorld()) && player.getLocation().distanceSquared(pl.getLocation()) < meSpyNearRadiusSq)) {
                pl.sendMessage(defaultComponent);
            } else if (pl.hasPermission(SlashMe.IDENTIFIER + ".admin") && pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global")) {
                pl.sendMessage(globalComponent);
            }
        }
    }

    private static @NotNull Component makeMeSpyComponent(Player player, String message, boolean global) {
        String playerNameString = SlashMe.getInstance().getConfig().getString("playerNameString", "playerName").replace("playerName", player.getName());

        if (SlashMe.getInstance().papiActive) {
            playerNameString = PlaceholderAPI.setPlaceholders(player, playerNameString);
        }

        TextColor nameColor;
        
        if (global) {
            nameColor = coordsToRGB(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
        } else {
            nameColor = NamedTextColor.GRAY;
        }

        Component nameComponent = Component.text("[/me] " + playerNameString).color(nameColor);

        String hoverText = "<yellow>Account name: <gold>" + ((TextComponent) player.displayName()).content();

        if (SlashMe.getInstance().papiActive) {
            hoverText += PlaceholderAPI.setPlaceholders(player, "\n<yellow>Voicechat: %cond_voicechat-status%");
        }

        if (global) {
            hoverText += "\n<reset>Teleport to player";

            nameComponent = nameComponent.clickEvent(ClickEvent.runCommand("/minecraft:tp " + player.getName()));
        }
        nameComponent = nameComponent.hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(MessageUtil.replaceFormatCodes(hoverText))));

        Component colonComponent = Component.text(": " + message).color(NamedTextColor.GRAY);

        return List.of(nameComponent, colonComponent).stream().collect(Component.toComponent());
    }

    private static @NotNull TextColor coordsToRGB(int x, int z) {
		int xMin = SlashMe.getInstance().getConfig().getInt("xMin", 4400);
		int xMax = SlashMe.getInstance().getConfig().getInt("xMax", 5600);
		int yMin = SlashMe.getInstance().getConfig().getInt("yMin", 4400);
		int yMax = SlashMe.getInstance().getConfig().getInt("yMax", 5600);

		double scaledX = (2 * (x - xMin) / (double) (xMax - xMin)) - 1;
		double scaledZ = (2 * (z - yMin) / (double) (yMax - yMin)) - 1;

		double hue = Math.toDegrees(Math.atan2(scaledZ, scaledX));
		hue = (hue + 360) % 360;

		double saturation = Math.sqrt(scaledX * scaledX + scaledZ * scaledZ);
        saturation = Math.max(0, Math.min(1, saturation));

		double brightness = 0.75;

        return TextColor.color(HSVLike.hsvLike((float) hue / 360, (float) saturation, (float) brightness));
	}

}
