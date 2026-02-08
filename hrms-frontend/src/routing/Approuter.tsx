import { createBrowserRouter, Navigate } from "react-router-dom";
import { ProtectedRoute, RoleGuard, GuestRoute } from "@/components/auth";
import { DashboardLayout } from "@/components/layout";
import { Role } from "@/types";

// Landing Page
import { LandingPage } from "@/pages/landing";

// Auth Pages
import { LoginPage, ResetPasswordPage } from "@/pages/auth";

// Dashboard
import { DashboardPage } from "@/pages/dashboard";

// Profile
import { ProfilePage, PersonalInfoPage, ContactInfoPage, PayInfoPage } from "@/pages/profile";

// Timesheet
import { TimesheetPage, CurrentTimesheetPage, TimesheetHistoryPage, TimesheetDetailPage } from "@/pages/timesheet";

// Payroll (Employee)
import { PayrollPage, PayrollHistoryPage, PayrollDetailPage } from "@/pages/payroll";

// Manager
import { DirectReportsPage, TeamTimesheetsPage, ManagerStatisticsPage } from "@/pages/manager";

// Admin
import {
  EmployeeListPage,
  EmployeeDetailPage,
  CreateEmployeePage,
  PayrollManagementPage,
  RunPayrollPage,
  AdminPayrollHistoryPage,
} from "@/pages/admin";

export const router = createBrowserRouter([

  // Public Routes

  {
    path: "/",
    element: (
      <GuestRoute>
        <LandingPage />
      </GuestRoute>
    ),
  },
  {
    path: "/login",
    element: (
      <GuestRoute>
        <LoginPage />
      </GuestRoute>
    ),
  },

  
  // Protected Dashboard Layout
  
  {
    element: (
      <ProtectedRoute>
        <DashboardLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: "/dashboard", element: <DashboardPage /> },

      // Reset Password
      { path: "/reset-password", element: <ResetPasswordPage /> },

      
      // Profile
      
      {
        path: "/profile",
        element: <ProfilePage />,
        children: [
          { index: true, element: <PersonalInfoPage /> },
          { path: "contact", element: <ContactInfoPage /> },
          { path: "pay", element: <PayInfoPage /> },
        ],
      },

      
      // Timesheets
      
      {
        path: "/timesheets",
        element: <TimesheetPage />,
        children: [
          { index: true, element: <CurrentTimesheetPage /> },
          { path: "history", element: <TimesheetHistoryPage /> },
        ],
      },
      { path: "/timesheets/:id", element: <TimesheetDetailPage /> },

      
      // Payroll (Employee)
      
      {
        path: "/payroll",
        element: <PayrollPage />,
        children: [{ index: true, element: <PayrollHistoryPage /> }],
      },
      { path: "/payroll/:id", element: <PayrollDetailPage /> },

      
      // Manager
      
      {
        path: "/manager/team",
        element: (
          <RoleGuard allowedRoles={[Role.MANAGER, Role.ADMIN]}>
            <DirectReportsPage />
          </RoleGuard>
        ),
      },
      {
        path: "/manager/timesheets",
        element: (
          <RoleGuard allowedRoles={[Role.MANAGER, Role.ADMIN]}>
            <TeamTimesheetsPage />
          </RoleGuard>
        ),
      },
      {
        path: "/manager/statistics",
        element: (
          <RoleGuard allowedRoles={[Role.MANAGER, Role.ADMIN]}>
            <ManagerStatisticsPage />
          </RoleGuard>
        ),
      },

      
      // Admin Employee Management
      
      {
        path: "/admin/employees",
        element: (
          <RoleGuard allowedRoles={[Role.ADMIN]}>
            <EmployeeListPage />
          </RoleGuard>
        ),
      },
      {
        path: "/admin/employees/create",
        element: (
          <RoleGuard allowedRoles={[Role.ADMIN]}>
            <CreateEmployeePage />
          </RoleGuard>
        ),
      },
      {
        path: "/admin/employees/:id",
        element: (
          <RoleGuard allowedRoles={[Role.ADMIN]}>
            <EmployeeDetailPage />
          </RoleGuard>
        ),
      },

      
      // Admin Payroll
      
      {
        path: "/admin/payroll",
        element: (
          <RoleGuard allowedRoles={[Role.ADMIN]}>
            <PayrollManagementPage />
          </RoleGuard>
        ),
        children: [
          { index: true, element: <RunPayrollPage /> },
          { path: "history", element: <AdminPayrollHistoryPage /> },
        ],
      },
    ],
  },


  // Catch-all redirect
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);
