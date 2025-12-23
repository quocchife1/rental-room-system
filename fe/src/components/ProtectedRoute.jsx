import React from 'react';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { user } = useSelector((state) => state.auth);

  // Debug: Xem log này trong Console của trình duyệt (F12)
  console.log("Current User Role:", user?.role);
  console.log("Allowed Roles:", allowedRoles);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Logic kiểm tra quyền
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    console.warn("Access Denied: Role mismatch");
    return <Navigate to="/" replace />; 
  }

  return children;
};

export default ProtectedRoute;