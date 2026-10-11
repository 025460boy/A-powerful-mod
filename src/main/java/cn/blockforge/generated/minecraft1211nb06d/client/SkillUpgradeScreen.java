package cn.blockforge.generated.minecraft1211nb06d.client;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.item.ModItems;
import cn.blockforge.generated.minecraft1211nb06d.network.SkillUpgradePayload;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillData;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillId;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUnlocks;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUpgrades;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** 通过 owo-ui 构建的技能升级页面。 */
public final class SkillUpgradeScreen extends BaseOwoScreen<FlowLayout> {
    public static int syncRevision;

    private FlowLayout panel;
    private int seenRevision = -1;
    private int refreshTicks;

    public SkillUpgradeScreen() {
        super(Component.translatable("screen." + GeneratedMod.MOD_ID + ".title"));
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        panel = Containers.verticalFlow(Sizing.fixed(380), Sizing.content());
        panel.surface(Surface.DARK_PANEL);
        panel.padding(Insets.of(10));
        panel.gap(5);
        root.child(panel);
        rebuildContents();
    }

    private void rebuildContents() {
        if (panel == null) return;
        LocalPlayer player = Minecraft.getInstance().player;
        panel.clearChildren();
        panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".title"))
                .shadow(true).sizing(Sizing.fill(), Sizing.content()));
        panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".subtitle"))
                .maxWidth(350).sizing(Sizing.fill(), Sizing.content()));
        for (SkillId id : SkillId.VALUES) {
            addSkill(id, player);
        }
        panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".close_hint"))
                .sizing(Sizing.fill(), Sizing.content()));
        seenRevision = syncRevision;
    }

    private void addSkill(SkillId id, LocalPlayer player) {
        SkillData data = player == null ? new SkillData() : SkillUnlocks.data(player);
        ItemStack icon = new ItemStack(switch (id) {
            case BURST -> ModItems.BURST_POTION.get();
            case GRAB -> ModItems.BIND_POTION.get();
            case FLY -> ModItems.FLY_POTION.get();
            case DASH -> ModItems.DASH_POTION.get();
        });
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(), Sizing.content()).gap(6);
        row.child(Components.item(icon).showOverlay(false).sizing(Sizing.fixed(22), Sizing.fixed(22)));
        FlowLayout details = Containers.verticalFlow(Sizing.fill(), Sizing.content()).gap(2);
        details.child(Components.label(SkillUpgrades.skillName(id)).shadow(true)
                .sizing(Sizing.fill(), Sizing.content()));

        boolean unlocked = data.has(id);
        int level = unlocked ? data.level(id) : 0;
        int max = SkillUpgrades.maxLevel(id);
        Component levelText = unlocked
                ? Component.translatable("screen." + GeneratedMod.MOD_ID + ".level", level, max)
                : Component.translatable("screen." + GeneratedMod.MOD_ID + ".locked");
        details.child(Components.label(levelText).maxWidth(320).sizing(Sizing.fill(), Sizing.content()));
        details.child(Components.label(effectText(id, level, unlocked)).maxWidth(320)
                .sizing(Sizing.fill(), Sizing.content()));
        row.child(details);
        panel.child(row);

        if (id == SkillId.DASH) {
            panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".no_upgrade"))
                    .maxWidth(350).sizing(Sizing.fill(), Sizing.content()));
            panel.child(Components.spacer(3));
            return;
        }
        if (!unlocked) {
            panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".unlock_first"))
                    .maxWidth(350).sizing(Sizing.fill(), Sizing.content()));
            panel.child(Components.spacer(3));
            return;
        }
        if (level >= max) {
            panel.child(Components.label(Component.translatable("screen." + GeneratedMod.MOD_ID + ".max_level"))
                    .sizing(Sizing.fill(), Sizing.content()));
            panel.child(Components.spacer(3));
            return;
        }

        int targetLevel = level + 1;
        int needed = SkillUpgrades.costCount(id, targetLevel);
        var material = SkillUpgrades.costItem(id);
        int owned = player == null ? 0 : SkillUpgrades.count(player.getInventory(), material);
        Component buttonText = Component.translatable("screen." + GeneratedMod.MOD_ID + ".upgrade_button",
                needed, material.getDescription());
        ButtonComponent button = Components.button(buttonText, b ->
                PacketDistributor.sendToServer(new SkillUpgradePayload((byte) id.ordinal())));
        button.active(owned >= needed);
        // owo 的 ButtonComponent 继承原版 Button，放进布局需要包一层 vanilla widget 容器
        io.wispforest.owo.ui.core.Component upgradeButton = Components.wrapVanillaWidget(button);
        upgradeButton.sizing(Sizing.fill(), Sizing.fixed(20));
        panel.child(upgradeButton);
        panel.child(Components.spacer(3));
    }

    private Component effectText(SkillId id, int level, boolean unlocked) {
        if (!unlocked) {
            return Component.translatable("screen." + GeneratedMod.MOD_ID + ".effect.locked");
        }
        return switch (id) {
            case BURST -> Component.translatable("screen." + GeneratedMod.MOD_ID + ".effect.burst",
                    SkillUpgrades.burstDamage(level), Component.translatable("screen." + GeneratedMod.MOD_ID
                            + (SkillUpgrades.burstExplodes(level) ? ".yes" : ".no")));
            case GRAB -> Component.translatable("screen." + GeneratedMod.MOD_ID + ".effect.grab",
                    durationText(SkillUpgrades.grabDurationTicks(level)));
            case FLY -> Component.translatable("screen." + GeneratedMod.MOD_ID + ".effect.fly",
                    durationText(SkillUpgrades.flyDurationTicks(level)),
                    Component.translatable("screen." + GeneratedMod.MOD_ID
                            + (SkillUpgrades.flyBoostUnlocked(level) ? ".yes" : ".no")));
            case DASH -> Component.translatable("screen." + GeneratedMod.MOD_ID + ".effect.dash");
        };
    }

    private Component durationText(int ticks) {
        return ticks < 0
                ? Component.translatable("screen." + GeneratedMod.MOD_ID + ".permanent")
                : Component.translatable("screen." + GeneratedMod.MOD_ID + ".seconds", ticks / 20);
    }

    @Override
    public void tick() {
        super.tick();
        refreshTicks++;
        if (syncRevision != seenRevision || refreshTicks >= 20) {
            refreshTicks = 0;
            rebuildContents();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientSkillHandler.KEY_UPGRADE.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
