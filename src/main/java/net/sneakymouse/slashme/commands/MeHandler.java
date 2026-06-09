package net.sneakymouse.slashme.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.PlaceholderAPI;
import net.coreprotect.CoreProtect;
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

public final class MeHandler {

	private static final Map<Player, String> spyHistory = new HashMap<>();

	private MeHandler() {
	}

	public static void execute(@NotNull Player player, @NotNull String message, int duration) {
		message = message.substring(0, Math.min(message.length(), 50));

		if (player.hasPermission(SlashMe.IDENTIFIER + ".formatmes")) {
			message = MessageUtil.replaceFormatCodes(message);
		} else {
			message = MiniMessage.miniMessage().escapeTags(message.replaceAll("\\x{00A7}", "&"));
		}

		if (!SlashMe.getInstance().playerChatBubbles.containsKey(player)) {
			MeEntity chatBubble = new MeEntity(player, message);
			SlashMe.getInstance().playerChatBubbles.put(player, chatBubble);

			chatBubble.spawn();

			Bukkit.getServer().getScheduler().runTaskLater(SlashMe.getInstance(), () -> {
				if (chatBubble.removeMessage(0))
					SlashMe.getInstance().removePlayer(player);
			}, Math.max(message.length() * 2, duration));
		} else {
			MeEntity chatBubble = SlashMe.getInstance().playerChatBubbles.get(player);

			int messageID = chatBubble.addMessage(message);

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), () -> {
				if (chatBubble.removeMessage(messageID))
					SlashMe.getInstance().removePlayer(player);
			}, duration);
		}

		if (!player.hasPermission(SlashMe.IDENTIFIER + ".hidespy")) {
			String lastMe = spyHistory.get(player);

			if (lastMe != null && lastMe.equals(message))
				return;

			spyHistory.put(player, message);
			meSpy(player, message);

			if (SlashMe.getInstance().coreprotectActive) {
				CoreProtect.getInstance().getAPI().logChat(player, "\u2215me " + message);
			}

			String character = player.getName();
			if (SlashMe.getInstance().papiActive) {
				character = PlaceholderAPI.setPlaceholders(player, "%sneakycharacters_character_name%")
						.replace("\"", "\\\"");
			}
			if (character.equals("%sneakycharacters_character_name%"))
				character = "";

			String username = player.getName();
			Double positionX = player.getLocation().getX();
			Double positionY = player.getLocation().getY();
			Double positionZ = player.getLocation().getZ();
			String sanitisedMessage = message
					.replace("\\", "\\\\")
					.replace("\"", "\\\"");
			SlashMe.getInstance().lokiChatStream.log(
					"{ \"character\": \"" + character + "\", \"username\": \"" + username
							+ "\", \"position\": { \"x\": "
							+ positionX + ", \"y\": " + positionY + ", \"z\": " + positionZ + " }, \"message\": \""
							+ sanitisedMessage + "\" }");
		}
	}

	private static void meSpy(Player player, String message) {
		double meSpyNearRadiusSq = Math.pow(SlashMe.getInstance().getConfig().getInt("meSpyNearRadius", 12), 2);

		Component defaultComponent = makeMeSpyComponent(player, message, false);
		Component globalComponent = makeMeSpyComponent(player, message, true);

		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (player.equals(pl) && !pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.none")) {
				pl.sendMessage(defaultComponent);
			} else if ((pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.near")
					|| (pl.hasPermission(SlashMe.IDENTIFIER + ".admin")
							&& pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global")))
					&& (player.getLocation().getWorld().equals(pl.getLocation().getWorld())
							&& player.getLocation().distanceSquared(pl.getLocation()) < meSpyNearRadiusSq)) {
				pl.sendMessage(defaultComponent);
			} else if (pl.hasPermission(SlashMe.IDENTIFIER + ".admin")
					&& pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global")) {
				pl.sendMessage(globalComponent);
			}
		}
	}

	private static @NotNull Component makeMeSpyComponent(Player player, String message, boolean global) {
		String playerNameString = SlashMe.getInstance().getConfig().getString("playerNameString", "playerName")
				.replace("playerName", player.getName());

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
		nameComponent = nameComponent.hoverEvent(
				HoverEvent.showText(MiniMessage.miniMessage().deserialize(MessageUtil.replaceFormatCodes(hoverText))));

		Component colonComponent = Component.text(": " + MessageUtil.removeFormatCodes(message))
				.color(NamedTextColor.GRAY);

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
