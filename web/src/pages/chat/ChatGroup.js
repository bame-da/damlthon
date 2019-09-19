import React, { useState } from "react";
import uuidv4 from "uuid/v4";
import { Grid, Table, TableHead, TableRow, TableCell, TableBody, TextField } from "@material-ui/core";
import ReactJson from "react-json-view";
import PageTitle from "../../components/PageTitle/PageTitle";
import { useLedgerState, useLedgerDispatch, sendCommand, fetchContracts } from "../../context/LedgerContext";
import { useStyles } from "./styles";
import { useUserState } from "../../context/UserContext";

export default function ChatGroup(props) {
  const classes = useStyles();
  const user = useUserState();
  const ledger = useLedgerState();
  const dispatch = useLedgerDispatch();
  const [isFetching, setIsFetching] = useState(false);

  const groupName = props.match.params.groupName;

  var [messageValue, setMessageValue] = useState("")

  const sendMessage = async () => {
    const templateId = { moduleName: "GroupChat", entityName: "ChatGroup" };
    const contractId = ledger.contracts.find(c => c.templateId.entityName === "ChatGroup" && c.argument.gid.id === groupName).contractId
    const choice = "Post_Message";
    const msgId = uuidv4();
    const indexContractId = ledger.contracts.find(c => c.templateId.entityName === "MessageIndex" && c.argument.gid.id === groupName).contractId
    const argument = { poster: user.login, micid: indexContractId, id: msgId, text: messageValue};
    const meta = {} // { ledgerEffectiveTime: 0 }; // Required if sandbox runs with static time
    const command = { templateId, contractId, choice, argument, meta };
    await sendCommand(dispatch, user.token, "exercise", command, () => console.log("Command is sending"), () => console.log("Error occurred"));
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

  // TODO loosing textfield focus on refresh
  return (
    <>
      <PageTitle title={props.match.params.groupName}/>
      <Grid container spacing={4}>
      <Grid item xs={12}>
        <Table className={classes.table} size="small">
          <TableHead>
            <TableRow className={classes.tableRow}>
              <TableCell key="Time" className={classes.cell1}>Time</TableCell>
              <TableCell key="Poster" className={classes.cell1}>Poster</TableCell>
              <TableCell key="Message" className={classes.cell1}>Message</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {ledger.contracts.filter(c => c.templateId.entityName === "Message" && c.argument.mid.gid.id === groupName).sort(compare).map((c, i) => (
              <TableRow key={i} className={classes.tableRow}>
                <TableCell className={[classes.tableCell, classes.cell1]}>{c.argument.postedAt}</TableCell>
                <TableCell className={[classes.tableCell, classes.cell1]}>{c.argument.mid.poster}</TableCell>
                <TableCell className={[classes.tableCell, classes.cell1]}>{c.argument.text}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        </Grid>
      </Grid>
      <Grid container spacing={4}>
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
    </Grid>
    </>
  );
}
