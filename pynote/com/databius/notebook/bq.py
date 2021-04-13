import pandas as pd
from google.cloud import bigquery
from threading import Thread

QUERY = '''
SELECT date, sym FROM `ghpr-dev.ahab_stage.packet` WHERE date >= "2020-01-03"
'''

def process():

    print('Thread.getName()')
    bq_clt = bigquery.Client(project='ghpr-dev')
    result = bq_clt.query(QUERY).result()
    print(result)
    df =  result.to_dataframe()

    df['sym'] = df['sym'].apply(lambda x: x.split('|')[2])

    # Parse for each date
    # dates = [dt.datetime.strftime(x, "%Y-%m-%d") for x in df['date'].unique()]

    weekday_list = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    full_affected_list = ''
    for d in sorted(df['date'].unique()):
        sym_list = df[df['date'] == d]['sym']
        print(sym_list)

for i in range(1, 1028):
    t = Thread(target=process)
    t.start()