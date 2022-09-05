package uk.protonull.smithery.forge;

import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.utilities.AmountMap;
import uk.protonull.smithery.utilities.Utilities;

public final class Forge implements InventoryHolder {

    private static final Component FORGE_TITLE = Component.text("Smithery");
    public static final NamespacedKey FORGE_KEY = Utilities.key("smithery", "forge");
    private static final String INGREDIENTS_KEY = "ingredients";
    private static final String TIME_KEY = "time";
    private static final String INVENTORY_KEY = "inventory";

    private final Furnace furnace;
    private final ForgeLocation location;
    private final AmountMap<String> ingredients;
    private long timeOfLastIngredientInsert;
    private final Inventory inventory;

    public Forge(final @NotNull Furnace furnace) {
        this.furnace = Objects.requireNonNull(furnace);
        this.location = new ForgeLocation() {
            @Override
            public @NotNull UUID getWorldUUID() {
                return furnace.getWorld().getUID();
            }
            @Override
            public int getX() {
                return furnace.getX();
            }
            @Override
            public int getY() {
                return furnace.getY();
            }
            @Override
            public int getZ() {
                return furnace.getZ();
            }
        };
        this.ingredients = new AmountMap.ArrayMap<>(0);
        this.timeOfLastIngredientInsert = 0L;
        this.inventory = Bukkit.createInventory(this, InventoryType.WORKBENCH, FORGE_TITLE);
    }

    /**
     * @return Returns this Forge's corresponding Furnace entity.
     */
    public @NotNull Furnace getFurnace() {
        return this.furnace;
    }

    /**
     * @return Returns this Forge's location. Be aware that this location's values reflect the Furnace entity's current
     *         location, not its original location. If the Furnace has been moved by any means in Minecraft, the Forge
     *         location will reflect that. Keep that in mind when using this as a map-key.
     */
    public @NotNull ForgeLocation getLocation() {
        return this.location;
    }

    /**
     * @return Returns this Forge's inventory.
     */
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    /**
     * @return Returns all the ingredients in this Forge.
     */
    public @NotNull AmountMap<String> getIngredients() {
        this.ingredients.removeEmpties();
        return this.ingredients;
    }

    /**
     * @return Returns the amount of time (in milliseconds) this Forge has been smelting.
     */
    public long getSmeltTime() {
        return System.currentTimeMillis() - getTimeOfLastIngredientInsert();
    }

    /**
     * @return Returns the timestamp (in milliseconds) of when the last ingredient was inserted.
     */
    public long getTimeOfLastIngredientInsert() {
        return this.timeOfLastIngredientInsert;
    }

    /**
     * @param timeOfLastIngredientInsert The time of the last ingredient insert to set.
     */
    public void setTimeOfLastIngredientInsert(final long timeOfLastIngredientInsert) {
        this.timeOfLastIngredientInsert = timeOfLastIngredientInsert;
    }

    /**
     * Convenience method to store this Forge's data onto its Furnace entity.
     */
    public void saveForge() {
        ForgeUtils.getFurnaceNBT(getFurnace()).put(Forge.FORGE_KEY.asString(), toNBT());
    }

    /**
     * Convenience method to close this Forge's inventory for all viewing players.
     */
    public void closeInventory() {
        ForgeManager.GUIS.remove(getInventory());
        for (final HumanEntity viewer : getInventory().getViewers()) {
            if (viewer instanceof final Player player) {
                player.closeInventory();
            }
        }
    }

    /**
     * Encodes this Forge as NBT.
     *
     * @return Returns a new NBT compound representing this Forge.
     */
    public @NotNull CompoundTag toNBT() {
        final var nbt = new CompoundTag();
        // Save ingredients
        final CompoundTag ingredientNBT = Utilities.getOrCreateCompound(nbt, INGREDIENTS_KEY);
        getIngredients().forEach(ingredientNBT::putInt);
        // Save time of last insert
        nbt.putLong(TIME_KEY, getTimeOfLastIngredientInsert());
        // Save inventory
        nbt.put(INVENTORY_KEY, Utilities.inventoryToNBT(getInventory()));
        return nbt;
    }

    /**
     * Updates this Forge to reflect the given NBT compound.
     *
     * @param nbt The NBT compound to decode.
     */
    public void fromNBT(final @NotNull CompoundTag nbt) {
        // Load ingredients
        final AmountMap<String> ingredients = getIngredients();
        ingredients.clear();
        final CompoundTag ingredientNBT = nbt.getCompound(INGREDIENTS_KEY);
        for (final String ingredient : ingredientNBT.getAllKeys()) {
            ingredients.put(ingredient, ingredientNBT.getInt(ingredient));
        }
        ingredients.removeEmpties();
        // Load time of last insert
        setTimeOfLastIngredientInsert(nbt.getLong(TIME_KEY));
        // Load inventory
        Utilities.inventoryFromNBT(getInventory(), nbt.getCompound(INVENTORY_KEY));
    }

}
