package com.kawasaki.lb.impl;

import com.kawasaki.dto.RpcReq;
import com.kawasaki.lb.LoadBalance;

import java.util.List;

public class RoundRobinLoadBalance implements LoadBalance {
    private int last = -1;

    @Override
    public String select(List<String> list, RpcReq rpcReq) {
        last++;
        last = last % list.size();

        return list.get(last);
    }
}
