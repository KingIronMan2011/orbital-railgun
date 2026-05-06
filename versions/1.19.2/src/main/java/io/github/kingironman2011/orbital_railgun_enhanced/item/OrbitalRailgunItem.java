package io.github.kingironman2011.orbital_railgun_enhanced.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.*;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class OrbitalRailgunItem extends Item implements IAnimatable {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private final AnimationFactory FACTORY = GeckoLibUtil.createFactory(this);

    public OrbitalRailgunItem() {
        super(new FabricItemSettings().group(net.minecraft.item.ItemGroup.COMBAT).rarity(Rarity.EPIC).maxCount(1));
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
    public void registerControllers(AnimationData animationData) {
    }

    @Override
    public AnimationFactory getFactory() {
        return FACTORY;
    }
}
