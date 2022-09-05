package uk.protonull.smithery.commands;

import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.Smithery;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public final class CommandRegistrar extends CommandManager {

    public static final String ROOT_COMMAND_ALIAS = "smithery";
    public static final String ADMIN_PERMISSION = "smithery.admin";

    public CommandRegistrar(final @NotNull Smithery plugin) {
        super(plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerCommands() {
        enableUnstableAPI("help"); // Deprecated/Beta
        registerCommand(new GiveForgeCommand());
        registerCommand(new HelpCommand());
        registerCommand(new ListRecipesCommand());
    }

    @Override
    public @NotNull Smithery getPlugin() {
        return (Smithery) super.getPlugin();
    }

}

