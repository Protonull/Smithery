package uk.protonull.smithery.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.protonull.smithery.forge.ForgeUtils;
import uk.protonull.smithery.utilities.Utilities;

@CommandAlias(CommandRegistrar.ROOT_COMMAND_ALIAS)
public final class GiveForgeCommand extends BaseCommand {

    @Subcommand("give")
    @Description("Gives a Forge item to a target, or yourself.")
    @Syntax("[target]")
    @CommandCompletion("@players")
    @CommandPermission(CommandRegistrar.ADMIN_PERMISSION)
    public void giveForge(final Player sender, @Optional final OnlinePlayer target) {
        if (target == null || target.getPlayer() == sender) {
            Utilities.giveOrDropItem(sender.getInventory(), ForgeUtils.newForgeItem());
            sender.sendMessage(ChatColor.GREEN + "You have been given a Forge item");
        }
        else {
            final Player recipient = target.getPlayer();
            Utilities.giveOrDropItem(recipient.getInventory(), ForgeUtils.newForgeItem());
            sender.sendMessage(ChatColor.GREEN + "You have given " + recipient.getName() + " a Forge item");
            recipient.sendMessage(ChatColor.GREEN + "You have been given a Forge item");
        }
    }

}
