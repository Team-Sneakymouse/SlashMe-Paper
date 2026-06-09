package net.sneakymouse.slashme.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.sneakymouse.slashme.SlashMe;

public final class SlashMeCommands {

	private static final ArgumentType<EntitySelectorArgumentResolver> ENTITY_ARGUMENT = ArgumentTypes.entity();

	private static final String ME_DESCRIPTION = "Describe your actions in a holographic message on your body.";
	private static final String MEE_DESCRIPTION = "Describe your actions in a holographic message on your body that lasts longer than normal.";
	private static final String YOU_DESCRIPTION = "Describe another entity's actions in a holographic message on their body.";
	private static final String YOUU_DESCRIPTION = "Describe another entity's actions in a holographic message on their body that lasts longer than normal.";
	private static final String MESPY_DESCRIPTION = "Configure which /me messages you can see.";

	private SlashMeCommands() {
	}

	public static void register(Commands registrar) {
		registrar.register(meCommand("me", 120), ME_DESCRIPTION, List.of());
		registrar.register(meCommand("mee", 300), MEE_DESCRIPTION, List.of());
		registrar.register(youCommand("you", 120), YOU_DESCRIPTION, List.of());
		registrar.register(youCommand("youu", 300), YOUU_DESCRIPTION, List.of());
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
							MeHandler.execute(player, message, duration, name);
							return Command.SINGLE_SUCCESS;
						}))
				.build();
	}

	private static LiteralCommandNode<CommandSourceStack> youCommand(String name, int duration) {
		RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> targetArgument = entityArgument("target")
				.executes(ctx -> {
					ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage()
							.deserialize("<red>Invalid Usage: /" + name + " <target> <Message>"));
					return 0;
				})
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(ctx -> executeYou(ctx, name, duration)));

		return Commands.literal(name)
				.requires(source -> source.getExecutor() instanceof Player
						&& source.getSender().hasPermission(commandPermission(name)))
				.executes(ctx -> {
					ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage()
							.deserialize("<red>Invalid Usage: /" + name + " <target> <Message>"));
					return 0;
				})
				.then(targetArgument)
				.build();
	}

	private static RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> entityArgument(
			String name) {
		return Commands.argument(name, ENTITY_ARGUMENT)
				.suggests(SlashMeCommands::suggestEntity);
	}

	private static <S> CompletableFuture<Suggestions> suggestEntity(CommandContext<S> context,
			SuggestionsBuilder builder) {
		if (ENTITY_ARGUMENT instanceof CustomArgumentType<?, ?> customArgument) {
			return customArgument.getNativeType().listSuggestions(context, builder);
		}
		return ENTITY_ARGUMENT.listSuggestions(context, builder);
	}

	private static int executeYou(CommandContext<CommandSourceStack> ctx, String commandName, int duration) {
		Player executor = (Player) ctx.getSource().getExecutor();
		EntitySelectorArgumentResolver targetResolver = ctx.getArgument("target", EntitySelectorArgumentResolver.class);

		final Entity entity;
		try {
			entity = targetResolver.resolve(ctx.getSource()).getFirst();
		} catch (CommandSyntaxException exception) {
			executor.sendMessage(Component.text(exception.getMessage()).color(NamedTextColor.RED));
			return 0;
		}

		if (!(entity instanceof LivingEntity target)) {
			executor.sendMessage(Component.text("That entity cannot display /you messages.")
					.color(NamedTextColor.RED));
			return 0;
		}

		String message = StringArgumentType.getString(ctx, "message");
		MeHandler.execute(executor, target, message, duration, commandName);
		return Command.SINGLE_SUCCESS;
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
