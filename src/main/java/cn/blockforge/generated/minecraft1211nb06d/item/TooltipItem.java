package cn.blockforge.generated.minecraft1211nb06d.item;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** 带说明行（来自 lang 的 tooltip.&lt;modid&gt;.&lt;key&gt;）的普通物品。 */
public class TooltipItem extends Item {
    private final String tooltipKey;

    public TooltipItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + GeneratedMod.MOD_ID + "." + tooltipKey)
                .withStyle(ChatFormatting.GRAY));
    }
}
