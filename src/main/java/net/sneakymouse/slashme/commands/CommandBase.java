package net.sneakymouse.slashme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.sneakymouse.slashme.SlashMe;

public abstract class CommandBase extends Command {

    protected CommandBase(@NotNull String name) {
        super(name);
        this.setPermission(SlashMe.IDENTIFIER + ".command." + this.getName());
    }

    @Override
    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);
    
}
