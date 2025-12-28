package me.lukiiy.headpoints.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocatorBarRenderer.class)
public class HeadpinsMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void headpoints$pins(GuiGraphics instance, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, Entity entity, Level level, PartialTickSupplier pts, GuiGraphics instance2, int o, TrackedWaypoint t) {
        Player player = t.id().left().map(level::getPlayerByUUID).orElse(null);

        if (player == null) {
            instance.blitSprite(renderPipeline, resourceLocation, i, j, k, l, m);
            return;
        }

        instance.drawString(minecraft.font, Component.object(new PlayerSprite(ResolvableProfile.createResolved(player.getGameProfile()), true)), i, j + 1, 0xFFFFFFFF, true);
    }
}
