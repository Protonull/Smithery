package uk.protonull.smithery.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.NBTHelper;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

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
     * Updates the item used in the given interact event to the given item.
     *
     * @param event The interact event.
     * @param item  The item to set.
     */
    public void setInteractItem(final @NotNull PlayerInteractEvent event,
                                final ItemStack item) {
        event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), item);
    }

    public @NotNull String requireNonBlankString(final String string,
                                                 final @NotNull String message) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(message);
        }
        return string;
    }

    /**
     * Attempts to add an item to an inventory. If this fails, it will drop the item at the inventory's location,
     * which it's assumed to have.
     *
     * @param inventory The inventory to add the item to.
     * @param item The item to add.
     */
    public void giveOrDropItem(final @NotNull Inventory inventory,
                               final @NotNull ItemStack item) {
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
    public void dropItem(final @NotNull Location location,
                         final @NotNull ItemStack item) {
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
    public <K, V> void removeIf(final @NotNull Map<K, V> map,
                                final @NotNull BiPredicate<K, V> predicate) {
        map.entrySet().removeIf((entry) -> predicate.test(entry.getKey(), entry.getValue()));
    }

    /**
     * Given that {@link PrepareItemCraftEvent} cannot be cancelled in the traditional sense, we need to instead
     * nullify its result.
     *
     * @param event The event to cancel.
     */
    public void cancelPrepareItemCraftEvent(final @NotNull PrepareItemCraftEvent event) {
        event.getInventory().setResult(null);
    }

    @Contract("!null -> !null")
    public @Nullable CompoundTag fromPDC(final PersistentDataContainer pdc) {
        final var craftPDC = (CraftPersistentDataContainer) pdc;
        return pdc == null ? null : new CompoundTag(craftPDC.getRaw()) {};
    }

    public @NotNull CompoundTag getOrCreateCompound(final @NotNull CompoundTag host,
                                                    final @NotNull String key) {
        return Objects.requireNonNullElseGet(host.getCompound(key), () -> {
            final var nbt = new CompoundTag();
            host.put(key, nbt);
            return nbt;
        });
    }

    /**
     * Encodes an inventory into an NBT compound. Only non-empty slots will be encoded.
     *
     * @param inventory The inventory to encode.
     * @return Returns a new NBT compound.
     */
    public @NotNull CompoundTag inventoryToNBT(final @NotNull Inventory inventory) {
        final var nbt = new CompoundTag();
        final ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];
            if (!isEmptyItem(item)) {
                nbt.put(Integer.toString(i), NBTHelper.itemStackToNBT(item).getRAW());
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
    public void inventoryFromNBT(final @NotNull Inventory inventory,
                                 final @NotNull CompoundTag nbt) {
        final ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, null);
        for (final String key : nbt.getAllKeys()) {
            final ItemStack parsed = NBTHelper.itemStackFromNBT(new NBTCompound(nbt.getCompound(key)));
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

}