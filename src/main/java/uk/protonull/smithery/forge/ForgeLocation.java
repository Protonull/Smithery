package uk.protonull.smithery.forge;

import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public abstract class ForgeLocation {

    public abstract @NotNull UUID getWorldUUID();

    public abstract int getX();

    public abstract int getY();

    public abstract int getZ();

    /**
     * Determines whether this location is within the given chunk.
     *
     * @param chunk The relevant chunk.
     * @return Returns true if the location is within the chunk.
     */
    public final boolean isWithinChunk(final @NotNull Chunk chunk) {
        return Objects.equals(getWorldUUID(), chunk.getWorld().getUID())
                && (getX() >> 4) == chunk.getX()
                && (getZ() >> 4) == chunk.getZ();
    }

    @Override
    public final @NotNull String toString() {
        return getWorldUUID() + ":" + getX() + "," + getY() + "," + getZ();
    }

    @Override
    public final boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof final ForgeLocation other) {
            return Objects.equals(getWorldUUID(), other.getWorldUUID())
                    && getX() == other.getX()
                    && getY() == other.getY()
                    && getZ() == other.getZ();
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(41, 11)
                .append(getWorldUUID())
                .append(getX())
                .append(getY())
                .append(getZ())
                .toHashCode();
    }

    public static class Static extends ForgeLocation {

        private final UUID worldUUID;
        private final int x;
        private final int y;
        private final int z;

        public Static(final @NotNull Location location) {
            this(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        public Static(final @NotNull Block block) {
            this(block.getWorld(), block.getX(), block.getY(), block.getZ());
        }

        public Static(final @NotNull World world,
                      final int x,
                      final int y,
                      final int z) {
            this(world.getUID(), x, y, z);
        }

        public Static(final @NotNull UUID worldUUID,
                      final int x,
                      final int y,
                      final int z) {
            this.worldUUID = Objects.requireNonNull(worldUUID);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public @NotNull UUID getWorldUUID() {
            return this.worldUUID;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getZ() {
            return this.z;
        }

    }

}
