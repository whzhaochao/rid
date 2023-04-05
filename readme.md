
# 容器化部署雪花算法遇到的问题

-  容器化无状态部署机器ID不可获取

    以前项目使用物理机器部署时，我们可以根据机器的IP分配对就的机器id,可是现在都是使用容器化部署，一般都是部署成无状态模式，所以以前根据机器IP分配机器ID就不可用了
-  一个容器一般只部署一个服务，所有服务id也不需要了
     


# 解决思路

- 将机器id和服务id合并
- 项目启用时通过redis获取一个workId



# RID的诞生

## 基本思路
   -  为每个微服务配置一个rid.redisKey,当作该服务在redis中的唯一标识服务
   -  在项目启用时，将rid.redisKey自增，获取到一个workId
   -  将wordId与maxWorkerId取余运算，得到单个服务的唯一workId


## 测试代码

```
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
```


## 优点
- 无需配置机器ID,服务ID,可以用微服务的applicationId当作rid.redisKey,可无状态化部署
- 单个微服务容器最大节点数量及生成速度可配置
- ID大部分情况是自增了

## 存在问题
- ID只能做到单个容器唯一，不可做到全局唯一
- 服务器时间回拨，可能导致ID重复
- ID不是严格全局自增,同一毫秒内rid.redisKey达到maxWorkerId后从0开始，可能导致生成ID比其它服务生成的小