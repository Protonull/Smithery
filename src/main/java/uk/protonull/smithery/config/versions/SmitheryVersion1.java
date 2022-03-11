package uk.protonull.smithery.config.versions;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.alloys.Alloy;
import uk.protonull.smithery.config.AbstractConfigParser;
import uk.protonull.smithery.forge.ForgeRecipe;
import uk.protonull.smithery.utilities.AmountMap;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;

public final class SmitheryVersion1 extends AbstractConfigParser {

    @Override
    public boolean matchesVersion() {
        return "1".equals(getConfig().getString("SmitheryConfig"));
    }

    @Override
    public boolean allowLenientQualities() {
        return getConfig().getBoolean("AllowLenientQualities");
    }

    @NotNull
    @Override
    public List<ForgeRecipe> parseRecipes() {
        final var recipes = new HashMap<String, ForgeRecipe>();
        final ConfigurationSection section = getConfig().getConfigurationSection("Recipes");
        if (section != null) {
            this.logger.info("Loading Recipes");
            for (final String slug : section.getKeys(false)) {
                this.logger.info(" Recipe Key: " + slug);
                final ConfigurationSection recipeSection = section.getConfigurationSection(slug);
                if (recipeSection == null) {
                    this.logger.info(" Recipe [" + slug + "] is not a section!");
                    continue;
                }
                // Name
                final String name = recipeSection.getString("name");
                if (StringUtils.isBlank(name)) {
                    this.logger.warning(" Recipe [" + slug + "] has a blank name!");
                    continue;
                }
                // Smelt Time
                final int cookTimeSeconds = recipeSection.getInt("cookTime");
                if (cookTimeSeconds < 10) {
                    this.logger.warning(" Recipe [" + slug + "] cook time [" + cookTimeSeconds + "] cannot be less than ten seconds.");
                    continue;
                }
                // Fail Chance
                double failPercentage = recipeSection.getDouble("failChance");
                if (failPercentage < 0d) {
                    this.logger.warning(" Recipe [" + slug + "] fail chance [" + failPercentage + "] is negative... clamping to 0%");
                    failPercentage = 0d;
                }
                else if (failPercentage > 100d) {
                    this.logger.warning(" Recipe [" + slug + "] fail chance [" + failPercentage + "] is over 100%... clamping to 100%");
                    failPercentage = 100d;
                }
                // Ingredients
                final AmountMap<String> ingredients = INTERNAL_parseIngredientsList(recipeSection);
                recipes.put(slug, new ForgeRecipe(
                        slug,
                        name,
                        TimeUnit.SECONDS.toMillis(cookTimeSeconds),
                        failPercentage,
                        ingredients
                ));
            }
        }
        return List.copyOf(recipes.values());
    }

    @NotNull
    private AmountMap<String> INTERNAL_parseIngredientsList(@NotNull final ConfigurationSection section) {
        final var map = new AmountMap.ArrayMap<String>();
        for (final String ingredient : ConfigHelper.getStringList(section, "ingredients")) {
            final String[] parts = StringUtils.split(ingredient, "/");
            if (parts.length != 2) {
                this.logger.warning("Ingredient [" + ingredient + "] is not valid!");
                continue;
            }
            parts[0] = Alloy.fromKey(parts[0]).generateKey();
            final int amount;
            try {
                amount = Integer.parseInt(parts[1]);
            }
            catch (final Throwable thrown) {
                this.logger.warning("Ingredient [" + ingredient + "] must have an integer amount!");
                continue;
            }
            if (amount < 1) {
                this.logger.warning("Ingredient [" + ingredient + "] must have a positive integer amount!");
                continue;
            }
            map.put(parts[0], amount);
        }
        return map;
    }

}
