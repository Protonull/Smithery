package uk.protonull.smithery.alloys;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public record Alloy(@NotNull String recipe,
                    @NotNull AlloyQuality quality) {

    /**
     * Creates a new alloy.
     *
     * @param recipe The recipe slug that created this alloy.
     * @param quality The quality of this alloy.
     */
    public Alloy {
        if (StringUtils.isBlank(recipe)) {
            throw new IllegalArgumentException("Alloy recipe cannot be blank!");
        }
        recipe = recipe.toUpperCase();
        if (quality == null) { // Ignore highlighter
            throw new IllegalArgumentException("Alloy quality cannot be null!");
        }
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
            case 2 -> new Alloy(parts[0], AlloyQuality.valueOf(parts[1]));
            default -> throw new IllegalArgumentException("Alloy key [" + raw + "] is invalid!");
        };
    }

    public static final Alloy SLAG = new Alloy("NONE", AlloyQuality.BEST);
    public static final NamespacedKey PDC_KEY = new NamespacedKey("smithery", "alloy");
    public static PersistentDataType<PersistentDataContainer, Alloy> TYPE = new PersistentDataType<>() {
        private static final NamespacedKey TYPE_KEY = new NamespacedKey(".", "type");
        private static final NamespacedKey QUALITY_KEY = new NamespacedKey(".", "quality");
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
            pdc.set(TYPE_KEY, PersistentDataType.STRING, alloy.recipe());
            if (!alloy.quality().isBest()) {
                pdc.set(QUALITY_KEY, PersistentDataType.STRING, alloy.quality().name());
            }
            return pdc;
        }
        @Override
        public @NotNull Alloy fromPrimitive(final @NotNull PersistentDataContainer pdc,
                                            final @NotNull PersistentDataAdapterContext context) {
            return new Alloy(
                    pdc.get(TYPE_KEY, PersistentDataType.STRING), // Ignore highlighter
                    EnumUtils.getEnum(
                            AlloyQuality.class,
                            pdc.get(QUALITY_KEY, PersistentDataType.STRING),
                            AlloyQuality.BEST));
        }
    };

}
