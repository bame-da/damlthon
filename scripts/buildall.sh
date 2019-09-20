cd ..

cd attachments
daml build
rm -r scala-codegen
daml codegen scala
sbt clean
sbt 'application / assembly'
cd ..

cd chat
daml build
cd ..

cd scripts