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

public final class SmelteryVersion4 extends AbstractConfigParser {

    @Override
    public boolean matchesVersion() {
        return "1.4".equals(getConfig().getString("SmelteryConfigVersion"));
    }

    @Override
    public boolean allowLenientQualities() {
        return getConfig().getBoolean("EnableHints");
    }

    @Override
    public @NotNull List<ForgeRecipe> parseRecipes() {
        final var recipes = new HashMap<String, ForgeRecipe>();
        final ConfigurationSection section = getConfig().getConfigurationSection("Recipes");
        if (section != null) {
            this.logger.info("Loading Recipes");
            for (final String recipeKey : section.getKeys(false)) {
                this.logger.info(" Recipe Key: " + recipeKey);
                final ConfigurationSection recipeSection = section.getConfigurationSection(recipeKey);
                if (recipeSection == null) {
                    this.logger.info(" Recipe [" + recipeKey + "] is not a section!");
                    continue;
                }
                // Slug
                final String slug = recipeSection.getString("Tag");
                if (StringUtils.isBlank(slug)) {
                    this.logger.warning(" Recipe [" + recipeKey + "] has a blank tag!");
                    continue;
                }
                if (recipes.containsKey(slug)) {
                    this.logger.warning(" Recipe [" + recipeKey + "] tag [" + slug + "] is already in use!");
                    continue;
                }
                // Name
                final String name = recipeSection.getString("Name");
                if (StringUtils.isBlank(name)) {
                    this.logger.warning(" Recipe [" + recipeKey + "] has a blank name!");
                    continue;
                }
                // Smelt Time
                final int smeltTimeMinutes = recipeSection.getInt("SmeltTime");
                if (smeltTimeMinutes < 1) {
                    this.logger.warning(" Recipe [" + recipeKey + "] smelt time [" + smeltTimeMinutes + "] cannot be less than one.");
                    continue;
                }
                // Fail Chance
                double failPercentage = recipeSection.getDouble("FailChance");
                if (failPercentage < 0d) {
                    this.logger.warning(" Recipe [" + recipeKey + "] fail chance [" + failPercentage + "] is negative... clamping to 0%");
                    failPercentage = 0d;
                }
                else if (failPercentage > 100d) {
                    this.logger.warning(" Recipe [" + recipeKey + "] fail chance [" + failPercentage + "] is over 100%... clamping to 100%");
                    failPercentage = 100d;
                }
                // Ingredients
                final AmountMap<String> ingredients = INTERNAL_parseIngredientsList(recipeSection);
                recipes.put(slug, new ForgeRecipe(
                        slug,
                        name,
                        TimeUnit.MINUTES.toMillis(smeltTimeMinutes),
                        failPercentage,
                        ingredients
                ));
            }
        }
        return List.copyOf(recipes.values());
    }

    private @NotNull AmountMap<String> INTERNAL_parseIngredientsList(final @NotNull ConfigurationSection section) {
        final var map = new AmountMap.ArrayMap<String>();
        for (final String ingredient : ConfigHelper.getStringList(section, "Ingredients")) {
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
