package uk.protonull.smithery.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This class is a wrapper around a conceptual config element.
 */
public abstract class ConfigElement<T> {

    private T element;

    /**
     * @return Returns the config element. Will attempt to parse the config element if there's currently no value. The
     * returned value should, for safety reasons, be immutable.
     */
    public @NotNull T get() {
        return this.element == null ?
                this.element = Objects.requireNonNull(parseElement(), "Element parser returned null!") :
                this.element;
    }

    /**
     * This method must be overloaded to extract the value of the config element, which CANNOT be null!
     *
     * @return Returns the parsed config element.
     */
    protected abstract @NotNull T parseElement();

    /**
     * Resets this config element's value back to null. Call this when Smeltery is being disabled or when you wish to
     * reload and reparse the config.
     */
    final void reset() {
        this.element = null;
    }

}
