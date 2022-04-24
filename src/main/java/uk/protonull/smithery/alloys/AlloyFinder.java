package uk.protonull.smithery.alloys;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class AlloyFinder {

    @FunctionalInterface
    public interface Finder {
        /**
         * Determines whether a particular interaction can occur.
         *
         * @param ingredient The ingredient to convert into an alloy.
         * @return Returns an alloy-slug or null.
         */
        String find(@NotNull ItemStack ingredient);
    }

    public final Finder DEFAULT_FINDER = (item) -> {
        final Alloy alloy = AlloyUtils.getAlloyFromItem(item.getItemMeta());
        return alloy == null ? item.getType().name() : alloy.generateKey();
    };

    private Finder FINDER = DEFAULT_FINDER;

    /**
     * This allows you to set a custom finder, should you choose to. If you wish to reset the finder to the default,
     * just pass in {@link #DEFAULT_FINDER} as the finder.
     *
     * @param finder The new finder to set.
     */
    public void setFinder(@NotNull final Finder finder) {
        FINDER = Objects.requireNonNull(finder, "Why are you trying to set a null finder?");
    }

    /**
     * Determines whether a particular interaction can occur.
     *
     * @param ingredient The ingredient to get the alloy-slug for.
     * @return Returns an alloy-slug or null.
     */
    public @NotNull String find(final @NotNull ItemStack ingredient) {
        if (FINDER == DEFAULT_FINDER) {
            return FINDER.find(ingredient);
        }
        final String found = FINDER.find(ingredient);
        return StringUtils.isBlank(found) ? DEFAULT_FINDER.find(ingredient) : found.toUpperCase();
    }

}
