"""
This application demonstrates how to perform core operations with the
Data Catalog API.

For more information, see the README.md and the official documentation at
https://cloud.google.com/data-catalog/docs.
"""
# Modified by Bryan

# -------------------------------
# Import required modules.
# -------------------------------
from google.api_core.exceptions import NotFound, PermissionDenied
from google.cloud import datacatalog_v1

# -------------------------------
# Set your Google Cloud Platform project ID.
# -------------------------------
project_id = 'peter-nguyen-sandbox'
location = 'asia-southeast1'

# -------------------------------
# Use Application Default Credentials to create a new
# Data Catalog client. GOOGLE_APPLICATION_CREDENTIALS
# environment variable must be set with the location
# of a service account key file.
# -------------------------------
datacatalog = datacatalog_v1.DataCatalogClient()

# -------------------------------
# 1. Environment cleanup: delete pre-existing data.
# -------------------------------
# Delete any pre-existing Entry with the same name
# that will be used in step 3.
expected_entry_name = datacatalog_v1.DataCatalogClient \
    .entry_path(project_id, location, 'fileset_entry_group', 'fileset_entry_id')

try:
    datacatalog.delete_entry(name=expected_entry_name)
    print("Deleted an entry")
except (NotFound, PermissionDenied):
    print("Did not find an existing entry to delete")

# Delete any pre-existing Entry Group with the same name
# that will be used in step 2.
expected_entry_group_name = datacatalog_v1.DataCatalogClient \
    .entry_group_path(project_id, location, 'fileset_entry_group')

try:
    datacatalog.delete_entry_group(name=expected_entry_group_name)
    print("Deleted an EntryGroup")
except (NotFound, PermissionDenied):
    print("Did not find an existing EntryGroup to delete")

# -------------------------------
# 2. Create an Entry Group.
# -------------------------------
entry_group_obj = datacatalog_v1.types.EntryGroup()
entry_group_obj.display_name = 'My Fileset Entry Group'
entry_group_obj.description = 'This Entry Group consists of ....'

entry_group = datacatalog.create_entry_group(
    parent=datacatalog_v1.DataCatalogClient.common_location_path(project_id, location),
    entry_group_id='fileset_entry_group',
    entry_group=entry_group_obj)
print('Created entry group: {}'.format(entry_group.name))

# -------------------------------
# 3. Create a Fileset Entry.
# -------------------------------
entry = datacatalog_v1.types.Entry()
entry.display_name = 'My Fileset - created through Python client library'
entry.description = 'This fileset consists of ....'
entry.gcs_fileset_spec.file_patterns.append('gs://my_bucket/*')
entry.gcs_fileset_spec.file_patterns.append('gs://my_bucket/*.csv')
entry.gcs_fileset_spec.file_patterns.append('gs://my_bucket/*.yaml')
entry.gcs_fileset_spec.file_patterns.append('gs://my_bucket/*.jsonl')
entry.type_ = datacatalog_v1.EntryType.FILESET

# Create the Schema, for example when you have a csv file.
print("Creating schema for FileSet...")
columns = []
columns.append(datacatalog_v1.types.ColumnSchema(
    column='first_name',
    description='First name',
    mode='REQUIRED',
    type_='STRING'))

columns.append(datacatalog_v1.types.ColumnSchema(
    column='last_name',
    description='Last name',
    mode='REQUIRED',
    type_='STRING'))

# Create sub columns for the addresses parent column
subcolumns = []

subcolumns.append(datacatalog_v1.types.ColumnSchema(
    column='city',
    description='City',
    mode='NULLABLE',
    type_='STRING'))

subcolumns.append(datacatalog_v1.types.ColumnSchema(
    column='state',
    description='State',
    mode='NULLABLE',
    type_='STRING'))

columns.append(datacatalog_v1.types.ColumnSchema(
    column='addresses',
    description='Addresses',
    mode='REPEATED',
    subcolumns=subcolumns,
    type_='RECORD'))

entry.schema.columns.extend(columns)

entry = datacatalog.create_entry(
    parent=entry_group.name,
    entry_id='fileset_entry_id',
    entry=entry)
print('Created entry: {}'.format(entry.name))