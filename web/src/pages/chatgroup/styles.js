import { makeStyles } from "@material-ui/styles";

export const useStyles = makeStyles(theme => ({
  contractTable: {
    // tableLayout: "auto",
    // width: "auto"
  },
  tableCell: {
    verticalAlign: "top",
    paddingTop: 6,
    paddingBottom: 6,
    fontSize: "1rem"
  },
  cell1: {
    width: "5%",
  },
  cell2: {
    width: "10%",
  },
  cell3: {
    width: "auto",
  },
  tableRow: {
    height: "auto"
  },
  modal: {
    position: 'absolute',
    width: 400,
    backgroundColor: theme.palette.background.paper,
    border: '2px solid #000',
    boxShadow: theme.shadows[5],
    padding: theme.spacing(2, 4, 3),
  },
}));
