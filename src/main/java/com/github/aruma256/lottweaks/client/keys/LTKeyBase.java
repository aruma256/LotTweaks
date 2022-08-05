package com.github.aruma256.lottweaks.client.keys;

import static com.github.aruma256.lottweaks.client.ClientUtil.getClient;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LTKeyBase extends KeyBinding {

	protected int pressTime = 0;
	private int mode = 0;
	private int modeSwitchTick = 0;
	private static final int MODE_SWITCH_TICKS_THRESHOLD = 5;
	
	public LTKeyBase(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}

	protected int getMode() {
		return this.mode - 1;
	}

	@SubscribeEvent
	public void onKeyInput(final KeyInputEvent event) {
		//Mark this key as handled.
	}

	@SubscribeEvent
	public void onClientTick(final TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.getPhase() == EventPriority.NORMAL) {
			if (this.isKeyDown()) {
				this.pressTime = Math.min(12345, this.pressTime + 1);
				if (this.pressTime == 1) {
					this.mode++;
					this.onKeyPressStart();
					this.modeSwitchTick = MODE_SWITCH_TICKS_THRESHOLD;
				}
				whilePressed();
			} else {
				if (this.pressTime > 0) {
					this.onKeyReleased();
					this.pressTime = 0;
				}
				if (this.modeSwitchTick > 0) {
					this.modeSwitchTick--;
					if (this.modeSwitchTick == 0) {
						this.mode = 0;
					}
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

	protected boolean isPlayerCreative() {
		return getClient().player.isCreative();
	}

}