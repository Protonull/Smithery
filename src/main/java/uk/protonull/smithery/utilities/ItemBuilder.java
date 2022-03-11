package uk.protonull.smithery.utilities;

import java.util.Objects;
import java.util.function.Consumer;
import lombok.Synchronized;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class ItemBuilder {

    private static final int MINIMUM_AMOUNT = 1;

    private Material material;
    private ItemMeta meta;
    private int amount;

    private ItemBuilder() {
        this.amount = MINIMUM_AMOUNT;
    }

    /**
     * @param material The material to set for this item.
     * @return Returns this builder.
     *
     * @throws IllegalArgumentException Throws an IAE if the given material fails am
     *                                  {@link ItemUtils#isValidItemMaterial(Material)} test.
     */
    @NotNull
    @Synchronized
    public ItemBuilder material(@NotNull final Material material) {
        if (!ItemUtils.isValidItemMaterial(material)) {
            throw new IllegalArgumentException("That is not a valid item material!");
        }
        this.material = material;
        return this;
    }

    /**
     * @param <T> The type to cast the item meta to.
     * @param handler The item meta handler.
     * @return Returns this builder.
     *
     * @throws NullPointerException Throws an NPE if a new item meta needs to be created, but the new meta is null.
     * @throws ClassCastException Throws an CCE if the item meta cannot be cast to the inferred type.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    @Synchronized
    public <T> ItemBuilder meta(@NotNull final Consumer<T> handler) {
        if (this.meta == null || !Bukkit.getItemFactory().isApplicable(this.meta, this.material)) {
            this.meta = Objects.requireNonNull(
                    Bukkit.getItemFactory().getItemMeta(this.material),
                    "Tried to create an item meta for [" + this.material + "] but it returned null!");
        }
        handler.accept((T) this.meta);
        return this;
    }

    /**
     * @param amount The amount to set for this item.
     * @return Returns this builder.
     *
     * @throws IllegalArgumentException Throws an IAE if the given amount is less than or equal to zero.
     */
    @NotNull
    @Synchronized
    public ItemBuilder amount(final int amount) {
        if (amount < MINIMUM_AMOUNT) {
            throw new IllegalArgumentException("Item amount cannot be less than or equal to zero!");
        }
        this.amount = amount;
        return this;
    }

    /**
     * @return Returns a new ItemStack based on this builder.
     */
    @NotNull
    @Synchronized
    public ItemStack build() {
        final var item = new ItemStack(this.material, this.amount);
        if (this.meta != null) {
            item.setItemMeta(this.meta);
        }
        return item;
    }

    /**
     * Creates a new builder with the given material.
     *
     * @param material The material to set for the builder.
     * @return Returns a new builder.
     */
    @NotNull
    public static ItemBuilder builder(@NotNull final Material material) {
        return new ItemBuilder().material(material);
    }

}
