const fetch = require('node-fetch');
const jwt = require('jsonwebtoken');

const [, , host, port, party, ledgerId] = process.argv;
    
const applicationId = "chat-invite-bot";
const payload = { ledgerId, applicationId, party };
const token = jwt.sign(payload, "secret");

async function pollAndPlay() {
    var req = {
        headers: {
          "Authorization" : "Bearer " + token
        }
      }

      await fetch("http://" + host + ":" + port + "/contracts/search", req)
        .then(result => result.json())
        .then(processInvites);
}

async function processInvites(acsResult) {
    //console.log("acs:", JSON.stringify(acsResult));
    for(var r = 0; r < acsResult.result.length; r++) {
      var activeContracts = acsResult.result[r].activeContracts;
      for(var i = 0; i < activeContracts.length; i++) {
        var c = activeContracts[i];
        if(c.templateId.entityName==="GroupInvite") {
          processInvite(c);
        }
        else if(c.templateId.entityName==="GroupMembershipRequest") {
          processRequest(acsResult, c);
        }
      }
    }
  }

async function processInvite(c) {
    if(c.argument.newMember === party) {
      console.log("Processing invite:", JSON.stringify(c));
      acceptInvite(c.contractId);
    }
}

async function processRequest(acs, c) {
    if(c.argument.member === party) {
      console.log("Processing request:", JSON.stringify(c));
      acceptRequest(acs, c);
    }
}

async function acceptInvite(cid) {
  console.log("accepting invite", cid);
  var body = {
    "templateId": {
        "moduleName": "GroupChat",
        "entityName": "GroupInvite"
    },
    "contractId": cid,
    "choice": "AcceptInvite",
    "argument": { }
  }

  var req = {
  method: "POST",
  headers: {
      "Authorization" : "Bearer " + token
  },
  body : JSON.stringify(body)
  }

  await fetch("http://" + host + ":" + port + "/command/exercise", req)
  .then(result => result.json())
  .then(console.log);
}

async function acceptRequest(acsResult, requestContract) {
  console.log("accepting request", JSON.stringify(requestContract));

  const groupName = requestContract.argument.groupId
  var groupCid = ""
  var messageIndexCids = []

    for(var r = 0; r < acsResult.result.length; r++) {
      var activeContracts = acsResult.result[r].activeContracts;
      for(var i = 0; i < activeContracts.length; i++) {
        var c = activeContracts[i];
        if (c.templateId.entityName === "ChatGroup" && c.argument.gid.id === groupName) {
          groupCid = c.contractId
        }
        else if (c.templateId.entityName === "MessageIndex" && c.argument.gid.id === groupName) {
          messageIndexCids.push(c.contractId)
        }
      }
    }

  var body = {
    "templateId": {
        "moduleName": "GroupChat",
        "entityName": "GroupMembershipRequest"
    },
    "contractId": requestContract.contractId,
    "choice": "Onboard",
    "argument": {
      groupCid,
      messageIndexCids
     }
  }

  var req = {
  method: "POST",
  headers: {
      "Authorization" : "Bearer " + token
  },
  body : JSON.stringify(body)
  }

  await fetch("http://" + host + ":" + port + "/command/exercise", req)
  .then(result => result.json())
  .then(console.log);
}

var timer = setInterval(()=> pollAndPlay(), 1000);