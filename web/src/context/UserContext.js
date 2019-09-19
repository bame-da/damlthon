import React from "react";
import uuidv4 from "uuid/v4";
import * as jwt from "jsonwebtoken";

var UserStateContext = React.createContext();
var UserDispatchContext = React.createContext();

function userReducer(state, action) {
  switch (action.type) {
    case "LOGIN_SUCCESS":
      return { ...state, isAuthenticated: true, token: action.token, user: action.user };
    case "LOGIN_FAILURE":
      return { ...state, isAuthenticated: false };
    case "SIGN_OUT_SUCCESS":
      return { ...state, isAuthenticated: false };
    default: {
      throw new Error(`Unhandled action type: ${action.type}`);
    }
  }
}

function UserProvider({ children }) {
  const token = localStorage.getItem("daml.token")
  const login = localStorage.getItem("daml.login")
  var [state, dispatch] = React.useReducer(userReducer, {
    isAuthenticated: !!token,
    token: token,
    login: login
  });

  return (
    <UserStateContext.Provider value={state}>
      <UserDispatchContext.Provider value={dispatch}>
        {children}
      </UserDispatchContext.Provider>
    </UserStateContext.Provider>
  );
}

function useUserState() {
  var context = React.useContext(UserStateContext);
  if (context === undefined) {
    throw new Error("useUserState must be used within a UserProvider");
  }
  return context;
}

function useUserDispatch() {
  var context = React.useContext(UserDispatchContext);
  if (context === undefined) {
    throw new Error("useUserDispatch must be used within a UserProvider");
  }
  return context;
}

export { UserProvider, useUserState, useUserDispatch, loginUser, signOut };

// ###########################################################

function loginUser(dispatch, login, password, history, setIsLoading, setError) {
  setError(false);
  setIsLoading(true);

  const ledgerId = "foobar";

  if (!!login) {
    const applicationId = uuidv4();
    const payload = { ledgerId, applicationId, party: login };
    const token = jwt.sign(payload, "secret");
    localStorage.setItem("daml.login", login);
    localStorage.setItem("daml.token", token);
    dispatch({ type: "LOGIN_SUCCESS", token, user: login });
    setError(null);
    setIsLoading(false);
    history.push("/app");
  } else {
    dispatch({ type: "LOGIN_FAILURE" });
    setError(true);
    setIsLoading(false);
  }
}

function signOut(dispatch, history) {
  localStorage.removeItem("daml.token");
  localStorage.removeItem("daml.login");
  dispatch({ type: "SIGN_OUT_SUCCESS" });
  history.push("/login");
}
