package com.neutronmc.mod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class Main implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MinecraftClient mc = MinecraftClient.getInstance();
	}
}