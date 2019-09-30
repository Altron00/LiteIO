package crazypants.enderio.base.conduit.redstone.rsnew;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.EnumDyeColor;

public interface ISignal {

  /**
   * Gets the current cached signal value
   */
  @Nullable
  Integer get(@Nonnull EnumDyeColor channel);

  /**
   * Acquires the signal value and caches it.
   * 
   * @return true if the value changed, false otherwise
   */
  boolean acquire(@Nonnull IRedstoneConduitNetwork network);

  void setDirty();

  boolean isDirty();

  @Nonnull
  UID getUID();

  default boolean needsTicking() {
    return false;
  }

  /**
   * Ticks the signal. This is expected to acquire() automatically if needed.
   * 
   * @param network
   * @param changed
   *          true if the network's signal level changed since the last tick. Computed signals should use this to check if they need to acquire or not.
   * @return true if the value changed, false otherwise
   */
  default boolean tick(@Nonnull IRedstoneConduitNetwork network, boolean changed) {
    return false;
  }
}