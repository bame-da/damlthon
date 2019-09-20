import React, { useState } from "react";
import { TextField } from "@material-ui/core";
import PageTitle from "../../components/PageTitle/PageTitle";
import { useLedgerState, useLedgerDispatch, sendCommand, fetchContracts } from "../../context/LedgerContext";
import { useStyles } from "./styles";
import { useUserState } from "../../context/UserContext";

export default function NewGroup() {
  const classes = useStyles();
  const user = useUserState();
  const dispatch = useLedgerDispatch();

  var [groupValue, setGroupValue] = useState("")

  const createGroup = async () => {
    const templateId = { moduleName: "GroupChat", entityName: "GroupSetup" };
    const argument = { creator: user.login, id: groupValue};
    const meta = {} // { ledgerEffectiveTime: 0 }; // Required if sandbox runs with static time
    const command = { templateId, argument, meta };
    const response = await sendCommand(dispatch, user.token, "create", command, () => console.log("Command is sending"), () => console.log("Error occurred"));

    const contractId = response.result.contractId
    
    // Confirm the group setup
    const choice = "CreateGroup";
    const exerciseCommand = { templateId, contractId, choice, argument: {}, meta: {} };
    await sendCommand(dispatch, user.token, "exercise", exerciseCommand, () => console.log("Command is sending"), () => console.log("Error occurred"));
  }

  return (
    <>
      <PageTitle title="Create a new Group"/>
      <TextField id="groupname"
        value={groupValue}
        placeholder="New group name"
        onChange={e => setGroupValue(e.target.value)}
        onKeyDown={e => {
        if (e.key === "Enter") {
          createGroup()
          setGroupValue("")
        }
      }
    }
    />
    </>
  );
}
