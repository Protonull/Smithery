package uk.protonull.smithery.forge;

import java.util.List;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.utilities.PersistentDataTypes;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;

@UtilityClass
public class ForgeUtils {

    public final Material FORGE_MATERIAL = Material.BLAST_FURNACE;

    /**
     * @return Returns a newly generated Forge (custom furnace).
     */
    @NotNull
    public ItemStack newForgeItem() {
        final var item = new ItemStack(FORGE_MATERIAL);
        item.editMeta((final ItemMeta meta) -> {
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(NamedTextColor.AQUA)
                    .content("Smithery")
                    .build());
            meta.lore(List.of(Component.text("Used to forge Alloys.")));
            meta.getPersistentDataContainer().set(Forge.FORGE_KEY, PersistentDataType.BYTE, (byte) 1);
        });
        return item;
    }

    /**
     * Determines whether a given item matches that which would be generated by {@link #newForgeItem()}.
     *
     * @param item The item to test.
     * @return Returns true if the given item is a Forge.
     */
    public boolean isForgeItem(final ItemStack item) {
        return item != null
                && item.getType() == FORGE_MATERIAL
                && item.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(Forge.FORGE_KEY, PersistentDataTypes.BOOLEAN, Boolean.FALSE);
    }

    // ------------------------------------------------------------
    // Furnace
    // ------------------------------------------------------------

    /**
     * Wraps a given furnace's {@link org.bukkit.persistence.PersistentDataContainer} within an NBT compound for easier
     * data manipulation. Since it's a wrapped PDC, you can manipulate it without needing to re-set the NBT back onto
     * the furnace, though you may need to call {@link org.bukkit.block.Furnace#update()}.
     *
     * @param furnace The furnace to get the NBT of.
     * @return Returns an NBT component for that furnace.
     */
    @NotNull
    public NBTCompound getFurnaceNBT(@NotNull final org.bukkit.block.Furnace furnace) {
        return new NBTCompound(furnace.getPersistentDataContainer());
    }

    /**
     * Retrieves a furnace's block data pre-cast to the relevant type. You <i>WILL</i> need to re-set the data with
     * {@link org.bukkit.block.Furnace#setBlockData(BlockData)} after you've finished.
     *
     * @param furnace The furnace to get the data of.
     * @return Returns the block data for that furnace.
     */
    @NotNull
    public org.bukkit.block.data.type.Furnace getFurnaceData(@NotNull final org.bukkit.block.Furnace furnace) {
        return (org.bukkit.block.data.type.Furnace) furnace.getBlockData();
    }

    // ------------------------------------------------------------
    // GUI
    // ------------------------------------------------------------

    /**
     * @return Returns a newly generated "Insert" button for the Forge GUI.
     */
    @NotNull
    public ItemStack newInsertButton() {
        final var item = new ItemStack(Material.LIME_DYE);
        item.editMeta((final ItemMeta meta) -> {
            meta.displayName(Component.text()
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(NamedTextColor.GREEN)
                    .content("Add ingredients to Forge")
                    .build());
            meta.lore(List.of(Component.text("Click to add the items in the matrix to the Forge")));
        });
        return item;
    }

}