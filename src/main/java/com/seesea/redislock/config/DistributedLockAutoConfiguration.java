//package com.seesea.redislock.config;
//
//
//import com.seesea.redislock.aspect.DistributedLockAspectConfiguration;
//import com.seesea.redislock.lock.RedisDistributedLock;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// * @author xiechongyang
// * @description
// * @createTime 2019/4/28 下午 3:08
// * @since JDK1.8
// */
//public class DistributedLockAutoConfiguration {
//
////
////    @Autowired
////    private RedisTemplate redisTemplate;
////
////    @Bean
//////    public IDistributedLock redisDistributedLock(RedisTemplate<Object, Object> redisTemplate){
////    public IDistributedLock redisDistributedLock() {
////        return new RedisDistributedLock(redisTemplate);
////    }
////
////
////    @Bean
////    public RedisTemplate<String, Object> stringSerializerRedisTemplate() {
////        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
////        redisTemplate.setKeySerializer(stringSerializer);
////        redisTemplate.setValueSerializer(stringSerializer);
////        redisTemplate.setHashKeySerializer(stringSerializer);
////        redisTemplate.setHashValueSerializer(stringSerializer);
////        return redisTemplate;
////    }
//
//    public DistributedLockAspectConfiguration DistributedLock(){
//        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(stringSerializer);
//        redisTemplate.setValueSerializer(stringSerializer);
//        redisTemplate.setHashKeySerializer(stringSerializer);
//        redisTemplate.setHashValueSerializer(stringSerializer);
//        return new DistributedLockAspectConfiguration(new RedisDistributedLock(redisTemplate));
//    }
//
//}
