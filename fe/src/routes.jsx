import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useSelector } from 'react-redux';

// --- Shared Components ---
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/MainLayout';

// --- Public Pages ---
import HomePage from './pages/HomePage';
import AboutPage from './pages/AboutPage';
import RoomDetailPage from './pages/RoomDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';

// --- Tenant Pages (Sinh viên) ---
import TenantLayout from './components/TenantLayout';
import TenantDashboard from './pages/tenant/TenantDashboard';
import MyContracts from './pages/tenant/MyContracts';
import MyInvoices from './pages/tenant/MyInvoices';
import MaintenanceRequests from './pages/tenant/MaintenanceRequests';
import MyReservations from './pages/tenant/MyReservations';
import TenantServices from './pages/tenant/TenantServices';

// --- Partner Pages (Chủ trọ) ---
import PartnerLayout from './components/PartnerLayout';
import PartnerDashboard from './pages/partner/PartnerDashboard';
import MyListings from './pages/partner/MyListings';
import CreateListing from './pages/partner/CreateListing';
import EditListing from './pages/partner/EditListing';
import ServicePackages from './pages/partner/ServicePackages';
import PartnerPostsList from './pages/public/PartnerPostsList';
import PartnerPostDetail from './pages/public/PartnerPostDetail';
// --- Staff Pages (Internal) ---
import RoomManagement from './pages/staff/RoomManagement';
import BookingManagement from './pages/staff/BookingManagement';
import ContractCreation from './pages/staff/ContractCreation';
import ContractsManagement from './pages/staff/ContractsManagement';
import Inspection from './pages/staff/Inspection';
import PostModeration from './pages/staff/PostModeration';
import ManagerCleaningBookingsPage from './pages/staff/ManagerCleaningBookingsPage';

// --- Finance & Technical ---
import InvoiceManagementPage from './pages/staff/finance/InvoiceManagementPage';
import InvoiceGenerationPage from './pages/staff/finance/InvoiceGenerationPage';
import FinancialReportsPage from './pages/staff/finance/FinancialReportsPage';
import MeterReadingsPage from './pages/staff/finance/MeterReadingsPage';
import MaintenanceBoardPage from './pages/staff/maintenance/MaintenanceBoardPage';

// --- Admin & Director ---
import DirectorDashboardPage from './pages/admin/DirectorDashboardPage';
import UserEmployeeManagementPage from './pages/admin/UserEmployeeManagementPage';
import SystemConfigurationPage from './pages/admin/SystemConfigurationPage';
import AuditLogsPage from './pages/admin/AuditLogsPage';
import BranchManagementPage from './pages/admin/BranchManagementPage';

