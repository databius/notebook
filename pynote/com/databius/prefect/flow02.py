import datetime, random
from typing import List
import prefect
from prefect import case, Flow, task, Task
from prefect.tasks.control_flow import case, merge

MY_KEYS = []


@task
def log(s):
    prefect.context.logger.info(str(s))


@task
def key(get: str):
    MY_KEYS.append(get)
    prefect.context.logger.info(f"I now have these keys: {MY_KEYS}")
    return MY_KEYS


@task
def door(req: List[str]):
    for key in req:
        if key not in MY_KEYS:
            raise Exception(f"I am missing this key: {key}")
    prefect.context.logger.info(f"I opened the {req} door")


@task
def must_be_heads():
    if random.random() > 0.3:
        prefect.context.logger.info(f"I tossed tails T.T")
        raise Exception("tails")
    prefect.context.logger.info(f"I tossed heads!")
    return "heads"


@task
def dice_roll(i):
    num = random.randint(1, 6)
    prefect.context.logger.info(f"I rolled a {num}")
    return num


@task
def add_all(nums):
    return sum(nums)


with Flow("exercise") as flow:
    # the dungeon
    key_a = key("a")
    key_b = key("b")
    key_c = key("c")
    door_a = door(["a"])
    door_b = door(["b"])
    door_c = door(["c"])
    door_bc = door(["b", "c"])

    key_a.set_downstream(door_a)
    key_b.set_downstream(door_b)
    key_c.set_downstream(door_c)
    door_bc.set_upstream(door_b)
    door_bc.set_upstream(door_c)

    # the boss
    # 1. toss a coin until you get heads (must_be_heads)
    heads = must_be_heads(
        upstream_tasks=[door_bc],
        task_args={
            "max_retries": 3,
            "retry_delay": datetime.timedelta(seconds=1),
        },
    )

    # 2. roll 5 dice (dice_roll)
    dice = dice_roll.map(i=range(5), upstream_tasks=[heads])
    # 3. print its sum (add_all)
    total = add_all(dice)
    log(f"its sum {total}")

    # 4. print next Collatz number: n/2 if even, 3n+1 if odd
    next_num = total // 2 if total % 2 == 0 else (total * 3) + 1
    log(f"next Collatz number: {next_num}")

flow.run()
flow.register("ramen")
