# redislock
redis分布式锁


    
    
    1 扫描包
    @ComponentScan({"com.seesea.*"})
   
    
    2 注入
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Bean
    public DistributedLockAspectConfiguration getDistributedLockAspectConfiguration(){
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return new DistributedLockAspectConfiguration(new RedisDistributedLock(redisTemplate));
    }
    
    
    3 使用
    //#demo.key.concat(#demo.value)
    @DistributeLock(name = "HAHA",value = "#a.key.concat(#a.value)",keepMills = 150000)
    public String test1(String a) throws InterruptedException {
 
        return a;
    }
