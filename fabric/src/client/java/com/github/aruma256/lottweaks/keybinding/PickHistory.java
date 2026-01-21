package com.github.aruma256.lottweaks.keybinding;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PickHistory {

	static final int HISTORY_SIZE = 10;

	private final Deque<ItemStack> history = new LinkedList<>();

	public PickHistory() {
	}

	public void add(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		Deque<ItemStack> tmpHistory = new LinkedList<>();
		tmpHistory.addAll(history);
		history.clear();
		while (!tmpHistory.isEmpty()) {
			if (!ItemStack.matches(tmpHistory.peekFirst(), itemStack)) {
				history.add(tmpHistory.pollFirst());
			} else {
				tmpHistory.removeFirst();
			}
		}
		while (history.size() >= HISTORY_SIZE) {
			history.pollLast();
		}
		history.addFirst(itemStack);
	}

	public boolean isEmpty() {
		return history.isEmpty();
	}

	public List<ItemStack> getAll() {
		return List.copyOf(history);
	}

}
