# Smeltery

Smithery supports, *to a certain extent*,
[Smeltery's 1.4 configuration](https://github.com/Ajaxan/Smeltery/blob/master/config-1.4.yml). Smithery does not
support hint words, phrases, special materials, base-items, or attributes. Below shows the extent of support:

```yaml
SmelteryConfigVersion: 1.4

# "Hints" are when lower-quality versions of recipe outputs are allowed by making recipes more lenient. If "hints" are
# disabled then only BEST quality alloys can be produced.
EnableHints: true

Recipes:
  0:
    # This name will appear on the resulting Alloy. It does not support formatting.
    Name: Steelgem
    # Tag is this recipe's unique-identifier. Do NOT give any other recipe the same Tag. Do NOT give recipes Tags that
    # match Bukkit-Materials. Tags should be capitalised.
    Tag: STEELGEM
    # SmeltTime is the amount of time, in minutes, this recipe will take to complete.
    SmeltTime: 3
    # FailChance is the percentage (0-100) chance that the recipe will fail outright.
    FailChance: 0
    # These are the ingredients for this recipe. Please be aware that you cannot have two or more recipes with the same
    # ingredients, even if their amounts are different.
    #
    # Ingredients can refer to Bukkit-Materials and Alloys.
    # Ingredients are defined as follows: "INGREDIENT/AMOUNT"
    #
    # If the "INGREDIENT" is a Bukkit-Material, write it as: "STONE_BRICKS/1"
    # See Materials: https://papermc.io/javadocs/paper/1.16/org/bukkit/Material.html
    #
    # If the "INGREDIENT" is an Alloy, write its Tag as such: "STEEL:GOOD/1"
    # There are four recipe-qualities: BEST, GOOD, OKAY, POOR
    # You can remove the quality, like so: "STEEL/1", if the required quality is BEST
    Ingredients:
      - IRON_INGOT/3
      - STEEL/1
  1:
    Name: Steel
    Tag: STEEL
    SmeltTime: 10
    FailChance: 0
    Ingredients:
      - IRON_INGOT/4
      - COAL/2
  2:
    Name: Heavy Steel
    Tag: HSTEEL
    SmeltTime: 1
    FailChance: 0
    Ingredients:
      - STEEL/1
      - COAL/4
```

This is not to suggest that a full Smeltery config would break Smithery, rather that the additional configuration data
would be ignored.