package uk.protonull.smithery.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.alloys.Alloy;
import uk.protonull.smithery.alloys.AlloyQuality;
import uk.protonull.smithery.alloys.AlloyUtils;
import uk.protonull.smithery.config.Config;
import uk.protonull.smithery.forge.ForgeRecipe;
import uk.protonull.smithery.utilities.Utilities;

@CommandAlias(CommandRegistrar.ROOT_COMMAND_ALIAS)
public final class ListRecipesCommand extends BaseCommand {

    @Subcommand("list")
    @Description("Lists all known Forge recipes")
    @CommandPermission(CommandRegistrar.ADMIN_PERMISSION)
    public void listRecipes(final CommandSender sender) {
        TextComponent.Builder response = Component.text()
                .color(NamedTextColor.GOLD)
                .content("All known Forge recipes:");
        if (Config.RECIPES.get().isEmpty()) {
            response.append(Component.text(" <none>", NamedTextColor.RED));
            sender.sendMessage(response);
            return;
        }
        sender.sendMessage(response);
        for (final ForgeRecipe recipe : Config.RECIPES.get()) {
            response = Component.text();
            response.append(
                    Component.text(" • "),
                    Component.text()
                            .color(NamedTextColor.YELLOW)
                            .content(recipe.slug())
                            .hoverEvent(HoverEvent.showText(Component.text(recipe.name())))
            );
            if (sender instanceof Player) {
                response.append(
                        Component.text()
                                .color(NamedTextColor.GREEN)
                                .content(" Completed[")
                                .append(INTERNAL_getQualityOptions(recipe, true))
                                .append(Component.text(']')),
                        Component.text()
                                .color(NamedTextColor.RED)
                                .content(" Molten[")
                                .append(INTERNAL_getQualityOptions(recipe, false))
                                .append(Component.text(']'))
                );
            }
            sender.sendMessage(response);
        }
    }

    private @NotNull List<Component> INTERNAL_getQualityOptions(final @NotNull ForgeRecipe recipe,
                                                                final boolean isCompleteAlloy) {
        final AlloyQuality[] qualities = AlloyQuality.values();
        final var options = new ArrayList<Component>((qualities.length * 2) - 1);
        for (final Iterator<AlloyQuality> iterator = IteratorUtils.arrayIterator(qualities); iterator.hasNext();) {
            final AlloyQuality quality = iterator.next();
            options.add(Component.text()
                    .color(NamedTextColor.AQUA)
                    .content(quality.name().substring(0, 1))
                    .hoverEvent(HoverEvent.showText(Component.text().append(
                            Component.text()
                                    .color(NamedTextColor.YELLOW)
                                    .content("Quality: "),
                            Component.text()
                                    .color(NamedTextColor.AQUA)
                                    .content(quality.name()),
                            Component.newline(),
                            Component.text()
                                    .color(NamedTextColor.GOLD)
                                    .content("Click to produce this Alloy at this quality!")
                    )))
                    .clickEvent(ClickEvent.runCommand(
                            "/smithery ADMIN_GENERATE_ALLOY " + isCompleteAlloy + " " + recipe.slug() + " " + quality.name()
                    ))
                    .build());
            if (iterator.hasNext()) {
                options.add(Component.text(","));
            }
        }
        return options;
    }

    @Subcommand("ADMIN_GENERATE_ALLOY")
    @CommandPermission(CommandRegistrar.ADMIN_PERMISSION)
    @Private
    public void generateAlloy(final Player sender,
                              final boolean isFullAlloy,
                              final String recipeSlug,
                              final String qualitySlug) {
        final ForgeRecipe recipe = Config.matchRecipe(recipeSlug);
        if (recipe == null) {
            throw new InvalidCommandArgument("That recipe does not exist!");
        }
        final AlloyQuality quality = EnumUtils.getEnum(AlloyQuality.class, qualitySlug);
        if (quality == null) {
            throw new InvalidCommandArgument("That quality does not exist!");
        }
        final Alloy alloy = new Alloy(recipe.slug(), quality);
        if (isFullAlloy) {
            Utilities.giveOrDropItem(sender.getInventory(), AlloyUtils.createAlloyFromRecipe(recipe, quality));
        }
        else {
            Utilities.giveOrDropItem(sender.getInventory(), AlloyUtils.newMoltenAlloy(alloy));
        }
        sender.sendMessage(Component.text("You've generated an Alloy.", NamedTextColor.GREEN));
    }

}
