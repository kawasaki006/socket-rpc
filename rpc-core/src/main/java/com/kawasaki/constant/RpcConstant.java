package com.kawasaki.constant;

public class RpcConstant {
    public static final int SERVER_PORT = 8888;

    public static final String ZK_IP = "192.168.56.10";
    public static final int ZK_PORT = 2181;
    public static final String ZK_RPC_ROOT_PATH = "/kawasaki-rpc";

    public static final String NETTY_RPC_KEY = "RpcResp";
    public static final byte[] RPC_MAGIC_CODE = new byte[] {(byte) 'p', (byte) 'o', (byte) 'y', (byte) 'o'};
    public static final int REQ_HEAD_LEN = 16;
    public static final int REQ_MAX_LEN = 1024 * 1024 * 8;
}
