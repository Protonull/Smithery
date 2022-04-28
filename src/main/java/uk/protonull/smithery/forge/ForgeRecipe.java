package uk.protonull.smithery.forge;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.utilities.AmountMap;
import uk.protonull.smithery.utilities.Utilities;

public record ForgeRecipe(String slug,
                          String name,
                          long cookTime,
                          double failChance,
                          AmountMap<String> ingredients) {

    public ForgeRecipe(final @NotNull String slug,
                       final @NotNull String name,
                       final long cookTime,
                       final double failChance,
                       final @NotNull AmountMap<String> ingredients) {
        this.slug = Utilities.requireNonBlankString(slug, "Recipe slug cannot be null!").toUpperCase();
        this.name = Utilities.requireNonBlankString(name, "Recipe name cannot be null!");
        this.cookTime = cookTime;
        this.failChance = failChance;
        this.ingredients = new AmountMap.Unmodifiable<>(Objects.requireNonNull(ingredients, "Ingredients cannot be null!"));
    }

}
