package uk.protonull.smithery.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.Smithery;
import uk.protonull.smithery.forge.ForgeRecipe;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public abstract class AbstractConfigParser {

    protected final CivLogger logger = CivLogger.getLogger(getClass());

    /**
     * @return Returns the config to parse from.
     */
    protected final @NotNull FileConfiguration getConfig() {
        return Smithery.getInstance().getConfig();
    }

    /**
     * @return Returns true if this parser supports this config and thus should be used to parse the config.
     */
    public abstract boolean matchesVersion();

    /**
     * @return Returns true if the Forges can produce lesser quality Alloys.
     */
    public abstract boolean allowLenientQualities();

    /**
     * @return Returns a list of unique Forge recipes.
     */
    public abstract @NotNull List<ForgeRecipe> parseRecipes();

}
