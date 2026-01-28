import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import authService from '../../../services/admin/authService';
import './AdminNav.css';

/**
 * Admin navigation sidebar
 */
class AdminNav extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentUser: authService.getCurrentUser(),
      activeRoute: window.location.pathname,
    };
  }

  isActive = (path) => {
    return this.state.activeRoute.startsWith(path);
  };

  render() {
    const { currentUser } = this.state;
    const role = currentUser?.role;

    return (
      <nav className="admin-nav">
        <ul className="nav-list">
          <li className={this.isActive('/admin/products') ? 'active' : ''}>
            <Link to="/admin/products">
              <span className="nav-icon">📦</span>
              Products
            </Link>
          </li>

          {role === 'ADMIN' && (
            <li className={this.isActive('/admin/audit') ? 'active' : ''}>
              <Link to="/admin/audit">
                <span className="nav-icon">📋</span>
                Audit Logs
              </Link>
            </li>
          )}

          <li className={this.isActive('/admin/reports') ? 'active' : ''}>
            <Link to="/admin/reports">
              <span className="nav-icon">📊</span>
              Reports
            </Link>
          </li>
        </ul>

        <div className="nav-footer">
          <div className="role-info">
            <strong>Access Level:</strong>
            <br />
            {role === 'ADMIN' && 'Full Access'}
            {role === 'EDITOR' && 'Create & Update'}
            {role === 'VIEWER' && 'View Only'}
          </div>
        </div>
      </nav>
    );
  }
}

export default AdminNav;
