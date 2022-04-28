package uk.protonull.smithery.alloys;

import java.util.Objects;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import uk.protonull.smithery.utilities.Utilities;

public record Alloy(@NotNull String recipe,
                    @NotNull AlloyQuality quality) {

    /**
     * Creates a new alloy.
     *
     * @param recipe The recipe slug that created this alloy.
     * @param quality The quality of this alloy.
     */
    public Alloy(final @NotNull String recipe,
                 final @NotNull AlloyQuality quality) {
        this.recipe = Utilities.requireNonBlankString(recipe, "Alloy recipe cannot be blank!").toUpperCase();
        this.quality = Objects.requireNonNull(quality, "Alloy quality cannot be null!");
    }

    /**
     * Determines whether this alloy is slag. Use this method over any kind of equals method since quality is
     * irrelevant to slag.
     *
     * @return Returns true if this alloy is slag.
     */
    public boolean isSlag() {
        return StringUtils.equals(recipe(), SLAG.recipe());
    }

    /**
     * @return Encodes this Alloy into a string.
     */
    public @NotNull String generateKey() {
        return quality().isBest() ? recipe() : recipe() + ":" + quality();
    }

    /**
     * Decodes an Alloy from a string.
     *
     * @param raw The string to decode.
     * @return Returns a new Alloy based on the given string.
     */
    public static @NotNull Alloy fromKey(final @NotNull String raw) {
        final String[] parts = StringUtils.split(raw, ":");
        return switch (parts.length) {
            case 1 -> new Alloy(parts[0], AlloyQuality.BEST);
            case 2 -> new Alloy(parts[0], EnumUtils.getEnum(AlloyQuality.class, parts[1]));
            default -> throw new IllegalArgumentException("Alloy key [" + raw + "] is invalid!");
        };
    }

    /**
     *
     */
    public static Alloy SLAG = new Alloy("NONE", AlloyQuality.BEST);

    public static final NamespacedKey PDC_KEY = Utilities.key("smithery", "alloy");
    public static PersistentDataType<PersistentDataContainer, Alloy> TYPE = new PersistentDataType<>() {
        private final NamespacedKey typeKey = Utilities.key(".", "type");
        private final NamespacedKey qualityKey = Utilities.key(".", "quality");
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }
        @Override
        public @NotNull Class<Alloy> getComplexType() {
            return Alloy.class;
        }
        @Override
        public @NotNull PersistentDataContainer toPrimitive(final @NotNull Alloy alloy,
                                                            final @NotNull PersistentDataAdapterContext context) {
            final PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(typeKey, PersistentDataType.STRING, alloy.recipe());
            if (!alloy.quality().isBest()) {
                pdc.set(qualityKey, PersistentDataType.STRING, alloy.quality().name());
            }
            return pdc;
        }
        @Override
        public @NotNull Alloy fromPrimitive(final @NotNull PersistentDataContainer pdc,
                                            final @NotNull PersistentDataAdapterContext context) {
            return new Alloy(
                    pdc.get(typeKey, PersistentDataType.STRING), // Ignore highlighter
                    Objects.requireNonNullElse(
                            EnumUtils.getEnum(
                                    AlloyQuality.class,
                                    pdc.get(qualityKey, PersistentDataType.STRING)),
                            AlloyQuality.BEST
                    ));
        }
    };

}
