local key = KEYS[1]
local threadId = ARGV[1]

if(redis.call('get', key) == threadId) then
    return redis.call('del', key)
end
return 0
