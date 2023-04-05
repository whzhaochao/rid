package com.zhaochao.id.test;

import com.zhaochao.id.Application;
import com.zhaochao.id.RID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class SpringBootApplicationTests {

    @Test
    public void testId() {
        Set<Long> ids = new HashSet<>();
        int size = 1000000;
        long startTime=System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            ids.add(RID.generateId());
        }
        System.out.println(String.format("generate %d ids spend  %s ms",size,System.currentTimeMillis()-startTime));
        Assert.assertEquals(size, ids.size());
    }

}
