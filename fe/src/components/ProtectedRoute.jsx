import React from 'react';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { user } = useSelector((state) => state.auth);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Logic kiểm tra quyền
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    // If a user is logged in but lacks permission for this route,
    // send them to the most relevant portal entry.
    const role = String(user?.role || '').toUpperCase();
    const employeeRoles = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY'];
    if (employeeRoles.includes(role)) return <Navigate to="/staff" replace />;
    if (role === 'TENANT') return <Navigate to="/tenant" replace />;
    if (role === 'PARTNER') return <Navigate to="/partner" replace />;
    if (role === 'GUEST') return <Navigate to="/guest" replace />;
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;