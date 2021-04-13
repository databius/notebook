import tabula

"""
https://pypi.org/project/tabula-py/
"""

tabula.convert_into("creationbasket.pdf", "output.csv", output_format="csv", pages='all')
