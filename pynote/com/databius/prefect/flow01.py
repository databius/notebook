import time

import prefect
from prefect import context, Flow, task, Parameter


@task
def log(s):
    prefect.context.logger.info(str(s))
    return s.upper()

@task
def sleep(t):
    time.sleep(t)

with Flow("my first flow") as flow:

    out = log("hello world")
    log("goodbye", upstream_tasks=[out])
    sleep(1)
    log(out)
    # print(out)

    p = Parameter('foo', 1)
    sleep(p)

# flow.run()
# flow.run({"foo": 1})
flow.register("ramen")

