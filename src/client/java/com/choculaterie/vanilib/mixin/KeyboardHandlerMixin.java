package com.choculaterie.vanilib.mixin;

import com.choculaterie.vanilib.gui.widget.CustomTextField;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void vanilib$routeToCustomTextField(long window, CharacterEvent event, CallbackInfo ci) {
		if (CustomTextField.routeCharTyped(event.codepoint())) {
			ci.cancel();
		}
	}
}
