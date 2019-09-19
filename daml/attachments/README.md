
DAML File Attachments
---------------------

Implements a DAML model to track file attachment uploads.

To build:

$ daml build
$ daml codegen scala
$ sbt 'application / assembly'

To run:

$ export EXTERNAL_IP=1.2.3.4 # this is optional, affects url generation
$ export LEDGER_HOST=localhost
$ export LEDGER_PORT=6865
$ export SERVER_PORT=8080
$ export PARTY=Alice
$ java -jar application/target/scala-2.12/application.jar &
$ export SERVER_PORT=8081
$ export PARTY=Bob
$ java -jar application/target/scala-2.12/application.jar
...

Upload the file to the server and get the contract id, urls etc:

$ curl -v -F 'attachment=@lapland.jpg' -F 'filename=lapland.jpg' -F 'observers=Bob,Charlie' localhost:8080/uploadAndSubmit  | jq .

