package net.sneakymouse.slashme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import net.sneakymouse.slashme.SlashMe;

public class CommandCleanTextDisplays extends Command {


	public CommandCleanTextDisplays(@NotNull String name) {
		super(name);
		this.description = "Clean up TextDisplays in a radius";
		this.usageMessage = "/" + this.getName() + " <radius>";
		this.setPermission("slashme.command.cleanuptextdisplays");
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if(!(sender instanceof Player player)) return false;

		int radius = 5;

		if(args.length > 0) {
			try{
				radius = Integer.parseInt(args[0]);
			} catch(NumberFormatException e){
				return false;
			}
		}

		for(Entity entity : player.getNearbyEntities(radius, radius, radius))
			if(entity instanceof TextDisplay && !SlashMe.getInstance().getChatBubbles().contains(entity)) entity.remove();

		return false;
	}

//	@Override
//	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		if (!(sender instanceof Player player)) return false;
//
//		int radius = 5;
//
//		if (args.length > 0) {
//			try {
//				radius = Integer.parseInt(args[0]);
//			} catch (NumberFormatException e) {
//				return false;
//			}
//		}
//
//		for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
//			if (entity instanceof TextDisplay && !slashme.getInstance().getChatBubbles().contains(entity)) {
//				entity.remove();
//			}
//		}
//		return true;
//	}



}