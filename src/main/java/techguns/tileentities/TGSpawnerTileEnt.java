package techguns.tileentities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.entity.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import techguns.capabilities.TGSpawnerNPCData;
import techguns.entities.npcs.ITGSpawnerNPC;

public class TGSpawnerTileEnt extends BasicTGTileEntity implements ITickable {

	protected Random rand = new Random();
	protected int delay=200;
	protected int spawndelay = 200;
	protected int mobsLeft=5;
	protected int maxActive=3;
	
	protected int spawnHeightOffset=0;
	
	//protected static final int retrydelay = 40;
	protected double spawnrange=2d;
	
	protected ArrayList<WeightedSpawnerEntity> mobtypes = new ArrayList<>();
	
	protected LinkedList<ITGSpawnerNPC> activeMobs = new LinkedList<>();
	
	protected ItemStack weaponOverride = ItemStack.EMPTY;
	
	public TGSpawnerTileEnt() {
		super(false);
		//this.addMobType(ZombieSoldier.class, 100);
	}
	
	public <T extends MobEntity & ITGSpawnerNPC> void addMobType(Class<T> clazz, int weight) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.setString("id", EntityRegistry.getEntry(clazz).getRegistryName().toString());
		
		WeightedSpawnerEntity ent = new WeightedSpawnerEntity(weight, nbt);
		this.mobtypes.add(ent);
	}

	public void despawnedEntity(ITGSpawnerNPC ent) {
		this.activeMobs.remove(ent);
	}
	
	public void killedEntity(ITGSpawnerNPC ent) {
		if(this.activeMobs.remove(ent)) {
			this.mobsLeft--;
			this.markDirty();
		}
	}
	
	public void relinkNPC(ITGSpawnerNPC ent) {
		if(!this.activeMobs.contains(ent)) {
			this.activeMobs.add(ent);
		}
	}
	
	@Override
	public boolean canBeWrenchRotated() {
		return false;
	}

	@Override
	public boolean canBeWrenchDismantled() {
		return false;
	}
	
	public void setParams(int mobsleft, int maxactive, int spawndelay, int spawnrange) {
		this.mobsLeft=mobsleft;
		this.maxActive=maxactive;
		this.spawndelay=spawndelay;
		this.delay=spawndelay;
		this.spawnrange=spawnrange;
	}
	
	public TGSpawnerTileEnt setWeaponOverride(ItemStack weapon) {
		this.weaponOverride = weapon;
		return this;
	}
	
	public void setSpawnHeightOffset(int offset) {
		this.spawnHeightOffset=offset;
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT compound) {
		compound.setByte("mobsLeft", (byte) this.mobsLeft);
		compound.setShort("delay", (short) this.delay);
		compound.setByte("maxActive", (byte) this.maxActive);
		compound.setShort("spawnDelay", (short) this.spawndelay);
		compound.setByte("spawnRange", (byte)this.spawnrange);
		compound.setShort("spawnHeightOffset", (short) this.spawnHeightOffset);
		
        ListNBT nbttaglist = new ListNBT();
        for(WeightedSpawnerEntity type : this.mobtypes) {
            nbttaglist.appendTag(type.toCompoundTag());
        }
        compound.setTag("mobtypes", nbttaglist);
		
        if(!weaponOverride.isEmpty()) {
        	CompoundNBT weapon = this.weaponOverride.writeToNBT(new CompoundNBT());
        	compound.setTag("weapon", weapon);
        }
        
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(CompoundNBT compound) {
		this.mobsLeft = compound.getByte("mobsLeft");
		this.delay = compound.getShort("delay");
		this.spawndelay = compound.getShort("spawnDelay");
		if(spawndelay<1) {
			spawndelay=200;
		}
		this.maxActive = compound.getShort("maxActive");
		if(maxActive<1) {
			maxActive=1;
		}
		this.spawnrange=compound.getByte("spawnRange");
		this.spawnHeightOffset = compound.getShort("spawnHeightOffset");
		
		if (compound.hasKey("mobtypes", 9))
        {
            ListNBT nbttaglist = compound.getTagList("mobtypes", 10);

            this.mobtypes.clear();
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                this.mobtypes.add(new WeightedSpawnerEntity(nbttaglist.getCompoundTagAt(i)));
            }
        }
		
		if(compound.hasKey("weapon", 10)) {
			CompoundNBT weapon = compound.getCompoundTag("weapon");
			this.weaponOverride = new ItemStack(weapon);
		}
		
		super.readFromNBT(compound);
	}

	@Override
	public void writeClientDataToNBT(CompoundNBT tags) {
		super.writeClientDataToNBT(tags);
	}

	@Override
	public void readClientDataFromNBT(CompoundNBT tags) {
		super.readClientDataFromNBT(tags);
	}

	protected boolean hasMobTypes() {
		return this.mobtypes.size()>0;
	}
	
	@Override
	public void update() {
		if (this.world.isRemote) return;
		this.delay--;
		
		if(this.delay<=0 && activeMobs.size() < Math.min(maxActive, mobsLeft) && this.hasMobTypes()) { 
			
			if (this.world.getDifficulty() != Difficulty.PEACEFUL) {
				
				WeightedSpawnerEntity entdata = WeightedRandom.<WeightedSpawnerEntity>getRandomItem(this.rand, this.mobtypes);
				
				BlockPos blockpos = this.getPos();
				
	            double d0 = (double)blockpos.getX() + (rand.nextDouble() - rand.nextDouble()) * this.spawnrange + 0.5D;
	            double d1 = (double)(blockpos.getY() + 1+ this.spawnHeightOffset);
	            double d2 = (double)blockpos.getZ() + (rand.nextDouble() - rand.nextDouble()) * this.spawnrange + 0.5D;
	            Entity entity = AnvilChunkLoader.readWorldEntityPos(entdata.getNbt(), world, d0, d1, d2, false);
	            
	            if (entity !=null && entity instanceof ITGSpawnerNPC && entity instanceof MobEntity) {
	            	ITGSpawnerNPC npc = (ITGSpawnerNPC) entity;
	            	MobEntity elb = (MobEntity) entity;
	
		          //  if (net.minecraftforge.event.ForgeEventFactory.canEntitySpawnSpawner(npc, this.world, (float)entity.posX, (float)entity.posY, (float)entity.posZ))
		          //  {
		            	if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(elb, this.world, (float)entity.posX, (float)entity.posY, (float)entity.posZ, null)) {
		                    elb.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (ILivingEntityData)null);
		            
		                    if(elb instanceof CreatureEntity) {
		                    	((CreatureEntity)elb).setHomePosAndDistance(blockpos, 10);
		                    }
		                    
			                AnvilChunkLoader.spawnEntity(entity, world);
			                world.playEvent(2004, blockpos, 0);
			
			                if(!weaponOverride.isEmpty() && elb instanceof MobEntity) {
			                	((MobEntity) elb).setItemStackToSlot(EquipmentSlotType.MAINHAND, this.weaponOverride.copy());
			                }
			                
			                elb.spawnExplosionParticle();
			                
			                this.activeMobs.add(npc);
			            	this.delay= this.spawndelay;
			            	TGSpawnerNPCData dat = TGSpawnerNPCData.get(npc);
			            	dat.setSpawnerPos(blockpos);
		            	}
		            //}
	            	
	            } else {
	            	
	        		this.delay= this.spawndelay;
	            
	            }
			} else {
				this.delay=this.spawndelay;
			}
		} else {
			if(this.delay<=0) {
				this.delay= this.spawndelay;
			
        		Iterator<ITGSpawnerNPC> it = this.activeMobs.iterator();
    			while(it.hasNext()) {
    				ITGSpawnerNPC npc = it.next();
    				
    				if(npc instanceof LivingEntity) {
	    				LivingEntity ent = (LivingEntity) npc;
	    				
	    				if(!ent.isEntityAlive() || ent.world.provider.getDimension()!= this.world.provider.getDimension()) {
	    					it.remove();
	    				}
    				} else {
    					it.remove();
    				}
    			}
			}
		}
		
		
		if(this.mobsLeft<=0) {
			if(!this.world.isRemote) {
				this.world.setBlockToAir(this.getPos());
			}
		}

	}

	/*
	public void debug() {
		System.out.println("Left:"+this.mobsLeft);
		System.out.println("Active:"+this.activeMobs.size());
		System.out.println("MaxActive:"+this.maxActive);
		System.out.println("Delay:"+this.delay+"/"+this.spawndelay);
		System.out.println("Types:"+this.mobtypes.size());
	}*/
	
}
