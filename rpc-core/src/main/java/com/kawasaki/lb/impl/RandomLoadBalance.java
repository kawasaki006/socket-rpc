package com.kawasaki.lb.impl;

import cn.hutool.core.util.RandomUtil;
import com.kawasaki.lb.LoadBalance;

import java.util.List;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public String select(List<String> list) {
        return RandomUtil.randomEle(list);
    }
}
