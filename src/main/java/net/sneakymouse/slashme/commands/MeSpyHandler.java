package net.sneakymouse.slashme.commands;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.sneakymouse.slashme.SlashMe;

public final class MeSpyHandler {

	private MeSpyHandler() {
	}

	public static void set(@NotNull Player player, @NotNull String setting) {
		@NonNull LuckPerms luckPerms = LuckPermsProvider.get();
		@Nullable User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		@NonNull NodeMap nodeMap = user.data();

		nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.none").build());
		nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.near").build());
		nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.global").build());

		if (!setting.equals("self"))
			nodeMap.add(Node.builder(SlashMe.IDENTIFIER + ".mespy." + setting).build());

		luckPerms.getUserManager().saveUser(user);
	}

}
