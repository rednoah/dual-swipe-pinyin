package ntu.csie.pinyin.model;

import static ntu.csie.pinyin.model.Final.*;

public enum Initial {


    A("ㄚ", a, i, o, n, ng),
    O("ㄛ", o, u),
    E("ㄜ", e, r, n),
    Y("ㄧ", i, a, ao, e, ou, an, in, ang, ing, ong, u, ue, uan, un),
    W("ㄨ", u, a, o, ai, ei, an, en, ang, eng),
    B("ㄅ", a, o, ai, ei, ao, an, en, ang, eng, i, iao, ie, ian, in, ing, u),
    P("ㄆ", a, o, ai, ei, ao, ou, an, en, ang, eng, i, iao, ie, ian, in, ing, u),
    M("ㄇ", a, o, e, ai, ei, ao, ou, an, en, ang, eng, i, iao, ie, iu, ian, in, ing, u),
    F("ㄈ", a, o, ei, ou, an, en, ang, eng, u),
    D("ㄉ", a, e, ai, ei, ao, ou, an, en, ang, eng, ong, i, iao, ie, iu, ian, ing, u, uo, ui, uan, un),
    T("ㄊ", a, e, ai, ao, ou, an, ang, eng, ong, i, iao, ie, ian, ing, u, uo, ui, uan, un),
    N("ㄋ", a, e, ai, ei, ao, an, en, ang, eng, ong, i, iao, ie, iu, ian, in, iang, ing, u, uo, uan, ü, üe),
    L("ㄌ", a, e, ai, ei, ao, ou, an, ang, eng, ong, i, iao, ie, iu, ian, in, iang, ing, u, uo, uan, un, ü, üe), // ignore 倆 lia
    G("ㄍ", a, e, ai, ei, ao, ou, an, en, ang, eng, ong, u, ua, uo, uai, ui, uan, un, uang),
    K("ㄎ", a, e, ai, ao, ou, an, en, ang, eng, ong, u, ua, uo, uai, ui, uan, un, uang),
    H("ㄏ", a, e, ai, ei, ao, ou, an, en, ang, eng, ong, u, ua, uo, uai, ui, uan, un, uang),
    Z("ㄗ", a, e, i, ai, ei, ao, ou, an, en, ang, eng, ong, u, uo, ui, uan, un),
    C("ㄘ", a, e, i, ai, ao, ou, an, en, ang, eng, ong, u, uo, ui, uan, un),
    S("ㄙ", a, e, i, ai, ao, ou, an, en, ang, eng, ong, u, uo, ui, uan, un),
    ZH("ㄓ", a, e, i, ai, ao, ou, an, en, ang, eng, ong, u, ua, uo, uai, ui, uan, un, uang),
    CH("ㄔ", a, e, i, ai, ao, ou, an, en, ang, eng, ong, u, uo, uai, ui, uan, un, uang),
    SH("ㄕ", a, e, i, ai, ei, ao, ou, an, en, ang, eng, u, ua, uo, uai, ui, uan, un, uang),
    R("ㄖ", e, i, ao, ou, an, en, ang, eng, ong, u, uo, ui, uan, un),
    J("ㄐ", i, ia, iao, ie, iu, ian, in, iang, ing, iong, u, ue, uan, un),
    Q("ㄑ", i, ia, iao, ie, iu, ian, in, iang, ing, iong, u, ue, uan, un),
    X("ㄒ", i, ia, iao, ie, iu, ian, in, iang, ing, iong, u, ue, uan, un);


    private final String zhuyin;


    private final Final[] finals;


    Initial(String zhuyin, Final... finals) {
        this.zhuyin = zhuyin;
        this.finals = finals;
    }


    public String getPinyin() {
        return name().toLowerCase();
    }


    public String getZhuyin() {
        return zhuyin;
    }


    public Final[] getFinals() {
        return finals.clone();
    }


    public static Initial[][] getPhoneticGroups() {
        return new Initial[][]{
                {B, P, M, F},
                {D, T, N, L},
                {G, K, H},
                {J, Q, X},
                {A, Y, E, O},
                {Z, C, S, W},
                {ZH, CH, SH, R}
        };
    }


    public static Initial[][] getQwertyGroups() {
        return new Initial[][]{
                {Q, W, E, R, T, Y, O, P},
                {A, S, SH, D, F, G, H, J, K, L},
                {Z, ZH, X, C, CH, B, N, M}
        };
    }


}