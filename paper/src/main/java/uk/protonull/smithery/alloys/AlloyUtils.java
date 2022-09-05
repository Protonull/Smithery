package uk.protonull.smithery.alloys;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.bukkit.Material;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.protonull.smithery.config.Config;
import uk.protonull.smithery.forge.ForgeRecipe;
import uk.protonull.smithery.utilities.AmountMap;
import uk.protonull.smithery.utilities.ItemBuilder;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreArrayUtils;

@UtilityClass
public final class AlloyUtils {

    /**
     * @param item The item to check.
     * @return Returns whether the given item is an alloy.
     */
    public boolean isItemAnAlloy(final ItemStack item) {
        return item != null && getAlloyFromItem(item.getItemMeta()) != null;
    }

    /**
     * @param meta The item meta to get the alloy from.
     * @return Returns the item's alloy. You can infer from a null that the item was not an alloy.
     */
    public @Nullable Alloy getAlloyFromItem(final @NotNull ItemMeta meta) {
        return meta.getPersistentDataContainer().get(Alloy.PDC_KEY, Alloy.TYPE);
    }

    /**
     * Assigns a given item an alloy. This method will NOT change any display information.
     *
     * @param meta The item meta to apply the alloy to.
     * @param alloy The alloy to apply.
     */
    public void setAlloyToItem(final @NotNull ItemMeta meta,
                               final @NotNull Alloy alloy) {
        meta.getPersistentDataContainer().set(Alloy.PDC_KEY, Alloy.TYPE, alloy);
    }

    /**
     * @return Returns a new alloy template item.
     */
    public @NotNull ItemStack newAlloyItem(final @NotNull Alloy alloy) {
        final var item = new ItemStack(Material.STICK);
        item.editMeta((final ItemMeta meta) -> {
            setAlloyToItem(meta, alloy);
            MetaUtils.addGlow(meta);
        });
        return item;
    }

