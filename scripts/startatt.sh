export EXTERNAL_IP=1.2.3.4 # this is optional, affects url generation
export LEDGER_HOST=localhost
export LEDGER_PORT=6011
export SERVER_PORT=8080
export PARTY=
java -jar ../daml/attachments/application/target/scala-2.12/application.jar
