package cn.blockforge.generated.minecraft1211nb06d.skill;

public enum SkillId {
    BURST(0, "burst"),
    GRAB(1, "grab"),
    FLY(2, "fly"),
    DASH(3, "dash");

    public static final SkillId[] VALUES = values();

    public final int bit;
    public final String key;

    SkillId(int bit, String key) {
        this.bit = bit;
        this.key = key;
    }

    public int mask() {
        return 1 << bit;
    }
}
