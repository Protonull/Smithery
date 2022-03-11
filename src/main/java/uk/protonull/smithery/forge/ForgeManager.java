package uk.protonull.smithery.forge;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.Smithery;
import uk.protonull.smithery.utilities.Utilities;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.CivLogger;

@UtilityClass
public class ForgeManager {

    private final CivLogger LOGGER = CivLogger.getLogger(ForgeManager.class);

    public final Map<ForgeLocation, Forge> FORGES = new HashMap<>();
    final Map<Inventory, Forge> GUIS = new IdentityHashMap<>();

    /**
     * Attempts to remove the Forge at the given location.
     *
     * @param location The location of the Forge.
     */
    public Forge removeForge(@NotNull final ForgeLocation location) {
        final Forge forge = FORGES.remove(location);
        if (forge != null) {
            forge.closeInventory();
        }
        return forge;
    }

    /**
     * Loads all Forges from all worlds. Please only use this within {@link Smithery#onEnable()}.
     */
    public void loadAllForges() {
        FORGES.clear();
        for (final World world : Bukkit.getWorlds()) {
            for (final Chunk chunk : world.getLoadedChunks()) {
                loadForgesInChunk(chunk);
            }
        }
    }

    /**
     * Loads all Forges from a given chunk.
     *
     * @param chunk The chunk to load Forges from.
     */
    public void loadForgesInChunk(@NotNull final Chunk chunk) {
        for (final BlockState state : chunk.getTileEntities(false)) {
            if (state.getType() != ForgeUtils.FORGE_MATERIAL) {
                continue;
            }
            final var furnace = (org.bukkit.block.Furnace) state;
            final PersistentDataContainer forgePDC = furnace
                    .getPersistentDataContainer()
                    .get(Forge.FORGE_KEY, PersistentDataType.TAG_CONTAINER);
            if (forgePDC == null) {
                continue;
            }
            final var forge = new Forge(furnace);
            FORGES.compute(forge.getLocation(),
                    (final ForgeLocation location, final Forge currentForge) -> {
                        if (currentForge != null) {
                            LOGGER.warning("Forge at [" + location + "] was just replaced on chunk load o.o'");
                        }
                        return forge;
                    });
            forge.fromNBT(new NBTCompound(forgePDC));
            //LOGGER.info("Forge at [" + forge.getLocation() + "] has been loaded.");
        }
    }

    /**
     * Stores, then clears, all loaded Forges. Please only use this within {@link Smithery#onDisable()}.
     */
    public void saveAllForges() {
        FORGES.values().removeIf((final Forge forge) -> {
            forge.saveForge();
            forge.closeInventory();
            return true;
        });
    }

    /**
     * Stores, and removes, all Forges within a given chunk.
     *
     * @param chunk The chunk to use as a coordinate clamp to match Forges against.
     */
    public void saveForgesInChunk(@NotNull final Chunk chunk) {
        Utilities.removeIf(FORGES, (final ForgeLocation location, final Forge forge) -> {
            if (location.isWithinChunk(chunk)) {
                forge.saveForge();
                forge.closeInventory();
                //LOGGER.info("Forge at [" + location + "] has been unloaded.");
                return true;
            }
            return false;
        });
    }

}
