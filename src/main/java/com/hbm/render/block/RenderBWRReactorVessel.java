package com.hbm.render.block;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;


public class RenderBWRReactorVessel extends TileEntitySpecialRenderer {
    
	private IModelCustom model;
    private ResourceLocation texture;
    
    public RenderBWRReactorVessel() {
        // Use Forge's built-in OBJ loader instead of CCModel
    	this.model = AdvancedModelLoader.loadModel(new ResourceLocation("hbm:models/bwr/bwr_reactor.obj"));
        texture = new ResourceLocation(RefStrings.MODID, "textures/blocks/bwr/bwr_reactor.png"); // Sets image for the tile entity itself
    } 
        
    
    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        
        // Scale down if your model is too large
        GL11.glScalef(6f,7f,6f); // 1/16 scale
        
        bindTexture(texture);
        model.renderAll();
        GL11.glPopMatrix();
    }
}