package techguns.gui.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import techguns.gui.widgets.SlotFabricator;
import techguns.gui.widgets.SlotItemHandlerOutput;
import techguns.gui.widgets.SlotMachineInputBG;
import techguns.gui.widgets.SlotMachineUpgrade;
import techguns.gui.widgets.SlotTG;
import techguns.tileentities.BlastFurnaceTileEnt;
import techguns.tileentities.operation.ItemStackHandlerPlus;

public class BlastFurnaceContainer extends BasicMachineContainer {

	protected BlastFurnaceTileEnt tile;
	
	public static final int SLOT_INPUT1_X=19;
	public static final int SLOT_INPUT2_X=47;
	
	public static final int SLOTS_ROW1_Y=17;
	
	public static final int SLOT_OUTPUT_X=116;
	public static final int SLOT_OUTPUT_Y=50;
	
	public static final int SLOT_UPGRADE_X=150;
	public static final int SLOT_UPGRADE_Y=50;
	
	public BlastFurnaceContainer(PlayerInventory player, BlastFurnaceTileEnt ent) {
		super(player, ent);
		
		this.tile=ent;
		
		IItemHandler inventory = ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.SOUTH);
		
		if (inventory instanceof ItemStackHandlerPlus) {
			ItemStackHandlerPlus handler = (ItemStackHandlerPlus) inventory;
	
			this.addSlotToContainer(new SlotMachineInputBG(handler, BlastFurnaceTileEnt.SLOT_INPUT1, SLOT_INPUT1_X, SLOTS_ROW1_Y, SlotTG.INGOTSLOT_TEX));
			this.addSlotToContainer(new SlotMachineInputBG(handler, BlastFurnaceTileEnt.SLOT_INPUT2, SLOT_INPUT2_X, SLOTS_ROW1_Y, SlotFabricator.FABRICATOR_SLOTTEX_POWDER));
			
			this.addSlotToContainer(new SlotItemHandlerOutput(inventory, BlastFurnaceTileEnt.SLOT_OUTPUT, SLOT_OUTPUT_X, SLOT_OUTPUT_Y));
			this.addSlotToContainer(new SlotMachineUpgrade(handler,  BlastFurnaceTileEnt.SLOT_UPGRADE, SLOT_UPGRADE_X, SLOT_UPGRADE_Y));
		}
		
		this.addPlayerInventorySlots(player);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity ply, int id) {
		int MAXSLOTS = BlastFurnaceTileEnt.SLOT_UPGRADE+36+1; //36 = inventory size
		
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = (Slot) this.inventorySlots.get(id);

			if(slot.getHasStack()){
				ItemStack stack1 = slot.getStack();
				stack=stack1.copy();
				if (!stack.isEmpty()){
					
					if (id >=0 && id<=BlastFurnaceTileEnt.SLOT_UPGRADE){
						if (!this.mergeItemStack(stack1, BlastFurnaceTileEnt.SLOT_UPGRADE+1, MAXSLOTS, false)) {
							return ItemStack.EMPTY;
						}
						slot.onSlotChange(stack1, stack);
					} else if (id >BlastFurnaceTileEnt.SLOT_UPGRADE && id <MAXSLOTS){
						
						int validslot = tile.getValidSlotForItemInMachine(stack1);
						//System.out.println("put it in slot"+validslot);
						if (validslot >=0){
							
							if(!this.mergeItemStack(stack1, validslot, validslot+1, false)){
								return ItemStack.EMPTY;
							}
							slot.onSlotChange(stack1, stack);
							
						} else {
							return ItemStack.EMPTY;
						}
						
						
					}

					if (stack1.getCount() == 0) {
						slot.putStack(ItemStack.EMPTY);
					} else {
						slot.onSlotChanged();
					}

					if (stack1.getCount() == stack.getCount()) {
						return ItemStack.EMPTY;
					}

					slot.onTake(ply, stack1);
				}
			}
		
			return stack;
	}

}
