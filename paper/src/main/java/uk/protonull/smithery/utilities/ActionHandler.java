package uk.protonull.smithery.utilities;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class ActionHandler {

    /**
     * These are <b>ALL</b> the different types of interactions one can have with a Forge. If any others are added,
     * they should be added here too.
     */
    public enum Action {
        ADD_FUEL_TO_FORGE,
        READ_FORGE_SMELT_TIME,
        LIST_FORGE_CONTENTS,
        ADD_CONTENTS_TO_FORGE,
        FLUSH_FORGE_CONTENTS,
        COLLECT_FORGE_RESULT,
        COOL_METAL_IN_CAULDRON
    }

    @FunctionalInterface
    public interface Checker {
        /**
         * Determines whether a particular interaction can occur.
         *
         * @param object The object being interacted with.
         * @param player The player interacting with the Forge.
         * @param action The recipe of interaction.
         * @return Returns true if the interaction is allowed.
         */
        boolean check(Object object,
                      @NotNull Player player,
                      @NotNull Action action);
    }

    public final Checker DEFAULT_CHECKER = (object, player, process) -> switch (process) {
        case READ_FORGE_SMELT_TIME -> player.hasPermission("smithery.checktime");
        case ADD_CONTENTS_TO_FORGE -> player.hasPermission("smithery.smelt");
        case LIST_FORGE_CONTENTS -> player.hasPermission("smithery.inspect");
        case COLLECT_FORGE_RESULT -> player.hasPermission("smithery.remove");
        case COOL_METAL_IN_CAULDRON -> player.hasPermission("smithery.cool");
        default -> true;
    };

    private Checker CHECKER = DEFAULT_CHECKER;

    /**
     * This allows you to set a custom handler, should you choose to. If you wish to reset the handler to the default,
     * just pass in {@link #DEFAULT_CHECKER} as the handler.
     *
     * @param checker The new handler to set.
     */
    public void setChecker(final @NotNull Checker checker) {
        CHECKER = Objects.requireNonNull(checker, "Why are you trying to set a null checker?");
    }

    /**
     * Determines whether a particular interaction can occur.
     *
     * @param object The object being interacted with.
     * @param player The player interacting with the Forge.
     * @param action The recipe of interaction.
     * @return Returns true if the interaction is allowed.
     */
    public boolean canHandle(final Object object,
                             final @NotNull Player player,
                             final @NotNull Action action) {
        return CHECKER.check(object,
                Objects.requireNonNull(player, "Player cannot be null!"),
                Objects.requireNonNull(action, "Action cannot be null!"));
    }

}
