all:

csvs-to-sqlite:
	rm -f plenos.db
	csvs-to-sqlite plenos.csv plenos.db

datasette-plugins:
	datasette install datasette-vega
	datasette install datasette-export-notebook
	datasette install datasette-copyable
	datasette install datasette-block-robots

datasette: csvs-to-sqlite datasette-plugins
	datasette plenos.db

sqlite-utils:
	sqlite-utils extract plenos.db plenos periodo_parlamentario
	sqlite-utils extract plenos.db plenos periodo_anual
	sqlite-utils extract plenos.db plenos legislatura
