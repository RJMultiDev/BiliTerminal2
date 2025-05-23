package com.huanli233.biliterminal2.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserInfo implements Serializable, Parcelable {
    public long mid;
    public String name;
    public String avatar;
    public String sign;
    public int fans;
    public int level;
    public int following;
    public boolean followed;
    public String notice;

    public int official;
    public String officialDesc;
    public long mtime;

    public int vip_role = 0;
    public String vipNicknameColor = "";

    public long currentExp = 0;
    public long nextExp = 0;

    public String medalName = "";
    public int medalLevel = 0;

    public String sys_notice = "";

    public LiveRoom live_room = null;

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, String sys_notice, LiveRoom live_room) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.sys_notice = sys_notice;
        this.live_room = live_room;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, long currentExp, long nextExp) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.currentExp = currentExp;
        this.nextExp = nextExp;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, int vip_role, String sys_notice, LiveRoom live_room) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.vip_role = vip_role;
        this.sys_notice = sys_notice;
        this.live_room = live_room;
    }

    public UserInfo(long mid, String name, String avatar, String sign, int fans, int following, int level, boolean followed, String notice, int official, String officialDesc, long mtime) {
        this.mid = mid;
        this.name = name;
        this.avatar = avatar;
        this.sign = sign;
        this.fans = fans;
        this.level = level;
        this.following = following;
        this.followed = followed;
        this.notice = notice;
        this.official = official;
        this.officialDesc = officialDesc;
        this.mtime = mtime;
    }

    public UserInfo() {
    }

    protected UserInfo(Parcel in) {
        mid = in.readLong();
        name = in.readString();
        avatar = in.readString();
        sign = in.readString();
        fans = in.readInt();
        level = in.readInt();
        followed = in.readInt() != 0;
        notice = in.readString();
        official = in.readInt();
        officialDesc = in.readString();
        mtime = in.readLong();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mid);
        parcel.writeString(name);
        parcel.writeString(avatar);
        parcel.writeString(sign);
        parcel.writeInt(fans);
        parcel.writeInt(level);
        parcel.writeInt((followed ? 1 : 0));
        parcel.writeString(notice);
        parcel.writeInt(official);
        parcel.writeString(officialDesc);
        parcel.writeLong(mtime);
    }
}
