package betterwithmods.common.blocks.mechanical.tile;

import betterwithmods.api.capabilities.CapabilityMechanicalPower;
import betterwithmods.api.tile.IMechanicalPower;
import betterwithmods.common.BWSounds;
import betterwithmods.common.blocks.mechanical.BlockMechMachines;
import betterwithmods.common.blocks.tile.TileBasicInventory;
import betterwithmods.common.registry.bulk.manager.MillManager;
import betterwithmods.common.registry.bulk.recipes.MillRecipe;
import betterwithmods.util.InvUtils;
import betterwithmods.util.MechanicalUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityMill extends TileBasicInventory implements ITickable, IMechanicalPower {

    public static final int GRIND_TIME = 200;

    public int power;
    public int grindCounter;
    private int grindType = 0;
    private boolean validateContents;
    private boolean containsIngredientsToGrind;

    public TileEntityMill() {
        this.grindCounter = 0;
        this.validateContents = true;
    }

    public boolean isActive() {
        return power > 0;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public BlockMechMachines getBlock() {
        return (BlockMechMachines) this.getBlockType();
    }

    @Override
    public void update() {
        if (this.getBlockWorld().isRemote)
            return;

        this.power = calculateInput();

        getBlock().setActive(world, pos, isActive());

        if (this.validateContents)
            validateContents();

        if (isActive())
            if (getBlockWorld().rand.nextInt(20) == 0)
                getBlockWorld().playSound(null, pos, BWSounds.STONEGRIND, SoundCategory.BLOCKS, 0.5F + getBlockWorld().rand.nextFloat() * 0.1F, 0.5F + getBlockWorld().rand.nextFloat() * 0.1F);

        if (this.containsIngredientsToGrind && isActive()) {
            if (!this.getBlockWorld().isRemote) {
                if (grindType == 2) {
                    if (this.getBlockWorld().rand.nextInt(25) < 2) {
                        getBlockWorld().playSound(null, pos, SoundEvents.ENTITY_GHAST_HURT, SoundCategory.BLOCKS, 1F, getBlockWorld().rand.nextFloat() * 0.4F + 0.8F);
                    }
                } else if (grindType == 1) {
                    if (this.getBlockWorld().rand.nextInt(20) < 2)
                        getBlockWorld().playSound(null, pos, SoundEvents.ENTITY_WOLF_HURT, SoundCategory.BLOCKS, 2.0F, (getBlockWorld().rand.nextFloat() - getBlockWorld().rand.nextFloat()) * 0.2F + 1.0F);
                }
            }
            this.grindCounter++;
            if (this.grindCounter > GRIND_TIME - 1) {
                grindContents();
                this.grindCounter = 0;
                this.validateContents = true;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("GrindCounter"))
            this.grindCounter = tag.getInteger("GrindCounter");
        this.power = tag.getInteger("power");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("GrindCounter", this.grindCounter);
        tag.setInteger("power", power);
        return tag;
    }

    @Override
    public int getInventorySize() {
        return 3;
    }

    public int getGrindType() {
        return this.grindType;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        validateContents();
        if (this.getBlockWorld() != null && !this.getBlockWorld().isRemote) {
            if (grindType == 1)
                this.getBlockWorld().playSound(null, this.pos, SoundEvents.ENTITY_WOLF_WHINE, SoundCategory.BLOCKS, 1F, 1.0F);
            this.validateContents = true;
        }
    }

    private void ejectStack(ItemStack stack) {
        List<EnumFacing> validDirections = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            IBlockState check = getBlockWorld().getBlockState(pos.offset(facing));
            if (check.getBlock().isReplaceable(getBlockWorld(), pos.offset(facing)) || getBlockWorld().isAirBlock(pos.offset(facing)))
                validDirections.add(facing);
        }

        if (validDirections.isEmpty()) {
            IBlockState down = getBlockWorld().getBlockState(pos.offset(EnumFacing.DOWN));
            if (down.getBlock().isReplaceable(getBlockWorld(), pos.offset(EnumFacing.DOWN)) || getBlockWorld().isAirBlock(pos.offset(EnumFacing.DOWN)))
                validDirections.add(EnumFacing.DOWN);
        }

        BlockPos offset;
        if (validDirections.size() > 1)
            offset = pos.offset(validDirections.get(getBlockWorld().rand.nextInt(validDirections.size())));
        else if (validDirections.isEmpty())
            offset = pos.offset(EnumFacing.UP);
        else
            offset = pos.offset(validDirections.get(0));

        InvUtils.ejectStackWithOffset(getBlockWorld(), offset, stack);
    }

    public double getGrindProgress() {
        return this.grindCounter / (double) GRIND_TIME;
    }

    public boolean isGrinding() {
        return this.grindCounter > 0;
    }

    private boolean grindContents() {
        MillManager mill = MillManager.getInstance();
        List<Object> ingredients = mill.getValidCraftingIngredients(inventory);

        if (ingredients != null) {
            if (grindType == 1)
                this.getBlockWorld().playSound(null, pos, SoundEvents.ENTITY_WOLF_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            NonNullList<ItemStack> output = mill.craftItem(world, this, inventory);
            if (!output.isEmpty()) {
                for (ItemStack anOutput : output) {
                    ItemStack stack = anOutput.copy();
                    if (!stack.isEmpty())
                        ejectStack(stack);
                }
            }
            return true;
        }
        return false;
    }

    private void validateContents() {
        int oldGrindType = getGrindType();
        int newGrindType = 0;
        MillRecipe recipe = MillManager.getInstance().getMostValidRecipe(inventory);
        if (recipe != null) {
            this.containsIngredientsToGrind = true;
            newGrindType = recipe.getGrindType();
        } else {
            this.grindCounter = 0;
            this.containsIngredientsToGrind = false;
        }
        this.validateContents = false;
        if (oldGrindType != newGrindType) {
            this.grindType = newGrindType;
        }
    }

    @Override
    public int getMechanicalOutput(EnumFacing facing) {
        return -1;
    }

    @Override
    public int getMechanicalInput(EnumFacing facing) {
        if (facing.getAxis().isVertical())
            return MechanicalUtil.getPowerOutput(world, pos.offset(facing), facing.getOpposite());
        if(world.getTileEntity(pos.offset(facing)) instanceof TileCrank) {
            return MechanicalUtil.getPowerOutput(world, pos.offset(facing), facing.getOpposite());
        }
        return 0;
    }

    @Override
    public int getMaximumInput(EnumFacing facing) {
        return 1;
    }

    @Override
    public int getMinimumInput(EnumFacing facing) {
        return 0;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
        if (capability == CapabilityMechanicalPower.MECHANICAL_POWER)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Nonnull
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
        if (capability == CapabilityMechanicalPower.MECHANICAL_POWER)
            return CapabilityMechanicalPower.MECHANICAL_POWER.cast(this);
        return super.getCapability(capability, facing);
    }

    @Override
    public World getBlockWorld() {
        return super.getWorld();
    }

    @Override
    public BlockPos getBlockPos() {
        return getPos();
    }
}
