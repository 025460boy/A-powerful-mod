package cn.blockforge.generated.minecraft1211nb06d.skill;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, GeneratedMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillData>> SKILLS =
            ATTACHMENT_TYPES.register("skills", () -> AttachmentType.builder(SkillData::new)
                    .serialize(SkillData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
