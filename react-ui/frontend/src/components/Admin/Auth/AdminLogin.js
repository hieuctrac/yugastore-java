import React, { Component } from 'react';
import authService from '../../../services/admin/authService';
import './AdminLogin.css';

/**
 * Admin login component
 */
class AdminLogin extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: '',
      password: '',
      error: null,
      loading: false,
    };
  }

  componentDidMount() {
    // Redirect if already logged in
    if (authService.isAuthenticated()) {
      this.props.history.push('/admin/products');
    }
  }

  handleChange = (e) => {
    this.setState({
      [e.target.name]: e.target.value,
      error: null,
    });
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    const { username, password } = this.state;

    // Validation
    if (!username || !password) {
      this.setState({ error: 'Please enter both username and password' });
      return;
    }

    this.setState({ loading: true, error: null });

    try {
      await authService.login(username, password);
      // Redirect to products page on success
      this.props.history.push('/admin/products');
    } catch (error) {
      console.error('Login error:', error);
      const errorMessage =
        error.response?.data?.message ||
        'Invalid username or password. Please try again.';
      this.setState({ error: errorMessage, loading: false });
    }
  };

  render() {
    const { username, password, error, loading } = this.state;

    return (
      <div className="admin-login-container">
        <div className="admin-login-box">
          <h2>Admin Portal</h2>
          <p className="login-subtitle">Sign in to manage products</p>

          {error && <div className="alert alert-danger">{error}</div>}

          <form onSubmit={this.handleSubmit}>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                name="username"
                className="form-control"
                value={username}
                onChange={this.handleChange}
                disabled={loading}
                autoFocus
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                name="password"
                className="form-control"
                value={password}
                onChange={this.handleChange}
                disabled={loading}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-block"
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="login-footer">
            <a href="/admin/auth/password-reset">Forgot password?</a>
          </div>
        </div>
      </div>
    );
  }
}

export default AdminLogin;
