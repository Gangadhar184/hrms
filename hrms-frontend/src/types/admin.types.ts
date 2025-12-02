/**
 * Admin Types
 */

import { Role, PayFrequency, PaymentMethod } from './common.types';

// Create Employee
export interface PayInfoRequest {
  salary: number;
  hourlyRate?: number;
  payFrequency: PayFrequency;
  paymentMethod: PaymentMethod;
  bankName?: string;
  accountNumber?: string;
  routingNumber?: string;
  taxId?: string;
}

export interface CreateEmployeeRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  hireDate: string;
  role: Role;
  managerId?: number;
  payInfo?: PayInfoRequest;
}

export interface CreateEmployeeResponse {
  message: string;
  employeeId: string;
  temporaryPassword: string;
  id: number;
}

// Update Employee
export interface UpdateEmployeePersonalInfoRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  email: string;
  managerId?: number;
  isActive: boolean;
}

export interface UpdatePayInfoRequest {
  salary: number;
  hourlyRate?: number;
  payFrequency: PayFrequency;
  paymentMethod: PaymentMethod;
  bankName?: string;
  accountNumber?: string;
  routingNumber?: string;
  taxId?: string;
}

// Employee Search Params
export interface EmployeeSearchParams {
  search?: string;
  role?: Role;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

// Statistics
export interface EmployeeStatistics {
  totalEmployees: number;
  totalAdmins?: number;
  totalManagers?: number;
  totalRegularEmployees?: number;
}