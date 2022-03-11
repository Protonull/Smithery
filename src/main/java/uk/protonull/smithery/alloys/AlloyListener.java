package uk.protonull.smithery.alloys;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;
import uk.protonull.smithery.utilities.Utilities;

public final class AlloyListener implements Listener {

    @EventHandler
    public void cancelDefaultRecipes(final PrepareItemCraftEvent event) {
        final Recipe recipe = event.getRecipe();
        if (recipe != null) {
            final boolean hasAlloyIngredients = AlloyUtils.doesMatrixContainAlloys(event.getInventory());
            final boolean definesAlloyIngredients = AlloyUtils.doesRecipeDefineAlloyIngredients(recipe);
            if (hasAlloyIngredients ^ definesAlloyIngredients) { // XOR
                Utilities.cancelPrepareItemCraftEvent(event);
            }
        }
    }

}
