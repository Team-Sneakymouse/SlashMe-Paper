package net.sneakymouse.slashme.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.NodeMap;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.sneakymouse.slashme.SlashMe;

public class CommandMeSpy extends CommandBase {

    public CommandMeSpy() {
        super("mespy");
        this.usageMessage = "/" + this.getName() + " [none/self/near/global]";
        this.description = "Describe your actions in a holographic message on your body.";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(Component.text("Invalid Usage: " + this.usageMessage).color(NamedTextColor.RED));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "none" :
            case "self" :
            case "near" :
                set(player, args[0].toLowerCase());
                break;
            case "global" :
                if (player.hasPermission(SlashMe.IDENTIFIER + ".admin")) {
                    set(player, args[0].toLowerCase());
                    break;
                } else {
                    player.sendMessage(Component.text("You don't have permission to use the global MeSpy.").color(NamedTextColor.RED));
                    return false;
                }
            default:
                player.sendMessage(Component.text("Invalid Usage: " + this.usageMessage).color(NamedTextColor.RED));
                return false;
        }

        player.sendMessage(Component.text("Your MeSpy setting has been updated to ").color(NamedTextColor.GREEN).append(Component.text("'" + args[0] + "'").color(NamedTextColor.AQUA).append(Component.text(".").color(NamedTextColor.GREEN))));
        return true;
    }

    private static void set(@NotNull Player player, @NotNull String setting) {
        @NonNull LuckPerms luckPerms = LuckPermsProvider.get();
        @Nullable User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        @NonNull NodeMap nodeMap = user.data();

        nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.none").build());
        nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.near").build());
        nodeMap.remove(Node.builder(SlashMe.IDENTIFIER + ".mespy.global").build());

        if (!setting.equals("self")) nodeMap.add(Node.builder(SlashMe.IDENTIFIER + ".mespy." + setting).build());

        luckPerms.getUserManager().saveUser(user);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if(args.length == 1){
            if (sender instanceof Player player && player.hasPermission(SlashMe.IDENTIFIER + ".admin")) {
                return List.of("none", "self", "near", "global");
            } else {
                return List.of("none", "self", "near");
            }
        } else{
            return List.of();
        }
    }
    
}
