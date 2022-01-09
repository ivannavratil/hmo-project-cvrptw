import datetime
import itertools
import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
import operator
from collections import defaultdict
from matplotlib import cm
from typing import Dict, Tuple, List


def parse_raw_graph_data_contents(lines: List[str]):
    expected_runtime = None
    params_to_all_scores: Dict[float, List[Tuple[int, float]]] = defaultdict(list)
    for line in lines:
        line = line.strip()
        if not line:
            continue
        parts = line.split(';')
        runtime_seconds = int(parts[0])
        vehicles_used = int(parts[1])
        total_distance = float(parts[2])
        result = (vehicles_used, total_distance)
        parameter_value = float(parts[3])

        if expected_runtime is None:
            expected_runtime = runtime_seconds
        elif abs(expected_runtime - runtime_seconds) > 3:
            print(f'ABNORMAL RUNTIME: {runtime_seconds}, expected {expected_runtime}')

        params_to_all_scores[parameter_value].append(result)

    params_to_score: Dict[float, Tuple[float, float]] = dict()
    for k, v in params_to_all_scores.items():
        avg_vehicles = float(np.mean(list(map(operator.itemgetter(0), v))))
        avg_distance = float(np.mean(list(map(operator.itemgetter(1), v))))
        params_to_score[k] = (avg_vehicles, avg_distance)

    return params_to_score


def convert_data_to_plot_format(params_to_score: Dict[float, Tuple[float, float]]):
    vehicles: List[float] = []
    distances: List[float] = []
    params: List[float] = []
    for k, v in params_to_score.items():
        vehicles.append(v[0])
        distances.append(v[1])
        params.append(k)

    return np.asarray(vehicles), np.asarray(distances), np.asarray(params),


def is_params_exponential(params: np.ndarray) -> bool:
    min_val = np.min(params)
    max_val = np.max(params)
    median_val = np.median(params)

    is_exponential = (max_val - median_val) / (median_val - min_val) > 10
    return is_exponential


def plot_scatter_param(ax, params_to_score: Dict[float, Tuple[float, float]], title: str):
    vehicles, distances, params = convert_data_to_plot_format(params_to_score)
    is_exponential = is_params_exponential(params)

    norm = mpl.colors.LogNorm() if is_exponential else mpl.colors.Normalize()
    cmap = cm.autumn

    ax.set_title(title, weight='bold')
    ax.set_xlabel('average number of vehicles', labelpad=6)
    ax.set_ylabel('average total distance', labelpad=6)

    ax.set_xmargin(0.15)
    ax.set_ymargin(0.15)

    im = ax.scatter(vehicles, distances, s=150, c=params, cmap=cmap, norm=norm, alpha=1)

    # min_area_index = np.argmin(params_area)
    # max_area_index = np.argmax(params_area)
    # min_area = params_area[min_area_index]
    # max_area = params_area[max_area_index]
    # min_value = params[min_area_index]
    # max_value = params[max_area_index]

    # for value, area in [(min_value, min_area), (max_value, max_area)]:
    #     ax.scatter([], [], c='k', alpha=0.2, s=area, label=str(value))
    # ax.legend(scatterpoints=1, frameon=True, labelspacing=1.3, borderpad=1.2, title='Legend')

    # for i, value in enumerate(params):
    #     ax.annotate(f'{param_name} = {value}', (vehicles[i], distances[i]))

    fig = ax.get_figure()
    fig.colorbar(im, ax=ax)


def plot_all_for_param(formatted_path_in: str, formatted_path_out: str, param_meta: Tuple[str, str], time_marker: str):
    fig, axs = plt.subplots(3, 2, figsize=(17, 17))
    for i, ax in enumerate(itertools.chain.from_iterable(axs)):
        instance_id = i + 1
        with open(formatted_path_in.format(time_marker=time_marker, instance_id=instance_id, param=param_meta[0])) as f:
            lines = f.readlines()

        params_to_score = parse_raw_graph_data_contents(lines)
        plot_scatter_param(ax, params_to_score, f'instance {instance_id}')

    fig.suptitle(f'Parameter variation ({time_marker}) - {param_meta[1]}', fontsize=21, y=0.995)
    # fig.supxlabel('average number of vehicles')
    # fig.supylabel('average total distance')

    fig.tight_layout()
    stamp = datetime.datetime.now().strftime("%Y-%m-%d-%H-%M-%S")
    fig.savefig(formatted_path_out.format(time_marker=time_marker, param=param_meta[0], stamp=stamp), dpi=300)
    fig.show()


parameter_names = [
    ('alpha', r'$\alpha$'),
    ('beta', r'$\beta$'),
    ('count', 'ants per iteration'),
    ('theta', r'$\theta$'),
    ('q0', r'$q_0$'),
    ('rho', r'$\rho$'),
    ('tau', r'$\tau_0$')
]


def plot_everything():
    for param_meta in parameter_names:
        plot_all_for_param(
            'src/main/resources/graph/res-{time_marker}-i{instance_id}-{param}-LS.txt',
            'src/main/resources/graph-rendered/{time_marker}-{param}-{stamp}-LS.png',
            param_meta, '5m'
        )


if __name__ == '__main__':
    plot_everything()
