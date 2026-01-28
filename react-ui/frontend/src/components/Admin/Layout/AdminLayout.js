import React, { Component } from 'react';
import AdminNav from './AdminNav';
import authService from '../../../services/admin/authService';
import './AdminLayout.css';

/**
 * Admin layout component with navigation and header
 */
class AdminLayout extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentUser: authService.getCurrentUser(),
      showLogoutConfirm: false,
    };
  }

  handleLogout = () => {
    this.setState({ showLogoutConfirm: true });
  };

  confirmLogout = async () => {
    await authService.logout();
    window.location.href = '/admin/login';
  };

  cancelLogout = () => {
    this.setState({ showLogoutConfirm: false });
  };

  render() {
    const { children } = this.props;
    const { currentUser, showLogoutConfirm } = this.state;

    return (
      <div className="admin-layout">
        <div className="admin-header">
          <div className="admin-header-left">
            <h1>YugaStore Admin Portal</h1>
          </div>
          <div className="admin-header-right">
            {currentUser && (
              <>
                <span className="user-info">
                  {currentUser.username}
                  <span className="user-role">{currentUser.role}</span>
                </span>
                <button
                  className="btn btn-sm btn-outline-light"
                  onClick={this.handleLogout}
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </div>

        <div className="admin-content">
          <AdminNav />
          <main className="admin-main">{children}</main>
        </div>

        {showLogoutConfirm && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h3>Confirm Logout</h3>
              <p>Are you sure you want to log out?</p>
              <div className="modal-actions">
                <button
                  className="btn btn-secondary"
                  onClick={this.cancelLogout}
                >
                  Cancel
                </button>
                <button
                  className="btn btn-primary"
                  onClick={this.confirmLogout}
                >
                  Logout
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }
}

export default AdminLayout;
