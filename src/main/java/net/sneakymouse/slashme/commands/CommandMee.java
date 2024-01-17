package net.sneakymouse.slashme.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandMee extends CommandMe {

    public CommandMee() {
        super("mee");
        this.description = "Describe your actions in a holographic message on your body that lasts longer than normal.";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return handle(sender, commandLabel, args, 300);
    }

}
