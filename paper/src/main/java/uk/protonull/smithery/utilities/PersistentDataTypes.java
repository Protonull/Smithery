package uk.protonull.smithery.utilities;

import lombok.experimental.UtilityClass;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class PersistentDataTypes {

    /**
     * Boolean data type... because <i>believe it or not</i> but PDC doesn't already have this ಠ_ಠ
     */
    public final PersistentDataType<Byte, Boolean> BOOLEAN = new PersistentDataType<>() {
        @Override
        public @NotNull Class<Byte> getPrimitiveType() {
            return Byte.class;
        }
        @Override
        public @NotNull Class<Boolean> getComplexType() {
            return Boolean.class;
        }
        @Override
        public @NotNull Byte toPrimitive(final @NotNull Boolean bool,
                                         final @NotNull PersistentDataAdapterContext adapter) {
            return (byte) (bool ? 1 : 0);
        }
        @Override
        public @NotNull Boolean fromPrimitive(final @NotNull Byte raw,
                                              final @NotNull PersistentDataAdapterContext adapter) {
            return raw != (byte) 0;
        }
    };

}
