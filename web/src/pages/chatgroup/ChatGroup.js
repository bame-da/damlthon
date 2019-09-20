import React, { useState } from "react";
import uuidv4 from "uuid/v4";
import { Grid, Table, Modal, TableRow, TableCell, TableBody, TextField, Button } from "@material-ui/core";
import { Add, AttachFile } from "@material-ui/icons";
import ReactJson from "react-json-view";
import PageTitle from "../../components/PageTitle/PageTitle";
import { useLedgerState, useLedgerDispatch, sendCommand, fetchContracts } from "../../context/LedgerContext";
import { useStyles } from "./styles";
import { useUserState } from "../../context/UserContext";
import {useDropzone} from 'react-dropzone';
import superagent from 'superagent';

import { Card, CardActionArea, CardContent, CardMedia, Typography } from '@material-ui/core';

export default function ChatGroup(props) {
  const {getInputProps, getRootProps} = useDropzone({
    onDrop: onDrop
  });
  const classes = useStyles();
  const user = useUserState();
  const ledger = useLedgerState();
  const dispatch = useLedgerDispatch();
  const [isFetching, setIsFetching] = useState(false);
  
  const [inviteOpen, setInviteOpen] = React.useState(false);

  const groupName = props.match.params.groupName;
  const groupContract = ledger.contracts.find(c => c.templateId.entityName === "ChatGroup" && c.argument.gid.id === groupName);

  var [messageValue, setMessageValue] = useState("")
  var [newMemberValue, setNewMemberValue] = useState("")

  const sendMessage = async () => {
    const templateId = { moduleName: "GroupChat", entityName: "ChatGroup" };
    const contractId = groupContract.contractId;
    const choice = "Post_Message";
    const msgId = uuidv4();
    const indexContractId = ledger.contracts.find(c => c.templateId.entityName === "MessageIndex" && c.argument.gid.id === groupName && c.argument.poster === user.login).contractId
    const argument = { poster: user.login, micid: indexContractId, id: msgId, text: messageValue, messageAttachment:
      uploadResult ? uploadResult.contract_id : null };
    const meta = {} // { ledgerEffectiveTime: 0 }; // Required if sandbox runs with static time
    const command = { templateId, contractId, choice, argument, meta };

    setUploadResult(null);
    await sendCommand(dispatch, user.token, "exercise", command, () => console.log("Command is sending"), () => console.log("Error occurred"));
  }

  const sendInvite = async () => {
    const templateId = { moduleName: "GroupChat", entityName: "GroupInvite" };
    const argument = { groupId: groupName, member: user.login, newMember: newMemberValue};
    const meta = {} // { ledgerEffectiveTime: 0 }; // Required if sandbox runs with static time
    const command = { templateId, argument, meta };
    await sendCommand(dispatch, user.token, "create", command, () => console.log("Command is sending"), () => console.log("Error occurred"));
  }

  // compare message contracts on postedAt
  function compare(a, b) {
    const postedAtA = a.argument.postedAt;
    const postedAtB = b.argument.postedAt;
  
    let comparison = 0;
    if (postedAtA > postedAtB) {
      comparison = 1;
    } else if (postedAtA < postedAtB) {
      comparison = -1;
    }
    return comparison;
  }

  const [uploadResult, setUploadResult] = useState("")
  function uploadCallback(err, res) {
    if (!err) {
      setUploadResult(res.body);
    }
    /*console.log("err:", err);
    console.log("res:", res);*/
  }

  function onDrop(files) {
    console.log(files);
    files.forEach(file => {
      console.log("uploading file: ", file.name);
      superagent
        .post('http://localhost:8080/uploadAndSubmit')
        .field('attachment', file)
        .field('observers',
           (groupContract != null) ? Object.keys(groupContract.argument.gid.members.textMap).join(",") : "")
        .field('filename', file.name)
        .end(uploadCallback);
    });
  }

  // TODO loosing textfield focus on refresh
  return (
    <>
      <PageTitle title={props.match.params.groupName}/>
      <Grid container spacing={4}>
      <Modal
        open={inviteOpen}
        onClose={() => setInviteOpen(false)}
      >
        <div className={classes.modal}>
          <h2 id="modal-title">Invite new member to {groupName}</h2>
          <TextField id="newmember"
            value={newMemberValue}
            placeholder="Alice"
            onChange={e => setNewMemberValue(e.target.value)}
            onKeyDown={e => {
              if (e.key === "Enter") {
                sendInvite()
                setNewMemberValue("")
                setInviteOpen(false)
              }
            }}
            />
        </div>
      </Modal>
      <Grid container>
        Members: {(groupContract != null) ? Object.keys(groupContract.argument.gid.members.textMap).join(",") : ""}
        <Button color="primary" onClick={() => setInviteOpen(true)}>{(<Add />)}</Button>
      </Grid>
      <Grid item xs={12}>
        <Table className={classes.table} size="small">
          <TableBody>
            {ledger.contracts.filter(c => c.templateId.entityName === "Message" && c.argument.mid.gid.id === groupName).sort(compare).map((c, i) => (
              <TableRow key={i} className={classes.tableRow}>
                <TableCell className={[classes.tableCell, classes.cell2]}>{c.argument.mid.poster}</TableCell>
                <TableCell className={[classes.tableCell, classes.cell3]}>{c.argument.text}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        </Grid>
      </Grid>
      <Grid container>
      <TextField id="message"
        value={messageValue}
        placeholder="Your message.."
        onChange={e => setMessageValue(e.target.value)}
        onKeyDown={e => {
        if (e.key === "Enter") {
          sendMessage()
          setMessageValue("")
          //fetchContracts(dispatch, user.token, setIsFetching, () => {})
        }
      }
    }
      />

    <Button {...getRootProps({className: 'dropzone'})} color="primary">{(<AttachFile />)}</Button>
    <input {...getInputProps()} />

    { uploadResult ? <img src={uploadResult.plain_url}></img> : "" }

    </Grid>
    </>
  );
}
