package uk.protonull.smithery.utilities;

import javax.annotation.Nonnull;
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
        @NotNull
        @Override
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }
        @NotNull
        @Override
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }
        @Nonnull
        @Override
        public Byte toPrimitive(@Nonnull final Boolean bool,
                                @Nonnull final PersistentDataAdapterContext adapter) {
            return (byte) (bool ? 1 : 0);
        }
        @Nonnull
        @Override
        public Boolean fromPrimitive(@Nonnull final Byte raw,
                                     @Nonnull final PersistentDataAdapterContext adapter) {
            return raw != (byte) 0;
        }
    };

}
