#!/bin/sh
set -eu

HASH=$(curl -s -F 'attachment=@file' localhost:8080/upload | jq -r .hash)
EXPECTED="$(cat file.sha1)"

echo "Uploading..."
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

