package betterwithmods.blocks;

import betterwithmods.BWRegistry;
import betterwithmods.api.block.IMechanicalBlock;
import betterwithmods.util.InvUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCrank extends BTWBlock implements IMechanicalBlock
{
	public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 7);
	private static int tickRate = 3;
	private static int delayBeforeReset = 15;
	public static float baseHeight = 0.25F;
	public BlockCrank()
	{
		super(Material.ROCK, "hand_crank");
		this.setHardness(0.5F);
		this.setSoundType(SoundType.WOOD);
		this.setTickRandomly(true);
		this.setDefaultState(getDefaultState().withProperty(STAGE, 0));
	}

	@Override
	public String[] getVariants() {
		return new String[]{"stage=0"};
	}

	@Override
	public int tickRate(World world)
	{
		return 3;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		return world.isSideSolid(pos.down(), EnumFacing.UP);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int meta = state.getValue(STAGE).intValue();
		
		if(meta == 0)
		{
			if(player.getFoodStats().getFoodLevel() > 6)
			{
				player.addExhaustion(2.0F);
				if(!world.isRemote)
				{
					if(!checkForOverpower(world, pos))
					{
						world.setBlockState(pos, state.withProperty(STAGE, 1));
						world.markBlockRangeForRenderUpdate(pos, pos);
						world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 1.0F, 2.0F);
						world.scheduleBlockUpdate(pos, this, tickRate(world), 5);
					}
					else
						breakCrank(world, pos);
				}
			}
			else if(world.isRemote)
			{
				player.addChatMessage(new TextComponentString("You are too exhausted to turn it."));
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state)
    {
        return false;
    }
	
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		//if(side != 1)
			//return super.shouldSideBeRendered(world, x, y, z, side);
		return true;
	}
	
	public boolean checkForOverpower(World world, BlockPos pos)
	{
		int potentialDevices = 0;
		for(int i = 0; i < 6; i++)
		{
			BlockPos offset = pos.offset(EnumFacing.getFront(i));
			if(i != 0)
			{
				Block block = world.getBlockState(offset).getBlock();
				if(block != null && block instanceof IMechanicalBlock)
				{
					IMechanicalBlock mech = (IMechanicalBlock)block;
					if(mech.canInputMechanicalPower())
						potentialDevices++;
				}
			}
		}
		return potentialDevices > 1;
	}
	
	public void breakCrank(World world, BlockPos pos)
	{
		InvUtils.ejectStackWithOffset(world, pos, new ItemStack(Items.STICK));
		InvUtils.ejectStackWithOffset(world, pos, new ItemStack(Blocks.COBBLESTONE, 2, 0));
		InvUtils.ejectStackWithOffset(world, pos, new ItemStack(BWRegistry.material, 1, 0));
		//world.playAuxSFX(2235, x, y, z, 0);
		world.setBlockToAir(pos);
	}

	@Override
	public boolean canOutputMechanicalPower() 
	{
		return true;
	}

	@Override
	public boolean canInputMechanicalPower() 
	{
		return false;
	}

	@Override
	public boolean isInputtingMechPower(World world, BlockPos pos) 
	{
		return false;
	}

	@Override
	public boolean isOutputtingMechPower(World world, BlockPos pos) 
	{
		return world.getBlockState(pos).getValue(STAGE).intValue() > 1;
	}

	@Override
	public boolean canInputPowerToSide(IBlockAccess world, BlockPos pos, EnumFacing dir) 
	{
		return false;
	}

	@Override
	public void overpower(World world, BlockPos pos) 
	{
		
	}

	@Override
	public boolean isMechanicalOn(IBlockAccess world, BlockPos pos) 
	{
		return false;
	}

	@Override
	public void setMechanicalOn(World world, BlockPos pos, boolean isOn) 
	{
		
	}

	@Override
	public boolean isMechanicalOnFromState(IBlockState state) 
	{
		return false;
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		int meta = state.getValue(STAGE).intValue();
		
		if(meta > 0)
		{
			if(meta < 7)
			{
				if(meta <= 6)
					world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 1.0F, 2.0F);
				if(meta <= 5)
					world.scheduleBlockUpdate(pos, this, tickRate(world) + meta, 5);
				else
					world.scheduleBlockUpdate(pos, this, 15, 5);
				
				world.setBlockState(pos, state.withProperty(STAGE, meta + 1));
			}
			else
			{
				world.setBlockState(pos, state.withProperty(STAGE, 0));
				world.markBlockRangeForRenderUpdate(pos, pos);
				world.scheduleBlockUpdate(pos, this, tickRate(world), 5);//world.markBlockForUpdate(pos);
				world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.7F);
			}
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block)
	{
		BlockPos down = pos.down();
		if(!world.isSideSolid(down, EnumFacing.UP))
		{
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(STAGE, meta);
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(STAGE);
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, STAGE);
	}
}