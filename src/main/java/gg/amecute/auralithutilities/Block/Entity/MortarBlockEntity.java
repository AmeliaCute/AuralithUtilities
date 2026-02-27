package gg.amecute.auralithutilities.Block.Entity;

import gg.amecute.auralithutilities.Registries.AuralithBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a stack of raw ores (up to MAX_STACK) and tracks pestle hits.
 *
 * Hits required = count  →  1 hit per item, so 8 raw_iron needs 8 hits.
 * Each completed hit outputs 2 crushed ore per item in the stack.
 *
 * hit_progress is synced to client so the renderer can show a "fill" level.
 */
public class MortarBlockEntity extends BlockEntity {

  public static final int MAX_STACK = 16;

  private ItemStack storedOre  = ItemStack.EMPTY;
  private int       hitsApplied = 0;   // how many pestle hits so far

  public MortarBlockEntity(BlockPos pos, BlockState state) {
    super(AuralithBlockEntities.MORTAR.get(), pos, state);
  }

  // ── Ore slot ─────────────────────────────────────────────────────────────

  public boolean isEmpty() { return storedOre.isEmpty(); }

  public ItemStack getStoredOre() { return storedOre; }

  /** Total hits needed to process the current stack (1 per item). */
  public int hitsNeeded() { return storedOre.isEmpty() ? 0 : storedOre.getCount(); }

  public int hitsApplied() { return hitsApplied; }

  /** 0.0 → 1.0 progress toward completion. */
  public float progress() {
    int needed = hitsNeeded();
    return needed == 0 ? 0f : (float) hitsApplied / needed;
  }

  /**
   * Try to add `incoming` to the mortar.
   * - If empty: accepts up to MAX_STACK items.
   * - If same item already inside: tops up to MAX_STACK.
   * - If different item: refuses.
   * Returns how many items were actually inserted.
   */
  public int insert(ItemStack incoming) {
    if (incoming.isEmpty()) return 0;

    if (!storedOre.isEmpty() && !ItemStack.isSameItem(storedOre, incoming))
      return 0; // different item type

    int room   = MAX_STACK - (storedOre.isEmpty() ? 0 : storedOre.getCount());
    int toAdd  = Math.min(room, incoming.getCount());
    if (toAdd <= 0) return 0;

    if (storedOre.isEmpty()) {
      storedOre = incoming.copyWithCount(toAdd);
    } else {
      storedOre.grow(toAdd);
    }

    markDirty();
    return toAdd;
  }

  /**
   * Apply one pestle hit.
   * @return true if the stack is now fully processed and ready to extract.
   */
  public boolean applyHit() {
    if (storedOre.isEmpty()) return false;
    hitsApplied = Math.min(hitsApplied + 1, hitsNeeded());
    markDirty();
    return hitsApplied >= hitsNeeded();
  }

  /** Consume the processed stack and reset. Call after applyHit() returns true. */
  public ItemStack extractProcessed() {
    ItemStack result = storedOre.copy();
    storedOre   = ItemStack.EMPTY;
    hitsApplied = 0;
    markDirty();
    return result;
  }

  /** Retrieve the raw ore without processing (player taking it back). */
  public ItemStack extractRaw() {
    ItemStack result = storedOre.copy();
    storedOre   = ItemStack.EMPTY;
    hitsApplied = 0;
    markDirty();
    return result;
  }

  // ── Persistence ──────────────────────────────────────────────────────────

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    if (!storedOre.isEmpty()) tag.put("stored_ore", storedOre.save(registries));
    tag.putInt("hits_applied", hitsApplied);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    storedOre   = tag.contains("stored_ore")
        ? ItemStack.parseOptional(registries, tag.getCompound("stored_ore"))
        : ItemStack.EMPTY;
    hitsApplied = tag.getInt("hits_applied");
  }

  // ── Client sync ──────────────────────────────────────────────────────────

  private void markDirty() {
    setChanged();
    if (level != null && !level.isClientSide)
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }
}