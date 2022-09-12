package uk.protonull.smithery.utilities;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * An attempt to fix the ever-present problem of tracking amounts. There are two default implementations:
 * {@link ArrayMap} and {@link HashMap}. Be aware that this class doesn't support key preprocessing, so you <i>will</i>
 * need to manually call {@link org.bukkit.inventory.ItemStack#asOne()} for example if you intend to use items.
 *
 * @author Protonull
 */
public interface AmountMap<T> extends Object2IntMap<T> {

    /**
     * Removes "empties", ie: keys with non-positive amounts.
     */
    default void removeEmpties() {
        values().removeIf((final Integer amount) -> amount == null || amount < 1);
    }

    /**
     * @return Returns the total amount of things stored in this map.
     */
    default int getTotalAmount() {
        int amount = 0;
        for (final int currentAmount : values()) {
            if (currentAmount > 0) {
                amount += currentAmount;
            }
        }
        return amount;
    }

    /**
     * Computes the amount for a certain key. If the computer returns a non-positive amount, the key is considered
     * "empty" and will be removed.
     *
     * @param key The key to compute the amount of.
     * @param computer The computer method/lambda to call.
     * @return Returns the new amount value, or zero if "empty".
     */
    default int computeAmount(final T key,
                              final @NotNull IntUnaryOperator computer) {
        return compute(key, (_key, amount) -> {
            amount = computer.applyAsInt(amount == null ? defaultReturnValue() : amount);
            return amount <= 0 ? null : amount;
        });
    }

    /**
     * Convenience method to add a particular amount to a given key.
     *
     * @param key The key to add to.
     * @param amount The amount to add, which can be null.
     * @return Returns the new amount value, or zero if "empty".
     */
    default int changeAmountBy(final T key,
                               final int amount) {
        return computeAmount(key, (final int currentAmount) -> currentAmount + amount);
    }

    /**
     * @deprecated Changing the default value from 0 in an amount map is not supported!
     */
    @Deprecated
    @Override
    default void defaultReturnValue(final int returnValue) {
        throw new NotImplementedException("Please don't try to change the default return value!");
    }

    // ------------------------------------------------------------
    // Default Implementations
    // ------------------------------------------------------------

    /**
     * Predefined increment method useful for {@link #computeAmount(Object, IntUnaryOperator)}.
     */
    IntUnaryOperator INCREMENT = (amount) -> amount + 1;

    /**
     * Predefined decrement method useful for {@link #computeAmount(Object, IntUnaryOperator)}.
     */
    IntUnaryOperator DECREMENT = (amount) -> amount - 1;

    /**
     * Array-map implementation class for {@link AmountMap}.
     */
    class ArrayMap<T> extends Object2IntArrayMap<T> implements AmountMap<T> {
        public ArrayMap() {
            this(16);
        }
        public ArrayMap(final int size) {
            super(size);
        }
    }

    /**
     * Hash-map implementation class for {@link AmountMap}.
     */
    class HashMap<T> extends Object2IntOpenHashMap<T> implements AmountMap<T> {
        public HashMap() {
            this(16);
        }
        public HashMap(final int size) {
            super(size);
        }
    }

    /**
     * Wrapper class to make an {@link AmountMap} unmodifiable.
     */
    class Unmodifiable<T> extends Object2IntMaps.UnmodifiableMap<T> implements AmountMap<T> {
        public Unmodifiable(final @NotNull AmountMap<T> map) {
            super(Objects.requireNonNull(map));
        }
    }

}