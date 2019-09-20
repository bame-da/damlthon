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
    fontSize: "0.75rem"
  },
  cell1: {
    width: "20%",
  },
  cell2: {
    width: "5%",
  },
  cell3: {
    width: "auto",
  },
  tableRow: {
    height: "auto"
  }
}));
