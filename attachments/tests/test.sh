#!/bin/sh
set -eu

echo "Uploading..."
RES=$(curl -s -F 'attachment=@file;filename=file;observers=Bob' localhost:8080/uploadAndSubmit)
HASH=$(cat $RES | jq -r .hash)
CONTRACTID=$(cat $RES | jq -r .contract_id)
EXPECTED="$(cat file.sha1)"

echo "Got contract id $CONTRACTID"

if [ "$HASH" != "$EXPECTED" ]; then
  echo "Hash mismatch on upload: got $HASH, expected $EXPECTED"
  exit 1
else
  echo "Upload succeeded and hash matched"
fi

echo "Downloading..."
DOWNHASH="$(curl -s localhost:8080/attachments/$HASH | sha1sum | cut -d' ' -f1)"
if [ "$DOWNHASH" != "$EXPECTED" ]; then
  echo "Hash mismatch on download: got $HASH, expected $EXPECTED"
  exit 1
else
  echo "Download succeeded and hash matched"
fi

