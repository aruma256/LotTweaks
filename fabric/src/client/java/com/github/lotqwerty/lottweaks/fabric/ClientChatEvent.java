package com.github.lotqwerty.lottweaks.fabric;

import java.util.ArrayList;

public class ClientChatEvent {

	private static final ArrayList<ClientChatEventListener> LISTENERS = new ArrayList<>();

	public static void registerListener(ClientChatEventListener listener) {
		LISTENERS.add(listener);
	}

	public static boolean post(ClientChatEvent event) {
		for (ClientChatEventListener listener: LISTENERS) {
			listener.onClientSendChat(event);
			if (event.canceled) {
				return true;
			}
		}
		return false;
	}

	private final String message; 
	private boolean canceled = false;

	public ClientChatEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean isCanceled() {
		return this.canceled;
	}
	
	public void setCanceled(boolean value) {
		this.canceled = value;
	}

	public static interface ClientChatEventListener {
		public void onClientSendChat(ClientChatEvent event);
	}

}
