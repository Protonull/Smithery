# Smithery

Smithery is a custom plugin, inspired by [Smeltery](https://www.spigotmc.org/resources/smeltery-free.35784/), that
facilitates the forging of Alloys. This plugin requires [at least] Java 16 and CivModCore to function.

Be aware that Smithery does not dictate how Forges are made nor how Alloys are used. Server admins should use
`/smithery give` to give themselves a Forge so that they can, for example, create a CraftEnhance recipe for it.
Likewise, use `/smithery list` to list all parsed recipes and be able to produce each Alloy at each quality at each
stage.

## Config

[See here](./src/main/resources/config.yml) for how to properly configure your Smithery plugin. Smithery also supports,
*to a certain extent*, Smeltery's 1.4 configuration. [Read more about that here](./SMELTERY.md).

## Advice

### Custom action handling
Say you have a block protection plugin and want to prevent players from extracting
alloys from protected Forges they don't have permission to access, take a gander at
`uk.protonull.smithery.utilities.ActionHandler` and create a fully custom, conditional action handler. By default, the
handler allows or denies based on permissions.

### Custom item handling
Say you have custom items that you wish to use as ingredients in Smithery, take a gander
at `uk.protonull.smithery.utilities.IngredientMatcher` and create a custom finder that will return a deterministic ID if
the given item matches any of your custom items. That ID can be used within the ingredients list of configured recipes.

### Alloy handling
Alloy data is stored within the item's PersistentDataContainer, example below:
```yaml
template:
  ==: org.bukkit.inventory.ItemStack
  v: 2586
  type: STICK
  meta:
    ==: ItemMeta
    meta-type: UNSPECIFIC
    display-name: '{"italic":false,"text":"Heavy Steel"}'
    lore:
      - '{"italic":false,"color":"white","extra":[{"color":"green","text":"GOOD"}],"text":"Quality:"}'
    enchants:
      DURABILITY: 1
    ItemFlags:
      - HIDE_ENCHANTS
    PublicBukkitValues:
      smithery:alloy:
        .:type: HSTEEL
        .:quality: GOOD
```
Be aware that the "type" and "quality" keys are 'ditto keys' to save space.