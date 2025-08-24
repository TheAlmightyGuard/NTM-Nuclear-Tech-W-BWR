package com.hbm.blocks.machine.bwr;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.bwr.TileEntityMachineBWRReactorVessel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BWRReactorVessel extends BlockContainer {
    
    // Cylinder dimensions - can be adjusted as needed
    private final int CYLINDER_DIAMETER = 11; // Must be odd number (3, 5, 7, etc.)
    private final int CYLINDER_HEIGHT = 15;
    
    // Safe removal flag to prevent infinite loops
    private static boolean safeRemoval = false;
    
    public ResourceLocation coverTexture;
    
    public BWRReactorVessel(Material material) {
        super(material);
        this.setHardness(3.0F);
        this.setResistance(15.0F);
        this.setBlockName("bwr_reactor");
        this.setBlockTextureName(RefStrings.MODID + ":bwr/bwr_reactor");
        this.setCreativeTab(CreativeTabs.tabBlock);
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
        // Get player's facing direction
        int direction = MathHelper.floor_double((double)((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;
        
        // Calculate radius (half of diameter)
        int radius = (CYLINDER_DIAMETER - 1) / 2;
        int extraOffset = 1; // Add 1 more block of distance
        
        int newX = x;
        int newZ = z;
        
        // Place the center of the structure (radius + 1) blocks in front of the player
        switch (direction) {
            case 0: // Player facing SOUTH - place center SOUTH of click position
                newZ = z + radius + extraOffset;
                break;
            case 1: // Player facing WEST - place center WEST of click position
                newX = x - radius - extraOffset;
                break;
            case 2: // Player facing NORTH - place center NORTH of click position
                newZ = z - radius - extraOffset;
                break;
            case 3: // Player facing EAST - place center EAST of click position
                newX = x + radius + extraOffset;
                break;
        }
        
        // Move the block to the new position if it's different
        if (newX != x || newZ != z) {
            // Remove the original block
            world.setBlockToAir(x, y, z);
            
            // Place the block at the new position
            if (world.isAirBlock(newX, y, newZ) || world.getBlock(newX, y, newZ).getMaterial().isReplaceable()) {
                world.setBlock(newX, y, newZ, this, 0, 3);
                
                // Create the structure from the new position
                if (!safeRemoval) {
                    createCylinderStructure(world, newX, y, newZ);
                }
            }
        } else {
            // If no movement needed, create structure normally
            super.onBlockPlacedBy(world, x, y, z, player, itemStack);
            if (!safeRemoval) {
                createCylinderStructure(world, x, y, z);
            }
        }
    }
   
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        
        // Only create structure if this is the main block (metadata 0) and not during safe removal
        // Note: Structure creation is now handled in onBlockPlacedBy to ensure proper placement
    }
    
    private void createCylinderStructure(World world, int x, int y, int z) {
        int radius = (CYLINDER_DIAMETER - 1) / 2; // Calculate radius from diameter
        
        safeRemoval = true;
        
        // Create cylindrical structure with rounded corners
        for (int dy = 0; dy < CYLINDER_HEIGHT; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    int targetZ = z + dz;
                    
                    // Calculate distances for rounded corners
                    int maxDistance = Math.max(Math.abs(dx), Math.abs(dz)); // Square shape
                    double euclideanDistance = Math.sqrt(dx * dx + dz * dz); // Round shape
                    
                    // Create rounded corners: use square shape but round the corners
                    // Keep the square shape but smooth the corners
                    boolean shouldPlace = maxDistance <= radius;
                    
                    // Round the corners by excluding blocks that are too far in the corners
                    if (shouldPlace) {
                        // For corner blocks, check if they're within the rounded boundary
                        if (Math.abs(dx) == radius && Math.abs(dz) == radius) {
                            // This is a corner block - only place if it's within the rounded radius
                            shouldPlace = euclideanDistance <= radius + 0.5; // Allow slight extension for smooth corners
                        } else if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                            // This is an edge block - always place for smooth transition
                            shouldPlace = true;
                        }
                    }
                    
                    // Only create blocks within the cylinder boundaries
                    if (shouldPlace) {
                        // Skip the position of the reactor vessel itself (center at current height)
                        if (dx == 0 && dz == 0 && dy == 0) continue;
                        
                        // Only replace air or replaceable blocks
                        if (world.isAirBlock(targetX, targetY, targetZ) || 
                            world.getBlock(targetX, targetY, targetZ).getMaterial().isReplaceable()) {
                            
                            int meta = 0;
                            
                            // Determine metadata based on relative position
                            if (dy < 0) {
                                meta = ForgeDirection.DOWN.ordinal();
                            } else if (dy > 0) {
                                meta = ForgeDirection.UP.ordinal();
                            } else if (dx < 0) {
                                meta = ForgeDirection.WEST.ordinal();
                            } else if (dx > 0) {
                                meta = ForgeDirection.EAST.ordinal();
                            } else if (dz < 0) {
                                meta = ForgeDirection.NORTH.ordinal();
                            } else if (dz > 0) {
                                meta = ForgeDirection.SOUTH.ordinal();
                            }
                            
                            world.setBlock(targetX, targetY, targetZ, this, meta, 3);
                        }
                    }
                }
            }
        }
        
        safeRemoval = false;
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Only break structure if this is the main block
        if (meta == 0 && !safeRemoval) {
            breakCylinderStructure(world, x, y, z);
        }
        
        super.breakBlock(world, x, y, z, block, meta);
    }
    
    private void breakCylinderStructure(World world, int x, int y, int z) {
        int radius = (CYLINDER_DIAMETER - 1) / 2;
        
        safeRemoval = true;
        
        // Break all connected structure blocks in rounded pattern
        for (int dy = 0; dy < CYLINDER_HEIGHT; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    int targetZ = z + dz;
                    
                    // Calculate distances for rounded corners (same logic as creation)
                    int maxDistance = Math.max(Math.abs(dx), Math.abs(dz));
                    double euclideanDistance = Math.sqrt(dx * dx + dz * dz);
                    
                    boolean shouldBreak = maxDistance <= radius;
                    
                    if (shouldBreak) {
                        if (Math.abs(dx) == radius && Math.abs(dz) == radius) {
                            shouldBreak = euclideanDistance <= radius + 0.5;
                        } else if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                            shouldBreak = true;
                        }
                    }
                    
                    // Only break blocks within the rounded boundaries
                    if (shouldBreak) {
                        // Skip the position of the reactor vessel itself
                        if (dx == 0 && dz == 0 && dy == 0) continue;
                        
                        // Break the structure block if it's this block
                        if (world.getBlock(targetX, targetY, targetZ) == this) {
                            world.setBlockToAir(targetX, targetY, targetZ);
                        }
                    }
                }
            }
        }
        
        safeRemoval = false;
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        
        // For main block (metadata 0)
        if (meta == 0 && !safeRemoval) {
            // If any structure block is broken, break the entire structure
            if (!isCylinderStructureIntact(world, x, y, z)) {
                breakCylinderStructure(world, x, y, z);
                world.setBlockToAir(x, y, z);
            }
        }
        // For structure blocks (metadata > 0)
        else if (meta > 0 && !safeRemoval) {
            // Find the main block and check if structure is intact
            int[] mainBlockPos = findMainBlock(world, x, y, z);
            if (mainBlockPos != null) {
                int mainX = mainBlockPos[0];
                int mainY = mainBlockPos[1];
                int mainZ = mainBlockPos[2];
                
                // If structure is not intact, break everything
                if (!isCylinderStructureIntact(world, mainX, mainY, mainZ)) {
                    breakCylinderStructure(world, mainX, mainY, mainZ);
                    world.setBlockToAir(mainX, mainY, mainZ);
                }
            } else {
                // If main block can't be found, break this structure block
                world.setBlockToAir(x, y, z);
            }
        }
    }

    private int[] findMainBlock(World world, int x, int y, int z) {
        int radius = (CYLINDER_DIAMETER - 1) / 2;
        int searchHeight = CYLINDER_HEIGHT;
        
        // Search in a larger area to account for the cylinder size
        for (int dy = -searchHeight; dy <= searchHeight; dy++) {
            for (int dx = -radius * 2; dx <= radius * 2; dx++) {
                for (int dz = -radius * 2; dz <= radius * 2; dz++) {
                    int checkX = x + dx;
                    int checkY = y + dy;
                    int checkZ = z + dz;
                    
                    // Check if this is the main reactor vessel block (metadata 0)
                    if (world.getBlock(checkX, checkY, checkZ) == this && 
                        world.getBlockMetadata(checkX, checkY, checkZ) == 0) {
                        return new int[]{checkX, checkY, checkZ};
                    }
                }
            }
        }
        return null; // Main block not found
    }
    
    private boolean isCylinderStructureIntact(World world, int x, int y, int z) {
        int radius = (CYLINDER_DIAMETER - 1) / 2;
        
        // Check if all rounded structure positions are still intact
        for (int dy = 0; dy < CYLINDER_HEIGHT; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    int targetZ = z + dz;
                    
                    // Calculate distances for rounded corners (same logic as creation)
                    int maxDistance = Math.max(Math.abs(dx), Math.abs(dz));
                    double euclideanDistance = Math.sqrt(dx * dx + dz * dz);
                    
                    boolean shouldCheck = maxDistance <= radius;
                    
                    if (shouldCheck) {
                        if (Math.abs(dx) == radius && Math.abs(dz) == radius) {
                            shouldCheck = euclideanDistance <= radius + 0.5;
                        } else if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                            shouldCheck = true;
                        }
                    }
                    
                    // Only check blocks within the rounded boundaries
                    if (shouldCheck) {
                        // Skip the position of the reactor vessel itself
                        if (dx == 0 && dz == 0 && dy == 0) continue;
                        
                        // If any structure position is not this block, the structure is broken
                        if (world.getBlock(targetX, targetY, targetZ) != this) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        // Only create tile entity for the main block (metadata 0)
        if (metadata == 0) {
            return new TileEntityMachineBWRReactorVessel();
        }
        return null;
    }
    
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    @Override
    public int getRenderType() {
        return -1;
    }
}