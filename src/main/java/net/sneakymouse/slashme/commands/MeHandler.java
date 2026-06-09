package net.sneakymouse.slashme.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
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

	public static void execute(@NotNull Player executor, @NotNull String message, int duration, @NotNull String commandName) {
		execute(executor, executor, message, duration, commandName);
	}

	public static void execute(@NotNull Player executor, @NotNull LivingEntity target, @NotNull String message,
			int duration, @NotNull String commandName) {
		message = message.substring(0, Math.min(message.length(), 50));

		if (executor.hasPermission(SlashMe.IDENTIFIER + ".formatmes")) {
			message = MessageUtil.replaceFormatCodes(message);
		} else {
			message = MiniMessage.miniMessage().escapeTags(message.replaceAll("\\x{00A7}", "&"));
		}

		UUID targetId = target.getUniqueId();

		if (!SlashMe.getInstance().entityChatBubbles.containsKey(targetId)) {
			MeEntity chatBubble = new MeEntity(target, message);
			SlashMe.getInstance().entityChatBubbles.put(targetId, chatBubble);

			chatBubble.spawn();

			Bukkit.getServer().getScheduler().runTaskLater(SlashMe.getInstance(), () -> {
				if (chatBubble.removeMessage(0))
					SlashMe.getInstance().removeEntity(target);
			}, Math.max(message.length() * 2, duration));
		} else {
			MeEntity chatBubble = SlashMe.getInstance().entityChatBubbles.get(targetId);

			int messageID = chatBubble.addMessage(message);

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SlashMe.getInstance(), () -> {
				if (chatBubble.removeMessage(messageID))
					SlashMe.getInstance().removeEntity(target);
			}, duration);
		}

		if (!executor.hasPermission(SlashMe.IDENTIFIER + ".hidespy")) {
			String spyKey = targetId + "\0" + message;
			String lastMe = spyHistory.get(executor);

			if (lastMe != null && lastMe.equals(spyKey))
				return;

			spyHistory.put(executor, spyKey);
			if (shouldBroadcastMeSpy(commandName)) {
				meSpy(executor, target, message, commandName);
			}

			if (SlashMe.getInstance().coreprotectActive) {
				String logMessage = executor.equals(target)
						? "\u2215" + commandName + " " + message
						: "\u2215" + commandName + " " + target.getName() + " " + message;
				CoreProtect.getInstance().getAPI().logChat(executor, logMessage);
			}

			String character = executor.getName();
			if (SlashMe.getInstance().papiActive) {
				character = PlaceholderAPI.setPlaceholders(executor, "%sneakycharacters_character_name%")
						.replace("\"", "\\\"");
			}
			if (character.equals("%sneakycharacters_character_name%"))
				character = "";

			String username = executor.getName();
			String targetName = target.getName();
			Location location = target.getLocation();
			Double positionX = location.getX();
			Double positionY = location.getY();
			Double positionZ = location.getZ();
			String sanitisedMessage = message
					.replace("\\", "\\\\")
					.replace("\"", "\\\"");
			SlashMe.getInstance().lokiChatStream.log(
					"{ \"character\": \"" + character + "\", \"username\": \"" + username
							+ "\", \"target\": \"" + targetName.replace("\"", "\\\"")
							+ "\", \"position\": { \"x\": "
							+ positionX + ", \"y\": " + positionY + ", \"z\": " + positionZ + " }, \"message\": \""
							+ sanitisedMessage + "\" }");
		}
	}

	private static boolean shouldBroadcastMeSpy(String commandName) {
		if (!commandName.equals("you") && !commandName.equals("youu")) {
			return true;
		}
		return SlashMe.getInstance().getConfig().getBoolean("meSpyIncludeYou", false);
	}

	private static void meSpy(Player executor, LivingEntity target, String message, String commandName) {
		double meSpyNearRadiusSq = Math.pow(SlashMe.getInstance().getConfig().getInt("meSpyNearRadius", 12), 2);
		Location targetLocation = target.getLocation();

		Component defaultComponent = makeMeSpyComponent(executor, target, message, commandName, false);
		Component globalComponent = makeMeSpyComponent(executor, target, message, commandName, true);

		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (executor.equals(pl) && !pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.none")) {
				pl.sendMessage(defaultComponent);
			} else if ((pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.near")
					|| (pl.hasPermission(SlashMe.IDENTIFIER + ".admin")
							&& pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global")))
					&& (targetLocation.getWorld().equals(pl.getLocation().getWorld())
							&& targetLocation.distanceSquared(pl.getLocation()) < meSpyNearRadiusSq)) {
				pl.sendMessage(defaultComponent);
			} else if (pl.hasPermission(SlashMe.IDENTIFIER + ".admin")
					&& pl.hasPermission(SlashMe.IDENTIFIER + ".mespy.global")) {
				pl.sendMessage(globalComponent);
			}
		}
	}

	private static @NotNull Component makeMeSpyComponent(Player executor, LivingEntity target, String message,
			String commandName, boolean global) {
		String playerNameString = SlashMe.getInstance().getConfig().getString("playerNameString", "playerName")
				.replace("playerName", executor.getName());

		if (SlashMe.getInstance().papiActive) {
			playerNameString = PlaceholderAPI.setPlaceholders(executor, playerNameString);
		}

		if (!executor.equals(target)) {
			playerNameString += " \u2192 " + target.getName();
		}

		TextColor nameColor;

		if (global) {
			nameColor = coordsToRGB(target.getLocation().getBlockX(), target.getLocation().getBlockZ());
		} else {
			nameColor = NamedTextColor.GRAY;
		}

		Component nameComponent = Component.text("[/" + commandName + "] " + playerNameString).color(nameColor);

		String hoverText = "<yellow>Account name: <gold>" + ((TextComponent) executor.displayName()).content();

		if (SlashMe.getInstance().papiActive) {
			hoverText += PlaceholderAPI.setPlaceholders(executor, "\n<yellow>Voicechat: %cond_voicechat-status%");
		}

		if (global) {
			hoverText += "\n<reset>Teleport to player";

			String teleportTarget = target instanceof Player targetPlayer ? targetPlayer.getName() : null;
			if (teleportTarget != null) {
				nameComponent = nameComponent.clickEvent(ClickEvent.runCommand("/minecraft:tp " + teleportTarget));
			}
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