const appRoutes = [
  // ==============================
  // 1. PUBLIC ROUTES
  // ==============================
  { path: '/', element: <HomePage /> },
  { path: '/about', element: <AboutPage /> },
  { path: '/rooms/:id', element: <RoomDetailPage /> },
  { path: '/partner-posts', element: (
      <MainLayout>
        <div className="min-h-[60vh]">
          <PartnerPostsList />
        </div>
      </MainLayout>
    ) },
  { path: '/partner-posts/:id', element: <PartnerPostDetail /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  
  // ==============================
  // 2. USER SHARED ROUTES
  // ==============================
  { 
    path: '/profile', 
    element: (
      <ProtectedRoute>
        <MainLayout>
           <div className="container mx-auto px-6 py-10">
              <ProfilePage />
           </div>
        </MainLayout>
      </ProtectedRoute>
    ) 
  },

  // ==============================
  // 3. GUEST PORTAL (Khách vãng lai)
  // ==============================
  {
    path: '/guest',
    element: (
      <ProtectedRoute allowedRoles={['GUEST']}>
        <MainLayout>
            {/* Wrapper layout cho Guest */}
            <div className="min-h-[60vh]">
                <Outlet />
            </div>
        </MainLayout>
      </ProtectedRoute>
    ),
    children: [
        { 
            path: 'my-reservations', 
            element: (
                <div className="container mx-auto px-6 py-10">
                    <MyReservations isGuestView={true} /> 
                </div>
            )
        },
        // Fallback cho guest
        { path: '', element: <Navigate to="my-reservations" replace /> }
    ]
  },

  // ==============================
  // 4. TENANT PORTAL (Sinh viên thuê phòng)
  // ==============================
  {
    path: '/tenant',
    element: (
      <ProtectedRoute allowedRoles={['TENANT']}>
        <TenantLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: 'dashboard', element: <TenantDashboard /> },
      { path: 'contracts', element: <MyContracts /> },
      { path: 'services', element: <TenantServices /> },
      { path: 'invoices', element: <MyInvoices /> },
      { path: 'maintenance', element: <MaintenanceRequests /> },
      { path: 'reservations', element: <MyReservations /> },
      { path: '', element: <Navigate to="dashboard" replace /> }
    ]
  },

  // ==============================
  // 5. PARTNER PORTAL (Chủ trọ đối tác) - MỚI
  // ==============================
  {
    path: '/partner',
    element: (
      <ProtectedRoute allowedRoles={['PARTNER']}>
        <MainLayout>
          <PartnerLayout />
        </MainLayout>
      </ProtectedRoute>
    ),
    children: [
      { path: 'dashboard', element: <PartnerDashboard /> },
      { path: 'my-listings', element: <MyListings /> },
      { path: 'create-listing', element: <CreateListing /> },
      { path: 'edit-listing/:id', element: <EditListing /> },
      { path: 'packages', element: <ServicePackages /> },
      { path: '', element: <Navigate to="dashboard" replace /> }
    ]
  },

  // ==============================
  // 6. FALLBACK
  // ==============================
  { path: '*', element: <Navigate to='/' replace /> }
  ,
  // ==============================
  // 7. STAFF PORTAL (Internal)
  // ==============================
  {
    path: '/staff',
    element: (
      <ProtectedRoute allowedRoles={['ADMIN','DIRECTOR','MANAGER','RECEPTIONIST','ACCOUNTANT','MAINTENANCE','SECURITY']}> 
        <MainLayout>
          <div className="min-h-[60vh]">
            <Outlet />
          </div>
        </MainLayout>
      </ProtectedRoute>
    ),
    children: [
      {
        path: 'finance/invoices',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','DIRECTOR','MANAGER','ACCOUNTANT']}>
            <InvoiceManagementPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'finance/generate',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','DIRECTOR','ACCOUNTANT']}>
            <InvoiceGenerationPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'finance/meter-readings',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','MANAGER']}>
            <MeterReadingsPage />
          </ProtectedRoute>
        )
      },
      { path: 'finance/reports', element: <Navigate to='/staff/finance/invoices' replace /> },
      {
        path: 'maintenance/board',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','MAINTENANCE']}>
            <MaintenanceBoardPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'rooms',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','DIRECTOR','MANAGER','RECEPTIONIST','MAINTENANCE','SECURITY']}>
            <RoomManagement />
          </ProtectedRoute>
        )
      },
      {
        path: 'cleaning-bookings',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']}>
            <ManagerCleaningBookingsPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'bookings',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','RECEPTIONIST']}>
            <BookingManagement />
          </ProtectedRoute>
        )
      },
      {
        path: 'contracts',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','MANAGER','RECEPTIONIST']}>
            <ContractsManagement />
          </ProtectedRoute>
        )
      },
      {
        path: 'contracts/create',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','RECEPTIONIST']}>
            <ContractCreation />
          </ProtectedRoute>
        )
      },
      {
        path: 'inspection',
        element: (
          <ProtectedRoute allowedRoles={['MANAGER']}>
            <Inspection />
          </ProtectedRoute>
        )
      },
      {
        path: 'posts/moderation',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN','RECEPTIONIST']}>
            <PostModeration />
          </ProtectedRoute>
        )
      },
      {
        path: '',
        element: (
          <StaffIndexRedirect />
        )
      }
    ]
  }
  ,
  // ==============================
  // 8. ADMIN & DIRECTOR PORTAL
  // ==============================
  {
    path: '/admin',
    element: (
      <ProtectedRoute allowedRoles={['ADMIN','DIRECTOR']}>
        <MainLayout>
          <div className="min-h-[60vh]">
            <Outlet />
          </div>
        </MainLayout>
      </ProtectedRoute>
    ),
    children: [
      { path: 'dashboard', element: <DirectorDashboardPage /> },
      { path: 'branches', element: <BranchManagementPage /> },
      { path: 'users', element: <UserEmployeeManagementPage /> },
      { path: 'config', element: <SystemConfigurationPage /> },
      { path: 'audit-logs', element: <AuditLogsPage /> },
      { path: '', element: <Navigate to='dashboard' replace /> }
    ]
  }
];

function StaffIndexRedirect() {
  const role = useSelector((s) => s?.auth?.user?.role);
  if (role === 'ACCOUNTANT') return <Navigate to="finance/invoices" replace />;
  return <Navigate to="rooms" replace />;
}

export default appRoutes;