    public @NotNull ItemStack createAlloyFromRecipe(final @NotNull ForgeRecipe recipe,
                                                    final @NotNull AlloyQuality quality) {
        final ItemStack item = newAlloyItem(new Alloy(recipe.slug(), quality));
        item.editMeta((final ItemMeta meta) -> {
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .content(recipe.name())
                    .build());
            meta.lore(List.of(
                    Component.text()
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.WHITE)
                            .content("Quality: ")
                            .append(Component.text()
                                    .color(switch (quality) {
                                        case BEST -> NamedTextColor.GOLD;
                                        case GOOD -> NamedTextColor.GREEN;
                                        case OKAY -> NamedTextColor.YELLOW;
                                        case POOR -> NamedTextColor.RED;
                                    })
                                    .content(quality.name()))
                            .build()
            ));
        });
        item.setAmount(recipe.yield());
        return item;
    }

    public @NotNull Alloy createAlloyFromIngredients(final @NotNull AmountMap<String> ingredients,
                                                     final long timeSpentSmelting) {
        if (MapUtils.isEmpty(ingredients)) {
            return Alloy.SLAG;
        }
        final ForgeRecipe matchedRecipe = IterableUtils.find(Config.RECIPES.get(), (final ForgeRecipe recipe) ->
                CollectionUtils.isEqualCollection(ingredients.keySet(), recipe.ingredients().keySet()));
        if (matchedRecipe == null) {
            return Alloy.SLAG;
        }
        if (ThreadLocalRandom.current().nextDouble(1d, 100d) <= matchedRecipe.failChance()) {
            return Alloy.SLAG;
        }
        final boolean amountsMatch = IterableUtils.matchesAll(ingredients.keySet(),
                (final String key) -> ingredients.getInt(key) == matchedRecipe.ingredients().getInt(key));
        AlloyQuality quality = AlloyQuality.BEST;
        if (Config.HINTS_ENABLED.get()) {
            // Ingredient Amounts
            if (!amountsMatch) {
                quality = quality.downgrade();
            }
            // Smelting time
            if (timeSpentSmelting < (matchedRecipe.cookTime() * 0.8d) || timeSpentSmelting > (matchedRecipe.cookTime() * 1.5d)) {
                return Alloy.SLAG;
            }
            if (timeSpentSmelting < (matchedRecipe.cookTime() * 0.9d) || timeSpentSmelting > (matchedRecipe.cookTime() * 1.3d)) {
                quality = quality.downgrade();
            }
        }
        else if (!amountsMatch
                || timeSpentSmelting < (matchedRecipe.cookTime() * 0.9d)
                || timeSpentSmelting > (matchedRecipe.cookTime() * 1.3d)) {
            return Alloy.SLAG;
        }
        return new Alloy(matchedRecipe.slug(), quality);
    }

    // ------------------------------------------------------------
    // Recipes
    // ------------------------------------------------------------

    /**
     * Determines whether a crafting matrix (the 2x2 or 3x3 area in the crafting inventory) contains Alloys.
     *
     * @param inventory The crafting inventory to check.
     * @return Returns true if the matrix contains Alloys.
     */
    public boolean doesMatrixContainAlloys(final @NotNull CraftingInventory inventory) {
        return MoreArrayUtils.anyMatch(inventory.getMatrix(), AlloyUtils::isItemAnAlloy);
    }

    /**
     * Determines whether a recipe defines Alloy ingredients.
     *
     * @param recipe The recipe to check.
     * @return Returns true if the recipe defines Alloy ingredients.
     */
    public boolean doesRecipeDefineAlloyIngredients(final @NotNull Recipe recipe) {
        if (recipe instanceof final ShapedRecipe shapedRecipe) {
            return IterableUtils.matchesAny(shapedRecipe.getIngredientMap().values(), AlloyUtils::isItemAnAlloy);
        }
        if (recipe instanceof final ShapelessRecipe shapelessRecipe) {
            return IterableUtils.matchesAny(shapelessRecipe.getIngredientList(), AlloyUtils::isItemAnAlloy);
        }
        return false;
    }

    // ------------------------------------------------------------
    // Molten Alloy
    // ------------------------------------------------------------

    public static final Material MOLTEN_ALLOY_MATERIAL = Material.LAVA_BUCKET;

    /**
     * @return Returns a new Alloy template item.
     */
    public @NotNull ItemStack newMoltenAlloy(final @NotNull Alloy alloy) {
        return ItemBuilder.builder(MOLTEN_ALLOY_MATERIAL)
                .meta((final ItemMeta meta) -> {
                    setAlloyToItem(meta, alloy);
                    meta.displayName(Component.text()
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.RED)
                            .content("Molten Metal")
                            .build());
                    meta.lore(List.of(Component.text("Take this to a water cauldron to cool into an alloy!")));
                    MetaUtils.addGlow(meta);
                })
                .build();
    }

    /**
     * Attempts to retrieve the Alloy value from a given item. If null is returns, it can be inferred that the item is
     * not a Molten Alloy.
     *
     * @param item The item to retrieve the Alloy from.
     * @return Returns an Alloy value, or null.
     */
    public @Nullable Alloy getMoltenAlloy(final ItemStack item) {
        return item == null || item.getType() != MOLTEN_ALLOY_MATERIAL ? null : getAlloyFromItem(item.getItemMeta());
    }

    // ------------------------------------------------------------
    // Slag
    // ------------------------------------------------------------

    /**
     * @return Returns a new slag item.
     */
    public @NotNull ItemStack newSlagItem() {
        final ItemStack item = newAlloyItem(Alloy.SLAG);
        item.editMeta((final ItemMeta meta) -> {
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(NamedTextColor.RED)
                    .content("Slag")
                    .build());
            meta.lore(List.of(
                    Component.text()
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                            .color(NamedTextColor.WHITE)
                            .content("Now look what you've done...")
                            .build()
            ));
        });
        return item;
    }

}