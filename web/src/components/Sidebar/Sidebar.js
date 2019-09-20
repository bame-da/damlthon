import React, { useState, useEffect } from "react";
import { Drawer, IconButton, List } from "@material-ui/core";
import { Add, Chat, ArrowBack } from "@material-ui/icons";
import { useTheme } from "@material-ui/styles";
import { withRouter } from "react-router-dom";
import classNames from "classnames";
import useStyles from "./styles";
import SidebarLink from "./components/SidebarLink/SidebarLink";
import { useLayoutState, useLayoutDispatch, toggleSidebar } from "../../context/LayoutContext";
import { useLedgerState } from "../../context/LedgerContext";

function Sidebar({ location }) {
  const ledger = useLedgerState();

  var classes = useStyles();
  var theme = useTheme();

  // global
  var { isSidebarOpened } = useLayoutState();
  var layoutDispatch = useLayoutDispatch();

  // local
  var [isPermanent, setPermanent] = useState(true);

  useEffect(function() {
    window.addEventListener("resize", handleWindowWidthChange);
    handleWindowWidthChange();
    return function cleanup() {
      window.removeEventListener("resize", handleWindowWidthChange);
    };
  });

  return (
    <Drawer
      variant={isPermanent ? "permanent" : "temporary"}
      className={classNames(classes.drawer, {
        [classes.drawerOpen]: isSidebarOpened,
        [classes.drawerClose]: !isSidebarOpened,
      })}
      classes={{
        paper: classNames({
          [classes.drawerOpen]: isSidebarOpened,
          [classes.drawerClose]: !isSidebarOpened,
        }),
      }}
      open={isSidebarOpened}
    >
      <div className={classes.toolbar} />
      <div className={classes.mobileBackButton}>
        <IconButton onClick={() => toggleSidebar(layoutDispatch)}>
          <ArrowBack
            classes={{
              root: classNames(classes.headerIcon, classes.headerIconCollapse),
            }}
          />
        </IconButton>
      </div>
      <List className={classes.sidebarList}>
        {ledger.contracts.filter(c => c.templateId.entityName === "ChatGroup").map((c, i) => (
        <SidebarLink
          key={c.argument["gid"]["id"]}
          label={c.argument["gid"]["id"]}
          path={("/app/chatgroup/") + c.argument["gid"]["id"]}
          icon={(<Chat />)}
          location={location}
          isSidebarOpened={isSidebarOpened}
        />
        ))}

        <SidebarLink
          key="newgroup"
          label="New Group"
          path="/app/newgroup"
          icon={(<Add />)}
          location={location}
          isSidebarOpened={isSidebarOpened}
        />
{/* 
        <SidebarLink
          key="contracts"
          label="Contracts"
          path="/app/contracts"
          icon={(<BorderAll />)}
          location={location}
          isSidebarOpened={isSidebarOpened}
        /> */}

      </List>
    </Drawer>
  );

  // ##################################################################
  function handleWindowWidthChange() {
    var windowWidth = window.innerWidth;
    var breakpointWidth = theme.breakpoints.values.md;
    var isSmallScreen = windowWidth < breakpointWidth;

    if (isSmallScreen && isPermanent) {
      setPermanent(false);
    } else if (!isSmallScreen && !isPermanent) {
      setPermanent(true);
    }
  }
}

export default withRouter(Sidebar);
