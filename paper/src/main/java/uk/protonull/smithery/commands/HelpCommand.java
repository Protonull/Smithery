package uk.protonull.smithery.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;

@CommandAlias(CommandRegistrar.ROOT_COMMAND_ALIAS)
public final class HelpCommand extends BaseCommand {

    @Description("Shows all Smithery commands")
    @Syntax("")
    @co.aikar.commands.annotation.HelpCommand
    public void help(final CommandSender sender,
                     final CommandHelp help) {
        help.showHelp();
    }

}
