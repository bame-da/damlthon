import React from "react";
import { Route, Switch, withRouter } from "react-router-dom";
import classnames from "classnames";
import useStyles from "./styles";
import Header from "../Header/Header";
import Sidebar from "../Sidebar/Sidebar";
import { useLayoutState } from "../../context/LayoutContext";
import Contracts from "../../pages/contracts/Contracts";
import GroupChat from "../../pages/chatgroup/ChatGroup";
import NewGroup from "../../pages/newgroup/NewGroup";
import Upload from "../../pages/upload/Upload";

function Layout(props) {
  const classes = useStyles();
  const layoutState = useLayoutState();

  return (
    <div className={classes.root}>
        <>
          <Header />
          <Sidebar />
          <div
            className={classnames(classes.content, {
              [classes.contentShift]: layoutState.isSidebarOpened,
            })}
          >
            <div className={classes.fakeToolbar} />
            <Switch>
              <Route path="/app/contracts" component={Contracts} />
              <Route path="/app/chatgroup/:groupName" component={GroupChat} />
              <Route path="/app/newgroup" component={NewGroup} />
              <Route path="/app/upload" component={Upload} />
            </Switch>
          </div>
        </>
    </div>
  );
}

export default withRouter(Layout);
