import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import authService from '../../services/admin/authService';

/**
 * Private route component that requires authentication
 */
const PrivateRoute = ({ component: Component, ...rest }) => {
  return (
    <Route
      {...rest}
      render={(props) =>
        authService.isAuthenticated() ? (
          <Component {...props} />
        ) : (
          <Redirect
            to={{
              pathname: '/admin/login',
              state: { from: props.location },
            }}
          />
        )
      }
    />
  );
};

export default PrivateRoute;
