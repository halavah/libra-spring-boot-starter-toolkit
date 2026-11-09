# RedissonUtil API 参考手册

## 📚 目录

1.  [核心内部类](#1-核心内部类)
    -   [1.1. LoggerCore API](#11-loggercore-api)
    -   [1.2. RedisDataCore API](#12-redisdatacore-api)
    -   [1.3. LettuceCore API](#13-lettucecore-api)
    -   [1.4. RedissonCore API](#14-redissoncore-api)
2.  [链式访问器 (RedisAccessor)](#2-链式访问器-redisaccessor)
    -   [2.1. 核心 API](#21-核心-api)
    -   [2.2. 事务 API](#22-事务-api)
    -   [2.3. 操作 API](#23-操作-api)
3.  [TopApi1：顶层静态API - 基础数据类型](#3-topapi1顶层静态api---基础数据类型)
    -   [3.1. Key (键操作)](#31-key-键操作)
    -   [3.2. String (字符串)](#32-string-字符串)
    -   [3.3. Hash (哈希)](#33-hash-哈希)
    -   [3.4. List (列表)](#34-list-列表)
    -   [3.5. Set (集合)](#35-set-集合)
    -   [3.6. ZSet (有序集合)](#36-zset-有序集合)
4.  [TopApi2：顶层静态API - 高级数据结构](#4-topapi2顶层静态api---高级数据结构)
    -   [4.1. Bitmap (位图)](#41-bitmap-位图)
    -   [4.2. HyperLogLog](#42-hyperloglog)
    -   [4.3. Geospatial (地理空间)](#43-geospatial-地理空间)
    -   [4.4. BloomFilter (布隆过滤器)](#44-bloomfilter-布隆过滤器)
5.  [TopApi3：顶层静态API - 服务器与脚本](#5-topapi3顶层静态api---服务器与脚本)
    -   [5.1. Server (服务器管理)](#51-server-服务器管理)
    -   [5.2. Scripting (脚本)](#52-scripting-脚本)
    -   [5.3. Streams (流)](#53-streams-流)
    -   [5.4. Pub/Sub (发布/订阅)](#54-pubsub-发布订阅)
6.  [TopApi4：顶层静态API - 分布式锁与同步](#6-topapi4顶层静态api---分布式锁与同步)
    -   [6.1. ReentrantLock (可重入锁)](#61-reentrantlock-可重入锁)
    -   [6.2. FairLock (公平锁)](#62-fairlock-公平锁)
    -   [6.3. ReadWriteLock (读写锁)](#63-readwritelock-读写锁)
    -   [6.4. Semaphore (信号量)](#64-semaphore-信号量)
    -   [6.5. CountDownLatch (倒计时器)](#65-countdownlatch-倒计时器)

---

## 1. 核心内部类

### 1.1. LoggerCore API

| 方法签名                         | 描述                 |
|:---------------------------------|:---------------------|
| `void log(String level, String message)` | (静态) 内部日志记录方法 |

### 1.2. RedisDataCore API

#### 静态管理方法

| 方法签名                                                                                             | 描述                                       |
|:-----------------------------------------------------------------------------------------------------|:-------------------------------------------|
| `RedisDataCore getConfig(String key)`                                                                | 获取指定键的数据源配置对象                 |
| `String getDefaultKey()`                                                                             | 获取当前全局默认数据源的名称               |
| `void setDefaultKey(String key)`                                                                     | 设置全局默认的数据源                       |
| `RedisDataCore register(String key, ...)`                                                            | 注册一个新的数据源 (提供两个重载版本)      |

#### 实例链式方法

| 方法签名                             | 描述                                                         |
|:-------------------------------------|:-------------------------------------------------------------|
| `RedisAccessor setDefaultDataSource()` | 将当前数据源实例设为全局默认，并返回一个针对该源的访问器     |
| `RedisAccessor setThreadDataSourceContext()` | 将当前数据源实例设为线程上下文，并返回一个针对该源的访问器 |
| `RedisAccessor accessor()`           | 直接返回一个操作当前数据源实例的访问器                       |

### 1.3. LettuceCore API

| 方法签名                                                                 | 描述                                                         |
|:-------------------------------------------------------------------------|:-------------------------------------------------------------|
| `StatefulRedisConnection<String, String> getConnection()`                | (静态) 获取一个当前生效数据源的连接                          |
| `StatefulRedisConnection<String, String> getConnection(String key)`      | (静态) 获取一个指定数据源的连接                              |
| `void use(String key)`                                                   | **【推荐】** (静态) 在当前线程中临时切换数据源               |
| `void clear()`                                                           | **【重要】** (静态) 清除当前线程的数据源设置，恢复使用全局默认 |
| `String getEffectiveDbKey()`                                             | (静态) 获取当前操作实际生效的数据源名称                      |
| `void returnConnection(String key, StatefulRedisConnection<...> connection)` | (静态) 将连接归还到指定数据源的连接池                        |

### 1.4. RedissonCore API

| 方法签名                         | 描述                               |
|:---------------------------------|:-----------------------------------|
| `RedissonClient getClient()`     | (静态) 获取一个当前生效数据源的Redisson客户端 |
| `RedissonClient getClient(String key)` | (静态) 获取一个指定数据源的Redisson客户端   |

---

## 2. 链式访问器 (RedisAccessor)

`RedisAccessor` 是 `RedissonUtil` 的高级用法核心，它实现了 `AutoCloseable` 接口，推荐使用 `try-with-resources` 语句来确保资源的自动释放。

### 2.1. 核心 API

| 方法签名                      | 描述                                                               |
|:------------------------------|:-------------------------------------------------------------------|
| `RedisAccessor(String dbKey)` | (构造函数) 获取一个操作指定数据源的访问器                          |
| `RedisAccessor()`             | (构造函数) 获取一个操作默认数据源的访问器                          |
| `void close()`                | (自动调用) 关闭访问器，将连接归还连接池，并自动放弃未提交的事务    |

### 2.2. 事务 API

| 方法签名                   | 描述                                                                                 |
|:---------------------------|:-------------------------------------------------------------------------------------|
| `String watch(String... keys)` | (乐观锁) 监视一个或多个 key，如果在事务执行之前这些 key 被其他命令所改动，那么事务将被打断 |
| `String unwatch()`         | 取消 `WATCH` 命令对所有 key 的监视                                                     |
| `RedisAccessor multi()`    | 开启一个新事务 (MULTI)，并返回自身以支持链式调用                                     |
| `List<Object> exec()`      | 执行事务队列中的所有命令 (EXEC)，并返回所有命令的执行结果                            |
| `void discard()`           | 放弃事务，清空命令队列 (DISCARD)                                                     |

### 2.3. 操作 API

`RedisAccessor` 实例暴露了所有数据类型的 `Ops` 对象，其API与顶层静态API完全相同，但所有操作都将**复用同一个连接**。

| 属性      | 描述                                  |
|:----------|:--------------------------------------|
| `KeyOps`  | 获取用于键操作的 `KeyOps` 实例        |
| `StringOps` | 获取用于字符串操作的 `StringOps` 实例 |
| `HashOps` | 获取用于哈希操作的 `HashOps` 实例     |
| `ListOps` | 获取用于列表操作的 `ListOps` 实例     |
| `SetOps`  | 获取用于集合操作的 `SetOps` 实例      |
| `ZSetOps` | 获取用于有序集合操作的 `ZSetOps` 实例 |
| `BitmapOps` | 获取用于位图操作的 `BitmapOps` 实例 |
| `ReentrantLockOps` | 获取用于可重入锁操作的 `ReentrantLockOps` 实例 |
| `FairLockOps` | 获取用于公平锁操作的 `FairLockOps` 实例 |
| `ReadWriteLockOps` | 获取用于读写锁操作的 `ReadWriteLockOps` 实例 |
| `SemaphoreOps` | 获取用于信号量操作的 `SemaphoreOps` 实例 |
| `CountDownLatchOps` | 获取用于倒计时器操作的 `CountDownLatchOps` 实例 |
| `BloomFilterOps` | 获取用于布隆过滤器操作的 `BloomFilterOps` 实例 |
| `HyperLogLogOps` | 获取用于HyperLogLog操作的 `HyperLogLogOps` 实例 |
| `GeoOps` | 获取用于地理空间操作的 `GeoOps` 实例 |
| `StreamOps` | 获取用于流操作的 `StreamOps` 实例 |

---

## 3. TopApi1：顶层静态API - 基础数据类型

这些静态API为基础Redis数据类型提供了便捷的代理入口，内部通过 `try-with-resources` 自动管理 `RedisAccessor` 的生命周期。

### 3.1. Key (键操作)

| 方法签名                                      | 描述                               |
|:----------------------------------------------|:-----------------------------------|
| `Long unlink(String... keys)`                 | 异步删除一个或多个键 (非阻塞)      |
| `Long touch(String... keys)`                  | 更新一个或多个键的最后访问时间     |
| `Boolean copy(String source, String destination)` | (Redis 6.2+) 复制一个键到新键    |
| `Long delete(String... keys)`                 | 删除一个或多个键，返回被删除键的数量 |
| `Long delete(Collection<String> keys)`        | 批量删除键，返回被删除键的数量     |
| `byte[] dump(String key)`                     | 序列化给定 key 的值                |
| `Boolean hasKey(String key)`                  | 检查键是否存在                     |
| `Boolean expire(String key, long timeout, TimeUnit unit)` | 为键设置过期时间                   |
| `Boolean expireAt(String key, Date date)`     | 为键设置一个具体的过期时间点       |
| `Set<String> keys(String pattern)`            | 查找匹配的键 (生产环境慎用)        |
| `Boolean move(String key, int dbIndex)`       | 将当前数据库的 key 移动到给定的数据库 db 当中 |
| `Boolean persist(String key)`                 | 移除给定 key 的过期时间，使得 key 永不过期 |
| `Long getExpire(String key, TimeUnit unit)`   | 以指定时间单位返回 key 的剩余过期时间 |
| `Long getExpire(String key)`                  | 以秒为单位，返回 key 的剩余过期时间 |
| `String randomKey()`                          | 从当前数据库中随机返回一个 key     |
| `void rename(String oldKey, String newKey)`   | 修改 key 的名称                    |
| `Boolean renameIfAbsent(String oldKey, String newKey)` | 仅当 newkey 不存在时，将 key 改名为 newkey |
| `String type(String key)`                     | 获取键存储的值的数据类型           |

### 3.2. String (字符串)

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `String get(String key)`                                        | 获取值                             |
| `String getDel(String key)`                                     | (原子操作) 获取键的值并立即删除它  |
| `String getEx(String key, long timeout, TimeUnit unit)`         | (原子操作) 获取键的值并为其设置新的过期时间 |
| `String getRange(String key, long start, long end)`             | 获取 key 中字符串值的子字符串      |
| `String getAndSet(String key, String value)`                    | 将给定 key 的值设为 value ，并返回 key 的旧值 |
| `Boolean getBit(String key, long offset)`                       | 对 key 所储存的字符串值，获取指定偏移量上的位(bit) |
| `Map<String, String> multiGet(Collection<String> keys)`         | 获取所有(一个或多个)给定 key 的值  |
| `void set(String key, String value)`                            | 设置值                             |
| `boolean setBit(String key, long offset, boolean value)`        | 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit) |
| `void setEx(String key, String value, long timeout, TimeUnit unit)` | 设置带过期时间的值                 |
| `boolean setIfAbsent(String key, String value)`                 | 如果键不存在，则设置值 (`SETNX`)   |
| `void setRange(String key, String value, long offset)`          | 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始 |
| `void multiSet(Map<String, String> maps)`                       | 同时设置一个或多个 key-value 对    |
| `boolean multiSetIfAbsent(Map<String, String> maps)`            | 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在 |
| `Integer append(String key, String value)`                      | 如果 key 已经存在并且是一个字符串，将 value 追加到 key 原来的值的末尾 |
| `Long incrBy(String key, long increment)`                       | 将 key 所储存的值加上给定的增量值  |
| `Double incrByFloat(String key, double increment)`              | 将 key 所储存的值加上给定的浮点增量值 |
| `Long size(String key)`                                         | 返回 key 所储存的字符串值的长度    |

### 3.3. Hash (哈希)

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `String hGet(String key, String field)`                         | 获取哈希中指定字段的值             |
| `Map<String, String> hGetAll(String key)`                       | 获取哈希中所有的字段和值           |
| `Map<String, String> hMultiGet(String key, Collection<String> fields)` | 获取所有给定字段的值               |
| `void hPut(String key, String hashKey, String value)`           | 设置哈希中指定字段的值             |
| `void hPutAll(String key, Map<String, String> maps)`            | 同时将多个 field-value (域-值)对设置到哈希表 key 中 |
| `Boolean hPutIfAbsent(String key, String hashKey, String value)` | 只有在字段 field 不存在时，设置哈希表字段的值 |
| `Long hDelete(String key, String... fields)`                    | 删除一个或多个哈希表字段           |
| `boolean hExists(String key, String field)`                     | 查看哈希表的指定字段是否存在       |
| `Long hIncrBy(String key, String field, long increment)`        | 为哈希表 key 中的指定字段的整数值加上增量 increment |
| `Double hIncrByFloat(String key, String field, double delta)`   | 为哈希表 key 中的指定字段的浮点数值加上增量 delta |
| `Set<String> hKeys(String key)`                                 | 获取所有哈希表中的字段             |
| `Long hSize(String key)`                                        | 获取哈希表中字段的数量             |
| `List<String> hValues(String key)`                              | 获取哈希表中所有值                 |
| `String hRandField(String key)`                                 | 从哈希表中随机获取一个字段         |
| `List<String> hRandField(String key, long count)`               | 从哈希表中随机获取多个字段         |
| `MapScanCursor<String, String> hScan(String key, ScanArgs options)` | 迭代哈希表中的键值对             |

### 3.4. List (列表)

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `String lIndex(String key, long index)`                         | 通过索引获取列表中的元素           |
| `List<String> lRange(String key, long start, long end)`         | 获取列表指定范围内的元素           |
| `Long lLeftPush(String key, String value)`                      | 将一个值插入到列表头部             |
| `Long lLeftPushAll(String key, String... value)`                | 将一个或多个值插入到列表头部       |
| `Long lLeftPushAll(String key, Collection<String> value)`       | 将一个集合的值插入到列表头部       |
| `Long lLeftPushIfPresent(String key, String value)`             | 将一个值插入到已存在的列表头部     |
| `Long lLeftPush(String key, String pivot, String value)`        | 在列表的元素前插入元素             |
| `Long lRightPush(String key, String value)`                     | 在列表中添加一个值到末尾           |
| `Long lRightPushAll(String key, String... value)`               | 在列表中添加一个或多个值到末尾     |
| `Long lRightPushAll(String key, Collection<String> value)`      | 在列表中添加一个集合的值到末尾     |
| `Long lRightPushIfPresent(String key, String value)`            | 为已存在的列表添加一个值到末尾     |
| `Long lRightPush(String key, String pivot, String value)`       | 在列表的元素后插入元素             |
| `void lSet(String key, long index, String value)`               | 通过索引设置列表元素的值           |
| `String lLeftPop(String key)`                                   | 移出并获取列表的第一个元素         |
| `String lBLeftPop(String key, long timeout, TimeUnit unit)`     | 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止 |
| `String lRightPop(String key)`                                  | 移出并获取列表的最后一个元素       |
| `String lBRightPop(String key, long timeout, TimeUnit unit)`    | 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止 |
| `String lRightPopAndLeftPush(String sourceKey, String destinationKey)` | 从一个列表的右侧弹出一个元素，并将其插入到另一个列表的左侧 |
| `String lBRightPopAndLeftPush(String sourceKey, ...)`           | `RPOPLPUSH` 的阻塞版本             |
| `Long lRemove(String key, long count, String value)`            | 移除列表元素                       |
| `void lTrim(String key, long start, long end)`                  | 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除 |
| `Long lLen(String key)`                                         | 获取列表长度                       |

### 3.5. Set (集合)

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `Long sAdd(String key, String... values)`                       | 向集合添加一个或多个成员           |
| `Set<String> sMembers(String key)`                              | 获取集合的全部成员                 |
| `Long sSize(String key)`                                        | 获取集合的成员数                   |
| `Boolean sIsMember(String key, String value)`                   | 判断 member 元素是否是集合 key 的成员 |
| `String sRandomMember(String key)`                              | 随机返回集合中一个元素             |
| `List<String> sRandomMembers(String key, long count)`           | 随机返回集合中指定数量的元素（可能重复） |
| `Set<String> sDistinctRandomMembers(String key, long count)`    | 随机返回集合中指定数量的唯一元素   |
| `ValueScanCursor<String> sScan(String key, ScanArgs options)`   | 迭代集合中的元素                   |
| `Set<String> sIntersect(String key, String otherKey)`           | 返回给定两个集合的交集             |
| `Set<String> sIntersect(String key, Collection<String> otherKeys)` | 返回给定所有集合的交集             |
| `Long sIntersectAndStore(String key, String otherKey, String destKey)` | 返回给定两个集合的交集并存储在 `destKey` 中 |
| `Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey)` | 返回给定所有集合的交集并存储在 `destKey` 中 |
| `Set<String> sUnion(String key, String otherKey)`               | 返回给定两个集合的并集             |
| `Set<String> sUnion(String key, Collection<String> otherKeys)`  | 返回所有给定集合的并集             |
| `Long sUnionAndStore(String key, String otherKey, String destKey)` | 所有给定集合的并集存储在 `destKey` 集合中 |
| `Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey)` | 所有给定集合的并集存储在 `destKey` 集合中 |
| `Set<String> sDifference(String key, String otherKey)`          | 返回给定两个集合的差集             |
| `Set<String> sDifference(String key, Collection<String> otherKeys)` | 返回所有给定集合的差集             |
| `Long sDifferenceAndStore(String key, String otherKey, String destKey)` | 返回给定两个集合的差集并存储在 `destKey` 中 |
| `Long sDifferenceAndStore(String key, Collection<String> otherKeys, String destKey)` | 返回所有给定集合的差集并存储在 `destKey` 中 |
| `Long sRemove(String key, String... values)`                    | 移除集合中一个或多个成员           |
| `String sPop(String key)`                                       | 移除并返回集合中的一个随机元素     |
| `Boolean sMove(String key, String value, String destKey)`       | 将 member 元素从 source 集合移动到 destination 集合 |

### 3.6. ZSet (有序集合)

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `Boolean zAdd(String key, String value, double score)`          | 向有序集合添加一个成员，或者更新已存在成员的分数 |
| `Long zAdd(String key, Set<ScoredValue<String>> values)`        | 向有序集合添加多个成员，或者更新已存在成员的分数 |
| `Set<String> zRange(String key, long start, long end)`          | 通过索引区间返回有序集合成中指定区间内的成员 |
| `Set<ScoredValue<String>> zRangeWithScores(String key, long start, long end)` | 通过索引区间返回有序集合成中指定区间内的成员和分数 |
| `Set<String> zRangeByScore(String key, double min, double max)` | 通过分数返回有序集合指定区间内的成员 |
| `Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max)` | 通过分数返回有序集合指定区间内的成员和分数 |
| `Set<ScoredValue<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end)` | 带偏移量的通过分数返回有序集合指定区间内的成员和分数 |
| `Set<String> zReverseRange(String key, long start, long end)`   | 返回有序集中指定区间内的成员，通过索引，分数从高到低 |
| `Set<ScoredValue<String>> zReverseRangeWithScores(String key, long start, long end)` | 返回有序集中指定区间内的成员和分数，通过索引，分数从高到低 |
| `Set<String> zReverseRangeByScore(String key, double min, double max)` | 返回有序集中指定分数区间内的成员，分数从高到低排序 |
| `Set<ScoredValue<String>> zReverseRangeByScoreWithScores(String key, double min, double max)` | 返回有序集中指定分数区间内的成员和分数，分数从高到低排序 |
| `Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end)` | 带偏移量的返回有序集中指定分数区间内的成员，分数从高到低排序 |
| `Long zRank(String key, String value)`                          | 返回有序集中指定成员的排名。其中有序集成员按分数值递增(从小到大)顺序排列 |
| `Long zReverseRank(String key, String value)`                   | 返回有序集中指定成员的排名。其中有序集成员按分数值递减(从大到小)顺序排列 |
| `Long zCount(String key, double min, double max)`               | 计算在有序集合中指定区间分数的成员数 |
| `Long zSize(String key)`                                        | 获取有序集合的成员数               |
| `Long zZCard(String key)`                                       | `zSize` 的别名                     |
| `Double zScore(String key, String value)`                       | 返回有序集中，成员的分数值         |
| `Long zUnionAndStore(String key, String otherKey, String destKey)` | 计算两个有序集合的并集，并存储到 `destKey` |
| `Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey)` | 计算多个有序集合的并集，并存储到 `destKey` |
| `Long zIntersectAndStore(String key, String otherKey, String destKey)` | 计算两个有序集合的交集，并存储到 `destKey` |
| `Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey)` | 计算多个有序集合的交集，并存储到 `destKey` |
| `ScoredValueScanCursor<String> zScan(String key, ScanArgs options)` | 迭代有序集合中的元素（包括元素成员和元素分值） |
| `Long zRemove(String key, String... values)`                    | 移除有序集合中的一个或多个成员     |
| `Double zIncrementScore(String key, String value, double delta)` | 有序集合中对指定成员的分数加上增量 delta |
| `Long zRemoveRange(String key, long start, long end)`           | 移除有序集合中给定的排名区间的所有成员 |
| `Long zRemoveRangeByScore(String key, double min, double max)`  | 移除有序集合中给定的分数区间的所有成员 |

---

## 4. TopApi2：顶层静态API - 高级数据结构

这些静态API为Redis高级数据结构提供了便捷的操作接口，包括位图、HyperLogLog、地理空间和布隆过滤器等。

### 4.1. Bitmap (位图)

对 `String` 类型的 value 进行位操作。

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `Long bitCount(String key)`                                     | 统计位图中被设置为 1 的位的数量    |
| `Long bitCount(String key, long start, long end)`               | 统计指定字节范围内被设置为 1 的位的数量 |
| `Long bitPos(String key, boolean state)`                        | 返回第一个被设置为 0 或 1 的位的位置 |
| `Long bitPos(String key, boolean state, long start)`            | 从指定字节开始，返回第一个被设置为 0 或 1 的位的位置 |
| `Long bitOpAnd(String destination, String... keys)`             | 对一个或多个 key 求逻辑并，并将结果保存到 `destination` key |
| `Long bitOpOr(String destination, String... keys)`              | 对一个或多个 key 求逻辑或，并将结果保存到 `destination` key |
| `Long bitOpXor(String destination, String... keys)`             | 对一个或多个 key 求逻辑异或，并将结果保存到 `destination` key |
| `Long bitOpNot(String destination, String source)`              | 对给定 key 求逻辑非，并将结果保存到 `destination` key |



### 4.4. BloomFilter (布隆过滤器)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `boolean tryInit(String key, long expectedElements, double falsePositiveProbability)` | 初始化布隆过滤器                                                   |
| `boolean add(String key, Object element)`                             | 添加元素到布隆过滤器                                               |
| `boolean addAll(String key, Collection<?> elements)`                  | 批量添加元素到布隆过滤器                                           |
| `boolean contains(String key, Object element)`                        | 检查元素是否可能存在于布隆过滤器中                                 |
| `long count(String key)`                                              | 获取已添加的元素数量 (估算值)                                      |
| `long getExpectedElements(String key)`                                | 获取预期元素数量                                                   |
| `double getFalsePositiveProbability(String key)`                      | 获取误判率                                                         |

---

## 5. TopApi3：顶层静态API - 服务器与脚本

这些静态API提供了Redis服务器管理、脚本执行、流处理和发布订阅等功能。

### 5.1. Server (服务器管理)

| 方法签名                               | 描述                               |
|:---------------------------------------|:-----------------------------------|
| `String ping()`                        | 检查服务器是否在线，返回 "PONG"    |
| `Long dbSize()`                        | 返回当前数据库的 key 的数量        |
| `String flushDb()`                     | **[高危]** 清空当前数据库          |
| `String flushAll()`                    | **[高危]** 清空所有数据库          |
| `String info()`                        | 获取服务器的各种信息和统计         |
| `String info(String section)`          | 获取服务器指定部分的信息           |
| `Map<String, String> configGet(String parameter)` | 获取服务器的配置参数               |
| `String configSet(String parameter, String value)` | 动态调整服务器的配置               |

### 5.4. Pub/Sub (发布/订阅)

使用 Redisson 的 `RTopic` 实现，支持对任意可序列化对象的发布和订阅。

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `long publish(String topic, Object message)`                    | 向指定主题发布一条消息             |
| `int subscribe(String topic, MessageListener<?> listener)`      | 订阅一个主题，并返回监听器ID       |
| `void unsubscribe(String topic, int listenerId)`                | 从指定主题中移除一个监听器         |
| `interface MessageListener<T>`                                  | 消息监听器接口 (包含 `onMessage(String channel, T msg)`) |

### 5.2. Scripting (脚本)

基于 Lettuce 的 `eval` 和 `evalsha` 实现，提供对 Lua 脚本的执行和管理能力。

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `<T> T eval(String script, ScriptOutputType type, String[] keys, String... values)` | 执行一个 Lua 脚本                  |
| `<T> T evalSha(String sha, ScriptOutputType type, String[] keys, String... values)` | 通过 SHA1 校验和执行一个已缓存的脚本 |
| `String scriptLoad(String script)`                              | 将脚本加载到服务器的脚本缓存中     |
| `List<Boolean> scriptExists(String... sha1)`                    | 检查指定的脚本是否存在于缓存中     |
| `String scriptFlush()`                                          | 从脚本缓存中移除所有的脚本         |
| `String scriptKill()`                                           | 杀死当前正在运行的、没有写操作的 Lua 脚本 |
| `enum ScriptOutputType`                                         | 定义脚本的返回类型 (BOOLEAN, INTEGER, MULTI, STATUS, VALUE) |

### 4.2. HyperLogLog

基于 Redisson 的 `RHyperLogLog` 实现，用于进行基数估算。

| 方法签名                                                        | 描述                               |
|:----------------------------------------------------------------|:-----------------------------------|
| `boolean pfAdd(String key, Object... elements)`                 | 向 HyperLogLog 添加一个或多个元素  |
| `long pfCount(String... keys)`                                  | 返回一个或多个 HyperLogLog 的并集基数估算值 |
| `void pfMerge(String destKey, String... sourceKeys)`            | 将多个 HyperLogLog 合并为一个      |

### 4.3. Geospatial (地理空间)

基于 Redisson 的 `RGeo` 实现，用于地理位置数据的存储和查询。

| 方法签名                                                                                              | 描述                                   |
|:------------------------------------------------------------------------------------------------------|:---------------------------------------|
| `Long geoAdd(String key, double longitude, double latitude, Object member)`                          | 添加单个地理位置                       |
| `Long geoAdd(String key, Map<Object, GeoPosition> geoEntries)`                                       | 批量添加地理位置                       |
| `Map<Object, GeoPosition> geoPos(String key, Object... members)`                                     | 获取成员的地理位置坐标                 |
| `Double geoDist(String key, Object member1, Object member2, GeoUnit unit)`                           | 计算两个成员间的地理距离               |
| `Map<Object, Double> geoRadius(String key, double longitude, double latitude, double radius, GeoUnit unit)` | 基于坐标进行半径查询（返回距离）       |
| `List<Object> geoRadius(String key, double longitude, double latitude, double radius, GeoUnit unit, int count)` | 基于坐标进行半径查询（限制数量）       |
| `Map<Object, Double> geoRadiusByMember(String key, Object member, double radius, GeoUnit unit)`      | 基于成员进行半径查询（返回距离）       |
| `List<Object> geoRadiusByMember(String key, Object member, double radius, GeoUnit unit, int count)`  | 基于成员进行半径查询（限制数量）       |
| `List<Object> geoSearch(String key, GeoSearchArgs args)`                                             | 高级地理空间搜索                       |
| `enum GeoUnit`                                                                                        | 距离单位枚举 (METERS, KILOMETERS, MILES, FEET) |

### 5.3. Streams (流)

基于 Redisson 的 `RStream` 实现，用于消息流的处理和消费者组管理。

| 方法签名                                                                                              | 描述                                   |
|:------------------------------------------------------------------------------------------------------|:---------------------------------------|
| `StreamMessageId add(String key, String field, Object value)`                                        | 添加单个字段消息到流                   |
| `StreamMessageId addAll(String key, Map<String, Object> fields)`                                     | 添加多字段消息到流                     |
| `Map<StreamMessageId, Map<String, Object>> read(String key, StreamMessageId startId)`               | 从指定 ID 开始读取消息                 |
| `Map<StreamMessageId, Map<String, Object>> read(String key, int count, StreamMessageId startId)`    | 从指定 ID 开始读取指定数量的消息       |
| `Map<StreamMessageId, Map<String, Object>> readRange(String key, StreamMessageId startId, StreamMessageId endId)` | 读取指定范围内的消息                   |
| `Map<StreamMessageId, Map<String, Object>> readRange(String key, int count, StreamMessageId startId, StreamMessageId endId)` | 读取指定范围内的指定数量消息           |
| `void createGroup(String key, String groupName)`                                                     | 创建消费者组（从流的末尾开始）         |
| `void createGroup(String key, String groupName, StreamMessageId id)`                                 | 创建消费者组（从指定消息 ID 开始）     |
| `Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName)` | 消费者组读取新消息                     |
| `Map<StreamMessageId, Map<String, Object>> readGroup(String key, String groupName, String consumerName, int count)` | 消费者组读取指定数量的新消息           |
| `long ack(String key, String groupName, StreamMessageId... ids)`                                     | 确认消息已被处理                       |
| `long delete(String key, StreamMessageId... ids)`                                                    | 删除指定的消息                         |
| `long length(String key)`                                                                             | 获取流中消息的数量                     |
| `long trim(String key, int count)`                                                                    | 修剪流，保留指定数量的最新消息         |
| `long trimNonStrict(String key, int count)`                                                           | 非严格修剪流（性能更好）               |

---

## 6. TopApi4：顶层静态API - 分布式锁与同步

这些静态API提供了基于Redisson的分布式锁和同步机制，支持可重入锁、公平锁、读写锁、信号量和倒计时器等。

### 6.1. ReentrantLock (可重入锁)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `void lock(String key)`                                               | 获取一个阻塞式、可重入的锁                                         |
| `void lock(String key, long leaseTime, TimeUnit unit)`               | 获取一个带租约时间的锁 (看门狗不生效)                              |
| `boolean tryLock(String key)`                                         | 尝试非阻塞地获取锁                                                 |
| `boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit)` | 在指定时间内尝试获取锁，并设置租约时间                             |
| `void unlock(String key)`                                             | 释放锁 (仅当锁被当前线程持有时)                                    |

### 6.2. FairLock (公平锁)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `void lock(String key)`                                               | 获取一个阻塞式公平锁                                               |
| `void lock(String key, long leaseTime, TimeUnit unit)`               | 获取一个带租约时间的公平锁 (看门狗不生效)                          |
| `boolean tryLock(String key)`                                         | 尝试非阻塞地获取公平锁                                             |
| `boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit)` | 在指定时间内尝试获取公平锁，并设置租约时间                         |
| `void unlock(String key)`                                             | 释放公平锁 (仅当锁被当前线程持有时)                                |

### 6.3. ReadWriteLock (读写锁)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `void readLock(String key)`                                           | 获取一个阻塞式读锁                                                 |
| `void readLock(String key, long leaseTime, TimeUnit unit)`           | 获取一个带租约时间的读锁 (看门狗不生效)                            |
| `boolean tryReadLock(String key)`                                     | 尝试非阻塞地获取读锁                                               |
| `boolean tryReadLock(String key, long waitTime, long leaseTime, TimeUnit unit)` | 在指定时间内尝试获取读锁，并设置租约时间                           |
| `void unlockRead(String key)`                                         | 释放读锁 (仅当锁被当前线程持有时)                                  |
| `void writeLock(String key)`                                          | 获取一个阻塞式写锁                                                 |
| `void writeLock(String key, long leaseTime, TimeUnit unit)`          | 获取一个带租约时间的写锁 (看门狗不生效)                            |
| `boolean tryWriteLock(String key)`                                    | 尝试非阻塞地获取写锁                                               |
| `boolean tryWriteLock(String key, long waitTime, long leaseTime, TimeUnit unit)` | 在指定时间内尝试获取写锁，并设置租约时间                           |
| `void unlockWrite(String key)`                                        | 释放写锁 (仅当锁被当前线程持有时)                                  |

### 6.4. Semaphore (信号量)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `boolean trySetPermits(String key, int permits)`                      | 设置信号量的许可证数量                                             |
| `void acquire(String key)`                                            | 获取一个许可证 (阻塞式)                                            |
| `void acquire(String key, int permits)`                               | 获取多个许可证 (阻塞式)                                            |
| `boolean tryAcquire(String key)`                                      | 尝试获取一个许可证 (非阻塞)                                        |
| `boolean tryAcquire(String key, int permits)`                         | 尝试获取多个许可证 (非阻塞)                                        |
| `boolean tryAcquire(String key, long timeout, TimeUnit unit)`         | 在指定时间内尝试获取一个许可证                                     |
| `void release(String key)`                                            | 释放一个许可证                                                     |
| `void release(String key, int permits)`                               | 释放多个许可证                                                     |

### 6.5. CountDownLatch (倒计时器)

| 方法签名                                                              | 描述                                                               |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------|
| `boolean trySetCount(String key, long count)`                         | 设置倒计时器的初始计数                                             |
| `void await(String key)`                                              | 等待计数器归零 (阻塞式)                                            |
| `boolean await(String key, long timeout, TimeUnit unit)`              | 在指定时间内等待计数器归零                                         |
| `void countDown(String key)`                                          | 将计数器减1                                                        |
| `long getCount(String key)`                                           | 获取当前计数值                                                     |
