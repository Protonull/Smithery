package uk.protonull.smithery.utilities;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.alloys.Alloy;
import uk.protonull.smithery.alloys.AlloyUtils;

@UtilityClass
public class IngredientMatcher {

    @FunctionalInterface
    public interface Matcher {
        /**
         * Attempts to match the given item with known custom items, returning the ID of that custom item.
         *
         * @param ingredient The ingredient to an ID for.
         * @return Returns a custom ingredient ID if matched, or null.
         */
        String getCustomIngredientID(@NotNull ItemStack ingredient);
    }

    private Matcher MATCHER = null;

    /**
     * This allows you to set a custom matcher, should you choose to.
     *
     * @param finder The new matcher to set, which can be null.
     */
    public void setFinder(final Matcher finder) {
        MATCHER = finder;
    }

    /**
     * Converts an item into an ingredient ID.
     *
     * @param ingredient The ingredient to an ID for.
     * @return Returns an ingredient ID.
     */
    public @NotNull String getIngredientID(final @NotNull ItemStack ingredient) {
        final Alloy alloy = AlloyUtils.getAlloyFromItem(ingredient.getItemMeta());
        if (alloy != null) {
            return alloy.generateKey();
        }
        if (MATCHER != null) {
            final String found = MATCHER.getCustomIngredientID(ingredient);
            if (StringUtils.isNotBlank(found)) {
                return found.toUpperCase();
            }
        }
        return ingredient.getType().name();
    }

}
