// WARNING: This file is autogenerated. DO NOT EDIT!
// Generated Tue Feb 24 09:44:06 +1000 2009
package com.kenai.constantine.platform.linux;
public enum TCP implements com.kenai.constantine.Constant {
TCP_MSS(512),
// TCP_MAX_SACK not defined
// TCP_MINMSS not defined
// TCP_MINMSSOVERLOAD not defined
TCP_MAXWIN(65535),
TCP_MAX_WINSHIFT(14),
// TCP_MAXBURST not defined
// TCP_MAXHLEN not defined
// TCP_MAXOLEN not defined
TCP_NODELAY(1),
TCP_MAXSEG(2);
// TCP_NOPUSH not defined
// TCP_NOOPT not defined
// TCP_KEEPALIVE not defined
// TCP_NSTATES not defined
// TCP_RETRANSHZ not defined
private final int value;
private TCP(int value) { this.value = value; }
public static final long MIN_VALUE = 1;
public static final long MAX_VALUE = 65535;

public final int value() { return value; }
}
