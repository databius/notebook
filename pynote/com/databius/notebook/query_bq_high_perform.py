# pip install --user --upgrade google-cloud-bigquery-storage[fastavro]
import google.cloud.bigquery as bq
import google.cloud.bigquery_storage_v1 as bqs


# light wrapper to keep the mess in
def bqs_query_iterator(bq_client, query):
    job = bq_client.query(query)
    job.done()
    table = bq_client.get_job(job.job_id).destination
    client = bqs.BigQueryReadClient()
    read_session = bqs.types.ReadSession()
    read_session.table = table = f"projects/{table.project}/datasets/{table.dataset_id}/tables/{table.table_id}"
    read_session.data_format = bqs.types.DataFormat.AVRO

    parent = "projects/{}".format({table.project})
    session = client.create_read_session(
        parent=parent,
        read_session=requested_session,
        # We'll use only a single stream for reading data from the table. However,
        # if you wanted to fan out multiple readers you could do so by having a
        # reader process each individual stream.
        max_stream_count=1,
    )
    reader = client.read_rows(session.streams[0].name)

    session = client.create_read_session(
        parent = f"projects/{bq_client.project}",
        read_session = read_session,
        max_stream_count=1,
    )
    reader = client.read_rows(session.streams[0].name)

    for element in reader.rows(session):
        yield element  # dict


# entry point
for _ in bqs_query_iterator(
        bq.Client(project="ghpr-prod"),
        """SELECT
           blob
         FROM
           `ghpr-prod`.Box_Hist.packet
         WHERE date = "2020-06-25"
         AND EXCH = "XOSE"
         AND sym = "FUTURE|XOSE|FUT_NK225_2009|2009"
         LIMIT 1000000
        """
):
    pass