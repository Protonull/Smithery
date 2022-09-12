package uk.protonull.smithery;

import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.alloys.AlloyCombinations;
import uk.protonull.smithery.alloys.AlloyListener;
import uk.protonull.smithery.commands.CommandRegistrar;
import uk.protonull.smithery.config.Config;
import uk.protonull.smithery.forge.ForgeListener;
import uk.protonull.smithery.forge.ForgeManager;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class Smithery extends ACivMod {

    private CommandRegistrar commands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            Config.forceParseAll();
        }
        catch (final Throwable thrown) {
            getLogger().log(Level.WARNING, "Could not parse config!", thrown);
            disable();
            return;
        }
        this.commands = new CommandRegistrar(this);
        this.commands.init();
        registerListener(new ForgeListener());
        registerListener(new AlloyListener());
        ForgeManager.loadAllForges();
        AlloyCombinations.generateCombinations();
    }

    @Override
    public void onDisable() {
        AlloyCombinations.clearCombinations();
        ForgeManager.saveAllForges();
        Config.reset();
        if (this.commands != null) {
            this.commands.reset();
            this.commands = null;
        }
    }

    public static @NotNull Smithery getInstance() {
        return JavaPlugin.getPlugin(Smithery.class);
    }

}
