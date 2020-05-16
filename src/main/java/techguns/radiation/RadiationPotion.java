package techguns.radiation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import techguns.TGSounds;
import techguns.api.radiation.TGRadiation;
import techguns.capabilities.TGExtendedPlayer;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.gui.player.TGPlayerInventoryGui;

public class RadiationPotion extends Effect {

	public RadiationPotion() {
		super(true, 0xcff8ff00);
	}

	@Override
	public void renderInventoryEffect(int x, int y, EffectInstance effect, Minecraft mc) {
		super.renderInventoryEffect(x, y, effect, mc);

		if(mc.currentScreen!=null) {
			mc.getTextureManager().bindTexture(TGPlayerInventoryGui.texture);
			mc.currentScreen.drawTexturedModalRect(x+8, y+8, 0, 168, 16, 16);
			mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
		}
	}

	@Override
	public void renderHUDEffect(int x, int y, EffectInstance effect, Minecraft mc, float alpha) {
		super.renderHUDEffect(x, y, effect, mc, alpha);
		
		mc.getTextureManager().bindTexture(TGPlayerInventoryGui.texture);
		
		mc.ingameGUI.drawTexturedModalRect(x+4, y+4, 0, 168, 16, 16);
		mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
	}

	@Override
	public List<ItemStack> getCurativeItems() {
		//Rad can't be cured by milk buckets, return new list
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		return ret;
	}

	@Override
	public void performEffect(LivingEntity elb, int amplifier) {
		
		int res = (int) elb.getEntityAttribute(TGRadiation.RADIATION_RESISTANCE).getAttributeValue();
		
		int amount = techguns.util.MathUtil.clamp(amplifier+1-res, 0, 1000);
		
		//System.out.println("Radiate:"+ amount + "|RES:"+res);
		
		if(elb instanceof PlayerEntity) {
			if(!((PlayerEntity)elb).capabilities.isCreativeMode ) {
				TGExtendedPlayer props = TGExtendedPlayer.get((PlayerEntity) elb);
				props.addRadiation(amount);
				
				if(elb.world.isRemote && amount > 0) {
					if ( amplifier >= 2) {
						elb.world.playSound((PlayerEntity)elb, elb.posX, elb.posY, elb.posZ, TGSounds.GEIGER_HIGH, SoundCategory.PLAYERS, 1f, 1f);
					} else {
						elb.world.playSound((PlayerEntity)elb, elb.posX, elb.posY, elb.posZ, TGSounds.GEIGER_LOW, SoundCategory.PLAYERS, 1f, 1f);
					}
				}
				
				/*if(props.radlevel>=TGRadiationSystem.MINOR_POISONING) {
					elb.addPotionEffect(new PotionEffect(MobEffects.HUNGER,20,0,false,false));
					
				}
				if (props.radlevel>=TGRadiationSystem.SEVERE_POISONING) {
					elb.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,20,0,false,false));
					elb.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS,20,0,false,false));
				} 
				if(props.radlevel>=TGRadiationSystem.LETHAL_POISONING) {
					elb.addPotionEffect(new PotionEffect(MobEffects.NAUSEA,20,0,false,false));
					elb.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,20,0,false,false));
					elb.attackEntityFrom(TGDamageSource.causeRadiationDamage(null, null, DeathType.LASER), 5.0f);
				}*/
			}
		} else {
			if ( elb instanceof MobEntity) {
				float damage = amount *0.5f;
				if(damage>=1.0f) {
					elb.attackEntityFrom(TGDamageSource.causeRadiationDamage(null, null, DeathType.BIO), damage);
				}
			}
		}
		
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return duration % 20 == 0;
	}

	
	
}
