import React from "react";
import { Button } from "@material-ui/core";
import useStyles from "./styles";
import { Typography } from "../Wrappers/Wrappers";

export default function PageTitle(props) {
  var classes = useStyles();

  return (
    <div className={classes.pageTitleContainer}>
      <Typography className={classes.typo} variant="h1" size="sm">
        {props.title}
      </Typography>
      {props.button && (
        <Button
          classes={{ root: classes.button }}
          variant="contained"
          size="large"
          color="primary"
          onClick={props.onButtonClick}
        >
          {props.button}
        </Button>
      )}
    </div>
  );
}
