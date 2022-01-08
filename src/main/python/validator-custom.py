import math
import sys


def parse_instance(path):
    with open(path, encoding='utf-8') as f:
        lines = f.readlines()

    vehicle_num, capacity = list(map(int, lines[2].split()))

    names = ('id', 'x', 'y', 'demand', 'ready', 'due', 'service')
    nodes = [dict(zip(names, map(int, line.split()))) for line in lines[7:]]

    return vehicle_num, capacity, nodes


def parse_solution(path):
    with open(path, encoding='utf-8') as f:
        lines = f.readlines()

    routes_num = int(lines[0])
    distance = float(lines[-1])

    routes = []
    i = 1
    for line in lines[1:-1]:
        assert line.startswith(f'{i}: '), f'wrong route number {i}'
        parts = line.split(':', 1)[1].strip().split('->')
        route = []
        for part in parts:
            assert part.endswith(')'), 'ending )'
            node, time = list(map(int, part[:-1].split('(')))
            route.append((node, time))
        routes.append(route)
        i += 1

    return routes_num, distance, routes


def validate(vehicle_num, capacity, nodes, routes_num, distance, routes):
    assert vehicle_num >= routes_num

    for i, node in enumerate(nodes):
        assert node['id'] == i

    total_travelled = 0
    visited_node_ids = set()

    for route in routes:
        assert len(route) >= 3
        assert route[0] == (0, 0)
        assert route[-1][0] == 0
        for customer_id, _ in route[1:-1]:
            assert customer_id != 0

        accumulated_demand = 0

        last_customer_id = 0
        last_customer_depart = 0
        for customer_id, start_time in route[1:]:
            customer = nodes[customer_id]
            if customer_id != 0:
                assert customer['id'] not in visited_node_ids
                visited_node_ids.add(customer_id)

            accumulated_demand += customer['demand']
            total_travelled += calc_distance(last_customer_id, customer_id, nodes)

            current_arrive = last_customer_depart + calc_travel_time(last_customer_id, customer_id, nodes)
            assert current_arrive <= customer['due'], f'{current_arrive} <= {customer["due"]}'

            current_service_start = max(current_arrive, customer['ready'])
            assert start_time == current_service_start, f'{route[1]} == {current_service_start}, {route}'

            last_customer_depart = current_service_start + customer['service']
            last_customer_id = customer_id

        assert accumulated_demand <= capacity

    assert len(visited_node_ids) == len(nodes) - 1
    assert abs(total_travelled - distance) < 1E-5


def calc_distance(id1, id2, nodes):
    return math.sqrt((nodes[id1]['x'] - nodes[id2]['x']) ** 2 + (nodes[id1]['y'] - nodes[id2]['y']) ** 2)


def calc_travel_time(id1, id2, nodes):
    return math.ceil(calc_distance(id1, id2, nodes))


if __name__ == '__main__':
    validate(*parse_instance(sys.argv[1]), *parse_solution(sys.argv[2]))
    print('Solution valid!')
