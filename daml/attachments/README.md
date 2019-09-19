
DAML File Attachments
---------------------

Implements a DAML model to track file attachment uploads.

To build:

$ daml build
$ daml codegen scala
$ sbt 'application / assembly'

To run:

$ export EXTERNAL_IP=1.2.3.4
$ java -jar application/target/scala-2.12/application.jar ledger-host 6865 8080 Alice
$ java -jar application/target/scala-2.12/application.jar ledger-host 6865 8081 Bob
$ java -jar application/target/scala-2.12/application.jar ledger-host 6865 8082 Charlie

Upload the file to the server and get the URL:

$ curl -s -F 'attachment=@myfile' localhost:8080/upload

Now create the AttachmentProposal contract with the url and hash.

