package com.zhaochao.id;

import org.springframework.stereotype.Component;

@Component
public class RID {

    public static Long generateId() {
        return ID.getInstance().id();
    }
}
