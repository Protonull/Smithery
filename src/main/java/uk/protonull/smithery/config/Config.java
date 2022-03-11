package uk.protonull.smithery.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.protonull.smithery.config.versions.SmelteryVersion4;
import uk.protonull.smithery.config.versions.SmitheryVersion1;
import uk.protonull.smithery.forge.ForgeRecipe;

@UtilityClass
public class Config {

    private final List<ConfigElement<?>> ELEMENTS = new ArrayList<>();

    private <T> ConfigElement<T> registerElement(final ConfigElement<T> element) {
        ELEMENTS.add(Objects.requireNonNull(element));
        return element;
    }

    /**
    * Forces all known config elements to parse themselves. Use this when Smithery is being enabled.
    */
    public void forceParseAll() {
        ELEMENTS.forEach(ConfigElement::get);
    }

    /**
    * Forces all known config elements to reset. Use this when Smithery is being disabled.
    */
    public void reset() {
        ELEMENTS.forEach(ConfigElement::reset);
        ELEMENTS.clear();
    }

    // ------------------------------------------------------------
    // Config Values
    // ------------------------------------------------------------

    public final ConfigElement<List<AbstractConfigParser>> SUPPORTED_VERSIONS = registerElement(new ConfigElement<>() {
        @NotNull
        @Override
        protected List<AbstractConfigParser> parseElement() {
            return List.of(
                    new SmitheryVersion1(),
                    new SmelteryVersion4()
            );
        }
    });

    public final ConfigElement<AbstractConfigParser> PARSER = registerElement(new ConfigElement<>() {
        @NotNull
        @Override
        protected AbstractConfigParser parseElement() {
            return Objects.requireNonNull(
                    IterableUtils.find(SUPPORTED_VERSIONS.get(), AbstractConfigParser::matchesVersion),
                    "Invalid config version!");
        }
    });

    public final ConfigElement<Boolean> HINTS_ENABLED = registerElement(new ConfigElement<>() {
        @NotNull
        @Override
        protected Boolean parseElement() {
            return PARSER.get().allowLenientQualities();
        }
    });

    public final ConfigElement<List<ForgeRecipe>> RECIPES = registerElement(new ConfigElement<>() {
        @NotNull
        @Override
        protected List<ForgeRecipe> parseElement() {
            return List.copyOf(PARSER.get().parseRecipes());
        }
    });

    /**
     * Attempts to match a recipe against the given slug.
     *
     * @param slug The recipe slug to find.
     * @return Returns a matched recipe, or null.
     */
    @Nullable
    public ForgeRecipe matchRecipe(final String slug) {
        return IterableUtils.find(RECIPES.get(), (final ForgeRecipe recipe) -> recipe.slug().equalsIgnoreCase(slug));
    }

}
