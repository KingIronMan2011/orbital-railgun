package io.github.kingironman2011.orbital_railgun_enhanced.impl.v1206;

import io.github.kingironman2011.orbital_railgun_enhanced.item.IOrbitalRailgunItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class OrbitalRailgunItem extends Item implements GeoItem, IOrbitalRailgunItem {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);
    public final MutableObject<GeoRenderProvider> renderProviderHolder = new MutableObject<>();

    public OrbitalRailgunItem() {
        super(new Item.Settings().rarity(Rarity.EPIC).maxCount(1));
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug("[ITEM] OrbitalRailgunItem created");
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 24000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.getItemCooldownManager().isCoolingDown(this)) {
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.debug("[ITEM] Player {} started using orbital railgun", user.getName().getString());
            }
            return ItemUsage.consumeHeldItem(world, user, hand);
        }

        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug(
                    "[ITEM] Player {} tried to use orbital railgun while on cooldown",
                    user.getName().getString());
        }
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    public void shoot(PlayerEntity user) {
        int cooldownTicks = ServerConfig.INSTANCE.getCooldownTicks();
        user.getItemCooldownManager().set(this, cooldownTicks);
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.debug(
                    "[ITEM] Applied cooldown of {} ticks to player {}",
                    cooldownTicks,
                    user.getName().getString());
        }
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.renderProviderHolder.getValue());
    }

    @Override
    public Object getRenderProvider() {
        return this.renderProviderHolder.getValue();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return CACHE;
    }
}
