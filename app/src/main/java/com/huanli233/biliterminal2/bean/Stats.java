package com.huanli233.biliterminal2.bean;

import java.io.Serializable;

public class Stats implements Serializable {
    public int view;
    public int like;
    public int reply;
    public int coin;
    public int share;
    public int danmaku;
    public int favorite;

    public boolean liked;
    public boolean favoured;
    public int coined;

    public boolean like_disabled;
    public boolean coin_disabled;
    public boolean reply_disabled;
    public boolean share_disabled;
    public int allow_coin;
}
