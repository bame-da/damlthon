import React, { useState } from "react";
import { HashRouter, Route, Switch, Redirect } from "react-router-dom";
import Layout from "./Layout/Layout";
import Error from "../pages/error/Error";
import Login from "../pages/login/Login";
import { useUserState } from "../context/UserContext";
import { useLedgerDispatch, fetchContracts } from "../context/LedgerContext";

var timer = null;

export default function App() {
  var { isAuthenticated } = useUserState();
  const [isFetching, setIsFetching] = useState(false);
  const dispatch = useLedgerDispatch();
  const user = useUserState();

  if (timer == null) {
    timer = setInterval(() => {
      if (isAuthenticated) {
       fetchContracts(dispatch, user.token, setIsFetching, () => {})
      }
    }, 5000);
  }

  return (
    <HashRouter>
      <Switch>
        <Route exact path="/" render={() => <Redirect to="/app/newgroup" />} />
        <Route
          exact
          path="/app"
          render={() => <Redirect to="/app/newgroup" />}
        />
        <PrivateRoute path="/app" component={Layout} />
        <PublicRoute path="/login" component={Login} />
        <Route component={Error} />
      </Switch>
    </HashRouter>
  );

  // #######################################################################

  function PrivateRoute({ component, ...rest }) {
    return (
      <Route
        {...rest}
        render={props =>
          isAuthenticated ? (
            React.createElement(component, props)
          ) : (
            <Redirect
              to={{
                pathname: "/login",
                state: {
                  from: props.location,
                },
              }}
            />
          )
        }
      />
    );
  }

  function PublicRoute({ component, ...rest }) {
    return (
      <Route
        {...rest}
        render={props =>
          isAuthenticated ? (
            <Redirect
              to={{
                pathname: "/",
              }}
            />
          ) : (
            React.createElement(component, props)
          )
        }
      />
    );
  }
}
