import React from "react";
import { Route, Redirect, RouteProps } from "react-router-dom";

interface PrivateRouteProps extends RouteProps {
  component: any;
}

const PrivateRoute = (props: PrivateRouteProps) => {
  let currentSession = sessionStorage.getItem("currentPlanet");

  const { component: Component, ...rest } = props;

  return (
    <Route
      {...rest}
      render={(props: any) => {
        if (currentSession != null) {
          return React.createElement(Component, props);
        } else {
          return (
            <Redirect to={{ pathname: "/", state: { from: props.location } }} />
          );
        }
      }}
    />
  );
};

export default PrivateRoute;
