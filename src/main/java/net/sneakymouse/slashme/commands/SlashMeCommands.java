package net.sneakymouse.slashme.commands;

import java.util.List;

import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sneakymouse.slashme.SlashMe;

public final class SlashMeCommands {

	private static final String ME_DESCRIPTION = "Describe your actions in a holographic message on your body.";
	private static final String MEE_DESCRIPTION = "Describe your actions in a holographic message on your body that lasts longer than normal.";
	private static final String MESPY_DESCRIPTION = "Configure which /me messages you can see.";

	private SlashMeCommands() {
	}

	public static void register(Commands registrar) {
		registrar.register(meCommand("me", 120), ME_DESCRIPTION, List.of());
		registrar.register(meCommand("mee", 300), MEE_DESCRIPTION, List.of());
		registrar.register(meSpyCommand(), MESPY_DESCRIPTION, List.of());
	}

	private static LiteralCommandNode<CommandSourceStack> meCommand(String name, int duration) {
		return Commands.literal(name)
				.requires(source -> source.getExecutor() instanceof Player
						&& source.getSender().hasPermission(commandPermission(name)))
				.executes(ctx -> {
					ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage()
							.deserialize("<red>Invalid Usage: /" + name + " [Message]"));
					return 0;
				})
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(ctx -> {
							Player player = (Player) ctx.getSource().getExecutor();
							String message = StringArgumentType.getString(ctx, "message");
							MeHandler.execute(player, message, duration);
							return Command.SINGLE_SUCCESS;
						}))
				.build();
	}

	private static LiteralCommandNode<CommandSourceStack> meSpyCommand() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mespy")
				.requires(source -> source.getExecutor() instanceof Player
						&& source.getSender().hasPermission(commandPermission("mespy")))
				.executes(ctx -> {
					ctx.getSource().getSender().sendMessage(Component
							.text("Invalid Usage: /mespy [none/self/near/global]").color(NamedTextColor.RED));
					return 0;
				});

		for (String mode : List.of("none", "self", "near")) {
			builder = builder.then(Commands.literal(mode).executes(ctx -> executeMeSpy(ctx, mode)));
		}

		builder = builder.then(Commands.literal("global")
				.requires(source -> source.getSender().hasPermission(SlashMe.IDENTIFIER + ".admin"))
				.executes(ctx -> executeMeSpy(ctx, "global")));

		return builder.build();
	}

	private static int executeMeSpy(CommandContext<CommandSourceStack> ctx, String mode) {
		Player player = (Player) ctx.getSource().getExecutor();
		MeSpyHandler.set(player, mode);
		player.sendMessage(Component.text("Your MeSpy setting has been updated to ").color(NamedTextColor.GREEN)
				.append(Component.text("'" + mode + "'").color(NamedTextColor.AQUA))
				.append(Component.text(".").color(NamedTextColor.GREEN)));
		return Command.SINGLE_SUCCESS;
	}

	private static String commandPermission(String command) {
		return SlashMe.IDENTIFIER + ".command." + command;
	}

}
