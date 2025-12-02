/**
 * Payroll Types
 */

import { PayrollStatus, type EmployeeInfo } from './common.types';

// Payroll Response
export interface PayrollResponse {
  id: number;
  employee: EmployeeInfo;
  payPeriodStart: string;
  payPeriodEnd: string;
  grossPay: number;
  netPay: number;
  taxDeduction: number;
  otherDeductions: number;
  bonus: number;
  status: PayrollStatus;
  processedAt?: string;
  processedBy?: string;
  paymentDate?: string;
  createdAt: string;
}

// Payroll Preview
export interface PayrollPreviewResponse {
  payPeriod: PayPeriodInfo;
  employees: EmployeePayrollInfo[];
  totalGrossPay: number;
  totalNetPay: number;
  employeeCount: number;
}

export interface PayPeriodInfo {
  startDate: string;
  endDate: string;
}

export interface EmployeePayrollInfo {
  employeeId: string;
  name: string;
  hoursWorked: number;
  grossPay: number;
  taxDeduction: number;
  otherDeductions: number;
  bonus: number;
  netPay: number;
}

// Run Payroll
export interface RunPayrollRequest {
  weekStartDate: string;
  paymentDate: string;
}

export interface RunPayrollResponse {
  message: string;
  processedCount: number;
  totalAmount: number;
  processedAt: string;
}

// Current Week Status
export interface PayrollWeekStatus {
  weekStartDate: string;
  weekEndDate: string;
  employeeCount: number;
  totalAmount?: number;
  processed: boolean;
  message?: string;
}