package uk.protonull.smithery.commands;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.Smithery;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

public final class CommandRegistrar extends AikarCommandManager {

    public static final String ROOT_COMMAND_ALIAS = "smithery";
    public static final String ADMIN_PERMISSION = "smithery.admin";

    public CommandRegistrar(@NotNull final Smithery plugin) {
        super(Objects.requireNonNull(plugin), false);
    }

    @Override
    public void registerCommands() {
        registerCommand(new GiveForgeCommand());
        registerCommand(new ListRecipesCommand());
    }

    @Override
    public Smithery getPlugin() {
        return (Smithery) super.getPlugin();
    }

}
