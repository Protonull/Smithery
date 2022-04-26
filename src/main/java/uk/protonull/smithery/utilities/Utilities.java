package uk.protonull.smithery.utilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTHelper;
import vg.civcraft.mc.civmodcore.util.CivLogger;

@UtilityClass
public class Utilities {

    private static final CivLogger LOGGER = CivLogger.getLogger(Utilities.class);

    /**
     * Checks whether the given item can be interpreted as an empty slot.
     *
     * @param item The item to check.
     * @return Returns true if the item can be interpreted as an empty slot.
     */
    public boolean isEmptyItem(final ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() < 1;
    }

    /**
     * Determines whether a given item is valid.
     *
     * @param item The item to check.
     * @return Returns true if the item is valid.
     */
    public boolean isValidItem(final ItemStack item) {
        return !isEmptyItem(item)
                && item.getType().isItem()
                && item.getAmount() <= item.getType().getMaxStackSize();
    }

    /**
     * Extends the behaviour of {@link ItemStack#subtract()} with the difference that an item subtracted below a valid
     * amount will return null.
     *
     * @param item The item to subtract.
     * @return Returns the subtracted item, or null if the item would no longer exist amount-wise.
     */
    public ItemStack subtractItem(final ItemStack item) {
        return subtractItem(item, 1);
    }

    /**
     * Extends the behaviour of {@link ItemStack#subtract()} with the difference that an item subtracted below a valid
     * amount will return null.
     *
     * @param item   The item to subtract.
     * @param amount The amount to subtract the item by.
     * @return Returns the subtracted item, or null if the item would no longer exist amount-wise.
     */
    public ItemStack subtractItem(final ItemStack item,
                                  final int amount) {
        return item == null ? null : item.subtract(amount).getAmount() < 1 ? null : item;
    }

    /**
     * Updates the item used in the given interact event to the given item.
     *
     * @param event The interact event.
     * @param item  The item to set.
     */
    public void setInteractItem(final PlayerInteractEvent event,
                                final ItemStack item) {
        event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), item);
    }

    @NotNull
    public String requireNonBlankString(final String string,
                                        @NotNull final String message) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(message);
        }
        return string;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public NamespacedKey key(@NotNull final String namespace,
                             @NotNull final String key) {
        return new NamespacedKey(namespace, key);
    }

    /**
     * Attempts to add an item to an inventory. If this fails, it will drop the item at the inventory's location,
     * which it's assumed to have.
     *
     * @param inventory The inventory to add the item to.
     * @param item The item to add.
     */
    public void giveOrDropItem(@NotNull final Inventory inventory,
                               @NotNull final ItemStack item) {
        final Collection<ItemStack> failedToAdd = inventory.addItem(item).values();
        if (!failedToAdd.isEmpty()) {
            final Location location = Objects.requireNonNull(inventory.getLocation());
            for (final ItemStack drop : failedToAdd) {
                dropItem(location, drop);
            }
        }
    }

    /**
     * Drops an item at a specific location with a slight upward vector.
     *
     * @param location The location the drop the item at.
     * @param item The item to drop.
     */
    public void dropItem(@NotNull final Location location,
                         @NotNull final ItemStack item) {
        location.getWorld().dropItem(location, item).setVelocity(new Vector(0d, 0.5d, 0d));
    }

    /**
     * This is a much, <i>much</i> better version of {@code map.entrySet().removeIf(...)} since you can access the key
     * and value via parameters rather a map entry.
     *
     * @param <K> The map's key type.
     * @param <V> The map's value type.
     * @param map The map to remove entries from.
     * @param predicate The method to test entries with. It should return true to remove that entry.
     */
    public <K, V> void removeIf(@NotNull final Map<K, V> map,
                                @NotNull final BiPredicate<K, V> predicate) {
        map.entrySet().removeIf((entry) -> predicate.test(entry.getKey(), entry.getValue()));
    }

    /**
     * Given that {@link PrepareItemCraftEvent} cannot be cancelled in the traditional sense, we need to instead
     * nullify its result.
     *
     * @param event The event to cancel.
     */
    public void cancelPrepareItemCraftEvent(@NotNull final PrepareItemCraftEvent event) {
        event.getInventory().setResult(null);
    }

    @NotNull
    public NBTCompound getOrCreateCompound(@NotNull final NBTCompound host,
                                           @NotNull final String key) {
        return Objects.requireNonNullElseGet(host.getCompound(key), () -> {
            final var nbt = new NBTCompound();
            host.setCompound(key, nbt);
            return nbt;
        });
    }

    /**
     * Checks whether an inventory has other viewers.
     *
     * @param inventory The inventory to check.
     * @param viewer The viewer to exclude from the check.
     * @return Returns true if an inventory has other viewers.
     */
    public boolean hasOtherViewersOtherThan(@NotNull final Inventory inventory,
                                            @NotNull final HumanEntity viewer) {
        final List<HumanEntity> viewers = inventory.getViewers();
        if (viewers.size() > 1) {
            return true;
        }
        final HumanEntity lastViewer = viewers.get(0);
        // If the last viewer is the given viewer, then there aren't any others.
        // If the last viewer isn't the given viewer, then the given viewer isn't actually a viewer.
        return lastViewer != viewer;
    }

    /**
     * Encodes an inventory into an NBT compound. Only non-empty slots will be encoded.
     *
     * @param inventory The inventory to encode.
     * @return Returns a new NBT compound.
     */
    public NBTCompound inventoryToNBT(@NotNull final Inventory inventory) {
        final var nbt = new NBTCompound();
        final ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];
            if (!isEmptyItem(item)) {
                nbt.setCompound(Integer.toString(i), NBTHelper.itemStackToNBT(item));
            }
        }
        return nbt;
    }

    /**
     * Decodes an NBT-encoded inventory into the given inventory. The given inventory is used to constrain parsing. Be
     * aware that the given inventory will be cleared to reflect the NBT-encoded inventory.
     *
     * @param inventory The inventory to decode into.
     * @param nbt The inventory to decode from.
     */
    public void inventoryFromNBT(@NotNull final Inventory inventory,
                                 @NotNull final NBTCompound nbt) {
        inventory.clear();
        final ItemStack[] contents = inventory.getContents();
        for (final String key : nbt.getKeys()) {
            final NBTCompound compound = nbt.getCompound(key);
            final ItemStack parsed = NBTHelper.itemStackFromNBT(compound);
            if (isEmptyItem(parsed)) {
                // Just ignore empty items
                continue;
            }
            final int index;
            try {
                index = Integer.parseInt(key);
            }
            catch (final NumberFormatException thrown) {
                LOGGER.log(Level.WARNING,
                        "Inventory slot [" + key + "] not a valid number! Item[" + parsed + "] will be ignored.",
                        new IllegalArgumentException());
                continue;
            }
            if (index < 0 || index >= contents.length) {
                LOGGER.log(Level.WARNING,
                        "Inventory slot [" + index + "] is out of bounds of array[" + contents.length + "]! Item[" + parsed + "] will be ignored.",
                        new IllegalArgumentException());
                continue;
            }
            contents[index] = parsed;
        }
        inventory.setContents(contents);
    }

    @SuppressWarnings("unchecked")
    public <T> void editBlockData(@NotNull final Block block,
                                  @NotNull final Consumer<@NotNull T> editor) {
        final T data = (T) block.getBlockData();
        editor.accept(data);
        block.setBlockData((BlockData) data);
    }

}