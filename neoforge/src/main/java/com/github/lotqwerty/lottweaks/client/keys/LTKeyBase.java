package com.github.lotqwerty.lottweaks.client.keys;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class LTKeyBase extends KeyMapping {

	protected int pressTime = 0;
	protected int doubleTapTick = 0;
	private static final int DOUBLE_TAP_MAX = 5;
	
	public LTKeyBase(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}

	
	protected void handleClientTick(final TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			if (this.isDown()) {
				this.pressTime = Math.min(12345, this.pressTime + 1);
				if (this.pressTime == 1) {
					this.onKeyPressStart();
					this.doubleTapTick = DOUBLE_TAP_MAX;
				}
				whilePressed();
			} else {
				if (this.pressTime > 0) {
					this.onKeyReleased();
					this.pressTime = 0;
				}
				if (this.doubleTapTick > 0) {
					this.doubleTapTick--;
				}
			}
		}
	}

	protected void onKeyPressStart() {
	}
	
	protected void whilePressed() {
	}

	protected void onKeyReleased() {
	}

}