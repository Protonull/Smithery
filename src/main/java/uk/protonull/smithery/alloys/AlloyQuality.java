package uk.protonull.smithery.alloys;

import org.jetbrains.annotations.NotNull;

public enum AlloyQuality {
    BEST,
    GOOD,
    OKAY,
    POOR;

    /**
     * @return Returns true if this quality is BEST.
     */
    public boolean isBest() {
        return this == BEST;
    }

    /**
     * @return Returns the quality considered better than this quality. Cannot go higher than BEST.
     */
    public @NotNull AlloyQuality upgrade() {
        return switch (this) {
            case BEST, GOOD -> BEST;
            case OKAY -> GOOD;
            case POOR -> OKAY;
        };
    }

    /**
     * @return Returns the quality considered lesser than this quality. Cannot go lower than POOR.
     */
    public @NotNull AlloyQuality downgrade() {
        return switch (this) {
            case BEST -> GOOD;
            case GOOD -> OKAY;
            case OKAY, POOR -> POOR;
        };
    }

}
