

import { TimesheetStatus, type EmployeeInfo } from './common.types';

// Timesheet Entry
export interface TimesheetEntryResponse {
  id: number;
  workDate: string;
  hoursWorked: number;
  description?: string;
}

export interface TimesheetEntryRequest {
  workDate: string;
  hoursWorked: number;
  description?: string;
}

// Timesheet
export interface TimesheetResponse {
  id: number;
  employee: EmployeeInfo;
  weekStartDate: string;
  weekEndDate: string;
  totalHours: number;
  status: TimesheetStatus;
  submittedAt?: string;
  reviewedAt?: string;
  reviewedBy?: ReviewerInfo;
  denialReason?: string;
  entries: TimesheetEntryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ReviewerInfo {
  id: number;
  name: string;
}

// Timesheet List (simplified)
export interface TimesheetListResponse {
  id: number;
  employeeName: string;
  employeeId: string;
  weekStartDate: string;
  weekEndDate: string;
  totalHours: number;
  status: TimesheetStatus;
  submittedAt?: string;
}

// Update Timesheet
export interface UpdateTimesheetRequest {
  entries: TimesheetEntryRequest[];
}

// Deny Timesheet
export interface DenyTimesheetRequest {
  reason: string;
}