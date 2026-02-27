package gg.amecute.auralithutilities.Block;

import com.mojang.serialization.MapCodec;
import gg.amecute.auralithutilities.Block.Entity.MortarBlockEntity;
import gg.amecute.auralithutilities.Item.PestleItem;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingEntry;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MortarBlock extends BaseEntityBlock 
{

  private static final MapCodec<MortarBlock> CODEC = simpleCodec(MortarBlock::new);
  private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);

  public MortarBlock(Properties properties) { super(properties); }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

  @Override
  public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

  @Override
  public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new MortarBlockEntity(pos, state);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) 
  {
    if (!(level.getBlockEntity(pos) instanceof MortarBlockEntity mortar))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

    if (PestleItem.isPestle(stack) && !mortar.isEmpty()) {
      if (!level.isClientSide) pestleHit(mortar, player, stack, (ServerLevel) level, pos);
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    String oreId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    if (OreProcessingRegistry.isRawOre(oreId)) {

      if (!level.isClientSide) 
      {
        ItemStack toInsert = player.isShiftKeyDown()
            ? stack
            : stack.copyWithCount(1);

        int inserted = mortar.insert(toInsert);
        if (inserted > 0) {
          stack.shrink(inserted);
          player.setItemInHand(hand, stack);
          playInsertSound(level, pos);
        } else 
        {
          player.displayClientMessage(
              net.minecraft.network.chat.Component.translatable(
                  mortar.isEmpty() ? "block.auralithcore.mortar.full"
                  : "block.auralithcore.mortar.wrong_item"),
              true);
        }
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof MortarBlockEntity mortar))
      return InteractionResult.PASS;

    if (!mortar.isEmpty() && !level.isClientSide) 
    {
      ItemStack ore = mortar.extractRaw();
      if (!player.getInventory().add(ore)) spawnDrop(level, pos, ore);
      level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.0f);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
  {
    if (!state.is(newState.getBlock())) 
      if (level.getBlockEntity(pos) instanceof MortarBlockEntity mortar && !mortar.isEmpty()) spawnDrop(level, pos, mortar.extractRaw());
    
    super.onRemove(state, level, pos, newState, movedByPiston);
  }

  private void pestleHit(MortarBlockEntity mortar, Player player, ItemStack pestleStack, ServerLevel level, BlockPos pos) 
  {

    boolean done = mortar.applyHit();

    pestleStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

    float pitch = 0.8f + mortar.progress() * 0.4f;
    level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.7f, pitch);

    int particleCount = done ? 20 : 5;
    level.sendParticles(ParticleTypes.CRIT,
        pos.getX() + 0.5, pos.getY() + 0.55, pos.getZ() + 0.5,
        particleCount, 0.25, 0.1, 0.25, 0.04);

    if (done) 
    {
      ItemStack processed = mortar.extractProcessed();
      String oreId = BuiltInRegistries.ITEM.getKey(processed.getItem()).toString();

      Optional<OreProcessingEntry> entryOpt = OreProcessingRegistry.getEntryForRaw(oreId);
      if (entryOpt.isEmpty()) return;

      ResourceLocation crushedRl = ResourceLocation.parse(entryOpt.get().crushed());
      Item crushedItem = BuiltInRegistries.ITEM.get(crushedRl);
      ItemStack output = new ItemStack(crushedItem, processed.getCount() * 2);
      spawnDrop(level, pos, output);

      level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
  }

  private static void playInsertSound(Level level, BlockPos pos) 
  {
    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);
  }

  private static void spawnDrop(Level level, BlockPos pos, ItemStack stack) 
  {
    ItemEntity e = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, stack);
    e.setDefaultPickUpDelay();
    level.addFreshEntity(e);
  }
}