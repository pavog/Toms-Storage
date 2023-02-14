package com.tom.storagemod.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.LevelEmitterBlockEntity;

public class LevelEmitterMenu extends AbstractFilteredMenu {
	private final Container inv;
	private LevelEmitterBlockEntity te;

	public LevelEmitterMenu(int wid, Inventory pinv) {
		this(wid, pinv, null);
	}

	public LevelEmitterMenu(int wid, Inventory pinv, LevelEmitterBlockEntity te) {
		super(Content.levelEmitterConatiner.get(), wid, pinv);
		this.inv = te == null ? new SimpleContainer(1) : new Container() {

			@Override
			public void clearContent() {
			}

			@Override
			public void setItem(int index, ItemStack stack) {
				te.setFilter(stack);
			}

			@Override
			public ItemStack removeItemNoUpdate(int index) {
				return ItemStack.EMPTY;
			}

			@Override
			public void setChanged() {
			}

			@Override
			public boolean stillValid(Player player) {
				return false;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public ItemStack getItem(int index) {
				return te.getFilter();
			}

			@Override
			public int getContainerSize() {
				return 1;
			}

			@Override
			public ItemStack removeItem(int index, int count) {
				return ItemStack.EMPTY;
			}
		};
		inv.startOpen(pinv.player);
		this.te = te;

		this.addSlot(new PhantomSlot(inv, 0, 43, 38));

		for(int k = 0; k < 3; ++k) {
			for(int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(pinv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(pinv, l, 8 + l * 18, 142));
		}

	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean stillValid(Player playerIn) {
		return te != null ? te.stillValid(playerIn) : true;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			if (index < 1) {
			} else {
				ItemStack is = slot.getItem().copy();
				is.setCount(1);
				Slot sl = this.slots.get(0);
				if(!ItemStack.isSame(sl.getItem(), is)) {
					if(sl.getItem().isEmpty()) {
						sl.set(is);
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		this.inv.stopOpen(playerIn);
	}

	private int lastCount = 0;
	private boolean lessThan = false;

	@Override
	public void receive(CompoundTag tag) {
		if(pinv.player.isSpectator() || te == null)return;
		super.receive(tag);
		if(tag.contains("count")) {
			int count = tag.getInt("count");
			boolean lt = tag.getBoolean("lessThan");
			te.setCount(count);
			te.setLessThan(lt);
		}
	}

	@Override
	public void broadcastChanges() {
		if(te == null)return;
		if(lastCount != te.getCount() || lessThan != te.isLessThan()) {
			CompoundTag mainTag = new CompoundTag();
			mainTag.putInt("count", te.getCount());
			mainTag.putBoolean("lessThan", te.isLessThan());
			lastCount = te.getCount();
			lessThan = te.isLessThan();
			NetworkHandler.sendTo((ServerPlayer) pinv.player, mainTag);
		}
		super.broadcastChanges();
	}
}
