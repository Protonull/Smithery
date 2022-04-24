package uk.protonull.smithery.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.command.AikarCommand;

@CommandAlias(CommandRegistrar.ROOT_COMMAND_ALIAS)
public final class HelpCommand extends AikarCommand {

    @co.aikar.commands.annotation.HelpCommand
    public void help(final CommandSender sender,
                     final CommandHelp help) {
        help.showHelp();
    }

}
