package uk.protonull.smithery.forge;

import org.apache.commons.lang3.StringUtils;
import uk.protonull.smithery.utilities.AmountMap;

public record ForgeRecipe(String slug,
                          String name,
                          int yield,
                          long cookTime,
                          double failChance,
                          AmountMap<String> ingredients) {

    public ForgeRecipe {
        if (StringUtils.isBlank(slug)) {
            throw new IllegalArgumentException("Recipe slug cannot be null!");
        }
        slug = slug.toUpperCase();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Recipe name cannot be null!");
        }
        if (ingredients == null) {
            throw new IllegalArgumentException("Ingredients cannot be null!");
        }
        ingredients = new AmountMap.Unmodifiable<>(ingredients);
    }

}
