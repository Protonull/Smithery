package uk.protonull.smithery.alloys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import uk.protonull.smithery.config.Config;
import uk.protonull.smithery.forge.ForgeRecipe;
import uk.protonull.smithery.utilities.Utilities;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

@UtilityClass
public class AlloyCombinations {

    private final CivLogger LOGGER = CivLogger.getLogger(AlloyCombinations.class);
    private final List<AlloyQuality> QUALITIES = Stream.of(AlloyQuality.values())
            .filter(Predicate.not(AlloyQuality::isBest))
            .toList();
    private final List<Recipe> RECIPES = new ArrayList<>();
    private int counter = 0;

    /**
     * Generates recipes that allow players to craft better versions of Alloys by combining them in a crafting matrix.
     */
    public void generateCombinations() {
        for (final ForgeRecipe recipe : Config.RECIPES.get()) {
            for (final AlloyQuality quality : QUALITIES) {
                final var shapedRecipe = new ShapedRecipe(
                        Utilities.key("smithery", "combination_" + (counter++)),
                        AlloyUtils.createAlloyFromRecipe(recipe, quality.upgrade()));
                shapedRecipe.shape("xx", "xx");
                shapedRecipe.setIngredient('x', AlloyUtils.createAlloyFromRecipe(recipe, quality));
                if (!RecipeManager.registerRecipe(shapedRecipe)) {
                    LOGGER.warning("Could not register Alloy[" + recipe.slug() + "] combination "
                            + "from [" + quality + "] to [" + quality.upgrade() + "]");
                    continue;
                }
                RECIPES.add(shapedRecipe);
            }
        }
    }

    /**
     * Clears all generated combinations.
     */
    public void clearCombinations() {
        RECIPES.forEach(RecipeManager::removeRecipe);
        RECIPES.clear();
        counter = 0;
    }

}
