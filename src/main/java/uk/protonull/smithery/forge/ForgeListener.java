package uk.protonull.smithery.forge;

import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import uk.protonull.smithery.alloys.Alloy;
import uk.protonull.smithery.alloys.AlloyFinder;
import uk.protonull.smithery.alloys.AlloyUtils;
import uk.protonull.smithery.config.Config;
import uk.protonull.smithery.utilities.ActionHandler;
import uk.protonull.smithery.utilities.AmountMap;
import uk.protonull.smithery.utilities.Utilities;
import vg.civcraft.mc.civmodcore.util.CivLogger;

public final class ForgeListener implements Listener {

    private final CivLogger logger = CivLogger.getLogger(getClass());

    @EventHandler
    public void loadChunkForges(final ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            ForgeManager.loadForgesInChunk(event.getChunk());
        }
    }

    @EventHandler
    public void unloadChunkForges(final ChunkUnloadEvent event) {
        ForgeManager.saveForgesInChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForgePlacement(final BlockPlaceEvent event) {
        if (ForgeUtils.isForgeItem(event.getItemInHand())
                && event.getBlockPlaced().getState(false) instanceof final Furnace furnace) {
            final var forge = new Forge(furnace);
            ForgeManager.FORGES.put(forge.getLocation(), forge);
            this.logger.info("New Forge placed at [" + forge.getLocation() + "]");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForgeBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (block.getType() == ForgeUtils.FORGE_MATERIAL) {
            final Forge forge = ForgeManager.removeForge(new ForgeLocation.Static(block));
            if (forge != null) {
                event.getPlayer().sendMessage(ChatColor.GRAY + "You've dismantled that forge.");
                this.logger.info("Forge at [" + forge.getLocation() + "] has been destroyed.");
                event.setDropItems(false);
                // Drop any items that happen to be inside the Furnace... for whatever reason
                final FurnaceInventory inventory = forge.getFurnace().getInventory();
                for (final ItemStack itemToDrop : inventory) {
                    if (!Utilities.isEmptyItem(itemToDrop)) {
                        Utilities.dropItem(block.getLocation(), itemToDrop);
                    }
                }
                // Drop Forge item
                Utilities.dropItem(block.getLocation(), ForgeUtils.newForgeItem());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onForgeInteraction(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != ForgeUtils.FORGE_MATERIAL) {
            return;
        }
        final Forge forge = ForgeManager.FORGES.get(new ForgeLocation.Static(clicked));
        if (forge == null) {
            return;
        }
        final Furnace furnace = forge.getFurnace();
        event.setCancelled(true);
        final Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }
        final ItemStack itemUsed = event.getItem();
        // Adding fuel to the Forge
        if (!Utilities.isEmptyItem(itemUsed)) {
            assert itemUsed != null;
            switch (itemUsed.getType()) {
                // Adding fuel to the Forge
                case LAVA_BUCKET -> {
                    if (AlloyUtils.getMoltenAlloy(itemUsed) != null) {
                        return;
                    }
                    if (ActionHandler.canHandle(forge, player, ActionHandler.Action.ADD_FUEL_TO_FORGE)) {
                        final org.bukkit.block.data.type.Furnace furnaceData = ForgeUtils.getFurnaceData(furnace);
                        if (!furnaceData.isLit()) {
                            furnaceData.setLit(true);
                            furnace.setBlockData(furnaceData);
                            furnace.update();
                            Utilities.setInteractItem(event, new ItemStack(Material.BUCKET));
                        }
                        return;
                    }
                    player.sendMessage(ChatColor.GRAY + "You weren't able to add fuel to that Forge.");
                    return;
                }
                // Extracting molten Alloy from the Forge
                case BUCKET -> {
                    if (ActionHandler.canHandle(forge, player, ActionHandler.Action.COLLECT_FORGE_RESULT)) {
                        final AmountMap<String> ingredients = forge.getIngredients();
                        if (ingredients.isEmpty()) {
                            player.sendMessage(ChatColor.GRAY + "Nothing to extract from that Forge.");
                            return;
                        }
                        final Alloy alloy = AlloyUtils.createAlloyFromIngredients(ingredients, forge.getSmeltTime());
                        ingredients.clear();
                        final org.bukkit.block.data.type.Furnace furnaceData = ForgeUtils.getFurnaceData(furnace);
                        furnaceData.setLit(false);
                        furnace.setBlockData(furnaceData);
                        furnace.update();
                        Utilities.setInteractItem(event, AlloyUtils.newMoltenAlloy(alloy));
                        furnace.getWorld().playEffect(furnace.getLocation(), Effect.BREWING_STAND_BREW, 0);
                        player.sendMessage(ChatColor.GRAY + "You scoop the molten metal out of the forge.");
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "You couldn't extract from that Forge.");
                    return;
                }
                // Purging the Forge
                case WATER_BUCKET -> {
                    if (ActionHandler.canHandle(forge, player, ActionHandler.Action.FLUSH_FORGE_CONTENTS)) {
                        final org.bukkit.block.data.type.Furnace furnaceData = ForgeUtils.getFurnaceData(furnace);
                        furnaceData.setLit(false);
                        furnace.setBlockData(furnaceData);
                        furnace.update();
                        forge.getIngredients().clear();
                        Utilities.setInteractItem(event, new ItemStack(Material.BUCKET));
                        furnace.getWorld().playEffect(furnace.getLocation(), Effect.BREWING_STAND_BREW, 0);
                        player.sendMessage(ChatColor.GRAY + "The Forge ingredients have been flushed out.");
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "You could not flush out that Forge.");
                    return;
                }
                // Measuring Forge smelting time
                case CLOCK -> {
                    final org.bukkit.block.data.type.Furnace furnaceData = ForgeUtils.getFurnaceData(furnace);
                    if (furnaceData.isLit()) {
                        if (ActionHandler.canHandle(forge, player, ActionHandler.Action.READ_FORGE_SMELT_TIME)) {
                            final long smeltTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(forge.getSmeltTime());
                            if (smeltTimeMinutes > 1) {
                                player.sendMessage(ChatColor.GRAY + "This Forge has been smelting for about " + smeltTimeMinutes + " minutes.");
                            }
                            else {
                                player.sendMessage(ChatColor.GRAY + "This Forge has just begun smelting.");
                            }
                            return;
                        }
                        player.sendMessage(ChatColor.GRAY + "You're unsure how long that Forge has been smelting.");
                        return;
                    }
                    player.sendMessage(ChatColor.GRAY + "You're pretty sure that Forge isn't smelting anything.");
                    return;
                }
            }
        }
        final org.bukkit.block.data.type.Furnace furnaceData = ForgeUtils.getFurnaceData(furnace);
        if (!furnaceData.isLit()) {
            player.sendMessage(ChatColor.GRAY + "This is a Forge. Right click with a Lava Bucket to begin smelting!");
            return;
        }
        final Inventory inventory = forge.getInventory();
        inventory.setItem(0, ForgeUtils.newInsertButton());
        player.openInventory(inventory);
        ForgeManager.GUIS.put(inventory, forge);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForgeGUIClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null
                || inventory.getType() != InventoryType.WORKBENCH
                || event.getSlot() != 0) {
            return;
        }
        final Forge forge = ForgeManager.GUIS.get(inventory);
        if (forge == null) {
            return;
        }
        event.setCancelled(true);
        inventory.setItem(0, null); // Remove result button temporarily
        final AmountMap<String> ingredients = forge.getIngredients();
        int amountsAdded = 0;
        for (final ItemStack ingredient : inventory) {
            if (!Utilities.isEmptyItem(ingredient)) {
                final String slug = AlloyFinder.find(ingredient);
                ingredients.addAmount(slug, ingredient.getAmount());
                amountsAdded += ingredient.getAmount();
            }
        }
        inventory.clear();
        inventory.setItem(0, ForgeUtils.newInsertButton());
        forge.setTimeOfLastIngredientInsert(System.currentTimeMillis());
        event.getWhoClicked().sendMessage(ChatColor.GRAY + "You added " + amountsAdded + " ingredients to the Forge.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCauldronInteraction(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block cauldron = event.getClickedBlock();
        if (cauldron == null || cauldron.getType() != Material.CAULDRON) {
            return;
        }
        final Alloy alloy = AlloyUtils.getMoltenAlloy(event.getItem());
        if (alloy == null) {
            return;
        }
        final Levelled cauldronData = (Levelled) cauldron.getBlockData();
        if (cauldronData.getLevel() != cauldronData.getMaximumLevel()) {
            event.setCancelled(true);
            return;
        }
        final Player player = event.getPlayer();
        if (!ActionHandler.canHandle(cauldron, player, ActionHandler.Action.COOL_METAL_IN_CAULDRON)) {
            player.sendMessage(ChatColor.GRAY + "You cannot cool that metal there!");
            return;
        }
        final ItemStack result;
        if (alloy.isSlag()) {
            player.sendMessage(ChatColor.GRAY + "The mixture cooled into slag.");
            result = AlloyUtils.newSlagItem();
        }
        else {
            final ForgeRecipe recipe = Config.matchRecipe(alloy.recipe());
            if (recipe == null) {
                player.sendMessage(ChatColor.GRAY + "The mixture cooled into slag.");
                this.logger.warning("Player[" + player.getName() + "] attempted to cool molten-alloy[" + alloy.recipe() + "] but it could not be matched.");
                result = AlloyUtils.newSlagItem();
            }
            else {
                switch (alloy.quality()) {
                    case BEST -> player.sendMessage(ChatColor.GRAY + "The metal hardens and cools.");
                    case GOOD -> player.sendMessage(ChatColor.GRAY + "The metal hardens and cools into something decent.");
                    case OKAY -> player.sendMessage(ChatColor.GRAY + "The result leave a lot to be desired.");
                    case POOR -> player.sendMessage(ChatColor.GRAY + "The result is close to worthless.");
                }
                result = AlloyUtils.createAlloyFromRecipe(recipe, alloy.quality());
            }
        }
        cauldronData.setLevel(0);
        cauldron.setBlockData(cauldronData);
        Utilities.giveOrDropItem(player.getInventory(), result);
        Utilities.setInteractItem(event, new ItemStack(Material.BUCKET));
    }

}
