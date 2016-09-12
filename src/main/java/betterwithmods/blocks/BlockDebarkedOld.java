package betterwithmods.blocks;

import betterwithmods.BWMod;
import betterwithmods.BWRegistry;
import betterwithmods.api.block.IBTWBlock;
import betterwithmods.api.block.IDebarkable;
import betterwithmods.client.BWCreativeTabs;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockDebarkedOld extends BlockOldLog implements IDebarkable,IBTWBlock
{
    public BlockDebarkedOld()
    {
        super();
        this.setCreativeTab(BWCreativeTabs.BWTAB);
        this.setUnlocalizedName("bwm:debarked_old");
        setRegistryName("debarked_old");
        GameRegistry.register(this);
        GameRegistry.register(BWMod.proxy.addItemBlockModel(new ItemBlockMeta(this)),getRegistryName());
    }

    @Override
    public String[] getVariants() {
        return  new String[]{"axis=y,variant=oak", "axis=y,variant=spruce", "axis=y,variant=birch", "axis=y,variant=jungle"};
    }

    @Override
    public ItemStack getBark(IBlockState state)
    {
        return new ItemStack(BWRegistry.bark, 1, this.damageDropped(state));
    }

    @Override
    public EnumFacing getFacing(IBlockAccess world, BlockPos pos) {
        return null;
    }

    @Override
    public void setFacing(World world, BlockPos pos, EnumFacing facing) {

    }

    @Override
    public EnumFacing getFacingFromBlockState(IBlockState state) {
        return null;
    }

    @Override
    public IBlockState setFacingInBlock(IBlockState state, EnumFacing facing) {
        return null;
    }

    @Override
    public boolean canRotateOnTurntable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canRotateHorizontally(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canRotateVertically(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public void rotateAroundYAxis(World world, BlockPos pos, boolean reverse) {

    }
}