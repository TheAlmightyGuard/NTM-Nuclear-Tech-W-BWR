package com.hbm.tileentity.machine.bwr;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMachineBWRReactorVessel extends TileEntity {
	

	private int rotation;
    private boolean active;
    
    public TileEntityMachineBWRReactorVessel() {
        this.rotation = 0;
        this.active = false;
    }
    
    public int getRotation() {
        return rotation;
    }
    
    public void setRotation(int rotation) {
        this.rotation = rotation;
        this.markDirty();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        this.markDirty();
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.rotation = compound.getInteger("rotation");
        this.active = compound.getBoolean("active");
    }
    
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("rotation", rotation);
        compound.setBoolean("active", active);
    }
    
    @Override
    public void updateEntity() {
        // Add your update logic here if needed
    }
}