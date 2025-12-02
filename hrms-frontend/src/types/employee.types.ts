import { Role, type ManagerInfo, type Address, PayFrequency, TimesheetStatus } from "./common.types";

// Employee Response
export interface EmployeeResponse {
  id: number;
  employeeId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  hireDate: string;
  role: Role;
  manager?: ManagerInfo;
  isFirstLogin: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Employee List Response (simplified)
export interface EmployeeListResponse {
  id: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  hireDate: string;
  isActive: boolean;
  managerName?: string;
}

// Contact Info
export interface ContactInfoResponse {
  id: number;
  phoneNumber?: string;
  mobileNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  address: Address;
  updatedAt: string;
}

export interface UpdateContactInfoRequest {
  phoneNumber?: string;
  mobileNumber?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

// Pay Info
export interface PayInfoResponse {
  id: number;
  salary: number;
  hourlyRate?: number;
  payFrequency: PayFrequency;
  paymentMethod: PaymentMethodData;
  bankName?: string;
  maskedAccountNumber?: string;
  lastPayDate?: string;
  nextPayDate?: string;
  updatedAt: string;
}

// Dashboard
export interface DashboardResponse {
  personalInfo: EmployeeResponse;
  recentActivity?: RecentActivityItem[];
  stats: DashboardStats;
}

export interface RecentActivityItem {
  action: string;
  description: string;
  timestamp: string;
}

export interface DashboardStats {
  directReportsCount?: number;
  pendingTimesheets?: number;
  currentTimesheetStatus?: TimesheetStatus;
}