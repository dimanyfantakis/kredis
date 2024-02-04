import io.kotest.core.spec.style.BehaviorSpec

class ServerTest : BehaviorSpec ({
    Given(" ") {

        When(" ") {

            Then(" ") {

            }
        }
    }
})

// getBasicType
// parseRespType
// constructResponse
// get
// set, etc.

// Example.
/*
>>> from server_ex import Client
>>> client = Client()
>>> client.mset('k1', 'v1', 'k2', ['v2-0', 1, 'v2-2'], 'k3', 'v3')
3
>>> client.get('k2')
['v2-0', 1, 'v2-2']
>>> client.mget('k3', 'k1')
['v3', 'v1']
>>> client.delete('k1')
1
>>> client.get('k1')
>>> client.delete('k1')
0
>>> client.set('kx', {'vx': {'vy': 0, 'vz': [1, 2, 3]}})
1
>>> client.get('kx')
{'vx': {'vy': 0, 'vz': [1, 2, 3]}}
>>> client.flush()
2
 */