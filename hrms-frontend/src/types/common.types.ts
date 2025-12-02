

export const Role = {
  EMPLOYEE: 'EMPLOYEE',
  MANAGER: 'MANAGER',
  ADMIN: 'ADMIN',
} as const;
export type Role = (typeof Role)[keyof typeof Role];


export const TimesheetStatus = {
  DRAFT: 'DRAFT',
  SUBMITTED: 'SUBMITTED',
  APPROVED: 'APPROVED',
  DENIED: 'DENIED',
} as const;
export type TimesheetStatus = (typeof TimesheetStatus)[keyof typeof TimesheetStatus];


export const PayrollStatus = {
  PREVIEW: 'PREVIEW',
  PROCESSED: 'PROCESSED',
  PAID: 'PAID',
} as const;
export type PayrollStatus = (typeof PayrollStatus)[keyof typeof PayrollStatus];


export const PayFrequency = {
  WEEKLY: 'WEEKLY',
  BI_WEEKLY: 'BI_WEEKLY',
  SEMI_MONTHLY: 'SEMI_MONTHLY',
  MONTHLY: 'MONTHLY',
} as const;
export type PayFrequency = (typeof PayFrequency)[keyof typeof PayFrequency];


export const PaymentMethod = {
  BANK_TRANSFER: 'BANK_TRANSFER',
  CHECK: 'CHECK',
  CASH: 'CASH',
  DIGITAL_WALLET: 'DIGITAL_WALLET',
} as const;
export type PaymentMethod = (typeof PaymentMethod)[keyof typeof PaymentMethod];

// Pagination
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Common Response
export interface MessageResponse<T = unknown>{
  message: string;
  timestamp?: string;
  data?: T;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

// Base Employee Info
export interface EmployeeInfo {
  id: number;
  employeeId: string;
  name: string;
  email: string;
}

export interface ManagerInfo {
  id: number;
  employeeId: string;
  name: string;
  email: string;
}

// Address
export interface Address {
  line1?: string;
  line2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}