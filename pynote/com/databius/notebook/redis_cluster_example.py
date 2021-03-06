from rediscluster import RedisCluster

startup_nodes = [{"host": "127.0.0.1", "port": 7001}, {"host": "127.0.0.1", "port": 7002},
                 {"host": "127.0.0.1", "port": 7003}, {"host": "127.0.0.1", "port": 7004},
                 {"host": "127.0.0.1", "port": 7005}, {"host": "127.0.0.1", "port": 7006}, ]
host_remap = [{"from_host": "172.17.0.2", "to_host": "127.0.0.1"}, {"from_host": "172.17.0.3", "to_host": "127.0.0.1"},
              {"from_host": "172.17.0.4", "to_host": "127.0.0.1"}, {"from_host": "172.17.0.5", "to_host": "127.0.0.1"},
              {"from_host": "172.17.0.6", "to_host": "127.0.0.1"},
              {"from_host": "172.17.0.7", "to_host": "127.0.0.1"}, ]
host_port_remap = [{"from_host": "172.17.0.2", "from_port": 7001, "to_host": "127.0.0.1", "to_port": 7001},
                   {"from_host": "172.17.0.3", "from_port": 7002, "to_host": "127.0.0.1", "to_port": 7002},
                   {"from_host": "172.17.0.4", "from_port": 7003, "to_host": "127.0.0.1", "to_port": 7003},
                   {"from_host": "172.17.0.5", "from_port": 7004, "to_host": "127.0.0.1", "to_port": 7004},
                   {"from_host": "172.17.0.6", "from_port": 7005, "to_host": "127.0.0.1", "to_port": 7005},
                   {"from_host": "172.17.0.7", "from_port": 7006, "to_host": "127.0.0.1", "to_port": 7006}, ]

rc = RedisCluster(
    startup_nodes=startup_nodes,
    decode_responses=True,
    host_port_remap=host_port_remap
)

## Debug output to show the client config/setup after client has been initialized.
## It should point to 127.0.0.1:7000 for those nodes.
# print(rc.connection_pool.nodes.nodes)

print(rc.set('foo', 'bar1'))
print(rc.get('foo'))
print(rc.get('foo1'))
