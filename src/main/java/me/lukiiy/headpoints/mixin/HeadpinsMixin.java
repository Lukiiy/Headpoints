package me.lukiiy.headpoints.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(LocatorBarRenderer.class)
public class HeadpinsMixin {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(
            method = "lambda$extractRenderState$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"
            )
    )
    private void headpoints$pins(GuiGraphicsExtractor instance, RenderPipeline tile, Identifier nineSlice, int i, int renderPipeline, int location, int x, int y, Operation<Void> original, @Local(argsOnly = true) TrackedWaypoint waypoint) {
        UUID id = waypoint.id().left().orElse(null);

        PlayerSkin skin = null;
        boolean hat = true;
        boolean upsideDown = false;

        if (id != null && minecraft.player != null) {
            PlayerInfo info = minecraft.player.connection.getPlayerInfo(id);

            if (info != null) {
                skin = info.getSkin();
                hat = info.showHat();
            }
        }

        if (skin == null || hat) {
            Entity e = minecraft.level != null && id != null ? minecraft.level.getEntity(id) : null;

            if (e instanceof Player player) {
                upsideDown = AvatarRenderer.isPlayerUpsideDown(player);
            } else if (skin == null && e instanceof ClientAvatarEntity clientAvatar) {
                skin = clientAvatar.getSkin();

                if (clientAvatar instanceof Avatar avatar) hat = avatar.isModelPartShown(PlayerModelPart.HAT);
            }
        }

        if (skin == null) {
            original.call(instance, tile, nineSlice, i, renderPipeline, location, x, y);
            return;
        }

        float dist = minecraft.getCameraEntity() != null ? Mth.sqrt((float) waypoint.distanceSquared(minecraft.getCameraEntity())) : 10; // TODO
        int scale = Mth.ceil(8 * headpoints$scale(dist, minecraft.getWaypointStyles().get(waypoint.icon().style)));
        int renderX = (i + 4) - scale / 2;
        int renderY = (renderPipeline + 4) - scale / 2;

        PlayerFaceExtractor.extractRenderState(instance, skin.body().texturePath(), renderX, renderY, scale, hat, upsideDown, -1);
    }

    @Unique
    private static float headpoints$scale(float dist, WaypointStyle style) {
        float near = style.nearDistance();
        float far = style.farDistance();

        if (dist <= near) return 1;
        if (dist >= far) return .5f;

        float normalized = (dist - near) / (far - near);

        return Mth.lerp(normalized, 1, .5f);
    }
}
